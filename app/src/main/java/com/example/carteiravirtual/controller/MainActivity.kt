package com.example.carteiravirtual.controller

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
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
    private val carteira = listOf(
        Moeda(saldo = 100000.0, tipo = TipoMoeda.BRL),
        Moeda(saldo = 50000.0, tipo = TipoMoeda.USD),
        Moeda(saldo = 0.5, tipo = TipoMoeda.BTC)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        tvSaldoReal = findViewById(R.id.tvSaldoReal)
        tvSaldoDolar = findViewById(R.id.tvSaldoDolar)
        tvSaldoBitcoin = findViewById(R.id.tvSaldoBitcoin)
        btnConverter = findViewById(R.id.btnConverter)

        atualizarValores(carteira)

        btnConverter.setOnClickListener {
            val intent = Intent(this, ConverterRecursosActivity::class.java)
            startActivity(intent)
        }
    }

    private fun atualizarValores(carteira: List<Moeda>) {
        carteira.forEach { moeda ->
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
                    // Formato específico para Bitcoin
                    tvSaldoBitcoin.text = "%.4f BTC".format(moeda.saldo)
                }
            }
        }
    }
}