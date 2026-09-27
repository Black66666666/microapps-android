package com.microapps.boxqr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.microapps.kit.BoxQrDeepLinkScreen
import com.microapps.kit.NeutralAppTheme
import com.microapps.kit.boxIdFromUri

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val boxId = boxIdFromUri(intent?.data)
        setContent { NeutralAppTheme { BoxQrDeepLinkScreen(boxId) } }
    }
}
