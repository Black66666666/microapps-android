package com.microapps.meetingmeter

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { MeetingMeterScreen() } }
    }
}

@Composable
private fun MeetingMeterScreen() {
    val context = LocalContext.current
    var people by remember { mutableStateOf("6") }
    var hourly by remember { mutableStateOf("40") }
    var currency by remember { mutableStateOf("€") }
    var running by remember { mutableStateOf(false) }
    var startedAt by remember { mutableLongStateOf(0L) }
    var elapsedMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(running, startedAt) {
        while (running) {
            elapsedMs = SystemClock.elapsedRealtime() - startedAt
            delay(250)
        }
    }

    val count = people.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val rate = hourly.replace(',', '.').toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val seconds = elapsedMs / 1000.0
    val cost = count * rate * seconds / 3600.0

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("MeetingMeter", style = MaterialTheme.typography.headlineMedium)
        Text("Сколько денег прямо сейчас стоит эта встреча?")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(people, { people = it.filter(Char::isDigit) }, label = { Text("Участников") }, modifier = Modifier.weight(1f), enabled = !running)
            OutlinedTextField(hourly, { hourly = it }, label = { Text("Стоимость часа") }, modifier = Modifier.weight(1f), enabled = !running)
        }
        OutlinedTextField(currency, { currency = it.take(3) }, label = { Text("Валюта") }, enabled = !running)
        Text("${formatDuration(elapsedMs)}", style = MaterialTheme.typography.displaySmall)
        Text("${"%.2f".format(cost)} $currency", style = MaterialTheme.typography.displayMedium)
        if (!running) {
            Button(onClick = { startedAt = SystemClock.elapsedRealtime() - elapsedMs; running = true }, enabled = count > 0 && rate > 0) { Text(if (elapsedMs == 0L) "Начать встречу" else "Продолжить") }
        } else {
            Button(onClick = { elapsedMs = SystemClock.elapsedRealtime() - startedAt; running = false }) { Text("Остановить") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { running = false; elapsedMs = 0L; startedAt = 0L }) { Text("Сброс") }
            Button(enabled = elapsedMs > 0, onClick = {
                val text = "MeetingMeter: ${count} участников · ${formatDuration(elapsedMs)} · стоимость встречи ${"%.2f".format(cost)} $currency"
                val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
                context.startActivity(Intent.createChooser(send, "Поделиться результатом"))
            }) { Text("Поделиться") }
        }
        Text("Расчёт приблизительный: участники × средняя стоимость часа × длительность.", style = MaterialTheme.typography.bodySmall)
    }
}

private fun formatDuration(ms: Long): String {
    val total = ms / 1000
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
