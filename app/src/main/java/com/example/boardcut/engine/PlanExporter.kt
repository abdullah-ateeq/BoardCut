package com.example.boardcut.engine

import com.example.boardcut.model.OptimizationResult
import com.example.boardcut.model.UnitOfMeasurement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PlanExporter {

    fun generateCuttingListText(result: OptimizationResult, projectName: String = "BoardCut Project"): String {
        val unit = result.unit
        val sb = StringBuilder()
        sb.appendLine("==========================================")
        sb.appendLine("       BOARDCUT - CUTTING LIST           ")
        sb.appendLine("==========================================")
        sb.appendLine("Project: $projectName")
        sb.appendLine("Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}")
        sb.appendLine("Measurement Unit: ${unit.label}")
        sb.appendLine("Total Boards Used: ${result.totalBoardsUsed}")
        sb.appendLine("Material Utilization: ${String.format(Locale.US, "%.1f", result.overallUtilizationPercent)}%")
        sb.appendLine("==========================================\n")

        for (board in result.boards) {
            sb.appendLine("--- BOARD #${board.boardIndex} ---")
            sb.appendLine("Stock Length: ${unit.formatMm(board.totalStockLengthMm)}")
            if (board.trimAllowanceMm > 0) {
                sb.appendLine("End Trim: ${unit.formatMm(board.trimAllowanceMm)}")
            }
            sb.appendLine("Utilization: ${String.format(Locale.US, "%.1f", board.utilizationPercent)}%")
            sb.appendLine("Total Cuts: ${board.cutsCount} | Kerf Loss: ${unit.formatMm(board.kerfLossMm)}")
            sb.appendLine("Cuts Sequence:")
            for (p in board.placedPieces) {
                sb.appendLine(
                    "  [Cut #${p.cutSequenceNumber}] ${p.label}: " +
                            "Length ${unit.formatMm(p.lengthMm)} " +
                            "(from ${unit.formatMm(p.startPositionMm)} to ${unit.formatMm(p.endPositionMm)})"
                )
            }
            if (board.leftoverMm > 0) {
                val status = if (board.isLeftoverReusable) "REUSABLE OFFCUT" else "SCRAP"
                sb.appendLine("  Remaining: ${unit.formatMm(board.leftoverMm)} ($status)")
            } else {
                sb.appendLine("  Remaining: 0 ${unit.symbol} (Clean finish)")
            }
            sb.appendLine()
        }

        sb.appendLine("==========================================")
        sb.appendLine("               SUMMARY                    ")
        sb.appendLine("==========================================")
        sb.appendLine("Total Usable Material: ${unit.formatMm(result.totalUsableCutLengthMm)}")
        sb.appendLine("Total Saw Kerf Loss:    ${unit.formatMm(result.totalKerfLossMm)}")
        if (result.totalTrimLossMm > 0) {
            sb.appendLine("Total Trim Loss:        ${unit.formatMm(result.totalTrimLossMm)}")
        }
        sb.appendLine("Total Leftover:         ${unit.formatMm(result.totalLeftoverMm)}")
        sb.appendLine("  - Reusable Offcuts:   ${unit.formatMm(result.reusableOffcutsLengthMm)} (${result.reusableOffcutsCount} boards)")
        sb.appendLine("  - Scrap Waste:        ${unit.formatMm(result.scrapWasteLengthMm)}")
        sb.appendLine("Overall Efficiency:     ${String.format(Locale.US, "%.1f", result.overallUtilizationPercent)}%")
        sb.appendLine("==========================================")
        return sb.toString()
    }

    fun generateCsv(result: OptimizationResult, projectName: String = "BoardCut Project"): String {
        val unit = result.unit
        val sb = StringBuilder()
        sb.appendLine("Project,\"${projectName.replace("\"", "\"\"")}\"")
        sb.appendLine("Unit,\"${unit.symbol}\"")
        sb.appendLine("Total Boards,${result.totalBoardsUsed}")
        sb.appendLine("Overall Utilization,${String.format(Locale.US, "%.2f", result.overallUtilizationPercent)}%")
        sb.appendLine()
        sb.appendLine("Board Number,Stock Length,Cut Sequence,Piece Label,Cut Length,Start Pos,End Pos,Remaining Offcut,Status")

        for (board in result.boards) {
            val stockLen = unit.fromMm(board.totalStockLengthMm)
            for (p in board.placedPieces) {
                sb.appendLine(
                    "${board.boardIndex}," +
                            "$stockLen," +
                            "${p.cutSequenceNumber}," +
                            "\"${p.label.replace("\"", "\"\"")}\"," +
                            "${unit.fromMm(p.lengthMm)}," +
                            "${unit.fromMm(p.startPositionMm)}," +
                            "${unit.fromMm(p.endPositionMm)}," +
                            "${unit.fromMm(board.leftoverMm)}," +
                            if (board.isLeftoverReusable) "Reusable" else "Scrap"
                )
            }
        }
        return sb.toString()
    }

    fun generateSimpleRequirementsText(
        stockLength: Double,
        pieces: List<com.example.boardcut.model.CutPiece>,
        unit: UnitOfMeasurement
    ): String {
        val sb = StringBuilder()
        sb.appendLine("Stock Board: ${unit.format(stockLength)}")
        sb.appendLine("Cut Requirements:")
        for (p in pieces) {
            val label = if (p.label.isNotBlank()) "${p.label}: " else ""
            sb.appendLine(" - $label${unit.format(p.length)} x ${p.quantity}")
        }
        return sb.toString()
    }
}
