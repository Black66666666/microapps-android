package com.microapps.buytomorrow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { BuyTomorrowScreen() } }
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

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("BuyTomorrow", style = MaterialTheme.typography.headlineMedium)
        Text("Не запрещай себе покупку. Просто отложи решение.")
        Text("Не потрачено: ${formatAmount(saved)}", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(name, { name = it }, label = { Text("Что хочется купить") }, modifier = Modifier.fillMaxWidth().testTag("wish-name"))
        OutlinedTextField(amount, { amount = it.filter { c -> c.isDigit() || c == '.' || c == ',' } }, label = { Text("Цена") }, modifier = Modifier.fillMaxWidth().testTag("wish-price"))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(24, 48, 72).forEach { hours -> Button(onClick = { waitHours = hours }, enabled = waitHours != hours) { Text("${hours}ч") } }
        }
        Button(modifier = Modifier.testTag("add-wish"), onClick = {
            val value = amount.replace(',', '.').toDoubleOrNull() ?: return@Button
            if (name.isNotBlank() && value >= 0) {
                save(wishes + Wish(name.trim(), value, System.currentTimeMillis(), waitHours))
                name = ""; amount = ""
            }
        }) { Text("Отложить покупку") }

        wishes.sortedByDescending { it.createdAt }.forEach { wish ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(wish.name, style = MaterialTheme.typography.titleMedium)
                    Text("${formatAmount(wish.amount)} · пауза ${wish.waitHours} ч · ${wish.status}")
                    if (wish.status == "waiting") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { updateStatus(wish, "skipped") }) { Text("Передумал") }
                            Button(onClick = { updateStatus(wish, "bought") }) { Text("Купил") }
                        }
                    }
                }
            }
        }
        Button(enabled = saved > 0, onClick = {
            val text = "BuyTomorrow: я не потратил ${formatAmount(saved)}, просто откладывая импульсивные покупки."
            val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
            context.startActivity(Intent.createChooser(send, "Поделиться результатом"))
        }) { Text("Поделиться экономией") }
    }
}

private fun formatAmount(value: Double): String = String.format(Locale.US, "%.2f", value)
private fun loadWishes(context: Context): List<Wish> = WishCodec.decode(context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("wishes", "").orEmpty())
private fun saveWishes(context: Context, wishes: List<Wish>) { context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putString("wishes", WishCodec.encode(wishes)).apply() }
