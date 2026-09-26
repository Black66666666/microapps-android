package com.microapps.matchchoice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MatchLogicTest {
    @Test fun inviteRoundTrips() {
        val source = Invite("Куда идём?", listOf("Пицца", "Рамен"), listOf("Рамен"))
        assertEquals(source, InviteCodec.decode(InviteCodec.encode(source)))
    }

    @Test fun malformedInviteIsRejected() = assertNull(InviteCodec.decode("not-an-invite"))

    @Test fun matchingKeepsCreatorOrder() {
        assertEquals(listOf("Пицца", "Суши"), matchingChoices(listOf("Пицца", "Рамен", "Суши"), setOf("Суши", "Пицца")))
    }
}
