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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.microapps.designsystem.AppHeader
import com.microapps.designsystem.GlassCard
import com.microapps.designsystem.GradientButton
import com.microapps.designsystem.NeonBackdrop
import com.microapps.designsystem.NeonCyan
import com.microapps.designsystem.NeonGreen
import com.microapps.designsystem.NeonPink
import com.microapps.designsystem.NeonTextField
import com.microapps.designsystem.SecondaryButton
import com.microapps.designsystem.SectionTitle
import com.microapps.designsystem.StatusPill
import com.microapps.designsystem.TextPrimary
import com.microapps.designsystem.TextSecondary
import com.microapps.designsystem.UnifiedAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { UnifiedAppTheme { MatchChoiceScreen() } }
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

    NeonBackdrop {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AppHeader(
                icon = "💞",
                title = "MatchChoice",
                subtitle = "Выберите независимо — увидите только совпадения"
            )

            if (imported == null) {
                GlassCard(Modifier.fillMaxWidth()) {
                    StatusPill("Уже есть приглашение?", NeonCyan)
                    Spacer(Modifier.height(10.dp))
                    NeonTextField(
                        value = inviteCode,
                        onValueChange = { inviteCode = it.trim() },
                        label = "Код приглашения",
                        modifier = Modifier.fillMaxWidth().testTag("invite-code")
                    )
                    Spacer(Modifier.height(10.dp))
                    GradientButton(
                        text = "Открыть приглашение",
                        leading = "✨",
                        enabled = InviteCodec.decode(inviteCode) != null,
                        modifier = Modifier.fillMaxWidth().testTag("open-invite"),
                        onClick = { imported = InviteCodec.decode(inviteCode) }
                    )
                }

                GlassCard(Modifier.fillMaxWidth()) {
                    SectionTitle("Создать свой выбор")
                    Spacer(Modifier.height(10.dp))
                    NeonTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Вопрос",
                        modifier = Modifier.fillMaxWidth().testTag("title")
                    )
                    Spacer(Modifier.height(10.dp))
                    NeonTextField(
                        value = rawOptions,
                        onValueChange = { rawOptions = it },
                        label = "Варианты, по одному в строке",
                        modifier = Modifier.fillMaxWidth().testTag("options"),
                        singleLine = false
                    )
                }

                GlassCard(Modifier.fillMaxWidth()) {
                    SectionTitle("Мой выбор")
                    Spacer(Modifier.height(8.dp))
                    options.forEach { option ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Checkbox(
                                modifier = Modifier.testTag("creator-$option"),
                                checked = option in creatorSelected,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = NeonCyan,
                                    uncheckedColor = TextSecondary,
                                    checkmarkColor = com.microapps.designsystem.Night
                                ),
                                onCheckedChange = { checked ->
                                    creatorSelected = if (checked) creatorSelected + option else creatorSelected - option
                                }
                            )
                            Text(
                                option,
                                modifier = Modifier.padding(top = 12.dp),
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    GradientButton(
                        text = "Отправить другу",
                        leading = "↗",
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
                GlassCard(Modifier.fillMaxWidth()) {
                    StatusPill("Выбор друга загружен", NeonGreen)
                    Spacer(Modifier.height(12.dp))
                    Text(invite.title, color = TextPrimary, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(4.dp))
                    Text("Отметь всё, что подходит тебе. Чужой выбор скрыт.", color = TextSecondary)
                }

                GlassCard(Modifier.fillMaxWidth()) {
                    invite.options.forEach { option ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Checkbox(
                                modifier = Modifier.testTag("guest-$option"),
                                checked = option in guestSelected,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = NeonPink,
                                    uncheckedColor = TextSecondary
                                ),
                                onCheckedChange = { checked ->
                                    guestSelected = if (checked) guestSelected + option else guestSelected - option
                                }
                            )
                            Text(option, modifier = Modifier.padding(top = 12.dp), color = TextPrimary)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    GradientButton(
                        text = "Показать совпадения",
                        leading = "✓",
                        modifier = Modifier.fillMaxWidth().testTag("show-matches"),
                        onClick = { result = matchingChoices(invite.creator, guestSelected) }
                    )
                }

                if (result.isNotEmpty()) {
                    GlassCard(Modifier.fillMaxWidth()) {
                        StatusPill("Есть совпадение", NeonGreen)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Совпало: ${result.joinToString()}",
                            color = TextPrimary,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                } else if (guestSelected.isNotEmpty()) {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Text("Пока совпадений нет", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                    }
                }

                SecondaryButton(
                    text = "Создать свой выбор",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { imported = null; guestSelected = emptySet(); result = emptyList() }
                )
            }
        }
    }
}
