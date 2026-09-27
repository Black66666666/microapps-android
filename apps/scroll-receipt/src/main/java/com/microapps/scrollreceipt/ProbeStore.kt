package com.microapps.scrollreceipt

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.os.Build
import android.view.accessibility.AccessibilityManager
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
    private val servicePrefs = app.getSharedPreferences("scroll_receipt_service_state", Context.MODE_PRIVATE)
    private val traceFile get() = app.filesDir.resolve("scroll_receipt_calibration_trace.jsonl")

    data class PlatformStats(val count: Int, val activeSeconds: Long, val state: String, val confidence: Double, val reason: String)

    fun update(decision: DetectorDecision) {
        ensureDay()
        val p = decision.platform.name.lowercase()
        val stateKey = "${p}_state"
        val reasonKey = "${p}_reason"
        val confidenceKey = "${p}_confidence"
        val oldState = prefs.getString(stateKey, DetectorState.OUTSIDE_TARGET_APP.name)
        val oldReason = prefs.getString(reasonKey, "none")
        val oldConfidence = prefs.getFloat(confidenceKey, 0f)
        val newConfidence = decision.confidence.toFloat()
        val stateChanged = oldState != decision.state.name || oldReason != decision.reason || kotlin.math.abs(oldConfidence - newConfidence) >= 0.05f
        if (!stateChanged && decision.increment <= 0) return

        val e = prefs.edit()
        if (stateChanged) {
            e.putString(stateKey, decision.state.name)
                .putString(reasonKey, decision.reason)
                .putFloat(confidenceKey, newConfidence)
        }
        if (decision.increment > 0) {
            e.putInt("${p}_count", prefs.getInt("${p}_count", 0) + decision.increment)
        }
        e.apply()
    }

    fun recordSnapshot(snapshot: AccessibilitySnapshotFactory.Snapshot, decision: DetectorDecision) {
        ensureDay()
        val event = snapshot.event
        val row = JSONObject()
            .put("t", event.timestampMs)
            .put("platform", decision.platform.name)
            .put("kind", event.kind.name)
            .put("raw_type", snapshot.rawEventType)
            .put("event_class", snapshot.eventClass)
            .put("source_id", snapshot.sourceId)
            .put("source_class", snapshot.sourceClass)
            .put("action", snapshot.action)
            .put("nodes", snapshot.nodes)
            .put("view_ids", JSONArray(snapshot.viewIds.take(32)))
            .put("classes", JSONArray(snapshot.classes.take(32)))
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

        runCatching {
            traceFile.appendText(row.toString() + "\n")
            trimTraceFile()
        }

        prefs.edit()
            .putInt("diagnostic_event_count", prefs.getInt("diagnostic_event_count", 0) + 1)
            .putInt("diagnostic_${event.kind.name.lowercase()}_count", prefs.getInt("diagnostic_${event.kind.name.lowercase()}_count", 0) + 1)
            .apply()
    }

    fun calibrationEventCount(): Int = prefs.getInt("diagnostic_event_count", 0)

    fun addActiveMillis(platform: TargetPlatform, millis: Long) {
        ensureDay(); if (millis <= 0L) return
        val key = "${platform.name.lowercase()}_active_ms"
        prefs.edit().putLong(key, prefs.getLong(key, 0L) + millis).apply()
    }

    fun setLastEvent(value: String) = prefs.edit().putString("last_event", value.take(300)).apply()
    fun lastEvent(): String = prefs.getString("last_event", "none") ?: "none"
    fun generation(): Int = prefs.getInt("generation", 0)

    fun markServiceConnected() {
        val now = System.currentTimeMillis()
        servicePrefs.edit()
            .putBoolean("connected", true)
            .putLong("heartbeat_wall_ms", now)
            .putLong("connected_wall_ms", now)
            .remove("last_error")
            .apply()
    }

    fun markServiceHeartbeat() {
        servicePrefs.edit()
            .putBoolean("connected", true)
            .putLong("heartbeat_wall_ms", System.currentTimeMillis())
            .apply()
    }

    fun markServiceDisconnected() {
        servicePrefs.edit().putBoolean("connected", false).putLong("heartbeat_wall_ms", System.currentTimeMillis()).apply()
    }

    fun recordServiceError(t: Throwable) {
        val value = "${t.javaClass.simpleName}: ${t.message ?: "no message"}".take(500)
        servicePrefs.edit()
            .putString("last_error", value)
            .putInt("error_count", servicePrefs.getInt("error_count", 0) + 1)
            .putLong("last_error_wall_ms", System.currentTimeMillis())
            .apply()
        setLastEvent("service_error/$value")
    }

    fun serviceAlive(): Boolean {
        if (!servicePrefs.getBoolean("connected", false)) return false
        val heartbeat = servicePrefs.getLong("heartbeat_wall_ms", 0L)
        return heartbeat > 0L && System.currentTimeMillis() - heartbeat <= 15_000L
    }

    fun accessibilityEnabled(): Boolean {
        val manager = app.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK).any {
            it.resolveInfo.serviceInfo.packageName == app.packageName && it.resolveInfo.serviceInfo.name.endsWith(".ScrollAccessibilityService")
        }
    }

    fun stats(platform: TargetPlatform): PlatformStats {
        ensureDay(); val p = platform.name.lowercase()
        return PlatformStats(
            prefs.getInt("${p}_count", 0),
            prefs.getLong("${p}_active_ms", 0L) / 1000L,
            prefs.getString("${p}_state", DetectorState.OUTSIDE_TARGET_APP.name) ?: DetectorState.OUTSIDE_TARGET_APP.name,
            prefs.getFloat("${p}_confidence", 0f).toDouble(),
            prefs.getString("${p}_reason", "none") ?: "none"
        )
    }

    fun startTest(platform: TargetPlatform) {
        ensureDay()
        runCatching { traceFile.delete() }
        val editor = prefs.edit()
            .putString("test_id", UUID.randomUUID().toString())
            .putString("test_platform", platform.name)
            .putLong("test_started", System.currentTimeMillis())
            .putInt("test_start_count", stats(platform).count)
            .putInt("diagnostic_event_count", 0)
            .remove("test_result")
        ProbeEventKind.entries.forEach { editor.putInt("diagnostic_${it.name.lowercase()}_count", 0) }
        editor.commit()
    }

    fun cancelTest() = prefs.edit().remove("test_platform").remove("test_id").remove("test_started").remove("test_start_count").apply()
    fun activeTest(): TargetPlatform? = prefs.getString("test_platform", null)?.let { runCatching { TargetPlatform.valueOf(it) }.getOrNull() }

    fun stopTest(actual: Int): JSONObject? {
        val platform = activeTest() ?: return null
        val detected = stats(platform).count - prefs.getInt("test_start_count", stats(platform).count)
        val candidates = candidateTransitionsFromTrace(platform)
        val error = kotlin.math.abs(detected - actual).toDouble() / actual.toDouble() * 100.0
        val candidateError = kotlin.math.abs(candidates - actual).toDouble() / actual.toDouble() * 100.0
        val result = JSONObject()
            .put("test_id", prefs.getString("test_id", "unknown"))
            .put("platform", platform.name)
            .put("detected_videos", detected)
            .put("candidate_transition_events", candidates)
            .put("captured_structural_events", calibrationEventCount())
            .put("manual_actual_videos", actual)
            .put("error_percent", error)
            .put("candidate_error_percent", candidateError)
            .put("pass_under_5_percent", error <= 5.0)
        prefs.edit().putString("test_result", result.toString()).remove("test_platform").commit()
        return result
    }

    fun lastTestResult(): JSONObject? = prefs.getString("test_result", null)?.let { runCatching { JSONObject(it) }.getOrNull() }

    fun resetAll() {
        val next = generation() + 1
        runCatching { traceFile.delete() }
        val e = prefs.edit()
            .putString("stats_date", LocalDate.now().toString())
            .putInt("generation", next)
            .putString("last_event", "counters_reset")
            .remove("test_platform")
            .remove("test_id")
            .remove("test_started")
            .remove("test_start_count")
            .remove("test_result")
            .putInt("diagnostic_event_count", 0)
        ProbeEventKind.entries.forEach { e.putInt("diagnostic_${it.name.lowercase()}_count", 0) }
        TargetPlatform.entries.forEach { p ->
            val k = p.name.lowercase()
            e.putInt("${k}_count", 0)
                .putLong("${k}_active_ms", 0L)
                .putString("${k}_state", DetectorState.OUTSIDE_TARGET_APP.name)
                .putString("${k}_reason", "none")
                .putFloat("${k}_confidence", 0f)
        }
        e.commit()
    }

    fun report(): JSONObject {
        ensureDay(); val platforms = JSONArray()
        TargetPlatform.entries.forEach { p ->
            val s = stats(p)
            platforms.put(
                JSONObject()
                    .put("platform", p.name)
                    .put("package", p.packageName)
                    .put("installed_version", installedVersion(p.packageName))
                    .put("detected_videos", s.count)
                    .put("active_seconds", s.activeSeconds)
                    .put("state", s.state)
                    .put("confidence", s.confidence)
                    .put("reason", s.reason)
            )
        }
        val counts = JSONObject()
        ProbeEventKind.entries.forEach { counts.put(it.name, prefs.getInt("diagnostic_${it.name.lowercase()}_count", 0)) }
        val enabled = accessibilityEnabled()
        val alive = serviceAlive()
        return JSONObject()
            .put("schema_version", 6)
            .put("local_date", LocalDate.now().toString())
            .put("generated_at", Instant.now().toString())
            .put("app_version", BuildConfig.VERSION_NAME)
            .put("automatic_counting", enabled && alive)
            .put("accessibility_service_enabled", enabled)
            .put("accessibility_service_alive", alive)
            .put("service_error_count", servicePrefs.getInt("error_count", 0))
            .put("service_last_error", servicePrefs.getString("last_error", null))
            .put("service_last_error_wall_ms", servicePrefs.getLong("last_error_wall_ms", 0L))
            .put("network_permission_declared", false)
            .put("raw_text_collected", false)
            .put("screenshots_collected", false)
            .put("device", JSONObject().put("manufacturer", Build.MANUFACTURER).put("model", Build.MODEL).put("android_release", Build.VERSION.RELEASE).put("sdk_int", Build.VERSION.SDK_INT))
            .put("platforms", platforms)
            .put("last_event", lastEvent())
            .put("last_controlled_test", lastTestResult())
            .put("captured_structural_events", calibrationEventCount())
            .put("event_kind_counts", counts)
            .put("diagnostic_trace", loadTrace())
    }

    private fun loadTrace(): JSONArray {
        val array = JSONArray()
        val lines = runCatching { if (traceFile.exists()) traceFile.readLines() else emptyList() }.getOrElse { emptyList() }
        lines.takeLast(200).forEach { line -> runCatching { array.put(JSONObject(line)) } }
        return array
    }

    private fun candidateTransitionsFromTrace(platform: TargetPlatform): Int {
        var count = 0
        var last = Long.MIN_VALUE
        val trace = loadTrace()
        for (i in 0 until trace.length()) {
            val row = trace.optJSONObject(i) ?: continue
            if (row.optString("platform") != platform.name) continue
            val kind = row.optString("kind")
            val sourceId = row.optString("source_id")
            val isCandidate = if (platform == TargetPlatform.YOUTUBE) {
                kind == ProbeEventKind.CONTENT_CHANGED.name && sourceId.contains("reel_recycler", ignoreCase = true)
            } else {
                kind == ProbeEventKind.SCROLLED.name || kind == ProbeEventKind.VIEW_SELECTED.name
            }
            if (!isCandidate) continue
            val t = row.optLong("t", Long.MIN_VALUE)
            val cooldown = if (platform == TargetPlatform.YOUTUBE) 500L else 320L
            if (last == Long.MIN_VALUE || t - last >= cooldown) { count += 1; last = t }
        }
        return count
    }

    private fun trimTraceFile() {
        if (!traceFile.exists() || traceFile.length() < 350_000L) return
        val tail = traceFile.readLines().takeLast(220)
        traceFile.writeText(tail.joinToString("\n", postfix = if (tail.isNotEmpty()) "\n" else ""))
    }

    private fun ensureDay() {
        val today = LocalDate.now().toString(); val old = prefs.getString("stats_date", null)
        if (old == today) return
        if (old == null) { prefs.edit().putString("stats_date", today).apply(); return }
        runCatching { traceFile.delete() }
        val e = prefs.edit().putString("stats_date", today).putInt("generation", generation() + 1).remove("test_platform").remove("test_result").putInt("diagnostic_event_count", 0)
        ProbeEventKind.entries.forEach { e.putInt("diagnostic_${it.name.lowercase()}_count", 0) }
        TargetPlatform.entries.forEach { p ->
            val k = p.name.lowercase()
            e.putInt("${k}_count", 0)
                .putLong("${k}_active_ms", 0L)
                .putString("${k}_state", DetectorState.OUTSIDE_TARGET_APP.name)
                .putString("${k}_reason", "none")
                .putFloat("${k}_confidence", 0f)
        }
        e.commit()
    }

    @Suppress("DEPRECATION")
    private fun installedVersion(packageName: String): String? = runCatching { app.packageManager.getPackageInfo(packageName, 0).versionName }.getOrNull()
}
