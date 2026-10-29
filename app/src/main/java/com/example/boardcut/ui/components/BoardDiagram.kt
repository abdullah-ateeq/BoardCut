package com.example.boardcut.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import com.example.boardcut.model.BoardCutPlan
import com.example.boardcut.model.PlacedPiece
import com.example.boardcut.model.UnitOfMeasurement
import com.example.boardcut.ui.theme.KerfCutRed
import com.example.boardcut.ui.theme.ReusableOffcutGreen
import com.example.boardcut.ui.theme.ScrapWasteSlate
import com.example.boardcut.ui.theme.TrimAllowanceOrange
import com.example.boardcut.ui.theme.getPieceColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BoardDiagramCard(
    plan: BoardCutPlan,
    unit: UnitOfMeasurement,
    modifier: Modifier = Modifier
) {
    var selectedPiece by remember { mutableStateOf<PlacedPiece?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Board # and specs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${plan.boardIndex}",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = plan.stockBoard.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Length: ${unit.formatMm(plan.totalStockLengthMm)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Utilization badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (plan.utilizationPercent >= 85.0) {
                        ReusableOffcutGreen.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = "${String.format("%.1f", plan.utilizationPercent)}% used",
                        color = if (plan.utilizationPercent >= 85.0) ReusableOffcutGreen else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Diagram
            BoardCanvas(
                plan = plan,
                unit = unit,
                selectedPiece = selectedPiece,
                onPieceSelected = { piece ->
                    selectedPiece = if (selectedPiece?.pieceId == piece?.pieceId) null else piece
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Selected Piece Details Banner
            if (selectedPiece != null) {
                val piece = selectedPiece!!
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${piece.label} (Cut #${piece.cutSequenceNumber}): ${unit.formatMm(piece.lengthMm)}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Mark: ${unit.formatMm(piece.startPositionMm)} → ${unit.formatMm(piece.endPositionMm)}",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Summary stats for this board
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${plan.cutsCount} cuts (${unit.formatMm(plan.kerfLossMm)} kerf)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (plan.trimAllowanceMm > 0) {
                    Text(
                        text = "Trim: ${unit.formatMm(plan.trimAllowanceMm)}",
                        fontSize = 12.sp,
                        color = TrimAllowanceOrange
                    )
                }

                if (plan.leftoverMm > 0) {
                    Text(
                        text = if (plan.isLeftoverReusable) {
                            "Offcut: ${unit.formatMm(plan.leftoverMm)} (Reusable)"
                        } else {
                            "Scrap: ${unit.formatMm(plan.leftoverMm)}"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (plan.isLeftoverReusable) ReusableOffcutGreen else ScrapWasteSlate
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cut sequence chips
            Text(
                text = "Cutting Sequence:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (plan.trimAllowanceMm > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TrimAllowanceOrange.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TrimAllowanceOrange.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "1. Trim ${unit.formatMm(plan.trimAllowanceMm)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TrimAllowanceOrange,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                plan.placedPieces.forEach { p ->
                    val isSelected = selectedPiece?.pieceId == p.pieceId
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.clip(RoundedCornerShape(6.dp))
                    ) {
                        Text(
                            text = "${p.cutSequenceNumber}. ${p.label} (${unit.formatMm(p.lengthMm)})",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                if (plan.leftoverMm > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (plan.isLeftoverReusable) ReusableOffcutGreen.copy(alpha = 0.15f) else ScrapWasteSlate.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Drop: ${unit.formatMm(plan.leftoverMm)}",
                            fontSize = 11.sp,
                            color = if (plan.isLeftoverReusable) ReusableOffcutGreen else ScrapWasteSlate,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardCanvas(
    plan: BoardCutPlan,
    unit: UnitOfMeasurement,
    selectedPiece: PlacedPiece?,
    onPieceSelected: (PlacedPiece?) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalMm = plan.totalStockLengthMm.coerceAtLeast(1.0)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE8DFD5))
            .border(1.dp, Color(0xFFC7BBAE), RoundedCornerShape(8.dp))
            .pointerInput(plan) {
                detectTapGestures { offset ->
                    val clickedRatio = offset.x / size.width
                    val clickedPosMm = clickedRatio * totalMm

                    // Find which piece was clicked
                    val clicked = plan.placedPieces.firstOrNull { p ->
                        clickedPosMm in p.startPositionMm..p.endPositionMm
                    }
                    onPieceSelected(clicked)
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val boardH = canvasH - 24.dp.toPx() // bottom 24dp for tape/ruler
            val boardTop = 4.dp.toPx()

            fun xForMm(mm: Double): Float = ((mm / totalMm) * canvasW).toFloat()

            // Draw Wood Texture Base
            drawRoundRect(
                color = Color(0xFFE2D6C6),
                topLeft = Offset(0f, boardTop),
                size = Size(canvasW, boardH),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Draw Trim Allowance if present
            if (plan.trimAllowanceMm > 0) {
                val trimW = xForMm(plan.trimAllowanceMm)
                drawRect(
                    color = TrimAllowanceOrange.copy(alpha = 0.5f),
                    topLeft = Offset(0f, boardTop),
                    size = Size(trimW, boardH)
                )
                // Diagonal hatch for trim
                drawLine(
                    color = TrimAllowanceOrange,
                    start = Offset(0f, boardTop),
                    end = Offset(trimW, boardTop + boardH),
                    strokeWidth = 1.5f
                )
            }

            // Draw Placed Pieces
            for ((idx, piece) in plan.placedPieces.withIndex()) {
                val startX = xForMm(piece.startPositionMm)
                val endX = xForMm(piece.endPositionMm)
                val pieceW = (endX - startX).coerceAtLeast(1f)
                val isSelected = selectedPiece?.pieceId == piece.pieceId

                val pieceColor = getPieceColor(idx)

                // Fill
                drawRoundRect(
                    color = if (isSelected) pieceColor.copy(alpha = 0.95f) else pieceColor.copy(alpha = 0.75f),
                    topLeft = Offset(startX, boardTop + 2.dp.toPx()),
                    size = Size(pieceW, boardH - 4.dp.toPx()),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )

                // Piece border
                drawRoundRect(
                    color = if (isSelected) Color.White else pieceColor.copy(alpha = 0.9f),
                    topLeft = Offset(startX, boardTop + 2.dp.toPx()),
                    size = Size(pieceW, boardH - 4.dp.toPx()),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                    style = Stroke(width = if (isSelected) 2.5f else 1f)
                )

                // Text inside piece (using nativeCanvas for clean diagram drawing)
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 10.dp.toPx()
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }

                // If wide enough, draw label and dimension
                if (pieceW > 35.dp.toPx()) {
                    val centerX = startX + pieceW / 2
                    val displayLabel = if (piece.label.length > 8 && pieceW < 60.dp.toPx()) {
                        piece.label.take(6) + ".."
                    } else piece.label

                    drawContext.canvas.nativeCanvas.drawText(
                        displayLabel,
                        centerX,
                        boardTop + (boardH / 2) - 2.dp.toPx(),
                        paint
                    )

                    val dimPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 8.5f.dp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        unit.formatMm(piece.lengthMm),
                        centerX,
                        boardTop + (boardH / 2) + 10.dp.toPx(),
                        dimPaint
                    )
                }

                // Saw kerf cut line at the end of the piece
                val cutLineX = endX
                drawLine(
                    color = KerfCutRed,
                    start = Offset(cutLineX, boardTop - 2.dp.toPx()),
                    end = Offset(cutLineX, boardTop + boardH + 2.dp.toPx()),
                    strokeWidth = 2f
                )
            }

            // Draw Leftover Section
            if (plan.leftoverMm > 0) {
                val lastPiece = plan.placedPieces.lastOrNull()
                val leftoverStartMm = if (lastPiece != null) {
                    lastPiece.endPositionMm + (plan.kerfLossMm / max(1, plan.cutsCount))
                } else plan.trimAllowanceMm
                val leftoverStartX = xForMm(leftoverStartMm).coerceAtMost(canvasW)
                val leftoverW = (canvasW - leftoverStartX).coerceAtLeast(0f)

                if (leftoverW > 0) {
                    val leftoverColor = if (plan.isLeftoverReusable) ReusableOffcutGreen else ScrapWasteSlate
                    drawRect(
                        color = leftoverColor.copy(alpha = 0.25f),
                        topLeft = Offset(leftoverStartX, boardTop + 2.dp.toPx()),
                        size = Size(leftoverW, boardH - 4.dp.toPx())
                    )

                    // Dashed border for remnant
                    drawRect(
                        color = leftoverColor,
                        topLeft = Offset(leftoverStartX, boardTop + 2.dp.toPx()),
                        size = Size(leftoverW, boardH - 4.dp.toPx()),
                        style = Stroke(
                            width = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                        )
                    )

                    if (leftoverW > 40.dp.toPx()) {
                        val remnantPaint = android.graphics.Paint().apply {
                            color = if (plan.isLeftoverReusable) android.graphics.Color.rgb(46, 125, 50) else android.graphics.Color.rgb(120, 144, 156)
                            textSize = 9.dp.toPx()
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        val title = if (plan.isLeftoverReusable) "Offcut" else "Scrap"
                        drawContext.canvas.nativeCanvas.drawText(
                            "$title ${unit.formatMm(plan.leftoverMm)}",
                            leftoverStartX + leftoverW / 2,
                            boardTop + (boardH / 2) + 3.dp.toPx(),
                            remnantPaint
                        )
                    }
                }
            }

            // Bottom Ruler / Measuring Tape
            val rulerY = boardTop + boardH + 4.dp.toPx()
            drawLine(
                color = Color(0xFF756A5E),
                start = Offset(0f, rulerY),
                end = Offset(canvasW, rulerY),
                strokeWidth = 1f
            )

            // Marks at 0, 25%, 50%, 75%, 100%
            val marks = listOf(0.0, 0.25, 0.5, 0.75, 1.0)
            val rulerPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(117, 106, 94)
                textSize = 8.dp.toPx()
                isAntiAlias = true
            }

            for (m in marks) {
                val markX = (m * canvasW).toFloat()
                drawLine(
                    color = Color(0xFF756A5E),
                    start = Offset(markX, rulerY),
                    end = Offset(markX, rulerY + 5.dp.toPx()),
                    strokeWidth = 1f
                )
                val markText = unit.formatMm(m * totalMm)
                val align = when (m) {
                    0.0 -> android.graphics.Paint.Align.LEFT
                    1.0 -> android.graphics.Paint.Align.RIGHT
                    else -> android.graphics.Paint.Align.CENTER
                }
                rulerPaint.textAlign = align
                drawContext.canvas.nativeCanvas.drawText(
                    markText,
                    markX,
                    rulerY + 14.dp.toPx(),
                    rulerPaint
                )
            }
        }
    }
}
