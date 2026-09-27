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

class ScrollAccessibilityService : AccessibilityService() {
    private lateinit var store: ProbeStore
    private val engine = MeasurementEngine()
    private val handler = Handler(Looper.getMainLooper())
    private var generation = -1
    private var timingPlatform: TargetPlatform? = null
    private var lastTick = 0L
    private var screenOn = true

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> { flushTime(); screenOn = false; timingPlatform = null; store.setLastEvent("screen_off") }
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> { screenOn = true; lastTick = SystemClock.elapsedRealtime(); store.setLastEvent("screen_on") }
            }
        }
    }
    private val ticker = object : Runnable {
        override fun run() {
            if (::store.isInitialized) store.markServiceHeartbeat()
            val active = timingPlatform
            if (active != null && rootInActiveWindow?.packageName?.toString() != active.packageName) { flushTime(); timingPlatform = null }
            flushTime(); handler.postDelayed(this, 1000L)
        }
    }

    override fun onServiceConnected() {
        store = ProbeStore(this)
        generation = store.generation()
        lastTick = SystemClock.elapsedRealtime()
        store.markServiceConnected()
        registerReceiver(screenReceiver, IntentFilter().apply { addAction(Intent.ACTION_SCREEN_OFF); addAction(Intent.ACTION_SCREEN_ON); addAction(Intent.ACTION_USER_PRESENT) })
        handler.post(ticker)
        store.setLastEvent("service_connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !::store.isInitialized) return
        store.markServiceHeartbeat()
        if (generation != store.generation()) { flushTime(); engine.reset(); timingPlatform = null; generation = store.generation() }
        val platform = TargetPlatform.fromPackage(event.packageName?.toString()) ?: run { flushTime(); timingPlatform = null; return }
        val snapshot = AccessibilitySnapshotFactory.from(event, rootInActiveWindow, resources.displayMetrics)
        val decision = engine.process(snapshot.event) ?: return
        store.update(decision)
        store.recordSnapshot(snapshot, decision)
        if (screenOn && decision.activeForTiming) switchTiming(platform) else { flushTime(); timingPlatform = null }
        store.setLastEvent("${decision.platform.name}/${snapshot.event.kind}/${decision.state}/inc=${decision.increment}/nodes=${snapshot.nodes}/class=${snapshot.eventClass ?: "-"}")
    }

    override fun onInterrupt() { flushTime(); timingPlatform = null }

    override fun onDestroy() {
        handler.removeCallbacks(ticker)
        flushTime()
        if (::store.isInitialized) store.markServiceDisconnected()
        runCatching { unregisterReceiver(screenReceiver) }
        super.onDestroy()
    }

    private fun switchTiming(platform: TargetPlatform) {
        val now = SystemClock.elapsedRealtime(); if (timingPlatform == platform) { if (lastTick == 0L) lastTick = now; return }
        flushTime(); timingPlatform = platform; lastTick = now
    }

    private fun flushTime() {
        val platform = timingPlatform ?: run { lastTick = SystemClock.elapsedRealtime(); return }
        val now = SystemClock.elapsedRealtime(); val delta = (now - lastTick).coerceIn(0L, 5000L)
        if (delta > 0L && screenOn) store.addActiveMillis(platform, delta); lastTick = now
    }
}
