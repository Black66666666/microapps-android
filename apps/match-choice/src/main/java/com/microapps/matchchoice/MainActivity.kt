package com.microapps.matchchoice

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
        setContent { MaterialTheme { MatchChoiceScreen() } }
    }
}

@Composable
private fun MatchChoiceScreen() {
    val context = LocalContext.current
    var title by remember { mutableStateOf("Куда идём?") }
    var rawOptions by remember { mutableStateOf("Пицца\nРамен\nБургеры\nСуши") }
    var inviteCode by remember { mutableStateOf("") }
    var imported by remember { mutableStateOf<Invite?>(null) }
    var creatorSelected by remember { mutableStateOf(setOf<String>()) }
    var guestSelected by remember { mutableStateOf(setOf<String>()) }
    var result by remember { mutableStateOf<List<String>>(emptyList()) }

    val options = rawOptions.lines().map { it.trim() }.filter { it.isNotEmpty() }.distinct()

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("MatchChoice", style = MaterialTheme.typography.headlineMedium)
        Text("Два человека выбирают независимо. Показываем только совпадения.")

        if (imported == null) {
            OutlinedTextField(title, { title = it }, label = { Text("Вопрос") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(rawOptions, { rawOptions = it }, label = { Text("Варианты, по одному в строке") }, modifier = Modifier.fillMaxWidth())
            Text("Мой выбор")
            options.forEach { option ->
                Row {
                    Checkbox(checked = option in creatorSelected, onCheckedChange = { checked ->
                        creatorSelected = if (checked) creatorSelected + option else creatorSelected - option
                    })
                    Text(option, modifier = Modifier.padding(top = 12.dp))
                }
            }
            Button(enabled = options.size >= 2 && creatorSelected.isNotEmpty(), onClick = {
                inviteCode = encodeInvite(Invite(title, options, creatorSelected.toList()))
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "MatchChoice: $title\nКод приглашения:\n$inviteCode")
                }
                context.startActivity(Intent.createChooser(send, "Отправить выбор"))
            }) { Text("Отправить другу") }

            OutlinedTextField(inviteCode, { inviteCode = it.trim() }, label = { Text("Или вставь код приглашения") }, modifier = Modifier.fillMaxWidth())
            Button(enabled = inviteCode.isNotBlank(), onClick = { imported = decodeInvite(inviteCode) }) { Text("Открыть приглашение") }
        } else {
            val invite = imported!!
            Text(invite.title, style = MaterialTheme.typography.titleLarge)
            Text("Отметь всё, что подходит тебе")
            invite.options.forEach { option ->
                Row {
                    Checkbox(checked = option in guestSelected, onCheckedChange = { checked ->
                        guestSelected = if (checked) guestSelected + option else guestSelected - option
                    })
                    Text(option, modifier = Modifier.padding(top = 12.dp))
                }
            }
            Button(onClick = { result = invite.creator.filter { it in guestSelected } }) { Text("Показать совпадения") }
            if (result.isNotEmpty()) Text("Совпало: ${result.joinToString()}", style = MaterialTheme.typography.titleMedium)
            else if (guestSelected.isNotEmpty()) Text("Пока совпадений нет")
            Button(onClick = { imported = null; guestSelected = emptySet(); result = emptyList() }) { Text("Создать свой выбор") }
        }
    }
}

data class Invite(val title: String, val options: List<String>, val creator: List<String>)

private fun encodeInvite(invite: Invite): String {
    val json = JSONObject().apply {
        put("title", invite.title)
        put("options", JSONArray(invite.options))
        put("creator", JSONArray(invite.creator))
    }
    return Base64.encodeToString(json.toString().toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
}

private fun decodeInvite(code: String): Invite? = runCatching {
    val json = JSONObject(String(Base64.decode(code.trim(), Base64.URL_SAFE or Base64.NO_WRAP)))
    fun JSONArray.toStrings() = (0 until length()).map { getString(it) }
    Invite(json.getString("title"), json.getJSONArray("options").toStrings(), json.getJSONArray("creator").toStrings())
}.getOrNull()
