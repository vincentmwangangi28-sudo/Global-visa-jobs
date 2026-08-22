package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.JobEntity
import com.example.data.UserProfileEntity
import com.example.data.VisaApplicationEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ApplicationMethodType(val title: String, val subtitle: String) {
    CAREER_PORTAL("Direct Web Portal", "Official company ATS / job portal"),
    DIRECT_EMAIL("Email Application", "Pre-formatted formal recruitment email"),
    TRACK_APPLICATION("Track & Log", "Record to In-App Application Tracker"),
    VISA_CHECKLIST("Visa Checklist", "Required docs & eligibility check")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationMethodDialog(
    job: JobEntity,
    viewModel: JobViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val profileOpt by viewModel.userProfile.collectAsStateWithLifecycle()
    val visaApps by viewModel.visaApplications.collectAsStateWithLifecycle()
    val existingApp = visaApps.firstOrNull { it.jobId == job.id }

    var selectedMethod by remember { mutableStateOf(ApplicationMethodType.CAREER_PORTAL) }

    // Cover letter generator state
    var generatedCoverLetter by remember { mutableStateOf<String?>(null) }
    var isGeneratingLetter by remember { mutableStateOf(false) }

    // Application note and status state
    var selectedStatus by remember { mutableStateOf(existingApp?.status ?: "Applied") }
    var applicationNotes by remember { mutableStateOf(existingApp?.notes ?: "") }

    val profile = profileOpt ?: UserProfileEntity()
    val candidateName = profile.fullName.ifEmpty { "International Applicant" }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .border(1.dp, NavyLight, RoundedCornerShape(20.dp))
                .testTag("application_method_dialog"),
            colors = CardDefaults.cardColors(containerColor = NavyDark),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Application Methods",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${job.company} • ${job.location}",
                            color = SlateMuted,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_app_method_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SlateMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Job banner badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NavyMedium)
                        .border(1.dp, NavyLight, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = job.title,
                                color = WhiteActive,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Visa Pathway: ${job.visaType}",
                                color = TealCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EmeraldGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${job.confidenceScore}% Sponsor Match",
                                color = EmeraldGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Bar
                ScrollableTabRow(
                    selectedTabIndex = selectedMethod.ordinal,
                    containerColor = NavyDark,
                    contentColor = EmeraldGreen,
                    edgePadding = 0.dp,
                    divider = { Divider(color = NavyLight) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ApplicationMethodType.values().forEach { method ->
                        Tab(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            text = {
                                Text(
                                    text = method.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedMethod == method) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedMethod == method) EmeraldGreen else SlateMuted
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedMethod) {
                        ApplicationMethodType.CAREER_PORTAL -> {
                            CareerPortalMethodContent(
                                job = job,
                                onOpenUrl = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(job.applicationUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        clipboardManager.setText(AnnotatedString(job.applicationUrl))
                                        Toast.makeText(context, "Link copied! Please open in browser.", Toast.LENGTH_LONG).show()
                                    }
                                },
                                onCopyUrl = {
                                    clipboardManager.setText(AnnotatedString(job.applicationUrl))
                                    Toast.makeText(context, "Application Portal URL copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        ApplicationMethodType.DIRECT_EMAIL -> {
                            DirectEmailMethodContent(
                                job = job,
                                profile = profile,
                                onOpenEmail = { subject, body ->
                                    try {
                                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:")
                                            putExtra(Intent.EXTRA_SUBJECT, subject)
                                            putExtra(Intent.EXTRA_TEXT, body)
                                        }
                                        context.startActivity(Intent.createChooser(emailIntent, "Send Application Email"))
                                    } catch (e: Exception) {
                                        clipboardManager.setText(AnnotatedString(body))
                                        Toast.makeText(context, "Email body copied to clipboard!", Toast.LENGTH_LONG).show()
                                    }
                                },
                                onCopyEmail = { body ->
                                    clipboardManager.setText(AnnotatedString(body))
                                    Toast.makeText(context, "Formal application email copied!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        ApplicationMethodType.TRACK_APPLICATION -> {
                            TrackApplicationMethodContent(
                                job = job,
                                existingApp = existingApp,
                                selectedStatus = selectedStatus,
                                onStatusChange = { selectedStatus = it },
                                applicationNotes = applicationNotes,
                                onNotesChange = { applicationNotes = it },
                                onSaveTracking = {
                                    val nowFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                                    val app = VisaApplicationEntity(
                                        jobId = job.id,
                                        jobTitle = job.title,
                                        company = job.company,
                                        country = job.country,
                                        status = selectedStatus,
                                        notes = applicationNotes,
                                        updatedDate = nowFormatted
                                    )
                                    viewModel.saveVisaApplication(app)
                                    Toast.makeText(context, "Application status logged: $selectedStatus!", Toast.LENGTH_SHORT).show()
                                },
                                onDeleteTracking = {
                                    viewModel.deleteVisaApplication(job.id)
                                    Toast.makeText(context, "Application tracking removed", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        ApplicationMethodType.VISA_CHECKLIST -> {
                            VisaChecklistMethodContent(
                                job = job,
                                profile = profile
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CareerPortalMethodContent(
    job: JobEntity,
    onOpenUrl: () -> Unit,
    onCopyUrl: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        border = BorderStroke(1.dp, NavyLight),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Official Employer Portal / ATS",
                    color = WhiteActive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Apply directly through ${job.company}'s official verified careers system. This ensures direct submission to the in-house talent acquisition team.",
                color = SlateMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // URL display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavyDark)
                    .border(1.dp, NavyLight, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = job.applicationUrl,
                        color = TealCyan,
                        fontSize = 11.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCopyUrl,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TealCyan),
                    border = BorderStroke(1.dp, TealCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(0.45f).height(42.dp).testTag("copy_portal_url_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy URL", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onOpenUrl,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = NavyDark),
                    modifier = Modifier.weight(0.55f).height(42.dp).testTag("launch_career_portal_btn")
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Portal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Tips for portal application
    Card(
        colors = CardDefaults.cardColors(containerColor = NavyMedium.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, NavyLight),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "📌 Tips for International Portal Applications:",
                color = AmberGold,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            PortalTipItem("Select 'Requires Sponsorship' honestly when prompted in ATS forms; this employer has verified sponsor licenses.")
            PortalTipItem("Upload your resume in PDF format adhering to ${job.country} layout standards.")
            PortalTipItem("Attach a customized cover letter explicitly referencing the ${job.visaType} route.")
        }
    }
}

@Composable
fun DirectEmailMethodContent(
    job: JobEntity,
    profile: UserProfileEntity,
    onOpenEmail: (subject: String, body: String) -> Unit,
    onCopyEmail: (body: String) -> Unit
) {
    val candidateName = profile.fullName.ifEmpty { "International Applicant" }
    val candidateNationality = profile.nationality.ifEmpty { "Global Candidate" }
    val candidateSkills = profile.skills.ifEmpty { "Key Technical / Professional Skills" }
    val candidateExp = profile.experience.ifEmpty { "Relevant Domain Experience" }

    val emailSubject = "Job Application: ${job.title} (${job.visaType}) - $candidateName"
    val emailBody = """
Dear Hiring Team at ${job.company},

I am writing to express my strong interest in the ${job.title} position located in ${job.location}.

As a seasoned professional with a background in ${candidateExp}, I have developed core expertise in:
- ${candidateSkills}

I noted that this role offers support for the ${job.visaType}. As an international candidate holding ${candidateNationality} citizenship, I am prepared with certified credentials and ready to relocate seamlessly.

I have attached my comprehensive CV and professional portfolio for your review. I would welcome the opportunity to discuss how my qualifications align with ${job.company}'s goals.

Thank you for your time and consideration.

Sincerely,
${candidateName}
Email: ${profile.linkedInEmail.ifEmpty { "[Your Email Address]" }}
Phone: [Your Phone Number]
LinkedIn: ${if (profile.linkedInConnected) "Verified via AI Studio" else "[Your LinkedIn Profile]"}
""".trimIndent()

    Card(
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        border = BorderStroke(1.dp, NavyLight),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Formal Recruitment Email Template",
                    color = WhiteActive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Pre-formatted email tailored specifically for ${job.company} with your profile credentials:",
                color = SlateMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subject preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavyDark)
                    .padding(8.dp)
            ) {
                Text(
                    text = "Subject: $emailSubject",
                    color = TealCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavyDark)
                    .border(1.dp, NavyLight, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = emailBody,
                    color = SlateMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onCopyEmail(emailBody) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TealCyan),
                    border = BorderStroke(1.dp, TealCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(0.45f).height(42.dp).testTag("copy_email_app_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Email", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { onOpenEmail(emailSubject, emailBody) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = NavyDark),
                    modifier = Modifier.weight(0.55f).height(42.dp).testTag("open_email_app_btn")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Launch Email", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TrackApplicationMethodContent(
    job: JobEntity,
    existingApp: VisaApplicationEntity?,
    selectedStatus: String,
    onStatusChange: (String) -> Unit,
    applicationNotes: String,
    onNotesChange: (String) -> Unit,
    onSaveTracking: () -> Unit,
    onDeleteTracking: () -> Unit
) {
    val statuses = listOf(
        "Applied",
        "Under Review",
        "Screening Call",
        "Technical Interview",
        "Offer Received",
        "Sponsorship Approved",
        "Visa Filed",
        "Visa Approved"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        border = BorderStroke(1.dp, NavyLight),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Application Pipeline Tracking",
                    color = WhiteActive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Log and track your application progress in your local and cloud-synced Visa Tracker:",
                color = SlateMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "APPLICATION STAGE:",
                color = TealCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Status chip selection
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                statuses.chunked(2).forEach { rowStatuses ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowStatuses.forEach { status ->
                            val isSelected = selectedStatus == status
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) EmeraldGreen else NavyDark)
                                    .border(
                                        1.dp,
                                        if (isSelected) EmeraldGreen else NavyLight,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onStatusChange(status) }
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = status,
                                    color = if (isSelected) NavyDark else WhiteActive,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Notes Input
            OutlinedTextField(
                value = applicationNotes,
                onValueChange = onNotesChange,
                label = { Text("Application Notes / Follow-up Dates", color = SlateMuted, fontSize = 12.sp) },
                placeholder = { Text("e.g. Applied via Workday portal, recruiter name Sarah, interview set for Friday...", color = SlateMuted.copy(alpha = 0.5f), fontSize = 11.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("application_method_notes_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldGreen,
                    unfocusedBorderColor = NavyLight,
                    focusedTextColor = WhiteActive,
                    unfocusedTextColor = WhiteActive,
                    focusedContainerColor = NavyDark,
                    unfocusedContainerColor = NavyDark
                ),
                shape = RoundedCornerShape(8.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Save / Delete Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (existingApp != null) {
                    OutlinedButton(
                        onClick = onDeleteTracking,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                        border = BorderStroke(1.dp, CoralRed.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(0.35f).height(42.dp).testTag("delete_app_tracking_btn")
                    ) {
                        Text("Remove", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Button(
                    onClick = onSaveTracking,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = NavyDark),
                    modifier = Modifier.weight(1f).height(42.dp).testTag("save_app_tracking_btn")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (existingApp != null) "Update Application Record" else "Log as Applied Today",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun VisaChecklistMethodContent(
    job: JobEntity,
    profile: UserProfileEntity
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        border = BorderStroke(1.dp, NavyLight),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${job.country} Visa Sponsorship Checklist",
                    color = WhiteActive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Ensure you have the following prerequisites ready before submitting for the ${job.visaType}:",
                color = SlateMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            VisaDocItem("Valid International Passport", "Must have minimum 6-12 months validity beyond intended start date.")
            VisaDocItem("Official Language Proficiency", "IELTS UKVI / General (6.0+), TOEFL iBT, or CELPIP certificate.")
            VisaDocItem("Educational Assessment (ECA)", "WES, ECCTIS, or UK ENIC degree equivalency verification.")
            VisaDocItem("Police Clearance Certificate", "Clean criminal background check from all countries lived in >12 months.")
            VisaDocItem("Targeted CV / Resume Format", "Tailored to ${job.country} hiring standards without photo/age requirements.")
            VisaDocItem("Employer Sponsorship Certificate (CoS/LMIA)", "Provided by ${job.company} upon formal job offer.")
        }
    }
}

@Composable
fun PortalTipItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("•", color = AmberGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, color = SlateMuted, fontSize = 11.sp, lineHeight = 15.sp)
    }
}

@Composable
fun VisaDocItem(title: String, desc: String) {
    var isChecked by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(NavyDark.copy(alpha = 0.6f))
            .clickable { isChecked = !isChecked }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { isChecked = it },
            colors = CheckboxDefaults.colors(
                checkedColor = EmeraldGreen,
                uncheckedColor = SlateMuted,
                checkmarkColor = NavyDark
            ),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isChecked) EmeraldGreen else WhiteActive,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
            Text(
                text = desc,
                color = SlateMuted,
                fontSize = 10.sp,
                lineHeight = 13.sp
            )
        }
    }
}
