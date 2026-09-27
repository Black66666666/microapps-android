package com.microapps.scrollreceipt

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microapps.designsystem.*
import com.microapps.scrollreceipt.core.DetectorState
import com.microapps.scrollreceipt.core.TargetPlatform
import kotlinx.coroutines.delay
import java.io.OutputStreamWriter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { UnifiedAppTheme { Screen() } } }
}

data class UiState(val enabled: Boolean, val tik: ProbeStore.PlatformStats, val reels: ProbeStore.PlatformStats, val shorts: ProbeStore.PlatformStats, val test: TargetPlatform?) {
    val count get() = tik.count + reels.count + shorts.count
    val seconds get() = tik.activeSeconds + reels.activeSeconds + shorts.activeSeconds
}

@Composable private fun Screen() {
    val context = LocalContext.current
    val store = remember { ProbeStore(context) }
    var ui by remember { mutableStateOf(snapshot(context, store)) }
    var diagnostics by remember { mutableStateOf(false) }
    var actual by remember { mutableStateOf("") }
    var confirmReset by remember { mutableStateOf(false) }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) runCatching { context.contentResolver.openOutputStream(uri)?.use { out -> OutputStreamWriter(out).use { it.write(store.report().toString(2)) } } }.onFailure { Toast.makeText(context, R.string.export_failed, Toast.LENGTH_LONG).show() }
    }
    LaunchedEffect(Unit) { while (true) { ui = snapshot(context, store); delay(1000) } }
    NeonBackdrop {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            AppHeader(stringResource(R.string.logo_symbol), stringResource(R.string.brand_name), stringResource(R.string.brand_subtitle))
            GlassCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) { SectionTitle(stringResource(R.string.automatic_measurement)); Text(stringResource(if (ui.enabled) R.string.service_enabled_detail else R.string.service_disabled_detail), color = TextSecondary, style = MaterialTheme.typography.bodySmall) }
                    StatusPill(stringResource(if (ui.enabled) R.string.counting_on_short else R.string.counting_off_short), if (ui.enabled) NeonGreen else NeonOrange)
                }
                Spacer(Modifier.height(12.dp))
                if (ui.enabled) SecondaryButton(stringResource(R.string.open_accessibility), { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }, Modifier.fillMaxWidth())
                else GradientButton(stringResource(R.string.enable_counting), { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }, Modifier.fillMaxWidth())
            }
            GlassCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.today), color = NeonCyan, fontWeight = FontWeight.Bold)
                Text(ui.count.toString(), color = TextPrimary, fontSize = 54.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.videos), color = TextSecondary)
                Spacer(Modifier.height(10.dp))
                Text("${duration(ui.seconds)}  ${stringResource(R.string.scrolling)}", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))
                StatusPill(liveLabel(ui), if (ui.enabled) NeonGreen else NeonOrange)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard(ui.tik.count.toString(), stringResource(R.string.tiktok), Modifier.weight(1f), NeonCyan)
                MetricCard(ui.reels.count.toString(), stringResource(R.string.reels), Modifier.weight(1f), NeonPink)
                MetricCard(ui.shorts.count.toString(), stringResource(R.string.shorts), Modifier.weight(1f), NeonPurple)
            }
            GlassCard(Modifier.fillMaxWidth()) { SectionTitle(stringResource(R.string.how_it_works)); Text(stringResource(R.string.always_on_explainer), color = TextSecondary, style = MaterialTheme.typography.bodyMedium) }
            SecondaryButton(if (diagnostics) stringResource(R.string.hide_diagnostics) else stringResource(R.string.show_diagnostics), { diagnostics = !diagnostics }, Modifier.fillMaxWidth())
            if (diagnostics) GlassCard(Modifier.fillMaxWidth()) {
                SectionTitle(stringResource(R.string.diagnostics_title)); Text(stringResource(R.string.diagnostics_explainer), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(10.dp)); Text(ui.test?.let { stringResource(R.string.active_test, platformName(it)) } ?: stringResource(R.string.no_active_test), color = NeonCyan)
                Spacer(Modifier.height(8.dp)); SecondaryButton(stringResource(R.string.start_tiktok), { startTest(context, store, TargetPlatform.TIKTOK) }, Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp)); SecondaryButton(stringResource(R.string.start_instagram), { startTest(context, store, TargetPlatform.INSTAGRAM) }, Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp)); SecondaryButton(stringResource(R.string.start_youtube), { startTest(context, store, TargetPlatform.YOUTUBE) }, Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp)); NeonTextField(actual, { actual = it.filter(Char::isDigit).take(5) }, stringResource(R.string.actual_count_short), Modifier.fillMaxWidth(), ui.test != null)
                Text(stringResource(R.string.actual_count_hint), color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(8.dp)); GradientButton(stringResource(R.string.stop_test), { val n = actual.toIntOrNull(); if (n == null || n <= 0) Toast.makeText(context, R.string.manual_count_invalid, Toast.LENGTH_LONG).show() else { store.stopTest(n); actual = ""; ui = snapshot(context, store) } }, Modifier.fillMaxWidth(), ui.test != null)
                store.lastTestResult()?.let { r -> Spacer(Modifier.height(8.dp)); StatusPill(if (r.optBoolean("pass_under_5_percent")) stringResource(R.string.test_pass, r.optDouble("error_percent")) else stringResource(R.string.test_fail, r.optDouble("error_percent")), if (r.optBoolean("pass_under_5_percent")) NeonGreen else NeonOrange) }
                Spacer(Modifier.height(8.dp)); Text(stringResource(R.string.last_event, store.lastEvent()), color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(8.dp)); SecondaryButton(stringResource(R.string.export_report), { export.launch("scroll-receipt-gate0a-report.json") }, Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp)); SecondaryButton(stringResource(R.string.reset_counters), { confirmReset = true }, Modifier.fillMaxWidth())
            }
            GlassCard(Modifier.fillMaxWidth()) { SectionTitle(stringResource(R.string.privacy_title)); Text(stringResource(R.string.privacy_note), color = TextSecondary, style = MaterialTheme.typography.bodySmall) }
        }
    }
    if (confirmReset) AlertDialog(onDismissRequest = { confirmReset = false }, title = { Text(stringResource(R.string.confirm_reset_title)) }, text = { Text(stringResource(R.string.confirm_reset_body)) }, confirmButton = { TextButton(onClick = { store.resetAll(); ui = snapshot(context, store); confirmReset = false }) { Text(stringResource(R.string.reset)) } }, dismissButton = { TextButton(onClick = { confirmReset = false }) { Text(stringResource(R.string.cancel)) } })
}

private fun snapshot(context: Context, store: ProbeStore) = UiState(accessEnabled(context), store.stats(TargetPlatform.TIKTOK), store.stats(TargetPlatform.INSTAGRAM), store.stats(TargetPlatform.YOUTUBE), store.activeTest())
private fun accessEnabled(context: Context): Boolean { val m = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager; return m.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK).any { it.resolveInfo.serviceInfo.packageName == context.packageName && it.resolveInfo.serviceInfo.name.endsWith(".ScrollAccessibilityService") } }
private fun startTest(context: Context, store: ProbeStore, platform: TargetPlatform) { val intent = context.packageManager.getLaunchIntentForPackage(platform.packageName); if (intent == null) { Toast.makeText(context, R.string.launch_failed, Toast.LENGTH_LONG).show(); return }; store.startTest(platform); runCatching { context.startActivity(intent) }.onFailure { store.cancelTest(); Toast.makeText(context, R.string.launch_failed, Toast.LENGTH_LONG).show() } }
@Composable private fun liveLabel(ui: UiState): String { if (!ui.enabled) return stringResource(R.string.live_disabled); val p = when { ui.tik.state == DetectorState.SHORT_MODE_ACTIVE.name -> TargetPlatform.TIKTOK; ui.reels.state == DetectorState.SHORT_MODE_ACTIVE.name -> TargetPlatform.INSTAGRAM; ui.shorts.state == DetectorState.SHORT_MODE_ACTIVE.name -> TargetPlatform.YOUTUBE; else -> null }; return p?.let { stringResource(R.string.live_counting, platformName(it)) } ?: stringResource(R.string.live_waiting) }
@Composable private fun platformName(p: TargetPlatform) = when (p) { TargetPlatform.TIKTOK -> stringResource(R.string.tiktok); TargetPlatform.INSTAGRAM -> stringResource(R.string.reels_full); TargetPlatform.YOUTUBE -> stringResource(R.string.shorts_full) }
@Composable private fun duration(s: Long): String { val h=s/3600; val m=(s%3600)/60; val sec=s%60; return when { h>0 -> stringResource(R.string.duration_hours_minutes,h,m); m>0 -> stringResource(R.string.duration_minutes_seconds,m,sec); else -> stringResource(R.string.duration_seconds,sec) } }
