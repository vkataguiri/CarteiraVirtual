package com.example.carteiravirtual.controller

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.carteiravirtual.R
import com.example.carteiravirtual.model.Moeda
import com.example.carteiravirtual.model.TipoMoeda
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tvSaldoReal: TextView
    private lateinit var tvSaldoDolar: TextView
    private lateinit var tvSaldoBitcoin: TextView
    private lateinit var btnConverter: Button

    // Dados de exemplo
    private var carteira: MutableMap<TipoMoeda, Moeda> = mutableMapOf(
        TipoMoeda.BRL to Moeda(saldo = 100000.0, tipo = TipoMoeda.BRL),
        TipoMoeda.USD to Moeda(saldo = 50000.0, tipo = TipoMoeda.USD),
        TipoMoeda.BTC to Moeda(saldo = 0.5, tipo = TipoMoeda.BTC)
    )

    private val conversaoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val dadosRetornados = result.data
            if (dadosRetornados != null && dadosRetornados.hasExtra("CARTEIRA_ATUALIZADA_EXTRA")) {
                val carteiraAtualizada = dadosRetornados.getSerializableExtra("CARTEIRA_ATUALIZADA_EXTRA") as? MutableMap<TipoMoeda, Moeda>

                if (carteiraAtualizada != null) {
                    this.carteira = carteiraAtualizada
                    atualizarValores()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        tvSaldoReal = findViewById(R.id.tvSaldoReal)
        tvSaldoDolar = findViewById(R.id.tvSaldoDolar)
        tvSaldoBitcoin = findViewById(R.id.tvSaldoBitcoin)
        btnConverter = findViewById(R.id.btnConverter)

        atualizarValores()

        btnConverter.setOnClickListener {
            val intent = Intent(this, ConverterRecursos::class.java)

            intent.putExtra("CARTEIRA_EXTRA", carteira as HashMap)
            conversaoLauncher.launch(intent)
        }
    }

    private fun atualizarValores() {
        carteira.values.forEach { moeda ->
            when (moeda.tipo) {
                TipoMoeda.BRL -> {
                    val formato = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                    tvSaldoReal.text = formato.format(moeda.saldo)
                }
                TipoMoeda.USD -> {
                    val formato = NumberFormat.getCurrencyInstance(Locale.US)
                    tvSaldoDolar.text = formato.format(moeda.saldo)
                }
                TipoMoeda.BTC -> {
                    tvSaldoBitcoin.text = "%.5f BTC".format(moeda.saldo)
                }
            }
        }
    }
}