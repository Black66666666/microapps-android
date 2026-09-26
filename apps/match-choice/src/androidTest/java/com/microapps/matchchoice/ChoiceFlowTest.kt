package com.microapps.matchchoice

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class ChoiceFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun guestCanOpenInviteAndSeeMatch() {
        val code = InviteCodec.encode(Invite("Что едим?", listOf("Пицца", "Рамен"), listOf("Пицца")))
        rule.onNodeWithTag("invite-code").performTextInput(code)
        rule.onNodeWithTag("open-invite").performClick()
        rule.onNodeWithTag("guest-Пицца").performClick()
        rule.onNodeWithTag("show-matches").performClick()
        rule.onNodeWithText("Совпало: Пицца").assertExists()
    }
}
