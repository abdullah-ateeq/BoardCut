package com.example.boardcut.model

import java.util.UUID

data class CutPiece(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "",
    val length: Double = 0.0,
    val quantity: Int = 1
) {
    fun lengthInMm(unit: UnitOfMeasurement): Double = unit.toMm(length)
}
