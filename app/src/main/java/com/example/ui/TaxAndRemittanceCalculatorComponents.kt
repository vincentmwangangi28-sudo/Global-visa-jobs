package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

data class DtaaTreatyInfo(
    val originCountry: String,
    val destinationCountry: String,
    val hasDtaaTreaty: Boolean,
    val treatySummary: String,
    val foreignTaxCreditRule: String,
    val remittanceTaxStatus: String, // "Tax-Free Remittance", "Exempt from Double Tax"
    val days183ResidencyRule: String
)

data class RemittanceTransferOption(
    val providerName: String,
    val transferFee: String,
    val exchangeRateText: String,
    val transferSpeed: String,
    val totalReceivedHomeCurrency: String,
    val isBestValue: Boolean
)

object TaxRemittanceRepository {
    val sampleTreaties = listOf(
        DtaaTreatyInfo(
            originCountry = "Kenya",
            destinationCountry = "United Kingdom",
            hasDtaaTreaty = true,
            treatySummary = "UK-Kenya Double Taxation Agreement (Statutory Instrument 1977/1299). Employment income is taxable in the UK where duties are performed.",
            foreignTaxCreditRule = "Taxes paid to HMRC in the UK are fully creditable against any Kenya Revenue Authority (KRA) obligations under the Foreign Tax Credit relief framework.",
            remittanceTaxStatus = "100% Tax-Free Inbound Remittance to Kenya bank / M-Pesa accounts",
            days183ResidencyRule = "If you spend 183+ days in the UK during the tax year, you are classified as UK Tax Resident (HMRC Statutory Residence Test)."
        ),
        DtaaTreatyInfo(
            originCountry = "Nigeria",
            destinationCountry = "United Kingdom",
            hasDtaaTreaty = true,
            treatySummary = "Nigeria-UK Double Taxation Agreement. Prevents double assessment on personal income and company dividends.",
            foreignTaxCreditRule = "PAYE taxes deducted at source in the UK qualify for unilateral tax credit with the Federal Inland Revenue Service (FIRS).",
            remittanceTaxStatus = "Tax-Exempt Diasporan Remittance to Nigerian Domiciliary or Naira accounts",
            days183ResidencyRule = "Primary tax liability shifts entirely to the UK once non-resident status in Nigeria is established."
        ),
        DtaaTreatyInfo(
            originCountry = "India",
            destinationCountry = "Germany",
            hasDtaaTreaty = true,
            treatySummary = "India-Germany Comprehensive DTAA (Article 15: Dependent Personal Services).",
            foreignTaxCreditRule = "German Lohnsteuer (income tax) qualifies for Article 23 Foreign Tax Credit relief on Indian Income Tax Return (ITR).",
            remittanceTaxStatus = "Transfers to NRE (Non-Resident External) bank accounts are completely exempt from Indian income tax and wealth tax.",
            days183ResidencyRule = "Standard 183-day residence rule applies for tax year allocation."
        ),
        DtaaTreatyInfo(
            originCountry = "Philippines",
            destinationCountry = "Canada",
            hasDtaaTreaty = true,
            treatySummary = "Canada-Philippines Tax Treaty. Protects Overseas Filipino Workers (OFWs) and immigrant workers from dual taxation.",
            foreignTaxCreditRule = "CRA income taxes paid in Canada offset BIR income obligations in the Philippines.",
            remittanceTaxStatus = "OFW remittance sent to family in the Philippines is completely tax-exempt under RA 10022.",
            days183ResidencyRule = "Canada Revenue Agency (CRA) deems worldwide income taxable once significant residential ties (home, spouse, bank accounts) are formed in Canada."
        )
    )
}

@Composable
fun TaxAndRemittanceCalculatorHub(
    modifier: Modifier = Modifier
) {
    var originCountry by remember { mutableStateOf("Kenya") }
    var destinationCountry by remember { mutableStateOf("United Kingdom") }
    var monthlyGrossEarnings by remember { mutableFloatStateOf(4500f) } // in destination currency
    var monthlyRemittancePercentage by remember { mutableFloatStateOf(20f) } // e.g. 20%
    var activeSubTab by remember { mutableIntStateOf(0) } // 0: Remittance Engine, 1: Double Tax Treaty (DTAA)

    val originCountries = listOf("Kenya", "Nigeria", "India", "Philippines", "Ghana", "South Africa")
    val destinationCountries = listOf("United Kingdom", "Canada", "Germany", "United States", "Australia")

    val currentDtaa = remember(originCountry, destinationCountry) {
        TaxRemittanceRepository.sampleTreaties.find {
            it.originCountry == originCountry && it.destinationCountry == destinationCountry
        } ?: TaxRemittanceRepository.sampleTreaties.first()
    }

    val currencySymbol = when (destinationCountry) {
        "United Kingdom" -> "£"
        "Canada" -> "CAD $"
        "Germany" -> "€"
        "Australia" -> "AUD $"
        else -> "$"
    }

    val homeCurrencySymbol = when (originCountry) {
        "Kenya" -> "KES"
        "Nigeria" -> "NGN"
        "India" -> "INR"
        "Philippines" -> "PHP"
        "Ghana" -> "GHS"
        "South Africa" -> "ZAR"
        else -> "USD"
    }

    val exchangeRateToHome = when (Pair(destinationCountry, originCountry)) {
        Pair("United Kingdom", "Kenya") -> 168.5 // 1 GBP = 168.5 KES
        Pair("United Kingdom", "Nigeria") -> 1950.0 // 1 GBP = 1950 NGN
        Pair("United Kingdom", "India") -> 108.2
        Pair("United Kingdom", "Philippines") -> 72.8
        Pair("United Kingdom", "Ghana") -> 19.8
        Pair("United Kingdom", "South Africa") -> 23.4
        Pair("Canada", "Kenya") -> 95.0
        Pair("Canada", "Nigeria") -> 1100.0
        Pair("Canada", "India") -> 61.5
        Pair("Germany", "Kenya") -> 141.0
        Pair("Germany", "Nigeria") -> 1630.0
        Pair("Germany", "India") -> 90.5
        else -> 130.0
    }

    val monthlyRemittanceAmount = (monthlyGrossEarnings * (monthlyRemittancePercentage / 100f))
    val estimatedHomeReceived = (monthlyRemittanceAmount * exchangeRateToHome).toLong()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("tax_remittance_screen")
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
                    imageVector = Icons.Default.CurrencyExchange,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Tax & Remittance Engine",
                    color = WhiteActive,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "DTAA double-tax treaties, home currency conversions & purchasing power",
                    color = SlateMuted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Country Selectors (Origin + Destination)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Origin Country", color = SlateMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(originCountries) { c ->
                        FilterChip(
                            selected = originCountry == c,
                            onClick = { originCountry = c },
                            label = { Text(c, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealCyan,
                                selectedLabelColor = NavyDark,
                                containerColor = NavyMedium,
                                labelColor = SlateMuted
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Destination Host Country", color = SlateMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(destinationCountries) { c ->
                        FilterChip(
                            selected = destinationCountry == c,
                            onClick = { destinationCountry = c },
                            label = { Text(c, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldGreen,
                                selectedLabelColor = NavyDark,
                                containerColor = NavyMedium,
                                labelColor = SlateMuted
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs: Remittance Calculator vs Double Tax Treaty
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                label = { Text("Home Remittance Power", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldGreen,
                    selectedLabelColor = NavyDark,
                    containerColor = NavyDark,
                    labelColor = SlateMuted
                ),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                label = { Text("Double Tax Treaty (DTAA)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TealCyan,
                    selectedLabelColor = NavyDark,
                    containerColor = NavyDark,
                    labelColor = SlateMuted
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (activeSubTab == 0) {
            // Remittance Calculator Sub-Tab
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    // Remittance Settings Slider Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyMedium)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Configure Monthly Remittance", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Monthly Remittance Ratio:", color = SlateMuted, fontSize = 12.sp)
                                Text("${monthlyRemittancePercentage.toInt()}% ($currencySymbol${monthlyRemittanceAmount.toInt()}/mo)", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Slider(
                                value = monthlyRemittancePercentage,
                                onValueChange = { monthlyRemittancePercentage = it },
                                valueRange = 5f..50f,
                                steps = 9,
                                colors = SliderDefaults.colors(
                                    thumbColor = EmeraldGreen,
                                    activeTrackColor = EmeraldGreen,
                                    inactiveTrackColor = NavyDark
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Converted Output Pill
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = NavyDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Estimated Value in $originCountry ($homeCurrencySymbol)", color = SlateMuted, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$homeCurrencySymbol ${String.format("%,d", estimatedHomeReceived)}",
                                        color = EmeraldGreen,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "1 $currencySymbol = $exchangeRateToHome $homeCurrencySymbol • Mid-Market FX",
                                        color = TealCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Home Purchasing Power Metric
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyMedium)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Purchasing Impact in Home Economy", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            val purchasingFactor = when (originCountry) {
                                "Kenya" -> (estimatedHomeReceived / 45000.0) // 45k KES avg living cost
                                "Nigeria" -> (estimatedHomeReceived / 350000.0)
                                "India" -> (estimatedHomeReceived / 30000.0)
                                "Philippines" -> (estimatedHomeReceived / 25000.0)
                                else -> 2.2
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${String.format("%.1f", purchasingFactor)}x Average Monthly Living Cost in $originCountry",
                                        color = WhiteActive,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "A single month's transfer covers extensive household rent, tuition, and family savings.",
                                        color = SlateMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Transfer Channels Comparison
                    Text("Compare Transfer Channels & Fees", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    val options = listOf(
                        RemittanceTransferOption("Wise (Direct Bank & Mobile Money)", "0.45% Low Fee ($currencySymbol${(monthlyRemittanceAmount * 0.0045).toInt()})", "Zero markup mid-market rate", "Instant to 2 Hours", "$homeCurrencySymbol ${String.format("%,d", (estimatedHomeReceived * 0.995).toLong())}", true),
                        RemittanceTransferOption("Remitly / WorldRemit", "Flat $currencySymbol 1.99 Fee", "0.8% FX spread margin", "Within 1 Hour", "$homeCurrencySymbol ${String.format("%,d", (estimatedHomeReceived * 0.985).toLong())}", false),
                        RemittanceTransferOption("Traditional Swift Wire", "$currencySymbol 15.00 Wire Fee", "2.5% FX bank spread markup", "1 to 3 Business Days", "$homeCurrencySymbol ${String.format("%,d", (estimatedHomeReceived * 0.96).toLong())}", false)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        options.forEach { opt ->
                            TransferOptionCard(option = opt)
                        }
                    }
                }
            }
        } else {
            // Double Tax Treaty (DTAA) Sub-Tab
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("$originCountry ↔ $destinationCountry DTAA", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = EmeraldGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = currentDtaa.remittanceTaxStatus,
                                        color = EmeraldGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(currentDtaa.treatySummary, color = WhiteActive, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }

                item {
                    DtaaDetailCard(
                        title = "Foreign Tax Credit (FTC) Relief",
                        body = currentDtaa.foreignTaxCreditRule,
                        icon = Icons.Default.ReceiptLong,
                        accentColor = TealCyan
                    )
                }

                item {
                    DtaaDetailCard(
                        title = "183-Day Physical Presence & Tax Residence",
                        body = currentDtaa.days183ResidencyRule,
                        icon = Icons.Default.CalendarToday,
                        accentColor = Color(0xFFFBC02D)
                    )
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = NavyDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, TealCyan)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = TealCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Dual Taxation Exemption Guarantee", color = TealCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Because $originCountry has a signed bilateral tax agreement with $destinationCountry, your salary earned abroad is NOT double-taxed when remitted home.",
                                    color = WhiteActive,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransferOptionCard(option: RemittanceTransferOption) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = NavyDark),
        border = if (option.isBestValue) androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(option.providerName, color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                if (option.isBestValue) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = EmeraldGreen
                    ) {
                        Text(
                            text = "Best Value",
                            color = NavyDark,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${option.transferFee} • ${option.transferSpeed}", color = SlateMuted, fontSize = 11.sp)
                Text(option.totalReceivedHomeCurrency, color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun DtaaDetailCard(
    title: String,
    body: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavyMedium)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(body, color = SlateMuted, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}
