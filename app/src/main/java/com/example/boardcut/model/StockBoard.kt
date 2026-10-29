package com.example.boardcut.model

import java.util.UUID

data class StockBoard(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Stock Board",
    val length: Double = 2440.0, // Length in current unit
    val width: Double? = null,
    val thickness: Double? = null,
    val quantity: Int? = null // null means unlimited stock available
) {
    fun lengthInMm(unit: UnitOfMeasurement): Double = unit.toMm(length)
}
