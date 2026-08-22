package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

private val SlateLight = Color(0xFF49454F)
private val CoralAccent = Color(0xFFFF5252)


// --- REAL-TIME DATA MODELS ---

data class LiveSponsorshipPulse(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val companyName: String,
    val jobTitle: String,
    val country: String,
    val city: String,
    val visaType: String,
    val salaryRange: String,
    val isFastTrack: Boolean = false,
    val sponsorRating: Double = 4.8,
    val isVerifiedCoS: Boolean = true
)

data class LiveEmbassySlot(
    val id: String,
    val consulateCity: String,
    val country: String,
    val visaCategory: String,
    val earliestSlotDate: String,
    val waitTimeDays: Int,
    val status: EmbassySlotStatus,
    val lastUpdatedSecondsAgo: Int = 12,
    val appointmentDropSchedule: String
)

enum class EmbassySlotStatus {
    SLOTS_OPEN_NOW,
    EXPEDITED_AVAILABLE,
    MODERATE_WAIT,
    LONG_BACKLOG
}

data class LiveExchangeTick(
    val currencyPair: String, // e.g. "GBP/KES", "EUR/INR", "USD/NGN", "CAD/PHP"
    val baseCurrency: String,
    val targetCurrency: String,
    val rate: Double,
    val deltaPercent: Double,
    val isUp: Boolean,
    val high24h: Double,
    val low24h: Double,
    val estimatedFeePercent: Double = 0.45
)

data class LiveCommunityMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderName: String,
    val senderCountryFlag: String,
    val targetCountry: String,
    val visaTrack: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isAiAdvisor: Boolean = false,
    val reactionsCount: Map<String, Int> = emptyMap()
)

data class LiveApplicationMilestone(
    val id: String,
    val applicantName: String,
    val roleTitle: String,
    val companyName: String,
    val country: String,
    val stageName: String,
    val stageDescription: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean,
    val timestampFormatted: String
)

// --- REAL-TIME REPOSITORY & STREAM GENERATOR ---

object RealTimeMobilityRepository {

    val initialPulses = listOf(
        LiveSponsorshipPulse(
            companyName = "Google DeepMind",
            jobTitle = "Staff Research Scientist (AI)",
            country = "United Kingdom",
            city = "London",
            visaType = "Skilled Worker (Fast-Track CoS)",
            salaryRange = "£140,000 - £195,000",
            isFastTrack = true,
            sponsorRating = 4.9
        ),
        LiveSponsorshipPulse(
            companyName = "BioNTech SE",
            jobTitle = "Lead Bioinformatics Engineer",
            country = "Germany",
            city = "Mainz / Berlin",
            visaType = "EU Blue Card (§18g)",
            salaryRange = "€88,000 - €115,000",
            isFastTrack = true,
            sponsorRating = 4.8
        ),
        LiveSponsorshipPulse(
            companyName = "Shopify",
            jobTitle = "Senior Infrastructure Architect",
            country = "Canada",
            city = "Toronto (Remote)",
            visaType = "Global Talent Stream (LMIA-Exempt)",
            salaryRange = "CAD $165,000 - $210,000",
            isFastTrack = true,
            sponsorRating = 4.9
        ),
        LiveSponsorshipPulse(
            companyName = "Atlassian",
            jobTitle = "Principal Cloud Platform Engineer",
            country = "Australia",
            city = "Sydney",
            visaType = "TSS 482 (Medium-Term)",
            salaryRange = "AUD $170,000 - $220,000",
            isFastTrack = true,
            sponsorRating = 4.8
        ),
        LiveSponsorshipPulse(
            companyName = "ASML",
            jobTitle = "Senior EUV Lithography Software Dev",
            country = "Netherlands",
            city = "Veldhoven / Eindhoven",
            visaType = "Kennismigrant (30% Tax Ruling)",
            salaryRange = "€78,000 - €105,000",
            isFastTrack = true,
            sponsorRating = 4.9
        ),
        LiveSponsorshipPulse(
            companyName = "Mercari, Inc.",
            jobTitle = "Backend Architect (Go/Kubernetes)",
            country = "Japan",
            city = "Tokyo (Minato)",
            visaType = "Highly Skilled Professional (HSP)",
            salaryRange = "¥12,000,000 - ¥16,500,000",
            isFastTrack = true,
            sponsorRating = 4.7
        )
    )

    val sampleEmbassySlots = listOf(
        LiveEmbassySlot(
            id = "slot-uk-nbo",
            consulateCity = "Nairobi (VFS Global)",
            country = "United Kingdom",
            visaCategory = "Skilled Worker / Priority CoS",
            earliestSlotDate = "Tomorrow, 09:30 AM",
            waitTimeDays = 2,
            status = EmbassySlotStatus.SLOTS_OPEN_NOW,
            lastUpdatedSecondsAgo = 4,
            appointmentDropSchedule = "Drops daily at 08:00 EAT"
        ),
        LiveEmbassySlot(
            id = "slot-ger-lag",
            consulateCity = "Lagos (German Consulate)",
            country = "Germany",
            visaCategory = "EU Blue Card (§18g Fast Track)",
            earliestSlotDate = "In 4 days (Sep 1)",
            waitTimeDays = 4,
            status = EmbassySlotStatus.EXPEDITED_AVAILABLE,
            lastUpdatedSecondsAgo = 18,
            appointmentDropSchedule = "Drops every Wednesday 09:00 UTC"
        ),
        LiveEmbassySlot(
            id = "slot-can-del",
            consulateCity = "New Delhi (IRCC / VFS)",
            country = "Canada",
            visaCategory = "Global Talent Stream Biometrics",
            earliestSlotDate = "Next Monday, 11:00 AM",
            waitTimeDays = 6,
            status = EmbassySlotStatus.EXPEDITED_AVAILABLE,
            lastUpdatedSecondsAgo = 9,
            appointmentDropSchedule = "Real-time rolling cancellations"
        ),
        LiveEmbassySlot(
            id = "slot-us-mnl",
            consulateCity = "Manila (US Embassy)",
            country = "United States",
            visaCategory = "H-1B Specialty Occupation",
            earliestSlotDate = "In 18 days (Sep 15)",
            waitTimeDays = 18,
            status = EmbassySlotStatus.MODERATE_WAIT,
            lastUpdatedSecondsAgo = 35,
            appointmentDropSchedule = "Drops Fridays 17:00 PHT"
        ),
        LiveEmbassySlot(
            id = "slot-aus-lhr",
            consulateCity = "London (Australian High Commission)",
            country = "Australia",
            visaCategory = "Subclass 482 / 186 Biometrics",
            earliestSlotDate = "Tomorrow, 02:15 PM",
            waitTimeDays = 1,
            status = EmbassySlotStatus.SLOTS_OPEN_NOW,
            lastUpdatedSecondsAgo = 7,
            appointmentDropSchedule = "Instant booking available"
        )
    )

    val sampleFxRates = listOf(
        LiveExchangeTick("GBP/KES", "GBP", "KES", 172.45, 0.28, true, 173.10, 171.80, 0.35),
        LiveExchangeTick("EUR/INR", "EUR", "INR", 91.82, -0.14, false, 92.15, 91.60, 0.40),
        LiveExchangeTick("USD/NGN", "USD", "NGN", 1585.00, 0.75, true, 1595.00, 1570.00, 0.50),
        LiveExchangeTick("CAD/PHP", "CAD", "PHP", 42.60, 0.12, true, 42.85, 42.40, 0.38),
        LiveExchangeTick("AUD/ZAR", "AUD", "ZAR", 12.18, -0.22, false, 12.35, 12.05, 0.45),
        LiveExchangeTick("EUR/USD", "EUR", "USD", 1.0925, 0.05, true, 1.0960, 1.0890, 0.20)
    )

    val sampleCommunityChat = listOf(
        LiveCommunityMessage(
            senderName = "Amara O.",
            senderCountryFlag = "🇳🇬",
            targetCountry = "United Kingdom",
            visaTrack = "Skilled Worker (Tier 2)",
            messageText = "Just received my CoS from DeepMind in London! Process took 8 days total with priority service. Happy to answer any questions about the tech interview rounds!"
        ),
        LiveCommunityMessage(
            senderName = "VisaGo AI Advisor",
            senderCountryFlag = "🤖",
            targetCountry = "Global",
            visaTrack = "Immigration Compliance",
            messageText = "Tip for UK applicants: Ensure your Certificate of Sponsorship (CoS) has the correct SOC code (e.g. 2136 for Programmers). The minimum salary threshold is £38,700 for standard route, or £30,960 for New Entrants.",
            isAiAdvisor = true
        ),
        LiveCommunityMessage(
            senderName = "Devendra P.",
            senderCountryFlag = "🇮🇳",
            targetCountry = "Germany",
            visaTrack = "EU Blue Card",
            messageText = "Anyone heading to Berlin on the EU Blue Card? The §18g Fast Track approval from the Foreigners Authority (LEA) took exactly 14 calendar days!"
        ),
        LiveCommunityMessage(
            senderName = "Carlos M.",
            senderCountryFlag = "🇧🇷",
            targetCountry = "Canada",
            visaTrack = "Global Talent Stream",
            messageText = "LMIA-exempt Category A processing through IRCC Toronto was stamped today in São Paulo! Flight booked for October."
        )
    )
}

// --- MAIN REAL-TIME MOBILITY HUB COMPOSABLE ---

@Composable
fun RealTimeMobilityHub(
    onNavigateToJobSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedSection by remember { mutableStateOf("LiveRadar") } // LiveRadar, EmbassySlots, LiveFX, CommunityChat, Telemetry

    // Live Stream States
    val livePulses = remember { mutableStateListOf<LiveSponsorshipPulse>().apply { addAll(RealTimeMobilityRepository.initialPulses) } }
    var isLiveStreamActive by remember { mutableStateOf(true) }
    var selectedCountryFilter by remember { mutableStateOf("All") }

    // Live Embassy Slot Sniping States
    val embassySlots = remember { mutableStateListOf<LiveEmbassySlot>().apply { addAll(RealTimeMobilityRepository.sampleEmbassySlots) } }
    var isSlotSnipingEnabled by remember { mutableStateOf(true) }
    var lastSlotAlertMessage by remember { mutableStateOf<String?>(null) }

    // Live FX Rates States
    val fxRates = remember { mutableStateListOf<LiveExchangeTick>().apply { addAll(RealTimeMobilityRepository.sampleFxRates) } }
    var lastFxUpdateTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Live Community Chat States
    val chatMessages = remember { mutableStateListOf<LiveCommunityMessage>().apply { addAll(RealTimeMobilityRepository.sampleCommunityChat) } }
    var currentChatInput by remember { mutableStateOf("") }
    var isAdvisorTyping by remember { mutableStateOf(false) }

    // Real-time background simulation tick loop
    LaunchedEffect(isLiveStreamActive) {
        if (isLiveStreamActive) {
            val companies = listOf(
                "Stripe London" to ("United Kingdom" to "Skilled Worker (Fast-Track CoS)"),
                "Zalando Berlin" to ("Germany" to "EU Blue Card"),
                "Amazon Vancouver" to ("Canada" to "Global Talent Stream"),
                "Canva Sydney" to ("Australia" to "Subclass 482"),
                "Adyen Amsterdam" to ("Netherlands" to "Kennismigrant (30% Ruling)"),
                "Rakuten Tokyo" to ("Japan" to "Highly Skilled Professional"),
                "Revolut London" to ("United Kingdom" to "Skilled Worker"),
                "Klarna Stockholm" to ("Sweden" to "Work Permit Fast-Track")
            )
            val titles = listOf(
                "Senior Backend Engineer (Go/Rust)",
                "Staff ML Research Engineer",
                "Full Stack Cloud Architect",
                "Principal SRE / Kubernetes",
                "Senior iOS / Android Engineer",
                "Data Platform Lead"
            )
            val salaries = listOf(
                "£95,000 - £130,000",
                "€85,000 - €110,000",
                "CAD $150,000 - $185,000",
                "AUD $160,000 - $195,000",
                "€80,000 - €105,000",
                "¥11,000,000 - ¥15,000,000"
            )

            while (isActive) {
                delay(7000) // New live event every 7 seconds
                val randCompany = companies.random()
                val newPulse = LiveSponsorshipPulse(
                    companyName = randCompany.first,
                    jobTitle = titles.random(),
                    country = randCompany.second.first,
                    city = randCompany.first.split(" ").last(),
                    visaType = randCompany.second.second,
                    salaryRange = salaries.random(),
                    isFastTrack = Random.nextBoolean(),
                    sponsorRating = 4.6 + (Random.nextInt(4) / 10.0)
                )
                livePulses.add(0, newPulse)
                if (livePulses.size > 25) {
                    livePulses.removeAt(livePulses.size - 1)
                }

                // Micro-fluctuate FX rates
                for (i in 0 until fxRates.size) {
                    val current = fxRates[i]
                    val delta = (Random.nextDouble(-0.15, 0.18))
                    val newRate = current.rate * (1.0 + (delta / 100.0))
                    val newDeltaPercent = current.deltaPercent + delta
                    fxRates[i] = current.copy(
                        rate = (newRate * 100).toInt() / 100.0,
                        deltaPercent = (newDeltaPercent * 100).toInt() / 100.0,
                        isUp = delta >= 0
                    )
                }
                lastFxUpdateTime = System.currentTimeMillis()
            }
        }
    }

    Scaffold(
        containerColor = NavyDark,
        contentColor = WhiteActive
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header with Pulsing Live Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pulsing Red/Green Live Radar Orb
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.85f,
                        targetValue = 1.25f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(900, easing = EaseInOutQuad),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF5252).copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp * pulseScale)
                                .clip(CircleShape)
                                .background(Color(0xFFFF5252))
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "LIVE GLOBAL MOBILITY RADAR",
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    color = if (isLiveStreamActive) EmeraldGreen.copy(alpha = 0.2f) else NavyLight,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isLiveStreamActive) EmeraldGreen else SlateMuted),
                    modifier = Modifier.clickable { isLiveStreamActive = !isLiveStreamActive }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLiveStreamActive) Icons.Default.Sensors else Icons.Default.SensorsOff,
                            contentDescription = null,
                            tint = if (isLiveStreamActive) EmeraldGreen else SlateMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isLiveStreamActive) "Streaming Live" else "Paused",
                            color = if (isLiveStreamActive) EmeraldGreen else SlateMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle & Metrics Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyMedium, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = TealCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Telemetry: Sub-second CoS & Slot Feed", color = SlateLight, fontSize = 11.sp)
                }

                Text(
                    text = "${livePulses.size} Live Events",
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section Selector Chips (Horizontal Scrollable)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val sections = listOf(
                    Triple("LiveRadar", "📡 Sponsorship Radar", Color(0xFFFF5252)),
                    Triple("EmbassySlots", "🏛️ Embassy Slot Sniper", TealCyan),
                    Triple("LiveFX", "💱 Real-Time FX Ticker", EmeraldGreen),
                    Triple("CommunityChat", "💬 Live Relocation Lounge", Color(0xFFCE93D8)),
                    Triple("Telemetry", "⚡ Status Telemetry", Color(0xFFFFB74D))
                )

                items(sections) { (key, label, accentColor) ->
                    val isSelected = selectedSection == key
                    Surface(
                        color = if (isSelected) accentColor.copy(alpha = 0.22f) else NavyMedium,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isSelected) accentColor else NavyLight),
                        modifier = Modifier.clickable { selectedSection = key }
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) WhiteActive else SlateLight,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content Area based on Selected Section
            AnimatedContent(
                targetState = selectedSection,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label = "RealTimeHubSection"
            ) { section ->
                when (section) {
                    "LiveRadar" -> LiveSponsorshipRadarSection(
                        livePulses = livePulses,
                        selectedCountryFilter = selectedCountryFilter,
                        onCountryFilterChange = { selectedCountryFilter = it },
                        onApplyOrSearch = onNavigateToJobSearch
                    )
                    "EmbassySlots" -> LiveEmbassySlotSniperSection(
                        slots = embassySlots,
                        isSnipingEnabled = isSlotSnipingEnabled,
                        onToggleSniping = { isSlotSnipingEnabled = it },
                        lastAlert = lastSlotAlertMessage,
                        onTriggerInstantCheck = {
                            coroutineScope.launch {
                                lastSlotAlertMessage = "⚡ Checked 14 consulates: New priority slot open in Nairobi & London VFS!"
                                delay(6000)
                                lastSlotAlertMessage = null
                            }
                        }
                    )
                    "LiveFX" -> LiveFxRemittanceSection(
                        fxRates = fxRates,
                        lastUpdateTime = lastFxUpdateTime
                    )
                    "CommunityChat" -> LiveRelocationChatSection(
                        messages = chatMessages,
                        currentInput = currentChatInput,
                        onInputChange = { currentChatInput = it },
                        isTyping = isAdvisorTyping,
                        onSendMessage = { text ->
                            if (text.isNotBlank()) {
                                chatMessages.add(
                                    LiveCommunityMessage(
                                        senderName = "You (Verified Expat)",
                                        senderCountryFlag = "🌍",
                                        targetCountry = "Global Mobility",
                                        visaTrack = "Candidate",
                                        messageText = text
                                    )
                                )
                                currentChatInput = ""

                                // Trigger automated instant AI Counsel reply
                                coroutineScope.launch {
                                    isAdvisorTyping = true
                                    delay(2000)
                                    isAdvisorTyping = false
                                    chatMessages.add(
                                        LiveCommunityMessage(
                                            senderName = "VisaGo AI Legal Agent",
                                            senderCountryFlag = "🤖",
                                            targetCountry = "Global",
                                            visaTrack = "Official Guidelines",
                                            messageText = "Great query! For '${text.take(30)}...', always verify that the employer holds an active A-rated sponsor license before signing. You can check the Official Sponsor Registry tab for 5-year filing history.",
                                            isAiAdvisor = true
                                        )
                                    )
                                }
                            }
                        }
                    )
                    "Telemetry" -> LiveApplicationTelemetrySection()
                }
            }
        }
    }
}

// --- SUB-SECTION 1: LIVE SPONSORSHIP RADAR ---

@Composable
fun LiveSponsorshipRadarSection(
    livePulses: List<LiveSponsorshipPulse>,
    selectedCountryFilter: String,
    onCountryFilterChange: (String) -> Unit,
    onApplyOrSearch: (String) -> Unit
) {
    val countries = listOf("All", "United Kingdom", "Germany", "Canada", "Australia", "Netherlands", "Japan")
    val filteredPulses = if (selectedCountryFilter == "All") livePulses else livePulses.filter { it.country == selectedCountryFilter }

    Column(modifier = Modifier.fillMaxSize()) {
        // Country filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            items(countries) { country ->
                val isSelected = selectedCountryFilter == country
                FilterChip(
                    selected = isSelected,
                    onClick = { onCountryFilterChange(country) },
                    label = { Text(country, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldGreen,
                        selectedLabelColor = NavyDark,
                        containerColor = NavyMedium,
                        labelColor = SlateLight
                    )
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredPulses, key = { it.id }) { pulse ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavyMedium),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (pulse.isFastTrack) EmeraldGreen.copy(alpha = 0.5f) else NavyLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = pulse.companyName,
                                        color = WhiteActive,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (pulse.isVerifiedCoS) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Verified Sponsor",
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = pulse.jobTitle,
                                    color = SlateLight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Surface(
                                color = NavyDark,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, NavyLight)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = CoralAccent, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("${pulse.city}, ${pulse.country}", color = WhiteActive, fontSize = 10.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Visa & Salary Tags
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = TealCyan.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, TealCyan.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = pulse.visaType,
                                    color = TealCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Text(
                                text = pulse.salaryRange,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏱️ Logged ${getRelativeTimeString(pulse.timestamp)}",
                                color = SlateMuted,
                                fontSize = 10.sp
                            )

                            Button(
                                onClick = { onApplyOrSearch(pulse.companyName) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Find Jobs", color = NavyDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = NavyDark, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-SECTION 2: LIVE EMBASSY & VFS APPOINTMENT SLOT SNIPER ---

@Composable
fun LiveEmbassySlotSniperSection(
    slots: List<LiveEmbassySlot>,
    isSnipingEnabled: Boolean,
    onToggleSniping: (Boolean) -> Unit,
    lastAlert: String?,
    onTriggerInstantCheck: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Watchdog Status Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isSnipingEnabled) NavyMedium else NavyDark
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isSnipingEnabled) TealCyan else SlateMuted),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Radar, contentDescription = null, tint = TealCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Consulate Slot Watchdog", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text("Continuous background monitoring for cancelled & emergency appointment slots", color = SlateLight, fontSize = 11.sp)
                    }

                    Switch(
                        checked = isSnipingEnabled,
                        onCheckedChange = onToggleSniping,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NavyDark,
                            checkedTrackColor = TealCyan,
                            uncheckedThumbColor = SlateMuted,
                            uncheckedTrackColor = NavyLight
                        )
                    )
                }

                if (lastAlert != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = EmeraldGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, EmeraldGreen)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(lastAlert, color = WhiteActive, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onTriggerInstantCheck,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, TealCyan)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = TealCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Instant Consulate Ping & Slot Scan", color = TealCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Slots List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(slots, key = { it.id }) { slot ->
                val statusColor = when (slot.status) {
                    EmbassySlotStatus.SLOTS_OPEN_NOW -> EmeraldGreen
                    EmbassySlotStatus.EXPEDITED_AVAILABLE -> TealCyan
                    EmbassySlotStatus.MODERATE_WAIT -> Color(0xFFFFB74D)
                    EmbassySlotStatus.LONG_BACKLOG -> Color(0xFFFF5252)
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = NavyMedium),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(slot.consulateCity, color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${slot.country} • ${slot.visaCategory}", color = SlateLight, fontSize = 12.sp)
                            }

                            Surface(
                                color = statusColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, statusColor)
                            ) {
                                Text(
                                    text = slot.status.name.replace("_", " "),
                                    color = statusColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EventAvailable, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Earliest: ${slot.earliestSlotDate}", color = WhiteActive, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Text("${slot.waitTimeDays}d Wait", color = SlateLight, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "🔄 ${slot.appointmentDropSchedule}",
                            color = SlateMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

// --- SUB-SECTION 3: LIVE FX REMITTANCE & CURRENCY TICKER ---

@Composable
fun LiveFxRemittanceSection(
    fxRates: List<LiveExchangeTick>,
    lastUpdateTime: Long
) {
    var selectedPair by remember { mutableStateOf(fxRates.firstOrNull()?.currencyPair ?: "GBP/KES") }
    var sendAmountText by remember { mutableStateOf("1000") }

    val activeTick = fxRates.find { it.currencyPair == selectedPair } ?: fxRates.first()
    val sendAmount = sendAmountText.toDoubleOrNull() ?: 1000.0
    val receivedAmount = sendAmount * activeTick.rate * (1.0 - (activeTick.estimatedFeePercent / 100.0))

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Live Rates Ticker Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(fxRates) { tick ->
                val isSelected = tick.currencyPair == selectedPair
                Surface(
                    color = if (isSelected) EmeraldGreen.copy(alpha = 0.2f) else NavyMedium,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isSelected) EmeraldGreen else NavyLight),
                    modifier = Modifier.clickable { selectedPair = tick.currencyPair }
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                        Text(tick.currencyPair, color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(String.format(Locale.US, "%.2f", tick.rate), color = WhiteActive, fontSize = 13.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = (if (tick.isUp) "+" else "") + String.format(Locale.US, "%.2f%%", tick.deltaPercent),
                                color = if (tick.isUp) EmeraldGreen else Color(0xFFFF5252),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Remittance Conversion Box
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Live Cross-Border Remittance", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("🟢 Micro-ticks live", color = EmeraldGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input Send Amount
                OutlinedTextField(
                    value = sendAmountText,
                    onValueChange = { sendAmountText = it },
                    label = { Text("You Send (${activeTick.baseCurrency})", color = SlateLight, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Conversion Output Card
                Surface(
                    color = NavyDark,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, NavyLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Recipient Receives (${activeTick.targetCurrency}):", color = SlateLight, fontSize = 11.sp)
                        Text(
                            text = String.format(Locale.US, "%,.2f %s", receivedAmount, activeTick.targetCurrency),
                            color = EmeraldGreen,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rate: 1 ${activeTick.baseCurrency} = ${String.format(Locale.US, "%.4f", activeTick.rate)} ${activeTick.targetCurrency} • Est. Fee: ${activeTick.estimatedFeePercent}%",
                            color = SlateMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 24h High / Low info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("24h Low: ${activeTick.low24h}", color = SlateMuted, fontSize = 10.sp)
                    Text("24h High: ${activeTick.high24h}", color = SlateMuted, fontSize = 10.sp)
                }
            }
        }
    }
}

// --- SUB-SECTION 4: LIVE COMMUNITY RELOCATION CHAT ---

@Composable
fun LiveRelocationChatSection(
    messages: List<LiveCommunityMessage>,
    currentInput: String,
    onInputChange: (String) -> Unit,
    isTyping: Boolean,
    onSendMessage: (String) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Channel Bar
        Surface(
            color = NavyMedium,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, NavyLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🌍 #global-sponsorship-lounge", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Text("🟢 164 Expats Online", color = EmeraldGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Messages List
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(messages, key = { it.id }) { msg ->
                val isAi = msg.isAiAdvisor
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAi) TealCyan.copy(alpha = 0.12f) else NavyMedium
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isAi) TealCyan.copy(alpha = 0.5f) else NavyLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(msg.senderCountryFlag, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(msg.senderName, color = if (isAi) TealCyan else WhiteActive, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Text(msg.visaTrack, color = SlateMuted, fontSize = 10.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(msg.messageText, color = SlateLight, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }

            if (isTyping) {
                item {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = TealCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("VisaGo AI Immigration Agent is typing...", color = TealCyan, fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Message Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = currentInput,
                onValueChange = onInputChange,
                placeholder = { Text("Ask relocation community or AI...", color = SlateMuted, fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = WhiteActive,
                    unfocusedTextColor = WhiteActive,
                    focusedBorderColor = EmeraldGreen,
                    unfocusedBorderColor = NavyLight
                ),
                maxLines = 2
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { onSendMessage(currentInput) },
                modifier = Modifier
                    .size(48.dp)
                    .background(EmeraldGreen, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = NavyDark)
            }
        }
    }
}

// --- SUB-SECTION 5: LIVE APPLICATION TELEMETRY ---

@Composable
fun LiveApplicationTelemetrySection() {
    val milestones = remember {
        listOf(
            LiveApplicationMilestone("m1", "Vincent M.", "Staff Infrastructure Engineer", "Google DeepMind", "United Kingdom", "CoS Assignment", "Certificate of Sponsorship issued by Home Office SMS portal (Ref: W092-2810)", true, false, "Aug 20, 14:15"),
            LiveApplicationMilestone("m2", "Vincent M.", "Staff Infrastructure Engineer", "Google DeepMind", "United Kingdom", "VFS Biometrics Scan", "Fingerprints and passport handed over at VFS Nairobi", true, false, "Aug 21, 09:30"),
            LiveApplicationMilestone("m3", "Vincent M.", "Staff Infrastructure Engineer", "Google DeepMind", "United Kingdom", "UKVI Decision Making", "Application currently with UK Visas & Immigration Consular Officer", false, true, "In Progress (Est. 48h)"),
            LiveApplicationMilestone("m4", "Vincent M.", "Staff Infrastructure Engineer", "Google DeepMind", "United Kingdom", "Passport Dispatch & Vignette", "Passport stamped with 90-day travel vignette ready for courier dispatch", false, false, "Pending Decision")
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Live Immigration Case Telemetry", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Active Case: UK Skilled Worker Fast-Track (DeepMind London)", color = SlateLight, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(milestones, key = { it.id }) { milestone ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (milestone.isCurrent) NavyMedium else NavyDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        if (milestone.isCompleted) EmeraldGreen else if (milestone.isCurrent) TealCyan else NavyLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = if (milestone.isCompleted) Icons.Default.CheckCircle else if (milestone.isCurrent) Icons.Default.HourglassTop else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (milestone.isCompleted) EmeraldGreen else if (milestone.isCurrent) TealCyan else SlateMuted,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(milestone.stageName, color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(milestone.timestampFormatted, color = if (milestone.isCurrent) TealCyan else SlateMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(milestone.stageDescription, color = SlateLight, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- HELPER TIME FORMATTER ---

fun getRelativeTimeString(timestamp: Long): String {
    val diff = (System.currentTimeMillis() - timestamp) / 1000
    return when {
        diff < 5 -> "just now"
        diff < 60 -> "${diff}s ago"
        diff < 3600 -> "${diff / 60}m ago"
        else -> "${diff / 3600}h ago"
    }
}

// --- TOP APP BAR LIVE RADAR TICKER WIDGET ---

@Composable
fun LiveRadarTopTickerBanner(
    onClickOpenHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liveItems = remember {
        listOf(
            "🔴 LIVE: DeepMind London posted Staff AI Researcher with Tier 2 CoS",
            "⚡ 2 VFS Nairobi priority slots opened for UK Skilled Worker",
            "💱 GBP/KES live rate reached 172.45 (+0.28%)",
            "🇨🇦 Shopify Toronto approved 3 Global Talent Stream LMIA exemptions",
            "🇩🇪 Berlin Foreigners Authority (LEA) issued 14-day Blue Card (§18g)",
            "🟢 164 Expats online in the Live Relocation Lounge"
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(4000)
            currentIndex = (currentIndex + 1) % liveItems.size
        }
    }

    Surface(
        color = NavyMedium,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClickOpenHub() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5252))
            )
            Spacer(modifier = Modifier.width(8.dp))
            AnimatedContent(
                targetState = liveItems[currentIndex],
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn()).togetherWith(
                        slideOutVertically { height -> -height } + fadeOut()
                    )
                },
                label = "TickerScroll",
                modifier = Modifier.weight(1f)
            ) { text ->
                Text(
                    text = text,
                    color = WhiteActive,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Open Real-time Hub",
                tint = EmeraldGreen,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

