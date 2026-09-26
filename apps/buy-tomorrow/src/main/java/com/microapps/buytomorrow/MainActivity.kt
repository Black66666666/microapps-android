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
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { BuyTomorrowScreen() } }
    }
}

data class Wish(val name: String, val amount: Double, val createdAt: Long, val waitHours: Int, val status: String = "waiting")

@Composable
private fun BuyTomorrowScreen() {
    val context = LocalContext.current
    var wishes by remember { mutableStateOf(loadWishes(context)) }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var waitHours by remember { mutableStateOf(48) }

    fun save(next: List<Wish>) { wishes = next; saveWishes(context, next) }
    fun updateStatus(target: Wish, status: String) {
        save(wishes.map { current -> if (current.createdAt == target.createdAt) current.copy(status = status) else current })
    }

    val saved = wishes.filter { it.status == "skipped" }.sumOf { it.amount }

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("BuyTomorrow", style = MaterialTheme.typography.headlineMedium)
        Text("Не запрещай себе покупку. Просто отложи решение.")
        Text("Не потрачено: ${"%.2f".format(saved)}", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(name, { name = it }, label = { Text("Что хочется купить") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(amount, { amount = it.filter { c -> c.isDigit() || c == '.' || c == ',' } }, label = { Text("Цена") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(24, 48, 72).forEach { hours ->
                Button(onClick = { waitHours = hours }, enabled = waitHours != hours) { Text("${hours}ч") }
            }
        }
        Button(onClick = {
            val value = amount.replace(',', '.').toDoubleOrNull() ?: return@Button
            if (name.isNotBlank()) {
                save(wishes + Wish(name.trim(), value, System.currentTimeMillis(), waitHours))
                name = ""
                amount = ""
            }
        }) { Text("Отложить покупку") }

        wishes.sortedByDescending { it.createdAt }.forEach { wish ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(wish.name, style = MaterialTheme.typography.titleMedium)
                    Text("${wish.amount} · пауза ${wish.waitHours} ч · ${wish.status}")
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
            val text = "BuyTomorrow: я не потратил ${"%.2f".format(saved)}, просто откладывая импульсивные покупки."
            val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
            context.startActivity(Intent.createChooser(send, "Поделиться результатом"))
        }) { Text("Поделиться экономией") }
    }
}

private fun loadWishes(context: Context): List<Wish> = runCatching {
    val raw = context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("wishes", "[]") ?: "[]"
    val array = JSONArray(raw)
    (0 until array.length()).map { i ->
        val o = array.getJSONObject(i)
        Wish(o.getString("name"), o.getDouble("amount"), o.getLong("createdAt"), o.getInt("waitHours"), o.optString("status", "waiting"))
    }
}.getOrDefault(emptyList())

private fun saveWishes(context: Context, wishes: List<Wish>) {
    val array = JSONArray()
    wishes.forEach { w ->
        array.put(JSONObject().apply {
            put("name", w.name)
            put("amount", w.amount)
            put("createdAt", w.createdAt)
            put("waitHours", w.waitHours)
            put("status", w.status)
        })
    }
    context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putString("wishes", array.toString()).apply()
}
