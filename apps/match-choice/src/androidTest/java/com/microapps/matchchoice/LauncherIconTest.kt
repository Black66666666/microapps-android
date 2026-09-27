package com.microapps.matchchoice

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LauncherIconTest {
    @Test fun launcherIconIsConfigured() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val info = context.packageManager.getApplicationInfo(context.packageName, 0)
        assertNotEquals(0, info.icon)
    }
}
