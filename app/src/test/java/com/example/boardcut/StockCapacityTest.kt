package com.example.boardcut

import com.example.boardcut.engine.CuttingOptimizer
import com.example.boardcut.model.CalculationSettings
import com.example.boardcut.model.CutPiece
import com.example.boardcut.model.StockBoard
import com.example.boardcut.model.UnitOfMeasurement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StockCapacityTest {

    @Test
    fun testMultiBoardSplitting() {
        // Stock of 1000 mm.
        // Kerf: 3 mm.
        // Piece: 600 mm x 2.
        // Cannot fit both 600 mm pieces on one 1000 mm board (600 + 3 + 600 = 1203 > 1000).
        // Must allocate across 2 boards!
        val stock = listOf(StockBoard(length = 1000.0))
        val pieces = listOf(CutPiece(label = "Rail", length = 600.0, quantity = 2))
        val settings = CalculationSettings(kerf = 3.0, trimAllowance = 0.0)

        val result = CuttingOptimizer.optimize(stock, pieces, settings, UnitOfMeasurement.MM)

        assertEquals(2, result.totalBoardsUsed)
        assertEquals(2, result.boards.size)
        assertEquals(1, result.boards[0].placedPieces.size)
        assertEquals(1, result.boards[1].placedPieces.size)
    }

    @Test
    fun testMultipleStockLengthSelection() {
        // Available stock: Short (1200 mm) and Long (2400 mm)
        // Pieces: 1 piece of 1100 mm.
        // Optimizer should choose the 1200 mm stock to avoid wasting a 2400 mm stock board!
        val stock = listOf(
            StockBoard(name = "Long Board", length = 2400.0),
            StockBoard(name = "Short Board", length = 1200.0)
        )
        val pieces = listOf(CutPiece(length = 1100.0, quantity = 1))
        val settings = CalculationSettings(kerf = 3.0, trimAllowance = 0.0)

        val result = CuttingOptimizer.optimize(stock, pieces, settings, UnitOfMeasurement.MM)

        assertEquals(1, result.totalBoardsUsed)
        assertEquals(1200.0, result.boards.first().totalStockLengthMm, 0.001)
    }

    @Test
    fun testTightPackingEfficiency() {
        // Stock: 2400 mm
        // Kerf: 3.2 mm
        // Pieces: 4 pieces of 590 mm
        // 4 * 590 = 2360 mm.
        // Kerf cuts: 3 cuts between pieces + 1 cut to sever remainder = 4 * 3.2 = 12.8 mm.
        // Total required = 2360 + 12.8 = 2372.8 mm <= 2400 mm.
        // All 4 should fit tightly on a single board!
        val stock = listOf(StockBoard(length = 2400.0))
        val pieces = listOf(CutPiece(label = "Leg", length = 590.0, quantity = 4))
        val settings = CalculationSettings(kerf = 3.2, trimAllowance = 0.0)

        val result = CuttingOptimizer.optimize(stock, pieces, settings, UnitOfMeasurement.MM)

        assertEquals(1, result.totalBoardsUsed)
        assertEquals(4, result.boards.first().placedPieces.size)
        assertTrue(result.overallUtilizationPercent > 95.0)
    }
}
