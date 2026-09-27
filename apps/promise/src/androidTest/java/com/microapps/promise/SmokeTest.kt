package com.microapps.promise
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class SmokeTest { @Test fun launchesWithIcon() { ActivityScenario.launch(MainActivity::class.java).use { s -> s.onActivity { a -> assertFalse(a.isFinishing); assertNotEquals(0, a.packageManager.getApplicationInfo(a.packageName, 0).icon) } } } }
