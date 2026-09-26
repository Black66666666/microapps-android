package com.microapps.buytomorrow

import org.junit.Assert.assertEquals
import org.junit.Test

class BuyTomorrowLogicTest {
    private val wishes = listOf(
        Wish("A", 10.0, 1L, 48, "waiting"),
        Wish("B", 25.5, 2L, 48, "skipped")
    )

    @Test fun wishesRoundTrip() = assertEquals(wishes, WishCodec.decode(WishCodec.encode(wishes)))
    @Test fun savedAmountCountsOnlySkipped() = assertEquals(25.5, savedAmount(wishes), 0.0001)
    @Test fun statusUpdateTargetsCorrectWish() = assertEquals("bought", updateWishStatus(wishes, 1L, "bought").first().status)
}
