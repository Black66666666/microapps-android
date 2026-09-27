package com.microapps.kit

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONArray

fun boxIdFromLink(link: String?): String? {
    if (link == null || !link.startsWith("boxqr://box/")) return null
    return link.removePrefix("boxqr://box/").substringBefore('?').substringBefore('#').takeIf { it.isNotBlank() && !it.contains('/') }
}

fun boxIdFromUri(uri: Uri?): String? = boxIdFromLink(uri?.toString())

@Composable
fun BoxQrDeepLinkScreen(initialBoxId: String? = null) {
    val context = LocalContext.current
    var boxName by remember { mutableStateOf("") }
    var contents by remember { mutableStateOf("") }
    var boxes by remember { mutableStateOf(loadArray(context, "boxes")) }
    var selectedId by remember(initialBoxId) { mutableStateOf(initialBoxId) }
    fun save(next: JSONArray) { boxes = next; saveArray(context, "boxes", next) }
    fun selectedIndex(): Int? = (0 until boxes.length()).firstOrNull { boxes.getJSONObject(it).optString("id") == selectedId }

    AppShell("BoxQR", "Подпиши коробку QR-кодом и быстро находи содержимое") {
        val selected = selectedIndex()?.let { boxes.getJSONObject(it) }
        if (selectedId != null) {
            ItemCard {
                if (selected == null) {
                    Text("Коробка не найдена на этом устройстве")
                    Text("QR относится к другой или удалённой записи.")
                } else {
                    Text(selected.optString("name"))
                    Text(selected.optString("contents").ifBlank { "Содержимое пока не указано" })
                    val uri = "boxqr://box/${selected.optString("id")}"
                    Image(
                        bitmap = remember(uri) { createQrBitmap(uri).asImageBitmap() },
                        contentDescription = "QR коробки",
                        modifier = Modifier.fillMaxWidth().height(260.dp)
                    )
                }
                Button(onClick = { selectedId = null }) { Text("К списку") }
            }
        } else {
            FormField(boxName, { boxName = it }, "Название коробки")
            FormField(contents, { contents = it }, "Что внутри, через запятую", singleLine = false)
            PrimaryAction("Создать коробку", boxName.isNotBlank()) {
                val id = newId()
                val next = JSONArray(boxes.toString())
                next.put(jsonObject("id" to id, "name" to boxName.trim(), "contents" to contents.trim()))
                save(next)
                selectedId = id
                boxName = ""
                contents = ""
            }
            for (i in 0 until boxes.length()) {
                val box = boxes.getJSONObject(i)
                ItemCard {
                    Text(box.optString("name"))
                    Text(box.optString("contents").ifBlank { "Содержимое пока не указано" })
                    Button(onClick = { selectedId = box.optString("id") }) { Text("Открыть QR") }
                }
            }
        }
    }
}
