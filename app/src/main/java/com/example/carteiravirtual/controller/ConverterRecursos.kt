package com.example.carteiravirtual.controller

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
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewResultado: TextView

    private var carteira = mutableMapOf(
        TipoMoeda.BRL to Moeda(id = 1, saldo = 100000.0, tipo = TipoMoeda.BRL),
        TipoMoeda.USD to Moeda(id = 2, saldo = 500000.0, tipo = TipoMoeda.USD),
        TipoMoeda.BTC to Moeda(id = 3, saldo = 0.5, tipo = TipoMoeda.BTC)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_converter_recursos)

        spinnerOrigem = findViewById(R.id.spinnerOrigem)
        spinnerDestino = findViewById(R.id.spinnerDestino)
        textInputLayoutValor = findViewById(R.id.textInputLayoutValor)
        editTextValorOrigem = findViewById(R.id.editTextValorOrigem)
        buttonConverter = findViewById(R.id.buttonConverter)
        progressBar = findViewById(R.id.progressBar)
        textViewResultado = findViewById(R.id.textViewResultado)

        setupSpinners()
        setupListeners()
    }

    private fun setupSpinners() {
        val moedas = TipoMoeda.entries.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, moedas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerOrigem.adapter = adapter
        spinnerDestino.adapter = adapter

        spinnerDestino.setSelection(1)
    }

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

                    textViewResultado.text = "Convertido: ${formatarValor(valorDestino, moedaDestino)}"
                    editTextValorOrigem.text?.clear()

                } catch(e: NumberFormatException) {
                    Toast.makeText(this@ConverterRecursos, e.message ?: "Valor inválido.", Toast.LENGTH_LONG).show()
                } catch(e: Exception) {
                    Toast.makeText(this@ConverterRecursos, e.message, Toast.LENGTH_LONG).show()
                } finally {
                    progressBar.visibility = View.GONE
                    buttonConverter.isEnabled = true
                }
            }
        }
    }

    private suspend fun getTaxa(moedaOrigem: TipoMoeda, moedaDestino: TipoMoeda): Double {
        if (moedaOrigem == moedaDestino) return 1.0

        val par = "${moedaOrigem.name}-${moedaDestino.name}"
        val urlString = "https://economia.awesomeapi.com.br/json/last/$par"

        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        try {
            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use {it.readText()}

            val chaveJson = "${moedaOrigem.name}${moedaDestino.name}"
            val jsonResponse = JSONObject(response)
            val parJson = jsonResponse.getJSONObject(chaveJson)

            val taxaStr = parJson.getString("bid")
            return taxaStr.toDouble()
        } catch(e: Exception) {
            throw Exception("Não foi possível obter a taxa de câmbio para $par")
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
}