package com.example.carteiravirtual.model

data class CotacaoResponse(
    //Resposta da cotação da Awesome API
    val success: Boolean,
    val data: Map<String, Cotacao>,
)

data class Cotacao(
    val code: String, //Moeda
    val codein: String, //Moeda de conversão
    val name: String, //Nome da moeda
    val high: Double, //Maior cotação
    val low: Double, //Menor cotação
    val varBid: Double, //Variação da cotação
    val pctChange: Double, //Variação percentual da cotação
    val bid: Double, //Cotação atual
    val ask: Double, //Cotação atual
    val timestamp: Long, //Timestamp da cotação
    val create_date: String, //Data da cotação
)
