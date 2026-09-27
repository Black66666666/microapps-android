package com.microapps.kit

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

internal const val STORE = "microapp_data"
internal val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

internal fun prefs(context: Context) = context.getSharedPreferences(STORE, Context.MODE_PRIVATE)
internal fun loadArray(context: Context, key: String): JSONArray = runCatching {
    JSONArray(prefs(context).getString(key, "[]") ?: "[]")
}.getOrElse { JSONArray() }
internal fun saveArray(context: Context, key: String, array: JSONArray) {
    prefs(context).edit().putString(key, array.toString()).apply()
}
internal fun jsonObject(vararg values: Pair<String, Any?>): JSONObject = JSONObject().apply {
    values.forEach { (key, value) -> put(key, value ?: JSONObject.NULL) }
}
internal fun newId(): String = UUID.randomUUID().toString()
internal fun today(): LocalDate = LocalDate.now()
internal fun dateFromMillis(value: Long): LocalDate = Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate()
internal fun millisAtStartOfDay(date: LocalDate): Long = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
internal fun parseDateOrNull(value: String): LocalDate? = runCatching { LocalDate.parse(value.trim(), dateFormatter) }.getOrNull()

internal fun shareText(context: Context, text: String, title: String = "Поделиться") {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, title))
}

@Composable
fun NeutralAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}

@Composable
fun AppShell(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(PaddingValues(20.dp)),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        content()
    }
}

@Composable
internal fun FormField(value: String, onValueChange: (String) -> Unit, label: String, singleLine: Boolean = true) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, singleLine = singleLine, modifier = Modifier.fillMaxWidth())
}

@Composable
internal fun ItemCard(content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { content() } }
}

@Composable
internal fun PrimaryAction(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) { Text(text) }
}
