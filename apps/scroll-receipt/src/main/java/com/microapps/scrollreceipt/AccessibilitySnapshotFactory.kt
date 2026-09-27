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
        val maxNodes = if (diagnostic) 220 else 90
        val maxDepth = if (diagnostic) 12 else 8
        val screenArea = (metrics.widthPixels.toLong() * metrics.heightPixels.toLong()).coerceAtLeast(1L)
        val eventClass = event.className?.toString()?.take(160)
        eventClass?.let { classes.add(it) }

        if (root != null) {
            val queue = ArrayDeque<Pair<AccessibilityNodeInfo, Int>>()
            queue.add(root to 0)
            while (queue.isNotEmpty() && visited < maxNodes) {
                val (node, depth) = queue.removeFirst()
                if (depth > maxDepth) continue
                visited += 1
                node.viewIdResourceName?.let { ids.add(it.take(180)) }
                node.className?.toString()?.let { classes.add(it.take(160)) }
                if (node.isScrollable) {
                    val bounds = Rect()
                    node.getBoundsInScreen(bounds)
                    val area = bounds.width().coerceAtLeast(0).toLong() * bounds.height().coerceAtLeast(0).toLong()
                    if (area.toDouble() / screenArea.toDouble() >= 0.48) dominantScrollable = true
                }
                for (i in 0 until node.childCount) {
                    node.getChild(i)?.let { queue.add(it to depth + 1) }
                }
            }
        }

        val sourceId = event.source?.viewIdResourceName?.take(180)
        val sourceClass = event.source?.className?.toString()?.take(160)
        sourceId?.let { ids.add(it) }
        sourceClass?.let { classes.add(it) }
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

    private fun mapKind(type: Int) = when (type) {
        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> ProbeEventKind.WINDOW_CHANGED
        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> ProbeEventKind.CONTENT_CHANGED
        AccessibilityEvent.TYPE_VIEW_SCROLLED -> ProbeEventKind.SCROLLED
        AccessibilityEvent.TYPE_VIEW_SELECTED -> ProbeEventKind.VIEW_SELECTED
        AccessibilityEvent.TYPE_WINDOWS_CHANGED -> ProbeEventKind.WINDOWS_CHANGED
        else -> ProbeEventKind.UNKNOWN
    }
}
