package com.microapps.whobringswhat

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistenceTest {
    @Test fun encodedStateCanBeStoredAndRestoredFromSharedPreferences() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("test-data", Context.MODE_PRIVATE)
        val state = BringState("Поездка", listOf(BringItem("Вода", "Аня")))
        prefs.edit().putString("state", BringStateCodec.encode(state)).commit()
        assertEquals(state, BringStateCodec.decode(prefs.getString("state", "").orEmpty()))
        prefs.edit().clear().commit()
    }
}
