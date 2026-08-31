package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.parser.SmartEntityParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("LensFlow", appName)
  }

  @Test
  fun `test receipt action extraction`() {
    val text = "STARBUCKS COFFEE\nTotal: $14.50\nDate: 10/24/2026\nItem: Caramel Macchiato"
    val actions = SmartEntityParser.parseActions(text, "Receipt", 35L)
    assertTrue("Should extract at least one action item", actions.isNotEmpty())
    val first = actions.first()
    assertEquals("Finance", first.category)
    assertTrue(first.title.contains("Receipt") || first.title.contains("14.50") || first.title.contains("Expense"))
  }

  @Test
  fun `test whiteboard milestone extraction`() {
    val text = "Sprint Goals:\n• Launch OCR v2 by Friday\n• Verify PC link"
    val actions = SmartEntityParser.parseActions(text, "Whiteboard", 40L)
    assertTrue("Should extract milestone actions", actions.isNotEmpty())
    assertEquals("Milestone", actions.first().category)
  }

  @Test
  fun `test business card contact extraction`() {
    val text = "Alex Rivera\nVP Engineering, Horizon AI\nalex@horizon.ai\n+1-555-0199"
    val actions = SmartEntityParser.parseActions(text, "Business Card", 25L)
    assertTrue("Should extract contact actions", actions.isNotEmpty())
    assertEquals("Contact", actions.first().category)
  }
}
