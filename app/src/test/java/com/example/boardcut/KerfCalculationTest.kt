package com.example.boardcut

import com.example.boardcut.engine.CuttingOptimizer
import com.example.boardcut.model.CalculationSettings
import com.example.boardcut.model.CutPiece
import com.example.boardcut.model.StockBoard
import com.example.boardcut.model.UnitOfMeasurement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KerfCalculationTest {

    @Test
    fun testKerfAccountingWithMultiplePieces() {
        // Stock of 2000 mm, Kerf of 3 mm, Trim of 0 mm
        // Cut pieces: 3 pieces of 500 mm each
        // Cut positions:
        // Piece 1: 0 -> 500. Kerf 1 cut: 500 -> 503.
        // Piece 2: 503 -> 1003. Kerf 2 cut: 1003 -> 1006.
        // Piece 3: 1006 -> 1506. Remainder is 2000 - 1506 = 494.
        // Cut 3 separates piece 3 from remainder: Kerf 3 cut: 1506 -> 1509.
        // Leftover offcut = 2000 - 1509 = 491 mm.
        // Total cuts = 3. Total kerf loss = 3 * 3 = 9 mm.
        val stock = listOf(StockBoard(length = 2000.0))
        val pieces = listOf(CutPiece(length = 500.0, quantity = 3))
        val settings = CalculationSettings(kerf = 3.0, trimAllowance = 0.0)

        val result = CuttingOptimizer.optimize(stock, pieces, settings, UnitOfMeasurement.MM)

        assertEquals(1, result.totalBoardsUsed)
        val board = result.boards.first()
        assertEquals(3, board.placedPieces.size)
        assertEquals(3, board.cutsCount)
        assertEquals(9.0, board.kerfLossMm, 0.001)
        assertEquals(1500.0, board.usedPiecesLengthMm, 0.001)
        assertEquals(491.0, board.leftoverMm, 0.001)

        // Mathematical balance: stock = pieces + kerf + leftover
        val totalSum = board.usedPiecesLengthMm + board.kerfLossMm + board.leftoverMm + board.trimAllowanceMm
        assertEquals(2000.0, totalSum, 0.001)
    }

    @Test
    fun testExactBoardFitNoTrailingKerf() {
        // Stock of 1000 mm.
        // Piece 1: 497 mm.
        // Kerf: 3 mm.
        // Piece 2: 500 mm.
        // Total pieces = 497 + 500 = 997 mm.
        // Kerf between them = 3 mm.
        // 497 + 3 + 500 = 1000 mm! Exactly reaches the end of the stock!
        // No trailing cut needed because 0 mm remainder.
        // Total cuts = 1 (between piece 1 and 2).
        // Total kerf = 3 mm. Leftover = 0 mm.
        val stock = listOf(StockBoard(length = 1000.0))
        val pieces = listOf(
            CutPiece(label = "P1", length = 497.0, quantity = 1),
            CutPiece(label = "P2", length = 500.0, quantity = 1)
        )
        val settings = CalculationSettings(kerf = 3.0, trimAllowance = 0.0)

        val result = CuttingOptimizer.optimize(stock, pieces, settings, UnitOfMeasurement.MM)

        assertEquals(1, result.totalBoardsUsed)
        val board = result.boards.first()
        assertEquals(1, board.cutsCount)
        assertEquals(3.0, board.kerfLossMm, 0.001)
        assertEquals(0.0, board.leftoverMm, 0.001)
        assertEquals(997.0, board.usedPiecesLengthMm, 0.001)
    }

    @Test
    fun testKerfWithEndTrimAllowance() {
        // Stock 2400 mm, Trim 10 mm, Kerf 3 mm
        // Piece: 1000 mm x 1
        // Cut 1: Trim 10 mm (from 0 to 10)
        // Piece: 10 to 1010 mm
        // Cut 2: Sever from leftover drop: kerf 3 mm (1010 to 1013)
        // Leftover = 2400 - 1013 = 1387 mm
        val stock = listOf(StockBoard(length = 2400.0))
        val pieces = listOf(CutPiece(length = 1000.0, quantity = 1))
        val settings = CalculationSettings(kerf = 3.0, trimAllowance = 10.0)

        val result = CuttingOptimizer.optimize(stock, pieces, settings, UnitOfMeasurement.MM)

        val board = result.boards.first()
        assertEquals(2, board.cutsCount) // Trim cut + sever cut
        assertEquals(3.0, board.kerfLossMm, 0.001)
        assertEquals(10.0, board.trimAllowanceMm, 0.001)
        assertEquals(1387.0, board.leftoverMm, 0.001)
    }
}
