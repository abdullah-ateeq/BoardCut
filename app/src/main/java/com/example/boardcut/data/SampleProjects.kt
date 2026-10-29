package com.example.boardcut.data

import com.example.boardcut.model.CalculationSettings
import com.example.boardcut.model.CutPiece
import com.example.boardcut.model.CuttingProject
import com.example.boardcut.model.StockBoard
import com.example.boardcut.model.UnitOfMeasurement

object SampleProjects {

    fun getSamples(): List<CuttingProject> = listOf(
        CuttingProject(
            id = "preset_bookshelf_mm",
            name = "Hardwood Bookshelf (5-Tier)",
            unit = UnitOfMeasurement.MM,
            stockBoards = listOf(
                StockBoard(name = "Pine Board (2400mm)", length = 2400.0, width = 200.0, thickness = 18.0)
            ),
            requiredPieces = listOf(
                CutPiece(label = "Side Uprights", length = 1800.0, quantity = 2),
                CutPiece(label = "Top & Bottom", length = 850.0, quantity = 2),
                CutPiece(label = "Middle Shelves", length = 814.0, quantity = 4),
                CutPiece(label = "Base Plinth", length = 850.0, quantity = 1)
            ),
            settings = CalculationSettings(kerf = 3.2, trimAllowance = 15.0, minUsableOffcut = 250.0)
        ),
        CuttingProject(
            id = "preset_workbench_inch",
            name = "Sturdy 2x4 Garage Workbench",
            unit = UnitOfMeasurement.INCH,
            stockBoards = listOf(
                StockBoard(name = "Standard 8ft 2x4", length = 96.0, width = 3.5, thickness = 1.5)
            ),
            requiredPieces = listOf(
                CutPiece(label = "Legs", length = 34.0, quantity = 4),
                CutPiece(label = "Long Rails", length = 48.0, quantity = 4),
                CutPiece(label = "End Stretchers", length = 20.0, quantity = 4),
                CutPiece(label = "Cross Braces", length = 21.0, quantity = 3)
            ),
            settings = CalculationSettings(kerf = 0.125, trimAllowance = 0.5, minUsableOffcut = 12.0)
        ),
        CuttingProject(
            id = "preset_picnic_cm",
            name = "Garden Picnic Bench",
            unit = UnitOfMeasurement.CM,
            stockBoards = listOf(
                StockBoard(name = "Cedar Planks 300cm", length = 300.0, width = 14.0, thickness = 3.8)
            ),
            requiredPieces = listOf(
                CutPiece(label = "Table Top Slats", length = 180.0, quantity = 5),
                CutPiece(label = "Seat Slats", length = 180.0, quantity = 4),
                CutPiece(label = "A-Frame Legs", length = 92.0, quantity = 4),
                CutPiece(label = "Under Support", length = 75.0, quantity = 3)
            ),
            settings = CalculationSettings(kerf = 0.3, trimAllowance = 1.0, minUsableOffcut = 30.0)
        )
    )
}
