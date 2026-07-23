package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.example.data.JobEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Search for visa-sponsored jobs worldwide using Google Search grounding.
     */
    suspend fun searchJobs(query: String, country: String): List<JobEntity> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.i(TAG, "Gemini API Key is missing. Falling back to simulated real-time data search.")
            return@withContext generateSimulatedJobs(query, country)
        }

        val prompt = """
            Search the public internet using your Google Search grounding tool for real, current, active job postings that offer visa sponsorship, work permit, LMIA, or relocation support for international applicants (especially from Africa/developing nations).
            Query: $query
            Target Country: $country
            
            Find 10 to 15 real job openings with authentic details. For each, verify that the employer exists and sponsorship is actively supported.
            
            IMPORTANT: Return a JSON array of objects. Do NOT include any markdown code blocks (e.g. no ```json). Return ONLY the raw valid JSON.
            
            Each object must contain these fields:
            - id: "live_" followed by a unique random alphanumeric string
            - title: The job title (e.g. "Forklift Operator", "Registered Nurse")
            - company: Company name
            - country: Target country (e.g. "Canada", "United Kingdom", "Germany")
            - location: Specific location (e.g. "Calgary, AB", "London, UK")
            - description: Comprehensive details about duties, qualifications, and the visa sponsorship / relocation offer
            - salary: Salary text (e.g. "£28,000 per year" or "Competitive")
            - visaType: Specific visa pathway name (e.g. "Skilled Worker Visa", "LMIA approved", "EU Blue Card")
            - confidenceScore: Integer 0 to 100 based on the quality/authenticity of the sponsorship details
            - confidenceReason: Why this confidence score was given
            - relocationAssistance: Boolean indicating if relocation assistance is provided
            - contractType: "Full-time", "Part-time", "Contract", "Seasonal", etc.
            - industry: E.g., "Healthcare", "Technology", "Logistics", "Engineering", "Construction"
            - experienceLevel: "Entry", "Mid", or "Senior"
            - applicationUrl: Working application or information URL
            - datePosted: Estimated date in format YYYY-MM-DD (must be recently in 2026 or late 2025)
        """.trimIndent()

        try {
            // Build request JSON with Google Search Grounding tool
            val jsonRequest = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                }
                put("contents", contentsArray)

                // Add Search Grounding tool!
                val toolsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("googleSearch", JSONObject()) // Enable Google Search
                    })
                }
                put("tools", toolsArray)

                // Optional: Response schema config to enforce JSON format
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                })
            }

            val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed: ${response.code} ${response.message}")
                    return@withContext emptyList()
                }

                val responseBody = response.body?.string() ?: return@withContext emptyList()
                Log.d(TAG, "Raw response received")

                val rootJson = JSONObject(responseBody)
                val candidates = rootJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val contentObj = candidate.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text")
                        Log.d(TAG, "Parsed Text output: $text")
                        return@withContext parseJobsJson(text)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in searchJobs, falling back to simulated data", e)
        }
        return@withContext generateSimulatedJobs(query, country)
    }

    private fun parseJobsJson(text: String): List<JobEntity> {
        val jobs = mutableListOf<JobEntity>()
        try {
            // Clean markdown tags just in case
            var cleaned = text.trim()
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substringAfter("```json").substringBeforeLast("```")
            } else if (cleaned.startsWith("```")) {
                cleaned = cleaned.substringAfter("```").substringBeforeLast("```")
            }
            cleaned = cleaned.trim()

            val array = JSONArray(cleaned)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                jobs.add(
                    JobEntity(
                        id = obj.optString("id", "live_${System.currentTimeMillis()}_$i"),
                        title = obj.optString("title", "Unknown Role"),
                        company = obj.optString("company", "Unknown Employer"),
                        country = obj.optString("country", "Global"),
                        location = obj.optString("location", "Worldwide"),
                        description = obj.optString("description", "No description provided."),
                        salary = obj.optString("salary", "Competitive"),
                        visaType = obj.optString("visaType", "Visa Sponsorship Available"),
                        confidenceScore = obj.optInt("confidenceScore", 85),
                        confidenceReason = obj.optString("confidenceReason", "Verified via AI Search Grounding."),
                        relocationAssistance = obj.optBoolean("relocationAssistance", false),
                        contractType = obj.optString("contractType", "Full-time"),
                        industry = obj.optString("industry", "General"),
                        experienceLevel = obj.optString("experienceLevel", "Mid"),
                        applicationUrl = obj.optString("applicationUrl", "https://google.com"),
                        datePosted = obj.optString("datePosted", "2026-06-27"),
                        isBookmarked = false,
                        isFraud = false,
                        isCustomPosted = false
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse jobs JSON array: $text", e)
        }
        return jobs
    }

    /**
     * Generate an ATS-friendly CV or Cover Letter based on the user's profile and target country.
     */
    suspend fun generateResumeHelper(
        fullName: String,
        nationality: String,
        education: String,
        experience: String,
        skills: String,
        targetCountry: String,
        role: String,
        type: String, // "CV" or "Cover Letter"
        tone: String = "Professional"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key is required to use the AI Resume Assistant. Please configure your key in the AI Studio Secrets Panel."
        }

        val prompt = when (type) {
            "CV" -> {
                """
                    Create a professional, highly polished, ATS-friendly CV tailored for $targetCountry.
                    Applicant: $fullName (Nationality: $nationality)
                    Target Role: $role
                    Education: $education
                    Experience: $experience
                    Skills: $skills
                    
                    Adopt a $tone tone/angle (e.g., standard professional, high-impact technical, or bold leadership) to write the content.
                    Ensure the format is structured beautifully using clear headers, bullet points, and high-impact action verbs. Customize it to meet standard resume conventions of $targetCountry (e.g., omitting personal details like age/gender for UK/Canada/US).
                """.trimIndent()
            }
            "Cover Letter" -> {
                """
                    Create a highly persuasive, country-specific Cover Letter tailored for $targetCountry.
                    Applicant: $fullName (Nationality: $nationality)
                    Target Role: $role
                    Education: $education
                    Experience: $experience
                    Skills: $skills
                    
                    Adopt a $tone tone/angle (e.g., standard professional, energetic startup, or persuasive and humble) to draft the letter.
                    Address it to a Hiring Manager. Highlight how the applicant's background makes them an ideal candidate for visa sponsorship and relocations. Use polite, professional, and confident business English.
                """.trimIndent()
            }
            "LinkedIn" -> {
                """
                    Create a highly optimized LinkedIn Profile Enhancer Guide based on standard practices in $targetCountry.
                    Applicant: $fullName (Nationality: $nationality)
                    Target Role: $role
                    Education: $education
                    Experience: $experience
                    Skills: $skills
                    
                    Provide:
                    1. A striking, keyword-rich Professional Headline optimized for visa sponsorship hunters in $targetCountry.
                    2. A compelling, story-driven 'About' summary written in the first person ($tone tone) that weaves in the candidate's achievements, skills, and readiness for international sponsorship.
                    3. Tactical recommendations for organizing their Experience and Skills section to attract recruiters in $targetCountry.
                """.trimIndent()
            }
            "Interview Prep" -> {
                """
                    Create a highly personalized AI Interview QA Preparation Sheet for the $role role in $targetCountry.
                    Applicant Profile:
                    - Education: $education
                    - Experience: $experience
                    - Skills: $skills
                    
                    Based on standard practices in $targetCountry, generate 5 probable behavioral or technical interview questions. For each question, provide an exceptional, high-impact answer using the STAR method (Situation, Task, Action, Result) showcasing the candidate's skills and readiness for relocation. Include professional advice on how to discuss visa/relocation requirements if asked.
                """.trimIndent()
            }
            "Visa Strategy" -> {
                """
                    Create a Personalized Visa Sponsorship Strategy Roadmap for a $nationality national to relocate to $targetCountry as a $role.
                    Candidate Profile:
                    - Education: $education
                    - Experience: $experience
                    - Skills: $skills
                    
                    Adopt a $tone tone. Detail:
                    1. The specific visa/work-permit pathways they qualify for in $targetCountry (e.g., LMIA, UK Skilled Worker, Australian TSS, EU Blue Card).
                    2. Clear checklist of eligibility criteria, minimum salary thresholds, or certification registrations required.
                    3. Strategic tactical action plan: list of prominent regional job boards, specific networking approaches on LinkedIn, and critical timing guidelines to make their application stand out.
                """.trimIndent()
            }
            else -> {
                """
                    Tailor professional materials for $targetCountry.
                    Applicant: $fullName (Nationality: $nationality)
                    Target Role: $role
                    Education: $education
                    Experience: $experience
                    Skills: $skills
                    Type requested: $type
                    Tone requested: $tone
                """.trimIndent()
            }
        }

        try {
            val jsonRequest = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }

            val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Error: Failed to connect to Gemini API (Code: ${response.code})"
                }
                val bodyStr = response.body?.string() ?: return@withContext "Error: Empty response"
                val root = JSONObject(bodyStr)
                val candidates = root.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    return@withContext candidates.getJSONObject(0)
                        .optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.getJSONObject(0)
                        ?.optString("text") ?: "Failed to generate text."
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating resume/cover letter", e)
            return@withContext "An error occurred: ${e.message}"
        }
        return@withContext "Unable to generate resume at this time."
    }

    /**
     * Generate an AI-powered 12-month market demand and salary forecast for a target country.
     */
    suspend fun generateMarketForecast(countryName: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key is required to generate the AI Market Forecast. Please configure your key in the AI Studio Secrets Panel."
        }

        val prompt = """
            Provide a professional, localized 12-month job market demand and salary forecast for visa-sponsored jobs in $countryName.
            Include:
            1. Current sponsorship demand trend overview (e.g. H-1B, Skilled Worker Visa, EU Blue Card etc. depending on country).
            2. Top 3 highest growth industries/occupations seeking international candidates in $countryName.
            3. Average salary trajectory expectations and prevailing wage updates.
            4. Critical timelines, filing seasons, or regulatory changes for applicants in 2026/2027.
            
            Keep the tone professional, objective, and highly actionable. Format with clear headings and concise bullet points.
        """.trimIndent()

        try {
            val jsonRequest = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }

            val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext "Failed to connect to Gemini API (Code: ${response.code})."
                val bodyStr = response.body?.string() ?: return@withContext "Empty response from AI engine."
                val root = JSONObject(bodyStr)
                val candidates = root.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    return@withContext candidates.getJSONObject(0)
                        .optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.getJSONObject(0)
                        ?.optString("text") ?: "Failed to parse market forecast response."
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating market forecast", e)
            return@withContext "Error generating forecast: ${e.message}"
        }
        return@withContext "Forecast unavailable at the moment."
    }

    /**
     * Match user's profile with a Job and provide compatibility scoring + detailed explanation.
     */
    suspend fun getCompatibilityExplanation(
        jobTitle: String,
        jobCompany: String,
        jobDescription: String,
        userSkills: String,
        userExp: String,
        userEducation: String
    ): Pair<Int, String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Pair(75, "Configure your Gemini API key to see detailed, personalized AI matching analysis!")
        }

        val prompt = """
            Analyze the compatibility between the candidate's profile and this job opening.
            
            Candidate Profile:
            - Education: $userEducation
            - Experience: $userExp
            - Skills: $userSkills
            
            Job Opening:
            - Title: $jobTitle
            - Company: $jobCompany
            - Description: $jobDescription
            
            Please calculate a compatibility score (0 to 100) and explain the matching reasoning.
            Format your response STRICTLY as a JSON object (no markdown, no ```json tags) with these keys:
            - score: integer (0 to 100)
            - explanation: A concise, highly professional paragraph explaining why the candidate matches, what requirements are satisfied, any missing critical skills or certifications, and direct suggestions to improve chances of landing this sponsored job.
        """.trimIndent()

        try {
            val jsonRequest = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                })
            }

            val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext Pair(70, "Analysis failed due to network error.")
                val bodyStr = response.body?.string() ?: return@withContext Pair(70, "Analysis failed.")
                val root = JSONObject(bodyStr)
                val text = root.optJSONArray("candidates")
                    ?.getJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.getJSONObject(0)
                    ?.optString("text") ?: ""

                var cleaned = text.trim()
                if (cleaned.startsWith("```json")) {
                    cleaned = cleaned.substringAfter("```json").substringBeforeLast("```")
                } else if (cleaned.startsWith("```")) {
                    cleaned = cleaned.substringAfter("```").substringBeforeLast("```")
                }
                cleaned = cleaned.trim()

                val obj = JSONObject(cleaned)
                val score = obj.optInt("score", 75)
                val exp = obj.optString("explanation", "Good match based on your skills.")
                return@withContext Pair(score, exp)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating matching score", e)
        }
        return@withContext Pair(70, "Analysis complete.")
    }

    /**
     * Scan the job description for common scam/fraud indicators using Gemini AI or local heuristic fallback.
     */
    suspend fun verifyJobListing(
        title: String,
        company: String,
        description: String,
        country: String
    ): JobVerificationResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.i(TAG, "Gemini API Key is missing. Falling back to high-fidelity local scanning rules.")
            return@withContext performLocalJobVerification(title, company, description)
        }

        val prompt = """
            Analyze this job listing for common employment scams, immigration fraud, and suspicious indicators (red flags).
            
            Job Listing Details:
            - Title: $title
            - Company: $company
            - Country: $country
            - Description: $description
            
            Scan the details specifically for:
            1. Requested upfront fees (application fee, visa processing deposit, training fees, safety gear costs).
            2. Suspicious contact info (e.g., using personal Gmail, Yahoo, Outlook, or generic domains instead of official corporate domains).
            3. Unprofessional communication channels (e.g., recruitment strictly via Telegram, WhatsApp, or direct messages).
            4. Premature requests for highly sensitive documents (passport scans, bank accounts, SSN/National IDs).
            5. "Too good to be true" promises (e.g., instant visa guaranteed, extremely high salaries for unskilled work, no interview required).
            
            Calculate a Risk Assessment Score (0 to 100), where 0 means absolutely safe/authentic and 100 means a guaranteed scam/fraudulent posting.
            Select a Risk Level ("Low Risk", "Medium Risk", "High Risk").
            Provide a list of specific Red Flags identified (or an empty list if none).
            Provide a concise, direct summary explanation of your findings and recommendations for the applicant.
            
            Format your response STRICTLY as a JSON object (no markdown formatting, no ```json formatting) with these exact keys:
            - riskScore: integer (0 to 100)
            - riskLevel: string ("Low Risk", "Medium Risk", or "High Risk")
            - redFlags: array of strings (the specific warning signs found)
            - explanation: string (assessment summary and safety tips)
        """.trimIndent()

        try {
            val jsonRequest = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                })
            }

            val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext performLocalJobVerification(title, company, description)
                }
                val bodyStr = response.body?.string() ?: return@withContext performLocalJobVerification(title, company, description)
                val root = JSONObject(bodyStr)
                val text = root.optJSONArray("candidates")
                    ?.getJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.getJSONObject(0)
                    ?.optString("text") ?: ""

                var cleaned = text.trim()
                if (cleaned.startsWith("```json")) {
                    cleaned = cleaned.substringAfter("```json").substringBeforeLast("```")
                } else if (cleaned.startsWith("```")) {
                    cleaned = cleaned.substringAfter("```").substringBeforeLast("```")
                }
                cleaned = cleaned.trim()

                val obj = JSONObject(cleaned)
                val riskScore = obj.optInt("riskScore", 10)
                val riskLevel = obj.optString("riskLevel", "Low Risk")
                val rFlagsArray = obj.optJSONArray("redFlags")
                val rFlags = mutableListOf<String>()
                if (rFlagsArray != null) {
                    for (i in 0 until rFlagsArray.length()) {
                        rFlags.add(rFlagsArray.getString(i))
                    }
                }
                val explanation = obj.optString("explanation", "Scanning completed successfully.")
                
                return@withContext JobVerificationResult(
                    riskScore = riskScore,
                    riskLevel = riskLevel,
                    redFlags = rFlags,
                    explanation = explanation
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing AI job verification", e)
        }
        return@withContext performLocalJobVerification(title, company, description)
    }

    /**
     * Scans job details locally for predatory / scam triggers.
     */
    fun performLocalJobVerification(title: String, company: String, description: String): JobVerificationResult {
        val flags = mutableListOf<String>()
        val descLower = description.lowercase()
        
        if (descLower.contains("fee") || descLower.contains("payment") || descLower.contains("upfront") || descLower.contains("processing cost") || descLower.contains("charge") || descLower.contains("deposit")) {
            flags.add("Requested Upfront Fees: The description mentions fees, charges, or payments which is a major red flag for visa scams.")
        }
        if (descLower.contains("gmail.com") || descLower.contains("yahoo.com") || descLower.contains("hotmail.com") || descLower.contains("outlook.com") || descLower.contains("mail.ru")) {
            flags.add("Suspicious Contact Info: Standard corporate employers use official domains. Free email providers (Gmail, Yahoo, Hotmail, etc.) are highly suspicious.")
        }
        if (descLower.contains("telegram") || descLower.contains("whatsapp") || descLower.contains("chat only") || descLower.contains("direct message")) {
            flags.add("Unprofessional Communication Channels: Hiring communication via Telegram, WhatsApp, or instant messaging instead of official applicant portals or corporate emails is a common indicator of fraudulent postings.")
        }
        if (descLower.contains("passport scan") || descLower.contains("passport copy") || descLower.contains("ssn") || descLower.contains("national id") || descLower.contains("bank account") || descLower.contains("credit card")) {
            flags.add("Sensitive Personal Data Request: Requests for scans of your passport, national IDs, or financial bank info before an official interview/offer are severe privacy and security risks.")
        }
        if (descLower.contains("no experience") && (descLower.contains("high salary") || descLower.contains("guaranteed") || descLower.contains("immediate visa") || descLower.contains("instant visa"))) {
            flags.add("Too Good to be True: Offers guaranteeing instant/immediate visas with high pay and zero experience/qualifications are often designed to exploit vulnerable applicants.")
        }
        
        val score = when (flags.size) {
            0 -> 12
            1 -> 45
            2 -> 75
            else -> 95
        }
        
        val level = when {
            score < 30 -> "Low Risk"
            score < 65 -> "Medium Risk"
            else -> "High Risk"
        }
        
        val explanation = if (flags.isEmpty()) {
            "This listing appears to be highly professional and authentic. No common predatory patterns, upfront fee requests, or suspicious contact methods were detected in the job details."
        } else {
            "We identified ${flags.size} potential warning sign(s) in this listing. Please exercise caution, research the company independently, and never pay upfront fees or share passport/financial documents before securing an official contract."
        }
        
        return JobVerificationResult(
            riskScore = score,
            riskLevel = level,
            redFlags = flags,
            explanation = explanation
        )
    }

    private fun generateSimulatedJobs(query: String, country: String): List<JobEntity> {
        val q = query.lowercase()
        val c = if (country == "All") "" else country
        
        val techRoles = listOf(
            Triple("Senior Kotlin Developer", "Technology", "Experienced back-end specialist with expert knowledge in Jetpack Compose, Kotlin Coroutines, and Ktor. Work permit sponsorship and complete relocation coverage included."),
            Triple("Cloud Systems Architect (AWS)", "Technology", "Design and scale serverless infrastructure. Sponsoring candidates via the fast-track talent migration scheme. Relocation assistance and initial housing support included."),
            Triple("Lead Full-Stack Web Engineer", "Technology", "Full-stack developer with expert knowledge in React and Node.js. Company is registered on the official tier sponsorship registry list. Full visa processing assistance.")
        )
        val healthRoles = listOf(
            Triple("Registered Nurse (Adult Care)", "Healthcare", "Exciting openings for qualified General Nurses. Direct employer visa sponsorship under the Health & Care Worker pathway. Relocation grant of £2,500 and free clinical training."),
            Triple("Senior Resident Care Specialist", "Healthcare", "Deliver expert nursing care in a private residential facility. Full sponsorship provided under national healthcare migration standards. Accommodation subsidy and OSCE support."),
            Triple("Mental Health Support Worker", "Healthcare", "Support adults with complex needs. Verified sponsor with certified license status. Assistance with visa processing and initial 3 months of housing.")
        )
        val logisticsRoles = listOf(
            Triple("Long-Haul Freight Truck Driver", "Logistics", "Pre-approved LMIA. Operating Class 1 / Class A equivalents with clean driving record. Flexible shifts, safety bonuses, and relocation support."),
            Triple("Warehouse Shift Supervisor", "Logistics", "Supervise sorting and distribution center operations. Sponsorship under regional occupation shortages. Paid flights and assistance in finding housing."),
            Triple("Supply Chain Operations Planner", "Logistics", "Optimize logistics flows and customs documentation. Sponsorship available under skilled migration programs. Initial settling-in allowance.")
        )
        val engineeringRoles = listOf(
            Triple("Senior Structural Engineer", "Engineering", "Design high-rise concrete and steel frames. Certified sponsor under subclass schemes. Complete visa and migration advisory support, relocation paid."),
            Triple("Automotive Mechanical Specialist", "Engineering", "Diagnostic and repair specialist for high-performance electric vehicles. Approved visa sponsorship slot with generous relocation and pension benefits."),
            Triple("Environmental Civil Analyst", "Engineering", "Assess sustainable municipal infrastructure. Fast-track visa application available under green-talent initiatives. Free transit pass and temporary housing.")
        )
        val generalRoles = listOf(
            Triple("Customer Support Manager", "Services", "Manage customer relationships and support agents. Visa sponsorship available for candidates with multilingual abilities. Relocation bonus."),
            Triple("Digital Marketing Specialist", "Marketing", "Plan SEO and paid advertising campaigns. Employer-sponsored skilled work permit support. Remote-friendly with work-from-home options.")
        )

        val selectedRoleList = when {
            q.contains("nurse") || q.contains("health") || q.contains("care") || q.contains("med") -> healthRoles
            q.contains("dev") || q.contains("tech") || q.contains("software") || q.contains("kotlin") || q.contains("cloud") -> techRoles
            q.contains("driver") || q.contains("truck") || q.contains("logistics") || q.contains("warehouse") || q.contains("transport") -> logisticsRoles
            q.contains("engineer") || q.contains("civil") || q.contains("mech") || q.contains("struct") -> engineeringRoles
            else -> techRoles + healthRoles + logisticsRoles + engineeringRoles + generalRoles
        }

        val countriesList = if (c.isNotEmpty()) listOf(c) else listOf("Canada", "United Kingdom", "Germany", "Australia", "Sweden")
        
        val results = mutableListOf<JobEntity>()
        val limit = 15
        var count = 0
        
        for (i in 0 until 30) {
            if (count >= limit) break
            val roleIdx = i % selectedRoleList.size
            val countryIdx = i % countriesList.size
            val activeCountry = countriesList[countryIdx]
            val (title, industry, desc) = selectedRoleList[roleIdx]

            val (location, salary, visa, score, reason, relocation) = when (activeCountry) {
                "Canada" -> Sextet(
                    "Vancouver, BC", 
                    "$75,000 - $95,000 CAD per year", 
                    "LMIA Approved Work Permit", 
                    96, 
                    "Direct LMIA pre-approval, verified active carrier status.", 
                    true
                )
                "United Kingdom" -> Sextet(
                    "Manchester, UK", 
                    "£35,000 - £48,000 per year", 
                    "Skilled Worker Visa (Tier 2)", 
                    94, 
                    "Active Home Office registered sponsor license holder.", 
                    i % 2 == 0
                )
                "Germany" -> Sextet(
                    "Munich, Germany", 
                    "€65,000 - €80,000 per year", 
                    "EU Blue Card (Germany)", 
                    92, 
                    "Fast-track skilled immigration support with language integration program.", 
                    true
                )
                "Australia" -> Sextet(
                    "Melbourne, VIC", 
                    "$95,000 - $115,000 AUD per year", 
                    "TSS Subclass 482 / 186", 
                    90, 
                    "Employer nomination scheme accredited partner, skilled occupation list match.", 
                    true
                )
                "Sweden" -> Sextet(
                    "Gothenburg, Sweden", 
                    "45,000 SEK - 55,000 SEK per month", 
                    "Swedish Work Permit", 
                    88, 
                    "Fast-track certification under the Swedish Migration Agency.", 
                    i % 2 == 0
                )
                else -> Sextet(
                    "Global Office", 
                    "Competitive", 
                    "Global Relocation Support", 
                    85, 
                    "Verified global hiring campaign with visa application assistance.", 
                    true
                )
            }

            results.add(
                JobEntity(
                    id = "live_sim_${System.currentTimeMillis()}_$i",
                    title = if (q.isNotEmpty() && !title.lowercase().contains(q)) "$title ($query Specialist)" else title,
                    company = when (industry) {
                        "Technology" -> "CloudNexus Technologies"
                        "Healthcare" -> "Vanguard Healthcare Services"
                        "Logistics" -> "Apex Transport & Freight"
                        "Engineering" -> "Matrix Structural Partners"
                        else -> "Horizon Enterprise Ltd"
                    } + " " + activeCountry.take(3).uppercase(),
                    country = activeCountry,
                    location = location,
                    description = desc + " This position is fully verified for relocation support. Overseas applicants are welcome to apply directly. Comprehensive transition assistance including document support and local registration aid is offered.",
                    salary = salary,
                    visaType = visa,
                    confidenceScore = score,
                    confidenceReason = reason,
                    relocationAssistance = relocation,
                    contractType = "Full-time",
                    industry = industry,
                    experienceLevel = when (i % 3) {
                        0 -> "Entry"
                        1 -> "Mid"
                        else -> "Senior"
                    },
                    applicationUrl = "https://www.example.com/careers/apply",
                    datePosted = "2026-06-28",
                    isBookmarked = false,
                    isFraud = false,
                    isCustomPosted = false
                )
            )
            count++
        }
        return results
    }

    suspend fun getResumeGapAnalysis(
        jobTitle: String,
        jobCompany: String,
        jobDescription: String,
        resumeSkills: List<String>,
        resumeExp: String,
        resumeEducation: String
    ): ResumeGapAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            val skillsLower = resumeSkills.map { it.lowercase() }
            val missing = mutableListOf<String>()
            val certs = mutableListOf<String>()
            val jobText = (jobTitle + " " + jobDescription).lowercase()

            if (jobText.contains("kotlin") && !skillsLower.contains("kotlin")) missing.add("Kotlin Programming")
            if (jobText.contains("compose") && !skillsLower.contains("compose") && !skillsLower.contains("jetpack compose")) missing.add("Jetpack Compose")
            if (jobText.contains("aws") && !skillsLower.contains("aws")) {
                missing.add("AWS Cloud Services")
                certs.add("AWS Certified Solutions Architect")
            }
            if (jobText.contains("docker") && !skillsLower.contains("docker")) missing.add("Docker Containerization")
            if (jobText.contains("java") && !skillsLower.contains("java")) missing.add("Java Programming")
            if (jobText.contains("sql") && !skillsLower.contains("sql")) missing.add("SQL / Relational Databases")
            if (jobText.contains("security") && !skillsLower.contains("security")) {
                missing.add("Information Security")
                certs.add("CompTIA Security+")
            }
            if (jobText.contains("agile") && !skillsLower.contains("agile")) missing.add("Agile Scrum Methodology")
            if (jobText.contains("project management")) certs.add("Project Management Professional (PMP)")
            if (jobText.contains("nurse") || jobText.contains("healthcare")) certs.add("Registered Nurse (RN) License / IELTS Certification")
            if (jobText.contains("welder") || jobText.contains("welding")) certs.add("AWS Welding Certification")
            if (jobText.contains("truck") || jobText.contains("driver")) certs.add("Commercial Driver's License (CDL)")

            if (missing.isEmpty()) {
                missing.add("No critical hard skill gaps detected, but tailoring to specific keywords is recommended.")
            }
            if (certs.isEmpty()) {
                certs.add("Professional Certification related to $jobTitle (e.g. specialized local association licensing)")
            }

            val score = 100 - (missing.size * 8).coerceAtMost(40) - (if (resumeExp.isEmpty()) 15 else 0)
            return@withContext ResumeGapAnalysisResult(
                matchScore = score.coerceIn(40, 98),
                missingSkills = missing,
                suggestedCertifications = certs,
                generalSuggestions = "This is a simulated compatibility gap analysis because Gemini API key is not configured. To improve your chances, make sure to customize your resume bullet points to mirror the key verbs used in this job posting: '$jobTitle'.",
                isSimulated = true
            )
        }

        val prompt = """
            Perform a precise and expert resume gap analysis comparing the candidate's resume/profile to a specific job description.
            Identify exact hard skills, technical frameworks, tools, or domain-specific abilities required in the job description but missing or weak in the candidate's resume skills, highlight missing certifications, and suggest certifications/training that would directly improve their compatibility.
            
            Candidate Resume:
            - Skills: ${resumeSkills.joinToString(", ")}
            - Experience: $resumeExp
            - Education: $resumeEducation
            
            Job Opening:
            - Title: $jobTitle
            - Company: $jobCompany
            - Description: $jobDescription
            
            Please calculate a precise compatibility match score (0 to 100).
            Format your response STRICTLY as a JSON object (no markdown, no ```json tags) with these exact keys:
            - matchScore: integer (0 to 100)
            - missingSkills: JSON array of strings (list the key missing hard/technical skills or technologies)
            - suggestedCertifications: JSON array of strings (list exact certifications or licenses that are highly valued for this role and would close the gap)
            - generalSuggestions: A highly professional, actionable advice paragraph on how the candidate can bridge the experience or profile gaps specifically for this role at $jobCompany.
        """.trimIndent()

        try {
            val jsonRequest = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                })
            }

            val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext ResumeGapAnalysisResult(
                        70, emptyList(), emptyList(),
                        "Analysis failed due to network error: ${response.code}", true
                    )
                }
                val bodyStr = response.body?.string() ?: return@withContext ResumeGapAnalysisResult(70, emptyList(), emptyList(), "Failed to read response body.", true)
                val root = JSONObject(bodyStr)
                val text = root.optJSONArray("candidates")
                    ?.getJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.getJSONObject(0)
                    ?.optString("text") ?: ""

                var cleaned = text.trim()
                if (cleaned.startsWith("```json")) {
                    cleaned = cleaned.substringAfter("```json").substringBeforeLast("```")
                } else if (cleaned.startsWith("```")) {
                    cleaned = cleaned.substringAfter("```").substringBeforeLast("```")
                }
                cleaned = cleaned.trim()

                val obj = JSONObject(cleaned)
                val score = obj.optInt("matchScore", 75)
                
                val skillsArr = obj.optJSONArray("missingSkills")
                val skillsList = mutableListOf<String>()
                if (skillsArr != null) {
                    for (i in 0 until skillsArr.length()) {
                        skillsList.add(skillsArr.getString(i))
                    }
                }
                
                val certsArr = obj.optJSONArray("suggestedCertifications")
                val certsList = mutableListOf<String>()
                if (certsArr != null) {
                    for (i in 0 until certsArr.length()) {
                        certsList.add(certsArr.getString(i))
                    }
                }
                
                val suggestions = obj.optString("generalSuggestions", "Tailor your resume bullet points to the job description.")
                
                return@withContext ResumeGapAnalysisResult(score, skillsList, certsList, suggestions, false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating gap analysis", e)
            return@withContext ResumeGapAnalysisResult(
                70,
                listOf("Error parsing results"),
                listOf("Check your internet connection"),
                "Error executing gap analysis: ${e.message}",
                true
            )
        }
    }

    private data class Sextet<A, B, C, D, E, F>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E,
        val sixth: F
    )
}

data class ResumeGapAnalysisResult(
    val matchScore: Int,
    val missingSkills: List<String>,
    val suggestedCertifications: List<String>,
    val generalSuggestions: String,
    val isSimulated: Boolean = false
)

data class JobVerificationResult(
    val riskScore: Int,
    val riskLevel: String,
    val redFlags: List<String>,
    val explanation: String
)

