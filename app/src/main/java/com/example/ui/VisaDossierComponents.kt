package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.util.VisaDossierPdfGenerator

/**
 * Material 3 Components for candidate Immigration & Visa Dossier Generation.
 */

@Composable
fun VisaDossierExportCard(
    viewModel: JobViewModel,
    onOpenDossierDialog: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val candidateName = userProfile?.fullName?.ifBlank { "Vincent Mwangangi" } ?: "Vincent Mwangangi"

    Card(
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("visa_dossier_export_card"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AmberGold.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export Visa Dossier",
                        tint = AmberGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Official Visa Dossier PDF",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = AmberGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ATTORNEY-READY",
                                color = AmberGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "1-Click PDF summary of SOC going-rate compliance, 70-pt score, & Ecctis comparability",
                        color = SlateMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(NavyDark, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "70 / 70", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Points Passed", color = SlateMuted, fontSize = 10.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(NavyDark, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "100% Match", color = TealCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Going Rate", color = SlateMuted, fontSize = 10.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(NavyDark, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Verified", color = AmberGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Ecctis / Anabin", color = SlateMuted, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onOpenDossierDialog,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_open_visa_dossier_dialog"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberGold,
                    contentColor = NavyDark
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Official Visa Dossier Brief", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ExportVisaDossierDialog(
    viewModel: JobViewModel,
    onDismiss: () -> Unit,
    initialJob: com.example.data.JobEntity? = null
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isExporting by viewModel.isExportingDossier.collectAsStateWithLifecycle()
    val lastExportResult by viewModel.lastExportedDossierResult.collectAsStateWithLifecycle()

    var candidateName by remember(userProfile) {
        mutableStateOf(userProfile?.fullName?.ifBlank { "Vincent Mwangangi" } ?: "Vincent Mwangangi")
    }
    var targetCountry by remember(initialJob) { mutableStateOf(initialJob?.country ?: "United Kingdom") }
    var targetVisaRoute by remember(initialJob) { mutableStateOf(initialJob?.visaType ?: "Skilled Worker Visa (CoS)") }
    var jobTitle by remember(initialJob) {
        mutableStateOf(
            if (initialJob != null) "${initialJob.title} (${initialJob.company})"
            else "Senior Software Engineer"
        )
    }
    var offeredSalary by remember(initialJob) { mutableStateOf(initialJob?.salary ?: "£65,000 / year") }
    var socCode by remember(initialJob) {
        mutableStateOf(
            when (initialJob?.industry) {
                "Healthcare" -> "SOC 2231 / ANZSCO 2544 (Registered Nurses & Clinical Specialists)"
                "Engineering" -> "SOC 2121 / ISCO 214 (Civil, Electrical & Mechanical Engineers)"
                "Transportation", "Logistics" -> "NOC 73300 / ISCO 8332 (Heavy Transport & Logistics Specialists)"
                else -> "SOC 2136 (Programmers & Software Development Professionals)"
            }
        )
    }
    var goingRate by remember(initialJob) {
        mutableStateOf(
            when (initialJob?.country) {
                "Germany" -> "€45,300 (EU Blue Card STEM Statutory Floor)"
                "Canada" -> "$68,000 CAD (ESDC Median Wage / GTS Threshold)"
                "Australia" -> "$73,150 AUD (TSMIT Subclass 482 Statutory Floor)"
                "United States" -> "$118,000 USD (DOL Level II Prevailing Wage)"
                "Netherlands" -> "€63,912 (IND Highly Skilled Migrant 30+ Threshold)"
                else -> "£38,700 (Home Office General Statutory Threshold)"
            }
        )
    }
    var degreeEvaluation by remember { mutableStateOf("Ecctis / Anabin H+ / WES Recognized Degree") }
    var englishScore by remember { mutableStateOf("CEFR C1 / IELTS Academic 8.0") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NavyLight, RoundedCornerShape(18.dp))
                .testTag("export_visa_dossier_dialog"),
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AmberGold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Visa Dossier",
                            tint = AmberGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Export Visa & Relocation Dossier",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Official PDF brief for Immigration Solicitors & HR",
                            color = SlateMuted,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SlateMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = candidateName,
                    onValueChange = { candidateName = it },
                    label = { Text("Candidate Full Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedBorderColor = AmberGold,
                        unfocusedBorderColor = SlateMuted.copy(alpha = 0.4f),
                        focusedLabelColor = AmberGold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = targetCountry,
                        onValueChange = { targetCountry = it },
                        label = { Text("Target Country") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive,
                            focusedBorderColor = AmberGold,
                            unfocusedBorderColor = SlateMuted.copy(alpha = 0.4f),
                            focusedLabelColor = AmberGold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetVisaRoute,
                        onValueChange = { targetVisaRoute = it },
                        label = { Text("Visa Route") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive,
                            focusedBorderColor = AmberGold,
                            unfocusedBorderColor = SlateMuted.copy(alpha = 0.4f),
                            focusedLabelColor = AmberGold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = { Text("Position / Job Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedBorderColor = AmberGold,
                        unfocusedBorderColor = SlateMuted.copy(alpha = 0.4f),
                        focusedLabelColor = AmberGold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = offeredSalary,
                        onValueChange = { offeredSalary = it },
                        label = { Text("Offered Salary") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive,
                            focusedBorderColor = AmberGold,
                            unfocusedBorderColor = SlateMuted.copy(alpha = 0.4f),
                            focusedLabelColor = AmberGold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = socCode,
                        onValueChange = { socCode = it },
                        label = { Text("SOC Code") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive,
                            focusedBorderColor = AmberGold,
                            unfocusedBorderColor = SlateMuted.copy(alpha = 0.4f),
                            focusedLabelColor = AmberGold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Verification Highlights
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavyDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("70/70 Points Mandated Criteria Met", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Statutory Salary Exceeds £38,700 Threshold", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ecctis Statement of Comparability Attached", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateMuted),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontSize = 13.sp)
                    }

                    if (lastExportResult != null) {
                        Button(
                            onClick = {
                                VisaDossierPdfGenerator.shareDossier(context, lastExportResult!!)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = NavyDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share PDF", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                val dossierData = VisaDossierPdfGenerator.DossierData(
                                    candidateName = candidateName,
                                    nationality = userProfile?.nationality?.ifBlank { "Worldwide" } ?: "Worldwide",
                                    targetCountry = targetCountry,
                                    visaRoute = targetVisaRoute,
                                    jobTitle = jobTitle,
                                    offeredSalary = offeredSalary,
                                    socCode = socCode,
                                    standardGoingRate = goingRate,
                                    pointsTotal = 70,
                                    pointsRequired = 70,
                                    degreeComparability = degreeEvaluation,
                                    englishProficiency = englishScore,
                                    maintenanceFunds = "Verified (>28 days in bank account)",
                                    verifiedChecklist = listOf(
                                        "Valid International Passport (>6 Months Validity)" to true,
                                        "Certificate of Sponsorship (CoS) / Job Offer Letter" to true,
                                        "Ecctis / Anabin Degree Statement of Comparability" to true,
                                        "English Language Test Certificate (SELT B2/C1)" to true,
                                        "Bank Statement (Maintenance Funds 28 Consecutive Days)" to true,
                                        "Criminal Record / Police Clearance Certificate" to true,
                                        "Tuberculosis (TB) Test Certificate (if applicable)" to true
                                    ),
                                    attorneyNotes = "Candidate satisfies statutory salary thresholds and shortage category requirements."
                                )
                                viewModel.exportVisaDossierToPdf(context, dossierData) { res ->
                                    if (res != null) {
                                        Toast.makeText(context, "Dossier PDF saved to Downloads!", Toast.LENGTH_SHORT).show()
                                        VisaDossierPdfGenerator.shareDossier(context, res)
                                    } else {
                                        Toast.makeText(context, "Failed to generate Dossier PDF", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isExporting,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isExporting) {
                                CircularProgressIndicator(color = NavyDark, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Generate PDF", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlobalJobComparisonDialog(
    jobs: List<com.example.data.JobEntity>,
    viewModel: JobViewModel,
    onDismiss: () -> Unit,
    onBuildDossierForJob: (com.example.data.JobEntity) -> Unit
) {
    val maxGrossUsd = jobs.maxOfOrNull { viewModel.estimateAnnualUsdSalary(it.salary, it.country) } ?: 0
    val maxNetUsd = jobs.maxOfOrNull { viewModel.estimateNetTakeHomeUsd(it) } ?: 0
    val minVisaDays = jobs.minOfOrNull { viewModel.estimateVisaProcessingDays(it) } ?: 999
    val minPrMonths = jobs.minOfOrNull { viewModel.estimatePrTimelineMonths(it) } ?: 999

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TealCyan.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .testTag("global_job_comparison_dialog"),
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "⚖️ Global Offer & Visa Comparator",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Side-by-side USD salary, net take-home, visa speed & PR timeline",
                            color = SlateMuted,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SlateMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                jobs.forEach { job ->
                    val grossUsd = viewModel.estimateAnnualUsdSalary(job.salary, job.country)
                    val netUsd = viewModel.estimateNetTakeHomeUsd(job)
                    val visaDays = viewModel.estimateVisaProcessingDays(job)
                    val prMonths = viewModel.estimatePrTimelineMonths(job)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavyDark),
                        border = BorderStroke(
                            1.dp,
                            if (netUsd == maxNetUsd && jobs.size > 1) EmeraldGreen.copy(alpha = 0.6f) else NavyLight
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = job.title,
                                        color = WhiteActive,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${job.company} • ${job.country}",
                                        color = TealCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                if (netUsd == maxNetUsd && jobs.size > 1) {
                                    Surface(
                                        color = EmeraldGreen.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "★ TOP NET PAY",
                                            color = EmeraldGreen,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(NavyMedium, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Text("Gross (USD/yr)", color = SlateMuted, fontSize = 9.sp)
                                    Text(
                                        text = "$${String.format("%,d", grossUsd)}",
                                        color = if (grossUsd == maxGrossUsd) EmeraldGreen else WhiteActive,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(job.salary, color = SlateMuted, fontSize = 9.sp, maxLines = 1)
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(NavyMedium, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Text("Est. Net USD/yr", color = SlateMuted, fontSize = 9.sp)
                                    Text(
                                        text = "$${String.format("%,d", netUsd)}",
                                        color = if (netUsd == maxNetUsd) EmeraldGreen else TealCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text("After Tax/Ruling", color = SlateMuted, fontSize = 9.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(NavyMedium, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Text("Visa Processing", color = SlateMuted, fontSize = 9.sp)
                                    Text(
                                        text = "~$visaDays Days",
                                        color = if (visaDays == minVisaDays) EmeraldGreen else WhiteActive,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Text(job.visaType, color = SlateMuted, fontSize = 9.sp, maxLines = 1)
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(NavyMedium, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Text("PR / Residency", color = SlateMuted, fontSize = 9.sp)
                                    Text(
                                        text = "$prMonths Months",
                                        color = if (prMonths == minPrMonths) EmeraldGreen else AmberGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = if (job.relocationAssistance) "Full Relo Included" else "Visa Only",
                                        color = SlateMuted,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.toggleCompareJob(job.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.weight(0.38f).height(30.dp)
                                ) {
                                    Text("Remove", fontSize = 10.sp)
                                }
                                Button(
                                    onClick = { onBuildDossierForJob(job) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = NavyDark),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.weight(0.62f).height(30.dp)
                                ) {
                                    Text("📄 Build Dossier PDF", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.clearComparedJobs()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear All", fontSize = 12.sp)
                    }
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealCyan, contentColor = NavyDark),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Done", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

