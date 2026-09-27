package com.microapps.scrollreceipt

import android.content.Context
import android.os.Build
import com.microapps.scrollreceipt.core.DetectorDecision
import com.microapps.scrollreceipt.core.DetectorState
import com.microapps.scrollreceipt.core.ProbeEventKind
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

    fun recordSnapshot(snapshot: AccessibilitySnapshotFactory.Snapshot, decision: DetectorDecision) {
        ensureDay()
        val trace = runCatching { JSONArray(prefs.getString("diagnostic_trace", "[]")) }.getOrElse { JSONArray() }
        val event = snapshot.event
        val activeTest = activeTest()
        val candidate = event.kind == ProbeEventKind.VIEW_SELECTED || event.kind == ProbeEventKind.SCROLLED
        if (activeTest == decision.platform && candidate) {
            val last = prefs.getLong("candidate_last_ms", Long.MIN_VALUE)
            if (last == Long.MIN_VALUE || event.timestampMs - last >= 320L) {
                prefs.edit()
                    .putLong("candidate_last_ms", event.timestampMs)
                    .putInt("candidate_transition_count", prefs.getInt("candidate_transition_count", 0) + 1)
                    .apply()
            }
        }
        val row = JSONObject()
            .put("t", event.timestampMs)
            .put("platform", decision.platform.name)
            .put("kind", event.kind.name)
            .put("nodes", snapshot.nodes)
            .put("view_ids", JSONArray(snapshot.viewIds.take(24)))
            .put("classes", JSONArray(snapshot.classes.take(24)))
            .put("from", event.fromIndex)
            .put("to", event.toIndex)
            .put("items", event.itemCount)
            .put("delta_y", event.scrollDeltaY)
            .put("scroll_y", event.scrollY)
            .put("max_scroll_y", event.maxScrollY)
            .put("dominant_scrollable", event.dominantFullScreenScrollable)
            .put("state", decision.state.name)
            .put("confidence", decision.confidence)
            .put("increment", decision.increment)
            .put("reason", decision.reason)
        trace.put(row)
        while (trace.length() > 160) trace.remove(0)
        prefs.edit().putString("diagnostic_trace", trace.toString()).apply()
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
        ensureDay()
        prefs.edit()
            .putString("test_id", UUID.randomUUID().toString())
            .putString("test_platform", platform.name)
            .putLong("test_started", System.currentTimeMillis())
            .putInt("test_start_count", stats(platform).count)
            .putInt("candidate_transition_count", 0)
            .remove("candidate_last_ms")
            .putString("diagnostic_trace", "[]")
            .remove("test_result")
            .apply()
    }
    fun cancelTest() = prefs.edit().remove("test_platform").remove("test_id").remove("test_started").remove("test_start_count").apply()
    fun activeTest(): TargetPlatform? = prefs.getString("test_platform", null)?.let { runCatching { TargetPlatform.valueOf(it) }.getOrNull() }
    fun stopTest(actual: Int): JSONObject? {
        val platform = activeTest() ?: return null
        val detected = stats(platform).count - prefs.getInt("test_start_count", stats(platform).count)
        val candidates = prefs.getInt("candidate_transition_count", 0)
        val error = kotlin.math.abs(detected - actual).toDouble() / actual.toDouble() * 100.0
        val candidateError = kotlin.math.abs(candidates - actual).toDouble() / actual.toDouble() * 100.0
        val result = JSONObject()
            .put("test_id", prefs.getString("test_id", "unknown"))
            .put("platform", platform.name)
            .put("detected_videos", detected)
            .put("candidate_transition_events", candidates)
            .put("manual_actual_videos", actual)
            .put("error_percent", error)
            .put("candidate_error_percent", candidateError)
            .put("pass_under_5_percent", error <= 5.0)
        prefs.edit().putString("test_result", result.toString()).remove("test_platform").apply(); return result
    }
    fun lastTestResult(): JSONObject? = prefs.getString("test_result", null)?.let { runCatching { JSONObject(it) }.getOrNull() }
    fun resetAll() { val next = generation() + 1; prefs.edit().clear().putInt("generation", next).apply() }
    fun report(): JSONObject {
        ensureDay(); val platforms = JSONArray()
        TargetPlatform.entries.forEach { p -> val s = stats(p); platforms.put(JSONObject().put("platform", p.name).put("package", p.packageName).put("installed_version", installedVersion(p.packageName)).put("detected_videos", s.count).put("active_seconds", s.activeSeconds).put("state", s.state).put("confidence", s.confidence).put("reason", s.reason)) }
        val trace = runCatching { JSONArray(prefs.getString("diagnostic_trace", "[]")) }.getOrElse { JSONArray() }
        return JSONObject()
            .put("schema_version", 3)
            .put("local_date", LocalDate.now().toString())
            .put("generated_at", Instant.now().toString())
            .put("app_version", BuildConfig.VERSION_NAME)
            .put("automatic_counting", true)
            .put("network_permission_declared", false)
            .put("raw_text_collected", false)
            .put("screenshots_collected", false)
            .put("device", JSONObject().put("manufacturer", Build.MANUFACTURER).put("model", Build.MODEL).put("android_release", Build.VERSION.RELEASE).put("sdk_int", Build.VERSION.SDK_INT))
            .put("platforms", platforms)
            .put("last_event", lastEvent())
            .put("last_controlled_test", lastTestResult())
            .put("diagnostic_trace", trace)
    }
    private fun ensureDay() {
        val today = LocalDate.now().toString(); val old = prefs.getString("stats_date", null)
        if (old == today) return
        if (old == null) { prefs.edit().putString("stats_date", today).apply(); return }
        val e = prefs.edit().putString("stats_date", today).putInt("generation", generation() + 1).remove("test_platform").remove("test_result").putString("diagnostic_trace", "[]")
        TargetPlatform.entries.forEach { p -> val k = p.name.lowercase(); e.putInt("${k}_count", 0).putLong("${k}_active_ms", 0L).putString("${k}_state", DetectorState.OUTSIDE_TARGET_APP.name).putFloat("${k}_confidence", 0f) }
        e.apply()
    }
    @Suppress("DEPRECATION") private fun installedVersion(packageName: String): String? = runCatching { app.packageManager.getPackageInfo(packageName, 0).versionName }.getOrNull()
}
