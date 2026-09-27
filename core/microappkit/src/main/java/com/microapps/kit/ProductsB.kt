package com.microapps.kit

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import java.time.temporal.ChronoUnit
import kotlin.random.Random

fun nextTurn(current: Int, size: Int): Int = if (size <= 0) 0 else (current + 1) % size

@Composable
fun TurnKeeperScreen() {
    val context = LocalContext.current
    var peopleText by remember { mutableStateOf(prefs(context).getString("turn_people", "") ?: "") }
    var current by remember { mutableStateOf(prefs(context).getInt("turn_current", 0)) }
    val people = peopleText.lines().map { it.trim() }.filter { it.isNotEmpty() }
    AppShell("TurnKeeper", "Очередь без споров: кто следующий") {
        FormField(peopleText, {
            peopleText = it
            prefs(context).edit().putString("turn_people", it).apply()
            if (current >= it.lines().count { line -> line.isNotBlank() }) current = 0
        }, "Участники, по одному в строке", singleLine = false)
        if (people.isNotEmpty()) {
            ItemCard {
                Text("Сейчас очередь: ${people[current.coerceIn(0, people.lastIndex)]}")
                Text("Следующий: ${people[nextTurn(current, people.size)]}")
            }
            PrimaryAction("Передать очередь") {
                current = nextTurn(current, people.size)
                prefs(context).edit().putInt("turn_current", current).apply()
            }
            Button(onClick = { current = 0; prefs(context).edit().putInt("turn_current", 0).apply() }) { Text("Сбросить") }
        }
    }
}

data class TimedTask(val id: String, val text: String, val minutes: Int)

fun pickTask(tasks: List<TimedTask>, availableMinutes: Int, seed: Int = Random.nextInt()): TimedTask? {
    val eligible = tasks.filter { it.minutes <= availableMinutes }
    return if (eligible.isEmpty()) null else eligible[kotlin.math.abs(seed % eligible.size)]
}

@Composable
fun FiveMinutesScreen() {
    val context = LocalContext.current
    var task by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("5") }
    var available by remember { mutableStateOf("5") }
    var items by remember { mutableStateOf(loadArray(context, "quick_tasks")) }
    var suggestion by remember { mutableStateOf<TimedTask?>(null) }
    fun save(next: JSONArray) { items = next; saveArray(context, "quick_tasks", next) }
    fun taskList() = (0 until items.length()).map { i -> items.getJSONObject(i).let { TimedTask(it.optString("id"), it.optString("text"), it.optInt("minutes")) } }
    AppShell("FiveMinutes", "Есть несколько минут? Получи одно подходящее дело") {
        FormField(task, { task = it }, "Дело")
        FormField(minutes, { minutes = it.filter(Char::isDigit) }, "Сколько минут займёт")
        PrimaryAction("Добавить дело", task.isNotBlank() && (minutes.toIntOrNull() ?: 0) > 0) {
            val next = JSONArray(items.toString()); next.put(jsonObject("id" to newId(), "text" to task.trim(), "minutes" to minutes.toInt())); save(next); task = ""
        }
        FormField(available, { available = it.filter(Char::isDigit) }, "Сколько минут есть сейчас")
        PrimaryAction("Что успеть?", items.length() > 0 && (available.toIntOrNull() ?: 0) > 0) { suggestion = pickTask(taskList(), available.toInt()) }
        suggestion?.let { ItemCard { Text(it.text); Text("≈ ${it.minutes} мин") } } ?: run { if (items.length() > 0) Text("Нажми «Что успеть?», чтобы получить одно дело") }
        for (item in taskList()) ItemCard { Text(item.text); Text("${item.minutes} мин") }
    }
}

fun daysUntil(date: java.time.LocalDate): Long = ChronoUnit.DAYS.between(today(), date)

@Composable
fun ReturnClockScreen() {
    val context = LocalContext.current
    var product by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(loadArray(context, "returns")) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    fun save(next: JSONArray) { items = next; saveArray(context, "returns", next) }
    AppShell("ReturnClock", "Не пропусти последний день возврата покупки") {
        FormField(product, { product = it }, "Покупка")
        FormField(deadline, { deadline = it }, "Вернуть до, YYYY-MM-DD")
        PrimaryAction("Добавить срок", product.isNotBlank() && parseDateOrNull(deadline) != null) {
            val date = parseDateOrNull(deadline)!!
            val id = newId(); val next = JSONArray(items.toString())
            next.put(jsonObject("id" to id, "product" to product.trim(), "deadline" to date.toString(), "done" to false)); save(next)
            if (Build.VERSION.SDK_INT >= 33) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            scheduleReminder(context, "return-$id", millisAtStartOfDay(date.minusDays(1)).plusHours(10), "Срок возврата завтра", product.trim())
            product = ""; deadline = ""
        }
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i); val date = parseDateOrNull(item.optString("deadline")) ?: today(); val left = daysUntil(date)
            ItemCard {
                Text(item.optString("product"))
                Text("Срок: $date · ${if (left >= 0) "осталось $left дн." else "просрочено ${-left} дн."}")
                if (!item.optBoolean("done")) Button(onClick = { val next = JSONArray(items.toString()); next.getJSONObject(i).put("done", true); save(next) }) { Text("Возврат закрыт") }
            }
        }
    }
}

@Composable
fun WhereIsItScreen() {
    val context = LocalContext.current
    var thing by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(loadArray(context, "locations")) }
    fun save(next: JSONArray) { items = next; saveArray(context, "locations", next) }
    AppShell("WhereIsIt", "Запомни, куда положил вещь") {
        FormField(thing, { thing = it }, "Вещь")
        FormField(place, { place = it }, "Где лежит")
        PrimaryAction("Сохранить", thing.isNotBlank() && place.isNotBlank()) {
            val next = JSONArray(items.toString()); next.put(jsonObject("id" to newId(), "thing" to thing.trim(), "place" to place.trim())); save(next); thing = ""; place = ""
        }
        FormField(query, { query = it }, "Поиск")
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            if (query.isBlank() || item.optString("thing").contains(query, true) || item.optString("place").contains(query, true)) {
                ItemCard { Text(item.optString("thing")); Text("Лежит: ${item.optString("place")}") }
            }
        }
    }
}

fun openedExpiry(opened: java.time.LocalDate, days: Int): java.time.LocalDate = opened.plusDays(days.coerceAtLeast(1).toLong())

@Composable
fun OpenedOnScreen() {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("7") }
    var items by remember { mutableStateOf(loadArray(context, "opened")) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    fun save(next: JSONArray) { items = next; saveArray(context, "opened", next) }
    AppShell("OpenedOn", "Срок после вскрытия — без наклеек и гаданий") {
        FormField(name, { name = it }, "Продукт")
        FormField(days, { days = it.filter(Char::isDigit) }, "Годен после вскрытия, дней")
        PrimaryAction("Открыл сегодня", name.isNotBlank() && (days.toIntOrNull() ?: 0) > 0) {
            val id = newId(); val expiry = openedExpiry(today(), days.toInt()); val next = JSONArray(items.toString())
            next.put(jsonObject("id" to id, "name" to name.trim(), "opened" to today().toString(), "days" to days.toInt())); save(next)
            if (Build.VERSION.SDK_INT >= 33) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            scheduleReminder(context, "opened-$id", millisAtStartOfDay(expiry).plusHours(9), "Срок после вскрытия", "Проверь ${name.trim()}")
            name = ""
        }
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i); val opened = parseDateOrNull(item.optString("opened")) ?: today(); val expiry = openedExpiry(opened, item.optInt("days", 1)); val left = daysUntil(expiry)
            ItemCard { Text(item.optString("name")); Text("Открыто: $opened"); Text("Использовать до: $expiry · ${if (left >= 0) "$left дн." else "срок вышел"}") }
        }
    }
}

private fun Long.plusHours(hours: Long): Long = this + hours * 60L * 60L * 1000L
