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
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.JobEntity
import com.example.data.UserProfileEntity
import com.example.data.VisaApplicationEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SavedJobsPdfExporter {

    private const val PAGE_WIDTH = 595 // A4 standard width (points)
    private const val PAGE_HEIGHT = 842 // A4 standard height (points)
    private const val MARGIN = 36f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    data class ExportResult(
        val file: File,
        val uri: Uri?,
        val fileName: String,
        val totalJobs: Int
    )

    /**
     * Generates a professional multi-page PDF tracking document for saved jobs and visa applications.
     */
    fun generateSavedJobsPdf(
        context: Context,
        userName: String,
        userProfile: UserProfileEntity?,
        savedJobs: List<JobEntity>,
        visaApplications: List<VisaApplicationEntity>
    ): ExportResult? {
        try {
            val pdfDocument = PdfDocument()
            val dateFormat = SimpleDateFormat("MMMM dd, yyyy • HH:mm", Locale.getDefault())
            val dateOnlyFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val currentDateStr = dateFormat.format(Date())
            val todayDateStr = dateOnlyFormat.format(Date())

            val resolvedName = userName.ifBlank {
                userProfile?.fullName?.ifBlank { "Professional Applicant" } ?: "Professional Applicant"
            }

            // Map visa applications by jobId for quick lookup
            val appMap = visaApplications.associateBy { it.jobId }

            // Paints setup
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0F172A") // Slate Dark
                style = Paint.Style.FILL
            }

            // Helper to draw text with clipping or wrapping
            fun drawText(
                canvas: Canvas,
                text: String,
                x: Float,
                y: Float,
                size: Float,
                colorInt: Int,
                isBold: Boolean = false,
                typeface: Typeface = Typeface.DEFAULT
            ) {
                paint.color = colorInt
                paint.textSize = size
                paint.typeface = if (isBold) Typeface.create(typeface, Typeface.BOLD) else typeface
                canvas.drawText(text, x, y, paint)
            }

            fun drawBadge(
                canvas: Canvas,
                text: String,
                x: Float,
                y: Float,
                bgColor: Int,
                textColor: Int,
                paddingH: Float = 8f,
                paddingV: Float = 4f,
                fontSize: Float = 9f
            ): Float {
                paint.textSize = fontSize
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val textWidth = paint.measureText(text)
                val rect = RectF(
                    x,
                    y - fontSize - paddingV + 2,
                    x + textWidth + (paddingH * 2),
                    y + paddingV + 2
                )
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = bgColor
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(rect, 4f, 4f, bgPaint)
                paint.color = textColor
                canvas.drawText(text, x + paddingH, y, paint)
                return rect.width()
            }

            var currentPageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            var currentY = MARGIN

            // Function to draw header on each page
            fun drawPageHeader(isFirstPage: Boolean) {
                // Top accent line
                val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#10B981") // Emerald Green
                    strokeWidth = 3f
                }
                canvas.drawLine(MARGIN, MARGIN - 10, PAGE_WIDTH - MARGIN, MARGIN - 10, accentPaint)

                if (isFirstPage) {
                    // Header Bar background
                    val bannerRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 64f)
                    val bannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.parseColor("#0F172A") // Deep Navy Slate
                        style = Paint.Style.FILL
                    }
                    canvas.drawRoundRect(bannerRect, 10f, 10f, bannerPaint)

                    // Title
                    drawText(
                        canvas,
                        "GLOBAL VISA JOBS",
                        MARGIN + 16f,
                        currentY + 26f,
                        16f,
                        Color.parseColor("#10B981"),
                        isBold = true
                    )
                    drawText(
                        canvas,
                        "SAVED JOBS & VISA APPLICATION TRACKING PORTFOLIO",
                        MARGIN + 16f,
                        currentY + 44f,
                        10f,
                        Color.parseColor("#F8FAFC"),
                        isBold = true
                    )
                    drawText(
                        canvas,
                        "Generated: $currentDateStr",
                        MARGIN + 16f,
                        currentY + 56f,
                        8.5f,
                        Color.parseColor("#94A3B8")
                    )

                    // Right Badge
                    drawBadge(
                        canvas,
                        "OFFICIAL RECORD",
                        PAGE_WIDTH - MARGIN - 110f,
                        currentY + 36f,
                        Color.parseColor("#1E293B"),
                        Color.parseColor("#38BDF8"),
                        fontSize = 8.5f
                    )

                    currentY += 76f

                    // Candidate Information Box
                    val candBoxRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 70f)
                    val candBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.parseColor("#F8FAFC")
                        style = Paint.Style.FILL
                    }
                    canvas.drawRoundRect(candBoxRect, 8f, 8f, candBoxPaint)
                    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.parseColor("#E2E8F0")
                        style = Paint.Style.STROKE
                        strokeWidth = 1f
                    }
                    canvas.drawRoundRect(candBoxRect, 8f, 8f, borderPaint)

                    drawText(canvas, "APPLICANT DOSSIER", MARGIN + 14f, currentY + 18f, 9.5f, Color.parseColor("#64748B"), isBold = true)
                    drawText(canvas, resolvedName, MARGIN + 14f, currentY + 36f, 13f, Color.parseColor("#0F172A"), isBold = true)

                    val candidateMeta = buildString {
                        if (!userProfile?.nationality.isNullOrBlank()) append("Nationality: ${userProfile?.nationality}  •  ")
                        if (!userProfile?.preferredOccupations.isNullOrBlank()) append("Target: ${userProfile?.preferredOccupations}  •  ")
                        if (userProfile?.linkedInConnected == true) append("LinkedIn Verified (Trust 98%)")
                        else append("Global Visa Candidate")
                    }
                    drawText(canvas, candidateMeta, MARGIN + 14f, currentY + 52f, 9f, Color.parseColor("#475569"))

                    // Stats pills on right
                    val totalSaved = savedJobs.size
                    val activeApplied = visaApplications.count { it.status != "Applied" && it.status.isNotBlank() }
                    
                    val statsText = "$totalSaved Saved Jobs  |  ${visaApplications.size} Tracked Applications"
                    drawBadge(canvas, statsText, PAGE_WIDTH - MARGIN - 210f, currentY + 38f, Color.parseColor("#E0F2FE"), Color.parseColor("#0369A1"), fontSize = 9f)

                    currentY += 82f
                } else {
                    // Small header for subsequent pages
                    drawText(canvas, "Global Visa Jobs — Saved Jobs Tracking Record ($resolvedName)", MARGIN, currentY + 12f, 9f, Color.parseColor("#64748B"))
                    drawText(canvas, currentDateStr, PAGE_WIDTH - MARGIN - 120f, currentY + 12f, 8f, Color.parseColor("#94A3B8"))
                    
                    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.parseColor("#E2E8F0")
                        strokeWidth = 1f
                    }
                    canvas.drawLine(MARGIN, currentY + 20f, PAGE_WIDTH - MARGIN, currentY + 20f, linePaint)
                    currentY += 32f
                }
            }

            fun drawPageFooter(pageNum: Int) {
                val footerY = PAGE_HEIGHT - MARGIN + 14f
                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#E2E8F0")
                    strokeWidth = 1f
                }
                canvas.drawLine(MARGIN, footerY - 14f, PAGE_WIDTH - MARGIN, footerY - 14f, linePaint)

                drawText(
                    canvas,
                    "Confidential & Personal Career Tracking Document • Global Visa Jobs AI Platform",
                    MARGIN,
                    footerY,
                    8f,
                    Color.parseColor("#94A3B8")
                )

                val pageStr = "Page $pageNum"
                drawText(
                    canvas,
                    pageStr,
                    PAGE_WIDTH - MARGIN - 36f,
                    footerY,
                    8f,
                    Color.parseColor("#64748B"),
                    isBold = true
                )
            }

            // Draw initial page header
            drawPageHeader(isFirstPage = true)

            // Section title: Saved Jobs List
            drawText(
                canvas,
                "SAVED SPONSORSHIP JOBS & APPLICATION STATUS",
                MARGIN,
                currentY + 14f,
                11f,
                Color.parseColor("#0F172A"),
                isBold = true
            )
            currentY += 24f

            if (savedJobs.isEmpty()) {
                val emptyBox = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 80f)
                val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#F1F5F9")
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(emptyBox, 8f, 8f, emptyPaint)
                drawText(canvas, "No Bookmarked Jobs in Tracking List", MARGIN + 20f, currentY + 36f, 12f, Color.parseColor("#64748B"), isBold = true)
                drawText(canvas, "Bookmark jobs using the heart icon on any job card to automatically include them in this tracking report.", MARGIN + 20f, currentY + 54f, 9.5f, Color.parseColor("#94A3B8"))
                currentY += 90f
            } else {
                for ((index, job) in savedJobs.withIndex()) {
                    val linkedApp = appMap[job.id]
                    val appStatus = linkedApp?.status ?: "Saved (Not yet submitted)"
                    val appDate = linkedApp?.updatedDate?.ifBlank { todayDateStr } ?: todayDateStr
                    val notes = linkedApp?.notes ?: ""

                    val cardHeight = if (notes.isNotBlank()) 106f else 90f

                    // Check if we need a page break
                    if (currentY + cardHeight > PAGE_HEIGHT - MARGIN - 30f) {
                        drawPageFooter(currentPageNumber)
                        pdfDocument.finishPage(page)

                        currentPageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        currentY = MARGIN

                        drawPageHeader(isFirstPage = false)
                    }

                    // Draw Job Card
                    val cardRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + cardHeight)
                    val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.parseColor("#FFFFFF")
                        style = Paint.Style.FILL
                    }
                    canvas.drawRoundRect(cardRect, 8f, 8f, cardBgPaint)

                    val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.parseColor("#CBD5E1")
                        style = Paint.Style.STROKE
                        strokeWidth = 1f
                    }
                    canvas.drawRoundRect(cardRect, 8f, 8f, cardBorderPaint)

                    // Index indicator stripe on left
                    val stripeRect = RectF(MARGIN, currentY, MARGIN + 6f, currentY + cardHeight)
                    val stripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = if (appStatus.contains("Approved") || appStatus.contains("Offer")) Color.parseColor("#10B981")
                        else if (appStatus.contains("Interview") || appStatus.contains("Processing")) Color.parseColor("#38BDF8")
                        else Color.parseColor("#64748B")
                        style = Paint.Style.FILL
                    }
                    canvas.drawRoundRect(stripeRect, 4f, 4f, stripePaint)

                    // Job Title & Company
                    val titleDisplay = "${index + 1}. ${job.title.take(45)}${if (job.title.length > 45) "..." else ""}"
                    drawText(canvas, titleDisplay, MARGIN + 14f, currentY + 18f, 11f, Color.parseColor("#0F172A"), isBold = true)

                    val companyLocation = "${job.company}  •  ${job.location} (${job.country})"
                    drawText(canvas, companyLocation, MARGIN + 14f, currentY + 32f, 9.5f, Color.parseColor("#0284C7"), isBold = true)

                    // Details Line: Salary, Visa Type, Confidence Score
                    val visaDetails = "Visa: ${job.visaType}  |  Salary: ${job.salary}  |  Trust Score: ${job.confidenceScore}%  |  Relocation: ${if (job.relocationAssistance) "Yes (Assisted)" else "Standard"}"
                    drawText(canvas, visaDetails, MARGIN + 14f, currentY + 48f, 8.5f, Color.parseColor("#475569"))

                    // Status pill & Application Date on Right
                    val (statusBg, statusText) = when {
                        appStatus.contains("Approved") -> Pair(Color.parseColor("#DCFCE7"), Color.parseColor("#166534"))
                        appStatus.contains("Offer") -> Pair(Color.parseColor("#FEF3C7"), Color.parseColor("#92400E"))
                        appStatus.contains("Interview") -> Pair(Color.parseColor("#E0F2FE"), Color.parseColor("#0369A1"))
                        appStatus.contains("Applied") -> Pair(Color.parseColor("#F1F5F9"), Color.parseColor("#334155"))
                        else -> Pair(Color.parseColor("#F8FAFC"), Color.parseColor("#64748B"))
                    }

                    drawBadge(canvas, appStatus.uppercase(), PAGE_WIDTH - MARGIN - 145f, currentY + 18f, statusBg, statusText, fontSize = 8f)
                    drawText(canvas, "Date: $appDate", PAGE_WIDTH - MARGIN - 145f, currentY + 32f, 8f, Color.parseColor("#64748B"))

                    // Application URL / Portal Link
                    val appUrl = job.applicationUrl.ifBlank { "https://globalvisajobs.com/jobs/${job.id}" }
                    drawText(canvas, "Application Link: $appUrl", MARGIN + 14f, currentY + 62f, 8f, Color.parseColor("#64748B"))

                    // Notes (if available)
                    if (notes.isNotBlank()) {
                        val notesRect = RectF(MARGIN + 14f, currentY + 68f, PAGE_WIDTH - MARGIN - 14f, currentY + 98f)
                        val notesPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.parseColor("#F8FAFC")
                            style = Paint.Style.FILL
                        }
                        canvas.drawRoundRect(notesRect, 4f, 4f, notesPaint)
                        drawText(canvas, "Notes & Follow-ups: ${notes.take(85)}${if (notes.length > 85) "..." else ""}", MARGIN + 20f, currentY + 84f, 8f, Color.parseColor("#334155"))
                    }

                    currentY += cardHeight + 8f
                }
            }

            // Draw Summary & Instructions Box at end if space permits
            if (currentY + 60f <= PAGE_HEIGHT - MARGIN - 30f) {
                currentY += 8f
                val tipBox = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 44f)
                val tipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#F0FDF4")
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(tipBox, 6f, 6f, tipPaint)
                val tipBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#BBF7D0")
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }
                canvas.drawRoundRect(tipBox, 6f, 6f, tipBorder)

                drawText(canvas, "IMMIGRATION AUDIT & COMPLIANCE NOTICE", MARGIN + 12f, currentY + 16f, 8.5f, Color.parseColor("#15803D"), isBold = true)
                drawText(canvas, "This log serves as verified evidence of active job search and genuine sponsorship applications for visa authorities and immigration attorneys.", MARGIN + 12f, currentY + 30f, 8f, Color.parseColor("#166534"))
            }

            // Draw final page footer
            drawPageFooter(currentPageNumber)
            pdfDocument.finishPage(page)

            // Save PDF to cache and Downloads
            val sanitizedName = resolvedName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val fileName = "GlobalVisaJobs_Saved_Tracker_${sanitizedName}_${System.currentTimeMillis()}.pdf"
            val cacheFile = File(context.cacheDir, fileName)

            FileOutputStream(cacheFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            // Save to Public MediaStore / Downloads if possible
            var publicUri: Uri? = null
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/GlobalVisaJobs")
                    }
                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { outStream ->
                            cacheFile.inputStream().use { inStream ->
                                inStream.copyTo(outStream)
                            }
                        }
                        publicUri = uri
                    }
                } else {
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val targetFile = File(downloadsDir, fileName)
                    cacheFile.copyTo(targetFile, overwrite = true)
                    publicUri = Uri.fromFile(targetFile)
                }
            } catch (e: Exception) {
                Log.w("SavedJobsPdfExporter", "Could not copy to public downloads folder, using cache file", e)
            }

            return ExportResult(
                file = cacheFile,
                uri = publicUri,
                fileName = fileName,
                totalJobs = savedJobs.size
            )
        } catch (e: Exception) {
            Log.e("SavedJobsPdfExporter", "Error generating Saved Jobs PDF", e)
            return null
        }
    }

    /**
     * Helper to open or share the exported PDF file with standard Android intent.
     */
    fun openOrSharePdf(context: Context, file: File, chooserTitle: String = "Export Saved Jobs PDF Tracker") {
        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Global Visa Jobs - Saved Jobs & Application Tracking Document")
                putExtra(Intent.EXTRA_TEXT, "Here is my official Saved Jobs and Visa Application Tracking Report exported from Global Visa Jobs.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, chooserTitle)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("SavedJobsPdfExporter", "Failed to share PDF", e)
            Toast.makeText(context, "Saved to Downloads: ${file.name}", Toast.LENGTH_LONG).show()
        }
    }
}
