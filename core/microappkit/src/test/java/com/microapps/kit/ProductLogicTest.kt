package com.microapps.kit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ProductLogicTest {
    @Test fun worthItCalculatesCostPerUse() {
        assertEquals(25.0, costPerUse(100.0, 4), 0.0001)
        assertEquals(100.0, costPerUse(100.0, 0), 0.0001)
    }

    @Test fun turnKeeperWrapsAround() {
        assertEquals(0, nextTurn(2, 3))
        assertEquals(2, nextTurn(1, 3))
    }

    @Test fun fiveMinutesOnlyPicksEligibleTask() {
        val tasks = listOf(TimedTask("1", "short", 5), TimedTask("2", "long", 30))
        assertEquals("short", pickTask(tasks, 10, 0)?.text)
        assertNull(pickTask(tasks, 2, 0))
    }

    @Test fun refillAndOpenedDatesAreDeterministic() {
        val day = LocalDate.of(2026, 9, 27)
        assertEquals(LocalDate.of(2026, 10, 27), nextRefillDate(day, 30))
        assertEquals(LocalDate.of(2026, 10, 4), openedExpiry(day, 7))
    }

    @Test fun fairPickUsesLeastPickedPool() {
        val people = listOf(FairPerson("A", 3), FairPerson("B", 1), FairPerson("C", 1))
        val index = chooseFair(people, 0)!!
        assertTrue(index == 1 || index == 2)
    }

    @Test fun boxQrLinkOpensOnlyValidBoxScheme() {
        assertEquals("abc-123", boxIdFromLink("boxqr://box/abc-123"))
        assertEquals("abc-123", boxIdFromLink("boxqr://box/abc-123?source=qr"))
        assertNull(boxIdFromLink("https://example.com/box/abc-123"))
        assertNull(boxIdFromLink("boxqr://box/a/b"))
    }
}
