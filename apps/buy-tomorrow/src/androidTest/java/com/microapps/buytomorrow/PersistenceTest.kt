package com.microapps.buytomorrow

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistenceTest {
    @Test fun encodedWishesCanBeStoredAndRestoredFromSharedPreferences() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("test-data", Context.MODE_PRIVATE)
        val wishes = listOf(Wish("Наушники", 99.0, 42L, 48))
        prefs.edit().putString("wishes", WishCodec.encode(wishes)).commit()
        assertEquals(wishes, WishCodec.decode(prefs.getString("wishes", "").orEmpty()))
        prefs.edit().clear().commit()
    }
}
