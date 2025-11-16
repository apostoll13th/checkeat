package com.checkeat.presentation.weight

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.checkeat.presentation.common.UiState
import java.text.SimpleDateFormat
import java.util.*

/**
 * Виджет для отслеживания веса
 */
@Composable
fun WeightWidget(
    viewModel: WeightViewModel = hiltViewModel()
) {
    val latestWeight by viewModel.latestWeight.collectAsState()
    val weightChange by viewModel.getWeightChange().collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                        Icons.Default.Scale,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Вес",
                        style = MaterialTheme.typography.titleMedium
                    )
                </Row>

                if (latestWeight != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${latestWeight!!.weightKg} кг",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        if (weightChange != null && weightChange != 0f) {
                            val change = weightChange!!
                            val sign = if (change > 0) "+" else ""
                            val color = if (change < 0)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error

                            Text(
                                "$sign${String.format("%.1f", change)} кг",
                                style = MaterialTheme.typography.labelMedium,
                                color = color
                            )
                        }
                    }
                } else {
                    Text(
                        "Нет записей",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            if (latestWeight != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale("ru")) }
                Text(
                    "Обновлено: ${dateFormat.format(latestWeight!!.date)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Записать вес")
            }
        }
    }

    if (showAddDialog) {
        AddWeightDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { weight, note ->
                viewModel.addWeight(weight, note)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddWeightDialog(
    onDismiss: () -> Unit,
    onAdd: (Float, String?) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Записать вес") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = {
                        // Разрешаем цифры и одну точку
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            weight = it
                        }
                    },
                    label = { Text("Вес (кг)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    suffix = { Text("кг") }
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Заметка (необязательно)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val weightFloat = weight.toFloatOrNull()
                    if (weightFloat != null && weightFloat > 0) {
                        onAdd(weightFloat, note.ifBlank { null })
                    }
                },
                enabled = weight.toFloatOrNull() != null && weight.toFloat() > 0
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
