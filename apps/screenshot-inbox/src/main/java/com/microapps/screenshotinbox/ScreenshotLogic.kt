package com.microapps.screenshotinbox

fun isScreenshot(name: String?, folder: String?): Boolean {
    return name.orEmpty().contains("screenshot", ignoreCase = true) ||
        folder.orEmpty().contains("screenshot", ignoreCase = true)
}
