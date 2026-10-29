package com.example.boardcut

import com.example.boardcut.engine.CuttingOptimizer
import com.example.boardcut.model.CalculationSettings
import com.example.boardcut.model.CutPiece
import com.example.boardcut.model.StockBoard
import com.example.boardcut.model.UnitOfMeasurement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WasteCalculationTest {

    @Test
    fun testMathematicalConservationOfLength() {
        // Across multiple boards and pieces, the total stock length must equal:
        // Usable Cut Length + Total Kerf Loss + Total Trim Loss + Total Leftovers
        val stock = listOf(StockBoard(length = 2440.0))
        val pieces = listOf(
            CutPiece(label = "Top", length = 1200.0, quantity = 2),
            CutPiece(label = "Side", length = 800.0, quantity = 3),
            CutPiece(label = "Shelf", length = 600.0, quantity = 4)
        )
        val settings = CalculationSettings(kerf = 3.2, trimAllowance = 10.0, minUsableOffcut = 250.0)

        val result = CuttingOptimizer.optimize(stock, pieces, settings, UnitOfMeasurement.MM)

        val reconstructedTotal = result.totalUsableCutLengthMm +
                result.totalKerfLossMm +
                result.totalTrimLossMm +
                result.totalLeftoverMm

        assertEquals(result.totalStockLengthMm, reconstructedTotal, 0.01)
    }

    @Test
    fun testReusableOffcutClassification() {
        // Stock 2000 mm, Trim 0 mm, Kerf 3 mm.
        // Piece 1: 1500 mm.
        // Leftover = 2000 - 1500 - 3 = 497 mm.
        // If minUsableOffcut is 300 mm, 497 mm is classified as REUSABLE.
        val stock = listOf(StockBoard(length = 2000.0))
        val pieces = listOf(CutPiece(length = 1500.0, quantity = 1))
        val settingsReusable = CalculationSettings(kerf = 3.0, trimAllowance = 0.0, minUsableOffcut = 300.0)

        val result1 = CuttingOptimizer.optimize(stock, pieces, settingsReusable, UnitOfMeasurement.MM)
        assertEquals(1, result1.reusableOffcutsCount)
        assertEquals(497.0, result1.reusableOffcutsLengthMm, 0.001)
        assertEquals(0.0, result1.scrapWasteLengthMm, 0.001)

        // If minUsableOffcut is 600 mm, 497 mm is classified as SCRAP.
        val settingsScrap = CalculationSettings(kerf = 3.0, trimAllowance = 0.0, minUsableOffcut = 600.0)
        val result2 = CuttingOptimizer.optimize(stock, pieces, settingsScrap, UnitOfMeasurement.MM)
        assertEquals(0, result2.reusableOffcutsCount)
        assertEquals(0.0, result2.reusableOffcutsLengthMm, 0.001)
        assertEquals(497.0, result2.scrapWasteLengthMm, 0.001)
    }

    @Test
    fun testUtilizationPercentageCalculation() {
        // Stock 1000 mm, 1 piece of 750 mm.
        // Pieces length sum = 750 mm.
        // Utilization = (750 / 1000) * 100 = 75.0%
        val stock = listOf(StockBoard(length = 1000.0))
        val pieces = listOf(CutPiece(length = 750.0, quantity = 1))
        val settings = CalculationSettings(kerf = 3.0, trimAllowance = 0.0)

        val result = CuttingOptimizer.optimize(stock, pieces, settings, UnitOfMeasurement.MM)
        assertEquals(75.0, result.overallUtilizationPercent, 0.001)
    }
}
