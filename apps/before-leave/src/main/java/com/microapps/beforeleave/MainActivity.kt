package com.microapps.beforeleave
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.microapps.kit.BeforeLeaveScreen
import com.microapps.kit.NeutralAppTheme
class MainActivity : ComponentActivity() { override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { NeutralAppTheme { BeforeLeaveScreen() } } } }
