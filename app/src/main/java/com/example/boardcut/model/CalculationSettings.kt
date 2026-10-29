package com.example.boardcut.model

data class CalculationSettings(
    val kerf: Double = 3.2, // in active unit
    val trimAllowance: Double = 10.0, // end trim per board in active unit
    val minUsableOffcut: Double = 200.0 // offcuts longer than this are considered reusable in active unit
) {
    fun kerfInMm(unit: UnitOfMeasurement): Double = unit.toMm(kerf)
    fun trimAllowanceInMm(unit: UnitOfMeasurement): Double = unit.toMm(trimAllowance)
    fun minUsableOffcutInMm(unit: UnitOfMeasurement): Double = unit.toMm(minUsableOffcut)
}
