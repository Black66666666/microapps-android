package com.microapps.kit

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.microapps.designsystem.TaviCard
import com.microapps.designsystem.TaviPrimaryButton
import com.microapps.designsystem.TaviScreen
import com.microapps.designsystem.TaviSecondaryButton
import com.microapps.designsystem.TaviTextField
import com.microapps.designsystem.TaviTheme
import com.microapps.designsystem.TaviTopBar
import com.microapps.designsystem.TaviWarmOrange
import com.microapps.designsystem.taviAccentFor
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

internal const val STORE = "microapp_data"
internal val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val LocalAppAccent = staticCompositionLocalOf<Color> { TaviWarmOrange }

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
    TaviTheme(content = content)
}

@Composable
fun AppShell(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    val accent = taviAccentFor(title)
    CompositionLocalProvider(LocalAppAccent provides accent) {
        TaviScreen {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TaviTopBar(title = title, subtitle = subtitle, accent = accent)
                Spacer(Modifier.height(8.dp))
                content()
            }
        }
    }
}

@Composable
internal fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true
) {
    TaviTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        singleLine = singleLine,
        accent = LocalAppAccent.current,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
internal fun ItemCard(content: @Composable () -> Unit) {
    TaviCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}

@Composable
internal fun PrimaryAction(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    TaviPrimaryButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        leading = "dot",
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
internal fun SecondaryAction(
    text: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onClick: () -> Unit
) {
    TaviSecondaryButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    )
}
