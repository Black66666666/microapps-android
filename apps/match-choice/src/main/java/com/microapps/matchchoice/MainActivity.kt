package com.microapps.matchchoice

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.microapps.designsystem.MatchChoiceAccent
import com.microapps.designsystem.TaviCard
import com.microapps.designsystem.TaviGraphite
import com.microapps.designsystem.TaviPrimaryButton
import com.microapps.designsystem.TaviScreen
import com.microapps.designsystem.TaviSecondaryButton
import com.microapps.designsystem.TaviSectionTitle
import com.microapps.designsystem.TaviStatusPill
import com.microapps.designsystem.TaviTextField
import com.microapps.designsystem.TaviTextSecondary
import com.microapps.designsystem.TaviTheme
import com.microapps.designsystem.TaviTopBar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TaviTheme { MatchChoiceScreen() } }
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

    TaviScreen {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TaviTopBar(
                title = "MatchChoice",
                subtitle = "Выберите независимо — увидите только совпадения",
                accent = MatchChoiceAccent
            )

            if (imported == null) {
                TaviCard(Modifier.fillMaxWidth()) {
                    TaviStatusPill("Уже есть приглашение?", MatchChoiceAccent)
                    Spacer(Modifier.height(10.dp))
                    TaviTextField(
                        value = inviteCode,
                        onValueChange = { inviteCode = it.trim() },
                        label = "Код приглашения",
                        accent = MatchChoiceAccent,
                        modifier = Modifier.fillMaxWidth().testTag("invite-code")
                    )
                    Spacer(Modifier.height(10.dp))
                    TaviPrimaryButton(
                        text = "Открыть приглашение",
                        leading = "dot",
                        enabled = InviteCodec.decode(inviteCode) != null,
                        modifier = Modifier.fillMaxWidth().testTag("open-invite"),
                        onClick = { imported = InviteCodec.decode(inviteCode) }
                    )
                }

                TaviCard(Modifier.fillMaxWidth()) {
                    TaviSectionTitle("Создать свой выбор")
                    Spacer(Modifier.height(10.dp))
                    TaviTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Вопрос",
                        accent = MatchChoiceAccent,
                        modifier = Modifier.fillMaxWidth().testTag("title")
                    )
                    Spacer(Modifier.height(10.dp))
                    TaviTextField(
                        value = rawOptions,
                        onValueChange = { rawOptions = it },
                        label = "Варианты, по одному в строке",
                        accent = MatchChoiceAccent,
                        modifier = Modifier.fillMaxWidth().testTag("options"),
                        singleLine = false
                    )
                }

                TaviCard(Modifier.fillMaxWidth()) {
                    TaviSectionTitle("Мой выбор")
                    Spacer(Modifier.height(8.dp))
                    options.forEach { option ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Checkbox(
                                modifier = Modifier.testTag("creator-$option"),
                                checked = option in creatorSelected,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MatchChoiceAccent,
                                    uncheckedColor = TaviTextSecondary,
                                    checkmarkColor = Color.White
                                ),
                                onCheckedChange = { checked ->
                                    creatorSelected = if (checked) creatorSelected + option else creatorSelected - option
                                }
                            )
                            Text(
                                option,
                                modifier = Modifier.padding(top = 12.dp),
                                color = TaviGraphite,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    TaviPrimaryButton(
                        text = "Отправить другу",
                        leading = "dot",
                        modifier = Modifier.fillMaxWidth().testTag("share-invite"),
                        enabled = options.size >= 2 && creatorSelected.isNotEmpty(),
                        onClick = {
                            inviteCode = InviteCodec.encode(Invite(title.trim(), options, creatorSelected.toList()))
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "MatchChoice: $title\nКод приглашения:\n$inviteCode")
                            }
                            context.startActivity(Intent.createChooser(send, "Отправить выбор"))
                        }
                    )
                }
            } else {
                val invite = imported!!
                TaviCard(Modifier.fillMaxWidth()) {
                    TaviStatusPill("Выбор друга загружен", MatchChoiceAccent)
                    Spacer(Modifier.height(12.dp))
                    Text(invite.title, color = TaviGraphite, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(4.dp))
                    Text("Отметь всё, что подходит тебе. Чужой выбор скрыт.", color = TaviTextSecondary)
                }

                TaviCard(Modifier.fillMaxWidth()) {
                    invite.options.forEach { option ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Checkbox(
                                modifier = Modifier.testTag("guest-$option"),
                                checked = option in guestSelected,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MatchChoiceAccent,
                                    uncheckedColor = TaviTextSecondary,
                                    checkmarkColor = Color.White
                                ),
                                onCheckedChange = { checked ->
                                    guestSelected = if (checked) guestSelected + option else guestSelected - option
                                }
                            )
                            Text(option, modifier = Modifier.padding(top = 12.dp), color = TaviGraphite)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    TaviPrimaryButton(
                        text = "Показать совпадения",
                        leading = "dot",
                        modifier = Modifier.fillMaxWidth().testTag("show-matches"),
                        onClick = { result = matchingChoices(invite.creator, guestSelected) }
                    )
                }

                if (result.isNotEmpty()) {
                    TaviCard(Modifier.fillMaxWidth()) {
                        TaviStatusPill("Есть совпадение", MatchChoiceAccent)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Совпало: ${result.joinToString()}",
                            color = TaviGraphite,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                } else if (guestSelected.isNotEmpty()) {
                    TaviCard(Modifier.fillMaxWidth()) {
                        Text("Пока совпадений нет", color = TaviGraphite, style = MaterialTheme.typography.titleMedium)
                    }
                }

                TaviSecondaryButton(
                    text = "Создать свой выбор",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { imported = null; guestSelected = emptySet(); result = emptyList() }
                )
            }
        }
    }
}
