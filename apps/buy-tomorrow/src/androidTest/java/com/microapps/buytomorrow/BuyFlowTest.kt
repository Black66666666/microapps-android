package com.microapps.buytomorrow

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class BuyFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun skippedPurchaseUpdatesSavingsAndPersists() {
        rule.onNodeWithTag("wish-name").performTextInput("Наушники")
        rule.onNodeWithTag("wish-price").performTextInput("100")
        rule.onNodeWithTag("add-wish").performClick()
        rule.onNodeWithText("Наушники").assertExists()
        rule.onNodeWithText("Передумал").performClick()
        rule.onNodeWithText("Не потрачено: 100.00").assertExists()
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()
        rule.onNodeWithText("Наушники").assertExists()
        rule.onNodeWithText("Не потрачено: 100.00").assertExists()
    }
}
