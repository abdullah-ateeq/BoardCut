package com.example.boardcut

import com.example.boardcut.engine.CuttingOptimizer
import com.example.boardcut.model.CalculationSettings
import com.example.boardcut.model.CutPiece
import com.example.boardcut.model.StockBoard
import com.example.boardcut.model.UnitOfMeasurement
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImpossibleCutsTest {

    @Test
    fun testPieceLargerThanStockBoard() {
        val stock = listOf(StockBoard(length = 2000.0))
        val pieces = listOf(CutPiece(label = "Long Beam", length = 2500.0, quantity = 1))
        val settings = CalculationSettings(kerf = 3.0, trimAllowance = 0.0)

        val validation = CuttingOptimizer.validateInputs(stock, pieces, settings, UnitOfMeasurement.MM)

        assertFalse(validation.isValid)
        assertNotNull(validation.errorMessage)
        assertTrue(validation.errorMessage!!.contains("exceeds maximum usable board length"))
    }

    @Test
    fun testPieceLargerThanStockMinusTrim() {
        // Stock 2000 mm, Trim 50 mm. Max usable = 1950 mm.
        // Piece is 1980 mm. Fits in 2000 mm raw, but exceeds usable 1950 mm after trim!
        val stock = listOf(StockBoard(length = 2000.0))
        val pieces = listOf(CutPiece(label = "Table Leg", length = 1980.0, quantity = 1))
        val settings = CalculationSettings(kerf = 3.0, trimAllowance = 50.0)

        val validation = CuttingOptimizer.validateInputs(stock, pieces, settings, UnitOfMeasurement.MM)

        assertFalse(validation.isValid)
        assertNotNull(validation.errorMessage)
        assertTrue(validation.errorMessage!!.contains("1950 mm after trim"))
    }

    @Test
    fun testTrimLargerThanStockBoard() {
        val stock = listOf(StockBoard(length = 1000.0))
        val pieces = listOf(CutPiece(length = 500.0, quantity = 1))
        val settings = CalculationSettings(kerf = 3.0, trimAllowance = 1200.0)

        val validation = CuttingOptimizer.validateInputs(stock, pieces, settings, UnitOfMeasurement.MM)

        assertFalse(validation.isValid)
        assertNotNull(validation.errorMessage)
        assertTrue(validation.errorMessage!!.contains("Trim allowance"))
    }

    @Test
    fun testZeroStockBoardLength() {
        val stock = listOf(StockBoard(length = 0.0))
        val pieces = listOf(CutPiece(length = 500.0, quantity = 1))
        val settings = CalculationSettings()

        val validation = CuttingOptimizer.validateInputs(stock, pieces, settings, UnitOfMeasurement.MM)

        assertFalse(validation.isValid)
        assertTrue(validation.errorMessage!!.contains("Stock board length must be greater than zero"))
    }

    @Test
    fun testEmptyCutPieces() {
        val stock = listOf(StockBoard(length = 2000.0))
        val pieces = emptyList<CutPiece>()
        val settings = CalculationSettings()

        val validation = CuttingOptimizer.validateInputs(stock, pieces, settings, UnitOfMeasurement.MM)

        assertFalse(validation.isValid)
        assertTrue(validation.errorMessage!!.contains("at least one piece"))
    }
}
