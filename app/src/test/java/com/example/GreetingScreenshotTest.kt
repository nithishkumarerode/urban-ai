package com.example

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.theme.GeoCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.urbancadastral.ui.components.CadastralMetricCard
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
  fun cadastral_metric_card_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme(darkTheme = true) {
        CadastralMetricCard(
          title = "Total Parcels",
          value = "482",
          subtitle = "Sector 4 Modernization",
          icon = Icons.Default.GridOn,
          accentColor = GeoCyan
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/cadastral_card.png")
  }
}
