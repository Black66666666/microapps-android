package com.microapps.meetingmeter

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.microapps.designsystem.AppHeader
import com.microapps.designsystem.GlassCard
import com.microapps.designsystem.GradientButton
import com.microapps.designsystem.NeonBackdrop
import com.microapps.designsystem.NeonCyan
import com.microapps.designsystem.NeonGreen
import com.microapps.designsystem.NeonPink
import com.microapps.designsystem.NeonPurple
import com.microapps.designsystem.NeonTextField
import com.microapps.designsystem.SecondaryButton
import com.microapps.designsystem.StatusPill
import com.microapps.designsystem.TextPrimary
import com.microapps.designsystem.TextSecondary
import com.microapps.designsystem.UnifiedAppTheme
import kotlinx.coroutines.delay
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { UnifiedAppTheme { MeetingMeterScreen() } }
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

    NeonBackdrop {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppHeader(
                icon = "◷",
                title = "MeetingMeter",
                subtitle = "Пусть стоимость встречи будет видна"
            )

            GlassCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatusPill(if (running) "Встреча идёт" else "Готов к старту", if (running) NeonGreen else NeonCyan)
                    Text("$count участников", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NeonTextField(
                        value = people,
                        onValueChange = { people = it.filter(Char::isDigit) },
                        label = "Участников",
                        modifier = Modifier.weight(1f).testTag("people"),
                        enabled = !running
                    )
                    NeonTextField(
                        value = hourly,
                        onValueChange = { hourly = it },
                        label = "Цена часа",
                        modifier = Modifier.weight(1f).testTag("hourly"),
                        enabled = !running
                    )
                }
                Spacer(Modifier.height(10.dp))
                NeonTextField(
                    value = currency,
                    onValueChange = { currency = it.take(3) },
                    label = "Валюта",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !running
                )
            }

            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                NeonCyan.copy(alpha = 0.14f),
                                NeonPurple.copy(alpha = 0.09f),
                                com.microapps.designsystem.NightRaised.copy(alpha = 0.96f)
                            )
                        )
                    )
                    .border(4.dp, if (running) NeonGreen else NeonCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        formatDuration(elapsedMs),
                        color = TextPrimary,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("duration")
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("стоимость сейчас", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                    Text(
                        "${String.format(Locale.US, "%.2f", cost)} $currency",
                        color = if (running) NeonPink else NeonCyan,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("cost")
                    )
                }
            }

            if (!running) {
                GradientButton(
                    text = if (elapsedMs == 0L) "Начать встречу" else "Продолжить",
                    leading = "▶",
                    modifier = Modifier.fillMaxWidth().testTag("start"),
                    enabled = count > 0 && rate > 0,
                    onClick = {
                        startedAt = SystemClock.elapsedRealtime() - elapsedMs
                        running = true
                    }
                )
            } else {
                GradientButton(
                    text = "Остановить",
                    leading = "Ⅱ",
                    modifier = Modifier.fillMaxWidth().testTag("stop"),
                    onClick = {
                        elapsedMs = SystemClock.elapsedRealtime() - startedAt
                        running = false
                    }
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(
                    text = "Сброс",
                    modifier = Modifier.weight(1f),
                    onClick = { running = false; elapsedMs = 0L; startedAt = 0L }
                )
                SecondaryButton(
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

            GlassCard(Modifier.fillMaxWidth()) {
                Text("Формула", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Участники × средняя стоимость часа × длительность. Это ориентир, а не бухгалтерский расчёт.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
