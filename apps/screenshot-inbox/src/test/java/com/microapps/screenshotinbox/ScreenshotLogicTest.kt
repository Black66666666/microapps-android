package com.microapps.screenshotinbox

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenshotLogicTest {
    @Test fun detectsScreenshotByFileName() = assertTrue(isScreenshot("Screenshot_2026.png", "Pictures"))
    @Test fun detectsScreenshotByFolderIgnoringCase() = assertTrue(isScreenshot("IMG_001.png", "Pictures/SCREENSHOTS"))
    @Test fun ignoresOrdinaryImages() = assertFalse(isScreenshot("holiday.jpg", "DCIM/Camera"))
}
