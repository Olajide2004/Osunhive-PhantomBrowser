package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.navigation.PhantomMainApp
import com.example.ui.theme.PhantomProfileTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val app = application as PhantomApplication

    setContent {
      val themePref by app.preferencesManager.themeFlow.collectAsState(initial = "DARK")
      val isDark = when (themePref) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
      }

      PhantomProfileTheme(darkTheme = isDark, dynamicColor = false) {
        PhantomMainApp()
      }
    }
  }
}
