package com.example.carteiravirtual.controller

import com.example.carteiravirtual.model.Moeda
import retrofit2.http.GET

interface AwesomeAPI {
    //GET dolar para real
    @GET("json/last/USD-BRL")
    suspend fun getDolarReal(): Moeda

    //GET dolar para BTC
    @GET("json/last/BTC-USD")
    suspend fun getDolarBTC(): Moeda

    //GET real para dólar
    @GET("json/last/USD-BRL")
    suspend fun getRealDolar(): Moeda

    //GET real para BTC
    @GET("json/last/BTC-BRL")
    suspend fun getRealBTC(): Moeda

    //GET BTC para real
    @GET("json/last/BTC-BRL")
    suspend fun getBTCReal(): Moeda

    //GET BTC para dólar
    @GET("json/last/BTC-USD")
    suspend fun getBTCDolar(): Moeda
}


