package com.microapps.whobringswhat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BringLogicTest {
    @Test fun stateRoundTrips() {
        val state = BringState("Пикник", listOf(BringItem("Лёд", "Иван"), BringItem("Уголь")))
        assertEquals(state, BringStateCodec.decode(BringStateCodec.encode(state)))
    }

    @Test fun shareTextMarksUnassignedItems() {
        val text = formatShareText(BringState("Пикник", listOf(BringItem("Лёд"))))
        assertTrue(text.contains("Лёд: свободно"))
    }
}
