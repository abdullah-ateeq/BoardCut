package com.example.boardcut.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.boardcut.model.CalculationSettings
import com.example.boardcut.model.StockBoard
import com.example.boardcut.model.UnitOfMeasurement

@Composable
fun StockInputCard(
    stockBoards: List<StockBoard>,
    settings: CalculationSettings,
    unit: UnitOfMeasurement,
    onStockBoardsChange: (List<StockBoard>) -> Unit,
    onSettingsChange: (CalculationSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var isAdvancedExpanded by remember { mutableStateOf(false) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SquareFoot,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Stock Boards Setup",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(onClick = { isAdvancedExpanded = !isAdvancedExpanded }) {
                    Text(
                        text = if (isAdvancedExpanded) "Less" else "Options",
                        fontSize = 13.sp
                    )
                    Icon(
                        imageVector = if (isAdvancedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stock board entries
            stockBoards.forEachIndexed { index, stock ->
                StockBoardEntryRow(
                    stock = stock,
                    index = index,
                    unit = unit,
                    canDelete = stockBoards.size > 1,
                    onUpdate = { updated ->
                        val list = stockBoards.toMutableList()
                        list[index] = updated
                        onStockBoardsChange(list)
                    },
                    onDelete = {
                        val list = stockBoards.toMutableList()
                        list.removeAt(index)
                        onStockBoardsChange(list)
                    }
                )
                if (index < stockBoards.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Add additional stock board length button
            OutlinedButton(
                onClick = {
                    val defaultLen = if (unit == UnitOfMeasurement.MM) 2440.0 else if (unit == UnitOfMeasurement.INCH) 96.0 else 240.0
                    val nextName = "Stock #${stockBoards.size + 1}"
                    onStockBoardsChange(stockBoards + StockBoard(name = nextName, length = defaultLen))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Another Stock Length", fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Saw Blade Kerf & Trim Allowance Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                var kerfText by remember(settings.kerf) {
                    mutableStateOf(settings.kerf.toString().removeSuffix(".0"))
                }
                OutlinedTextField(
                    value = kerfText,
                    onValueChange = { str ->
                        val filtered = str.filter { it.isDigit() || it == '.' }
                        kerfText = filtered
                        val parsed = filtered.toDoubleOrNull() ?: 0.0
                        onSettingsChange(settings.copy(kerf = parsed))
                    },
                    label = { Text("Saw Kerf (${unit.symbol})", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                var trimText by remember(settings.trimAllowance) {
                    mutableStateOf(settings.trimAllowance.toString().removeSuffix(".0"))
                }
                OutlinedTextField(
                    value = trimText,
                    onValueChange = { str ->
                        val filtered = str.filter { it.isDigit() || it == '.' }
                        trimText = filtered
                        val parsed = filtered.toDoubleOrNull() ?: 0.0
                        onSettingsChange(settings.copy(trimAllowance = parsed))
                    },
                    label = { Text("End Trim (${unit.symbol})", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // Advanced options (min reusable offcut threshold)
            AnimatedVisibility(visible = isAdvancedExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    var minOffcutText by remember(settings.minUsableOffcut) {
                        mutableStateOf(settings.minUsableOffcut.toString().removeSuffix(".0"))
                    }
                    OutlinedTextField(
                        value = minOffcutText,
                        onValueChange = { str ->
                            val filtered = str.filter { it.isDigit() || it == '.' }
                            minOffcutText = filtered
                            val parsed = filtered.toDoubleOrNull() ?: 0.0
                            onSettingsChange(settings.copy(minUsableOffcut = parsed))
                        },
                        label = { Text("Min Reusable Offcut (${unit.symbol})", fontSize = 12.sp) },
                        supportingText = {
                            Text(
                                "Offcuts longer than this are tracked as reusable stock drops.",
                                fontSize = 11.sp
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun StockBoardEntryRow(
    stock: StockBoard,
    index: Int,
    unit: UnitOfMeasurement,
    canDelete: Boolean,
    onUpdate: (StockBoard) -> Unit,
    onDelete: () -> Unit
) {
    var lengthText by remember(stock.length) {
        mutableStateOf(stock.length.toString().removeSuffix(".0"))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = stock.name,
            onValueChange = { onUpdate(stock.copy(name = it)) },
            label = { Text("Stock Label", fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.weight(1.2f),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        OutlinedTextField(
            value = lengthText,
            onValueChange = { str ->
                val filtered = str.filter { it.isDigit() || it == '.' }
                lengthText = filtered
                val parsed = filtered.toDoubleOrNull() ?: 0.0
                onUpdate(stock.copy(length = parsed))
            },
            label = { Text("Length (${unit.symbol})", fontSize = 11.sp) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (canDelete) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Remove stock",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
