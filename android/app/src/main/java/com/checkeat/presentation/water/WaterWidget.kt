package com.checkeat.presentation.water

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Компактный виджет водного баланса для StatsScreen
 */
@Composable
fun WaterWidget(
    viewModel: WaterViewModel = hiltViewModel()
) {
    val waterTotal by viewModel.todayWaterTotal.collectAsState()
    val waterGoal by viewModel.waterGoal.collectAsState()
    val progress by viewModel.waterProgress.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Вода",
                        style = MaterialTheme.typography.titleMedium
                    )
                </Row>
                Text(
                    "$waterTotal / $waterGoal мл",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Прогресс-бар
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Быстрые кнопки добавления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAddButton(
                    text = "200 мл",
                    onClick = { viewModel.addWater(200) },
                    modifier = Modifier.weight(1f)
                )
                QuickAddButton(
                    text = "250 мл",
                    onClick = { viewModel.addWater(250) },
                    modifier = Modifier.weight(1f)
                )
                QuickAddButton(
                    text = "500 мл",
                    onClick = { viewModel.addWater(500) },
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Другое количество")
                }
            }
        }
    }

    if (showAddDialog) {
        AddWaterDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { amount ->
                viewModel.addWater(amount)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun QuickAddButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun AddWaterDialog(
    onDismiss: () -> Unit,
    onAdd: (Int) -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить воду") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { char -> char.isDigit() } },
                label = { Text("Количество (мл)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountInt = amount.toIntOrNull()
                    if (amountInt != null && amountInt > 0) {
                        onAdd(amountInt)
                    }
                },
                enabled = amount.toIntOrNull() != null && amount.toInt() > 0
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
