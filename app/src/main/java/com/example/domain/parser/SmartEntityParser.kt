package com.example.domain.parser

import com.example.domain.model.ActionItem
import java.util.UUID
import java.util.regex.Pattern

object SmartEntityParser {

    private val EMAIL_REGEX = Regex("""[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}""")
    private val PHONE_REGEX = Regex("""(?:\+?\d{1,3}[-.\s]?)?\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}""")
    private val CURRENCY_REGEX = Regex("""(?:[\$€£¥]\s*[\d,]+(?:\.\d{2})?|[\d,]+(?:\.\d{2})?\s*[\$€£¥])""")
    private val DATE_REGEX = Regex("""\b(?:\d{1,2}[/-]\d{1,2}(?:[/-]\d{2,4})?|(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\.?\s+\d{1,2}(?:st|nd|rd|th)?(?:\s*,?\s*\d{4})?|Today|Tomorrow|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)\b""", RegexOption.IGNORE_CASE)

    fun parseActions(
        rawText: String,
        mode: String,
        latencyMs: Long = 0L
    ): List<ActionItem> {
        val trimmed = rawText.trim()
        if (trimmed.isBlank()) {
            return listOf(
                ActionItem(
                    id = UUID.randomUUID().toString(),
                    title = "No text detected in document",
                    dateOrTime = "Rescan",
                    details = "Point the camera at clear, printed or handwritten text with good lighting.",
                    category = "Guide"
                )
            )
        }

        val lines = trimmed.lines().map { it.trim() }.filter { it.isNotBlank() }
        val items = mutableListOf<ActionItem>()

        // 1. Check for specific document type entities
        when (mode) {
            "Receipt" -> {
                val totalLine = lines.firstOrNull { it.contains("total", ignoreCase = true) || it.contains("amount", ignoreCase = true) || it.contains("due", ignoreCase = true) }
                val amountMatch = if (totalLine != null) CURRENCY_REGEX.find(totalLine) else CURRENCY_REGEX.find(rawText)
                val dateMatch = DATE_REGEX.find(rawText)?.value ?: "Today"

                if (amountMatch != null) {
                    val amountStr = amountMatch.value.replace(" ", "")
                    items.add(
                        ActionItem(
                            id = UUID.randomUUID().toString(),
                            title = "Settle Expense ($amountStr)",
                            dateOrTime = dateMatch,
                            details = totalLine ?: "Detected expense balance $amountStr.",
                            category = "Finance"
                        )
                    )
                }

                // Look for line items (lines with numbers or currency)
                val itemLines = lines.filter { it != totalLine && (CURRENCY_REGEX.containsMatchIn(it) || it.contains("x ", ignoreCase = true) || it.contains("•")) }
                itemLines.take(3).forEach { line ->
                    items.add(
                        ActionItem(
                            id = UUID.randomUUID().toString(),
                            title = line.take(45),
                            dateOrTime = dateMatch,
                            details = "Parsed line item from receipt.",
                            category = "Finance"
                        )
                    )
                }

                if (items.isEmpty() && lines.isNotEmpty()) {
                    items.add(
                        ActionItem(
                            id = UUID.randomUUID().toString(),
                            title = "Review Receipt (${lines.first().take(30)})",
                            dateOrTime = dateMatch,
                            details = "Parsed ${lines.size} lines from receipt.",
                            category = "Finance"
                        )
                    )
                }
            }

            "Business Card" -> {
                val email = EMAIL_REGEX.find(rawText)?.value
                val phone = PHONE_REGEX.find(rawText)?.value
                val nameCandidates = lines.filter { line ->
                    !line.contains("@") && !PHONE_REGEX.containsMatchIn(line) && line.length in 3..40
                }
                val primaryName = nameCandidates.firstOrNull() ?: "Scanned Contact"

                val contactDetails = listOfNotNull(
                    email?.let { "Email: $it" },
                    phone?.let { "Phone: $it" }
                ).joinToString(" • ").ifBlank {
                    nameCandidates.drop(1).firstOrNull() ?: "Parsed contact info"
                }

                items.add(
                    ActionItem(
                        id = UUID.randomUUID().toString(),
                        title = "Follow up with $primaryName",
                        dateOrTime = "Today",
                        details = contactDetails,
                        category = "Contact"
                    )
                )

                if (email != null || phone != null) {
                    items.add(
                        ActionItem(
                            id = UUID.randomUUID().toString(),
                            title = "Save contact details to address book",
                            dateOrTime = "Immediate",
                            details = listOfNotNull(email, phone).joinToString(", "),
                            category = "Contact"
                        )
                    )
                }
            }

            "Invoice" -> {
                val totalMatch = CURRENCY_REGEX.find(rawText)
                val dateMatch = DATE_REGEX.find(rawText)?.value ?: "Due Date"
                val invoiceNo = lines.firstOrNull { it.contains("inv", ignoreCase = true) || it.contains("invoice", ignoreCase = true) || it.contains("#") }

                if (totalMatch != null) {
                    val amountStr = totalMatch.value.replace(" ", "")
                    items.add(
                        ActionItem(
                            id = UUID.randomUUID().toString(),
                            title = "Approve Invoice ($amountStr)",
                            dateOrTime = dateMatch,
                            details = invoiceNo ?: "Invoice total amount: $amountStr",
                            category = "Finance"
                        )
                    )
                }

                // Add action line items
                lines.filter { it != invoiceNo && (it.contains("due", ignoreCase = true) || it.contains("action", ignoreCase = true) || it.contains("vendor", ignoreCase = true) || CURRENCY_REGEX.containsMatchIn(it)) }
                    .take(2)
                    .forEach { line ->
                        items.add(
                            ActionItem(
                                id = UUID.randomUUID().toString(),
                                title = line.take(45),
                                dateOrTime = dateMatch,
                                details = "Invoice line item record.",
                                category = "Finance"
                            )
                        )
                    }
            }

            else -> {
                // Whiteboard, Notes, and General Documents
                val bulletLines = lines.filter {
                    it.startsWith("•") || it.startsWith("-") || it.startsWith("*") ||
                    Regex("""^\d+[\.\)]\s+""").containsMatchIn(it) ||
                    it.startsWith("Task", ignoreCase = true) ||
                    it.startsWith("Milestone", ignoreCase = true) ||
                    it.startsWith("Action", ignoreCase = true) ||
                    it.startsWith("TODO", ignoreCase = true)
                }

                if (bulletLines.isNotEmpty()) {
                    bulletLines.take(5).forEachIndexed { idx, bl ->
                        val clean = bl.replace(Regex("""^[•\-\*\d\.\)\s]+"""), "").trim()
                        val lineDate = DATE_REGEX.find(bl)?.value ?: if (idx == 0) "Today" else "Upcoming"
                        items.add(
                            ActionItem(
                                id = UUID.randomUUID().toString(),
                                title = clean.ifBlank { "Action Item ${idx + 1}" }.take(50),
                                dateOrTime = lineDate,
                                details = "Extracted milestone from document.",
                                category = if (mode == "Whiteboard" || bl.contains("milestone", ignoreCase = true)) "Milestone" else "Task"
                            )
                        )
                    }
                } else {
                    // Fallback to meaningful lines
                    lines.take(4).forEachIndexed { idx, line ->
                        val lineDate = DATE_REGEX.find(line)?.value ?: "Today"
                        items.add(
                            ActionItem(
                                id = UUID.randomUUID().toString(),
                                title = line.take(50),
                                dateOrTime = lineDate,
                                details = "Extracted line from ${mode.lowercase()}.",
                                category = if (mode == "Whiteboard") "Milestone" else "Task"
                            )
                        )
                    }
                }
            }
        }

        // Guarantee at least one valid parsed item if lines existed
        if (items.isEmpty()) {
            items.add(
                ActionItem(
                    id = UUID.randomUUID().toString(),
                    title = lines.first().take(45),
                    dateOrTime = "Today",
                    details = "Parsed document text checklist.",
                    category = "Task"
                )
            )
        }

        return items
    }
}

