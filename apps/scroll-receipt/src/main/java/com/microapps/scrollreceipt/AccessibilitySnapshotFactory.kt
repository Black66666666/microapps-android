package com.microapps.scrollreceipt

import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.microapps.scrollreceipt.core.ProbeEvent
import com.microapps.scrollreceipt.core.ProbeEventKind
import java.util.ArrayDeque

object AccessibilitySnapshotFactory {
    data class Snapshot(
        val event: ProbeEvent,
        val nodes: Int,
        val viewIds: Set<String>,
        val classes: Set<String>,
        val rawEventType: Int,
        val eventClass: String?,
        val sourceId: String?,
        val sourceClass: String?,
        val action: Int
    )

    fun from(
        event: AccessibilityEvent,
        root: AccessibilityNodeInfo?,
        metrics: DisplayMetrics,
        diagnostic: Boolean = false
    ): Snapshot {
        val ids = linkedSetOf<String>()
        val classes = linkedSetOf<String>()
        var visited = 0
        var dominantScrollable = false
        val screenArea = (metrics.widthPixels.toLong() * metrics.heightPixels.toLong()).coerceAtLeast(1L)
        val eventClass = event.className?.toString()?.take(160)
        eventClass?.let { classes.add(it) }

        // Normal counting deliberately avoids walking the accessibility tree. Full
        // traversal is diagnostic-only and nodes are recycled on Android <= 12L,
        // where AccessibilityNodeInfo still uses object pooling.
        if (diagnostic && root != null) {
            val queue = ArrayDeque<Pair<AccessibilityNodeInfo, Int>>()
            queue.add(root to 0)
            while (queue.isNotEmpty() && visited < 220) {
                val (node, depth) = queue.removeFirst()
                if (depth > 12) {
                    recycleCompat(node)
                    continue
                }
                visited += 1
                runCatching { node.viewIdResourceName?.let { ids.add(it.take(180)) } }
                runCatching { node.className?.toString()?.let { classes.add(it.take(160)) } }
                if (runCatching { node.isScrollable }.getOrDefault(false)) {
                    val bounds = Rect()
                    runCatching { node.getBoundsInScreen(bounds) }
                    val area = bounds.width().coerceAtLeast(0).toLong() * bounds.height().coerceAtLeast(0).toLong()
                    if (area.toDouble() / screenArea.toDouble() >= 0.48) dominantScrollable = true
                }
                val childCount = runCatching { node.childCount }.getOrDefault(0)
                for (i in 0 until childCount) runCatching { node.getChild(i) }.getOrNull()?.let { queue.add(it to depth + 1) }
                recycleCompat(node)
            }
            while (queue.isNotEmpty()) recycleCompat(queue.removeFirst().first)
        }

        val source = runCatching { event.source }.getOrNull()
        val sourceId = runCatching { source?.viewIdResourceName?.take(180) }.getOrNull()
        val sourceClass = runCatching { source?.className?.toString()?.take(160) }.getOrNull()
        sourceId?.let { ids.add(it) }
        sourceClass?.let { classes.add(it) }
        if (!diagnostic && source != null && runCatching { source.isScrollable }.getOrDefault(false)) {
            val bounds = Rect()
            runCatching { source.getBoundsInScreen(bounds) }
            val area = bounds.width().coerceAtLeast(0).toLong() * bounds.height().coerceAtLeast(0).toLong()
            dominantScrollable = area.toDouble() / screenArea.toDouble() >= 0.48
            visited = 1
        }
        source?.let { recycleCompat(it) }

        val deltaY = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) event.scrollDeltaY else 0
        return Snapshot(
            event = ProbeEvent(
                timestampMs = event.eventTime,
                packageName = event.packageName?.toString().orEmpty(),
                kind = mapKind(event.eventType),
                viewIds = ids,
                classNames = classes,
                fromIndex = event.fromIndex,
                toIndex = event.toIndex,
                itemCount = event.itemCount,
                scrollDeltaY = deltaY,
                scrollY = event.scrollY,
                maxScrollY = event.maxScrollY,
                dominantFullScreenScrollable = dominantScrollable,
                eventClass = eventClass,
                sourceId = sourceId,
                sourceClass = sourceClass
            ),
            nodes = visited,
            viewIds = ids,
            classes = classes,
            rawEventType = event.eventType,
            eventClass = eventClass,
            sourceId = sourceId,
            sourceClass = sourceClass,
            action = event.action
        )
    }

    @Suppress("DEPRECATION")
    private fun recycleCompat(node: AccessibilityNodeInfo) {
        if (Build.VERSION.SDK_INT < 33) runCatching { node.recycle() }
    }

    private fun mapKind(type: Int) = when (type) {
        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> ProbeEventKind.WINDOW_CHANGED
        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> ProbeEventKind.CONTENT_CHANGED
        AccessibilityEvent.TYPE_VIEW_SCROLLED -> ProbeEventKind.SCROLLED
        AccessibilityEvent.TYPE_VIEW_SELECTED -> ProbeEventKind.VIEW_SELECTED
        AccessibilityEvent.TYPE_WINDOWS_CHANGED -> ProbeEventKind.WINDOWS_CHANGED
        else -> ProbeEventKind.UNKNOWN
    }
}
