package com.microapps.turnkeeper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.microapps.kit.NeutralAppTheme
import com.microapps.kit.TurnKeeperScreen
class MainActivity : ComponentActivity() { override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { NeutralAppTheme { TurnKeeperScreen() } } } }
