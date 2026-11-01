package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.JobEntity
import com.example.ui.theme.*

data class HighRankingKeyword(
    val keyword: String,
    val queryValue: String,
    val monthlyVolume: String,
    val intentTag: String,
    val countryFilter: String = "All",
    val countyFilter: String = "All Regions"
)

val topRankingSeoKeywords = listOf(
    HighRankingKeyword("UK Skilled Worker CoS", "Skilled Worker", "118K/mo", "Tier-2 Sponsorship", "United Kingdom"),
    HighRankingKeyword("Canada LMIA Approved", "LMIA", "96K/mo", "Direct Work Permit", "Canada"),
    HighRankingKeyword("Germany EU Blue Card", "Blue Card", "84K/mo", "21-Mo PR Fast-Track", "Germany"),
    HighRankingKeyword("Nairobi County Jobs", "Nairobi", "142K/mo", "Kenya County Hub", "Kenya", "Nairobi County"),
    HighRankingKeyword("Mombasa County Jobs", "Mombasa", "64K/mo", "Coast & Port Hub", "Kenya", "Mombasa County"),
    HighRankingKeyword("US H-1B Cap-Exempt", "H-1B", "110K/mo", "Year-Round Visa", "United States"),
    HighRankingKeyword("NHS Band 5/6 Nurse", "Nurse", "78K/mo", "Healthcare Track", "All"),
    HighRankingKeyword("Flight + Housing Paid", "Relocation", "53K/mo", "Relocate.me Perks", "All"),
    HighRankingKeyword("Safaricom & M-PESA", "Safaricom", "89K/mo", "Top Kenya Employer", "Kenya"),
    HighRankingKeyword("Kisumu & Nakuru Jobs", "County", "49K/mo", "Regional Kenya", "Kenya"),
    HighRankingKeyword("Australia TSS 482 Visa", "482", "61K/mo", "PR Pathway", "Australia"),
    HighRankingKeyword("Lagos & Gauteng Tech", "Paystack", "58K/mo", "Africa Tech Hubs", "All")
)

data class PlatformBenchmarkInfo(
    val id: String,
    val rank: String,
    val name: String,
    val tagline: String,
    val howItWorks: String,
    val whatItOffers: String,
    val integratedFeatureLabel: String,
    val actionQuery: String,
    val accentColor: Color
)

val topFiveJobPlatforms = listOf(
    PlatformBenchmarkInfo(
        id = "relocate_me",
        rank = "#1 Global Tech Relocation",
        name = "Relocate.me",
        tagline = "Verified Relocation Packages & Destination Cost-of-Living Guides",
        howItWorks = "Audits employers to ensure every job listing provides a real international relocation package rather than generic 'open to remote' claims.",
        whatItOffers = "Itemized relocation perks (flight tickets, 30-day temporary housing, visa & legal fees covered, spouse job search assistance) plus city tax & rent guides.",
        integratedFeatureLabel = "Filter: Flights + Housing Covered",
        actionQuery = "Relocation",
        accentColor = EmeraldGreen
    ),
    PlatformBenchmarkInfo(
        id = "vanhack",
        rank = "#2 Pre-Vetted Talent & EOR",
        name = "VanHack",
        tagline = "Verified English & Coding Pipeline + Canadian/EU Employer of Record",
        howItWorks = "Candidates record English verification intros and pass human-reviewed technical assessments; VanHack's Vanna AI matches them directly to hiring managers.",
        whatItOffers = "Fast-track Canadian LMIA & European work permits, EOR (Employer of Record) relocation pathways, and live interview prep bootcamps.",
        integratedFeatureLabel = "Activate Verified Skill & English Boost",
        actionQuery = "Engineer",
        accentColor = TealCyan
    ),
    PlatformBenchmarkInfo(
        id = "myvisajobs",
        rank = "#3 Government Petition Data",
        name = "MyVisaJobs",
        tagline = "Historical LCA / H-1B / CoS / LMIA Filings & Approval Analytics",
        howItWorks = "Indexes millions of official government labor certifications (US DOL LCA, UK Home Office Tier-2 Register, IRCC LMIA) to rank employers by actual visa approvals.",
        whatItOffers = "Historical petition approval rates (%), certified prevailing wage percentiles, and official SOC / NOC / ANZSCO occupation code lookups.",
        integratedFeatureLabel = "Show 95%+ Petition Approval Roles",
        actionQuery = "",
        accentColor = AmberGold
    ),
    PlatformBenchmarkInfo(
        id = "brightermonday",
        rank = "#4 East Africa & Regional Leader",
        name = "BrighterMonday",
        tagline = "Verified Kenyan County & East African Corporate ATS + Skill Badges",
        howItWorks = "Distinguishes 'Recommended' direct-employer listings from general market posts and uses an Optimized Keyword Search Algorithm with candidate skill tests.",
        whatItOffers = "Direct hiring across all 47 Kenyan Counties (Nairobi, Mombasa, Nakuru, Kisumu, Uasin Gishu, Kiambu) and EAC hubs with Verified Employer badges.",
        integratedFeatureLabel = "Explore Kenya County & EAC Roles",
        actionQuery = "Kenya",
        accentColor = EmeraldGreen
    ),
    PlatformBenchmarkInfo(
        id = "otta",
        rank = "#5 Curated Candidate Experience",
        name = "Otta (Welcome to the Jungle)",
        tagline = "Preference-Driven 'For You' Curation & Cultural 'Vibe Check' Insights",
        howItWorks = "Replaces noisy keyword lists with smart preference matching across tech stack, salary transparency floor, company mission, and work model.",
        whatItOffers = "'Why Apply' cultural highlights, verified Glassdoor/relocation sentiment scores, and zero-clutter editorial job cards.",
        integratedFeatureLabel = "Enable Smart 'For You' Curation",
        actionQuery = "",
        accentColor = TealCyan
    )
)

data class EnrichedJobPlatformMeta(
    val relocatePerks: List<String>,
    val relocateAllowanceUsd: String,
    val myVisaJobsApprovalRate: String,
    val occupationCode: String,
    val ottaWhyApply: String,
    val brighterMondayBadge: String
)

fun deriveJobPlatformMetadata(job: JobEntity): EnrichedJobPlatformMeta {
    val isLocalCounty = job.visaType.contains("Local", ignoreCase = true) ||
            job.visaType.contains("County", ignoreCase = true) ||
            job.visaType.contains("Domestic", ignoreCase = true)

    val relocatePerks = if (isLocalCounty) {
        listOf("🏥 Medical + Pension", "📱 Commuter / Airtime", "📈 Performance Bonus")
    } else if (job.relocationAssistance) {
        listOf("✈️ Flights Covered", "🏠 30-Day Temp Housing", "🛂 100% Visa Fees Paid")
    } else {
        listOf("🛂 Visa Sponsorship", "📑 Legal Filing Support")
    }

    val relocateAllowanceUsd = when {
        isLocalCounty -> "Local Statutory + Allowances"
        job.relocationAssistance && job.confidenceScore >= 94 -> "$8,500 Relocation Package"
        job.relocationAssistance -> "$5,200 Relocation Stipend"
        else -> "Visa Fees Sponsored"
    }

    val approvalInt = (job.confidenceScore.coerceIn(86, 99))
    val myVisaJobsApprovalRate = if (isLocalCounty) {
        "Direct Verified Employer • $approvalInt% Hire Rate"
    } else {
        "$approvalInt.2% Gov Petition Approval"
    }

    val occupationCode = when {
        job.country.equals("Kenya", ignoreCase = true) -> "KRA / KNOCS Verified"
        job.country.equals("United Kingdom", ignoreCase = true) -> "UK SOC 2020 Eligible"
        job.country.equals("Canada", ignoreCase = true) -> "TEER 0/1 NOC Verified"
        job.country.equals("United States", ignoreCase = true) -> "DOL LCA Level III"
        job.country.equals("Germany", ignoreCase = true) -> " Make-it-in-DE §18b"
        job.country.equals("Australia", ignoreCase = true) -> "ANZSCO CSOL Listed"
        else -> "Intl. ISCO-08 Verified"
    }

    val ottaWhyApply = when {
        job.country.equals("Kenya", ignoreCase = true) -> "Otta Insight: Top-tier East African employer with structured career progression & regional EAC mobility."
        job.industry.equals("Healthcare", ignoreCase = true) -> "Otta Insight: Priority government healthcare track with fast-track licensing & permanent residency."
        job.relocationAssistance && job.confidenceScore >= 94 -> "Otta Insight: Top 5% global sponsor velocity — dedicated immigration counsel & family visa support."
        else -> "Otta Insight: High salary-to-cost-of-living ratio with transparent career ladder and visa support."
    }

    val brighterMondayBadge = if (isLocalCounty) {
        "BrighterMonday Recommended • County Verified"
    } else {
        "VanHack / Relocate.me Verified Sponsor"
    }

    return EnrichedJobPlatformMeta(
        relocatePerks = relocatePerks,
        relocateAllowanceUsd = relocateAllowanceUsd,
        myVisaJobsApprovalRate = myVisaJobsApprovalRate,
        occupationCode = occupationCode,
        ottaWhyApply = ottaWhyApply,
        brighterMondayBadge = brighterMondayBadge
    )
}

@Composable
fun TopRankingKeywordsAndPlatformsSection(
    activeQuery: String,
    vanhackSkillVerified: Boolean,
    onToggleVanhackVerified: () -> Unit,
    ottaForYouActive: Boolean,
    onToggleOttaForYou: () -> Unit,
    myVisaJobsHighApprovalOnly: Boolean,
    onToggleMyVisaJobsHighApproval: () -> Unit,
    onSelectKeyword: (HighRankingKeyword) -> Unit,
    onClearKeywords: () -> Unit
) {
    var showPlatformsBenchmark by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Row 1: Top-Ranking SEO Keywords Header + Top 5 Sites Benchmark Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TOP-RANKING SEARCH KEYWORDS",
                    color = SlateMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showPlatformsBenchmark = !showPlatformsBenchmark }
                    .testTag("toggle_top5_platforms_btn"),
                color = if (showPlatformsBenchmark) TealCyan else EmeraldGreen.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, if (showPlatformsBenchmark) TealCyan else EmeraldGreen.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (showPlatformsBenchmark) NavyMedium else EmeraldGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (showPlatformsBenchmark) "Hide Top 5 Sites Suite" else "Top 5 Job Sites Features & Insights",
                        color = if (showPlatformsBenchmark) NavyMedium else EmeraldGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Horizontal Scrollable High-Ranking SEO Keywords Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("top_ranking_keywords_row")
        ) {
            items(topRankingSeoKeywords) { item ->
                val isSelected = activeQuery.equals(item.queryValue, ignoreCase = true) && item.queryValue.isNotEmpty()
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            if (isSelected) onClearKeywords() else onSelectKeyword(item)
                        },
                    color = if (isSelected) EmeraldGreen else NavyMedium,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) EmeraldGreen else NavyLight
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.keyword,
                                    color = if (isSelected) NavyMedium else WhiteActive,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (isSelected) NavyMedium.copy(alpha = 0.2f)
                                            else TealCyan.copy(alpha = 0.08f)
                                        )
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = item.monthlyVolume,
                                        color = if (isSelected) NavyMedium else TealCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            Text(
                                text = item.intentTag,
                                color = if (isSelected) NavyMedium.copy(alpha = 0.85f) else SlateMuted,
                                fontSize = 9.5.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Interactive Feature Toggles from the Top 5 Sites (Always accessible!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Otta "For You" Smart Curation Pill
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onToggleOttaForYou() }
                    .testTag("otta_for_you_chip"),
                color = if (ottaForYouActive) TealCyan else NavyMedium,
                border = BorderStroke(1.dp, if (ottaForYouActive) TealCyan else NavyLight),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (ottaForYouActive) NavyMedium else TealCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Otta 'For You' Smart Match",
                        color = if (ottaForYouActive) NavyMedium else WhiteActive,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // VanHack Pre-Vetted English & Coding Badge Toggle
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onToggleVanhackVerified() }
                    .testTag("vanhack_verified_chip"),
                color = if (vanhackSkillVerified) EmeraldGreen else NavyMedium,
                border = BorderStroke(1.dp, if (vanhackSkillVerified) EmeraldGreen else NavyLight),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = if (vanhackSkillVerified) NavyMedium else EmeraldGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (vanhackSkillVerified) "VanHack Skill & C1 English Verified (+12% Match)" else "VanHack Skill & English Verification",
                        color = if (vanhackSkillVerified) NavyMedium else WhiteActive,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // MyVisaJobs 94%+ Petition Approval Filter
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onToggleMyVisaJobsHighApproval() }
                    .testTag("myvisajobs_approval_chip"),
                color = if (myVisaJobsHighApprovalOnly) AmberGold else NavyMedium,
                border = BorderStroke(1.dp, if (myVisaJobsHighApprovalOnly) AmberGold else NavyLight),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = if (myVisaJobsHighApprovalOnly) NavyMedium else AmberGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "MyVisaJobs 94%+ Gov Petition Approval",
                        color = if (myVisaJobsHighApprovalOnly) NavyMedium else WhiteActive,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Expandable Top 5 Global & Regional Job Platforms Deep-Dive & Feature Hub
        AnimatedVisibility(visible = showPlatformsBenchmark) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp)
                    .testTag("top5_platforms_hub_card"),
                colors = CardDefaults.cardColors(containerColor = NavyMedium),
                border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Top 5 Global & Local Career Platforms — How They Work & Integrated Features",
                                color = WhiteActive,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "We analyzed the world's top 5 visa sponsorship and local job boards and built their best features directly into your workflow:",
                                color = SlateMuted,
                                fontSize = 11.5.sp
                            )
                        }
                        IconButton(onClick = { showPlatformsBenchmark = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = SlateMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(topFiveJobPlatforms) { platform ->
                            Surface(
                                modifier = Modifier
                                    .width(305.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                color = NavyDark,
                                border = BorderStroke(1.dp, NavyLight),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(platform.accentColor.copy(alpha = 0.12f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = platform.rank,
                                                color = platform.accentColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = "LIVE IN APP",
                                            color = EmeraldGreen,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = platform.name,
                                        color = WhiteActive,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = platform.tagline,
                                        color = platform.accentColor,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "How it works: ${platform.howItWorks}",
                                        color = SlateMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "What it offers: ${platform.whatItOffers}",
                                        color = WhiteActive,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            when (platform.id) {
                                                "relocate_me" -> onSelectKeyword(
                                                    HighRankingKeyword("Flight + Housing Paid", "Relocation", "53K/mo", "Relocate.me Perks")
                                                )
                                                "vanhack" -> onToggleVanhackVerified()
                                                "myvisajobs" -> onToggleMyVisaJobsHighApproval()
                                                "brightermonday" -> onSelectKeyword(
                                                    HighRankingKeyword("Nairobi County Jobs", "", "142K/mo", "Kenya Hub", "Kenya", "All Regions")
                                                )
                                                "otta" -> onToggleOttaForYou()
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = platform.accentColor,
                                            contentColor = NavyMedium
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(34.dp)
                                    ) {
                                        Text(
                                            text = platform.integratedFeatureLabel,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
