package com.microapps.scrollreceipt

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.microapps.scrollreceipt.core.MeasurementEngine
import com.microapps.scrollreceipt.core.TargetPlatform
import java.util.concurrent.Executors

class ScrollAccessibilityService : AccessibilityService() {
    private lateinit var store: ProbeStore
    private val engine = MeasurementEngine()
    private val handler = Handler(Looper.getMainLooper())
    private val diagnosticIo = Executors.newSingleThreadExecutor()
    private var generation = -1
    private var diagnosticPlatform: TargetPlatform? = null
    private var timingPlatform: TargetPlatform? = null
    private var lastTick = 0L
    private var lastTargetEventAt = 0L
    private var lastHeartbeatAt = 0L
    private var lastGenerationCheckAt = 0L
    private var lastDiagnosticCheckAt = 0L
    private var lastSummaryAt = 0L
    private var lastSummaryState = ""
    private var screenOn = true

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!::store.isInitialized) return
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    flushTime(); screenOn = false; timingPlatform = null
                    store.setLastEvent("screen_off")
                }
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    screenOn = true; lastTick = SystemClock.elapsedRealtime()
                    store.setLastEvent("screen_on")
                }
            }
        }
    }

    private val ticker = object : Runnable {
        override fun run() {
            if (::store.isInitialized) {
                val now = SystemClock.elapsedRealtime()
                if (now - lastHeartbeatAt >= 10_000L) {
                    store.markServiceHeartbeat()
                    lastHeartbeatAt = now
                }
                // Avoid rootInActiveWindow polling here: on older MIUI builds it is an
                // expensive cross-process call. Stop timing only after prolonged silence.
                if (timingPlatform != null && lastTargetEventAt > 0L && now - lastTargetEventAt > 20_000L) {
                    flushTime(); timingPlatform = null
                } else {
                    flushTime()
                }
            }
            handler.postDelayed(this, 5_000L)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        try {
            store = ProbeStore(this)
            generation = store.generation()
            diagnosticPlatform = store.activeTest()
            lastTick = SystemClock.elapsedRealtime()
            lastHeartbeatAt = lastTick
            lastGenerationCheckAt = lastTick
            lastDiagnosticCheckAt = lastTick
            store.markServiceConnected()
            registerReceiver(
                screenReceiver,
                IntentFilter().apply {
                    addAction(Intent.ACTION_SCREEN_OFF)
                    addAction(Intent.ACTION_SCREEN_ON)
                    addAction(Intent.ACTION_USER_PRESENT)
                }
            )
            handler.post(ticker)
            store.setLastEvent("service_connected")
        } catch (t: Throwable) {
            if (::store.isInitialized) store.recordServiceError(t)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !::store.isInitialized) return
        try {
            val now = SystemClock.elapsedRealtime()
            if (now - lastGenerationCheckAt >= 2_000L) {
                val currentGeneration = store.generation()
                if (generation != currentGeneration) {
                    flushTime()
                    engine.reset()
                    timingPlatform = null
                    generation = currentGeneration
                }
                lastGenerationCheckAt = now
            }
            if (now - lastDiagnosticCheckAt >= 2_000L) {
                diagnosticPlatform = store.activeTest()
                lastDiagnosticCheckAt = now
            }

            val platform = TargetPlatform.fromPackage(event.packageName?.toString()) ?: return
            lastTargetEventAt = now
            val diagnostic = diagnosticPlatform == platform
            val snapshot = AccessibilitySnapshotFactory.from(
                event = event,
                root = if (diagnostic) rootInActiveWindow else null,
                metrics = resources.displayMetrics,
                diagnostic = diagnostic
            )
            val decision = engine.process(snapshot.event) ?: return
            store.update(decision)

            if (diagnostic) {
                diagnosticIo.execute {
                    runCatching { store.recordSnapshot(snapshot, decision) }
                        .onFailure { store.recordServiceError(it) }
                }
            }

            if (screenOn && decision.activeForTiming) switchTiming(platform)
            else { flushTime(); timingPlatform = null }

            val stateKey = "${decision.platform.name}/${decision.state}/${decision.reason}"
            if (diagnostic || decision.increment > 0 || stateKey != lastSummaryState || now - lastSummaryAt >= 5_000L) {
                store.setLastEvent(
                    "${decision.platform.name}/${snapshot.event.kind}/${decision.state}/inc=${decision.increment}/nodes=${snapshot.nodes}/class=${snapshot.eventClass ?: "-"}"
                )
                lastSummaryAt = now
                lastSummaryState = stateKey
            }
        } catch (t: Throwable) {
            store.recordServiceError(t)
            flushTime()
            timingPlatform = null
        }
    }

    override fun onInterrupt() {
        if (::store.isInitialized) store.markServiceInterrupted()
        flushTime()
        timingPlatform = null
    }

    override fun onDestroy() {
        handler.removeCallbacks(ticker)
        flushTime()
        diagnosticIo.shutdownNow()
        if (::store.isInitialized) store.markServiceDisconnected()
        runCatching { unregisterReceiver(screenReceiver) }
        super.onDestroy()
    }

    private fun switchTiming(platform: TargetPlatform) {
        val now = SystemClock.elapsedRealtime()
        if (timingPlatform == platform) {
            if (lastTick == 0L) lastTick = now
            return
        }
        flushTime()
        timingPlatform = platform
        lastTick = now
    }

    private fun flushTime() {
        val platform = timingPlatform ?: run {
            lastTick = SystemClock.elapsedRealtime()
            return
        }
        val now = SystemClock.elapsedRealtime()
        val delta = (now - lastTick).coerceIn(0L, 10_000L)
        if (delta > 0L && screenOn && ::store.isInitialized) store.addActiveMillis(platform, delta)
        lastTick = now
    }
}
