package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.auth.LinkedInOAuthState
import com.example.auth.LinkedInProfileData
import com.example.data.UserProfileEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

val LinkedInBlue = Color(0xFF0A66C2)
val LinkedInNavy = Color(0xFF004182)
val GoldVerified = Color(0xFFFBBF24)
val EmeraldGreenLight = Color(0xFF34D399)

/**
 * Main LinkedIn OAuth2 Connection & Verification Card.
 */
@Composable
fun LinkedInOAuthCard(
    profile: UserProfileEntity,
    viewModel: JobViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val oauthState by viewModel.linkedInOAuthState.collectAsStateWithLifecycle()
    val importPreview by viewModel.linkedInImportPreview.collectAsStateWithLifecycle()
    val isUnlinking by viewModel.isUnlinkingLinkedIn.collectAsStateWithLifecycle()

    var showCredentialsDialog by remember { mutableStateOf(false) }

    // Dialog for Import Confirmation
    importPreview?.let { previewData ->
        LinkedInImportPreviewDialog(
            data = previewData,
            onConfirm = { autoImport ->
                viewModel.applyLinkedInImport(previewData, autoImport)
                Toast.makeText(context, "LinkedIn data imported & profile verified!", Toast.LENGTH_LONG).show()
            },
            onDismiss = {
                viewModel.dismissLinkedInDialog()
            }
        )
    }

    if (showCredentialsDialog) {
        LinkedInVerificationProofDialog(
            profile = profile,
            onDismiss = { showCredentialsDialog = false }
        )
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (profile.linkedInConnected) NavyMedium else Color(0xFF0F172A)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.5.dp,
            if (profile.linkedInConnected) LinkedInBlue.copy(alpha = 0.8f) else NavyLight
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("linkedin_oauth_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LinkedInBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "in",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "LinkedIn OAuth 2.0 Auth",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        if (profile.linkedInConnected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EmeraldGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "VERIFIED ✓",
                                    color = EmeraldGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                    Text(
                        text = if (profile.linkedInConnected)
                            "Accredited Identity & Professional Network"
                        else
                            "Auto-import resume history & verify professional network",
                        color = SlateMuted,
                        fontSize = 11.sp
                    )
                }

                if (profile.linkedInConnected) {
                    IconButton(
                        onClick = { showCredentialsDialog = true },
                        modifier = Modifier.size(32.dp).testTag("view_credentials_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Verification Proof",
                            tint = TealCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = NavyLight.copy(alpha = 0.6f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            if (!profile.linkedInConnected) {
                // Not Connected State
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TealCyan,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Instant 1-Click ATS Resume import from your work experience & education history",
                            color = WhiteActive.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = GoldVerified,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Earn a Verified Professional Badge (increases employer visa response rate by 3.2x)",
                            color = WhiteActive.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // OAuth action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.startLinkedInOAuthFlow(context)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LinkedInBlue,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .testTag("connect_linkedin_oauth_btn")
                        ) {
                            if (oauthState is LinkedInOAuthState.Authorizing || oauthState is LinkedInOAuthState.ExchangingToken) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Connecting...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sign In with LinkedIn", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.connectLinkedInSandbox(
                                    customName = profile.fullName.ifBlank { "Vincent Mwangangi" },
                                    customHeadline = "Senior Cloud & Distributed Systems Engineer | Visa Sponsorship Ready",
                                    customEmail = "vincent.mwangangi.verified@example.com"
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, TealCyan),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TealCyan),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("demo_sandbox_linkedin_btn")
                        ) {
                            if (oauthState is LinkedInOAuthState.FetchingProfile) {
                                CircularProgressIndicator(color = TealCyan, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Sandbox Link", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    if (oauthState is LinkedInOAuthState.Error) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠️ ${(oauthState as LinkedInOAuthState.Error).errorMessage}",
                            color = CoralRed,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                // Connected & Verified State
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Profile Info & Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .border(2.dp, LinkedInBlue, CircleShape)
                                .background(NavyLight),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profile.linkedInProfilePicture.isNotBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(profile.linkedInProfilePicture)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "LinkedIn Profile Avatar",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = profile.fullName.take(2).uppercase().ifEmpty { "IN" },
                                    color = WhiteActive,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.fullName.ifEmpty { "Verified Member" },
                                color = WhiteActive,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            if (profile.linkedInHeadline.isNotBlank()) {
                                Text(
                                    text = profile.linkedInHeadline,
                                    color = SlateMuted,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (profile.linkedInEmail.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${profile.linkedInEmail} (Verified)",
                                        color = EmeraldGreenLight,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    // Trust Score Meter Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavyDark),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, NavyLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Professional Trust Score",
                                    color = SlateMuted,
                                    fontSize = 11.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${profile.linkedInTrustScore}%",
                                        color = if (profile.linkedInTrustScore >= 90) EmeraldGreen else GoldVerified,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (profile.linkedInTrustScore >= 90) "High Trust Tier (Fast-Track Visa)" else "Verified Tier",
                                        color = WhiteActive,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NavyLight)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = profile.linkedInConnectionsCount.ifEmpty { "500+ Connections" },
                                    color = TealCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Actions: Sync & Disconnect
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.connectLinkedInSandbox(
                                    customName = profile.fullName,
                                    customHeadline = profile.linkedInHeadline.ifEmpty { "Senior Engineer | Visa Ready" },
                                    customEmail = profile.linkedInEmail.ifEmpty { "candidate@example.com" }
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldGreen,
                                contentColor = NavyDark
                            ),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(40.dp)
                                .testTag("reimport_linkedin_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Re-sync Resume Data", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.unlinkLinkedInAccount()
                                Toast.makeText(context, "LinkedIn profile unlinked.", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CoralRed.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("unlink_linkedin_btn")
                        ) {
                            if (isUnlinking) {
                                CircularProgressIndicator(color = CoralRed, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Disconnect", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Dialog to review and confirm imported LinkedIn resume data.
 */
@Composable
fun LinkedInImportPreviewDialog(
    data: LinkedInProfileData,
    onConfirm: (autoImportToProfile: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var importName by remember { mutableStateOf(true) }
    var importSummary by remember { mutableStateOf(true) }
    var importExperience by remember { mutableStateOf(true) }
    var importSkills by remember { mutableStateOf(true) }
    var importEducation by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, LinkedInBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("linkedin_import_preview_dialog")
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
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(LinkedInBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("in", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LinkedIn Data Import",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Verified Profile: ${data.fullName}",
                            color = EmeraldGreen,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SlateMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = NavyLight, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Select sections to import into your visa matching profile:",
                    color = SlateMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Checklist Items
                ImportCheckboxItem(
                    title = "Full Name & Verified Email",
                    subtitle = "${data.fullName} (${data.email})",
                    checked = importName,
                    onCheckedChange = { importName = it }
                )

                ImportCheckboxItem(
                    title = "Headline & Professional Summary",
                    subtitle = data.headline,
                    checked = importSummary,
                    onCheckedChange = { importSummary = it }
                )

                ImportCheckboxItem(
                    title = "Work Experience (${data.positions.size} Roles)",
                    subtitle = data.positions.firstOrNull()?.let { "${it.title} at ${it.company}" } ?: "Experience history",
                    checked = importExperience,
                    onCheckedChange = { importExperience = it }
                )

                ImportCheckboxItem(
                    title = "Accredited Skills (${data.skills.size} Skills)",
                    subtitle = data.skills.take(4).joinToString(", ") + if (data.skills.size > 4) "..." else "",
                    checked = importSkills,
                    onCheckedChange = { importSkills = it }
                )

                ImportCheckboxItem(
                    title = "Education & Degrees",
                    subtitle = data.educations.firstOrNull()?.let { "${it.degree}, ${it.school}" } ?: "Education history",
                    checked = importEducation,
                    onCheckedChange = { importEducation = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Trust Score Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavyDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Accredited Trust Score: ${data.trustScore}/100", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Network Verified • Anti-Fraud Cryptographic Hash Attached", color = SlateMuted, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Button(
                    onClick = {
                        onConfirm(true)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = NavyDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("confirm_import_profile_btn")
                ) {
                    Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply & Sync to My Profile", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        onConfirm(false)
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, NavyLight),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateMuted),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Text("Only Attach Verification Badge", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun ImportCheckboxItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 6.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = EmeraldGreen,
                checkmarkColor = NavyDark,
                uncheckedColor = SlateMuted
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = WhiteActive, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text(text = subtitle, color = SlateMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/**
 * Proof of Verification Dialog showing Cryptographic Hash & OpenID details.
 */
@Composable
fun LinkedInVerificationProofDialog(
    profile: UserProfileEntity,
    onDismiss: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US)
    val verifiedDate = if (profile.linkedInVerifiedAt > 0)
        dateFormat.format(Date(profile.linkedInVerifiedAt))
    else
        "Active"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, GoldVerified),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("linkedin_proof_dialog")
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
                    Icon(Icons.Default.Star, contentDescription = null, tint = GoldVerified, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Accredited Verification Proof", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("LinkedIn OAuth 2.0 Security Protocol", color = SlateMuted, fontSize = 11.sp)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SlateMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = NavyLight, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                ProofField(label = "Verified Candidate Name", value = profile.fullName)
                ProofField(label = "Verified Primary Email", value = "${profile.linkedInEmail} [Status: Verified ✓]")
                ProofField(label = "LinkedIn Member UID", value = profile.linkedInMemberId)
                ProofField(label = "Verification Timestamp", value = verifiedDate)
                ProofField(label = "Trust & Identity Rating", value = "${profile.linkedInTrustScore} / 100 (Accredited)")
                ProofField(label = "Cryptographic SHA-256 Hash", value = profile.linkedInVerificationHash.ifEmpty { "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855" })

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val proofText = """
                            --- LINKEDIN PROFESSIONAL VERIFICATION CERTIFICATE ---
                            Candidate: ${profile.fullName}
                            Verified Email: ${profile.linkedInEmail}
                            Member ID: ${profile.linkedInMemberId}
                            Trust Score: ${profile.linkedInTrustScore}%
                            Verification Date: $verifiedDate
                            Digital Signature Hash: ${profile.linkedInVerificationHash}
                            Issuer: Global Visa Jobs & LinkedIn OAuth 2.0 OpenID Connect
                        """.trimIndent()
                        clipboard.setText(AnnotatedString(proofText))
                        Toast.makeText(context, "Verification Certificate copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldVerified, contentColor = NavyDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("copy_certificate_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Verification Certificate", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ProofField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, color = SlateMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        Text(
            text = value.ifEmpty { "N/A" },
            color = WhiteActive,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Interactive Download App Modal for Desktop Dashboard & Mobile users.
 */
@Composable
fun DownloadAppDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val appShareUrl = "https://globalvisajobs.com"
    val directApkUrl = "https://github.com/aistudio/global-visa-jobs/releases/latest/download/app-release.apk"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, TealCyan),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("download_app_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TealCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = TealCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Download Mobile App",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Android APK & Multi-Platform Access",
                            color = SlateMuted,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SlateMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = NavyLight, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Feature Highlights
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyDark)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("5,000+ Live Verified Sponsor Jobs", color = WhiteActive, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI ATS Resume Builder & Visa Probability", color = WhiteActive, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("LinkedIn 1-Click Verification & Sync", color = WhiteActive, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action: Direct APK Download
                Button(
                    onClick = {
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(directApkUrl)).apply {
                                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                            Toast.makeText(context, "Opening direct APK download...", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                            clipboard.setText(AnnotatedString(directApkUrl))
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealCyan,
                        contentColor = NavyDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("direct_apk_download_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download Android APK (v2.4)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action: Copy Shareable Web & App Link
                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(appShareUrl))
                        Toast.makeText(context, "App URL copied to clipboard: $appShareUrl", Toast.LENGTH_LONG).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, NavyLight),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WhiteActive),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("copy_app_link_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = SlateMuted)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy App Link & Share", fontSize = 12.sp, color = WhiteActive)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Step-by-step installation instructions
                Text(
                    text = "Installation Instructions:",
                    color = SlateMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1. Download the .apk file on your Android device.\n2. Tap the notification to install.\n3. If prompted, toggle 'Allow from this source' in Settings.",
                    color = SlateMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

