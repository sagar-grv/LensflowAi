package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.domain.model.TelemetryState
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.LensFlowTheme
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
  fun home_screen_screenshot() {
    composeTestRule.setContent {
      LensFlowTheme {
        HomeScreen(
          scans = emptyList(),
          selectedFilter = "All",
          onFilterSelect = {},
          onScanClick = {},
          onQuickSampleClick = {},
          onRecordClick = {},
          onDeleteClick = {},
          isOffline = true,
          telemetry = TelemetryState()
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/home_screen.png")
  }
}
