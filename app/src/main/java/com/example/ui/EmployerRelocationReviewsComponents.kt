package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

data class EmployerImmigrationReview(
    val id: String,
    val companyName: String,
    val country: String,
    val roleTitle: String,
    val originCountry: String,
    val visaPathway: String,
    val overallImmigrationRating: Double, // out of 5.0
    val cosOrLmiaSpeedDays: Int,
    val legalCounselQuality: String, // "Fragomen LLP (5/5)", "BAL Legal (4.8/5)", "In-House Immigration Team"
    val relocationPackagePaid: String, // "Full ($12,000 + Flight + 60d Airbnb)", "Visa Fees Only", "Self-Funded"
    val prSponsorshipPledge: String, // "Direct PR support after 6 months", "Day-1 PERM", "Requires 2 years tenure"
    val reviewTitle: String,
    val reviewBody: String,
    val authorLabel: String, // "Verified H-1B Engineer", "Verified UK Skilled Worker"
    val datePosted: String,
    val helpfulCount: Int
)

object EmployerReviewsRepository {
    val sampleReviews = listOf(
        EmployerImmigrationReview(
            id = "rev_001",
            companyName = "DeepMind Technologies UK",
            country = "United Kingdom",
            roleTitle = "Senior AI Research Engineer",
            originCountry = "Kenya",
            visaPathway = "Skilled Worker Visa (Fast-track)",
            overallImmigrationRating = 4.9,
            cosOrLmiaSpeedDays = 7,
            legalCounselQuality = "Fragomen LLP UK (Exceptional)",
            relocationPackagePaid = "Full Flights for family + 60 days Kings Cross apartment + £8,000 allowance",
            prSponsorshipPledge = "Guaranteed ILR sponsorship at 5-year milestone with all lawyer fees covered",
            reviewTitle = "Flawless relocation from Nairobi to London in under 3 weeks",
            reviewBody = "Fragomen assigned a dedicated paralegal who reviewed every page of my passport, degrees, and TB test within 24 hours. The defined Certificate of Sponsorship was assigned in 3 days. DeepMind paid for priority visa appointment processing and Heathrow airport pickup.",
            authorLabel = "Verified Skilled Worker Hire",
            datePosted = "Jan 2026",
            helpfulCount = 42
        ),
        EmployerImmigrationReview(
            id = "rev_002",
            companyName = "Shopify Canada Inc.",
            country = "Canada",
            roleTitle = "Staff Backend Engineer",
            originCountry = "Nigeria",
            visaPathway = "Global Talent Stream (GTS)",
            overallImmigrationRating = 4.8,
            cosOrLmiaSpeedDays = 12,
            legalCounselQuality = "PwC Law Canada",
            relocationPackagePaid = "CAD $15,000 lump sum + full family flight tickets + SOWP lawyer fees",
            prSponsorshipPledge = "Express Entry 50-point LMIA support immediately after onboarding",
            reviewTitle = "GTS two-week processing lived up to the hype",
            reviewBody = "They submitted my GTS LMIA under Category B in Ottawa and had it approved in 8 business days. My spouse received her Open Work Permit at the same time. The CAD 15k relocation stipend cleared directly into my account before landing in Toronto.",
            authorLabel = "Verified GTS Applicant",
            datePosted = "Dec 2025",
            helpfulCount = 38
        ),
        EmployerImmigrationReview(
            id = "rev_003",
            companyName = "SAP SE Germany",
            country = "Germany",
            roleTitle = "Cloud Infrastructure Architect",
            originCountry = "India",
            visaPathway = "EU Blue Card (§18g)",
            overallImmigrationRating = 4.7,
            cosOrLmiaSpeedDays = 14,
            legalCounselQuality = "BAL Legal Germany",
            relocationPackagePaid = "€9,000 relocation bonus + German intensive A1/B1 courses for family",
            prSponsorshipPledge = "Settlement permit (Niederlassungserlaubnis) support after 21 months",
            reviewTitle = "Accelerated procedure (§81a) made embassy appointment effortless",
            reviewBody = "SAP pre-approved my degree with ZAB (Central Office for Foreign Education) and obtained the pre-approval letter from the Berlin immigration office (LEA). I got an embassy appointment in Bangalore within 4 days of request.",
            authorLabel = "Verified Blue Card Expat",
            datePosted = "Feb 2026",
            helpfulCount = 29
        ),
        EmployerImmigrationReview(
            id = "rev_004",
            companyName = "ASML Netherlands",
            country = "Netherlands",
            roleTitle = "Optical Systems Architect",
            originCountry = "South Africa",
            visaPathway = "Highly Skilled Migrant (30% Ruling)",
            overallImmigrationRating = 4.9,
            cosOrLmiaSpeedDays = 6,
            legalCounselQuality = "KPMG Meijburg Legal",
            relocationPackagePaid = "Full Container Shipping + Eindhoven Temporary Villa + 30% Ruling Prep",
            prSponsorshipPledge = "5-Year Dutch Permanent Residency legal filings",
            reviewTitle = "IND Recognized portal meant visa was granted in 5 days",
            reviewBody = "Because ASML is an IND Recognized Sponsor, they didn't have to send paper files. The digital approval arrived in less than a week. KPMG prepared the 30% tax ruling application alongside my contract.",
            authorLabel = "Verified Kennismigrant",
            datePosted = "Jan 2026",
            helpfulCount = 31
        )
    )
}

@Composable
fun EmployerRelocationReviewsHub(
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterCountry by remember { mutableStateOf("All Countries") }
    var showSubmitReviewModal by remember { mutableStateOf(false) }

    val countries = listOf("All Countries", "United Kingdom", "Canada", "Germany", "Netherlands")

    val reviews = remember(searchQuery, selectedFilterCountry) {
        EmployerReviewsRepository.sampleReviews.filter { rev ->
            val matchesSearch = searchQuery.isBlank() ||
                    rev.companyName.contains(searchQuery, ignoreCase = true) ||
                    rev.roleTitle.contains(searchQuery, ignoreCase = true) ||
                    rev.reviewTitle.contains(searchQuery, ignoreCase = true)

            val matchesCountry = selectedFilterCountry == "All Countries" || rev.country.equals(selectedFilterCountry, ignoreCase = true)

            matchesSearch && matchesCountry
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("employer_relocation_reviews_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFF9800).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = null,
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Immigration Reviews",
                        color = WhiteActive,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real candidate feedback on CoS speed, legal & relocation perks",
                        color = SlateMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Button(
                onClick = { showSubmitReviewModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = NavyDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Write Review", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter by company, origin country, or law firm...", color = SlateMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldGreen) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = NavyMedium,
                unfocusedContainerColor = NavyMedium,
                focusedBorderColor = EmeraldGreen,
                unfocusedBorderColor = NavyLight,
                focusedTextColor = WhiteActive,
                unfocusedTextColor = WhiteActive
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Country Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(countries) { country ->
                FilterChip(
                    selected = selectedFilterCountry == country,
                    onClick = { selectedFilterCountry = country },
                    label = { Text(country, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldGreen,
                        selectedLabelColor = NavyDark,
                        containerColor = NavyMedium,
                        labelColor = SlateMuted
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (selectedFilterCountry == country) EmeraldGreen else NavyLight,
                        enabled = true,
                        selected = selectedFilterCountry == country
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Reviews List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reviews, key = { it.id }) { review ->
                ImmigrationReviewCard(review = review)
            }
        }
    }

    if (showSubmitReviewModal) {
        SubmitImmigrationReviewDialog(
            onDismiss = { showSubmitReviewModal = false }
        )
    }
}

@Composable
fun ImmigrationReviewCard(
    review: EmployerImmigrationReview,
    modifier: Modifier = Modifier
) {
    var helpfulCount by remember { mutableIntStateOf(review.helpfulCount) }
    var hasLiked by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.companyName,
                        color = WhiteActive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${review.roleTitle} • Relocated from ${review.originCountry} to ${review.country}",
                        color = SlateMuted,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldGreen.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${review.overallImmigrationRating} / 5.0",
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ReviewBadge(text = "CoS: ${review.cosOrLmiaSpeedDays} Days", icon = Icons.Default.Speed)
                ReviewBadge(text = review.visaPathway.take(18), icon = Icons.Default.Verified)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Review Title & Body
            Text(
                text = "\"${review.reviewTitle}\"",
                color = WhiteActive,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = review.reviewBody,
                color = SlateMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Key Relocation Terms
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = NavyDark
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Relocation Package: ${review.relocationPackagePaid}", color = TealCyan, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Text(text = "Legal Firm: ${review.legalCounselQuality}", color = WhiteActive, fontSize = 11.sp)
                    Text(text = "PR / Green Card Policy: ${review.prSponsorshipPledge}", color = SlateMuted, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${review.authorLabel} • ${review.datePosted}",
                    color = SlateMuted,
                    fontSize = 10.sp
                )

                OutlinedButton(
                    onClick = {
                        if (!hasLiked) {
                            helpfulCount++
                            hasLiked = true
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        Icons.Default.ThumbUp,
                        contentDescription = null,
                        tint = if (hasLiked) EmeraldGreen else SlateMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Helpful ($helpfulCount)", fontSize = 10.sp, color = if (hasLiked) EmeraldGreen else SlateMuted)
                }
            }
        }
    }
}

@Composable
private fun ReviewBadge(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = NavyDark
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(11.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = text, color = WhiteActive, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SubmitImmigrationReviewDialog(
    onDismiss: () -> Unit
) {
    var companyName by remember { mutableStateOf("") }
    var roleTitle by remember { mutableStateOf("") }
    var rating by remember { mutableFloatStateOf(5f) }
    var reviewTitle by remember { mutableStateOf("") }
    var reviewBody by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            color = NavyDark
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (!isSubmitted) {
                    Text("Share Immigration & Visa Experience", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("Help fellow international candidates assess sponsor transparency", color = SlateMuted, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Company Name (e.g. DeepMind, Shopify)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NavyMedium,
                            unfocusedContainerColor = NavyMedium,
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = roleTitle,
                        onValueChange = { roleTitle = it },
                        label = { Text("Job Title / Role") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NavyMedium,
                            unfocusedContainerColor = NavyMedium,
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = reviewTitle,
                        onValueChange = { reviewTitle = it },
                        label = { Text("Summary Headline") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NavyMedium,
                            unfocusedContainerColor = NavyMedium,
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = reviewBody,
                        onValueChange = { reviewBody = it },
                        label = { Text("Detailed Immigration Experience (CoS speed, legal help)") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NavyMedium,
                            unfocusedContainerColor = NavyMedium,
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text("Cancel", color = SlateMuted)
                        }
                        Button(
                            onClick = { isSubmitted = true },
                            enabled = companyName.isNotBlank() && reviewBody.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Publish Review", color = NavyDark, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Review Submitted Successfully!", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Your verified feedback is now visible to international jobseekers.", color = SlateMuted, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("Done", color = NavyDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
