package com.microapps.buytomorrow

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.microapps.designsystem.AppHeader
import com.microapps.designsystem.GlassCard
import com.microapps.designsystem.GradientButton
import com.microapps.designsystem.NeonBackdrop
import com.microapps.designsystem.NeonChip
import com.microapps.designsystem.NeonCyan
import com.microapps.designsystem.NeonGreen
import com.microapps.designsystem.NeonOrange
import com.microapps.designsystem.NeonTextField
import com.microapps.designsystem.SecondaryButton
import com.microapps.designsystem.SectionTitle
import com.microapps.designsystem.StatusPill
import com.microapps.designsystem.TextPrimary
import com.microapps.designsystem.TextSecondary
import com.microapps.designsystem.UnifiedAppTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { UnifiedAppTheme { BuyTomorrowScreen() } }
    }
}

@Composable
private fun BuyTomorrowScreen() {
    val context = LocalContext.current
    var wishes by remember { mutableStateOf(loadWishes(context)) }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var waitHours by remember { mutableStateOf(48) }

    fun save(next: List<Wish>) { wishes = next; saveWishes(context, next) }
    fun updateStatus(target: Wish, status: String) { save(updateWishStatus(wishes, target.createdAt, status)) }
    val saved = savedAmount(wishes)

    NeonBackdrop {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AppHeader(
                icon = "🛒",
                title = "BuyTomorrow",
                subtitle = "Пауза перед импульсивной покупкой"
            )

            GlassCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Сохранено решениями", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Не потрачено: ${formatAmount(saved)}",
                            color = TextPrimary,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    StatusPill(if (saved > 0) "Экономия" else "Старт", NeonGreen)
                }
            }

            if (wishes.isNotEmpty()) {
                SectionTitle("Мои решения")
                wishes.sortedByDescending { it.createdAt }.forEach { wish ->
                    GlassCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(wish.name, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${formatAmount(wish.amount)} · пауза ${wish.waitHours} ч",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            when (wish.status) {
                                "skipped" -> StatusPill("Передумал", NeonGreen)
                                "bought" -> StatusPill("Куплено", NeonOrange)
                                else -> StatusPill("Ждём", NeonCyan)
                            }
                        }
                        if (wish.status == "waiting") {
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SecondaryButton(
                                    text = "Передумал",
                                    modifier = Modifier.weight(1f),
                                    onClick = { updateStatus(wish, "skipped") }
                                )
                                SecondaryButton(
                                    text = "Купил",
                                    modifier = Modifier.weight(1f),
                                    onClick = { updateStatus(wish, "bought") }
                                )
                            }
                        }
                    }
                }
            }

            GlassCard(Modifier.fillMaxWidth()) {
                SectionTitle("Новая покупка")
                Spacer(Modifier.height(10.dp))
                NeonTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Что хочется купить",
                    modifier = Modifier.fillMaxWidth().testTag("wish-name")
                )
                Spacer(Modifier.height(10.dp))
                NeonTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                    label = "Цена",
                    modifier = Modifier.fillMaxWidth().testTag("wish-price")
                )
                Spacer(Modifier.height(12.dp))
                Text("Сколько подождать?", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(24, 48, 72).forEach { hours ->
                        NeonChip(
                            text = "${hours}ч",
                            selected = waitHours == hours,
                            onClick = { waitHours = hours }
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                GradientButton(
                    text = "Отложить покупку",
                    leading = "⏳",
                    modifier = Modifier.fillMaxWidth().testTag("add-wish"),
                    onClick = {
                        val value = amount.replace(',', '.').toDoubleOrNull() ?: return@GradientButton
                        if (name.isNotBlank() && value >= 0) {
                            save(wishes + Wish(name.trim(), value, System.currentTimeMillis(), waitHours))
                            name = ""
                            amount = ""
                        }
                    }
                )
            }

            GradientButton(
                text = "Поделиться экономией",
                leading = "↗",
                modifier = Modifier.fillMaxWidth(),
                enabled = saved > 0,
                onClick = {
                    val text = "BuyTomorrow: я не потратил ${formatAmount(saved)}, просто откладывая импульсивные покупки."
                    val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
                    context.startActivity(Intent.createChooser(send, "Поделиться результатом"))
                }
            )
        }
    }
}

private fun formatAmount(value: Double): String = String.format(Locale.US, "%.2f", value)
private fun loadWishes(context: Context): List<Wish> = WishCodec.decode(context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("wishes", "").orEmpty())
private fun saveWishes(context: Context, wishes: List<Wish>) { context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putString("wishes", WishCodec.encode(wishes)).apply() }
