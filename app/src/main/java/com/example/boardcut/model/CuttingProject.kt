package com.example.boardcut.model

import java.util.UUID

data class CuttingProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "My Project",
    val createdAt: Long = System.currentTimeMillis(),
    val unit: UnitOfMeasurement = UnitOfMeasurement.MM,
    val stockBoards: List<StockBoard> = listOf(StockBoard()),
    val requiredPieces: List<CutPiece> = emptyList(),
    val settings: CalculationSettings = CalculationSettings()
)
