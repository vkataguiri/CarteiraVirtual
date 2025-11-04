package com.example.carteiravirtual.controller

import com.example.carteiravirtual.model.Cotacao
import com.example.carteiravirtual.model.CotacaoResponse
import com.example.carteiravirtual.model.Moeda
import retrofit2.http.GET

interface AwesomeAPI {
    //GET dolar para real
    @GET("json/last/USD-BRL")
    suspend fun getDolarReal(): CotacaoResponse

    //GET dolar para BTC
    @GET("json/last/BTC-USD")
    suspend fun getDolarBTC(): CotacaoResponse

    //GET real para dólar
    @GET("json/last/USD-BRL")
    suspend fun getRealDolar(): CotacaoResponse

    //GET real para BTC
    @GET("json/last/BTC-BRL")
    suspend fun getRealBTC(): CotacaoResponse

    //GET BTC para real
    @GET("json/last/BTC-BRL")
    suspend fun getBTCReal(): CotacaoResponse

    //GET BTC para dólar
    @GET("json/last/BTC-USD")
    suspend fun getBTCDolar(): CotacaoResponse
}


