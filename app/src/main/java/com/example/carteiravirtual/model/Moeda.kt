package com.example.carteiravirtual.model

import java.io.Serializable

data class Moeda(
    val id: Int = 0,
    val saldo: Double,
    val tipo: TipoMoeda
): Serializable