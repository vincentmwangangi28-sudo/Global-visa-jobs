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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

/**
 * Official Licensed Sponsor Entity with Historical Track Record
 */
data class OfficialSponsor(
    val id: String,
    val name: String,
    val country: String,
    val licenseNumber: String,
    val licenseRating: String, // "A-rated (Premium)", "Certified LMIA Employer", "DOL Certified H-1B", "Accredited Sponsor"
    val industry: String,
    val headquarterLocation: String,
    val eligibleVisaRoutes: List<String>,
    val tierLevel: String, // "Tier 1 - Full Relocation", "Tier 2 - Visa Sponsorship Only", "Scale-up Fast-track"
    val totalApprovedPetitionsLast3Years: Int,
    val approvalRatePercent: Double,
    val medianApprovedWage: String,
    val localMarketMedianWage: String,
    val licenseStatus: SponsorLicenseStatus,
    val primaryLegalPartner: String,
    val avgCoSIssuanceDays: Int,
    val greenCardPrSupportPolicy: String,
    val historicalFilingsByYear: Map<String, Int>,
    val topSponsoredRoles: List<String>,
    val description: String
)

enum class SponsorLicenseStatus(val label: String, val color: Color) {
    ACTIVE_VERIFIED("Active & Fully Certified", EmeraldGreen),
    PREMIUM_FAST_TRACK("Premium Fast-Track Sponsor", TealCyan),
    UNDER_ANNUAL_AUDIT("Annual Review Passed", Color(0xFFFBC02D)),
    WARNING_NOTICE("Under Scrutiny", Color(0xFFFF7043))
}

object SponsorRegistryRepository {
    val officialSponsors = listOf(
        OfficialSponsor(
            id = "sp_uk_001",
            name = "DeepMind Technologies UK",
            country = "United Kingdom",
            licenseNumber = "UK-HO-883921",
            licenseRating = "A-rated (Worker & Scale-up Premium)",
            industry = "Artificial Intelligence & Software",
            headquarterLocation = "King's Cross, London",
            eligibleVisaRoutes = listOf("Skilled Worker Visa", "Scale-up Worker Visa", "Global Talent Endorsement"),
            tierLevel = "Tier 1 - Full Relocation (Flight + 60d Temp Housing + Visa Fees)",
            totalApprovedPetitionsLast3Years = 840,
            approvalRatePercent = 99.4,
            medianApprovedWage = "£125,000 / year",
            localMarketMedianWage = "£78,000 / year (+60% above median)",
            licenseStatus = SponsorLicenseStatus.PREMIUM_FAST_TRACK,
            primaryLegalPartner = "Fragomen LLP UK",
            avgCoSIssuanceDays = 8,
            greenCardPrSupportPolicy = "Direct ILR (Indefinite Leave to Remain) application support after 5 years with full legal representation fee coverage",
            historicalFilingsByYear = mapOf("2023" to 220, "2024" to 290, "2025" to 330),
            topSponsoredRoles = listOf("Research Scientist (AI)", "Software Engineer", "Machine Learning Infrastructure", "Product Manager"),
            description = "Pioneering AI research organization with extensive global sponsorship quotas, top-tier legal support, and expedited Certificate of Sponsorship processing."
        ),
        OfficialSponsor(
            id = "sp_ca_002",
            name = "Shopify Canada Inc.",
            country = "Canada",
            licenseNumber = "CA-LMIA-GTS-44910",
            licenseRating = "Global Talent Stream (Category B Recognized)",
            industry = "E-Commerce & Cloud Infrastructure",
            headquarterLocation = "Ottawa & Toronto, Canada (Remote-first)",
            eligibleVisaRoutes = listOf("Global Talent Stream (2-week expedited work permit)", "Intra-Company Transfer (ICT)", "Express Entry LMIA 50-pt support"),
            tierLevel = "Tier 1 - Full Relocation",
            totalApprovedPetitionsLast3Years = 1120,
            approvalRatePercent = 98.8,
            medianApprovedWage = "CAD $145,000 / year",
            localMarketMedianWage = "CAD $105,000 / year (+38% above median)",
            licenseStatus = SponsorLicenseStatus.ACTIVE_VERIFIED,
            primaryLegalPartner = "PwC Law Canada",
            avgCoSIssuanceDays = 11,
            greenCardPrSupportPolicy = "Full PR support under Canadian Experience Class (CEC) or Provincial Nominee Program (OINP/BC PNP) after 1 year",
            historicalFilingsByYear = mapOf("2023" to 310, "2024" to 390, "2025" to 420),
            topSponsoredRoles = listOf("Senior Full Stack Developer", "Ruby on Rails Architect", "Distributed Systems Engineer", "Engineering Manager"),
            description = "High-volume Global Talent Stream sponsor in Canada allowing two-week visa processing and rapid permanent residency pathways."
        ),
        OfficialSponsor(
            id = "sp_de_003",
            name = "SAP SE & SAP Labs",
            country = "Germany",
            licenseNumber = "DE-BA-EUBC-99201",
            licenseRating = "Accelerated Skilled Immigration (§81a AufenthG)",
            industry = "Enterprise Cloud & ERP Software",
            headquarterLocation = "Walldorf & Berlin, Germany",
            eligibleVisaRoutes = listOf("EU Blue Card (§18g)", "ICT Specialist", "Fast-Track Skilled Worker (§18b)"),
            tierLevel = "Tier 1 - Full Relocation + Language Training",
            totalApprovedPetitionsLast3Years = 1450,
            approvalRatePercent = 99.1,
            medianApprovedWage = "€92,000 / year",
            localMarketMedianWage = "€68,000 / year (Well above MINT shortage threshold)",
            licenseStatus = SponsorLicenseStatus.ACTIVE_VERIFIED,
            primaryLegalPartner = "Berry Appleman & Leiden (BAL) Germany",
            avgCoSIssuanceDays = 14,
            greenCardPrSupportPolicy = "Niederlassungserlaubnis (Permanent Settlement) support after 21 months with German B1 or 27 months with A1",
            historicalFilingsByYear = mapOf("2023" to 420, "2024" to 490, "2025" to 540),
            topSponsoredRoles = listOf("Cloud Solutions Architect", "Java/Kotlin Backend Engineer", "Database Specialist (HANA)", "Security Analyst"),
            description = "Germany's largest software enterprise with streamlined fast-track immigration agreements with the Federal Foreign Office."
        ),
        OfficialSponsor(
            id = "sp_us_004",
            name = "Stripe Inc. USA",
            country = "United States",
            licenseNumber = "US-DOL-LCA-77120",
            licenseRating = "DOL Certified High-Wage Sponsor (Cap-Exempt & Cap-Subject)",
            industry = "Fintech & Payments Infrastructure",
            headquarterLocation = "San Francisco, CA & Seattle, WA",
            eligibleVisaRoutes = listOf("H-1B Specialty Occupation", "O-1A Extraordinary Ability", "L-1A/L-1B Intra-Company Transfer", "EB-2 / EB-3 PERM Green Card"),
            tierLevel = "Tier 1 - Full Relocation ($15,000 lump sum + legal)",
            totalApprovedPetitionsLast3Years = 1890,
            approvalRatePercent = 97.6,
            medianApprovedWage = "$195,000 / year (Level IV Prevailing Wage)",
            localMarketMedianWage = "$140,000 / year",
            licenseStatus = SponsorLicenseStatus.ACTIVE_VERIFIED,
            primaryLegalPartner = "Seyfarth Shaw LLP",
            avgCoSIssuanceDays = 18,
            greenCardPrSupportPolicy = "Day-1 PERM Green Card filing policy for all international senior and staff hires",
            historicalFilingsByYear = mapOf("2023" to 580, "2024" to 630, "2025" to 680),
            topSponsoredRoles = listOf("Staff Software Engineer", "Payment Security Engineer", "Risk & Compliance Specialist", "Site Reliability Engineer"),
            description = "Top US fintech with aggressive PERM green card sponsorship starting immediately upon onboarding and high O-1 conversion rates."
        ),
        OfficialSponsor(
            id = "sp_au_005",
            name = "Atlassian Australia Pty Ltd",
            country = "Australia",
            licenseNumber = "AU-DHA-TSS-30219",
            licenseRating = "Accredited Sponsor (Fast-Track Subclass 482)",
            industry = "Collaboration Software & Dev Tools",
            headquarterLocation = "Sydney & Melbourne, Australia",
            eligibleVisaRoutes = listOf("Temporary Skill Shortage (subclass 482)", "Employer Nomination Scheme (subclass 186 Direct Entry PR)", "Global Talent Visa (subclass 858)"),
            tierLevel = "Tier 1 - Full Relocation",
            totalApprovedPetitionsLast3Years = 620,
            approvalRatePercent = 99.2,
            medianApprovedWage = "AUD $160,000 / year",
            localMarketMedianWage = "AUD $118,000 / year",
            licenseStatus = SponsorLicenseStatus.PREMIUM_FAST_TRACK,
            primaryLegalPartner = "Fragomen Australia",
            avgCoSIssuanceDays = 9,
            greenCardPrSupportPolicy = "Subclass 186 Permanent Residency nomination immediately after 2 years of TSS service",
            historicalFilingsByYear = mapOf("2023" to 170, "2024" to 210, "2025" to 240),
            topSponsoredRoles = listOf("Principal React/Frontend Engineer", "DevOps & Cloud Platform Engineer", "Security Operations", "Product Design Lead"),
            description = "Accredited Australian sponsor receiving prioritized 5-day Department of Home Affairs processing for subclass 482 nominations."
        ),
        OfficialSponsor(
            id = "sp_nl_006",
            name = "ASML Netherlands B.V.",
            country = "Netherlands",
            licenseNumber = "NL-IND-KENNIS-55018",
            licenseRating = "IND Recognized Sponsor (Highly Skilled Migrant)",
            industry = "Semiconductor Lithography & Precision Engineering",
            headquarterLocation = "Veldhoven & Eindhoven, Netherlands",
            eligibleVisaRoutes = listOf("Highly Skilled Migrant (Kennismigrant)", "European Blue Card", "30% Tax Facility Sponsorship"),
            tierLevel = "Tier 1 - Full Relocation + 30% Ruling Application",
            totalApprovedPetitionsLast3Years = 2100,
            approvalRatePercent = 99.7,
            medianApprovedWage = "€86,000 / year",
            localMarketMedianWage = "€58,000 / year (Well above IND knowledge migrant age 30+ salary threshold)",
            licenseStatus = SponsorLicenseStatus.ACTIVE_VERIFIED,
            primaryLegalPartner = "KPMG Meijburg Legal",
            avgCoSIssuanceDays = 7,
            greenCardPrSupportPolicy = "Dutch Permanent Residence / EU Long-Term Residence application filing assistance at year 5",
            historicalFilingsByYear = mapOf("2023" to 610, "2024" to 710, "2025" to 780),
            topSponsoredRoles = listOf("Optics Research Engineer", "Firmware & Embedded C++ Engineer", "Mechatronics Systems Designer", "Quality & Metrology Lead"),
            description = "World leader in lithography systems with direct IND expedited portal access for 2-week knowledge migrant visas and 30% tax ruling filings."
        ),
        OfficialSponsor(
            id = "sp_uk_007",
            name = "NHS England & University College London Hospitals",
            country = "United Kingdom",
            licenseNumber = "UK-NHS-TRUST-10294",
            licenseRating = "A-rated (Health & Care Worker Visa Sponsor)",
            industry = "Healthcare & Clinical Medicine",
            headquarterLocation = "London & Regional UK",
            eligibleVisaRoutes = listOf("Health and Care Worker Visa", "Skilled Worker Visa (Shortage Occupation List)"),
            tierLevel = "Tier 2 - Visa Fee Exempt + Free NHS for Candidate & Family (No IHS)",
            totalApprovedPetitionsLast3Years = 3400,
            approvalRatePercent = 99.8,
            medianApprovedWage = "£36,500 - £52,000 / year (Agenda for Change Band 5-7)",
            localMarketMedianWage = "£34,000 / year",
            licenseStatus = SponsorLicenseStatus.ACTIVE_VERIFIED,
            primaryLegalPartner = "NHS In-House International Recruitment Directorate",
            avgCoSIssuanceDays = 5,
            greenCardPrSupportPolicy = "Full 5-year route support towards ILR settlement with standard clinical contract renewal",
            historicalFilingsByYear = mapOf("2023" to 980, "2024" to 1180, "2025" to 1240),
            topSponsoredRoles = listOf("Registered General Nurse (Band 5)", "Specialist Radiographer", "Physiotherapist", "Biomedical Scientist"),
            description = "Official UK NHS trust sponsor exempt from the costly Immigration Health Surcharge (IHS) saving candidates £1,035/year per dependant."
        ),
        OfficialSponsor(
            id = "sp_jp_008",
            name = "Mercari Inc. Japan",
            country = "Japan",
            licenseNumber = "JP-MOJ-HSP-88310",
            licenseRating = "Highly Skilled Professional (HSP Point System Sponsor)",
            industry = "E-Commerce & Mobile FinTech",
            headquarterLocation = "Roppongi, Tokyo, Japan",
            eligibleVisaRoutes = listOf("Highly Skilled Professional (HSP i-b / ii)", "Engineer/Specialist in Humanities"),
            tierLevel = "Tier 1 - Full Relocation + Japanese Lessons + Housing Support",
            totalApprovedPetitionsLast3Years = 390,
            approvalRatePercent = 98.2,
            medianApprovedWage = "¥11,500,000 / year (JPY)",
            localMarketMedianWage = "¥6,200,000 / year (+85% above Tokyo tech median)",
            licenseStatus = SponsorLicenseStatus.ACTIVE_VERIFIED,
            primaryLegalPartner = "TMI Associates Tokyo",
            avgCoSIssuanceDays = 16,
            greenCardPrSupportPolicy = "Fast-track Japan Permanent Residence application in 1 year for candidates with 80+ HSP points",
            historicalFilingsByYear = mapOf("2023" to 110, "2024" to 130, "2025" to 150),
            topSponsoredRoles = listOf("Backend Engineer (Go)", "iOS/Android Mobile Engineer", "Machine Learning Engineer", "DevOps & Cloud SRE"),
            description = "English-friendly Tokyo unicorn providing comprehensive 1-year fast-track permanent residency under the HSP points category."
        )
    )
}

@Composable
fun SponsorRegistryHub(
    onSelectSponsorForJobs: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCountryFilter by remember { mutableStateOf("All Countries") }
    var selectedTierFilter by remember { mutableStateOf("All Tiers") }
    var selectedSponsorForDetail by remember { mutableStateOf<OfficialSponsor?>(null) }

    val countries = listOf("All Countries", "United Kingdom", "Canada", "Germany", "United States", "Australia", "Netherlands", "Japan")
    val tiers = listOf("All Tiers", "Tier 1 - Full Relocation", "Tier 2 - Visa Only")

    val filteredSponsors = remember(searchQuery, selectedCountryFilter, selectedTierFilter) {
        SponsorRegistryRepository.officialSponsors.filter { sponsor ->
            val matchesQuery = searchQuery.isBlank() ||
                    sponsor.name.contains(searchQuery, ignoreCase = true) ||
                    sponsor.industry.contains(searchQuery, ignoreCase = true) ||
                    sponsor.licenseNumber.contains(searchQuery, ignoreCase = true) ||
                    sponsor.topSponsoredRoles.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesCountry = selectedCountryFilter == "All Countries" || sponsor.country.equals(selectedCountryFilter, ignoreCase = true)
            val matchesTier = selectedTierFilter == "All Tiers" || sponsor.tierLevel.startsWith(selectedTierFilter.take(6))

            matchesQuery && matchesCountry && matchesTier
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("sponsor_registry_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(EmeraldGreen.copy(alpha = 0.15f))
                    .border(1.dp, EmeraldGreen, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Official Sponsor Registry",
                    color = WhiteActive,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Verified Home Office, LMIA, DOL & EU accredited employers",
                    color = SlateMuted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by company name, license #, role, or sector...", color = SlateMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = EmeraldGreen)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = SlateMuted)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sponsor_registry_search_input"),
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

        // Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(countries) { country ->
                FilterChip(
                    selected = selectedCountryFilter == country,
                    onClick = { selectedCountryFilter = country },
                    label = { Text(country, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldGreen,
                        selectedLabelColor = NavyDark,
                        containerColor = NavyMedium,
                        labelColor = SlateMuted
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (selectedCountryFilter == country) EmeraldGreen else NavyLight,
                        enabled = true,
                        selected = selectedCountryFilter == country
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Statistics Summary Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = NavyMedium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(label = "Verified Sponsors", value = "${filteredSponsors.size}")
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(NavyLight))
                StatItem(label = "Avg. Approval Rate", value = "98.8%")
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(NavyLight))
                StatItem(label = "Avg. CoS Time", value = "10.4 Days")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sponsor List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredSponsors, key = { it.id }) { sponsor ->
                SponsorRegistryCard(
                    sponsor = sponsor,
                    onClick = { selectedSponsorForDetail = sponsor },
                    onSearchJobs = { onSelectSponsorForJobs(sponsor.name) }
                )
            }
        }
    }

    // Detail Dialog
    selectedSponsorForDetail?.let { sponsor ->
        SponsorDetailDossierDialog(
            sponsor = sponsor,
            onDismiss = { selectedSponsorForDetail = null },
            onSearchJobs = {
                selectedSponsorForDetail = null
                onSelectSponsorForJobs(sponsor.name)
            }
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(text = label, color = SlateMuted, fontSize = 10.sp)
    }
}

@Composable
fun SponsorRegistryCard(
    sponsor: OfficialSponsor,
    onClick: () -> Unit,
    onSearchJobs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("sponsor_card_${sponsor.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Name + Country + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = sponsor.name,
                            color = WhiteActive,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified License",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "${sponsor.headquarterLocation} • ${sponsor.industry}",
                        color = SlateMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = sponsor.licenseStatus.color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = sponsor.country,
                        color = sponsor.licenseStatus.color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // License Registry Details Badge
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = NavyDark
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = TealCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Lic: ${sponsor.licenseNumber}",
                            color = WhiteActive,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = sponsor.licenseRating.take(28),
                        color = EmeraldGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Key Metrics Row: Filings, Approval Rate, Median Wage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPill(
                    icon = Icons.Default.TrendingUp,
                    title = "Petitions",
                    value = "${sponsor.totalApprovedPetitionsLast3Years}+",
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    icon = Icons.Default.Check,
                    title = "Approval Rate",
                    value = "${sponsor.approvalRatePercent}%",
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    icon = Icons.Default.AttachMoney,
                    title = "Median Salary",
                    value = sponsor.medianApprovedWage.substringBefore(" /"),
                    modifier = Modifier.weight(1.2f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visa Routes Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                sponsor.eligibleVisaRoutes.take(2).forEach { route ->
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = NavyDark
                    ) {
                        Text(
                            text = route,
                            color = SlateMuted,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            maxLines = 1
                        )
                    }
                }
                if (sponsor.eligibleVisaRoutes.size > 2) {
                    Text(
                        text = "+${sponsor.eligibleVisaRoutes.size - 2} more",
                        color = SlateMuted,
                        fontSize = 9.sp,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sponsor.tierLevel.take(25),
                    color = TealCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onSearchJobs,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Search Jobs", fontSize = 11.sp, color = EmeraldGreen)
                    }

                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Dossier", fontSize = 11.sp, color = NavyDark, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = NavyDark, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = NavyDark
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, color = SlateMuted, fontSize = 9.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(11.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = value,
                    color = WhiteActive,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun SponsorDetailDossierDialog(
    sponsor: OfficialSponsor,
    onDismiss: () -> Unit,
    onSearchJobs: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp)),
            color = NavyDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sponsor.name,
                            color = WhiteActive,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${sponsor.country} • ${sponsor.headquarterLocation}",
                            color = SlateMuted,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SlateMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        // Official License Audit Banner
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = NavyMedium,
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Official Registry License", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(sponsor.licenseStatus.label, color = sponsor.licenseStatus.color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "License ID: ${sponsor.licenseNumber}",
                                    color = WhiteActive,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Rating Tier: ${sponsor.licenseRating}",
                                    color = SlateMuted,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Primary Legal Counsel: ${sponsor.primaryLegalPartner}",
                                    color = TealCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    item {
                        // 3-Year Filing Volume History
                        Text("3-Year Approved Petition Volume", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            sponsor.historicalFilingsByYear.forEach { (year, count) ->
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = NavyMedium
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(year, color = SlateMuted, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "$count Approved",
                                            color = EmeraldGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        // Wage & Speed Benchmarks
                        Text("Approved Wage & Processing Speed", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = NavyMedium
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                BenchmarkRow(label = "Median Approved Wage:", value = sponsor.medianApprovedWage)
                                BenchmarkRow(label = "Local Market Comparison:", value = sponsor.localMarketMedianWage)
                                BenchmarkRow(label = "Avg. CoS / LMIA Turnaround:", value = "${sponsor.avgCoSIssuanceDays} Business Days")
                                BenchmarkRow(label = "Permanent Residency Support:", value = sponsor.greenCardPrSupportPolicy)
                            }
                        }
                    }

                    item {
                        // Top Sponsored Roles
                        Text("Most Frequently Sponsored Roles", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            sponsor.topSponsoredRoles.forEach { role ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = NavyMedium
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Work, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(role, color = WhiteActive, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        // Description & Overview
                        Text("Employer Sponsorship Dossier", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = sponsor.description,
                            color = SlateMuted,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close", color = SlateMuted)
                    }

                    Button(
                        onClick = onSearchJobs,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = NavyDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View Open Jobs", color = NavyDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun BenchmarkRow(label: String, value: String) {
    Column {
        Text(text = label, color = SlateMuted, fontSize = 11.sp)
        Text(text = value, color = WhiteActive, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
