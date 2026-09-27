package com.microapps.boxqr
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
class SmokeTest { @get:Rule val rule = createAndroidComposeRule<MainActivity>(); @Test fun launchesWithIcon() { rule.onNodeWithText("BoxQR").assertExists(); val info = rule.activity.packageManager.getApplicationInfo(rule.activity.packageName, 0); assertNotEquals(0, info.icon) } }
