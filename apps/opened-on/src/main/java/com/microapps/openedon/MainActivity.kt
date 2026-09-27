package com.microapps.openedon
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.microapps.kit.NeutralAppTheme
import com.microapps.kit.OpenedOnScreen
class MainActivity : ComponentActivity() { override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { NeutralAppTheme { OpenedOnScreen() } } } }
