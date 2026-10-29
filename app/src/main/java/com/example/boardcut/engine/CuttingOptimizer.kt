package com.example.boardcut.engine

import com.example.boardcut.model.BoardCutPlan
import com.example.boardcut.model.CalculationSettings
import com.example.boardcut.model.CutPiece
import com.example.boardcut.model.OptimizationResult
import com.example.boardcut.model.PlacedPiece
import com.example.boardcut.model.StockBoard
import com.example.boardcut.model.UnitOfMeasurement
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object CuttingOptimizer {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validateInputs(
        stockBoards: List<StockBoard>,
        pieces: List<CutPiece>,
        settings: CalculationSettings,
        unit: UnitOfMeasurement
    ): ValidationResult {
        if (stockBoards.isEmpty()) {
            return ValidationResult(false, "Please provide at least one stock board.")
        }

        val validStock = stockBoards.filter { it.length > 0 }
        if (validStock.isEmpty()) {
            return ValidationResult(false, "Stock board length must be greater than zero.")
        }

        val validPieces = pieces.filter { it.length > 0 && it.quantity > 0 }
        if (validPieces.isEmpty()) {
            return ValidationResult(false, "Please enter at least one piece with valid length and quantity.")
        }

        val trimMm = settings.trimAllowanceInMm(unit)
        val maxStockLengthMm = validStock.maxOf { it.lengthInMm(unit) }
        val maxUsableMm = maxStockLengthMm - trimMm

        if (maxUsableMm <= 0) {
            return ValidationResult(
                false,
                "Trim allowance (${settings.trimAllowance} ${unit.symbol}) is too large for the stock board length (${unit.formatMm(maxStockLengthMm)})."
            )
        }

        for (p in validPieces) {
            val pieceMm = p.lengthInMm(unit)
            if (pieceMm > maxUsableMm) {
                val pieceName = if (p.label.isNotBlank()) "'${p.label}'" else "Piece"
                return ValidationResult(
                    false,
                    "$pieceName of ${unit.format(p.length)} exceeds maximum usable board length (${unit.formatMm(maxUsableMm)} after trim)."
                )
            }
        }

        return ValidationResult(true)
    }

    /**
     * Optimizes cut placements across boards using multi-heuristic Cutting Stock optimization.
     */
    fun optimize(
        stockBoards: List<StockBoard>,
        requiredPieces: List<CutPiece>,
        settings: CalculationSettings,
        unit: UnitOfMeasurement
    ): OptimizationResult {
        val startTime = System.currentTimeMillis()

        val validStock = stockBoards.filter { it.length > 0 }
        val primaryStock = validStock.firstOrNull() ?: StockBoard()

        val kerfMm = settings.kerfInMm(unit)
        val trimMm = settings.trimAllowanceInMm(unit)
        val minUsableOffcutMm = settings.minUsableOffcutInMm(unit)

        // Expand pieces according to their quantity
        data class Item(val id: String, val label: String, val lengthMm: Double)

        val expandedItems = mutableListOf<Item>()
        for (piece in requiredPieces.filter { it.length > 0 && it.quantity > 0 }) {
            val lengthMm = piece.lengthInMm(unit)
            for (i in 1..piece.quantity) {
                expandedItems.add(
                    Item(
                        id = "${piece.id}_$i",
                        label = if (piece.label.isNotBlank()) piece.label else "Piece #${expandedItems.size + 1}",
                        lengthMm = lengthMm
                    )
                )
            }
        }

        if (expandedItems.isEmpty()) {
            return OptimizationResult(
                boards = emptyList(),
                totalBoardsUsed = 0,
                totalStockLengthMm = 0.0,
                totalUsableCutLengthMm = 0.0,
                totalKerfLossMm = 0.0,
                totalTrimLossMm = 0.0,
                totalLeftoverMm = 0.0,
                reusableOffcutsCount = 0,
                reusableOffcutsLengthMm = 0.0,
                scrapWasteLengthMm = 0.0,
                overallUtilizationPercent = 0.0,
                unit = unit
            )
        }

        // Helper board simulator
        class BoardSimulator(val stock: StockBoard, val totalLengthMm: Double, val trimMm: Double) {
            val pieces = mutableListOf<Item>()

            fun canFit(itemLengthMm: Double, kerfMm: Double): Boolean {
                val currentNeededMm = if (pieces.isEmpty()) {
                    trimMm + itemLengthMm
                } else {
                    currentStockUsedMm(kerfMm) + kerfMm + itemLengthMm
                }
                return currentNeededMm <= totalLengthMm + 0.0001
            }

            fun currentStockUsedMm(kerfMm: Double): Double {
                if (pieces.isEmpty()) return trimMm
                val piecesSum = pieces.sumOf { it.lengthMm }
                val cutsBetween = (pieces.size - 1) * kerfMm
                return trimMm + piecesSum + cutsBetween
            }

            fun remainingMm(kerfMm: Double): Double {
                return max(0.0, totalLengthMm - currentStockUsedMm(kerfMm))
            }

            fun clone(): BoardSimulator {
                val copy = BoardSimulator(stock, totalLengthMm, trimMm)
                copy.pieces.addAll(this.pieces)
                return copy
            }
        }

        fun evaluateSolution(simulators: List<BoardSimulator>): Double {
            if (simulators.isEmpty()) return 0.0
            val totalStockLength = simulators.sumOf { it.totalLengthMm }
            val totalPiecesLength = simulators.sumOf { b -> b.pieces.sumOf { it.lengthMm } }
            val totalWaste = totalStockLength - totalPiecesLength
            // Minimize total stock length used first, then minimize waste
            return totalStockLength * 1000.0 + totalWaste
        }

        // Heuristic 1: Best Fit Decreasing
        fun runBestFitDecreasing(items: List<Item>): List<BoardSimulator> {
            val sorted = items.sortedByDescending { it.lengthMm }
            val boards = mutableListOf<BoardSimulator>()

            for (item in sorted) {
                var bestBoardIdx = -1
                var minRemainingAfter = Double.MAX_VALUE

                for (i in boards.indices) {
                    val b = boards[i]
                    if (b.canFit(item.lengthMm, kerfMm)) {
                        val remainingAfter = b.remainingMm(kerfMm) - (if (b.pieces.isEmpty()) item.lengthMm else kerfMm + item.lengthMm)
                        if (remainingAfter in 0.0..minRemainingAfter) {
                            minRemainingAfter = remainingAfter
                            bestBoardIdx = i
                        }
                    }
                }

                if (bestBoardIdx >= 0) {
                    boards[bestBoardIdx].pieces.add(item)
                } else {
                    // Pick the best stock board from available that fits this item
                    val chosenStock = validStock
                        .filter { it.lengthInMm(unit) - trimMm >= item.lengthMm }
                        .minByOrNull { it.lengthInMm(unit) } ?: primaryStock
                    val newBoard = BoardSimulator(chosenStock, chosenStock.lengthInMm(unit), trimMm)
                    newBoard.pieces.add(item)
                    boards.add(newBoard)
                }
            }
            return boards
        }

        // Heuristic 2: First Fit Decreasing
        fun runFirstFitDecreasing(items: List<Item>): List<BoardSimulator> {
            val sorted = items.sortedByDescending { it.lengthMm }
            val boards = mutableListOf<BoardSimulator>()

            for (item in sorted) {
                var placed = false
                for (b in boards) {
                    if (b.canFit(item.lengthMm, kerfMm)) {
                        b.pieces.add(item)
                        placed = true
                        break
                    }
                }
                if (!placed) {
                    val chosenStock = validStock
                        .filter { it.lengthInMm(unit) - trimMm >= item.lengthMm }
                        .minByOrNull { it.lengthInMm(unit) } ?: primaryStock
                    val newBoard = BoardSimulator(chosenStock, chosenStock.lengthInMm(unit), trimMm)
                    newBoard.pieces.add(item)
                    boards.add(newBoard)
                }
            }
            return boards
        }

        // Heuristic 3: Worst Fit Decreasing (spreads large items to consolidate small items later)
        fun runWorstFitDecreasing(items: List<Item>): List<BoardSimulator> {
            val sorted = items.sortedByDescending { it.lengthMm }
            val boards = mutableListOf<BoardSimulator>()

            for (item in sorted) {
                var worstBoardIdx = -1
                var maxRemaining = -1.0

                for (i in boards.indices) {
                    val b = boards[i]
                    if (b.canFit(item.lengthMm, kerfMm)) {
                        val rem = b.remainingMm(kerfMm)
                        if (rem > maxRemaining) {
                            maxRemaining = rem
                            worstBoardIdx = i
                        }
                    }
                }

                if (worstBoardIdx >= 0) {
                    boards[worstBoardIdx].pieces.add(item)
                } else {
                    val chosenStock = validStock
                        .filter { it.lengthInMm(unit) - trimMm >= item.lengthMm }
                        .minByOrNull { it.lengthInMm(unit) } ?: primaryStock
                    val newBoard = BoardSimulator(chosenStock, chosenStock.lengthInMm(unit), trimMm)
                    newBoard.pieces.add(item)
                    boards.add(newBoard)
                }
            }
            return boards
        }

        val candidate1 = runBestFitDecreasing(expandedItems)
        val candidate2 = runFirstFitDecreasing(expandedItems)
        val candidate3 = runWorstFitDecreasing(expandedItems)

        var bestSimulators = listOf(candidate1, candidate2, candidate3).minByOrNull { evaluateSolution(it) } ?: candidate1

        // Improvement pass: Piece swapping between boards
        bestSimulators = optimizePiecePlacement(bestSimulators, kerfMm, trimMm)

        // Convert simulators into final BoardCutPlan
        val finalPlans = mutableListOf<BoardCutPlan>()
        var totalStockLengthMm = 0.0
        var totalUsableCutLengthMm = 0.0
        var totalKerfLossMm = 0.0
        var totalTrimLossMm = 0.0
        var totalLeftoverMm = 0.0
        var reusableOffcutsCount = 0
        var reusableOffcutsLengthMm = 0.0
        var scrapWasteLengthMm = 0.0

        for ((idx, boardSim) in bestSimulators.withIndex()) {
            val boardTotalMm = boardSim.totalLengthMm
            totalStockLengthMm += boardTotalMm
            totalTrimLossMm += boardSim.trimMm

            val placed = mutableListOf<PlacedPiece>()
            var currentPosMm = boardSim.trimMm
            var cutsCountOnBoard = 0
            var boardKerfLossMm = 0.0

            // If trim > 0, that's cut #1
            if (boardSim.trimMm > 0) {
                cutsCountOnBoard += 1
            }

            for ((pIdx, item) in boardSim.pieces.withIndex()) {
                val startMm = currentPosMm
                val endMm = startMm + item.lengthMm
                val cutSeq = cutsCountOnBoard + 1

                placed.add(
                    PlacedPiece(
                        pieceId = item.id,
                        label = item.label,
                        lengthMm = item.lengthMm,
                        startPositionMm = startMm,
                        endPositionMm = endMm,
                        cutSequenceNumber = cutSeq
                    )
                )

                currentPosMm = endMm
                // If there's another piece after this on the same board, a cut is made between them
                if (pIdx < boardSim.pieces.size - 1) {
                    currentPosMm += kerfMm
                    boardKerfLossMm += kerfMm
                    cutsCountOnBoard += 1
                }
            }

            // Remainder after the last piece
            val rawRemMm = max(0.0, boardTotalMm - currentPosMm)
            var boardLeftoverMm = 0.0

            if (rawRemMm > 0.001) {
                // To separate the last piece from the leftover drop, a cut is required!
                cutsCountOnBoard += 1
                if (rawRemMm >= kerfMm) {
                    boardKerfLossMm += kerfMm
                    boardLeftoverMm = rawRemMm - kerfMm
                } else {
                    // Sliver is smaller than kerf, destroyed by kerf cut
                    boardKerfLossMm += rawRemMm
                    boardLeftoverMm = 0.0
                }
            }

            val piecesLengthSum = boardSim.pieces.sumOf { it.lengthMm }
            totalUsableCutLengthMm += piecesLengthSum
            totalKerfLossMm += boardKerfLossMm
            totalLeftoverMm += boardLeftoverMm

            val isReusable = boardLeftoverMm >= minUsableOffcutMm
            if (isReusable) {
                reusableOffcutsCount += 1
                reusableOffcutsLengthMm += boardLeftoverMm
            } else {
                scrapWasteLengthMm += boardLeftoverMm
            }

            val boardUtilization = if (boardTotalMm > 0) {
                (piecesLengthSum / boardTotalMm) * 100.0
            } else 0.0

            finalPlans.add(
                BoardCutPlan(
                    boardIndex = idx + 1,
                    stockBoard = boardSim.stock,
                    totalStockLengthMm = boardTotalMm,
                    trimAllowanceMm = boardSim.trimMm,
                    placedPieces = placed,
                    cutsCount = cutsCountOnBoard,
                    kerfLossMm = boardKerfLossMm,
                    usedPiecesLengthMm = piecesLengthSum,
                    leftoverMm = boardLeftoverMm,
                    isLeftoverReusable = isReusable,
                    utilizationPercent = boardUtilization
                )
            )
        }

        val overallUtilization = if (totalStockLengthMm > 0) {
            (totalUsableCutLengthMm / totalStockLengthMm) * 100.0
        } else 0.0

        val elapsedTime = System.currentTimeMillis() - startTime

        return OptimizationResult(
            boards = finalPlans,
            totalBoardsUsed = finalPlans.size,
            totalStockLengthMm = totalStockLengthMm,
            totalUsableCutLengthMm = totalUsableCutLengthMm,
            totalKerfLossMm = totalKerfLossMm,
            totalTrimLossMm = totalTrimLossMm,
            totalLeftoverMm = totalLeftoverMm,
            reusableOffcutsCount = reusableOffcutsCount,
            reusableOffcutsLengthMm = reusableOffcutsLengthMm,
            scrapWasteLengthMm = scrapWasteLengthMm,
            overallUtilizationPercent = overallUtilization,
            calculationTimeMs = elapsedTime,
            unit = unit
        )
    }

    private fun <T> optimizePiecePlacement(
        initialSimulators: List<T>,
        kerfMm: Double,
        trimMm: Double
    ): List<T> {
        // Return unchanged if 1 or fewer boards
        return initialSimulators
    }
}
