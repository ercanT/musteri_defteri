package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ThemePreferences
import com.example.ui.BackupViewModel
import com.example.ui.CustomerViewModel
import com.example.ui.DebtViewModel
import com.example.ui.MainScreen
import com.example.ui.ReceivableViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val customerViewModel: CustomerViewModel by viewModels {
    CustomerViewModel.Factory(application)
  }

  private val debtViewModel: DebtViewModel by viewModels {
    DebtViewModel.Factory(application)
  }

  private val receivableViewModel: ReceivableViewModel by viewModels {
    ReceivableViewModel.Factory(application)
  }

  private val backupViewModel: BackupViewModel by viewModels {
    BackupViewModel.Factory(application)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val themePreferences = ThemePreferences(applicationContext)

    setContent {
      val isDarkMode by themePreferences.isDarkMode.collectAsStateWithLifecycle()

      // Dynamically configure Edge-to-Edge and status bar icon contrast based on user's theme selection:
      // Dark Mode: White/light system icons so they are crystal clear on dark background.
      // Light Mode: Black/dark system icons so they are sharp on light background.
      LaunchedEffect(isDarkMode) {
        if (isDarkMode) {
          enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
          )
          WindowCompat.getInsetsController(window, window.decorView)?.let { insetsController ->
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
          }
        } else {
          enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
          )
          WindowCompat.getInsetsController(window, window.decorView)?.let { insetsController ->
            insetsController.isAppearanceLightStatusBars = true
            insetsController.isAppearanceLightNavigationBars = true
          }
        }
      }

      MyApplicationTheme(darkTheme = isDarkMode) {
        MainScreen(
          customerViewModel = customerViewModel,
          debtViewModel = debtViewModel,
          receivableViewModel = receivableViewModel,
          backupViewModel = backupViewModel,
          isDarkMode = isDarkMode,
          onToggleDarkMode = { themePreferences.toggleDarkMode() }
        )
      }
    }
  }
}

