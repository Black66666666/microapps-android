package com.microapps.whobringswhat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import com.microapps.designsystem.TaviCard
import com.microapps.designsystem.TaviGraphite
import com.microapps.designsystem.TaviPrimaryButton
import com.microapps.designsystem.TaviScreen
import com.microapps.designsystem.TaviSectionTitle
import com.microapps.designsystem.TaviStatusPill
import com.microapps.designsystem.TaviTextField
import com.microapps.designsystem.TaviTextSecondary
import com.microapps.designsystem.TaviTheme
import com.microapps.designsystem.TaviTopBar
import com.microapps.designsystem.TaviSuccess
import com.microapps.designsystem.WhoBringsWhatAccent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TaviTheme { WhoBringsWhatScreen() } }
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

    TaviScreen {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TaviTopBar(
                title = "WhoBringsWhat",
                subtitle = "Планируйте вместе без путаницы",
                accent = WhoBringsWhatAccent
            )

            TaviCard(Modifier.fillMaxWidth()) {
                TaviStatusPill("Совместный список", WhoBringsWhatAccent)
                Spacer(Modifier.height(12.dp))
                TaviTextField(
                    value = eventName,
                    onValueChange = { value -> eventName = value; persist(name = value) },
                    label = "Событие",
                    accent = WhoBringsWhatAccent,
                    modifier = Modifier.fillMaxWidth().testTag("event-name")
                )
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth()) {
                    TaviTextField(
                        value = itemText,
                        onValueChange = { itemText = it },
                        label = "Что нужно",
                        accent = WhoBringsWhatAccent,
                        modifier = Modifier.weight(1f).testTag("item-input")
                    )
                    Spacer(Modifier.width(10.dp))
                    TaviPrimaryButton(
                        text = "+",
                        modifier = Modifier.width(64.dp).testTag("add-item"),
                        onClick = {
                            val value = itemText.trim()
                            if (value.isNotEmpty()) {
                                items += BringItem(value)
                                itemText = ""
                                persist()
                            }
                        }
                    )
                }
            }

            if (items.isEmpty()) {
                TaviCard(Modifier.fillMaxWidth()) {
                    Text("Список пока пуст", color = TaviGraphite, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(5.dp))
                    Text("Добавь вещи: лёд, еду, напитки, игры…", color = TaviTextSecondary)
                }
            } else {
                TaviSectionTitle("Кто что берёт")
                items.forEachIndexed { index, item ->
                    TaviCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.name, color = TaviGraphite, style = MaterialTheme.typography.titleMedium)
                            if (item.owner.isBlank()) {
                                TaviStatusPill("Свободно", WhoBringsWhatAccent)
                            } else {
                                TaviStatusPill("Назначено", TaviSuccess)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        TaviTextField(
                            value = item.owner,
                            onValueChange = { owner ->
                                items[index] = item.copy(owner = owner)
                                persist()
                            },
                            label = "Кто берёт",
                            accent = WhoBringsWhatAccent,
                            modifier = Modifier.fillMaxWidth().testTag("owner-$index")
                        )
                    }
                }
            }

            TaviPrimaryButton(
                text = "Поделиться списком",
                leading = "dot",
                modifier = Modifier.fillMaxWidth(),
                enabled = items.isNotEmpty(),
                onClick = {
                    val body = formatShareText(BringState(eventName, items.toList()))
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, body)
                    }
                    context.startActivity(Intent.createChooser(send, "Отправить список"))
                }
            )
        }
    }
}
