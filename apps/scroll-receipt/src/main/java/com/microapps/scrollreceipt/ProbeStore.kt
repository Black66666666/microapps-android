package com.microapps.scrollreceipt

import android.content.Context
import android.os.Build
import com.microapps.scrollreceipt.core.DetectorDecision
import com.microapps.scrollreceipt.core.DetectorState
import com.microapps.scrollreceipt.core.TargetPlatform
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class ProbeStore(context: Context) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("scroll_receipt_gate0a", Context.MODE_PRIVATE)
    data class PlatformStats(val count: Int, val activeSeconds: Long, val state: String, val confidence: Double, val reason: String)

    fun update(decision: DetectorDecision) {
        ensureDay()
        val p = decision.platform.name.lowercase()
        val e = prefs.edit().putString("${p}_state", decision.state.name).putString("${p}_reason", decision.reason).putFloat("${p}_confidence", decision.confidence.toFloat())
        if (decision.increment > 0) e.putInt("${p}_count", prefs.getInt("${p}_count", 0) + decision.increment)
        e.apply()
    }
    fun addActiveMillis(platform: TargetPlatform, millis: Long) {
        ensureDay(); if (millis <= 0L) return
        val key = "${platform.name.lowercase()}_active_ms"
        prefs.edit().putLong(key, prefs.getLong(key, 0L) + millis).apply()
    }
    fun setLastEvent(value: String) = prefs.edit().putString("last_event", value.take(300)).apply()
    fun lastEvent(): String = prefs.getString("last_event", "none") ?: "none"
    fun generation(): Int = prefs.getInt("generation", 0)
    fun stats(platform: TargetPlatform): PlatformStats {
        ensureDay(); val p = platform.name.lowercase()
        return PlatformStats(prefs.getInt("${p}_count", 0), prefs.getLong("${p}_active_ms", 0L) / 1000L, prefs.getString("${p}_state", DetectorState.OUTSIDE_TARGET_APP.name) ?: DetectorState.OUTSIDE_TARGET_APP.name, prefs.getFloat("${p}_confidence", 0f).toDouble(), prefs.getString("${p}_reason", "none") ?: "none")
    }
    fun startTest(platform: TargetPlatform) {
        ensureDay(); prefs.edit().putString("test_id", UUID.randomUUID().toString()).putString("test_platform", platform.name).putLong("test_started", System.currentTimeMillis()).putInt("test_start_count", stats(platform).count).remove("test_result").apply()
    }
    fun cancelTest() = prefs.edit().remove("test_platform").remove("test_id").remove("test_started").remove("test_start_count").apply()
    fun activeTest(): TargetPlatform? = prefs.getString("test_platform", null)?.let { runCatching { TargetPlatform.valueOf(it) }.getOrNull() }
    fun stopTest(actual: Int): JSONObject? {
        val platform = activeTest() ?: return null
        val detected = stats(platform).count - prefs.getInt("test_start_count", stats(platform).count)
        val error = kotlin.math.abs(detected - actual).toDouble() / actual.toDouble() * 100.0
        val result = JSONObject().put("test_id", prefs.getString("test_id", "unknown")).put("platform", platform.name).put("detected_videos", detected).put("manual_actual_videos", actual).put("error_percent", error).put("pass_under_5_percent", error <= 5.0)
        prefs.edit().putString("test_result", result.toString()).remove("test_platform").apply(); return result
    }
    fun lastTestResult(): JSONObject? = prefs.getString("test_result", null)?.let { runCatching { JSONObject(it) }.getOrNull() }
    fun resetAll() { val next = generation() + 1; prefs.edit().clear().putInt("generation", next).apply() }
    fun report(): JSONObject {
        ensureDay(); val platforms = JSONArray()
        TargetPlatform.entries.forEach { p -> val s = stats(p); platforms.put(JSONObject().put("platform", p.name).put("package", p.packageName).put("installed_version", installedVersion(p.packageName)).put("detected_videos", s.count).put("active_seconds", s.activeSeconds).put("state", s.state).put("confidence", s.confidence).put("reason", s.reason)) }
        return JSONObject().put("schema_version", 2).put("local_date", LocalDate.now().toString()).put("generated_at", Instant.now().toString()).put("app_version", BuildConfig.VERSION_NAME).put("automatic_counting", true).put("network_permission_declared", false).put("raw_text_collected", false).put("screenshots_collected", false).put("device", JSONObject().put("manufacturer", Build.MANUFACTURER).put("model", Build.MODEL).put("android_release", Build.VERSION.RELEASE).put("sdk_int", Build.VERSION.SDK_INT)).put("platforms", platforms).put("last_event", lastEvent()).put("last_controlled_test", lastTestResult())
    }
    private fun ensureDay() {
        val today = LocalDate.now().toString(); val old = prefs.getString("stats_date", null)
        if (old == today) return
        if (old == null) { prefs.edit().putString("stats_date", today).apply(); return }
        val e = prefs.edit().putString("stats_date", today).putInt("generation", generation() + 1).remove("test_platform").remove("test_result")
        TargetPlatform.entries.forEach { p -> val k = p.name.lowercase(); e.putInt("${k}_count", 0).putLong("${k}_active_ms", 0L).putString("${k}_state", DetectorState.OUTSIDE_TARGET_APP.name).putFloat("${k}_confidence", 0f) }
        e.apply()
    }
    @Suppress("DEPRECATION") private fun installedVersion(packageName: String): String? = runCatching { app.packageManager.getPackageInfo(packageName, 0).versionName }.getOrNull()
}
