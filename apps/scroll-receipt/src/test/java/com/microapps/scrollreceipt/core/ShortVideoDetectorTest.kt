package com.microapps.scrollreceipt.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ShortVideoDetectorTest {
    private fun e(t: Long, p: TargetPlatform, kind: ProbeEventKind, ids: Set<String>, dy: Int = 0) = ProbeEvent(t, p.packageName, kind, viewIds = ids, scrollDeltaY = dy, dominantFullScreenScrollable = true)
    @Test fun youtubeCountsAndDeduplicates() {
        val d = ShortVideoDetector(TargetPlatform.YOUTUBE)
        assertEquals(0, d.process(e(1000, TargetPlatform.YOUTUBE, ProbeEventKind.CONTENT_CHANGED, setOf("shorts_player"))).increment)
        assertEquals(1, d.process(e(1200, TargetPlatform.YOUTUBE, ProbeEventKind.CONTENT_CHANGED, setOf("reel_watch"))).increment)
        assertEquals(1, d.process(e(2000, TargetPlatform.YOUTUBE, ProbeEventKind.SCROLLED, setOf("shorts_player"), 900)).increment)
        assertEquals(0, d.process(e(2100, TargetPlatform.YOUTUBE, ProbeEventKind.SCROLLED, setOf("shorts_player"), 900)).increment)
    }
    @Test fun instagramCommentsPause() {
        val d = ShortVideoDetector(TargetPlatform.INSTAGRAM)
        d.process(e(1000, TargetPlatform.INSTAGRAM, ProbeEventKind.CONTENT_CHANGED, setOf("reels_viewer")))
        d.process(e(1200, TargetPlatform.INSTAGRAM, ProbeEventKind.CONTENT_CHANGED, setOf("clips_viewer")))
        val paused = d.process(e(2000, TargetPlatform.INSTAGRAM, ProbeEventKind.CONTENT_CHANGED, setOf("comments_bottom_sheet")))
        assertEquals(DetectorState.PAUSED, paused.state)
        assertEquals(0, d.process(e(2600, TargetPlatform.INSTAGRAM, ProbeEventKind.SCROLLED, setOf("comments_bottom_sheet"), 900)).increment)
    }
    @Test fun packageExitStopsTiming() {
        val d = ShortVideoDetector(TargetPlatform.YOUTUBE)
        d.process(e(1000, TargetPlatform.YOUTUBE, ProbeEventKind.CONTENT_CHANGED, setOf("shorts_player")))
        d.process(e(1200, TargetPlatform.YOUTUBE, ProbeEventKind.CONTENT_CHANGED, setOf("reel_watch")))
        val r = d.process(ProbeEvent(1500, "com.example.other", ProbeEventKind.WINDOW_CHANGED))
        assertEquals(DetectorState.OUTSIDE_TARGET_APP, r.state); assertFalse(r.activeForTiming)
    }
}
