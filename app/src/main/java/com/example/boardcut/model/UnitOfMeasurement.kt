package com.example.boardcut.model

import java.util.Locale

enum class UnitOfMeasurement(
    val symbol: String,
    val label: String,
    val toMmMultiplier: Double,
    val defaultKerf: Double,
    val defaultTrim: Double
) {
    MM(
        symbol = "mm",
        label = "Millimeters (mm)",
        toMmMultiplier = 1.0,
        defaultKerf = 3.2,
        defaultTrim = 10.0
    ),
    CM(
        symbol = "cm",
        label = "Centimeters (cm)",
        toMmMultiplier = 10.0,
        defaultKerf = 0.32,
        defaultTrim = 1.0
    ),
    INCH(
        symbol = "in",
        label = "Inches (in)",
        toMmMultiplier = 25.4,
        defaultKerf = 0.125, // 1/8"
        defaultTrim = 0.5    // 1/2"
    ),
    FEET(
        symbol = "ft",
        label = "Feet (ft)",
        toMmMultiplier = 304.8,
        defaultKerf = 0.0104, // ~1/8"
        defaultTrim = 0.0417  // ~1/2"
    );

    fun toMm(value: Double): Double = value * toMmMultiplier

    fun fromMm(valueMm: Double): Double = valueMm / toMmMultiplier

    fun format(valueInCurrentUnit: Double, decimals: Int = -1): String {
        val dec = if (decimals >= 0) decimals else when (this) {
            MM -> 1
            CM -> 2
            INCH -> 3
            FEET -> 3
        }
        val formatted = String.format(Locale.US, "%.${dec}f", valueInCurrentUnit)
        // Clean trailing zeros for clean readability (e.g. 500.0 -> 500)
        val clean = if (formatted.contains(".")) {
            formatted.trimEnd('0').trimEnd('.')
        } else {
            formatted
        }
        return "$clean $symbol"
    }

    fun formatMm(valueMm: Double, decimals: Int = -1): String {
        return format(fromMm(valueMm), decimals)
    }

    companion object {
        fun fromSymbol(symbol: String): UnitOfMeasurement {
            return entries.firstOrNull { it.symbol.equals(symbol, ignoreCase = true) } ?: MM
        }
    }
}
