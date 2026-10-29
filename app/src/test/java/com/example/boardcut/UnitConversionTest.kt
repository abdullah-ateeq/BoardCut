package com.example.boardcut

import com.example.boardcut.model.UnitOfMeasurement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnitConversionTest {

    @Test
    fun testMmConversions() {
        val mm = UnitOfMeasurement.MM
        assertEquals(100.0, mm.toMm(100.0), 0.0001)
        assertEquals(100.0, mm.fromMm(100.0), 0.0001)
        assertEquals("500 mm", mm.format(500.0))
    }

    @Test
    fun testCmConversions() {
        val cm = UnitOfMeasurement.CM
        assertEquals(1000.0, cm.toMm(100.0), 0.0001)
        assertEquals(10.0, cm.fromMm(100.0), 0.0001)
        assertEquals("25 cm", cm.format(25.0))
    }

    @Test
    fun testInchConversions() {
        val inch = UnitOfMeasurement.INCH
        // 1 inch = 25.4 mm
        assertEquals(254.0, inch.toMm(10.0), 0.0001)
        assertEquals(10.0, inch.fromMm(254.0), 0.0001)
        // 8 feet = 96 inches = 2438.4 mm
        assertEquals(2438.4, inch.toMm(96.0), 0.0001)
    }

    @Test
    fun testFeetConversions() {
        val ft = UnitOfMeasurement.FEET
        // 1 ft = 304.8 mm
        assertEquals(3048.0, ft.toMm(10.0), 0.0001)
        assertEquals(8.0, ft.fromMm(2438.4), 0.0001)
    }

    @Test
    fun testRoundtripConversion() {
        val initialMm = 1850.5
        for (unit in UnitOfMeasurement.entries) {
            val converted = unit.fromMm(initialMm)
            val backToMm = unit.toMm(converted)
            assertEquals(initialMm, backToMm, 0.001)
        }
    }

    @Test
    fun testSymbolLookup() {
        assertEquals(UnitOfMeasurement.MM, UnitOfMeasurement.fromSymbol("mm"))
        assertEquals(UnitOfMeasurement.CM, UnitOfMeasurement.fromSymbol("cm"))
        assertEquals(UnitOfMeasurement.INCH, UnitOfMeasurement.fromSymbol("in"))
        assertEquals(UnitOfMeasurement.FEET, UnitOfMeasurement.fromSymbol("ft"))
        // Case-insensitive fallback
        assertEquals(UnitOfMeasurement.INCH, UnitOfMeasurement.fromSymbol("IN"))
        assertEquals(UnitOfMeasurement.MM, UnitOfMeasurement.fromSymbol("unknown"))
    }
}
