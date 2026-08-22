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
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.GeminiApiClient
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class ConsularQuestion(
    val id: String,
    val category: String, // "Genuine Vacancy", "Ties to Home Country", "Financial & Salary", "Technical & Duties", "Immigration History"
    val visaPathway: String,
    val questionText: String,
    val consularOfficerTone: String, // "Strict / Skeptical", "Neutral Investigative", "Fact Verification"
    val commonPitfalls: String,
    val modelAnswer: String
)

data class InterviewEvaluationResult(
    val scoreOutOf100: Int,
    val verdict: String, // "Visa Approval Likely", "221(g) Administrative Review Risk", "High Refusal Risk"
    val verdictColor: Color,
    val officerPerspective: String,
    val strengths: List<String>,
    val improvementTips: List<String>,
    val suggestedBetterResponse: String
)

object ConsularQuestionBank {
    val sampleQuestions = listOf(
        ConsularQuestion(
            id = "cq_01",
            category = "Genuine Vacancy",
            visaPathway = "UK Skilled Worker Visa",
            questionText = "Why was it necessary for your UK employer to hire you from overseas instead of hiring someone already located in London?",
            consularOfficerTone = "Strict / Skeptical",
            commonPitfalls = "Saying 'I don't know' or sounding like you are taking a job from a local. Never imply you were hired just because you'd accept lower pay.",
            modelAnswer = "My employer conducted a specialized recruitment campaign for a Senior Distributed Systems Engineer. My background in building high-throughput microservices handling 50k QPS in fintech directly matches their proprietary legacy-to-cloud migration architecture, where there is an acute skills shortage in the UK market."
        ),
        ConsularQuestion(
            id = "cq_02",
            category = "Ties to Home Country",
            visaPathway = "US H-1B / L-1 Visa Stamping",
            questionText = "What are your immediate and long-term plans when your current authorized visa period expires?",
            consularOfficerTone = "Neutral Investigative",
            commonPitfalls = "Saying 'I want to live in America forever' if applying for non-dual-intent categories, or failing to acknowledge the lawful limits of the visa validity.",
            modelAnswer = "I plan to successfully execute my employer's 3-year cloud infrastructure modernization roadmap. Once my authorized petition period concludes, I will abide by all US immigration regulations, either returning home to leverage this global experience or renewing through lawful petition extensions approved by USCIS."
        ),
        ConsularQuestion(
            id = "cq_03",
            category = "Financial & Salary",
            visaPathway = "Canada LMIA Work Permit",
            questionText = "What is your exact agreed base salary, and who paid for your LMIA processing fees, flights, and settlement expenses?",
            consularOfficerTone = "Fact Verification",
            commonPitfalls = "Admitting you paid recruitment fees or LMIA fees (which is illegal under Canadian federal law), or not knowing your exact gross wage.",
            modelAnswer = "My contracted gross base salary is CAD $125,000 per year plus full extended health benefits. Under Canadian labor laws and our agreement, all LMIA application fees, legal representation costs, and initial flight relocation expenses were 100% paid by my employer, with zero recruitment deductions from my pay."
        ),
        ConsularQuestion(
            id = "cq_04",
            category = "Technical & Duties",
            visaPathway = "Germany EU Blue Card",
            questionText = "Explain your day-to-day software architectural responsibilities at SAP/Berlin without using excessive buzzwords.",
            consularOfficerTone = "Technical Verification",
            commonPitfalls = "Giving generic statements like 'I code software' or reading a memorized generic job description.",
            modelAnswer = "I am responsible for designing Kotlin-based RESTful backend microservices for the global billing platform. I write automated test suites, optimize PostgreSQL database query indexing, and participate in peer code reviews and sprint planning to ensure 99.99% uptime."
        ),
        ConsularQuestion(
            id = "cq_05",
            category = "Immigration History",
            visaPathway = "Australia TSS 482",
            questionText = "Have you ever had a visa refusal, overstay, or administrative cancellation in any country worldwide?",
            consularOfficerTone = "Strict / Skeptical",
            commonPitfalls = "Attempting to hide a previous tourist or student visa rejection. Consular systems share biometric databases across Five Eyes (US, UK, CA, AU, NZ).",
            modelAnswer = "I disclosed all details fully on my application form: in 2021 I received a standard 214(b) visitor visa refusal for insufficient travel ties to the US. Since then, I have maintained a flawless international travel record, advanced my career, and complied with all immigration protocols."
        )
    )
}

@Composable
fun ConsularInterviewSimulatorHub(
    viewModel: JobViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedPathway by remember { mutableStateOf("UK Skilled Worker Visa") }
    var selectedQuestionIndex by remember { mutableIntStateOf(0) }
    var candidateAnswerText by remember { mutableStateOf("") }
    var isEvaluating by remember { mutableStateOf(false) }
    var evaluationResult by remember { mutableStateOf<InterviewEvaluationResult?>(null) }
    var showQuestionBankModal by remember { mutableStateOf(false) }

    val pathways = listOf("UK Skilled Worker Visa", "US H-1B / L-1 Visa Stamping", "Canada LMIA Work Permit", "Germany EU Blue Card", "Australia TSS 482")

    val currentQuestions = remember(selectedPathway) {
        ConsularQuestionBank.sampleQuestions.filter { it.visaPathway == selectedPathway }
            .ifEmpty { ConsularQuestionBank.sampleQuestions }
    }

    val activeQuestion = currentQuestions.getOrNull(selectedQuestionIndex.coerceIn(0, currentQuestions.size - 1))
        ?: ConsularQuestionBank.sampleQuestions.first()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("consular_interview_simulator_screen")
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
                    .background(Color(0xFFE91E63).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFFF48FB1), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = Color(0xFFF48FB1),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Consular Interview Simulator",
                    color = WhiteActive,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "AI Mock Embassy Interview & Genuine Vacancy Test",
                    color = SlateMuted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Visa Pathway Selection
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(pathways) { pathway ->
                FilterChip(
                    selected = selectedPathway == pathway,
                    onClick = {
                        selectedPathway = pathway
                        selectedQuestionIndex = 0
                        evaluationResult = null
                        candidateAnswerText = ""
                    },
                    label = { Text(pathway, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldGreen,
                        selectedLabelColor = NavyDark,
                        containerColor = NavyMedium,
                        labelColor = SlateMuted
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (selectedPathway == pathway) EmeraldGreen else NavyLight,
                        enabled = true,
                        selected = selectedPathway == pathway
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                // Consular Officer Simulation Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyMedium),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF283593)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = WhiteActive, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Consular Adjudication Officer", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Embassy Visa Window #4", color = SlateMuted, fontSize = 10.sp)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NavyDark
                            ) {
                                Text(
                                    text = activeQuestion.consularOfficerTone,
                                    color = Color(0xFFFFB74D),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Speech Bubble
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp),
                            color = NavyDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, NavyLight)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "\"${activeQuestion.questionText}\"",
                                    color = WhiteActive,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 22.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Category: ${activeQuestion.category}",
                                        color = EmeraldGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Question ${selectedQuestionIndex + 1} of ${currentQuestions.size}",
                                        color = SlateMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Danger Pitfall Warning
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFD32F2F).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Common Visa Rejection Trap:", color = Color(0xFFEF5350), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text(activeQuestion.commonPitfalls, color = WhiteActive, fontSize = 11.sp, lineHeight = 15.sp)
                            }
                        }
                    }
                }
            }

            item {
                // Candidate Answer Input Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyMedium)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Your Verbal Response to the Officer", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = candidateAnswerText,
                            onValueChange = { candidateAnswerText = it },
                            placeholder = { Text("Speak or type how you would answer the consular officer concisely...", color = SlateMuted, fontSize = 13.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("candidate_answer_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = NavyDark,
                                unfocusedContainerColor = NavyDark,
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = NavyLight,
                                focusedTextColor = WhiteActive,
                                unfocusedTextColor = WhiteActive
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    candidateAnswerText = activeQuestion.modelAnswer
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(42.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TealCyan)
                            ) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Use Model Answer", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    if (candidateAnswerText.isNotBlank()) {
                                        isEvaluating = true
                                        coroutineScope.launch {
                                            evaluationResult = evaluateCandidateAnswer(
                                                question = activeQuestion,
                                                candidateAnswer = candidateAnswerText
                                            )
                                            isEvaluating = false
                                        }
                                    }
                                },
                                enabled = candidateAnswerText.isNotBlank() && !isEvaluating,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.2f).height(42.dp).testTag("evaluate_answer_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                            ) {
                                if (isEvaluating) {
                                    CircularProgressIndicator(color = NavyDark, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = NavyDark, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Evaluate Answer", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // AI Evaluation Results Card
            evaluationResult?.let { result ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyMedium),
                        border = androidx.compose.foundation.BorderStroke(1.dp, result.verdictColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Consular Officer Adjudication Score", color = SlateMuted, fontSize = 11.sp)
                                    Text(
                                        text = "${result.scoreOutOf100} / 100",
                                        color = result.verdictColor,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = result.verdictColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = result.verdict,
                                        color = result.verdictColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Officer Perspective Breakdown
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = NavyDark
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("How the Consular Officer Interpreted This:", color = TealCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(result.officerPerspective, color = WhiteActive, fontSize = 12.sp, lineHeight = 17.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Strengths & Improvement Tips
                            Text("Key Observations", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            result.strengths.forEach { s ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(s, color = WhiteActive, fontSize = 12.sp)
                                }
                            }

                            result.improvementTips.forEach { tip ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(tip, color = SlateMuted, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Recommended Polish
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = NavyDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Recommended High-Scoring Response:", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(result.suggestedBetterResponse, color = WhiteActive, fontSize = 12.sp, lineHeight = 17.sp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Next Question Button
                Button(
                    onClick = {
                        selectedQuestionIndex = (selectedQuestionIndex + 1) % currentQuestions.size
                        evaluationResult = null
                        candidateAnswerText = ""
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyMedium)
                ) {
                    Text("Next Consular Scenario (${selectedQuestionIndex + 1}/${currentQuestions.size})", color = WhiteActive, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = EmeraldGreen)
                }
            }
        }
    }
}

private fun evaluateCandidateAnswer(
    question: ConsularQuestion,
    candidateAnswer: String
): InterviewEvaluationResult {
    val answerLower = candidateAnswer.lowercase()
    val wordCount = candidateAnswer.split("\\s+".toRegex()).size

    var score = 70
    val strengths = mutableListOf<String>()
    val improvementTips = mutableListOf<String>()

    if (wordCount in 25..120) {
        score += 15
        strengths.add("Concise and direct length (avoided rambling or excessive hesitation)")
    } else if (wordCount < 15) {
        score -= 20
        improvementTips.add("Answer is overly terse; provide concrete context regarding your qualifications")
    }

    if (answerLower.contains("salary") || answerLower.contains("contract") || answerLower.contains("employer") || answerLower.contains("sponsor")) {
        score += 10
        strengths.add("Directly addresses the official legal and employment relationship")
    }

    if (answerLower.contains("forever") || answerLower.contains("immigrate permanently") && question.category == "Ties to Home Country") {
        score -= 25
        improvementTips.add("Avoid expressing intent to remain permanently without referencing lawful extension procedures")
    }

    val finalScore = score.coerceIn(35, 98)
    val (verdict, color) = when {
        finalScore >= 85 -> Pair("Visa Approval Highly Likely", EmeraldGreen)
        finalScore >= 65 -> Pair("Moderate Risk - Clear & Fact-Based", Color(0xFFFFB74D))
        else -> Pair("High Risk of Administrative Scrutiny", Color(0xFFEF5350))
    }

    return InterviewEvaluationResult(
        scoreOutOf100 = finalScore,
        verdict = verdict,
        verdictColor = color,
        officerPerspective = "The officer evaluates for consistency with your Certificate of Sponsorship/I-129 petition, wage compliance, and genuine role duties.",
        strengths = strengths.ifEmpty { listOf("Tone is polite and professional", "Clear terminology used") },
        improvementTips = improvementTips.ifEmpty { listOf("Mention your specific tech stack or project milestone dates", "Keep answers under 60 seconds") },
        suggestedBetterResponse = question.modelAnswer
    )
}
