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
import kotlin.random.Random

@Composable
fun PackTogetherScreen() {
    val context = LocalContext.current
    var trip by remember { mutableStateOf(prefs(context).getString("pack_trip", "Поездка") ?: "Поездка") }
    var newItem by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(loadArray(context, "pack_items")) }
    fun save(next: JSONArray) { items = next; saveArray(context, "pack_items", next) }
    AppShell("PackTogether", "Общий список вещей перед поездкой") {
        FormField(trip, { trip = it; prefs(context).edit().putString("pack_trip", it).apply() }, "Поездка")
        FormField(newItem, { newItem = it }, "Что взять")
        PrimaryAction("Добавить", newItem.isNotBlank()) {
            val next = JSONArray(items.toString()); next.put(jsonObject("id" to newId(), "text" to newItem.trim(), "packed" to false)); save(next); newItem = ""
        }
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Checkbox(item.optBoolean("packed"), onCheckedChange = { checked -> val next = JSONArray(items.toString()); next.getJSONObject(i).put("packed", checked); save(next) })
                Text(item.optString("text"))
            }
        }
        PrimaryAction("Поделиться списком", items.length() > 0) {
            val body = buildString {
                appendLine(trip)
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    appendLine("${if (item.optBoolean("packed")) "✓" else "○"} ${item.optString("text")}")
                }
            }
            shareText(context, body, "Отправить список")
        }
    }
}

@Composable
fun PromiseScreen() {
    val context = LocalContext.current
    var person by remember { mutableStateOf("") }
    var promise by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(loadArray(context, "promises")) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    fun save(next: JSONArray) { items = next; saveArray(context, "promises", next) }
    AppShell("Promise", "Записывай обещания людям, а не только задачи себе") {
        FormField(person, { person = it }, "Кому")
        FormField(promise, { promise = it }, "Что обещал", singleLine = false)
        FormField(due, { due = it }, "До даты, YYYY-MM-DD")
        PrimaryAction("Сохранить обещание", person.isNotBlank() && promise.isNotBlank()) {
            val id = newId(); val next = JSONArray(items.toString()); val date = parseDateOrNull(due)
            next.put(jsonObject("id" to id, "person" to person.trim(), "promise" to promise.trim(), "due" to (date?.toString() ?: ""), "done" to false)); save(next)
            if (date != null) {
                if (Build.VERSION.SDK_INT >= 33) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                scheduleReminder(context, "promise-$id", millisAtStartOfDay(date).plus(9 * 60 * 60 * 1000L), "Обещание для ${person.trim()}", promise.trim())
            }
            person = ""; promise = ""; due = ""
        }
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            ItemCard {
                Text("${item.optString("person")}: ${item.optString("promise")}")
                if (item.optString("due").isNotBlank()) Text("До ${item.optString("due")}")
                Text(if (item.optBoolean("done")) "Выполнено" else "Открыто")
                if (!item.optBoolean("done")) Button(onClick = { val next = JSONArray(items.toString()); next.getJSONObject(i).put("done", true); save(next) }) { Text("Выполнено") }
            }
        }
    }
}

@Composable
fun GiftPocketScreen() {
    val context = LocalContext.current
    var person by remember { mutableStateOf("") }
    var idea by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(loadArray(context, "gifts")) }
    fun save(next: JSONArray) { items = next; saveArray(context, "gifts", next) }
    AppShell("GiftPocket", "Сохраняй идеи подарков тогда, когда они возникают") {
        FormField(person, { person = it }, "Для кого")
        FormField(idea, { idea = it }, "Идея подарка")
        FormField(note, { note = it }, "Ссылка / заметка", singleLine = false)
        PrimaryAction("Сохранить идею", person.isNotBlank() && idea.isNotBlank()) {
            val next = JSONArray(items.toString()); next.put(jsonObject("id" to newId(), "person" to person.trim(), "idea" to idea.trim(), "note" to note.trim(), "bought" to false)); save(next); idea = ""; note = ""
        }
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            ItemCard {
                Text("${item.optString("person")}: ${item.optString("idea")}")
                if (item.optString("note").isNotBlank()) Text(item.optString("note"))
                if (!item.optBoolean("bought")) Button(onClick = { val next = JSONArray(items.toString()); next.getJSONObject(i).put("bought", true); save(next) }) { Text("Куплено") } else Text("✓ Куплено")
            }
        }
    }
}

@Composable
fun BeforeLeaveScreen() {
    val context = LocalContext.current
    var itemText by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(loadArray(context, "leave_items")) }
    fun save(next: JSONArray) { items = next; saveArray(context, "leave_items", next) }
    AppShell("BeforeLeave", "Один короткий список перед выходом") {
        FormField(itemText, { itemText = it }, "Что проверить / взять")
        PrimaryAction("Добавить", itemText.isNotBlank()) {
            val next = JSONArray(items.toString()); next.put(jsonObject("id" to newId(), "text" to itemText.trim(), "checked" to false)); save(next); itemText = ""
        }
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Checkbox(item.optBoolean("checked"), onCheckedChange = { checked -> val next = JSONArray(items.toString()); next.getJSONObject(i).put("checked", checked); save(next) })
                Text(item.optString("text"))
            }
        }
        PrimaryAction("Новый выход — сбросить отметки", items.length() > 0) {
            val next = JSONArray(items.toString()); for (i in 0 until next.length()) next.getJSONObject(i).put("checked", false); save(next)
        }
    }
}

data class FairPerson(val name: String, val picks: Int)
fun chooseFair(people: List<FairPerson>, seed: Int = Random.nextInt()): Int? {
    if (people.isEmpty()) return null
    val min = people.minOf { it.picks }
    val candidates = people.indices.filter { people[it].picks == min }
    return candidates[kotlin.math.abs(seed % candidates.size)]
}

@Composable
fun FairPickScreen() {
    val context = LocalContext.current
    var person by remember { mutableStateOf("") }
    var people by remember { mutableStateOf(loadArray(context, "fair_people")) }
    var selected by remember { mutableStateOf<String?>(null) }
    fun save(next: JSONArray) { people = next; saveArray(context, "fair_people", next) }
    fun list() = (0 until people.length()).map { i -> people.getJSONObject(i).let { FairPerson(it.optString("name"), it.optInt("picks")) } }
    AppShell("FairPick", "Случайный выбор, который помнит прошлые результаты") {
        FormField(person, { person = it }, "Участник / вариант")
        PrimaryAction("Добавить", person.isNotBlank()) {
            val next = JSONArray(people.toString()); next.put(jsonObject("name" to person.trim(), "picks" to 0)); save(next); person = ""
        }
        PrimaryAction("Выбрать честно", people.length() > 0) {
            val index = chooseFair(list()) ?: return@PrimaryAction
            val next = JSONArray(people.toString()); val obj = next.getJSONObject(index); obj.put("picks", obj.optInt("picks") + 1); selected = obj.optString("name"); save(next)
        }
        selected?.let { ItemCard { Text("Выбрано: $it") } }
        list().forEach { ItemCard { Text(it.name); Text("Выбирался: ${it.picks} раз") } }
        Button(onClick = { val next = JSONArray(people.toString()); for (i in 0 until next.length()) next.getJSONObject(i).put("picks", 0); save(next); selected = null }) { Text("Сбросить историю") }
    }
}
