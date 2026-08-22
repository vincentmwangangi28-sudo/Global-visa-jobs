package com.example.ui

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.UserProfileEntity
import com.example.ui.theme.*

enum class VisaPointsSystem(val country: String, val title: String, val threshold: Int) {
    UK_SKILLED_WORKER("United Kingdom", "UK Skilled Worker Visa", 70),
    CANADA_EXPRESS_ENTRY("Canada", "Canada Express Entry (CRS)", 480),
    GERMANY_CHANCENKARTE("Germany", "Germany Opportunity Card (Chancenkarte)", 6),
    AUSTRALIA_SKILLSELECT("Australia", "Australia SkillSelect (189/190)", 65)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisaPointsCalculatorScreen(viewModel: JobViewModel) {
    val profileOpt by viewModel.userProfile.collectAsStateWithLifecycle()
    val profile = profileOpt ?: UserProfileEntity()

    var selectedSystem by remember { mutableStateOf(VisaPointsSystem.UK_SKILLED_WORKER) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("visa_points_calculator_screen")
    ) {
        // Title Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            border = BorderStroke(1.dp, TealCyan.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Immigration Points & Eligibility Engine",
                        color = WhiteActive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Calculate exact points for points-tested visas with official Home Office, IRCC & DHA criteria.",
                        color = SlateMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // System selector tabs
        ScrollableTabRow(
            selectedTabIndex = selectedSystem.ordinal,
            containerColor = NavyDark,
            contentColor = EmeraldGreen,
            edgePadding = 0.dp,
            divider = { Divider(color = NavyLight) },
            modifier = Modifier.fillMaxWidth()
        ) {
            VisaPointsSystem.values().forEach { sys ->
                Tab(
                    selected = selectedSystem == sys,
                    onClick = { selectedSystem = sys },
                    text = {
                        Text(
                            text = sys.country,
                            fontSize = 12.sp,
                            fontWeight = if (selectedSystem == sys) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedSystem == sys) EmeraldGreen else SlateMuted
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedSystem) {
            VisaPointsSystem.UK_SKILLED_WORKER -> UkSkilledWorkerCalculator(profile)
            VisaPointsSystem.CANADA_EXPRESS_ENTRY -> CanadaExpressEntryCalculator(profile)
            VisaPointsSystem.GERMANY_CHANCENKARTE -> GermanyChancenkarteCalculator(profile)
            VisaPointsSystem.AUSTRALIA_SKILLSELECT -> AustraliaSkillSelectCalculator(profile)
        }
    }
}

// ----------------------------------------------------
// 1. UK SKILLED WORKER (70 Points Threshold)
// ----------------------------------------------------
@Composable
fun UkSkilledWorkerCalculator(profile: UserProfileEntity) {
    var hasSponsorOffer by remember { mutableStateOf(true) }
    var isEligibleSkillLevel by remember { mutableStateOf(true) }
    var englishLevelB1 by remember { mutableStateOf(true) }
    var salaryOption by remember { mutableStateOf("going_rate") } // going_rate (20), shortage (20), stem_phd (20), phd (10), new_entrant (20), below_rate (0)

    val mandatoryScore = (if (hasSponsorOffer) 20 else 0) +
            (if (isEligibleSkillLevel) 20 else 0) +
            (if (englishLevelB1) 10 else 0)

    val tradeableScore = when (salaryOption) {
        "going_rate" -> 20
        "shortage" -> 20
        "stem_phd" -> 20
        "new_entrant" -> 20
        "phd" -> 10
        else -> 0
    }

    val totalPoints = mandatoryScore + tradeableScore
    val isEligible = totalPoints >= 70 && mandatoryScore == 50

    ScoreSummaryBanner(
        title = "UK Skilled Worker Visa",
        currentPoints = totalPoints,
        threshold = 70,
        isEligible = isEligible,
        statusText = if (isEligible) "ELIGIBLE FOR SPONSORSHIP" else if (mandatoryScore < 50) "MISSING MANDATORY CRITERIA" else "NEEDS MORE SALARY / BONUS POINTS"
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text(
        text = "1. MANDATORY CRITERIA (50 Points Required)",
        color = TealCyan,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
    )

    Spacer(modifier = Modifier.height(6.dp))

    PointsToggleItem(
        title = "Job offer from Home Office-approved sponsor",
        points = 20,
        checked = hasSponsorOffer,
        onCheckedChange = { hasSponsorOffer = it }
    )
    PointsToggleItem(
        title = "Job at appropriate skill level (RQF Level 3 / A-level+)",
        points = 20,
        checked = isEligibleSkillLevel,
        onCheckedChange = { isEligibleSkillLevel = it }
    )
    PointsToggleItem(
        title = "English language skills at CEFR Level B1 (IELTS 4.0+)",
        points = 10,
        checked = englishLevelB1,
        onCheckedChange = { englishLevelB1 = it }
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text(
        text = "2. TRADEABLE SALARY & EDUCATION (20 Points Required)",
        color = TealCyan,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
    )

    Spacer(modifier = Modifier.height(6.dp))

    val salaryOptions = listOf(
        Pair("going_rate", "Salary meets standard going rate / £38,700+ threshold (+20 pts)"),
        Pair("shortage", "Job on Immigration Salary List / Shortage Occupation (+20 pts)"),
        Pair("stem_phd", "Relevant STEM PhD in subject related to job (+20 pts)"),
        Pair("new_entrant", "New entrant to UK labour market / Under 26 years old (+20 pts)"),
        Pair("phd", "Relevant non-STEM PhD qualification (+10 pts)"),
        Pair("below_rate", "Salary below threshold without concessions (+0 pts)")
    )

    salaryOptions.forEach { (key, label) ->
        PointsRadioItem(
            label = label,
            isSelected = salaryOption == key,
            onClick = { salaryOption = key }
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    RecommendationCard(
        recommendation = if (isEligible) {
            "✅ You meet all Home Office requirements (50 mandatory + $tradeableScore tradeable = $totalPoints pts). Ensure your sponsoring employer issues a valid Certificate of Sponsorship (CoS)."
        } else {
            "⚠️ You currently have $totalPoints/70 points. Ensure you secure an offer with a Home Office licensed sponsor and confirm your role meets the salary going rate."
        }
    )
}

// ----------------------------------------------------
// 2. CANADA EXPRESS ENTRY CRS CALCULATOR
// ----------------------------------------------------
@Composable
fun CanadaExpressEntryCalculator(profile: UserProfileEntity) {
    var ageBracket by remember { mutableStateOf("20-29") } // 110, 30: 105, 35: 75, 40: 45
    var educationLevel by remember { mutableStateOf("Master") } // PhD: 150, Master: 135, Double: 128, Bachelor: 120
    var languageCLB by remember { mutableStateOf("CLB9") } // CLB9+: 136, CLB8: 100, CLB7: 76
    var foreignExpYears by remember { mutableStateOf("3+") } // 3+: 50 (with CLB9), 2: 25, 1: 15
    var hasLmiaOffer by remember { mutableStateOf(true) } // +50 pts (or 200 for executive)
    var hasPnpNomination by remember { mutableStateOf(false) } // +600 pts

    val agePts = when (ageBracket) {
        "20-29" -> 110
        "30" -> 105
        "31-34" -> 90
        "35-39" -> 70
        "40-44" -> 40
        else -> 10
    }

    val eduPts = when (educationLevel) {
        "Doctorate / PhD" -> 150
        "Master / Professional Degree" -> 135
        "Two or more certificates/degrees" -> 128
        "Bachelor Degree (3+ yrs)" -> 120
        else -> 90
    }

    val langPts = when (languageCLB) {
        "CLB 9+ (IELTS L8.0, R7.0, W7.0, S7.0)" -> 136
        "CLB 8 (IELTS 7.5/6.5)" -> 100
        "CLB 7 (IELTS 6.0 across all)" -> 76
        else -> 40
    }

    val expPts = when (foreignExpYears) {
        "3+ Years Skilled Foreign Exp" -> 50
        "2 Years Skilled Exp" -> 25
        "1 Year Skilled Exp" -> 15
        else -> 0
    }

    val bonusPts = (if (hasLmiaOffer) 50 else 0) + (if (hasPnpNomination) 600 else 0)
    val totalCrs = (agePts + eduPts + langPts + expPts + bonusPts).coerceAtMost(1200)
    val isCompetitive = totalCrs >= 480

    ScoreSummaryBanner(
        title = "Canada Express Entry CRS",
        currentPoints = totalCrs,
        threshold = 480,
        isEligible = isCompetitive,
        statusText = if (hasPnpNomination) "GUARANTEED ITA (PNP 600+)" else if (totalCrs >= 500) "HIGHLY COMPETITIVE POOL SCORE" else if (totalCrs >= 480) "COMPETITIVE IN CATEGORY DRAWS" else "BELOW RECENT GENERAL CUTOFFS"
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text(text = "HUMAN CAPITAL & FACTOR SCORES:", color = TealCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    Spacer(modifier = Modifier.height(6.dp))

    DropdownSelector(
        label = "Age Bracket (Max 110 pts)",
        selectedValue = ageBracket,
        options = listOf("20-29", "30", "31-34", "35-39", "40-44", "45+"),
        onSelect = { ageBracket = it }
    )

    DropdownSelector(
        label = "Level of Education (Max 150 pts)",
        selectedValue = educationLevel,
        options = listOf("Doctorate / PhD", "Master / Professional Degree", "Two or more certificates/degrees", "Bachelor Degree (3+ yrs)", "Post-secondary Diploma"),
        onSelect = { educationLevel = it }
    )

    DropdownSelector(
        label = "Official Language Score (Max 136 pts)",
        selectedValue = languageCLB,
        options = listOf("CLB 9+ (IELTS L8.0, R7.0, W7.0, S7.0)", "CLB 8 (IELTS 7.5/6.5)", "CLB 7 (IELTS 6.0 across all)", "CLB 6 or lower"),
        onSelect = { languageCLB = it }
    )

    DropdownSelector(
        label = "Foreign Work Experience (Max 50 pts)",
        selectedValue = foreignExpYears,
        options = listOf("3+ Years Skilled Foreign Exp", "2 Years Skilled Exp", "1 Year Skilled Exp", "No Experience"),
        onSelect = { foreignExpYears = it }
    )

    Spacer(modifier = Modifier.height(10.dp))
    Text(text = "ADDITIONAL ADAPTABILITY BONUSES:", color = TealCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    Spacer(modifier = Modifier.height(6.dp))

    PointsToggleItem(
        title = "Valid LMIA Job Offer (TEER 1, 2, or 3)",
        points = 50,
        checked = hasLmiaOffer,
        onCheckedChange = { hasLmiaOffer = it }
    )
    PointsToggleItem(
        title = "Provincial Nominee Program (PNP) Nomination",
        points = 600,
        checked = hasPnpNomination,
        onCheckedChange = { hasPnpNomination = it }
    )

    Spacer(modifier = Modifier.height(12.dp))

    RecommendationCard(
        recommendation = "CRS Breakdown: Age ($agePts) + Edu ($eduPts) + Lang ($langPts) + Exp ($expPts) + Bonuses ($bonusPts) = $totalCrs. Tip: Scoring CLB 9 (IELTS 8,7,7,7) activates maximum skill transferability bonus."
    )
}

// ----------------------------------------------------
// 3. GERMANY CHANCENKARTE (6 Points Threshold)
// ----------------------------------------------------
@Composable
fun GermanyChancenkarteCalculator(profile: UserProfileEntity) {
    var hasPartialRecognition by remember { mutableStateOf(true) } // 4 pts
    var isShortageOccupation by remember { mutableStateOf(true) } // 1 pt
    var experienceLevel by remember { mutableStateOf("5+ years") } // 5+ yrs: 3 pts, 2+ yrs: 2 pts, none: 0
    var languageLevel by remember { mutableStateOf("German B1 (2 pts)") } // B2: 3, B1: 2, A2: 1, English C1: 1
    var ageBracket by remember { mutableStateOf("Under 35 (2 pts)") } // <35: 2, 35-40: 1, >40: 0
    var previousGermanyStay by remember { mutableStateOf(false) } // 1 pt

    val recPts = if (hasPartialRecognition) 4 else 0
    val shortPts = if (isShortageOccupation) 1 else 0
    val expPts = when (experienceLevel) {
        "5+ years (within last 7 yrs)" -> 3
        "2+ years (within last 5 yrs)" -> 2
        else -> 0
    }
    val langPts = when (languageLevel) {
        "German B2 (3 pts)" -> 3
        "German B1 (2 pts)" -> 2
        "German A2 (1 pt)" -> 1
        "English C1 / Native (1 pt)" -> 1
        else -> 0
    }
    val agePts = when (ageBracket) {
        "Under 35 (2 pts)" -> 2
        "Age 35-40 (1 pt)" -> 1
        else -> 0
    }
    val stayPts = if (previousGermanyStay) 1 else 0

    val totalPoints = recPts + shortPts + expPts + langPts + agePts + stayPts
    val isEligible = totalPoints >= 6

    ScoreSummaryBanner(
        title = "Germany Opportunity Card (Chancenkarte)",
        currentPoints = totalPoints,
        threshold = 6,
        isEligible = isEligible,
        statusText = if (isEligible) "ELIGIBLE TO RECEIVE CHANCENKARTE" else "REQUIRES MINIMUM 6 POINTS"
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text(text = "CHANCENKARTE POINT CRITERIA:", color = TealCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    Spacer(modifier = Modifier.height(6.dp))

    PointsToggleItem(
        title = "Partial recognition of foreign degree / qualification (Anabin)",
        points = 4,
        checked = hasPartialRecognition,
        onCheckedChange = { hasPartialRecognition = it }
    )
    PointsToggleItem(
        title = "Qualification in bottleneck shortage occupation (IT, Healthcare, Eng)",
        points = 1,
        checked = isShortageOccupation,
        onCheckedChange = { isShortageOccupation = it }
    )

    DropdownSelector(
        label = "Professional Experience (Max 3 pts)",
        selectedValue = experienceLevel,
        options = listOf("5+ years (within last 7 yrs)", "2+ years (within last 5 yrs)", "Less than 2 years"),
        onSelect = { experienceLevel = it }
    )

    DropdownSelector(
        label = "Language Competence (Max 3 pts)",
        selectedValue = languageLevel,
        options = listOf("German B2 (3 pts)", "German B1 (2 pts)", "German A2 (1 pt)", "English C1 / Native (1 pt)", "Basic / None"),
        onSelect = { languageLevel = it }
    )

    DropdownSelector(
        label = "Age Factor (Max 2 pts)",
        selectedValue = ageBracket,
        options = listOf("Under 35 (2 pts)", "Age 35-40 (1 pt)", "Over 40 (0 pts)"),
        onSelect = { ageBracket = it }
    )

    PointsToggleItem(
        title = "Previous lawful stay in Germany (>6 consecutive months)",
        points = 1,
        checked = previousGermanyStay,
        onCheckedChange = { previousGermanyStay = it }
    )

    Spacer(modifier = Modifier.height(12.dp))

    RecommendationCard(
        recommendation = "The Chancenkarte allows you to enter Germany for up to 12 months to search for skilled employment and work part-time (up to 20 hrs/week) while interviewing."
    )
}

// ----------------------------------------------------
// 4. AUSTRALIA SKILLSELECT (65 Points Threshold)
// ----------------------------------------------------
@Composable
fun AustraliaSkillSelectCalculator(profile: UserProfileEntity) {
    var ageBracket by remember { mutableStateOf("25-32 years (30 pts)") } // 18-24: 25, 25-32: 30, 33-39: 25, 40-44: 15
    var englishScore by remember { mutableStateOf("Superior (IELTS 8.0 / 20 pts)") } // Superior: 20, Proficient (7.0): 10, Competent: 0
    var overseasExp by remember { mutableStateOf("5-7 years (10 pts)") } // 8+: 15, 5-7: 10, 3-4: 5
    var educationDegree by remember { mutableStateOf("Bachelor / Master (15 pts)") } // Doctorate: 20, Bachelor/Master: 15, Trade/Diploma: 10
    var nominationType by remember { mutableStateOf("State Nominated 190 (+5 pts)") } // Independent 189: 0, State 190: 5, Regional 491: 15

    val agePts = when (ageBracket) {
        "25-32 years (30 pts)" -> 30
        "18-24 years (25 pts)", "33-39 years (25 pts)" -> 25
        "40-44 years (15 pts)" -> 15
        else -> 0
    }

    val engPts = when (englishScore) {
        "Superior (IELTS 8.0 / 20 pts)" -> 20
        "Proficient (IELTS 7.0 / 10 pts)" -> 10
        else -> 0
    }

    val expPts = when (overseasExp) {
        "8+ years (15 pts)" -> 15
        "5-7 years (10 pts)" -> 10
        "3-4 years (5 pts)" -> 5
        else -> 0
    }

    val eduPts = when (educationDegree) {
        "Doctorate (20 pts)" -> 20
        "Bachelor / Master (15 pts)" -> 15
        "Trade / Diploma (10 pts)" -> 10
        else -> 0
    }

    val nomPts = when (nominationType) {
        "Regional Provisional 491 (+15 pts)" -> 15
        "State Nominated 190 (+5 pts)" -> 5
        else -> 0
    }

    val totalPoints = agePts + engPts + expPts + eduPts + nomPts
    val isEligible = totalPoints >= 65

    ScoreSummaryBanner(
        title = "Australia SkillSelect (Subclass 189 / 190 / 491)",
        currentPoints = totalPoints,
        threshold = 65,
        isEligible = isEligible,
        statusText = if (totalPoints >= 85) "SUPERIOR PRIORITY (INVITATION IMMINENT)" else if (isEligible) "ELIGIBLE FOR EXPRESSION OF INTEREST (EOI)" else "BELOW 65 POINT PASS MARK"
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text(text = "AUSTRALIA DHA POINT CRITERIA:", color = TealCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    Spacer(modifier = Modifier.height(6.dp))

    DropdownSelector(
        label = "Age Factor (Max 30 pts)",
        selectedValue = ageBracket,
        options = listOf("25-32 years (30 pts)", "18-24 years (25 pts)", "33-39 years (25 pts)", "40-44 years (15 pts)", "45+ (0 pts)"),
        onSelect = { ageBracket = it }
    )

    DropdownSelector(
        label = "English Language (Max 20 pts)",
        selectedValue = englishScore,
        options = listOf("Superior (IELTS 8.0 / 20 pts)", "Proficient (IELTS 7.0 / 10 pts)", "Competent (IELTS 6.0 / 0 pts)"),
        onSelect = { englishScore = it }
    )

    DropdownSelector(
        label = "Overseas Skilled Employment (Max 15 pts)",
        selectedValue = overseasExp,
        options = listOf("8+ years (15 pts)", "5-7 years (10 pts)", "3-4 years (5 pts)", "Under 3 years (0 pts)"),
        onSelect = { overseasExp = it }
    )

    DropdownSelector(
        label = "Educational Qualifications (Max 20 pts)",
        selectedValue = educationDegree,
        options = listOf("Doctorate (20 pts)", "Bachelor / Master (15 pts)", "Trade / Diploma (10 pts)"),
        onSelect = { educationDegree = it }
    )

    DropdownSelector(
        label = "Visa Nomination Stream Bonus",
        selectedValue = nominationType,
        options = listOf("Regional Provisional 491 (+15 pts)", "State Nominated 190 (+5 pts)", "Independent Skilled 189 (+0 pts)"),
        onSelect = { nominationType = it }
    )

    Spacer(modifier = Modifier.height(12.dp))

    RecommendationCard(
        recommendation = "Total Score: $totalPoints pts. For high-demand occupations, targeting Subclass 491 grants an immediate +15 points bonus with priority state processing."
    )
}

// ----------------------------------------------------
// REUSABLE HELPER UI COMPONENTS
// ----------------------------------------------------
@Composable
fun ScoreSummaryBanner(
    title: String,
    currentPoints: Int,
    threshold: Int,
    isEligible: Boolean,
    statusText: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isEligible) EmeraldGreen.copy(alpha = 0.15f) else AmberGold.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, if (isEligible) EmeraldGreen else AmberGold),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = WhiteActive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = statusText,
                        color = if (isEligible) EmeraldGreen else AmberGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isEligible) EmeraldGreen else AmberGold)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$currentPoints / $threshold pts",
                        color = NavyDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            val progress = (currentPoints.toFloat() / threshold.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = progress,
                color = if (isEligible) EmeraldGreen else AmberGold,
                trackColor = NavyDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
fun PointsToggleItem(
    title: String,
    points: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(NavyMedium)
            .border(1.dp, if (checked) EmeraldGreen.copy(alpha = 0.5f) else NavyLight, RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = EmeraldGreen,
                checkmarkColor = NavyDark,
                uncheckedColor = SlateMuted
            ),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            color = WhiteActive,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (checked) EmeraldGreen.copy(alpha = 0.2f) else NavyDark)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "+$points pts",
                color = if (checked) EmeraldGreen else SlateMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun PointsRadioItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(NavyMedium)
            .border(1.dp, if (isSelected) EmeraldGreen.copy(alpha = 0.5f) else NavyLight, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = EmeraldGreen,
                unselectedColor = SlateMuted
            ),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = if (isSelected) WhiteActive else SlateMuted,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, color = SlateMuted, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(2.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldGreen,
                    unfocusedBorderColor = NavyLight,
                    focusedTextColor = WhiteActive,
                    unfocusedTextColor = WhiteActive,
                    focusedContainerColor = NavyMedium,
                    unfocusedContainerColor = NavyMedium
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(NavyDark)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = WhiteActive, fontSize = 12.sp) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RecommendationCard(recommendation: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        border = BorderStroke(1.dp, NavyLight),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = TealCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = recommendation,
                color = SlateMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
