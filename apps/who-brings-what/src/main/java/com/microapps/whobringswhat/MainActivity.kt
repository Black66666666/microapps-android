package com.microapps.whobringswhat

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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { WhoBringsWhatScreen() } }
    }
}

@Composable
private fun WhoBringsWhatScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("data", Context.MODE_PRIVATE) }
    val initial = remember { BringStateCodec.decode(prefs.getString("state", "").orEmpty()) ?: BringState("Пикник", emptyList()) }
    var eventName by remember { mutableStateOf(initial.eventName) }
    var itemText by remember { mutableStateOf("") }
    val items = remember { mutableStateListOf<BringItem>().apply { addAll(initial.items) } }

    fun persist(name: String = eventName, currentItems: List<BringItem> = items.toList()) {
        prefs.edit().putString("state", BringStateCodec.encode(BringState(name, currentItems))).apply()
    }

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("WhoBringsWhat", style = MaterialTheme.typography.headlineMedium)
        Text("Создай общий список и распредели, кто что приносит.")
        OutlinedTextField(eventName, { value -> eventName = value; persist(name = value) }, label = { Text("Событие") }, modifier = Modifier.fillMaxWidth().testTag("event-name"))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(itemText, { itemText = it }, label = { Text("Что нужно") }, modifier = Modifier.weight(1f).testTag("item-input"))
            Button(modifier = Modifier.testTag("add-item"), onClick = {
                val value = itemText.trim()
                if (value.isNotEmpty()) { items += BringItem(value); itemText = ""; persist() }
            }) { Text("+") }
        }
        if (items.isEmpty()) Text("Добавь вещи: лёд, уголь, напитки…")
        items.forEachIndexed { index, item ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = item.owner,
                        onValueChange = { owner -> items[index] = item.copy(owner = owner); persist() },
                        label = { Text("Кто берёт") },
                        modifier = Modifier.fillMaxWidth().testTag("owner-$index")
                    )
                }
            }
        }
        Button(enabled = items.isNotEmpty(), onClick = {
            val body = formatShareText(BringState(eventName, items.toList()))
            val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, body) }
            context.startActivity(Intent.createChooser(send, "Отправить список"))
        }) { Text("Поделиться списком") }
    }
}
