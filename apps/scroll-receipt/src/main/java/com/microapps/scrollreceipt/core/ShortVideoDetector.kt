package com.microapps.scrollreceipt.core

class ShortVideoDetector(private val platform: TargetPlatform) {
    private val strongHints = when (platform) {
        TargetPlatform.TIKTOK -> setOf("aweme", "video", "feed", "pager", "vertical", "tiktok")
        TargetPlatform.INSTAGRAM -> setOf("reel", "reels", "clips", "clips_viewer", "reels_viewer")
        TargetPlatform.YOUTUBE -> setOf("shorts", "reel", "reel_watch", "shorts_player")
    }
    private val pauseHints = setOf("comment", "comments", "bottom_sheet", "dialog")
    private val modeThreshold = if (platform == TargetPlatform.TIKTOK) 0.60 else 0.68
    private val transitionCooldownMs = if (platform == TargetPlatform.YOUTUBE) 220L else 450L
    private var state = DetectorState.OUTSIDE_TARGET_APP
    private var evidenceCount = 0
    private var lastStrongModeAt = 0L
    private var lastCountAt = Long.MIN_VALUE
    private var firstVideoSeen = false

    fun reset() {
        state = DetectorState.OUTSIDE_TARGET_APP
        evidenceCount = 0
        lastStrongModeAt = 0L
        lastCountAt = Long.MIN_VALUE
        firstVideoSeen = false
    }

    fun process(event: ProbeEvent): DetectorDecision {
        if (event.packageName != platform.packageName) {
            state = DetectorState.OUTSIDE_TARGET_APP
            evidenceCount = 0
            firstVideoSeen = false
            return decision(0, 0.0, "different package", false)
        }

        val tokens = (event.viewIds + event.classNames).map { it.lowercase() }.toSet()
        val sourceTokens = listOfNotNull(event.sourceId, event.sourceClass, event.eventClass).map { it.lowercase() }
        val overlay = pauseHints.any { hint -> sourceTokens.any { it.contains(hint) } }
        val confidence = confidence(event, tokens)

        if (overlay && state == DetectorState.SHORT_MODE_ACTIVE) {
            state = DetectorState.PAUSED
            return decision(0, confidence, "foreground overlay", false)
        }

        if (confidence >= modeThreshold) {
            lastStrongModeAt = event.timestampMs
            if (state == DetectorState.PAUSED) {
                state = DetectorState.SHORT_MODE_ACTIVE
                evidenceCount = 2
                return decision(0, confidence, "returned to short-video UI", true)
            }
            if (state != DetectorState.SHORT_MODE_ACTIVE) {
                evidenceCount += 1
                state = DetectorState.TARGET_APP_NON_SHORT_MODE
                if (evidenceCount < 2) return decision(0, confidence, "short mode evidence 1/2", false)
                state = DetectorState.SHORT_MODE_ACTIVE
                if (!firstVideoSeen) {
                    firstVideoSeen = true
                    lastCountAt = event.timestampMs
                    return decision(1, confidence, "short mode confirmed; first visible video", true)
                }
            }
            if (state == DetectorState.SHORT_MODE_ACTIVE && isTransition(event, tokens)) {
                if (event.timestampMs - lastCountAt >= transitionCooldownMs) {
                    lastCountAt = event.timestampMs
                    return decision(1, confidence, if (platform == TargetPlatform.YOUTUBE) "YouTube reel recycler changed" else "new short-video transition", true)
                }
                return decision(0, confidence, "duplicate transition event ignored", true)
            }
            return decision(0, confidence, "short mode active", true)
        }

        if (state == DetectorState.SHORT_MODE_ACTIVE || state == DetectorState.PAUSED) {
            val canLeave = event.kind == ProbeEventKind.WINDOW_CHANGED || event.kind == ProbeEventKind.WINDOWS_CHANGED || event.kind == ProbeEventKind.CONTENT_CHANGED
            if (canLeave && event.timestampMs - lastStrongModeAt >= 2500L) {
                state = DetectorState.TARGET_APP_NON_SHORT_MODE
                evidenceCount = 0
                firstVideoSeen = false
                return decision(0, confidence, "short-mode evidence expired", false)
            }
        } else {
            state = DetectorState.TARGET_APP_NON_SHORT_MODE
            evidenceCount = 0
        }
        return decision(0, confidence, "target app, short mode not confirmed", false)
    }

    private fun confidence(event: ProbeEvent, tokens: Set<String>): Double {
        if (platform == TargetPlatform.YOUTUBE && youtubeShortUi(tokens)) return 0.95
        val matching = strongHints.count { hint -> tokens.any { it.contains(hint) } }
        var score = when {
            matching >= 3 -> 0.75
            matching == 2 -> 0.62
            matching == 1 -> 0.46
            else -> 0.0
        }
        if (event.dominantFullScreenScrollable) score += if (platform == TargetPlatform.TIKTOK) 0.26 else 0.12
        if (event.kind == ProbeEventKind.SCROLLED && meaningfulScroll(event)) score += 0.12
        return score.coerceIn(0.0, 1.0)
    }

    private fun isTransition(event: ProbeEvent, tokens: Set<String>): Boolean {
        if (platform == TargetPlatform.YOUTUBE) {
            return event.kind == ProbeEventKind.CONTENT_CHANGED &&
                event.sourceId?.contains("reel_recycler", ignoreCase = true) == true &&
                youtubeShortUi(tokens)
        }
        if (event.kind != ProbeEventKind.SCROLLED && event.kind != ProbeEventKind.VIEW_SELECTED) return false
        val indexChanged = event.fromIndex >= 0 && event.toIndex >= 0 && event.fromIndex != event.toIndex
        return indexChanged || kotlin.math.abs(event.scrollDeltaY) >= 160 || (event.dominantFullScreenScrollable && meaningfulScroll(event))
    }

    private fun youtubeShortUi(tokens: Set<String>): Boolean {
        val hasReelRecycler = tokens.any { it.contains("reel_recycler") }
        val hasPlayerPage = tokens.any { it.contains("reel_player_page_container") }
        return hasReelRecycler && hasPlayerPage
    }

    private fun meaningfulScroll(event: ProbeEvent) = kotlin.math.abs(event.scrollDeltaY) >= 80 || event.maxScrollY > 0 || event.fromIndex != event.toIndex
    private fun decision(increment: Int, confidence: Double, reason: String, active: Boolean) = DetectorDecision(platform, state, increment, confidence, reason, active)
}
