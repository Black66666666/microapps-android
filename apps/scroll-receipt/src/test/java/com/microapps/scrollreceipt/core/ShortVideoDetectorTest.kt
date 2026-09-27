package com.microapps.scrollreceipt.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ShortVideoDetectorTest {
    private fun e(
        t: Long,
        p: TargetPlatform,
        kind: ProbeEventKind,
        ids: Set<String>,
        dy: Int = 0,
        dominant: Boolean = true,
        eventClass: String? = null,
        sourceId: String? = null,
        sourceClass: String? = null
    ) = ProbeEvent(
        timestampMs = t,
        packageName = p.packageName,
        kind = kind,
        viewIds = ids,
        scrollDeltaY = dy,
        dominantFullScreenScrollable = dominant,
        eventClass = eventClass,
        sourceId = sourceId,
        sourceClass = sourceClass
    )

    @Test fun youtubeCountsGenericAndDeduplicates() {
        val d = ShortVideoDetector(TargetPlatform.YOUTUBE)
        assertEquals(0, d.process(e(1000, TargetPlatform.YOUTUBE, ProbeEventKind.CONTENT_CHANGED, setOf("shorts_player"))).increment)
        assertEquals(1, d.process(e(1200, TargetPlatform.YOUTUBE, ProbeEventKind.CONTENT_CHANGED, setOf("reel_watch"))).increment)
    }

    @Test fun youtubeRedmiSourceTraceCountsFiveVisibleShorts() {
        val d = ShortVideoDetector(TargetPlatform.YOUTUBE)
        val reelId = "com.google.android.youtube:id/reel_recycler"
        var total = 0

        // The exact reel_recycler source is strong enough to identify the first
        // visible Short without traversing the full accessibility tree.
        total += d.process(
            e(
                1000,
                TargetPlatform.YOUTUBE,
                ProbeEventKind.CONTENT_CHANGED,
                setOf(reelId),
                dominant = false,
                eventClass = "android.support.v7.widget.RecyclerView",
                sourceId = reelId,
                sourceClass = "android.support.v7.widget.RecyclerView"
            )
        ).increment

        // A duplicate event immediately after entering the reel is ignored.
        total += d.process(
            e(1100, TargetPlatform.YOUTUBE, ProbeEventKind.CONTENT_CHANGED, setOf(reelId), dominant = false, sourceId = reelId)
        ).increment

        // Four genuinely separate reel transitions -> five visible Shorts total.
        listOf(2000L, 3500L, 5000L, 6500L).forEach { t ->
            total += d.process(
                e(
                    t,
                    TargetPlatform.YOUTUBE,
                    ProbeEventKind.CONTENT_CHANGED,
                    setOf(reelId),
                    dominant = false,
                    eventClass = "android.support.v7.widget.RecyclerView",
                    sourceId = reelId,
                    sourceClass = "android.support.v7.widget.RecyclerView"
                )
            ).increment
        }
        assertEquals(5, total)
    }

    @Test fun youtubeSeekBarProgressDoesNotCreateExtraVideos() {
        val d = ShortVideoDetector(TargetPlatform.YOUTUBE)
        val ids = setOf(
            "com.google.android.youtube:id/reel_recycler",
            "com.google.android.youtube:id/reel_player_page_container",
            "com.google.android.youtube:id/reel_progress_bar"
        )
        d.process(e(1000, TargetPlatform.YOUTUBE, ProbeEventKind.CONTENT_CHANGED, ids, dominant = false))
        assertEquals(1, d.process(e(1002, TargetPlatform.YOUTUBE, ProbeEventKind.CONTENT_CHANGED, ids, dominant = false)).increment)
        assertEquals(0, d.process(e(15000, TargetPlatform.YOUTUBE, ProbeEventKind.CONTENT_CHANGED, ids, dominant = false, eventClass = "android.widget.SeekBar")).increment)
        assertEquals(0, d.process(e(30000, TargetPlatform.YOUTUBE, ProbeEventKind.CONTENT_CHANGED, ids, dominant = false, eventClass = "android.widget.SeekBar")).increment)
    }

    @Test fun hiddenYoutubeSearchNodeDoesNotPauseShorts() {
        val d = ShortVideoDetector(TargetPlatform.YOUTUBE)
        val ids = setOf(
            "com.google.android.youtube:id/reel_recycler",
            "com.google.android.youtube:id/reel_player_page_container",
            "com.google.android.youtube:id/menu_search"
        )
        d.process(e(1000, TargetPlatform.YOUTUBE, ProbeEventKind.CONTENT_CHANGED, ids, dominant = false))
        val active = d.process(e(1002, TargetPlatform.YOUTUBE, ProbeEventKind.CONTENT_CHANGED, ids, dominant = false))
        assertEquals(DetectorState.SHORT_MODE_ACTIVE, active.state)
    }

    @Test fun ordinaryYoutubeFeedDoesNotCountAsShorts() {
        val d = ShortVideoDetector(TargetPlatform.YOUTUBE)
        val ids = setOf(
            "com.google.android.youtube:id/results",
            "com.google.android.youtube:id/youtube_logo",
            "com.google.android.youtube:id/pivot_bar"
        )
        assertEquals(0, d.process(e(1000, TargetPlatform.YOUTUBE, ProbeEventKind.CONTENT_CHANGED, ids)).increment)
        assertEquals(0, d.process(e(1500, TargetPlatform.YOUTUBE, ProbeEventKind.SCROLLED, ids, 900)).increment)
    }

    @Test fun instagramCommentsPause() {
        val d = ShortVideoDetector(TargetPlatform.INSTAGRAM)
        d.process(e(1000, TargetPlatform.INSTAGRAM, ProbeEventKind.CONTENT_CHANGED, setOf("reels_viewer")))
        d.process(e(1200, TargetPlatform.INSTAGRAM, ProbeEventKind.CONTENT_CHANGED, setOf("clips_viewer")))
        val paused = d.process(
            e(
                2000,
                TargetPlatform.INSTAGRAM,
                ProbeEventKind.CONTENT_CHANGED,
                setOf("comments_bottom_sheet"),
                sourceId = "com.instagram.android:id/comments_bottom_sheet"
            )
        )
        assertEquals(DetectorState.PAUSED, paused.state)
        assertEquals(0, d.process(e(2600, TargetPlatform.INSTAGRAM, ProbeEventKind.SCROLLED, setOf("comments_bottom_sheet"), 900, sourceId = "com.instagram.android:id/comments_bottom_sheet")).increment)
    }

    @Test fun packageExitStopsTiming() {
        val d = ShortVideoDetector(TargetPlatform.YOUTUBE)
        d.process(e(1000, TargetPlatform.YOUTUBE, ProbeEventKind.CONTENT_CHANGED, setOf("shorts_player")))
        d.process(e(1200, TargetPlatform.YOUTUBE, ProbeEventKind.CONTENT_CHANGED, setOf("reel_watch")))
        val r = d.process(ProbeEvent(1500, "com.example.other", ProbeEventKind.WINDOW_CHANGED))
        assertEquals(DetectorState.OUTSIDE_TARGET_APP, r.state)
        assertFalse(r.activeForTiming)
    }
}
