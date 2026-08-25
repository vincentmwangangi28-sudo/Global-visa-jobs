package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * High-performance client-side PDF Generation Engine for the AI ATS Resume Optimizer.
 * Renders structured and AI-tailored resumes into downloadable, printable, and ATS-compliant
 * PDF files across multiple customizable professional templates.
 */
object AtsResumePdfGenerator {

    private const val TAG = "AtsResumePdfGenerator"
    private const val PAGE_WIDTH = 595 // Standard A4 width in points (72 DPI)
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points (72 DPI)
    private const val MARGIN_LEFT = 40f
    private const val MARGIN_RIGHT = 40f
    private const val MARGIN_TOP = 40f
    private const val MARGIN_BOTTOM = 44f
    private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT // 515 points

    data class PdfExportResult(
        val file: File,
        val uri: Uri?,
        val fileName: String,
        val pageCount: Int
    )

    enum class ResumePdfTemplate(val displayName: String, val primaryColorHex: String, val accentColorHex: String) {
        ATS_STANDARD("ATS Standard", "#111827", "#374151"),
        MODERN_EXECUTIVE("Modern Executive", "#0F172A", "#2563EB"),
        CREATIVE_TECH("Creative Tech", "#064E3B", "#059669"),
        EUROPASS_EU("Europass / DIN", "#1E3A8A", "#2563EB")
    }

    /**
     * Generates a clean, professional PDF file client-side and saves a local copy in the app cache.
     */
    fun generateResumePdf(
        context: Context,
        candidateName: String,
        markdownContent: String,
        templateName: String = "ATS Standard"
    ): PdfExportResult? {
        return try {
            val pdfDoc = buildPdfDocument(candidateName, markdownContent, templateName)
            val cleanName = candidateName.ifBlank { "Candidate" }.replace(Regex("[^a-zA-Z0-9_]"), "_")
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val fileName = "${cleanName}_Tailored_Resume_$timestamp.pdf"

            // Save to application cache for sharing and quick viewing
            val cacheDir = File(context.cacheDir, "resumes")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val outputFile = File(cacheDir, fileName)

            FileOutputStream(outputFile).use { fos ->
                pdfDoc.writeTo(fos)
            }

            val fileUri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    outputFile
                )
            } catch (e: Exception) {
                Log.w(TAG, "FileProvider URI generation fallback", e)
                Uri.fromFile(outputFile)
            }

            // Also attempt to save to system MediaStore Downloads folder
            val mediaStoreUri = savePdfToDownloads(context, fileName, pdfDoc)

            val pageCount = pdfDoc.pages.size.coerceAtLeast(1)
            pdfDoc.close()

            PdfExportResult(
                file = outputFile,
                uri = mediaStoreUri ?: fileUri,
                fileName = fileName,
                pageCount = pageCount
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating Resume PDF", e)
            null
        }
    }

    /**
     * Builds the PdfDocument in-memory and returns raw byte array.
     */
    fun generatePdfBytes(
        candidateName: String,
        markdownContent: String,
        templateName: String = "ATS Standard"
    ): ByteArray {
        val pdfDoc = buildPdfDocument(candidateName, markdownContent, templateName)
        val baos = ByteArrayOutputStream()
        pdfDoc.writeTo(baos)
        pdfDoc.close()
        return baos.toByteArray()
    }

    /**
     * Writes generated PDF directly to an OutputStream (e.g. from Storage Access Framework CreateDocument launcher).
     */
    fun writeResumeToStream(
        candidateName: String,
        markdownContent: String,
        templateName: String,
        outputStream: OutputStream
    ): Boolean {
        return try {
            val pdfDoc = buildPdfDocument(candidateName, markdownContent, templateName)
            pdfDoc.writeTo(outputStream)
            pdfDoc.close()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed writing PDF to output stream", e)
            false
        }
    }

    /**
     * Saves PDF document directly to the Android MediaStore Downloads collection.
     */
    fun savePdfToDownloads(context: Context, fileName: String, pdfDocument: PdfDocument): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        return try {
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let { destUri ->
                resolver.openOutputStream(destUri)?.use { out ->
                    pdfDocument.writeTo(out)
                }
            }
            uri
        } catch (e: Exception) {
            Log.e(TAG, "Failed saving PDF to MediaStore", e)
            null
        }
    }

    /**
     * Helper to create an Intent for sharing the generated PDF file.
     */
    fun createShareIntent(context: Context, file: File, candidateName: String): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "${candidateName.ifBlank { "Applicant" }} - Tailored ATS Resume (PDF)")
            putExtra(Intent.EXTRA_TEXT, "Attached is the tailored ATS-optimized resume for $candidateName.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Core layout and rendering engine.
     */
    private fun buildPdfDocument(
        candidateName: String,
        markdownContent: String,
        templateName: String
    ): PdfDocument {
        val pdfDoc = PdfDocument()

        val selectedTemplate = when (templateName) {
            "Modern Executive" -> ResumePdfTemplate.MODERN_EXECUTIVE
            "Creative Tech" -> ResumePdfTemplate.CREATIVE_TECH
            "Europass / DIN" -> ResumePdfTemplate.EUROPASS_EU
            else -> ResumePdfTemplate.ATS_STANDARD
        }

        val primaryColor = Color.parseColor(selectedTemplate.primaryColorHex)
        val accentColor = Color.parseColor(selectedTemplate.accentColorHex)
        val bodyColor = Color.parseColor("#334155")
        val mutedColor = Color.parseColor("#64748B")
        val dividerColor = Color.parseColor("#CBD5E1")

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bodyColor
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryColor
            textSize = 19f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val sectionHeadingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryColor
            textSize = 11.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.05f
        }

        val subHeadingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val contactPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = mutedColor
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            strokeWidth = 1.2f
            style = Paint.Style.STROKE
        }

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDoc.startPage(pageInfo)
        var canvas = page.canvas
        var currentY = MARGIN_TOP

        fun startNewPage() {
            // Draw page footer on current page before closing
            drawPageFooter(canvas, pageNumber, selectedTemplate, primaryColor, mutedColor)
            pdfDoc.finishPage(page)

            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            page = pdfDoc.startPage(pageInfo)
            canvas = page.canvas
            currentY = MARGIN_TOP

            // Draw top accent bar on subsequent pages if Modern Executive
            if (selectedTemplate == ResumePdfTemplate.MODERN_EXECUTIVE) {
                bgPaint.color = primaryColor
                canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 6f, bgPaint)
                currentY += 10f
            }
        }

        fun ensureSpace(neededHeight: Float) {
            if (currentY + neededHeight > PAGE_HEIGHT - MARGIN_BOTTOM) {
                startNewPage()
            }
        }

        // 1. Template Top Header Banner
        when (selectedTemplate) {
            ResumePdfTemplate.MODERN_EXECUTIVE -> {
                // Top full-width banner
                bgPaint.color = Color.parseColor("#0F172A")
                canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 8f, bgPaint)
                bgPaint.color = accentColor
                canvas.drawRect(0f, 8f, PAGE_WIDTH.toFloat(), 11f, bgPaint)
                currentY = MARGIN_TOP + 5f
            }
            ResumePdfTemplate.CREATIVE_TECH -> {
                // Subtle left border accent
                bgPaint.color = primaryColor
                canvas.drawRect(0f, 0f, 6f, PAGE_HEIGHT.toFloat(), bgPaint)
            }
            else -> {}
        }

        // Parse markdown content line by line
        val rawLines = markdownContent.split("\n")
        var isFirstHeading = true

        var index = 0
        while (index < rawLines.size) {
            val line = rawLines[index]
            val trimmed = line.trim()

            if (trimmed.isEmpty()) {
                currentY += 4f
                index++
                continue
            }

            when {
                // 1. Title / Candidate Name (# Header)
                trimmed.startsWith("# ") -> {
                    val titleText = trimmed.substring(2).replace("**", "").replace("*", "").trim()
                    ensureSpace(40f)

                    if (selectedTemplate == ResumePdfTemplate.ATS_STANDARD) {
                        // Centered name for traditional ATS
                        val nameWidth = namePaint.measureText(titleText)
                        val startX = (PAGE_WIDTH - nameWidth) / 2f
                        canvas.drawText(titleText, startX, currentY + 16f, namePaint)
                    } else {
                        // Left-aligned with subtle bold accent
                        canvas.drawText(titleText, MARGIN_LEFT, currentY + 16f, namePaint)
                    }
                    currentY += 24f
                    isFirstHeading = false
                }

                // 2. Section Heading (## Section)
                trimmed.startsWith("## ") -> {
                    val sectionText = trimmed.substring(3).replace("**", "").replace("*", "").trim().uppercase()
                    ensureSpace(34f)
                    currentY += 10f

                    when (selectedTemplate) {
                        ResumePdfTemplate.MODERN_EXECUTIVE -> {
                            // Section with left pill indicator
                            bgPaint.color = accentColor
                            val pillRect = RectF(MARGIN_LEFT, currentY, MARGIN_LEFT + 4f, currentY + 13f)
                            canvas.drawRoundRect(pillRect, 2f, 2f, bgPaint)

                            canvas.drawText(sectionText, MARGIN_LEFT + 10f, currentY + 11f, sectionHeadingPaint)
                            linePaint.color = dividerColor
                            linePaint.strokeWidth = 0.8f
                            canvas.drawLine(MARGIN_LEFT + 10f + sectionHeadingPaint.measureText(sectionText) + 8f, currentY + 7f, PAGE_WIDTH - MARGIN_RIGHT, currentY + 7f, linePaint)
                            currentY += 18f
                        }
                        ResumePdfTemplate.CREATIVE_TECH -> {
                            // Emerald section heading with underline
                            canvas.drawText(sectionText, MARGIN_LEFT, currentY + 11f, sectionHeadingPaint)
                            linePaint.color = accentColor
                            linePaint.strokeWidth = 1.5f
                            val textW = sectionHeadingPaint.measureText(sectionText)
                            canvas.drawLine(MARGIN_LEFT, currentY + 15f, MARGIN_LEFT + textW + 12f, currentY + 15f, linePaint)
                            currentY += 20f
                        }
                        ResumePdfTemplate.EUROPASS_EU -> {
                            // Europass blue heading
                            sectionHeadingPaint.color = primaryColor
                            canvas.drawText(sectionText, MARGIN_LEFT, currentY + 11f, sectionHeadingPaint)
                            linePaint.color = accentColor
                            linePaint.strokeWidth = 1.0f
                            canvas.drawLine(MARGIN_LEFT, currentY + 15f, PAGE_WIDTH - MARGIN_RIGHT, currentY + 15f, linePaint)
                            currentY += 20f
                        }
                        else -> {
                            // ATS Standard clean divider
                            canvas.drawText(sectionText, MARGIN_LEFT, currentY + 11f, sectionHeadingPaint)
                            linePaint.color = Color.parseColor("#111827")
                            linePaint.strokeWidth = 1.0f
                            canvas.drawLine(MARGIN_LEFT, currentY + 15f, PAGE_WIDTH - MARGIN_RIGHT, currentY + 15f, linePaint)
                            currentY += 20f
                        }
                    }
                }

                // 3. Sub-section / Job Title (### Subtitle)
                trimmed.startsWith("### ") -> {
                    val subText = trimmed.substring(4).replace("**", "").replace("*", "").trim()
                    ensureSpace(20f)
                    currentY += 4f
                    canvas.drawText(subText, MARGIN_LEFT, currentY + 10f, subHeadingPaint)
                    currentY += 14f
                }

                // 4. Bullet item (- or * or •)
                trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ") -> {
                    val bulletText = when {
                        trimmed.startsWith("- ") -> trimmed.substring(2)
                        trimmed.startsWith("* ") -> trimmed.substring(2)
                        trimmed.startsWith("• ") -> trimmed.substring(2)
                        else -> trimmed
                    }.replace("**", "").replace("*", "").trim()

                    val indent = MARGIN_LEFT + 14f
                    val bulletWidth = CONTENT_WIDTH - 14f
                    val wrappedLines = wrapText(bulletText, textPaint, bulletWidth)

                    ensureSpace(wrappedLines.size * 13f + 4f)

                    // Draw bullet point
                    val bulletCenterY = currentY + 7f
                    bgPaint.color = if (selectedTemplate == ResumePdfTemplate.CREATIVE_TECH) accentColor else primaryColor
                    canvas.drawCircle(MARGIN_LEFT + 5f, bulletCenterY, 2f, bgPaint)

                    // Draw wrapped text
                    for (wLine in wrappedLines) {
                        canvas.drawText(wLine, indent, currentY + 10f, textPaint)
                        currentY += 13f
                    }
                }

                // 5. Contact information bar or standard paragraph
                else -> {
                    val cleanText = trimmed.replace("**", "").replace("*", "").trim()

                    // Check if this is a contact details line (e.g. Email | Phone | Location)
                    val isContactLine = cleanText.contains("@") || cleanText.contains("Email") || cleanText.contains("Phone") || cleanText.contains("Location") || cleanText.contains("LinkedIn")

                    if (isContactLine) {
                        ensureSpace(18f)
                        if (selectedTemplate == ResumePdfTemplate.ATS_STANDARD) {
                            val contactW = contactPaint.measureText(cleanText)
                            val startX = ((PAGE_WIDTH - contactW) / 2f).coerceAtLeast(MARGIN_LEFT)
                            canvas.drawText(cleanText, startX, currentY + 10f, contactPaint)
                        } else {
                            canvas.drawText(cleanText, MARGIN_LEFT, currentY + 10f, contactPaint)
                        }
                        currentY += 16f
                    } else {
                        // Regular wrapped text paragraph
                        val wrappedLines = wrapText(cleanText, textPaint, CONTENT_WIDTH)
                        ensureSpace(wrappedLines.size * 13f + 4f)

                        for (wLine in wrappedLines) {
                            canvas.drawText(wLine, MARGIN_LEFT, currentY + 10f, textPaint)
                            currentY += 13f
                        }
                    }
                }
            }
            index++
        }

        // Draw footer on the last page
        drawPageFooter(canvas, pageNumber, selectedTemplate, primaryColor, mutedColor)
        pdfDoc.finishPage(page)

        return pdfDoc
    }

    /**
     * Draws standard page number footer and ATS verification stamp.
     */
    private fun drawPageFooter(
        canvas: Canvas,
        pageNumber: Int,
        template: ResumePdfTemplate,
        primaryColor: Int,
        mutedColor: Int
    ) {
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = mutedColor
            textSize = 7.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val footerY = PAGE_HEIGHT - 20f

        // Subtle divider above footer
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 0.6f
        }
        canvas.drawLine(MARGIN_LEFT, footerY - 8f, PAGE_WIDTH - MARGIN_RIGHT, footerY - 8f, linePaint)

        // Left text: VisaGo AI ATS Optimized Resume
        canvas.drawText("Generated via VisaGo AI • ATS Optimized Format", MARGIN_LEFT, footerY, footerPaint)

        // Right text: Page X
        val pageText = "Page $pageNumber"
        val pageTextWidth = footerPaint.measureText(pageText)
        canvas.drawText(pageText, PAGE_WIDTH - MARGIN_RIGHT - pageTextWidth, footerY, footerPaint)
    }

    /**
     * Text wrapping utility to split long strings into lines that fit within maxWidth points.
     */
    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isBlank()) return emptyList()
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val candidate = if (currentLine.isEmpty()) word else "${currentLine} $word"
            if (paint.measureText(candidate) <= maxWidth) {
                currentLine.append(if (currentLine.isEmpty()) word else " $word")
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                }
                // Handle exceptionally long single words that exceed maxWidth
                if (paint.measureText(word) > maxWidth) {
                    var remainingWord = word
                    while (paint.measureText(remainingWord) > maxWidth && remainingWord.isNotEmpty()) {
                        var cutLen = remainingWord.length - 1
                        while (cutLen > 0 && paint.measureText(remainingWord.substring(0, cutLen)) > maxWidth) {
                            cutLen--
                        }
                        if (cutLen > 0) {
                            lines.add(remainingWord.substring(0, cutLen))
                            remainingWord = remainingWord.substring(cutLen)
                        } else {
                            lines.add(remainingWord.take(1))
                            remainingWord = remainingWord.drop(1)
                        }
                    }
                    currentLine = StringBuilder(remainingWord)
                } else {
                    currentLine = StringBuilder(word)
                }
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }

        return lines
    }
}
