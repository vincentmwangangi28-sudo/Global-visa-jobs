package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.VisaDocumentEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class DocumentExpiryStatus(val label: String, val color: Color, val bgColor: Color) {
    VALID("Valid", EmeraldGreen, EmeraldGreen.copy(alpha = 0.15f)),
    EXPIRING_SOON("Expires in <90 Days", AmberGold, AmberGold.copy(alpha = 0.15f)),
    CRITICAL_URGENT("Expires in <30 Days", Color(0xFFE65100), Color(0xFFE65100).copy(alpha = 0.2f)),
    EXPIRED("Expired / Renewal Required", CoralRed, CoralRed.copy(alpha = 0.2f)),
    NO_EXPIRY("Permanent / No Expiry", SlateMuted, NavyLight)
}

fun calculateExpiryStatus(expiryDateStr: String): Pair<DocumentExpiryStatus, String> {
    if (expiryDateStr.isBlank() || expiryDateStr.contains("PENDING", ignoreCase = true)) {
        return Pair(DocumentExpiryStatus.NO_EXPIRY, "Pending Issue")
    }

    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val expiryDate = sdf.parse(expiryDateStr) ?: return Pair(DocumentExpiryStatus.NO_EXPIRY, "Unknown")
        val now = Date()
        val diffMillis = expiryDate.time - now.time
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

        return when {
            diffDays < 0 -> Pair(DocumentExpiryStatus.EXPIRED, "Expired ${-diffDays}d ago")
            diffDays <= 30 -> Pair(DocumentExpiryStatus.CRITICAL_URGENT, "Expires in $diffDays days!")
            diffDays <= 90 -> Pair(DocumentExpiryStatus.EXPIRING_SOON, "Expires in $diffDays days")
            else -> {
                val months = (diffDays / 30).toInt()
                Pair(DocumentExpiryStatus.VALID, "Valid for ~$months mos ($diffDays days)")
            }
        }
    } catch (e: Exception) {
        return Pair(DocumentExpiryStatus.NO_EXPIRY, expiryDateStr)
    }
}

@Composable
fun VisaDocumentVaultScreen(viewModel: JobViewModel) {
    val context = LocalContext.current
    val documents by viewModel.visaDocuments.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var showPrepopulateConfirm by remember { mutableStateOf(false) }

    val categories = listOf("All", "Passport", "Language Test", "ECA / Degree", "Police Clearance", "Medical", "CoS / LMIA Offer")
    val filteredDocs = if (selectedCategoryFilter == "All") {
        documents
    } else {
        documents.filter { it.category == selectedCategoryFilter }
    }

    val expiredCount = documents.count { calculateExpiryStatus(it.expiryDate).first == DocumentExpiryStatus.EXPIRED }
    val expiringSoonCount = documents.count {
        val st = calculateExpiryStatus(it.expiryDate).first
        st == DocumentExpiryStatus.EXPIRING_SOON || st == DocumentExpiryStatus.CRITICAL_URGENT
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("visa_doc_vault_screen")
    ) {
        // Vault Header card
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            border = BorderStroke(1.dp, NavyLight),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Immigration Document Vault & Deadlines",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Track critical document validity, expiration countdowns, and official certifications.",
                            color = SlateMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Metric Counters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VaultMetricBadge(
                        label = "Total Vault Docs",
                        count = documents.size.toString(),
                        color = TealCyan,
                        modifier = Modifier.weight(1f)
                    )
                    VaultMetricBadge(
                        label = "Expiring Soon",
                        count = expiringSoonCount.toString(),
                        color = if (expiringSoonCount > 0) AmberGold else EmeraldGreen,
                        modifier = Modifier.weight(1f)
                    )
                    VaultMetricBadge(
                        label = "Expired",
                        count = expiredCount.toString(),
                        color = if (expiredCount > 0) CoralRed else EmeraldGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons Row (Add Document & Prepopulate Checklist)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { showPrepopulateConfirm = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TealCyan),
                border = BorderStroke(1.dp, TealCyan.copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(0.48f)
                    .height(40.dp)
                    .testTag("prepopulate_docs_btn")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Standard Checklist", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = NavyDark),
                modifier = Modifier
                    .weight(0.52f)
                    .height(40.dp)
                    .testTag("add_new_doc_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Document", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategoryFilter).coerceAtLeast(0),
            containerColor = NavyDark,
            contentColor = EmeraldGreen,
            edgePadding = 0.dp,
            divider = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategoryFilter == cat
                Tab(
                    selected = isSelected,
                    onClick = { selectedCategoryFilter = cat },
                    text = {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) EmeraldGreen else SlateMuted
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Document List
        if (filteredDocs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SlateMuted,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No documents found in this category.",
                        color = SlateMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap 'Standard Checklist' to load default visa documents.",
                        color = TealCyan,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredDocs, key = { it.id }) { doc ->
                    VisaDocumentCard(
                        doc = doc,
                        onToggleVerify = {
                            viewModel.updateVisaDocument(doc.copy(isVerified = !doc.isVerified))
                        },
                        onDelete = {
                            viewModel.deleteVisaDocument(doc.id)
                            Toast.makeText(context, "Deleted ${doc.documentName}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddVisaDocumentDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newDoc ->
                viewModel.saveVisaDocument(newDoc)
                Toast.makeText(context, "Saved ${newDoc.documentName} to vault!", Toast.LENGTH_SHORT).show()
                showAddDialog = false
            }
        )
    }

    if (showPrepopulateConfirm) {
        AlertDialog(
            onDismissRequest = { showPrepopulateConfirm = false },
            title = { Text("Pre-populate Visa Document Vault", color = WhiteActive, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "This will load the standard required immigration documents (Passport, IELTS/CELPIP, ECA assessment, Police Clearance, Medical Exam, and Sponsorship Certificate) into your secure local vault.",
                    color = SlateMuted,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.prePopulateVisaDocuments("General")
                        showPrepopulateConfirm = false
                        Toast.makeText(context, "Loaded Standard Document Vault Checklist!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = NavyDark)
                ) {
                    Text("Load Checklist", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showPrepopulateConfirm = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateMuted)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = NavyMedium,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun VisaDocumentCard(
    doc: VisaDocumentEntity,
    onToggleVerify: () -> Unit,
    onDelete: () -> Unit
) {
    val (status, expiryCountdown) = calculateExpiryStatus(doc.expiryDate)

    Card(
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        border = BorderStroke(1.dp, if (doc.isVerified) EmeraldGreen.copy(alpha = 0.5f) else NavyLight),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Category icon / badge
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(status.bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (doc.category) {
                            "Passport" -> Icons.Default.Person
                            "Language Test" -> Icons.Default.Star
                            "ECA / Degree" -> Icons.Default.Info
                            "Police Clearance" -> Icons.Default.Lock
                            "Medical" -> Icons.Default.Favorite
                            else -> Icons.Default.List
                        },
                        contentDescription = null,
                        tint = status.color,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doc.documentName,
                        color = WhiteActive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${doc.category} • ${doc.issuingAuthority.ifEmpty { "Official Body" }}",
                        color = SlateMuted,
                        fontSize = 11.sp
                    )
                }

                // Verification check
                IconButton(
                    onClick = onToggleVerify,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (doc.isVerified) Icons.Default.CheckCircle else Icons.Default.Check,
                        contentDescription = "Verified status",
                        tint = if (doc.isVerified) EmeraldGreen else SlateMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = CoralRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details and Expiry Ribbon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(NavyDark)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (doc.documentNumber.isNotBlank()) {
                    Text(
                        text = "Ref: ${doc.documentNumber}",
                        color = TealCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = "Expiry: ${doc.expiryDate.ifEmpty { "N/A" }}",
                        color = SlateMuted,
                        fontSize = 10.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(status.bgColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = expiryCountdown,
                        color = status.color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (doc.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📝 ${doc.notes}",
                    color = SlateMuted,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun VaultMetricBadge(
    label: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(NavyDark)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = count, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Text(text = label, color = SlateMuted, fontSize = 9.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVisaDocumentDialog(
    onDismiss: () -> Unit,
    onSave: (VisaDocumentEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Passport") }
    var number by remember { mutableStateOf("") }
    var authority by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val categories = listOf("Passport", "Language Test", "ECA / Degree", "Police Clearance", "Medical", "CoS / LMIA Offer", "Biometrics", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyDark),
            border = BorderStroke(1.dp, NavyLight),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Add Visa Document",
                    color = WhiteActive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Document Name (e.g. Passport, IELTS)", color = SlateMuted, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight,
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedContainerColor = NavyMedium,
                        unfocusedContainerColor = NavyMedium
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                DropdownSelector(
                    label = "Category",
                    selectedValue = category,
                    options = categories,
                    onSelect = { category = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Document / Reference Number", color = SlateMuted, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight,
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedContainerColor = NavyMedium,
                        unfocusedContainerColor = NavyMedium
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = authority,
                    onValueChange = { authority = it },
                    label = { Text("Issuing Authority (e.g. Home Office, WES)", color = SlateMuted, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight,
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedContainerColor = NavyMedium,
                        unfocusedContainerColor = NavyMedium
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text("Expiry Date (YYYY-MM-DD)", color = SlateMuted, fontSize = 11.sp) },
                    placeholder = { Text("2028-12-31", color = SlateMuted.copy(alpha = 0.5f), fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight,
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedContainerColor = NavyMedium,
                        unfocusedContainerColor = NavyMedium
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Requirements", color = SlateMuted, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight,
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedContainerColor = NavyMedium,
                        unfocusedContainerColor = NavyMedium
                    ),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateMuted),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(
                                    VisaDocumentEntity(
                                        documentName = name,
                                        category = category,
                                        documentNumber = number,
                                        issuingAuthority = authority,
                                        expiryDate = expiryDate,
                                        notes = notes,
                                        isVerified = false
                                    )
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = NavyDark),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save to Vault", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
