package com.example.carteiravirtual.controller

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.carteiravirtual.R
import com.example.carteiravirtual.model.Moeda
import com.example.carteiravirtual.model.TipoMoeda
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ConverterRecursos : AppCompatActivity() {
    private lateinit var spinnerOrigem: Spinner
    private lateinit var spinnerDestino: Spinner
    private lateinit var textInputLayoutValor: TextInputLayout
    private lateinit var editTextValorOrigem: TextInputEditText
    private lateinit var buttonConverter: Button
    private lateinit var buttonComprar: Button
    private lateinit var buttonVoltar: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewResultado: TextView

    private lateinit var carteira: MutableMap<TipoMoeda, Moeda>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_converter_recursos)

        val extras = intent.extras
        if (extras != null && extras.containsKey("CARTEIRA_EXTRA")) {
            carteira = intent.getSerializableExtra("CARTEIRA_EXTRA") as? MutableMap<TipoMoeda, Moeda> ?: carregarCarteiraPadrao()

        } else {
            Toast.makeText(this, "Erro ao carregar carteira. Usando dados padrão.", Toast.LENGTH_LONG).show()
            carteira = carregarCarteiraPadrao()
        }

        spinnerOrigem = findViewById(R.id.spinnerOrigem)
        spinnerDestino = findViewById(R.id.spinnerDestino)
        textInputLayoutValor = findViewById(R.id.textInputLayoutValor)
        editTextValorOrigem = findViewById(R.id.editTextValorOrigem)
        buttonConverter = findViewById(R.id.buttonConverter)
        buttonVoltar = findViewById(R.id.buttonVoltar)
        buttonComprar = findViewById(R.id.buttonComprar)
        progressBar = findViewById(R.id.progressBar)
        textViewResultado = findViewById(R.id.textViewResultado)

        setupSpinners()
        setupListeners()
    }

    private fun carregarCarteiraPadrao(): MutableMap<TipoMoeda, Moeda> {
        return mutableMapOf(
            TipoMoeda.BRL to Moeda(id = 1, saldo = 100000.0, tipo = TipoMoeda.BRL),
            TipoMoeda.USD to Moeda(id = 2, saldo = 50000.0, tipo = TipoMoeda.USD),
            TipoMoeda.BTC to Moeda(id = 3, saldo = 0.5, tipo = TipoMoeda.BTC),
        )
    }

    private fun setupSpinners() {
        val moedas = TipoMoeda.entries.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, moedas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerOrigem.adapter = adapter
        spinnerDestino.adapter = adapter

        spinnerDestino.setSelection(1)
    }

    @SuppressLint("SetTextI18n")
    private fun setupListeners() {
        buttonConverter.setOnClickListener {
            val origemStr = spinnerOrigem.selectedItem.toString()
            val destinoStr = spinnerDestino.selectedItem.toString()
            val valorStr = editTextValorOrigem.text.toString()

            if (origemStr == destinoStr) {
                Toast.makeText(this, "As moedas de origem e destino não podem ser iguais.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (valorStr.isBlank()) {
                textInputLayoutValor.error = "Insira um valor."
                return@setOnClickListener
            }

            textInputLayoutValor.error = null

            lifecycleScope.launch(Dispatchers.Main) {
                progressBar.visibility = View.VISIBLE
                buttonConverter.isEnabled = false
                buttonComprar.isEnabled = false
                textViewResultado.text = ""

                try {
                    val valorOrigem = valorStr.toDouble()
                    if (valorOrigem <= 0) {
                        throw NumberFormatException("O valor deve ser positivo.")
                    }

                    val moedaOrigem = TipoMoeda.valueOf(origemStr)
                    val moedaDestino = TipoMoeda.valueOf(destinoStr)

                    val saldoOrigemAtual = carteira[moedaOrigem]?.saldo ?: 0.0
                    if (valorOrigem > saldoOrigemAtual) {
                        throw Exception("Saldo insuficiente na carteira de ${moedaOrigem.name}.")
                    }

                    val taxa = withContext(Dispatchers.IO) {
                        getTaxa(moedaOrigem, moedaDestino)
                    }

                    val valorDestino = valorOrigem * taxa

                    val saldoDestinoAtual = carteira[moedaDestino]?.saldo ?: 0.0

                    carteira[moedaOrigem] = carteira[moedaOrigem]!!.copy(saldo = saldoOrigemAtual - valorOrigem)
                    carteira[moedaDestino] = carteira[moedaDestino]!!.copy(saldo = saldoDestinoAtual + valorDestino)

                    atualizarCarteira()

                    textViewResultado.text = "Convertido: ${formatarValor(valorDestino, moedaDestino)}"
                    editTextValorOrigem.text?.clear()

                } catch(e: NumberFormatException) {
                    Toast.makeText(this@ConverterRecursos, e.message ?: "Valor inválido.", Toast.LENGTH_LONG).show()
                } catch(e: Exception) {
                    Toast.makeText(this@ConverterRecursos, e.message, Toast.LENGTH_LONG).show()
                } finally {
                    progressBar.visibility = View.GONE
                    buttonConverter.isEnabled = true
                    buttonComprar.isEnabled = true
                }
            }
        }

        buttonComprar.setOnClickListener {
            val origemStr = spinnerOrigem.selectedItem.toString()
            val destinoStr = spinnerDestino.selectedItem.toString()
            val valorStr = editTextValorOrigem.text.toString()

            if (origemStr == destinoStr) {
                Toast.makeText(this, "As moedas de origem e destino não podem ser iguais.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (valorStr.isBlank()) {
                textInputLayoutValor.error = "Insira um valor para comprar."
                return@setOnClickListener
            }

            textInputLayoutValor.error = null

            lifecycleScope.launch(Dispatchers.Main) {
                progressBar.visibility = View.VISIBLE
                buttonConverter.isEnabled = false
                buttonComprar.isEnabled = false
                textViewResultado.text = ""

                try {
                    val valorOrigem = valorStr.toDouble()
                    if (valorOrigem <= 0) {
                        throw NumberFormatException("O valor da compra deve ser positivo.")
                    }

                    val moedaOrigem = TipoMoeda.valueOf(origemStr)
                    val moedaDestino = TipoMoeda.valueOf(destinoStr)

                    val taxa = withContext(Dispatchers.IO) {
                        getTaxa(moedaOrigem, moedaDestino)
                    }

                    val valorDestino = valorOrigem * taxa

                    val saldoDestinoAtual = carteira[moedaDestino]?.saldo ?: 0.0

                    carteira[moedaDestino] = carteira[moedaDestino]!!.copy(saldo = saldoDestinoAtual + valorDestino)

                    atualizarCarteira()

                    textViewResultado.text = "Comprado: ${formatarValor(valorDestino, moedaDestino)}"
                    editTextValorOrigem.text?.clear()
                } catch (e: NumberFormatException) {
                    Toast.makeText(this@ConverterRecursos, e.message ?: "Valor inválido.", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@ConverterRecursos, e.message, Toast.LENGTH_LONG).show()
                } finally {
                    progressBar.visibility = View.GONE
                    buttonConverter.isEnabled = true
                    buttonComprar.isEnabled = true
                }
            }
        }

        buttonVoltar.setOnClickListener {
            finish()
        }
    }

    private fun getTaxa(moedaOrigem: TipoMoeda, moedaDestino: TipoMoeda): Double {
        if (moedaOrigem == moedaDestino) return 1.0

        if (moedaDestino == TipoMoeda.BTC) {
            val parInvertido = "${moedaDestino.name}-${moedaOrigem.name}"
            val taxaInvertida = chamarApiDeTaxa(parInvertido, moedaDestino, moedaOrigem)

            if (taxaInvertida == 0.0) {
                throw Exception("Taxa de câmbio invertida retornou 0.")
            }
            return 1.0 / taxaInvertida
        }

        val parDireto = "${moedaOrigem.name}-${moedaDestino.name}"
        return chamarApiDeTaxa(parDireto, moedaOrigem, moedaDestino)
    }

    private fun chamarApiDeTaxa(par: String, origemApi: TipoMoeda, destinoApi: TipoMoeda): Double {
        val urlString = "https://economia.awesomeapi.com.br/json/last/$par"

        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        try {
            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use {it.readText()}

            val chaveJson = "${origemApi.name}${destinoApi.name}"
            val jsonReponse = JSONObject(response)
            val parJson = jsonReponse.getJSONObject(chaveJson)

            val taxaStr = parJson.getString("bid")
            return taxaStr.toDouble()
        } catch(e: Exception) {
            throw Exception("Não foi possível obter a taxa de câmbio para $par: ${e.message}")
        } finally {
            connection.disconnect()
        }
    }

    private fun formatarValor(valor: Double, moeda: TipoMoeda): String {
        return when (moeda) {
            TipoMoeda.BRL -> "R$ ${"%.2f".format(valor)}"
            TipoMoeda.USD -> "$ ${"%.2f".format(valor)}"
            TipoMoeda.BTC -> "₿ ${"%.5f".format(valor)}"
        }
    }

    private fun atualizarCarteira() {
        val intentResultado = Intent()

        intentResultado.putExtra("CARTEIRA_ATUALIZADA_EXTRA", carteira as HashMap)
        setResult(RESULT_OK, intentResultado)
    }
}