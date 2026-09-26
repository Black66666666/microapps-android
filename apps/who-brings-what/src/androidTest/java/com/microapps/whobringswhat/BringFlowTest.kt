package com.microapps.whobringswhat

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class BringFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun itemAndOwnerSurviveActivityRecreation() {
        rule.onNodeWithTag("item-input").performTextInput("Лёд")
        rule.onNodeWithTag("add-item").performClick()
        rule.onNodeWithTag("owner-0").performTextInput("Аня")
        rule.onNodeWithText("Лёд").assertExists()
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()
        rule.onNodeWithText("Лёд").assertExists()
        rule.onNodeWithText("Аня").assertExists()
    }
}
