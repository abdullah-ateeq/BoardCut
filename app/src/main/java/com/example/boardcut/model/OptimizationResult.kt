package com.example.boardcut.model

data class PlacedPiece(
    val pieceId: String,
    val label: String,
    val lengthMm: Double,
    val startPositionMm: Double,
    val endPositionMm: Double,
    val cutSequenceNumber: Int
)

data class BoardCutPlan(
    val boardIndex: Int,
    val stockBoard: StockBoard,
    val totalStockLengthMm: Double,
    val trimAllowanceMm: Double,
    val placedPieces: List<PlacedPiece>,
    val cutsCount: Int,
    val kerfLossMm: Double,
    val usedPiecesLengthMm: Double,
    val leftoverMm: Double,
    val isLeftoverReusable: Boolean,
    val utilizationPercent: Double
)

data class OptimizationResult(
    val boards: List<BoardCutPlan>,
    val totalBoardsUsed: Int,
    val totalStockLengthMm: Double,
    val totalUsableCutLengthMm: Double,
    val totalKerfLossMm: Double,
    val totalTrimLossMm: Double,
    val totalLeftoverMm: Double,
    val reusableOffcutsCount: Int,
    val reusableOffcutsLengthMm: Double,
    val scrapWasteLengthMm: Double,
    val overallUtilizationPercent: Double,
    val unplacedPieces: List<CutPiece> = emptyList(),
    val calculationTimeMs: Long = 0L,
    val unit: UnitOfMeasurement = UnitOfMeasurement.MM
)
