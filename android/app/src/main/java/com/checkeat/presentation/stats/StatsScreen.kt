package com.checkeat.presentation.stats

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.checkeat.presentation.common.UiState

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val dailyStatsState by viewModel.dailyStatsState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Статистика",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

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
                            StatItem(
                                "Калории",
                                stats.total_calories.toInt().toString(),
                                "ккал"
                            )
                            StatItem(
                                "Белки",
                                stats.total_proteins.toInt().toString(),
                                "г"
                            )
                            StatItem(
                                "Жиры",
                                stats.total_fats.toInt().toString(),
                                "г"
                            )
                            StatItem(
                                "Углеводы",
                                stats.total_carbs.toInt().toString(),
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
