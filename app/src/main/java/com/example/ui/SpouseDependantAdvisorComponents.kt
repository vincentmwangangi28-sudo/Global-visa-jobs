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
import com.example.ui.theme.*

data class FamilyRelocationPathway(
    val id: String,
    val country: String,
    val primaryVisaCategory: String,
    val spouseWorkRights: String, // "Unrestricted Open Work Permit", "Requires Separate EAD", "Full Open Labor Market"
    val spouseWorkSummary: String,
    val childrenEducationRights: String, // "Free Public K-12 Schooling", "Subsidized State Education", "International School Fees Apply"
    val healthcareScheme: String, // "Full NHS Coverage via IHS", "Provincial Public Health (OHIP/MSP)", "Statutory Health Insurance (TK/AOK)"
    val ihsOrHealthCostAnnualPerPerson: String,
    val monthlyMaintenanceProofPerDependant: String,
    val minimumPrimarySalaryForFamily: String,
    val familyReunionProcessingTimeWeeks: Int,
    val permanentResidencyIncluded: Boolean,
    val keyLegalAdvice: String
)

object SpouseDependantRepository {
    val pathways = listOf(
        FamilyRelocationPathway(
            id = "fam_uk_sw",
            country = "United Kingdom",
            primaryVisaCategory = "Skilled Worker Visa (Code 2136 / Standard)",
            spouseWorkRights = "Full Unrestricted Open Work Permit",
            spouseWorkSummary = "Spouse/partner can take any full-time, part-time, or self-employed work without employer sponsorship, subject to main applicant holding an eligible skilled role.",
            childrenEducationRights = "Free State-Funded Public K-12 Schooling",
            healthcareScheme = "Full NHS Access (Immigration Health Surcharge applies)",
            ihsOrHealthCostAnnualPerPerson = "£1,035 / year (Adults) • £776 / year (Children under 18)",
            monthlyMaintenanceProofPerDependant = "£285 for partner + £315 for first child + £200 per subsequent child (held 28 days)",
            minimumPrimarySalaryForFamily = "£38,700 / year (or £29,000 for previous entrants / ISL shortage codes)",
            familyReunionProcessingTimeWeeks = 3,
            permanentResidencyIncluded = true,
            keyLegalAdvice = "If primary applicant is sponsored under Health & Care Worker route, spouse still retains full work rights and is 100% exempt from the £1,035/yr IHS healthcare fee."
        ),
        FamilyRelocationPathway(
            id = "fam_ca_gts",
            country = "Canada",
            primaryVisaCategory = "Global Talent Stream / LMIA Work Permit (TEER 0, 1, 2, 3)",
            spouseWorkRights = "Spousal Open Work Permit (SOWP)",
            spouseWorkSummary = "Spouse is eligible for a C41/C42 Spousal Open Work Permit valid for the same duration as primary worker's permit, with no job offer required.",
            childrenEducationRights = "Free Public Elementary & Secondary Education (K-12)",
            healthcareScheme = "Provincial Public Health Insurance (e.g., OHIP in Ontario, MSP in BC, RAMQ in Quebec)",
            ihsOrHealthCostAnnualPerPerson = "CAD $0 (Covered by provincial taxes once 6-month employment begins)",
            monthlyMaintenanceProofPerDependant = "CAD $4,000 for spouse + CAD $3,000 per dependent child (settlement buffer)",
            minimumPrimarySalaryForFamily = "CAD $60,000 / year (Meets Low-Income Cut-Off LICO threshold)",
            familyReunionProcessingTimeWeeks = 4,
            permanentResidencyIncluded = true,
            keyLegalAdvice = "SOWP applications can be submitted simultaneously with the primary applicant's Global Talent Stream work permit for expedited 2-week dual processing."
        ),
        FamilyRelocationPathway(
            id = "fam_de_eubc",
            country = "Germany",
            primaryVisaCategory = "EU Blue Card (§18g AufenthG - New 2024/2026 Rules)",
            spouseWorkRights = "Immediate Unrestricted Work Authorization",
            spouseWorkSummary = "Spouses of EU Blue Card holders have immediate, unrestricted access to the German labor market without prior German language (A1) certificate requirements.",
            childrenEducationRights = "Free Public Gymnasium / Realschule + €250/mo Kindergeld Child Benefit",
            healthcareScheme = "Statutory Family Health Insurance (Gesetzliche Krankenversicherung - TK/Barmer/AOK)",
            ihsOrHealthCostAnnualPerPerson = "€0 Extra (Spouse and children are co-insured for free under primary applicant's GKV plan)",
            monthlyMaintenanceProofPerDependant = "None required if primary gross salary satisfies Blue Card threshold (€41,041 MINT / €45,300 standard)",
            minimumPrimarySalaryForFamily = "€41,041 / year (MINT Shortage) or €45,300 / year (Standard)",
            familyReunionProcessingTimeWeeks = 6,
            permanentResidencyIncluded = true,
            keyLegalAdvice = "Germany pays €250 per month per child in Kindergeld (child benefit) directly to the expat family from month one of official municipal registration (Anmeldung)."
        ),
        FamilyRelocationPathway(
            id = "fam_us_h1b",
            country = "United States",
            primaryVisaCategory = "H-1B Specialty Occupation / L-1 Intra-Company",
            spouseWorkRights = "Conditional H-4 EAD (or Immediate L-2S)",
            spouseWorkSummary = "H-4 spouses can only apply for employment authorization (EAD) AFTER the primary worker has an approved Form I-140 Immigrant Petition. L-2 spouses (L-1 dependent) have immediate work rights.",
            childrenEducationRights = "Free Public K-12 District Schooling",
            healthcareScheme = "Employer-Sponsored Group Health Insurance (PPO/HDHP)",
            ihsOrHealthCostAnnualPerPerson = "$1,800 - $4,200 / year (Employer family premium deduction)",
            monthlyMaintenanceProofPerDependant = "Affidavit of Support (Form I-134) showing income 125% above HHS Poverty Guidelines",
            minimumPrimarySalaryForFamily = "$75,000 / year",
            familyReunionProcessingTimeWeeks = 8,
            permanentResidencyIncluded = true,
            keyLegalAdvice = "For H-1B holders wanting immediate spousal employment, negotiate Day-1 I-140 filing in your employment contract to unlock H-4 EAD within the first 12-18 months."
        ),
        FamilyRelocationPathway(
            id = "fam_au_tss",
            country = "Australia",
            primaryVisaCategory = "Temporary Skill Shortage (subclass 482 Medium-Term)",
            spouseWorkRights = "Unrestricted Secondary Visa Work Authorization",
            spouseWorkSummary = "Secondary visa holders (spouse/de facto) enjoy full work and study rights in Australia for the full duration of the subclass 482 visa.",
            childrenEducationRights = "Public Schooling (State fees apply in NSW/ACT ~$5k/yr; Free in WA/SA/NT/QLD)",
            healthcareScheme = "Overseas Visitor Health Cover (OVHC Mandatory)",
            ihsOrHealthCostAnnualPerPerson = "AUD $2,400 - $3,600 / year for comprehensive family OVHC policy",
            monthlyMaintenanceProofPerDependant = "AUD $7,300 for partner + AUD $3,100 per child (annual support threshold)",
            minimumPrimarySalaryForFamily = "AUD $73,150 / year (TSMIT Temporary Skilled Migration Income Threshold)",
            familyReunionProcessingTimeWeeks = 4,
            permanentResidencyIncluded = true,
            keyLegalAdvice = "Children attending government public schools in Western Australia (WA) and South Australia (SA) are exempt from international student tuition fees under subclass 482."
        ),
        FamilyRelocationPathway(
            id = "fam_nl_hsm",
            country = "Netherlands",
            primaryVisaCategory = "Highly Skilled Migrant (Kennismigrant / 30% Ruling)",
            spouseWorkRights = "Free Labor Market Access (No TWV Required)",
            spouseWorkSummary = "Spouse receives residence permit with 'Arbeid is vrij toegestaan' (work freely permitted), allowing employment and freelancing without a separate work permit.",
            childrenEducationRights = "Free Dutch Public Primary & Secondary Education + Dutch Language Immersion (Taalklas)",
            healthcareScheme = "Dutch Basic Health Insurance (Zorgverzekering)",
            ihsOrHealthCostAnnualPerPerson = "€1,600 / year per adult (Children under 18 covered 100% free with no deductible)",
            monthlyMaintenanceProofPerDependant = "€0 extra if primary meets IND monthly threshold (€3,909/mo for under 30, €5,331/mo for 30+)",
            minimumPrimarySalaryForFamily = "€46,908 / year (Age <30) or €63,972 / year (Age 30+)",
            familyReunionProcessingTimeWeeks = 3,
            permanentResidencyIncluded = true,
            keyLegalAdvice = "Children under 18 receive free Dutch statutory healthcare with zero premiums and zero deductibles. The 30% tax ruling substantially boosts net income for family living."
        ),
        FamilyRelocationPathway(
            id = "fam_jp_hsp",
            country = "Japan",
            primaryVisaCategory = "Highly Skilled Professional (HSP Points Visa)",
            spouseWorkRights = "Full-Time Work Without Qualification Limits",
            spouseWorkSummary = "Spouse of an HSP visa holder can engage in full-time academic research, specialized humanities, or international business without needing independent educational degrees.",
            childrenEducationRights = "Free Public Elementary and Junior High (Local Japanese Schools)",
            healthcareScheme = "National / Employee Health Insurance (Shakai Hoken)",
            ihsOrHealthCostAnnualPerPerson = "70% medical costs paid by state; 30% copay capped by High-Cost Medical Care Benefit",
            monthlyMaintenanceProofPerDependant = "¥8,000,000/yr combined household income if sponsoring parents or domestic helper",
            minimumPrimarySalaryForFamily = "¥6,000,000 / year (JPY)",
            familyReunionProcessingTimeWeeks = 4,
            permanentResidencyIncluded = true,
            keyLegalAdvice = "HSP visa holders with household income over ¥8M can also sponsor their own parents (or spouse's parents) to relocate to Japan to help care for children under 7."
        )
    )
}

@Composable
fun SpouseDependantAdvisorHub(
    modifier: Modifier = Modifier
) {
    var selectedCountry by remember { mutableStateOf("United Kingdom") }
    var spouseRelocating by remember { mutableStateOf(true) }
    var childrenCount by remember { mutableIntStateOf(1) }
    var showBudgetCalculator by remember { mutableStateOf(false) }

    val countries = SpouseDependantRepository.pathways.map { it.country }
    val currentPathway = remember(selectedCountry) {
        SpouseDependantRepository.pathways.find { it.country == selectedCountry } ?: SpouseDependantRepository.pathways.first()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("spouse_dependant_advisor_screen")
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
                    .background(Color(0xFF9C27B0).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFFCE93D8), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FamilyRestroom,
                    contentDescription = null,
                    tint = Color(0xFFCE93D8),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Spouse & Dependant Advisor",
                    color = WhiteActive,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Family work permits, schooling, health & maintenance funds",
                    color = SlateMuted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Country Selector Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(countries) { country ->
                FilterChip(
                    selected = selectedCountry == country,
                    onClick = { selectedCountry = country },
                    label = { Text(country, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldGreen,
                        selectedLabelColor = NavyDark,
                        containerColor = NavyMedium,
                        labelColor = SlateMuted
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (selectedCountry == country) EmeraldGreen else NavyLight,
                        enabled = true,
                        selected = selectedCountry == country
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Toggle Mode: Overview Matrix vs Interactive Cost Calculator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !showBudgetCalculator,
                onClick = { showBudgetCalculator = false },
                label = { Text("Family Relocation Matrix", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NavyLight,
                    selectedLabelColor = WhiteActive,
                    containerColor = NavyDark,
                    labelColor = SlateMuted
                ),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = showBudgetCalculator,
                onClick = { showBudgetCalculator = true },
                label = { Text("Family Cost Calculator", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldGreen,
                    selectedLabelColor = NavyDark,
                    containerColor = NavyDark,
                    labelColor = SlateMuted
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (!showBudgetCalculator) {
            // Relocation Matrix View
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Visa Pathway Overview Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyMedium)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentPathway.primaryVisaCategory,
                                    color = EmeraldGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = EmeraldGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${currentPathway.familyReunionProcessingTimeWeeks} wks turnaround",
                                        color = EmeraldGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Minimum Primary Salary: ${currentPathway.minimumPrimarySalaryForFamily}",
                                color = WhiteActive,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                item {
                    // Spousal Work Rights Card
                    RightsDetailCard(
                        icon = Icons.Default.Work,
                        iconTint = TealCyan,
                        title = "Spouse / Partner Work Authorization",
                        statusBadge = currentPathway.spouseWorkRights,
                        description = currentPathway.spouseWorkSummary
                    )
                }

                item {
                    // Children Schooling & Public Education
                    RightsDetailCard(
                        icon = Icons.Default.School,
                        iconTint = Color(0xFFFBC02D),
                        title = "Children K-12 Schooling & Child Benefit",
                        statusBadge = currentPathway.childrenEducationRights,
                        description = "State public education eligibility and local registration rules for dependent children accompanying the primary visa holder."
                    )
                }

                item {
                    // Healthcare & Insurance Scheme
                    RightsDetailCard(
                        icon = Icons.Default.HealthAndSafety,
                        iconTint = EmeraldGreen,
                        title = "Healthcare System & Insurance Fees",
                        statusBadge = currentPathway.healthcareScheme,
                        description = "Annual Surcharge / Cost: ${currentPathway.ihsOrHealthCostAnnualPerPerson}"
                    )
                }

                item {
                    // Maintenance Funds Proof
                    RightsDetailCard(
                        icon = Icons.Default.AccountBalance,
                        iconTint = Color(0xFF90CAF9),
                        title = "Mandatory Bank Maintenance Proof",
                        statusBadge = "Proof of Funds Required",
                        description = currentPathway.monthlyMaintenanceProofPerDependant
                    )
                }

                item {
                    // Legal Insight & Pro Tip Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = NavyDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, TealCyan)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = TealCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Strategic Relocation Counsel", color = TealCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(currentPathway.keyLegalAdvice, color = WhiteActive, fontSize = 12.sp, lineHeight = 17.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // Interactive Family Budget Calculator View
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyMedium)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Configure Your Family Composition", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Spouse toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Spouse / De Facto Partner", color = WhiteActive, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text("Include spousal visa + open work permit fees", color = SlateMuted, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = spouseRelocating,
                                    onCheckedChange = { spouseRelocating = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = NavyDark,
                                        checkedTrackColor = EmeraldGreen
                                    )
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = NavyLight)

                            // Children count stepper
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Number of Dependent Children", color = WhiteActive, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text("Under 18 years old", color = SlateMuted, fontSize = 11.sp)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { if (childrenCount > 0) childrenCount-- },
                                        enabled = childrenCount > 0,
                                        modifier = Modifier.size(32.dp).background(NavyDark, CircleShape)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = WhiteActive, modifier = Modifier.size(16.dp))
                                    }

                                    Text(
                                        text = "$childrenCount",
                                        color = EmeraldGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    )

                                    IconButton(
                                        onClick = { if (childrenCount < 6) childrenCount++ },
                                        enabled = childrenCount < 6,
                                        modifier = Modifier.size(32.dp).background(NavyDark, CircleShape)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = WhiteActive, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    // Calculated Cost Breakdown Card
                    val adultCount = 1 + (if (spouseRelocating) 1 else 0)
                    val totalDependants = (if (spouseRelocating) 1 else 0) + childrenCount

                    val estimatedVisaFees = when (selectedCountry) {
                        "United Kingdom" -> (adultCount * 624) + (childrenCount * 624)
                        "Canada" -> (adultCount * 155) + (if (spouseRelocating) 100 else 0) + (childrenCount * 100) // SOWP + Visitor
                        "Germany" -> (adultCount * 75) + (childrenCount * 37)
                        "United States" -> (adultCount * 190) + (childrenCount * 190)
                        "Australia" -> 3255 + (if (spouseRelocating) 3255 else 0) + (childrenCount * 815)
                        else -> (adultCount * 350) + (childrenCount * 200)
                    }

                    val estimatedHealthcareSurchargeAnnual = when (selectedCountry) {
                        "United Kingdom" -> (adultCount * 1035) + (childrenCount * 776)
                        "Australia" -> (adultCount * 1400) + (childrenCount * 600)
                        else -> 0 // Germany, Canada, NL covered under standard taxes/statutory
                    }

                    val estimatedMaintenanceSavingsBuffer = when (selectedCountry) {
                        "United Kingdom" -> 1270 + (if (spouseRelocating) 285 else 0) + (childrenCount * 315)
                        "Canada" -> 10000 + (if (spouseRelocating) 4000 else 0) + (childrenCount * 3000)
                        "Germany" -> 2500 + (if (spouseRelocating) 1500 else 0) + (childrenCount * 1000)
                        else -> 8000 + (if (spouseRelocating) 3000 else 0) + (childrenCount * 2000)
                    }

                    val currencySymbol = when (selectedCountry) {
                        "United Kingdom" -> "£"
                        "Canada" -> "CAD $"
                        "Germany", "Netherlands" -> "€"
                        "Australia" -> "AUD $"
                        "Japan" -> "¥"
                        else -> "$"
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyMedium)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Estimated Family Relocation Settlement Budget", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            BudgetBreakdownRow(
                                title = "Government Visa & Application Fees:",
                                value = "$currencySymbol$estimatedVisaFees",
                                subtitle = "Covers primary applicant + $totalDependants dependants"
                            )

                            if (estimatedHealthcareSurchargeAnnual > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                BudgetBreakdownRow(
                                    title = "Annual Health Surcharge / OVHC Insurance:",
                                    value = "$currencySymbol$estimatedHealthcareSurchargeAnnual",
                                    subtitle = "Annual family coverage (NHS IHS / OVHC)"
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            BudgetBreakdownRow(
                                title = "Recommended Proof of Funds / 28-Day Bank Buffer:",
                                value = "$currencySymbol$estimatedMaintenanceSavingsBuffer",
                                subtitle = "Shows ability to sustain initial apartment deposit & living costs"
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = EmeraldGreen.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Estimated Total Initial Capital Required:", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(
                                        text = "$currencySymbol${estimatedVisaFees + estimatedHealthcareSurchargeAnnual + estimatedMaintenanceSavingsBuffer}",
                                        color = EmeraldGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
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

@Composable
private fun RightsDetailCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    statusBadge: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavyMedium)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = WhiteActive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = NavyDark
            ) {
                Text(
                    text = statusBadge,
                    color = iconTint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                color = SlateMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun BudgetBreakdownRow(
    title: String,
    value: String,
    subtitle: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = WhiteActive, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(value, color = EmeraldGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Text(subtitle, color = SlateMuted, fontSize = 10.sp)
    }
}
