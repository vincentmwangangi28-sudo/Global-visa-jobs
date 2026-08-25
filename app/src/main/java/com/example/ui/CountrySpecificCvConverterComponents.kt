package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

data class CountryCvFormat(
    val id: String,
    val countryName: String,
    val flag: String,
    val standardName: String,
    val pageRecommendation: String,
    val keySections: List<String>,
    val complianceRules: List<String>,
    val rightToWorkDeclaration: String,
    val sampleConvertedText: String
)

object CountryCvFormatDirectory {
    val formats = listOf(
        CountryCvFormat(
            id = "germany",
            countryName = "Germany / EU",
            flag = "🇩🇪",
            standardName = "Europass / DIN 5008 Tabellarischer Lebenslauf",
            pageRecommendation = "1 - 2 Pages strictly reverse chronological",
            keySections = listOf(
                "Persönliche Daten (Contact, Citizenship)",
                "Berufserfahrung (Reverse-chronological duties)",
                "Ausbildung & Studium (Degree & German equivalency)",
                "Sprachkenntnisse (CEFR: English C1, German B1)",
                "Aufenthaltsstatus (EU Blue Card / Visa readiness)"
            ),
            complianceRules = listOf(
                "Include CEFR language proficiency levels (A1 to C2)",
                "Declare Anabin/ZAB recognized university degree status",
                "Reverse chronological date format (MM/YYYY - MM/YYYY)",
                "Clean tabular format with clear separation of duties"
            ),
            rightToWorkDeclaration = "Aufenthalts- & Arbeitsstatus: Eligible for EU Blue Card (§ 18b Abs. 2 AufenthG) with recognized STEM degree and qualifying salary offer.",
            sampleConvertedText = """
                # TABELLARISCHER LEBENSLAUF (GERMANY / EUROPASS STANDARD)
                
                ## PERSÖNLICHE DATEN
                - Name: Vincent Mwangangi
                - Email: vincent.mwangangi@example.com | Tel: +49 152 555 0192
                - Wohnort: Berlin, Germany (or Relocating Candidate)
                - Staatsangehörigkeit: Kenya | Aufenthaltsstatus: Eligible for EU Blue Card (§ 18b AufenthG)
                
                ## BERUFLICHE QUALIFIKATION & ERFAHRUNG
                
                03/2022 – Heute | Senior Android & Cloud Infrastructure Engineer
                TechSolutions Global Ltd, Nairobi & Remote
                - Architecture & implementation of high-throughput mobile applications using Kotlin, Jetpack Compose, and Coroutines.
                - Reduced backend API latency by 34% through optimized caching and asynchronous coroutines dispatching.
                - Mentored 4 junior engineers on clean MVVM architecture and automated unit testing suites.
                
                08/2019 – 02/2022 | Software Engineer (Mobile & Backend Systems)
                DataSphere Systems Inc.
                - Developed RESTful microservice clients and SQLite/Room local persistence layers with 99.8% crash-free sessions.
                - Collaborated in cross-functional Agile sprints with international product managers.
                
                ## AUSBILDUNG & AKADEMISCHER WERDEGANG
                
                09/2015 – 11/2019 | Bachelor of Science in Computer Science
                University of Nairobi (Anabin Status: H+ Recognized)
                - Focus: Distributed Systems, Mobile Computing, Algorithm Design
                
                ## SPRACHKENNTNISSE (CEFR STANDARD)
                - English: C2 (Proficient / Fluent business & technical)
                - German: B1 (Zertifikat Deutsch / Professional working)
                - Swahili: Native
                
                ## IT-KENNTNISSE & TECHNOLOGIEN
                - Languages: Kotlin, Java, Python, SQL
                - Frameworks: Jetpack Compose, Android SDK, Ktor, Coroutines, Room DB, Git, CI/CD
            """.trimIndent()
        ),
        CountryCvFormat(
            id = "canada",
            countryName = "Canada",
            flag = "🇨🇦",
            standardName = "Canadian Federal Resume (NOC/TEER Aligned)",
            pageRecommendation = "2 Pages with quantitative impact metrics",
            keySections = listOf(
                "Target NOC / TEER Classification Code",
                "Professional Executive Summary",
                "Core Technical & Leadership Competencies",
                "Work History with Quantified Achievements",
                "Education & WES Credential Assessment"
            ),
            complianceRules = listOf(
                "Map past job titles to Canadian NOC 2021 Codes (e.g. NOC 21222)",
                "No photo, age, marital status, or SIN number (Canadian Human Rights Act)",
                "Quantify achievements with percentages, revenue, or time saved",
                "Specify WES Educational Credential Assessment (ECA) completion"
            ),
            rightToWorkDeclaration = "Immigration & LMIA Status: Seeking Employer-Specific LMIA / Global Skills Strategy (2-week processing eligible) or Express Entry PNP nomination.",
            sampleConvertedText = """
                # CANADIAN STANDARD RESUME (NOC 21222 - SOFTWARE DEVELOPERS & PROGRAMMERS)
                
                VINCENT MWANGANGI
                Toronto, ON (Relocation Ready) | vincent.mwangangi@example.com | (647) 555-0149
                LinkedIn: linkedin.com/in/vincentmwangangi | Global Skills Strategy (GSS) Ready
                
                ## PROFESSIONAL SUMMARY
                Results-driven Senior Mobile & Distributed Software Engineer with 5+ years of production experience designing scalable mobile architectures with Kotlin, Jetpack Compose, and cloud integrations. Proven track record boosting application performance by 35% and supporting 500k+ active users. Verified WES ECA equivalent to Canadian 4-Year Bachelor Degree.
                
                ## CANADIAN CLASSIFICATION & WORK RIGHTS
                - Target National Occupational Classification: NOC 21222 (TEER 1 - Software Engineers)
                - Educational Credential Assessment: WES Canadian Equivalence Verified
                - Work Permit Eligibility: LMIA-supported Work Permit / Global Skills Strategy 14-day stream
                
                ## CORE COMPETENCIES
                - Native Android (Kotlin, Compose, Room, MVVM, Clean Architecture)
                - Cloud & Backend Integration (REST APIs, Coroutines, Flow, Firebase)
                - Quality & Performance (CI/CD Pipelines, Robolectric, Automated Testing)
                
                ## PROFESSIONAL EXPERIENCE
                
                SENIOR SOFTWARE ENGINEER | TechSolutions Global
                March 2022 – Present
                - Architected 6 enterprise Android modules utilizing Jetpack Compose and Kotlin Coroutines, decreasing cold startup time by 40%.
                - Spearheaded offline-first database synchronization engine using Room DB and Kotlin Flow, ensuring 100% data fidelity during network dropouts.
                - Supervised a team of 4 developers across sprint planning, code reviews, and production releases.
                
                SOFTWARE DEVELOPER | DataSphere Systems
                August 2019 – February 2022
                - Built core user interface workflows resulting in a 25% increase in user retention over 12 months.
                - Designed and documented robust REST API client endpoints adhering to international security standards.
                
                ## EDUCATION & CERTIFICATIONS
                - Bachelor of Science in Computer Science | University of Nairobi (WES Canadian Equivalency Verified)
                - Certified Associate Android Developer (Google)
            """.trimIndent()
        ),
        CountryCvFormat(
            id = "uk",
            countryName = "United Kingdom",
            flag = "🇬🇧",
            standardName = "UK Skilled Worker 2-Page Standard CV",
            pageRecommendation = "2 Pages maximum with explicit CoS statement",
            keySections = listOf(
                "Personal Information & Location",
                "Right to Work & Sponsorship Requirement Statement",
                "Professional Profile & Technical Overview",
                "Career History with Key Responsibilities & Highlights",
                "Education & UK ENIC Statement"
            ),
            complianceRules = listOf(
                "Clearly state Visa Sponsorship requirement (Skilled Worker Dependant / CoS)",
                "Use British English spelling (e.g., optimised, modelled, programme)",
                "Do not include date of birth, marital status, or photograph",
                "Group key accomplishments with bold action verbs"
            ),
            rightToWorkDeclaration = "Right to Work in the UK: Requires Skilled Worker Visa Sponsorship (Eligible under SOC Code 2136 / Shortage Occupation List).",
            sampleConvertedText = """
                # UNITED KINGDOM PROFESSIONAL CURRICULUM VITAE
                
                VINCENT MWANGANGI
                London, United Kingdom (Relocation Supported)
                Email: vincent.mwangangi@example.com | Mobile: +44 7700 900124
                
                ## IMMIGRATION & SPONSORSHIP STATUS
                - Visa Status: Seeking UK Skilled Worker Visa Sponsorship (SOC Code 2136: Programmers & Software Developers)
                - UK ENIC Status: Bachelor Degree verified equivalent to UK Bachelor (Honours) standard
                - English Language Requirement: CEFR Level C1 / UKVI IELTS Academic Passed (Score: 8.0)
                
                ## PROFESSIONAL PROFILE
                A seasoned Software Engineer with extensive experience in architecting modern Android applications and distributed cloud services. Proficient in Kotlin, Jetpack Compose, and asynchronous state management. Adept at transforming complex business requirements into high-performance, maintainable software systems within fast-paced Agile environments.
                
                ## KEY TECHNICAL SKILLS
                - Primary: Kotlin, Java, Jetpack Compose, Coroutines, Flow, Room Database
                - Architecture: MVVM, Clean Architecture, SOLID Principles, Modular Design
                - Tools & DevOps: Git, GitHub Actions, Docker, Gradle KTS, JIRA
                
                ## CAREER HISTORY
                
                Senior Android Developer | TechSolutions Global
                March 2022 – Present
                - Led the refactoring of legacy monolithic modules into lightweight Jetpack Compose feature libraries, reducing application build time by 28%.
                - Designed offline-first caching algorithms that improved data retrieval speeds by 45% for over 200,000 active subscribers.
                - Collaborated closely with UK and European stakeholders to define technical roadmaps and product compliance standards.
                
                Software Engineer | DataSphere Systems
                August 2019 – February 2022
                - Delivered 12 major feature updates to production with a 99.9% crash-free session rate.
                - Authored comprehensive unit and UI test suites using JUnit and Espresso.
                
                ## EDUCATION & QUALIFICATIONS
                - BSc (Hons) in Computer Science | University of Nairobi (UK ENIC Statement of Comparability Verified)
                - Professional Member, British Computer Society (BCS Candidate)
            """.trimIndent()
        ),
        CountryCvFormat(
            id = "australia",
            countryName = "Australia",
            flag = "🇦🇺",
            standardName = "Australian Standard CV (ANZSCO 261312)",
            pageRecommendation = "2 - 3 Pages detailing technical environment",
            keySections = listOf(
                "Contact Details & Location Readiness",
                "ANZSCO Classification & Skills Assessment",
                "Career Summary & Technical Skill Matrix",
                "Employment History with Detailed Technical Environment",
                "Education & ACS Recognition"
            ),
            complianceRules = listOf(
                "Align experience with Australian Computer Society (ACS) Core Body of Knowledge",
                "Include ANZSCO Code (e.g. 261312 Developer Programmer)",
                "Detail the exact technology stack used for each historical project",
                "Highlight Australian temporary/permanent migration readiness (TSS 482 / 189 / 190)"
            ),
            rightToWorkDeclaration = "Visa & Work Rights: Seeking Subclass 482 Temporary Skill Shortage (TSS) employer sponsorship or Subclass 186 Employer Nomination.",
            sampleConvertedText = """
                # AUSTRALIAN STANDARD CURRICULUM VITAE (ANZSCO 261312)
                
                VINCENT MWANGANGI
                Sydney, NSW (Relocating) | vincent.mwangangi@example.com | +61 400 555 182
                
                ## VISA & WORK ELIGIBILITY
                - ANZSCO Classification: 261312 (Developer Programmer) / 261313 (Software Engineer)
                - Skills Assessment: Australian Computer Society (ACS) Migration Skills Assessment Ready
                - Visa Objective: Subclass 482 TSS Visa / Subclass 186 PR Direct Entry Stream
                - PTE Academic Score: 86/90 (Superior English equivalent)
                
                ## EXECUTIVE SUMMARY
                Accomplished Developer Programmer with over 5 years of commercial experience in native mobile engineering and high-scale backend services. Highly adept in Kotlin, Jetpack Compose, SQLite, and cloud API integration. Demonstrated track record delivering robust software across fintech and logistics domains.
                
                ## TECHNICAL ENVIRONMENT & SKILLS MATRIX
                - Languages: Kotlin, Java, SQL, Bash
                - Frameworks: Jetpack Compose, Material 3, Android Architecture Components, Coroutines
                - Testing: JUnit, Robolectric, MockK
                - Cloud & Tools: Google Cloud Platform, GitHub Actions, Gradle KTS
                
                ## EMPLOYMENT HISTORY
                
                Senior Developer Programmer | TechSolutions Global
                March 2022 – Present
                - Environment: Kotlin, Jetpack Compose, Coroutines, Flow, Room DB, REST APIs, Git, Agile/Scrum
                - Architected high-volume client-server interfaces supporting 350k daily active transactions.
                - Reduced mobile battery consumption by 22% through intelligent background sync scheduling.
                - Mentored cross-functional engineering teams and conducted rigorous code reviews.
                
                Software Engineer | DataSphere Systems
                August 2019 – February 2022
                - Environment: Java, Android SDK, SQLite, Retrofit, Firebase
                - Developed core features for mobile point-of-sale systems utilized by over 5,000 retail merchants.
                
                ## EDUCATION & PROFESSIONAL ACCREDITATION
                - Bachelor of Science in Computer Science | University of Nairobi (ACS Section A Recognised)
            """.trimIndent()
        ),
        CountryCvFormat(
            id = "usa",
            countryName = "United States",
            flag = "🇺🇸",
            standardName = "US 1-Page High-Impact ATS Resume",
            pageRecommendation = "Strict 1 Page format (XYZ Formula)",
            keySections = listOf(
                "Header (Name, City/State, Email, GitHub/LinkedIn)",
                "Education & Honors",
                "Technical Skills (Languages, Frameworks, Tools)",
                "Work Experience (Google XYZ Formula: Accomplished [X] as measured by [Y] by doing [Z])",
                "Key Projects / Patents"
            ),
            complianceRules = listOf(
                "Strict 1-page length for candidates with under 8 years experience",
                "Use Google 'XYZ Formula': Accomplished X, measured by Y, by doing Z",
                "Zero photos, graphics, or tables to maximize ATS parser compatibility",
                "Highlight H-1B transfer, O-1 extraordinary ability, or L-1 eligibility"
            ),
            rightToWorkDeclaration = "Work Authorization: Eligible for H-1B Specialty Occupation sponsorship or O-1A Extraordinary Ability petition.",
            sampleConvertedText = """
                # VINCENT MWANGANGI
                New York, NY (Relocation Ready) | vincent.mwangangi@example.com | (212) 555-0198
                linkedin.com/in/vincentmwangangi | github.com/vincentmwangangi | H-1B / O-1 Eligible
                
                ## EDUCATION
                University of Nairobi — Bachelor of Science in Computer Science | GPA: 3.8/4.0
                Relevant Coursework: Distributed Systems, Mobile Computing, Algorithms & Data Structures
                
                ## TECHNICAL SKILLS
                - Languages: Kotlin, Java, Python, SQL, C++
                - Frameworks & Tools: Jetpack Compose, Android SDK, Coroutines, Room DB, Git, Docker, CI/CD, REST APIs
                
                ## PROFESSIONAL EXPERIENCE
                TechSolutions Global — Senior Software Engineer (Android & Cloud) | Mar 2022 – Present
                - Increased mobile app transaction throughput by 42% for 500K+ monthly active users by re-architecting client data caching with Kotlin Coroutines and Room DB.
                - Reduced crash rate from 1.4% to 0.08% across 12 releases by establishing automated CI/CD unit testing pipelines with 85% code coverage.
                - Spearheaded the complete migration of 4 core modules to Jetpack Compose, accelerating feature delivery velocity by 30%.
                
                DataSphere Systems — Software Engineer | Aug 2019 – Feb 2022
                - Built and deployed 8 production-grade REST microservice endpoints handling 20K requests/min with sub-50ms latency.
                - Optimized SQLite database indexes and query plans, cutting peak mobile memory footprint by 25%.
                
                ## PROJECTS & AWARDS
                - Open Source Mobile Engine: Created a reactive Android state machine library with 1,200+ GitHub stars.
                - Hackathon Champion (FinTech East Africa): Developed offline-first payment gateway with instant SMS reconciliation.
            """.trimIndent()
        )
    )
}

@Composable
fun CountrySpecificCvConverterCard(
    viewModel: JobViewModel,
    onOpenFullConverter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF6750A4).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .testTag("country_specific_cv_converter_card"),
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF6750A4).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFFD0BCFF), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Country CV Converter",
                        tint = Color(0xFFD0BCFF),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Country-Specific CV Formatter",
                        color = WhiteActive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "1-Click convert your resume to Europass (DE), Canada (LMIA), UK standard, Australia, or US 1-page",
                        color = SlateMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Country Pills Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(CountryCvFormatDirectory.formats) { format ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavyDark)
                            .border(1.dp, NavyLight, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = format.flag, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = format.countryName, color = WhiteActive, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onOpenFullConverter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("btn_open_cv_converter_dialog"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
            ) {
                Icon(Icons.Default.Transform, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Country CV Format Converter", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun CountrySpecificCvConverterDialog(
    viewModel: JobViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedCountryId by remember { mutableStateOf("germany") }
    val activeFormat = CountryCvFormatDirectory.formats.firstOrNull { it.id == selectedCountryId }
        ?: CountryCvFormatDirectory.formats.first()

    var activeCvText by remember(selectedCountryId) { mutableStateOf(activeFormat.sampleConvertedText) }
    var isConverting by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .border(1.dp, NavyLight, RoundedCornerShape(18.dp))
                .testTag("country_cv_converter_dialog"),
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Public, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Country CV Converter",
                                color = WhiteActive,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Compliant with national immigration and recruitment ATS standards",
                                color = SlateMuted,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SlateMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Country Switcher Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(CountryCvFormatDirectory.formats) { format ->
                        val isSelected = selectedCountryId == format.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) EmeraldGreen else NavyDark)
                                .border(1.dp, if (isSelected) EmeraldGreen else NavyLight, RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedCountryId = format.id
                                    activeCvText = format.sampleConvertedText
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("select_country_cv_${format.id}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(format.flag, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = format.countryName,
                                    color = if (isSelected) NavyDark else WhiteActive,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Format Details & Rules Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavyDark),
                    border = BorderStroke(1.dp, NavyLight),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activeFormat.standardName,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TealCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = activeFormat.pageRecommendation, color = TealCyan, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Right-to-Work Clause Included: \"${activeFormat.rightToWorkDeclaration}\"",
                            color = AmberGold,
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Converted Text Area (Scrollable & Editable)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NavyDark)
                        .border(1.dp, NavyLight, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = activeCvText,
                            color = WhiteActive,
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("${activeFormat.countryName} CV", activeCvText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied ${activeFormat.countryName} format CV to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldGreen),
                        border = BorderStroke(1.dp, EmeraldGreen)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Text", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val templateToUse = when (activeFormat.id) {
                                "germany" -> "Europass / DIN"
                                "canada" -> "Modern Executive"
                                "uk" -> "ATS Standard"
                                "australia" -> "Creative Tech"
                                else -> "ATS Standard"
                            }
                            val res = com.example.util.AtsResumePdfGenerator.generateResumePdf(
                                context = context,
                                candidateName = "Candidate_${activeFormat.id.uppercase()}",
                                markdownContent = activeCvText,
                                templateName = templateToUse
                            )
                            if (res != null) {
                                Toast.makeText(context, "PDF Downloaded! Saved ${res.fileName} to Downloads (${res.pageCount} pages)", Toast.LENGTH_LONG).show()
                                val shareIntent = com.example.util.AtsResumePdfGenerator.createShareIntent(context, res.file, "Candidate")
                                context.startActivity(Intent.createChooser(shareIntent, "Save or Send ${activeFormat.countryName} PDF"))
                            } else {
                                Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("btn_download_country_pdf"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = NavyDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download PDF (.pdf)", color = NavyDark, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
