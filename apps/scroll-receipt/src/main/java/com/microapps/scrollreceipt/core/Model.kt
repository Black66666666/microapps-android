package com.microapps.scrollreceipt.core

enum class TargetPlatform(val packageName: String) {
    TIKTOK("com.zhiliaoapp.musically"),
    INSTAGRAM("com.instagram.android"),
    YOUTUBE("com.google.android.youtube");
    companion object {
        fun fromPackage(packageName: String?): TargetPlatform? = entries.firstOrNull { it.packageName == packageName }
    }
}

enum class ProbeEventKind { WINDOW_CHANGED, CONTENT_CHANGED, SCROLLED, VIEW_SELECTED, WINDOWS_CHANGED, UNKNOWN }
enum class DetectorState { OUTSIDE_TARGET_APP, TARGET_APP_NON_SHORT_MODE, SHORT_MODE_ACTIVE, PAUSED }

data class ProbeEvent(
    val timestampMs: Long,
    val packageName: String,
    val kind: ProbeEventKind,
    val viewIds: Set<String> = emptySet(),
    val classNames: Set<String> = emptySet(),
    val fromIndex: Int = -1,
    val toIndex: Int = -1,
    val itemCount: Int = -1,
    val scrollDeltaY: Int = 0,
    val scrollY: Int = 0,
    val maxScrollY: Int = 0,
    val dominantFullScreenScrollable: Boolean = false
)

data class DetectorDecision(
    val platform: TargetPlatform,
    val state: DetectorState,
    val increment: Int,
    val confidence: Double,
    val reason: String,
    val activeForTiming: Boolean
)
