package com.microapps.whereisit
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.microapps.kit.NeutralAppTheme
import com.microapps.kit.WhereIsItScreen
class MainActivity : ComponentActivity() { override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { NeutralAppTheme { WhereIsItScreen() } } } }
