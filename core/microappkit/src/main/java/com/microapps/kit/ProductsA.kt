package com.microapps.kit

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONArray
import java.time.Duration
import java.time.LocalDate

private val shortVideoPackages = setOf(
    "com.zhiliaoapp.musically",
    "com.ss.android.ugc.trill",
    "com.instagram.android",
    "com.google.android.youtube"
)

fun usageAccessGranted(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    return appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName) == AppOpsManager.MODE_ALLOWED
}

fun shortVideoMinutesToday(context: Context): Long {
    if (!usageAccessGranted(context)) return 0
    val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val start = millisAtStartOfDay(today())
    val end = System.currentTimeMillis()
    return manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
        .filter { it.packageName in shortVideoPackages }
        .sumOf { it.totalTimeInForeground }
        .let { Duration.ofMillis(it).toMinutes() }
}

@Composable
fun ScrollReceiptScreen() {
    val context = LocalContext.current
    var refresh by remember { mutableIntStateOf(0) }
    val granted = usageAccessGranted(context)
    val minutes = remember(refresh, granted) { shortVideoMinutesToday(context) }
    val clips = minutes * 6
    AppShell("Scroll Receipt", "Чек за сегодняшний скроллинг коротких видео") {
        ItemCard {
            Text("Сегодня: $minutes мин")
            Text("Оценочно просмотрено: ≈ $clips коротких роликов")
            Text("Считается время TikTok, Instagram и YouTube. Количество роликов — приблизительная оценка.")
        }
        if (!granted) {
            PrimaryAction("Разрешить статистику использования") {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        } else {
            PrimaryAction("Обновить") { refresh++ }
            PrimaryAction("Поделиться чеком") {
                shareText(context, "Scroll Receipt: сегодня я провёл $minutes мин в TikTok / Reels / YouTube и посмотрел примерно $clips коротких роликов.")
            }
        }
    }
}

fun costPerUse(price: Double, uses: Int): Double = if (uses <= 0) price else price / uses

@Composable
fun WorthItScreen() {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(loadArray(context, "worth_items")) }
    fun save(next: JSONArray) { items = next; saveArray(context, "worth_items", next) }
    AppShell("WorthIt", "Считай реальную стоимость вещи за одно использование") {
        FormField(name, { name = it }, "Вещь")
        FormField(price, { price = it.filter { c -> c.isDigit() || c == '.' || c == ',' } }, "Цена")
        PrimaryAction("Добавить", name.isNotBlank() && price.replace(',', '.').toDoubleOrNull() != null) {
            val next = JSONArray(items.toString())
            next.put(jsonObject("id" to newId(), "name" to name.trim(), "price" to price.replace(',', '.').toDouble(), "uses" to 0))
            save(next); name = ""; price = ""
        }
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val uses = item.optInt("uses")
            val itemPrice = item.optDouble("price")
            ItemCard {
                Text(item.optString("name"))
                Text("Использований: $uses")
                Text("Стоимость за использование: %.2f".format(costPerUse(itemPrice, uses)))
                Button(onClick = {
                    val next = JSONArray(items.toString()); val updated = next.getJSONObject(i); updated.put("uses", uses + 1); save(next)
                }) { Text("Использовал ещё раз") }
            }
        }
    }
}

@Composable
fun BorrowBackScreen() {
    val context = LocalContext.current
    var thing by remember { mutableStateOf("") }
    var person by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(loadArray(context, "borrow_items")) }
    fun save(next: JSONArray) { items = next; saveArray(context, "borrow_items", next) }
    AppShell("BorrowBack", "Не забывай, кому и что одолжил") {
        FormField(thing, { thing = it }, "Что отдал")
        FormField(person, { person = it }, "Кому")
        FormField(due, { due = it }, "Вернуть до, YYYY-MM-DD")
        PrimaryAction("Запомнить", thing.isNotBlank() && person.isNotBlank()) {
            val next = JSONArray(items.toString())
            next.put(jsonObject("id" to newId(), "thing" to thing.trim(), "person" to person.trim(), "due" to due.trim(), "returned" to false))
            save(next); thing = ""; person = ""; due = ""
        }
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            ItemCard {
                Text("${item.optString("thing")} → ${item.optString("person")}")
                if (item.optString("due").isNotBlank()) Text("Срок: ${item.optString("due")}")
                Text(if (item.optBoolean("returned")) "Возвращено" else "Ещё не вернули")
                if (!item.optBoolean("returned")) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { shareText(context, "Привет! Напоминаю про ${item.optString("thing")}, которое я тебе одолжил${if (item.optString("due").isNotBlank()) " до ${item.optString("due")}" else ""}.") }) { Text("Напомнить") }
                        Button(onClick = { val next = JSONArray(items.toString()); next.getJSONObject(i).put("returned", true); save(next) }) { Text("Вернули") }
                    }
                }
            }
        }
    }
}

fun createQrBitmap(text: String, size: Int = 640): Bitmap {
    val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
    return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also { bitmap ->
        for (x in 0 until size) for (y in 0 until size) bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
    }
}

@Composable
fun BoxQrScreen() {
    val context = LocalContext.current
    var boxName by remember { mutableStateOf("") }
    var contents by remember { mutableStateOf("") }
    var boxes by remember { mutableStateOf(loadArray(context, "boxes")) }
    var shownQr by remember { mutableStateOf<String?>(null) }
    fun save(next: JSONArray) { boxes = next; saveArray(context, "boxes", next) }
    AppShell("BoxQR", "Подпиши коробку QR-кодом и быстро находи содержимое") {
        FormField(boxName, { boxName = it }, "Название коробки")
        FormField(contents, { contents = it }, "Что внутри, через запятую", singleLine = false)
        PrimaryAction("Создать коробку", boxName.isNotBlank()) {
            val id = newId(); val next = JSONArray(boxes.toString())
            next.put(jsonObject("id" to id, "name" to boxName.trim(), "contents" to contents.trim()))
            save(next); shownQr = id; boxName = ""; contents = ""
        }
        shownQr?.let { id ->
            val uri = "boxqr://box/$id"
            Image(bitmap = remember(id) { createQrBitmap(uri).asImageBitmap() }, contentDescription = "QR коробки", modifier = Modifier.fillMaxWidth().height(260.dp))
            Text("QR содержит ссылку $uri")
        }
        for (i in 0 until boxes.length()) {
            val box = boxes.getJSONObject(i)
            ItemCard {
                Text(box.optString("name"))
                Text(box.optString("contents").ifBlank { "Содержимое пока не указано" })
                Button(onClick = { shownQr = box.optString("id") }) { Text("Показать QR") }
            }
        }
    }
}

fun nextRefillDate(last: LocalDate, intervalDays: Int): LocalDate = last.plusDays(intervalDays.coerceAtLeast(1).toLong())

@Composable
fun RefillScreen() {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var interval by remember { mutableStateOf("30") }
    var items by remember { mutableStateOf(loadArray(context, "refill_items")) }
    fun save(next: JSONArray) { items = next; saveArray(context, "refill_items", next) }
    AppShell("Refill", "Предсказывай, когда снова понадобится расходник") {
        FormField(name, { name = it }, "Расходник")
        FormField(interval, { interval = it.filter(Char::isDigit) }, "Обычно хватает на дней")
        PrimaryAction("Добавить", name.isNotBlank() && (interval.toIntOrNull() ?: 0) > 0) {
            val next = JSONArray(items.toString())
            next.put(jsonObject("id" to newId(), "name" to name.trim(), "interval" to interval.toInt(), "last" to today().toString()))
            save(next); name = ""; interval = "30"
        }
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val last = parseDateOrNull(item.optString("last")) ?: today()
            val days = item.optInt("interval", 30)
            val nextDate = nextRefillDate(last, days)
            ItemCard {
                Text(item.optString("name"))
                Text("Последнее пополнение: $last")
                Text("Следующее примерно: $nextDate")
                Button(onClick = { val next = JSONArray(items.toString()); next.getJSONObject(i).put("last", today().toString()); save(next) }) { Text("Купил / пополнил сегодня") }
            }
        }
    }
}
