package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*

data class ExpatMentor(
    val id: String,
    val name: String,
    val currentRole: String,
    val company: String,
    val country: String,
    val flag: String,
    val city: String,
    val originCountry: String,
    val visaPathway: String,
    val relocationYear: String,
    val rating: Double,
    val reviewsCount: Int,
    val bio: String,
    val specialties: List<String>,
    val availableHours: String
)

data class CommunityThread(
    val id: String,
    val authorName: String,
    val authorBadge: String,
    val countryTag: String,
    val title: String,
    val content: String,
    val upvotes: Int,
    val repliesCount: Int,
    val timeAgo: String
)

object ExpatMentorshipDirectory {
    val mentors = listOf(
        ExpatMentor(
            id = "mentor_01",
            name = "Samuel Karanja",
            currentRole = "Staff Distributed Systems Engineer",
            company = "Revolut",
            country = "United Kingdom",
            flag = "🇬🇧",
            city = "London",
            originCountry = "Kenya",
            visaPathway = "UK Skilled Worker (Fast-Track CoS)",
            relocationYear = "2022",
            rating = 4.9,
            reviewsCount = 48,
            bio = "Relocated from Nairobi to London under the UK Skilled Worker route. Helped 30+ engineers prepare their technical interview portfolios, evaluate CoS sponsorship packages, and find family housing in Zone 2/3.",
            specialties = listOf("UK Skilled Worker CoS", "NHS/Fintech Sponsors", "Salary Negotiation", "UK Housing Setup"),
            availableHours = "Evenings & Weekends (GMT)"
        ),
        ExpatMentor(
            id = "mentor_02",
            name = "Dr. Priya Nair",
            currentRole = "Senior Data Scientist & AI Researcher",
            company = "Shopify",
            country = "Canada",
            flag = "🇨🇦",
            city = "Toronto",
            originCountry = "India",
            visaPathway = "Canada Global Skills Strategy (2-Week LMIA)",
            relocationYear = "2021",
            rating = 5.0,
            reviewsCount = 62,
            bio = "Secured Global Talent Stream LMIA work permit in 14 days and transitioned to Canadian Permanent Residence via Express Entry CEC. Deep expertise in WES credential recognition and tech interviews.",
            specialties = listOf("Global Talent Stream", "Express Entry / CEC", "WES Credential Prep", "Ontario Tech Hubs"),
            availableHours = "Saturday Mornings (EST)"
        ),
        ExpatMentor(
            id = "mentor_03",
            name = "Marcus Weber-Okafor",
            currentRole = "Principal Backend Architect",
            company = "Delivery Hero / SAP",
            country = "Germany",
            flag = "🇩🇪",
            city = "Berlin",
            originCountry = "Nigeria",
            visaPathway = "Germany EU Blue Card (§ 18b)",
            relocationYear = "2020",
            rating = 4.9,
            reviewsCount = 37,
            bio = "Guided over 40 candidates through the German consulate visa appointment process, Anabin degree recognition, Anmeldung registration, and English-speaking software team placements in Berlin & Munich.",
            specialties = listOf("EU Blue Card Fast-Track", "Anabin Degree Equivalence", "Berlin Housing Anmeldung", "German Tax Brackets"),
            availableHours = "Sundays 3 PM - 7 PM (CET)"
        ),
        ExpatMentor(
            id = "mentor_04",
            name = "Grace Achieng",
            currentRole = "Lead Clinical Nurse Specialist",
            company = "Monash Health",
            country = "Australia",
            flag = "🇦🇺",
            city = "Melbourne",
            originCountry = "Uganda",
            visaPathway = "Australia TSS 482 -> 186 PR",
            relocationYear = "2023",
            rating = 4.8,
            reviewsCount = 29,
            bio = "Successfully navigated AHPRA nursing registration, hospital visa sponsorship, and Melbourne settlement. Passionate about guiding healthcare workers and engineers through the Australian points system.",
            specialties = listOf("AHPRA / Healthcare Sponsorship", "TSS 482 Skills Assessment", "Melbourne Schooling", "Superannuation"),
            availableHours = "Weekdays 6 PM - 9 PM (AEST)"
        ),
        ExpatMentor(
            id = "mentor_05",
            name = "David Chen-Mwangi",
            currentRole = "Senior iOS Infrastructure Lead",
            company = "Stripe",
            country = "United States",
            flag = "🇺🇸",
            city = "San Francisco",
            originCountry = "Kenya",
            visaPathway = "US H-1B & O-1A Extraordinary Ability",
            relocationYear = "2021",
            rating = 5.0,
            reviewsCount = 54,
            bio = "Mastered the O-1A criteria (open-source contributions, high remuneration, judge of others' work) and H-1B consular stamping in Nairobi. Mentors top 1% engineers aiming for Silicon Valley sponsors.",
            specialties = listOf("O-1A Extraordinary Criteria", "H-1B Stamping Strategy", "Consular Interview Prep", "Bay Area Relocation"),
            availableHours = "Fridays & Saturdays (PST)"
        )
    )

    val sampleThreads = listOf(
        CommunityThread(
            id = "th_01",
            authorName = "Samuel K. (Verified UK Expat)",
            authorBadge = "🇬🇧 London Mentor",
            countryTag = "UK Skilled Worker",
            title = "PSA: UK Home Office priority visa processing times for Q3 2026",
            content = "For anyone waiting on their Certificate of Sponsorship (CoS), standard priority decisions at TLScontact Nairobi & Lagos are averaging 6 working days instead of 15. Make sure your Tuberculosis test certificate is from an approved IOM clinic or you will get delayed!",
            upvotes = 42,
            repliesCount = 18,
            timeAgo = "2h ago"
        ),
        CommunityThread(
            id = "th_02",
            authorName = "Priya N. (Canada GTS)",
            authorBadge = "🇨🇦 Toronto Mentor",
            countryTag = "Canada LMIA",
            title = "How to answer the LMIA recruitment fee question at the border",
            content = "CBSA border officers at Pearson Airport are cracking down on recruitment agencies. Remember: Canadian law strictly prohibits employers from charging candidates any LMIA or relocation processing fees. Keep your signed zero-fee letter handy!",
            upvotes = 67,
            repliesCount = 24,
            timeAgo = "5h ago"
        ),
        CommunityThread(
            id = "th_03",
            authorName = "Marcus W. (Berlin Expat)",
            authorBadge = "🇩🇪 Germany Blue Card",
            countryTag = "Germany EU Blue Card",
            title = "Anabin H+ University status check shortcut for IT professionals",
            content = "If your university is listed as H+ but your specific degree major is not listed, you only need the ZAB Statement of Comparability. Don't let recruiters tell you that you need German language B2 if you earn over €45.3k gross!",
            upvotes = 38,
            repliesCount = 11,
            timeAgo = "1d ago"
        )
    )
}

@Composable
fun ExpatMentorshipCommunityHub(
    viewModel: JobViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCountryFilter by remember { mutableStateOf("All") }
    var activeSubTab by remember { mutableStateOf("Mentors") } // "Mentors" vs "Community Feed"
    var selectedMentorForBooking by remember { mutableStateOf<ExpatMentor?>(null) }
    var showNewThreadDialog by remember { mutableStateOf(false) }

    val countries = listOf("All", "United Kingdom", "Canada", "Germany", "Australia", "United States")

    val filteredMentors = if (selectedCountryFilter == "All") {
        ExpatMentorshipDirectory.mentors
    } else {
        ExpatMentorshipDirectory.mentors.filter { it.country == selectedCountryFilter }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("expat_mentorship_community_hub")
    ) {
        // Hub Top Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TealCyan.copy(alpha = 0.15f))
                    .border(1.dp, TealCyan, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = TealCyan,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Verified Expat Mentorship & Community",
                    color = WhiteActive,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Connect 1-on-1 with relocated professionals who cracked global visa sponsorship",
                    color = SlateMuted,
                    fontSize = 11.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sub Tab Switcher: Mentors Directory vs Community Discussions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyMedium, shape = RoundedCornerShape(10.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                Pair("Mentors", "Verified Mentors (${filteredMentors.size})"),
                Pair("Community", "Relocation AMA & Peer Feed")
            ).forEach { (tabKey, tabTitle) ->
                val isSelected = activeSubTab == tabKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) EmeraldGreen else Color.Transparent)
                        .clickable { activeSubTab = tabKey }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabTitle,
                        color = if (isSelected) NavyDark else WhiteActive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Country Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(countries) { country ->
                val isSelected = selectedCountryFilter == country
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCountryFilter = country },
                    label = {
                        val flag = when (country) {
                            "United Kingdom" -> "🇬🇧 "
                            "Canada" -> "🇨🇦 "
                            "Germany" -> "🇩🇪 "
                            "Australia" -> "🇦🇺 "
                            "United States" -> "🇺🇸 "
                            else -> "🌍 "
                        }
                        Text("$flag$country", fontSize = 11.sp)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldGreen,
                        selectedLabelColor = NavyDark,
                        containerColor = NavyMedium,
                        labelColor = SlateMuted
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        selectedBorderColor = EmeraldGreen,
                        borderColor = NavyLight
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content Area
        if (activeSubTab == "Mentors") {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredMentors) { mentor ->
                    ExpatMentorCard(
                        mentor = mentor,
                        onBookSession = { selectedMentorForBooking = mentor }
                    )
                }
            }
        } else {
            // Community Discussions Feed
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Relocation Threads & Advice",
                        color = WhiteActive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    TextButton(
                        onClick = { showNewThreadDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = EmeraldGreen)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Post Question", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(ExpatMentorshipDirectory.sampleThreads) { thread ->
                        CommunityThreadCard(thread = thread)
                    }
                }
            }
        }
    }

    // Booking Dialog
    selectedMentorForBooking?.let { mentor ->
        BookMentorshipSessionDialog(
            mentor = mentor,
            onDismiss = { selectedMentorForBooking = null },
            onConfirmBooking = { topic, date ->
                Toast.makeText(context, "Mentorship Request sent to ${mentor.name}! They will confirm via email.", Toast.LENGTH_LONG).show()
                selectedMentorForBooking = null
            }
        )
    }

    // New Question Thread Dialog
    if (showNewThreadDialog) {
        PostCommunityThreadDialog(
            onDismiss = { showNewThreadDialog = false },
            onPost = { title, content ->
                Toast.makeText(context, "Question posted to the global expat community feed!", Toast.LENGTH_SHORT).show()
                showNewThreadDialog = false
            }
        )
    }
}

@Composable
fun ExpatMentorCard(
    mentor: ExpatMentor,
    onBookSession: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NavyLight, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Row 1: Mentor Name, Flag, Country, and Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen.copy(alpha = 0.2f))
                        .border(1.5.dp, EmeraldGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mentor.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                        color = EmeraldGreen,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = mentor.name,
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = mentor.flag, fontSize = 14.sp)
                    }
                    Text(
                        text = "${mentor.currentRole} at ${mentor.company}",
                        color = EmeraldGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${mentor.city}, ${mentor.country} • Relocated ${mentor.relocationYear} from ${mentor.originCountry}",
                        color = SlateMuted,
                        fontSize = 10.5.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = AmberGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "${mentor.rating}", color = AmberGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text(text = "(${mentor.reviewsCount} reviews)", color = SlateMuted, fontSize = 9.5.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Verified Visa Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TealCyan.copy(alpha = 0.12f))
                    .border(1.dp, TealCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = TealCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Verified Pathway: ${mentor.visaPathway}",
                        color = TealCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mentor.bio,
                color = WhiteActive.copy(alpha = 0.9f),
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Specialties tags
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                mentor.specialties.take(3).forEach { spec ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NavyDark)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(text = spec, color = SlateMuted, fontSize = 9.5.sp)
                    }
                }
            }

            // Booking CTA Button
            Button(
                onClick = onBookSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("btn_book_mentor_${mentor.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = NavyDark, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Book 1-on-1 Visa & Relocation Advice", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun CommunityThreadCard(thread: CommunityThread) {
    val context = LocalContext.current
    var upvoteCount by remember { mutableIntStateOf(thread.upvotes) }
    var hasUpvoted by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NavyLight, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = thread.authorBadge, color = TealCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "• ${thread.timeAgo}", color = SlateMuted, fontSize = 10.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NavyDark)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = thread.countryTag, color = AmberGold, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = thread.title,
                color = WhiteActive,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = thread.content,
                color = SlateMuted,
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (!hasUpvoted) {
                                upvoteCount += 1
                                hasUpvoted = true
                            } else {
                                upvoteCount -= 1
                                hasUpvoted = false
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Upvote",
                            tint = if (hasUpvoted) EmeraldGreen else SlateMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(text = "$upvoteCount upvotes", color = if (hasUpvoted) EmeraldGreen else SlateMuted, fontSize = 11.sp)
                }

                TextButton(onClick = {
                    Toast.makeText(context, "Opening replies thread...", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${thread.repliesCount} Answers", color = EmeraldGreen, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun BookMentorshipSessionDialog(
    mentor: ExpatMentor,
    onDismiss: () -> Unit,
    onConfirmBooking: (String, String) -> Unit
) {
    var topic by remember { mutableStateOf("Visa sponsorship review & employer negotiation") }
    var targetDate by remember { mutableStateOf("This Saturday 4:00 PM") }
    var additionalNotes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NavyLight, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(mentor.flag, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Book Session with ${mentor.name}",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "${mentor.currentRole} • ${mentor.city}",
                            color = EmeraldGreen,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Primary Advice Topic:", color = WhiteActive, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight,
                        focusedContainerColor = NavyDark,
                        unfocusedContainerColor = NavyDark
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Preferred Day & Time (${mentor.availableHours}):", color = WhiteActive, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight,
                        focusedContainerColor = NavyDark,
                        unfocusedContainerColor = NavyDark
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Specific Questions / Context:", color = WhiteActive, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = additionalNotes,
                    onValueChange = { additionalNotes = it },
                    placeholder = { Text("e.g. Received a UK offer at £55k, need advice on CoS priority timing...", color = SlateMuted, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight,
                        focusedContainerColor = NavyDark,
                        unfocusedContainerColor = NavyDark
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = SlateMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirmBooking(topic, targetDate) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Confirm Booking Request", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PostCommunityThreadDialog(
    onDismiss: () -> Unit,
    onPost: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NavyLight, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Ask the Expat Community",
                    color = WhiteActive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Get real answers on visa wait times, sponsor vetting, and moving logistics",
                    color = SlateMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Question Title (e.g. Canada LMIA processing times)", color = SlateMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight,
                        focusedContainerColor = NavyDark,
                        unfocusedContainerColor = NavyDark
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Details & Situation", color = SlateMuted) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight,
                        focusedContainerColor = NavyDark,
                        unfocusedContainerColor = NavyDark
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = SlateMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) onPost(title, content)
                        },
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Publish Question", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
