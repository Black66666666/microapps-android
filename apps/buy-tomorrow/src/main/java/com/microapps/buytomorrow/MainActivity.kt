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
import com.microapps.designsystem.BuyTomorrowAccent
import com.microapps.designsystem.TaviCard
import com.microapps.designsystem.TaviChip
import com.microapps.designsystem.TaviGraphite
import com.microapps.designsystem.TaviPrimaryButton
import com.microapps.designsystem.TaviScreen
import com.microapps.designsystem.TaviSecondaryButton
import com.microapps.designsystem.TaviSectionTitle
import com.microapps.designsystem.TaviStatusPill
import com.microapps.designsystem.TaviSuccess
import com.microapps.designsystem.TaviTextField
import com.microapps.designsystem.TaviTextSecondary
import com.microapps.designsystem.TaviTheme
import com.microapps.designsystem.TaviTopBar
import com.microapps.designsystem.TaviWarmOrange
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TaviTheme { BuyTomorrowScreen() } }
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

    TaviScreen {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TaviTopBar(
                title = "BuyTomorrow",
                subtitle = "Хорошие покупки начинаются с паузы",
                accent = BuyTomorrowAccent
            )

            TaviCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Сохранено решениями", color = TaviTextSecondary, style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Не потрачено: ${formatAmount(saved)}",
                            color = TaviGraphite,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    TaviStatusPill(if (saved > 0) "Экономия" else "Старт", if (saved > 0) TaviSuccess else BuyTomorrowAccent)
                }
            }

            if (wishes.isNotEmpty()) {
                TaviSectionTitle("Мои решения")
                wishes.sortedByDescending { it.createdAt }.forEach { wish ->
                    TaviCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(wish.name, color = TaviGraphite, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${formatAmount(wish.amount)} · пауза ${wish.waitHours} ч",
                                    color = TaviTextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            when (wish.status) {
                                "skipped" -> TaviStatusPill("Передумал", TaviSuccess)
                                "bought" -> TaviStatusPill("Куплено", TaviWarmOrange)
                                else -> TaviStatusPill("Ждём", BuyTomorrowAccent)
                            }
                        }
                        if (wish.status == "waiting") {
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TaviSecondaryButton(
                                    text = "Передумал",
                                    modifier = Modifier.weight(1f),
                                    onClick = { updateStatus(wish, "skipped") }
                                )
                                TaviSecondaryButton(
                                    text = "Купил",
                                    modifier = Modifier.weight(1f),
                                    onClick = { updateStatus(wish, "bought") }
                                )
                            }
                        }
                    }
                }
            }

            TaviCard(Modifier.fillMaxWidth()) {
                TaviSectionTitle("Новая покупка")
                Spacer(Modifier.height(10.dp))
                TaviTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Что хочется купить",
                    accent = BuyTomorrowAccent,
                    modifier = Modifier.fillMaxWidth().testTag("wish-name")
                )
                Spacer(Modifier.height(10.dp))
                TaviTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                    label = "Цена",
                    accent = BuyTomorrowAccent,
                    modifier = Modifier.fillMaxWidth().testTag("wish-price")
                )
                Spacer(Modifier.height(12.dp))
                Text("Сколько подождать?", color = TaviTextSecondary, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(24, 48, 72).forEach { hours ->
                        TaviChip(
                            text = "${hours}ч",
                            selected = waitHours == hours,
                            accent = BuyTomorrowAccent,
                            onClick = { waitHours = hours }
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                TaviPrimaryButton(
                    text = "Отложить покупку",
                    leading = "dot",
                    modifier = Modifier.fillMaxWidth().testTag("add-wish"),
                    onClick = {
                        val value = amount.replace(',', '.').toDoubleOrNull() ?: return@TaviPrimaryButton
                        if (name.isNotBlank() && value >= 0) {
                            save(wishes + Wish(name.trim(), value, System.currentTimeMillis(), waitHours))
                            name = ""
                            amount = ""
                        }
                    }
                )
            }

            TaviPrimaryButton(
                text = "Поделиться экономией",
                leading = "dot",
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
