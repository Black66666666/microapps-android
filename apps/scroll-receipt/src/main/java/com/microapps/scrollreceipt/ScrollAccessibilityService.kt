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
    private var timingPlatform: TargetPlatform? = null
    private var lastTick = 0L
    private var lastHeartbeatAt = 0L
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
                if (now - lastHeartbeatAt >= 5_000L) {
                    store.markServiceHeartbeat()
                    lastHeartbeatAt = now
                }
                val active = timingPlatform
                if (active != null) {
                    val activePackage = runCatching { rootInActiveWindow?.packageName?.toString() }.getOrNull()
                    if (activePackage != active.packageName) {
                        flushTime(); timingPlatform = null
                    }
                }
                flushTime()
            }
            handler.postDelayed(this, 2_000L)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        store = ProbeStore(this)
        generation = store.generation()
        lastTick = SystemClock.elapsedRealtime()
        lastHeartbeatAt = lastTick
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
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !::store.isInitialized) return
        try {
            if (generation != store.generation()) {
                flushTime()
                engine.reset()
                timingPlatform = null
                generation = store.generation()
            }

            val platform = TargetPlatform.fromPackage(event.packageName?.toString()) ?: run {
                flushTime(); timingPlatform = null; return
            }
            val diagnostic = store.activeTest() == platform
            val snapshot = AccessibilitySnapshotFactory.from(
                event = event,
                root = rootInActiveWindow,
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

            if (screenOn && decision.activeForTiming) {
                switchTiming(platform)
            } else {
                flushTime(); timingPlatform = null
            }

            val now = SystemClock.elapsedRealtime()
            val stateKey = "${decision.platform.name}/${decision.state}/${decision.reason}"
            if (diagnostic || decision.increment > 0 || stateKey != lastSummaryState || now - lastSummaryAt >= 2_000L) {
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
        val delta = (now - lastTick).coerceIn(0L, 5_000L)
        if (delta > 0L && screenOn && ::store.isInitialized) store.addActiveMillis(platform, delta)
        lastTick = now
    }
}
