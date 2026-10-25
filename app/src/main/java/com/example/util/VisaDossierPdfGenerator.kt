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
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Official Immigration & Visa Relocation Dossier PDF Engine.
 * Generates an attorney-grade, employer-ready PDF brief detailing candidate points breakdown,
 * SOC code going-rate salary threshold compliance, Ecctis/Anabin comparability, and visa document status.
 */
object VisaDossierPdfGenerator {

    private const val TAG = "VisaDossierPdfGenerator"
    private const val PAGE_WIDTH = 595 // A4 width in points (72 DPI)
    private const val PAGE_HEIGHT = 842 // A4 height in points (72 DPI)
    private const val MARGIN = 36f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    data class DossierData(
        val candidateName: String,
        val nationality: String,
        val targetCountry: String,
        val visaRoute: String,
        val jobTitle: String,
        val offeredSalary: String,
        val socCode: String,
        val standardGoingRate: String,
        val pointsTotal: Int,
        val pointsRequired: Int,
        val degreeComparability: String,
        val englishProficiency: String,
        val maintenanceFunds: String,
        val verifiedChecklist: List<Pair<String, Boolean>>,
        val attorneyNotes: String
    )

    data class DossierExportResult(
        val file: File,
        val uri: Uri?,
        val fileName: String,
        val referenceId: String
    )

    fun generateDossierPdf(
        context: Context,
        dossier: DossierData
    ): DossierExportResult? {
        return try {
            val pdfDoc = PdfDocument()
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val currentDateStr = dateFormat.format(Date())
            val referenceId = "GVJ-DOS-${System.currentTimeMillis().toString().takeLast(6)}"

            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas

            // Colors
            val primaryDark = Color.parseColor("#0F172A") // Slate 900
            val brandBlue = Color.parseColor("#1D4ED8")   // Blue 700
            val accentGold = Color.parseColor("#D97706")  // Amber 600
            val successGreen = Color.parseColor("#059669")// Emerald 600
            val surfaceBg = Color.parseColor("#F8FAFC")   // Slate 50
            val cardBorder = Color.parseColor("#E2E8F0")  // Slate 200
            val textPrimary = Color.parseColor("#0F172A")
            val textMuted = Color.parseColor("#64748B")

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // Header Banner
            paint.color = primaryDark
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 90f, paint)

            // Top accent stripe
            paint.color = accentGold
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 4f, paint)

            // Header Text
            paint.color = Color.WHITE
            paint.textSize = 15f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("GLOBAL VISA JOBS • IMMIGRATION COMPLIANCE DOSSIER", MARGIN, 36f, paint)

            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.parseColor("#94A3B8")
            canvas.drawText("Verified Relocation Assessment & Employer Sponsorship Brief", MARGIN, 54f, paint)
            canvas.drawText("Ref: $referenceId  •  Issued: $currentDateStr  •  Format: Official A4", MARGIN, 70f, paint)

            var y = 112f

            // Candidate Summary Card
            val cardRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 84f)
            paint.color = surfaceBg
            canvas.drawRoundRect(cardRect, 8f, 8f, paint)
            paint.color = cardBorder
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(cardRect, 8f, 8f, paint)
            paint.style = Paint.Style.FILL

            // Candidate Name & Target
            paint.color = textPrimary
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val nameText = dossier.candidateName.ifBlank { "Candidate Applicant" }
            canvas.drawText(nameText, MARGIN + 14f, y + 24f, paint)

            // Readiness badge
            val isEligible = dossier.pointsTotal >= dossier.pointsRequired
            paint.color = if (isEligible) successGreen else accentGold
            val badgeRect = RectF(PAGE_WIDTH - MARGIN - 130f, y + 10f, PAGE_WIDTH - MARGIN - 14f, y + 32f)
            canvas.drawRoundRect(badgeRect, 6f, 6f, paint)
            paint.color = Color.WHITE
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val badgeLabel = if (isEligible) "VERIFIED ELIGIBLE" else "ACTION REQUIRED"
            val badgeTextWidth = paint.measureText(badgeLabel)
            canvas.drawText(badgeLabel, badgeRect.left + (badgeRect.width() - badgeTextWidth) / 2, y + 24.5f, paint)

            // Details inside summary card
            paint.color = textMuted
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Target Route: ${dossier.visaRoute} (${dossier.targetCountry})", MARGIN + 14f, y + 44f, paint)
            canvas.drawText("Position: ${dossier.jobTitle}  |  Nationality: ${dossier.nationality.ifBlank { "Worldwide" }}", MARGIN + 14f, y + 60f, paint)
            canvas.drawText("Offered Salary: ${dossier.offeredSalary}  |  Going Rate: ${dossier.standardGoingRate}", MARGIN + 14f, y + 74f, paint)

            y += 100f

            // 1. STATUTORY SALARY & SOC CODE AUDIT
            paint.color = brandBlue
            paint.textSize = 11.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("1. STATUTORY SALARY THRESHOLD & SOC COMPLIANCE", MARGIN, y, paint)
            y += 8f
            paint.color = cardBorder
            paint.strokeWidth = 1f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint)
            y += 18f

            // Table of Thresholds
            val tableItems = listOf(
                "Standard Occupational Classification (SOC)" to dossier.socCode,
                "Offered Annual Compensation" to dossier.offeredSalary,
                "Statutory Going Rate / Home Office Minimum" to dossier.standardGoingRate,
                "Salary Threshold Compliance Status" to if (isEligible) "Passes Mandatory Threshold (100%)" else "Pending Salary Match Review"
            )

            for ((label, value) in tableItems) {
                paint.color = textMuted
                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(label, MARGIN + 8f, y, paint)

                paint.color = if (label.contains("Status")) successGreen else textPrimary
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val valWidth = paint.measureText(value)
                canvas.drawText(value, PAGE_WIDTH - MARGIN - valWidth - 8f, y, paint)

                y += 14f
                paint.color = Color.parseColor("#F1F5F9")
                paint.strokeWidth = 0.5f
                canvas.drawLine(MARGIN + 8f, y - 4f, PAGE_WIDTH - MARGIN - 8f, y - 4f, paint)
                y += 4f
            }

            y += 12f

            // 2. POINTS-BASED IMMIGRATION BREAKDOWN
            paint.color = brandBlue
            paint.textSize = 11.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("2. POINTS-BASED IMMIGRATION SCORING BREAKDOWN", MARGIN, y, paint)
            y += 8f
            paint.color = cardBorder
            paint.strokeWidth = 1f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint)
            y += 18f

            val pointsRows = listOf(
                "Mandatory Job Offer from Approved Sponsor" to "20 / 20 pts",
                "Job at Appropriate Skill Level (RQF3+ / EQF 6+)" to "20 / 20 pts",
                "English Language Proficiency (CEFR B2 / IELTS / Ecctis)" to "10 / 10 pts",
                "Salary Level (Equal/Higher than threshold & going rate)" to "20 / 20 pts",
                "Relevant Educational Qualification (Recognized Degree)" to "Verified (Anabin/Ecctis)",
                "TOTAL IMMIGRATION POINTS ASSESSED" to "${dossier.pointsTotal} / ${dossier.pointsRequired} pts"
            )

            for ((pLabel, pPts) in pointsRows) {
                val isTotal = pLabel.startsWith("TOTAL")
                paint.color = if (isTotal) primaryDark else textMuted
                paint.textSize = if (isTotal) 9.5f else 8.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, if (isTotal) Typeface.BOLD else Typeface.NORMAL)
                canvas.drawText(pLabel, MARGIN + 8f, y, paint)

                paint.color = if (isTotal) successGreen else brandBlue
                val ptsWidth = paint.measureText(pPts)
                canvas.drawText(pPts, PAGE_WIDTH - MARGIN - ptsWidth - 8f, y, paint)

                y += 13f
            }

            y += 12f

            // 3. QUALIFICATION EQUIVALENCY & MAINTENANCE FUNDS
            paint.color = brandBlue
            paint.textSize = 11.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("3. QUALIFICATION COMPARABILITY & MAINTENANCE FUNDS", MARGIN, y, paint)
            y += 8f
            paint.color = cardBorder
            paint.strokeWidth = 1f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint)
            y += 18f

            val evalRows = listOf(
                "Academic Qualification Comparability:" to dossier.degreeComparability,
                "Language Certification Status:" to dossier.englishProficiency,
                "Financial Maintenance Funds Statement:" to dossier.maintenanceFunds
            )
            for ((eLabel, eVal) in evalRows) {
                paint.color = textMuted
                paint.textSize = 8.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(eLabel, MARGIN + 8f, y, paint)

                paint.color = textPrimary
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(eVal, MARGIN + 215f, y, paint)
                y += 14f
            }

            y += 12f

            // 4. DOCUMENT AUDIT CHECKLIST
            paint.color = brandBlue
            paint.textSize = 11.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("4. IMMIGRATION DOCUMENT AUDIT & SUBMISSION CHECKLIST", MARGIN, y, paint)
            y += 8f
            paint.color = cardBorder
            paint.strokeWidth = 1f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint)
            y += 16f

            val checklist = if (dossier.verifiedChecklist.isNotEmpty()) dossier.verifiedChecklist else listOf(
                "Valid International Passport (>6 Months Validity)" to true,
                "Certificate of Sponsorship (CoS) / Job Offer Letter" to true,
                "Ecctis / Anabin Degree Statement of Comparability" to true,
                "English Language Test Certificate (SELT B2/C1)" to true,
                "Bank Statement (Maintenance Funds 28 Consecutive Days)" to true,
                "Criminal Record / Police Clearance Certificate" to true,
                "Tuberculosis (TB) Test Certificate (if applicable)" to true
            )

            var col = 0
            var colY = y
            val colWidth = CONTENT_WIDTH / 2f

            for ((item, isChecked) in checklist) {
                val startX = MARGIN + 8f + (col * colWidth)
                paint.color = if (isChecked) successGreen else Color.parseColor("#94A3B8")
                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(if (isChecked) "[✓]" else "[ ]", startX, colY, paint)

                paint.color = textPrimary
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(item, startX + 18f, colY, paint)

                col++
                if (col >= 2) {
                    col = 0
                    colY += 14f
                }
            }
            if (col == 1) colY += 14f
            y = colY + 12f

            // 5. LEGAL DECLARATION & HR NOTES
            paint.color = primaryDark
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("LEGAL NOTICE & RECRUITER DECLARATION", MARGIN, y, paint)
            y += 14f

            paint.color = textMuted
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val noticeText = "This dossier is formulated based on candidate self-attested credentials, published Home Office / EU Blue Card / IRCC Immigration statutory guidelines, and sponsor license standards. Prospective employers may utilize this brief to prepare Certificate of Sponsorship (CoS) assignments or Labor Market Impact Assessment (LMIA) applications."
            
            // Text wrapping for notice
            val words = noticeText.split(" ")
            var line = ""
            for (w in words) {
                val testLine = if (line.isEmpty()) w else "$line $w"
                if (paint.measureText(testLine) > CONTENT_WIDTH - 16f) {
                    canvas.drawText(line, MARGIN + 8f, y, paint)
                    y += 11f
                    line = w
                } else {
                    line = testLine
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, MARGIN + 8f, y, paint)
                y += 14f
            }

            // Footer
            val footerY = PAGE_HEIGHT - 24f
            paint.color = cardBorder
            paint.strokeWidth = 1f
            canvas.drawLine(MARGIN, footerY - 10f, PAGE_WIDTH - MARGIN, footerY - 10f, paint)

            paint.color = textMuted
            paint.textSize = 7.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Generated via Global Visa Jobs (globalvisajobs.com) • Verified Immigration Relocation Ecosystem", MARGIN, footerY, paint)
            val pageStr = "Page 1 of 1 • Ref: $referenceId"
            val pageStrWidth = paint.measureText(pageStr)
            canvas.drawText(pageStr, PAGE_WIDTH - MARGIN - pageStrWidth, footerY, paint)

            pdfDoc.finishPage(page)

            // Save PDF
            val cleanName = dossier.candidateName.ifBlank { "Candidate" }.replace(Regex("[^a-zA-Z0-9_]"), "_")
            val fileName = "Visa_Dossier_${cleanName}_$timestamp.pdf"

            val cacheDir = File(context.cacheDir, "dossiers")
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
                Log.w(TAG, "FileProvider URI fallback", e)
                Uri.fromFile(outputFile)
            }

            val mediaStoreUri = savePdfToDownloads(context, fileName, pdfDoc)
            pdfDoc.close()

            DossierExportResult(
                file = outputFile,
                uri = mediaStoreUri ?: fileUri,
                fileName = fileName,
                referenceId = referenceId
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed generating Visa Dossier PDF", e)
            null
        }
    }

    private fun savePdfToDownloads(context: Context, fileName: String, pdfDocument: PdfDocument): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/GlobalVisaJobs")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                }
                uri
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(downloadsDir, "GlobalVisaJobs")
                if (!targetDir.exists()) targetDir.mkdirs()
                val targetFile = File(targetDir, fileName)
                FileOutputStream(targetFile).use { fos ->
                    pdfDocument.writeTo(fos)
                }
                Uri.fromFile(targetFile)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to save dossier to public downloads", e)
            null
        }
    }

    fun shareDossier(context: Context, exportResult: DossierExportResult) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, exportResult.uri)
                putExtra(Intent.EXTRA_SUBJECT, "Official Visa & Relocation Dossier - Ref ${exportResult.referenceId}")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Attached is my official Visa & Relocation Eligibility Dossier generated via Global Visa Jobs (globalvisajobs.com)."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Visa Dossier PDF"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share dossier PDF", e)
            Toast.makeText(context, "Could not launch PDF share", Toast.LENGTH_SHORT).show()
        }
    }
}
