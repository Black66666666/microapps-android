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
import com.microapps.designsystem.AppHeader
import com.microapps.designsystem.GlassCard
import com.microapps.designsystem.GradientButton
import com.microapps.designsystem.NeonBackdrop
import com.microapps.designsystem.NeonCyan
import com.microapps.designsystem.NeonGreen
import com.microapps.designsystem.NeonTextField
import com.microapps.designsystem.SectionTitle
import com.microapps.designsystem.StatusPill
import com.microapps.designsystem.TextPrimary
import com.microapps.designsystem.TextSecondary
import com.microapps.designsystem.UnifiedAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { UnifiedAppTheme { WhoBringsWhatScreen() } }
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

    NeonBackdrop {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AppHeader(
                icon = "👥",
                title = "WhoBringsWhat",
                subtitle = "Планируйте вместе без путаницы"
            )

            GlassCard(Modifier.fillMaxWidth()) {
                StatusPill("Совместный список", NeonCyan)
                Spacer(Modifier.height(12.dp))
                NeonTextField(
                    value = eventName,
                    onValueChange = { value -> eventName = value; persist(name = value) },
                    label = "Событие",
                    modifier = Modifier.fillMaxWidth().testTag("event-name")
                )
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth()) {
                    NeonTextField(
                        value = itemText,
                        onValueChange = { itemText = it },
                        label = "Что нужно",
                        modifier = Modifier.weight(1f).testTag("item-input")
                    )
                    Spacer(Modifier.width(10.dp))
                    GradientButton(
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
                GlassCard(Modifier.fillMaxWidth()) {
                    Text("Список пока пуст", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(5.dp))
                    Text("Добавь вещи: лёд, еду, напитки, игры…", color = TextSecondary)
                }
            } else {
                SectionTitle("Кто что берёт")
                items.forEachIndexed { index, item ->
                    GlassCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.name, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            if (item.owner.isBlank()) StatusPill("Свободно") else StatusPill("Назначено", NeonGreen)
                        }
                        Spacer(Modifier.height(10.dp))
                        NeonTextField(
                            value = item.owner,
                            onValueChange = { owner ->
                                items[index] = item.copy(owner = owner)
                                persist()
                            },
                            label = "Кто берёт",
                            modifier = Modifier.fillMaxWidth().testTag("owner-$index")
                        )
                    }
                }
            }

            GradientButton(
                text = "Поделиться списком",
                leading = "↗",
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
