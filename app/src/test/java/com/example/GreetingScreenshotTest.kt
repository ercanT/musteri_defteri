package com.example

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.data.CustomerEntity
import com.example.data.CustomerStatus
import com.example.ui.CustomerCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        CustomerCard(
          customer = CustomerEntity(
            id = 1,
            fullName = "Ahmet Yılmaz",
            phoneNumber = "0532 123 45 67",
            address = "Bağdat Cad. No: 120 Kadıköy/İstanbul",
            extraNotes = "Özel tasarım sipariş teslimatı yapılacak.",
            status = CustomerStatus.PARASI_GELDI.name,
            amount = 1000.0,
            isFavorite = true
          ),
          onStatusChange = {},
          onToggleFavorite = {},
          onEditClick = {},
          onDeleteClick = {},
          modifier = Modifier.padding(16.dp)
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

