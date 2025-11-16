package com.checkeat.presentation.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.checkeat.presentation.common.UiState
import com.checkeat.presentation.goals.GoalsViewModel
import com.checkeat.presentation.water.WaterWidget
import com.checkeat.presentation.weight.WeightWidget
import com.checkeat.presentation.tasks.TasksWidget

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    goalsViewModel: GoalsViewModel = hiltViewModel()
) {
    val dailyStatsState by viewModel.dailyStatsState.collectAsState()
    val goalsState by goalsViewModel.goalsState.collectAsState()
    var showGoalsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Статистика",
                style = MaterialTheme.typography.headlineMedium
            )
            IconButton(onClick = { showGoalsDialog = true }) {
                Icon(Icons.Default.Settings, contentDescription = "Настроить цели")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Виджет веса
        WeightWidget()

        Spacer(modifier = Modifier.height(16.dp))

        // Водный виджет
        WaterWidget()

        Spacer(modifier = Modifier.height(16.dp))

        // Виджет ежедневных задач
        TasksWidget()

        Spacer(modifier = Modifier.height(16.dp))

        when (dailyStatsState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Success -> {
                val stats = (dailyStatsState as UiState.Success).data
                val goals = (goalsState as? UiState.Success)?.data

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Сегодня", style = MaterialTheme.typography.titleMedium)
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItemWithProgress(
                                "Калории",
                                stats.total_calories.toInt(),
                                goals?.caloriesGoal?.toInt() ?: 2000,
                                "ккал"
                            )
                            StatItemWithProgress(
                                "Белки",
                                stats.total_proteins.toInt(),
                                goals?.proteinsGoal?.toInt() ?: 150,
                                "г"
                            )
                            StatItemWithProgress(
                                "Жиры",
                                stats.total_fats.toInt(),
                                goals?.fatsGoal?.toInt() ?: 70,
                                "г"
                            )
                            StatItemWithProgress(
                                "Углеводы",
                                stats.total_carbs.toInt(),
                                goals?.carbsGoal?.toInt() ?: 250,
                                "г"
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Приёмов пищи: ${stats.meals_count}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопки переключения периода
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = true,
                        onClick = { viewModel.loadDailyStats() },
                        label = { Text("День") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.loadWeeklyStats() },
                        label = { Text("Неделя") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.loadMonthlyStats() },
                        label = { Text("Месяц") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            is UiState.Error -> {
                val error = (dailyStatsState as UiState.Error).message
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Ошибка: $error",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadDailyStats() }) {
                            Text("Повторить")
                        }
                    }
                }
            }
            else -> {}
        }

        if (showGoalsDialog) {
            GoalsDialog(
                goalsViewModel = goalsViewModel,
                onDismiss = { showGoalsDialog = false }
            )
        }
    }
}

@Composable
fun StatItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(unit, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun StatItemWithProgress(
    label: String,
    value: Int,
    goal: Int,
    unit: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text("$value", style = MaterialTheme.typography.titleLarge)
        Text("/ $goal $unit", style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(4.dp))
        val progress = if (goal > 0) (value.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.width(60.dp).height(4.dp),
        )
    }
}

@Composable
fun GoalsDialog(
    goalsViewModel: GoalsViewModel,
    onDismiss: () -> Unit
) {
    val goalsState by goalsViewModel.goalsState.collectAsState()
    val currentGoals = (goalsState as? UiState.Success)?.data

    var calories by remember { mutableStateOf(currentGoals?.caloriesGoal?.toInt()?.toString() ?: "2000") }
    var proteins by remember { mutableStateOf(currentGoals?.proteinsGoal?.toInt()?.toString() ?: "150") }
    var fats by remember { mutableStateOf(currentGoals?.fatsGoal?.toInt()?.toString() ?: "70") }
    var carbs by remember { mutableStateOf(currentGoals?.carbsGoal?.toInt()?.toString() ?: "250") }
    var water by remember { mutableStateOf(currentGoals?.waterGoalMl?.toString() ?: "2000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Дневные цели") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it.filter { char -> char.isDigit() } },
                    label = { Text("Калории (ккал)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = proteins,
                    onValueChange = { proteins = it.filter { char -> char.isDigit() } },
                    label = { Text("Белки (г)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fats,
                    onValueChange = { fats = it.filter { char -> char.isDigit() } },
                    label = { Text("Жиры (г)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it.filter { char -> char.isDigit() } },
                    label = { Text("Углеводы (г)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = water,
                    onValueChange = { water = it.filter { char -> char.isDigit() } },
                    label = { Text("Вода (мл)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    goalsViewModel.updateGoals(
                        caloriesGoal = calories.toFloatOrNull() ?: 2000f,
                        proteinsGoal = proteins.toFloatOrNull() ?: 150f,
                        fatsGoal = fats.toFloatOrNull() ?: 70f,
                        carbsGoal = carbs.toFloatOrNull() ?: 250f,
                        waterGoalMl = water.toIntOrNull() ?: 2000
                    )
                    onDismiss()
                }
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
