package com.checkeat.presentation.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Улучшенные UI компоненты для экранов
 */

@Composable
fun CameraScreenContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = "Камера",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Сфотографируйте еду",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "AI проанализирует калории и питательность",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { /* TODO: Открыть камеру */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Сделать фото")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = { /* TODO: Выбрать из галереи */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Выбрать из галереи")
        }
    }
}

@Composable
fun HistoryScreenContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "История анализов",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        EmptyState(
            icon = Icons.Default.History,
            title = "Пока нет анализов",
            subtitle = "Сфотографируйте еду чтобы начать"
        )
    }
}

@Composable
fun StatsScreenContent() {
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
                    StatItem("Калории", "0", "ккал")
                    StatItem("Белки", "0", "г")
                    StatItem("Жиры", "0", "г")
                    StatItem("Углеводы", "0", "г")
                }
            }
        }
    }
}

@Composable
fun NotesScreenContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Заметки",
                style = MaterialTheme.typography.headlineMedium
            )
            FloatingActionButton(
                onClick = { /* TODO: Создать заметку */ },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        EmptyState(
            icon = Icons.Default.Notes,
            title = "Заметок пока нет",
            subtitle = "Создайте первую заметку"
        )
    }
}

@Composable
fun ProfileScreenContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Профиль",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Пользователь CheckEat",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))

        SettingsItem("Настройки", Icons.Default.Settings)
        SettingsItem("Экспорт данных", Icons.Default.Download)
        SettingsItem("О приложении", Icons.Default.Info)
        SettingsItem("Выйти", Icons.Default.ExitToApp)
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
fun SettingsItem(title: String, icon: ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
