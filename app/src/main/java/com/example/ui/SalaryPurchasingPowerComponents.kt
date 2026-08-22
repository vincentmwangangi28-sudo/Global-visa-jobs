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
import com.example.ui.theme.*

data class DestinationTaxModel(
    val country: String,
    val currencySymbol: String,
    val currencyCode: String,
    val flag: String,
    val effectiveTaxRatePercent: Double, // Approx effective tax + social security
    val avgMonthlyRent1BedCenter: Double,
    val avgMonthlyUtilities: Double,
    val avgMonthlyGroceries: Double,
    val avgMonthlyTransport: Double,
    val healthcareDeductionMonthly: Double,
    val exchangeRateToUsd: Double // 1 unit in USD
)

val destinationTaxModels = listOf(
    DestinationTaxModel("United Kingdom", "£", "GBP", "🇬🇧", 0.28, 1450.0, 210.0, 320.0, 160.0, 85.0, 1.28),
    DestinationTaxModel("Canada", "CA$", "CAD", "🇨🇦", 0.29, 2100.0, 190.0, 440.0, 130.0, 60.0, 0.74),
    DestinationTaxModel("Germany", "€", "EUR", "🇩🇪", 0.38, 1150.0, 260.0, 350.0, 90.0, 280.0, 1.08),
    DestinationTaxModel("United States", "$", "USD", "🇺🇸", 0.25, 2300.0, 220.0, 480.0, 140.0, 180.0, 1.0),
    DestinationTaxModel("Australia", "AU$", "AUD", "🇦🇺", 0.30, 2200.0, 240.0, 460.0, 160.0, 120.0, 0.65),
    DestinationTaxModel("Netherlands", "€", "EUR", "🇳🇱", 0.37, 1600.0, 230.0, 380.0, 110.0, 140.0, 1.08),
    DestinationTaxModel("United Arab Emirates", "AED ", "AED", "🇦🇪", 0.00, 6500.0, 800.0, 1600.0, 400.0, 350.0, 0.27),
    DestinationTaxModel("Singapore", "S$", "SGD", "🇸🇬", 0.12, 3200.0, 210.0, 520.0, 150.0, 180.0, 0.75)
)

@Composable
fun SalaryPurchasingPowerCalculator(viewModel: JobViewModel) {
    var selectedCountryName by remember { mutableStateOf("United Kingdom") }
    var annualGrossSalary by remember { mutableStateOf(65000.0) }
    var isCityCenterRent by remember { mutableStateOf(true) }

    val model = destinationTaxModels.find { it.country == selectedCountryName } ?: destinationTaxModels[0]

    // Calculations
    val monthlyGross = annualGrossSalary / 12.0
    val monthlyTaxAndSocial = monthlyGross * model.effectiveTaxRatePercent
    val monthlyNet = monthlyGross - monthlyTaxAndSocial

    val adjustedRent = if (isCityCenterRent) model.avgMonthlyRent1BedCenter else model.avgMonthlyRent1BedCenter * 0.72
    val totalLivingCost = adjustedRent + model.avgMonthlyUtilities + model.avgMonthlyGroceries + model.avgMonthlyTransport + model.healthcareDeductionMonthly
    val monthlySavings = monthlyNet - totalLivingCost
    val savingsRate = if (monthlyNet > 0) ((monthlySavings / monthlyNet) * 100.0).coerceIn(-100.0, 100.0) else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("salary_purchasing_power_calc")
    ) {
        // Hero Header
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
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Global Purchasing Power & Net Pay",
                        color = WhiteActive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Real net take-home salary after destination income tax, mandatory social deductions, and local cost of living.",
                        color = SlateMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Country Selector Chips
        Text(text = "DESTINATION COUNTRY:", color = TealCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(6.dp))

        ScrollableTabRow(
            selectedTabIndex = destinationTaxModels.indexOfFirst { it.country == selectedCountryName }.coerceAtLeast(0),
            containerColor = NavyDark,
            contentColor = EmeraldGreen,
            edgePadding = 0.dp,
            divider = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            destinationTaxModels.forEach { item ->
                val isSelected = item.country == selectedCountryName
                Tab(
                    selected = isSelected,
                    onClick = {
                        selectedCountryName = item.country
                        // adjust default salary appropriately for currency scale
                        annualGrossSalary = when (item.currencyCode) {
                            "AED" -> 240000.0
                            "SGD" -> 90000.0
                            "CAD", "AUD" -> 85000.0
                            "EUR" -> 62000.0
                            "USD" -> 95000.0
                            else -> 60000.0
                        }
                    },
                    text = {
                        Text(
                            text = "${item.flag} ${item.country}",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) EmeraldGreen else SlateMuted
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Annual Gross Salary Slider / Input
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            border = BorderStroke(1.dp, NavyLight),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Offered Annual Gross Salary:",
                        color = SlateMuted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${model.currencySymbol}${String.format("%,.0f", annualGrossSalary)} / yr",
                        color = EmeraldGreen,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val minSal = when (model.currencyCode) {
                    "AED" -> 60000f
                    else -> 25000f
                }
                val maxSal = when (model.currencyCode) {
                    "AED" -> 600000f
                    else -> 200000f
                }

                Slider(
                    value = annualGrossSalary.toFloat().coerceIn(minSal, maxSal),
                    onValueChange = { annualGrossSalary = it.toDouble() },
                    valueRange = minSal..maxSal,
                    colors = SliderDefaults.colors(
                        thumbColor = EmeraldGreen,
                        activeTrackColor = EmeraldGreen,
                        inactiveTrackColor = NavyDark
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${model.currencySymbol}${String.format("%,.0f", minSal.toDouble())}", color = SlateMuted, fontSize = 10.sp)
                    Text("${model.currencySymbol}${String.format("%,.0f", maxSal.toDouble())}", color = SlateMuted, fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Accommodation toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavyDark)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isCityCenterRent) "🏢 Living in City Center (Metro)" else "🏡 Living in Outer Suburbs",
                        color = WhiteActive,
                        fontSize = 11.sp
                    )
                    Switch(
                        checked = isCityCenterRent,
                        onCheckedChange = { isCityCenterRent = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldGreen,
                            checkedTrackColor = EmeraldGreen.copy(alpha = 0.3f),
                            uncheckedThumbColor = SlateMuted,
                            uncheckedTrackColor = NavyMedium
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Take Home vs Cost Breakdown
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            border = BorderStroke(1.dp, if (monthlySavings > 0) EmeraldGreen.copy(alpha = 0.4f) else CoralRed.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "MONTHLY FINANCIAL BREAKDOWN",
                    color = TealCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                FinancialRowItem("Monthly Gross Pay", "+${model.currencySymbol}${String.format("%,.0f", monthlyGross)}", WhiteActive)
                FinancialRowItem("Income Tax & Social Security (~${(model.effectiveTaxRatePercent * 100).toInt()}%)", "-${model.currencySymbol}${String.format("%,.0f", monthlyTaxAndSocial)}", CoralRed)
                Divider(color = NavyLight, modifier = Modifier.padding(vertical = 6.dp))
                FinancialRowItem("Net Take-Home Pay (Monthly)", "${model.currencySymbol}${String.format("%,.0f", monthlyNet)}", EmeraldGreen, isBold = true)

                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "ESTIMATED LOCAL MONTHLY EXPENSES:", color = SlateMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))

                FinancialRowItem("Rent (1-Bed Apartment)", "-${model.currencySymbol}${String.format("%,.0f", adjustedRent)}", SlateMuted)
                FinancialRowItem("Utilities & High-Speed Internet", "-${model.currencySymbol}${String.format("%,.0f", model.avgMonthlyUtilities)}", SlateMuted)
                FinancialRowItem("Groceries & Food", "-${model.currencySymbol}${String.format("%,.0f", model.avgMonthlyGroceries)}", SlateMuted)
                FinancialRowItem("Public Transport / Commute", "-${model.currencySymbol}${String.format("%,.0f", model.avgMonthlyTransport)}", SlateMuted)
                if (model.healthcareDeductionMonthly > 0) {
                    FinancialRowItem("Health Surcharge / Private Care", "-${model.currencySymbol}${String.format("%,.0f", model.healthcareDeductionMonthly)}", SlateMuted)
                }

                Divider(color = NavyLight, modifier = Modifier.padding(vertical = 6.dp))

                FinancialRowItem("Total Living Expenses", "-${model.currencySymbol}${String.format("%,.0f", totalLivingCost)}", AmberGold)

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Net Savings Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (monthlySavings > 0) EmeraldGreen.copy(alpha = 0.15f) else CoralRed.copy(alpha = 0.15f))
                        .border(1.dp, if (monthlySavings > 0) EmeraldGreen else CoralRed, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (monthlySavings > 0) "Est. Monthly Savings Margin" else "Monthly Deficit Warning",
                                color = if (monthlySavings > 0) EmeraldGreen else CoralRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Savings Rate: ${String.format("%.1f", savingsRate)}% of net salary",
                                color = SlateMuted,
                                fontSize = 10.sp
                            )
                        }

                        Text(
                            text = "${model.currencySymbol}${String.format("%,.0f", monthlySavings)} / mo",
                            color = if (monthlySavings > 0) EmeraldGreen else CoralRed,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Country Relocation Quick Advice
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            border = BorderStroke(1.dp, NavyLight),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = TealCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Purchasing Power Note (${model.country}):", color = TealCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                val advice = when (model.country) {
                    "United Kingdom" -> "The minimum Skilled Worker salary threshold is £38,700/year (or £29,000 for ISL/shortage codes). London cost of living is approx 25% higher than regional UK cities (Manchester, Birmingham, Edinburgh)."
                    "Canada" -> "Income tax includes combined Federal & Provincial rates. The average salary in tech is CAD $90,000+. Toronto and Vancouver represent higher rent markets compared to Calgary and Montreal."
                    "Germany" -> "Statutory health, pension, and unemployment insurance contribute ~19% on top of progressive income tax. However, high-quality public healthcare, transport, and tuition-free education offset costs."
                    "United States" -> "Salaries in tech/engineering are significantly higher ($100k-$180k+), but employer-sponsored health insurance deductibles and state taxes (e.g. CA, NY) should be factored in."
                    "United Arab Emirates" -> "0% personal income tax allows 100% take-home pay, making UAE a premier wealth accumulation hub for international professionals."
                    else -> "Consider remote/hybrid flexibility to optimize accommodation costs outside expensive central business districts."
                }
                Text(
                    text = advice,
                    color = SlateMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun FinancialRowItem(
    label: String,
    value: String,
    valueColor: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (isBold) WhiteActive else SlateMuted,
            fontSize = if (isBold) 12.sp else 11.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = if (isBold) 13.sp else 11.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}
