package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.LensFlowDatabase
import com.example.data.repository.ScanRepository
import com.example.data.repository.ScanRepositoryImpl
import com.example.domain.model.ActionItem
import com.example.domain.model.ScanRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ScanRepositoryTest {

    private lateinit var database: LensFlowDatabase
    private lateinit var repository: ScanRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, LensFlowDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ScanRepositoryImpl(database.scanDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveScan() = runBlocking {
        val item1 = ActionItem(title = "Approve invoice total $450.00", dateOrTime = "Friday", details = "Net 30 terms", category = "Finance")
        val scan = ScanRecord(
            id = "test-scan-1",
            title = "Vendor Invoice",
            type = "Invoice",
            timestamp = "10:30 AM, Aug 31",
            isOfflineProcessed = true,
            engineName = "Google ML Kit (On-Device OCR)",
            latencyMs = 38L,
            rawExtractedText = "Apex Cloud Services\nTotal: $450.00\nDue: Friday",
            items = listOf(item1)
        )

        repository.insertScan(scan)

        val retrieved = repository.getScanById("test-scan-1")
        assertNotNull(retrieved)
        assertEquals("Vendor Invoice", retrieved?.title)
        assertEquals("Invoice", retrieved?.type)
        assertEquals(1, retrieved?.items?.size)
        assertEquals("Approve invoice total $450.00", retrieved?.items?.first()?.title)

        val allScans = repository.getAllScans().first()
        assertEquals(1, allScans.size)
    }

    @Test
    fun testToggleActionItem() = runBlocking {
        val item1 = ActionItem(id = "item-123", title = "Follow up with client", dateOrTime = "Today", details = "Email sent", isChecked = false)
        val scan = ScanRecord(
            id = "scan-toggle",
            title = "Meeting Notes",
            type = "Notes",
            timestamp = "2:00 PM, Aug 31",
            items = listOf(item1)
        )

        repository.insertScan(scan)

        // Toggle to true
        repository.toggleActionItem("scan-toggle", "item-123")
        val updated = repository.getScanById("scan-toggle")
        assertTrue(updated?.items?.first()?.isChecked == true)

        // Toggle back to false
        repository.toggleActionItem("scan-toggle", "item-123")
        val toggledBack = repository.getScanById("scan-toggle")
        assertTrue(toggledBack?.items?.first()?.isChecked == false)
    }

    @Test
    fun testDeleteScan() = runBlocking {
        val scan = ScanRecord(
            id = "scan-del",
            title = "Temporary Note",
            type = "Notes",
            timestamp = "3:15 PM, Aug 31"
        )
        repository.insertScan(scan)
        assertNotNull(repository.getScanById("scan-del"))

        repository.deleteScanById("scan-del")
        assertNull(repository.getScanById("scan-del"))
    }
}
