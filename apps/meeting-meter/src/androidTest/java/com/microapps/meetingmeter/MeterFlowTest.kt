package com.microapps.meetingmeter

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class MeterFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun timerCanStartAndStop() {
        rule.onNodeWithTag("start").performClick()
        rule.onNodeWithText("Остановить").assertExists()
        Thread.sleep(500)
        rule.onNodeWithTag("stop").performClick()
        rule.onNodeWithText("Продолжить").assertExists()
    }
}
