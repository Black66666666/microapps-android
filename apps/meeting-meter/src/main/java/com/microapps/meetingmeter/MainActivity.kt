package com.microapps.meetingmeter

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.microapps.designsystem.MeetingMeterAccent
import com.microapps.designsystem.TaviCard
import com.microapps.designsystem.TaviGraphite
import com.microapps.designsystem.TaviHeroObjectFrame
import com.microapps.designsystem.TaviPrimaryButton
import com.microapps.designsystem.TaviScreen
import com.microapps.designsystem.TaviSecondaryButton
import com.microapps.designsystem.TaviStatusPill
import com.microapps.designsystem.TaviSuccess
import com.microapps.designsystem.TaviTextField
import com.microapps.designsystem.TaviTextSecondary
import com.microapps.designsystem.TaviTheme
import com.microapps.designsystem.TaviTopBar
import kotlinx.coroutines.delay
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TaviTheme { MeetingMeterScreen() } }
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
    val cost = meetingCost(count, rate, elapsedMs)

    TaviScreen {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TaviTopBar(
                title = "MeetingMeter",
                subtitle = "Пусть стоимость встречи будет видна",
                accent = MeetingMeterAccent
            )

            TaviCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TaviStatusPill(
                        if (running) "Встреча идёт" else "Готов к старту",
                        if (running) TaviSuccess else MeetingMeterAccent
                    )
                    Text("$count участников", color = TaviTextSecondary, style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TaviTextField(
                        value = people,
                        onValueChange = { people = it.filter(Char::isDigit) },
                        label = "Участников",
                        accent = MeetingMeterAccent,
                        modifier = Modifier.weight(1f).testTag("people"),
                        enabled = !running
                    )
                    TaviTextField(
                        value = hourly,
                        onValueChange = { hourly = it },
                        label = "Цена часа",
                        accent = MeetingMeterAccent,
                        modifier = Modifier.weight(1f).testTag("hourly"),
                        enabled = !running
                    )
                }
                Spacer(Modifier.height(10.dp))
                TaviTextField(
                    value = currency,
                    onValueChange = { currency = it.take(3) },
                    label = "Валюта",
                    accent = MeetingMeterAccent,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !running
                )
            }

            TaviHeroObjectFrame(
                modifier = Modifier.size(220.dp),
                accent = if (running) TaviSuccess else MeetingMeterAccent
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        formatDuration(elapsedMs),
                        color = TaviGraphite,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("duration")
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("стоимость сейчас", color = TaviTextSecondary, style = MaterialTheme.typography.labelMedium)
                    Text(
                        "${String.format(Locale.US, "%.2f", cost)} $currency",
                        color = MeetingMeterAccent,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("cost")
                    )
                }
            }

            if (!running) {
                TaviPrimaryButton(
                    text = if (elapsedMs == 0L) "Начать встречу" else "Продолжить",
                    leading = "dot",
                    modifier = Modifier.fillMaxWidth().testTag("start"),
                    enabled = count > 0 && rate > 0,
                    onClick = {
                        startedAt = SystemClock.elapsedRealtime() - elapsedMs
                        running = true
                    }
                )
            } else {
                TaviPrimaryButton(
                    text = "Остановить",
                    leading = "dot",
                    modifier = Modifier.fillMaxWidth().testTag("stop"),
                    onClick = {
                        elapsedMs = SystemClock.elapsedRealtime() - startedAt
                        running = false
                    }
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TaviSecondaryButton(
                    text = "Сброс",
                    modifier = Modifier.weight(1f),
                    onClick = { running = false; elapsedMs = 0L; startedAt = 0L }
                )
                TaviSecondaryButton(
                    text = "Поделиться",
                    modifier = Modifier.weight(1f),
                    enabled = elapsedMs > 0,
                    onClick = {
                        val text = "MeetingMeter: $count участников · ${formatDuration(elapsedMs)} · стоимость встречи ${String.format(Locale.US, "%.2f", cost)} $currency"
                        val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
                        context.startActivity(Intent.createChooser(send, "Поделиться результатом"))
                    }
                )
            }

            TaviCard(Modifier.fillMaxWidth()) {
                Text("Формула", color = TaviGraphite, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Участники × средняя стоимость часа × длительность. Это ориентир, а не бухгалтерский расчёт.",
                    color = TaviTextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
