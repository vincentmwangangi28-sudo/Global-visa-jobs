package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.network.GeminiApiClient
import com.example.network.RapidApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class JobRepository(private val db: AppDatabase, private val context: Context) {
    private val jobDao = db.jobDao()

    val allJobs: Flow<List<JobEntity>> = jobDao.getAllJobsFlow()
    val userProfile: Flow<UserProfileEntity?> = jobDao.getUserProfileFlow()
    val allAlerts: Flow<List<CustomAlertEntity>> = jobDao.getAllAlertsFlow()
    val allVisaApplications: Flow<List<VisaApplicationEntity>> = jobDao.getAllVisaApplicationsFlow()
    val allRelocationTasks: Flow<List<RelocationTaskEntity>> = jobDao.getAllRelocationTasksFlow()
    val allNotifications: Flow<List<JobNotificationEntity>> = jobDao.getAllNotificationsFlow()
    val allVisaDocuments: Flow<List<VisaDocumentEntity>> = jobDao.getAllVisaDocumentsFlow()

    suspend fun initializeWithDefaultJobs() {
        try {
            // Gracefully initialize Firebase Sync Framework
            FirebaseSyncManager.initialize(context)

            // Start listening to real-time incoming Firestore jobs feed
            FirebaseSyncManager.listenToLiveJobsFeed { liveJob ->
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        checkAndTriggerNotifications(listOf(liveJob))
                        jobDao.insertJob(liveJob)
                    } catch (e: Exception) {
                        Log.e("JobRepository", "Failed to process incoming live job from Firestore", e)
                    }
                }
            }

            val existing = jobDao.getAllJobsFlow().firstOrNull() ?: emptyList()
            if (existing.size < DefaultJobs.list.size) {
                Log.d("JobRepository", "Pre-populating database with default sponsor jobs.")
                jobDao.insertJobs(DefaultJobs.list)
            }
            // Ensure profile exists
            val profile = jobDao.getUserProfile()
            if (profile == null) {
                jobDao.insertProfile(UserProfileEntity())
            }
            
            // Populate default general relocation tasks if empty
            val existingTasks = jobDao.getAllRelocationTasksFlow().firstOrNull() ?: emptyList()
            if (existingTasks.isEmpty()) {
                prePopulateRelocationTasks("General")
            }
        } catch (e: Exception) {
            Log.e("JobRepository", "Initialization failed", e)
        }
    }

    suspend fun checkAndTriggerNotifications(incomingJobs: List<JobEntity>) {
        try {
            val newJobs = incomingJobs.filter { job ->
                jobDao.getJobById(job.id) == null
            }
            if (newJobs.isEmpty()) return

            val profile = jobDao.getUserProfile()
            val alerts = jobDao.getAllAlertsFlow().firstOrNull() ?: emptyList()
            val userEmail = "vincentmwangangi28@gmail.com"
            val userName = profile?.fullName ?: "Vincent"

            for (job in newJobs) {
                var notificationTriggered = false

                // 1. Match custom alerts first
                for (alert in alerts) {
                    val countryMatches = alert.country.equals("All", ignoreCase = true) || 
                                         alert.country.isBlank() || 
                                         job.country.equals(alert.country, ignoreCase = true)
                    
                    val queryMatches = job.title.contains(alert.queryText, ignoreCase = true) || 
                                       job.description.contains(alert.queryText, ignoreCase = true)

                    if (countryMatches && queryMatches) {
                        val title = "Alert Match: ${job.title}"
                        val message = "${job.company} posted a new job matching '${alert.queryText}' in ${job.country}!"
                        val emailContent = generateSimulatedEmailBody(job, userEmail, userName)

                        val notification = JobNotificationEntity(
                            title = title,
                            message = message,
                            jobId = job.id,
                            isEmail = alert.isEmailAlert,
                            emailContent = emailContent,
                            isPush = alert.isPushAlert
                        )
                        jobDao.insertNotification(notification)

                        if (alert.isPushAlert) {
                            showLocalNotification(title, message)
                        }
                        notificationTriggered = true
                    }
                }

                // 2. If no custom alerts matched, match user skill profile & visa preferences
                if (!notificationTriggered && profile != null && profile.skills.isNotEmpty()) {
                    val desiredCountries = profile.desiredCountries.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    
                    val countryMatches = desiredCountries.isEmpty() || desiredCountries.any {
                        it.equals(job.country, ignoreCase = true)
                    }

                    val skills = profile.skills.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    
                    val skillMatches = skills.isNotEmpty() && skills.any { skill ->
                        job.title.contains(skill, ignoreCase = true) || 
                        job.description.contains(skill, ignoreCase = true)
                    }

                    if (countryMatches && skillMatches) {
                        val title = "Profile Match: ${job.title}"
                        val message = "New opportunity at ${job.company} in ${job.country} matches your skill profile!"
                        val emailContent = generateSimulatedEmailBody(job, userEmail, userName)

                        val notification = JobNotificationEntity(
                            title = title,
                            message = message,
                            jobId = job.id,
                            isEmail = true,
                            emailContent = emailContent,
                            isPush = true
                        )
                        jobDao.insertNotification(notification)
                        showLocalNotification(title, message)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("JobRepository", "Error running matching notifications", e)
        }
    }

    private fun generateSimulatedEmailBody(job: JobEntity, userEmail: String, userName: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background-color: #0b1120; color: #f8fafc; padding: 20px; margin: 0; }
                    .card { background-color: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 24px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }
                    .header { font-size: 20px; font-weight: bold; color: #10b981; border-bottom: 2px solid #334155; padding-bottom: 12px; margin-bottom: 16px; }
                    .job-title { font-size: 18px; font-weight: bold; color: #ffffff; }
                    .company { font-size: 15px; color: #38bdf8; margin-bottom: 12px; }
                    .detail-row { margin: 8px 0; font-size: 14px; }
                    .label { font-weight: bold; color: #94a3b8; }
                    .badge { display: inline-block; background-color: #059669; color: #ffffff; padding: 4px 8px; border-radius: 6px; font-size: 12px; font-weight: bold; }
                    .footer { font-size: 12px; color: #64748b; margin-top: 24px; border-top: 1px solid #334155; padding-top: 12px; text-align: center; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="header">📧 New Matching Job Alert Dispatched!</div>
                    <p>Hello <strong>${userName.ifEmpty { "Applicant" }}</strong>,</p>
                    <p>A new job opening has been posted that matches your skills and visa requirements:</p>
                    
                    <div class="job-title">${job.title}</div>
                    <div class="company">${job.company}</div>
                    
                    <div class="detail-row"><span class="label">📍 Location:</span> ${job.location}, ${job.country}</div>
                    <div class="detail-row"><span class="label">🛂 Visa Sponsorship:</span> <span class="badge">${job.visaType}</span></div>
                    <div class="detail-row"><span class="label">💰 Estimated Salary:</span> ${job.salary}</div>
                    <div class="detail-row"><span class="label">📦 Relocation Assistance:</span> ${if (job.relocationAssistance) "Included" else "No"}</div>
                    
                    <p style="margin-top: 16px;">We recommend applying immediately to secure your visa sponsor path!</p>
                    
                    <div class="footer">
                        Sent to <strong>$userEmail</strong><br>
                        Powered by the VisaGo AI Real-Time Sponsor Alert System.
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun showLocalNotification(title: String, message: String) {
        try {
            val channelId = "sponsor_jobs_alerts"
            val channelName = "Sponsor Job Alerts"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId, 
                    channelName, 
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for new matching visa sponsor jobs"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = if (intent != null) {
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            } else {
                null
            }

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                
            if (pendingIntent != null) {
                builder.setContentIntent(pendingIntent)
            }

            // Safe permissions check
            if (Build.VERSION.SDK_INT < 33 || 
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
            } else {
                Log.w("JobRepository", "POST_NOTIFICATIONS permission not granted. Native push suppressed.")
            }
        } catch (e: Exception) {
            Log.e("JobRepository", "Failed to show local notification", e)
        }
    }

    suspend fun searchAndScrapeJobs(query: String, country: String): Boolean {
        try {
            // Call live search grounding API
            val liveJobs = GeminiApiClient.searchJobs(query, country)
            if (liveJobs.isNotEmpty()) {
                // Check notifications first, before database has them inserted
                checkAndTriggerNotifications(liveJobs)
                jobDao.insertJobs(liveJobs)
                return true
            }
        } catch (e: Exception) {
            Log.e("JobRepository", "Live search scrape failed", e)
        }
        return false
    }

    suspend fun fetchAndSaveIndeedJobs(companyName: String, locality: String, start: Int): List<JobEntity> {
        try {
            val jobs = RapidApiClient.getIndeedCompanyJobs(companyName, locality, start)
            if (jobs.isNotEmpty()) {
                checkAndTriggerNotifications(jobs)
                jobDao.insertJobs(jobs)
            }
            return jobs
        } catch (e: Exception) {
            Log.e("JobRepository", "Failed to fetch and save Indeed jobs", e)
        }
        return emptyList()
    }

    suspend fun fetchAndSaveGoogleJobs(
        query: String,
        location: String,
        country: String = "US",
        domain: String = "com",
        maxRows: Int = 20
    ): List<JobEntity> {
        try {
            val jobs = RapidApiClient.getGoogleJobsScraper(query, location, country, domain, maxRows)
            if (jobs.isNotEmpty()) {
                checkAndTriggerNotifications(jobs)
                jobDao.insertJobs(jobs)
            }
            return jobs
        } catch (e: Exception) {
            Log.e("JobRepository", "Failed to fetch and save Google Jobs", e)
        }
        return emptyList()
    }

    suspend fun fetchAndSaveMultiSourceJobs(
        searchTerm: String,
        location: String,
        countryIndeed: String = "USA",
        resultsWanted: Int = 10
    ): List<JobEntity> {
        try {
            val jobs = RapidApiClient.getMultiSourceJobs(searchTerm, location, countryIndeed, resultsWanted)
            if (jobs.isNotEmpty()) {
                checkAndTriggerNotifications(jobs)
                jobDao.insertJobs(jobs)
            }
            return jobs
        } catch (e: Exception) {
            Log.e("JobRepository", "Failed to fetch and save Multi-source jobs", e)
        }
        return emptyList()
    }

    /**
     * Master Aggregator: Fetches and saves jobs across all active API keys and endpoints
     * (Gemini AI Live Web Search, RapidAPI Google Jobs Scraper, PR Labs Multi-Source Jobs, Indeed Sourced Sponsors).
     */
    suspend fun fetchAllJobsFromAllApis(
        query: String = "Visa sponsorship",
        country: String = "All"
    ): ApiJobsAggregationResult = coroutineScope {
        val effectiveQuery = query.trim().ifEmpty { "Visa sponsorship jobs" }
        val effectiveCountry = if (country.isBlank()) "All" else country
        val locationParam = if (effectiveCountry == "All") "Remote / Global" else effectiveCountry
        val countryCode = when (effectiveCountry.lowercase()) {
            "canada" -> "CA"
            "united kingdom", "uk" -> "GB"
            "germany" -> "DE"
            "australia" -> "AU"
            "france" -> "FR"
            else -> "US"
        }

        // Launch concurrent fetches across all API endpoints
        val geminiDeferred = async {
            try {
                GeminiApiClient.searchJobs(effectiveQuery, effectiveCountry)
            } catch (e: Exception) {
                Log.e("JobRepository", "Gemini API job fetch failed", e)
                emptyList<JobEntity>()
            }
        }

        val googleJobsDeferred = async {
            try {
                RapidApiClient.getGoogleJobsScraper(
                    query = effectiveQuery,
                    location = locationParam,
                    country = countryCode,
                    domain = "com",
                    maxRows = 20
                )
            } catch (e: Exception) {
                Log.e("JobRepository", "Google Jobs Scraper fetch failed", e)
                emptyList<JobEntity>()
            }
        }

        val multiSourceDeferred = async {
            try {
                RapidApiClient.getMultiSourceJobs(
                    searchTerm = effectiveQuery,
                    location = locationParam,
                    countryIndeed = if (countryCode == "US") "USA" else countryCode,
                    resultsWanted = 10
                )
            } catch (e: Exception) {
                Log.e("JobRepository", "Multi-Source PR Labs API fetch failed", e)
                emptyList<JobEntity>()
            }
        }

        val indeedDeferred = async {
            try {
                val targetCompany = when (effectiveCountry.lowercase()) {
                    "united kingdom", "uk" -> "NHS Trust"
                    "canada" -> "Shopify"
                    "germany" -> "Siemens"
                    else -> "Google"
                }
                RapidApiClient.getIndeedCompanyJobs(
                    companyName = targetCompany,
                    locality = countryCode.lowercase(),
                    start = 1
                )
            } catch (e: Exception) {
                Log.e("JobRepository", "Indeed Company Jobs fetch failed", e)
                emptyList<JobEntity>()
            }
        }

        val geminiJobs = geminiDeferred.await()
        val googleJobs = googleJobsDeferred.await()
        val multiSourceJobs = multiSourceDeferred.await()
        val indeedJobs = indeedDeferred.await()

        // Combine and deduplicate jobs by ID or title+company
        val combined = (geminiJobs + googleJobs + multiSourceJobs + indeedJobs)
            .distinctBy { job ->
                if (job.id.isNotBlank()) job.id else "${job.title.lowercase().trim()}_${job.company.lowercase().trim()}"
            }

        if (combined.isNotEmpty()) {
            checkAndTriggerNotifications(combined)
            jobDao.insertJobs(combined)
        }

        ApiJobsAggregationResult(
            totalJobsAdded = combined.size,
            geminiJobsCount = geminiJobs.size,
            googleJobsCount = googleJobs.size,
            multiSourceJobsCount = multiSourceJobs.size,
            indeedJobsCount = indeedJobs.size,
            allFetchedJobs = combined
        )
    }

    /**
     * Add ALL available jobs: Inserts all curated global seed sponsor listings
     * into Room and queries all active API endpoints concurrently.
     */
    suspend fun addAllAvailableJobs(): Pair<Int, ApiJobsAggregationResult> {
        // 1. Insert all available verified default sponsor jobs
        jobDao.insertJobs(DefaultJobs.list)
        val defaultCount = DefaultJobs.list.size

        // 2. Fetch from all live API keys
        val apiResult = try {
            fetchAllJobsFromAllApis(query = "Visa sponsorship", country = "All")
        } catch (e: Exception) {
            Log.e("JobRepository", "Error aggregating all API keys in addAllAvailableJobs", e)
            ApiJobsAggregationResult()
        }

        val totalNowInDb = (jobDao.getAllJobsFlow().firstOrNull() ?: emptyList()).size
        return Pair(totalNowInDb, apiResult)
    }

    suspend fun toggleBookmark(job: JobEntity) {
        val updated = job.copy(isBookmarked = !job.isBookmarked)
        jobDao.updateJob(updated)
        
        // Sync saved jobs to Firebase
        if (updated.isBookmarked) {
            FirebaseSyncManager.saveJobToFirebase(updated)
        } else {
            FirebaseSyncManager.removeJobFromFirebase(updated.id)
        }
    }

    suspend fun flagAsFraud(job: JobEntity) {
        jobDao.updateJob(job.copy(isFraud = true, confidenceScore = 10))
    }

    suspend fun deleteJob(jobId: String) {
        jobDao.deleteJobById(jobId)
    }

    suspend fun postCustomJob(job: JobEntity) {
        val newJob = job.copy(isCustomPosted = true)
        checkAndTriggerNotifications(listOf(newJob))
        jobDao.insertJob(newJob)
    }

    suspend fun saveProfile(profile: UserProfileEntity) {
        jobDao.insertProfile(profile)
        // Sync to Firebase
        FirebaseSyncManager.uploadProfileToFirebase(profile)
    }

    suspend fun linkLinkedInProfile(data: com.example.auth.LinkedInProfileData, autoImportToProfile: Boolean) {
        val current = jobDao.getUserProfile() ?: UserProfileEntity()
        val formattedResume = com.example.auth.LinkedInOAuthService.formatAsAtsResume(data)
        
        val updated = if (autoImportToProfile) {
            current.copy(
                fullName = if (data.fullName.isNotBlank()) data.fullName else current.fullName,
                skills = if (data.skills.isNotEmpty()) data.skills.joinToString(", ") else current.skills,
                experience = if (data.positions.isNotEmpty()) {
                    data.positions.joinToString("\n\n") { pos ->
                        "${pos.title} at ${pos.company} (${pos.startDate} - ${pos.endDate}): ${pos.description}"
                    }
                } else current.experience,
                education = if (data.educations.isNotEmpty()) {
                    data.educations.joinToString("\n") { edu ->
                        "${edu.degree} in ${edu.fieldOfStudy}, ${edu.school} (${edu.startYear}-${edu.endYear})"
                    }
                } else current.education,
                preferredOccupations = if (data.headline.isNotBlank()) data.headline.take(60) else current.preferredOccupations,
                resumeText = formattedResume,
                linkedInConnected = true,
                linkedInMemberId = data.memberId,
                linkedInEmail = data.email,
                linkedInEmailVerified = data.isEmailVerified,
                linkedInHeadline = data.headline,
                linkedInProfilePicture = data.profilePictureUrl,
                linkedInVerifiedAt = data.verifiedAtTimestamp,
                linkedInVerificationHash = data.verificationHash,
                linkedInTrustScore = data.trustScore,
                linkedInConnectionsCount = data.connectionsCount,
                linkedInIndustry = data.industry
            )
        } else {
            current.copy(
                linkedInConnected = true,
                linkedInMemberId = data.memberId,
                linkedInEmail = data.email,
                linkedInEmailVerified = data.isEmailVerified,
                linkedInHeadline = data.headline,
                linkedInProfilePicture = data.profilePictureUrl,
                linkedInVerifiedAt = data.verifiedAtTimestamp,
                linkedInVerificationHash = data.verificationHash,
                linkedInTrustScore = data.trustScore,
                linkedInConnectionsCount = data.connectionsCount,
                linkedInIndustry = data.industry
            )
        }
        jobDao.insertProfile(updated)
        FirebaseSyncManager.uploadProfileToFirebase(updated)
    }

    suspend fun unlinkLinkedInProfile() {
        val current = jobDao.getUserProfile() ?: UserProfileEntity()
        val updated = current.copy(
            linkedInConnected = false,
            linkedInMemberId = "",
            linkedInEmail = "",
            linkedInEmailVerified = false,
            linkedInHeadline = "",
            linkedInProfilePicture = "",
            linkedInVerifiedAt = 0L,
            linkedInVerificationHash = "",
            linkedInTrustScore = 0,
            linkedInConnectionsCount = "",
            linkedInIndustry = ""
        )
        jobDao.insertProfile(updated)
        FirebaseSyncManager.uploadProfileToFirebase(updated)
    }

    fun getJobDao(): JobDao = jobDao

    suspend fun getUserProfile(): UserProfileEntity? {
        return jobDao.getUserProfile()
    }

    suspend fun addAlert(queryText: String, country: String, email: Boolean, push: Boolean, telegram: Boolean) {
        val alert = CustomAlertEntity(
            queryText = queryText,
            country = country,
            isEmailAlert = email,
            isPushAlert = push,
            isTelegramAlert = telegram
        )
        jobDao.insertAlert(alert)

        // Sync subscription to Firestore
        val firestoreAlert = FirestoreJobAlert(
            id = "alert_${System.currentTimeMillis()}",
            queryText = queryText,
            country = country,
            userEmail = "vincentmwangangi28@gmail.com",
            isActive = true,
            pushEnabled = push,
            emailEnabled = email
        )
        FirebaseSyncManager.subscribeJobAlertToFirestore(firestoreAlert)

        try {
            val existingJobs = jobDao.getAllJobsFlow().firstOrNull() ?: emptyList()
            val userEmail = "vincentmwangangi28@gmail.com"
            val profile = jobDao.getUserProfile()
            val userName = profile?.fullName ?: "Vincent"

            for (job in existingJobs) {
                val countryMatches = country.equals("All", ignoreCase = true) || 
                                     country.isBlank() || 
                                     job.country.equals(country, ignoreCase = true)
                val queryMatches = job.title.contains(queryText, ignoreCase = true) || 
                                   job.description.contains(queryText, ignoreCase = true)

                if (countryMatches && queryMatches) {
                    val title = "Alert Match: ${job.title}"
                    val message = "${job.company} has an active job matching '${queryText}' in ${job.country}!"
                    val emailContent = generateSimulatedEmailBody(job, userEmail, userName)

                    val notification = JobNotificationEntity(
                        title = title,
                        message = message,
                        jobId = job.id,
                        isEmail = email,
                        emailContent = emailContent,
                        isPush = push
                    )
                    jobDao.insertNotification(notification)

                    if (push) {
                        showLocalNotification(title, message)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("JobRepository", "Error triggering immediate matches for new alert", e)
        }
    }

    suspend fun toggleRealtimeSearchAlert(
        queryText: String,
        country: String,
        enabled: Boolean,
        email: Boolean = true,
        push: Boolean = true
    ): Boolean {
        val cleanQuery = queryText.trim().ifEmpty { "Visa sponsorship jobs" }
        val cleanCountry = if (country.isBlank()) "All" else country

        val existingAlerts = jobDao.getAllAlertsFlow().firstOrNull() ?: emptyList()
        val match = existingAlerts.find {
            it.queryText.equals(cleanQuery, ignoreCase = true) &&
            (it.country.equals(cleanCountry, ignoreCase = true) || (it.country.isBlank() && cleanCountry == "All"))
        }

        if (enabled) {
            if (match == null) {
                addAlert(
                    queryText = cleanQuery,
                    country = cleanCountry,
                    email = email,
                    push = push,
                    telegram = false
                )
            }
            return true
        } else {
            if (match != null) {
                deleteAlert(match.id)
            }
            return false
        }
    }

    suspend fun triggerSimulatedRealtimeJobAlert(queryText: String, country: String): JobEntity {
        val effectiveQuery = queryText.trim().ifEmpty { "Senior Android Developer" }
        val effectiveCountry = if (country.isBlank() || country == "All") "United Kingdom" else country

        val companyNames = listOf("Revolut", "Monzo Bank", "Deliveroo", "Spotify", "Shopify", "Klarna", "Siemens Healthineers", "AstraZeneca")
        val sampleCompany = companyNames.random()
        val sampleSalary = when (effectiveCountry) {
            "United Kingdom" -> "£85,000 - £110,000 / yr"
            "Canada" -> "$130,000 - $165,000 CAD / yr"
            "Germany" -> "€75,000 - €95,000 / yr"
            "Australia" -> "$140,000 - $180,000 AUD / yr"
            "Sweden" -> "65,000 - 85,000 SEK / mo"
            else -> "$120,000 - $150,000 USD / yr"
        }

        val visaCategory = when (effectiveCountry) {
            "United Kingdom" -> "Skilled Worker Visa (CoS Guaranteed)"
            "Canada" -> "Global Talent Stream / LMIA Exempt"
            "Germany" -> "EU Blue Card Sponsored"
            "Australia" -> "TSS Subclass 482 / 186 PR Track"
            "Sweden" -> "Swedish Work Permit (Fast-Track)"
            else -> "Full Visa Sponsorship Provided"
        }

        val timestamp = System.currentTimeMillis()
        val liveJob = JobEntity(
            id = "live_rt_${timestamp}",
            title = effectiveQuery,
            company = sampleCompany,
            country = effectiveCountry,
            location = "$sampleCompany HQ ($effectiveCountry)",
            description = "Immediate hiring for $effectiveQuery with verified visa sponsorship, relocation package, and legal fee reimbursement for overseas talent.",
            salary = sampleSalary,
            visaType = visaCategory,
            confidenceScore = 98,
            confidenceReason = "Live Firestore Real-Time Verified Sponsor Alert",
            relocationAssistance = true,
            contractType = "Full-time",
            industry = "Technology",
            experienceLevel = "Senior",
            applicationUrl = "https://www.google.com/search?q=${java.net.URLEncoder.encode("$sampleCompany $effectiveQuery careers", "UTF-8")}",
            datePosted = "Just now (Live Alert)",
            isCustomPosted = false
        )

        // Publish to Firestore live stream and local database
        FirebaseSyncManager.publishJobToLiveFeed(liveJob)
        checkAndTriggerNotifications(listOf(liveJob))
        jobDao.insertJob(liveJob)

        return liveJob
    }

    suspend fun deleteAlert(id: Int) {
        jobDao.deleteAlertById(id)
        FirebaseSyncManager.unsubscribeJobAlertFromFirestore("alert_$id")
    }

    // Notification action handlers
    suspend fun markNotificationAsRead(id: Int) {
        jobDao.markNotificationAsRead(id)
    }

    suspend fun deleteNotification(id: Int) {
        jobDao.deleteNotificationById(id)
    }

    suspend fun clearAllNotifications() {
        jobDao.clearAllNotifications()
    }

    // Visa Application tracking functions
    suspend fun saveVisaApplication(app: VisaApplicationEntity) {
        jobDao.insertVisaApplication(app)
        FirebaseSyncManager.syncVisaApplicationToFirebase(app)
    }

    suspend fun deleteVisaApplication(jobId: String) {
        jobDao.deleteVisaApplicationById(jobId)
        FirebaseSyncManager.deleteVisaApplicationFromFirebase(jobId)
    }

    // Relocation Checklist methods
    suspend fun saveRelocationTask(task: RelocationTaskEntity) {
        jobDao.insertRelocationTask(task)
    }

    suspend fun updateRelocationTask(task: RelocationTaskEntity) {
        jobDao.updateRelocationTask(task)
    }

    suspend fun deleteRelocationTask(id: Int) {
        jobDao.deleteRelocationTaskById(id)
    }

    suspend fun clearAllRelocationTasks() {
        jobDao.clearAllRelocationTasks()
    }

    suspend fun prePopulateRelocationTasks(country: String) {
        val defaultTasks = when (country) {
            "Canada" -> listOf(
                RelocationTaskEntity(taskName = "Obtain positive LMIA or Job Offer approval letter", category = "Visa Documents", country = "Canada"),
                RelocationTaskEntity(taskName = "Apply for Work Permit online (IRCC Portal)", category = "Visa Documents", country = "Canada"),
                RelocationTaskEntity(taskName = "Prepare Proof of Funds (Bank statements, assets)", category = "Visa Documents", country = "Canada"),
                RelocationTaskEntity(taskName = "Complete medical exam and police clearance certificate", category = "Visa Documents", country = "Canada"),
                RelocationTaskEntity(taskName = "Research housing in target province (Rentals.ca, Kijiji)", category = "Housing", country = "Canada"),
                RelocationTaskEntity(taskName = "Budget for first/last month's rent deposit", category = "Housing", country = "Canada"),
                RelocationTaskEntity(taskName = "Apply for provincial health insurance (e.g. OHIP, MSP, AHCIP) upon arrival", category = "Health Insurance", country = "Canada"),
                RelocationTaskEntity(taskName = "Purchase private travel health insurance for the first 3 months", category = "Health Insurance", country = "Canada"),
                RelocationTaskEntity(taskName = "Apply for Social Insurance Number (SIN) at Service Canada", category = "Other", country = "Canada")
            )
            "United Kingdom" -> listOf(
                RelocationTaskEntity(taskName = "Request Certificate of Sponsorship (CoS) from employer", category = "Visa Documents", country = "United Kingdom"),
                RelocationTaskEntity(taskName = "Submit Skilled Worker Visa application online", category = "Visa Documents", country = "United Kingdom"),
                RelocationTaskEntity(taskName = "Book UKVI Biometrics appointment", category = "Visa Documents", country = "United Kingdom"),
                RelocationTaskEntity(taskName = "Provide Tuberculosis (TB) test certificate if applicable", category = "Visa Documents", country = "United Kingdom"),
                RelocationTaskEntity(taskName = "Research flats on Rightmove, Zoopla & register with local agencies", category = "Housing", country = "United Kingdom"),
                RelocationTaskEntity(taskName = "Obtain UK guarantor or prepare 6 months advance rent", category = "Housing", country = "United Kingdom"),
                RelocationTaskEntity(taskName = "Pay the Immigration Health Surcharge (IHS) to access NHS", category = "Health Insurance", country = "United Kingdom"),
                RelocationTaskEntity(taskName = "Register with a local GP (General Practitioner) clinic", category = "Health Insurance", country = "United Kingdom"),
                RelocationTaskEntity(taskName = "Collect Biometric Residence Permit (BRP) from designated Post Office", category = "Other", country = "United Kingdom")
            )
            "Australia" -> listOf(
                RelocationTaskEntity(taskName = "Get Employer Nomination Scheme (Subclass 186/482) confirmation", category = "Visa Documents", country = "Australia"),
                RelocationTaskEntity(taskName = "Submit visa application on ImmiAccount", category = "Visa Documents", country = "Australia"),
                RelocationTaskEntity(taskName = "Undergo skills assessment & English proficiency test (IELTS/PTE)", category = "Visa Documents", country = "Australia"),
                RelocationTaskEntity(taskName = "Search rental markets on Realestate.com.au & Domain", category = "Housing", country = "Australia"),
                RelocationTaskEntity(taskName = "Inspect rental properties (requires local contact or agent)", category = "Housing", country = "Australia"),
                RelocationTaskEntity(taskName = "Obtain Overseas Visitors Health Cover (OVHC) if required", category = "Health Insurance", country = "Australia"),
                RelocationTaskEntity(taskName = "Check eligibility and enroll in Medicare (reciprocal health agreement)", category = "Health Insurance", country = "Australia"),
                RelocationTaskEntity(taskName = "Apply for Tax File Number (TFN) from Australian Taxation Office", category = "Other", country = "Australia")
            )
            "Germany" -> listOf(
                RelocationTaskEntity(taskName = "Obtain signed employment contract and declaration of employment", category = "Visa Documents", country = "Germany"),
                RelocationTaskEntity(taskName = "Book National Visa D appointment at local German embassy", category = "Visa Documents", country = "Germany"),
                RelocationTaskEntity(taskName = "Prepare recognized university degree or certificate translation", category = "Visa Documents", country = "Germany"),
                RelocationTaskEntity(taskName = "Book temporary housing (e.g. Wunderflats, WG-Gesucht) allowing Anmeldung", category = "Housing", country = "Germany"),
                RelocationTaskEntity(taskName = "Complete Anmeldung (city registration) within 14 days of moving", category = "Housing", country = "Germany"),
                RelocationTaskEntity(taskName = "Secure public health insurance (e.g. TK, AOK) or certified expat insurance", category = "Health Insurance", country = "Germany"),
                RelocationTaskEntity(taskName = "Receive Sozialversicherungsnummer (social security number)", category = "Health Insurance", country = "Germany"),
                RelocationTaskEntity(taskName = "Open German bank account (e.g. N26, Sparkasse) for salary deposits", category = "Other", country = "Germany")
            )
            "Sweden" -> listOf(
                RelocationTaskEntity(taskName = "Wait for Swedish Migration Agency (Migrationsverket) email offer", category = "Visa Documents", country = "Sweden"),
                RelocationTaskEntity(taskName = "Submit complete application & pay processing fee", category = "Visa Documents", country = "Sweden"),
                RelocationTaskEntity(taskName = "Book appointment for biometrics and passport verification", category = "Visa Documents", country = "Sweden"),
                RelocationTaskEntity(taskName = "Research housing in Stockholm/Gothenburg (Qasa, Blocket Bostad)", category = "Housing", country = "Sweden"),
                RelocationTaskEntity(taskName = "Register with Swedish Tax Agency (Skatteverket) for personal identity number", category = "Housing", country = "Sweden"),
                RelocationTaskEntity(taskName = "Check registration for Swedish public healthcare system", category = "Health Insurance", country = "Sweden"),
                RelocationTaskEntity(taskName = "Buy comprehensive home and health insurance (e.g. Hedvig, IF)", category = "Health Insurance", country = "Sweden"),
                RelocationTaskEntity(taskName = "Apply for Swedish ID Card (Identitetskort) at Skatteverket", category = "Other", country = "Sweden")
            )
            else -> listOf(
                RelocationTaskEntity(taskName = "Gather formal job offer & sponsorship details from employer", category = "Visa Documents", country = "General"),
                RelocationTaskEntity(taskName = "Confirm passport is valid for at least 6 months past entry", category = "Visa Documents", country = "General"),
                RelocationTaskEntity(taskName = "Acquire certified translations for degrees and birth certificates", category = "Visa Documents", country = "General"),
                RelocationTaskEntity(taskName = "Book temporary accommodation for the first 2-4 weeks", category = "Housing", country = "General"),
                RelocationTaskEntity(taskName = "Compare long-term rental market portals in target city", category = "Housing", country = "General"),
                RelocationTaskEntity(taskName = "Acquire international private health insurance covering transition", category = "Health Insurance", country = "General"),
                RelocationTaskEntity(taskName = "Research local health registration requirements and GP systems", category = "Health Insurance", country = "General"),
                RelocationTaskEntity(taskName = "Research local transit options, sim cards, and utility providers", category = "Other", country = "General")
            )
        }
        for (task in defaultTasks) {
            jobDao.insertRelocationTask(task)
        }
    }

    // Document Vault & Expiry methods
    suspend fun saveVisaDocument(doc: VisaDocumentEntity) {
        jobDao.insertVisaDocument(doc)
    }

    suspend fun updateVisaDocument(doc: VisaDocumentEntity) {
        jobDao.updateVisaDocument(doc)
    }

    suspend fun deleteVisaDocument(id: Int) {
        jobDao.deleteVisaDocumentById(id)
    }

    suspend fun clearAllVisaDocuments() {
        jobDao.clearAllVisaDocuments()
    }

    suspend fun prePopulateVisaDocuments(targetCountry: String) {
        val docs = listOf(
            VisaDocumentEntity(
                documentName = "International Passport",
                category = "Passport",
                documentNumber = "A12345678",
                issuingAuthority = "Immigration & Passport Directorate",
                issueDate = "2023-01-15",
                expiryDate = "2033-01-15",
                notes = "Must have at least 2 blank visa pages and 6 months validity.",
                isVerified = true
            ),
            VisaDocumentEntity(
                documentName = "Language Proficiency Certificate (IELTS UKVI / CELPIP)",
                category = "Language Test",
                documentNumber = "TRF-98217349",
                issuingAuthority = "British Council / IDP / Paragon",
                issueDate = "2025-06-10",
                expiryDate = "2027-06-10",
                notes = "Valid for 2 years from test date. Required score: Band 6.5+.",
                isVerified = true
            ),
            VisaDocumentEntity(
                documentName = "Educational Credential Assessment (ECA)",
                category = "ECA / Degree",
                documentNumber = "WES-8492019",
                issuingAuthority = "World Education Services / ECCTIS",
                issueDate = "2024-04-12",
                expiryDate = "2029-04-12",
                notes = "Valid for 5 years. Verifies Master's/Bachelor's degree equivalency.",
                isVerified = true
            ),
            VisaDocumentEntity(
                documentName = "Police Clearance Certificate (PCC)",
                category = "Police Clearance",
                documentNumber = "PCC-2026-4412",
                issuingAuthority = "National Police Criminal Records Bureau",
                issueDate = "2026-02-01",
                expiryDate = "2026-08-01",
                notes = "Must be less than 6 months old at time of visa submission.",
                isVerified = false
            ),
            VisaDocumentEntity(
                documentName = "Medical & TB Examination Certificate",
                category = "Medical",
                documentNumber = "MED-00918",
                issuingAuthority = "IOM / Panel Approved Physician Clinic",
                issueDate = "2026-03-10",
                expiryDate = "2026-09-10",
                notes = "Panel clinic health exam valid for 6 months.",
                isVerified = false
            ),
            VisaDocumentEntity(
                documentName = "Certificate of Sponsorship (CoS) / LMIA Offer",
                category = "CoS / LMIA Offer",
                documentNumber = "COS-PENDING",
                issuingAuthority = "Sponsoring Employer / Ministry",
                issueDate = "2026-08-01",
                expiryDate = "2026-11-01",
                notes = "Issued by verified sponsor upon formal contract acceptance.",
                isVerified = false
            )
        )
        for (doc in docs) {
            jobDao.insertVisaDocument(doc)
        }
    }
}

data class ApiJobsAggregationResult(
    val totalJobsAdded: Int = 0,
    val geminiJobsCount: Int = 0,
    val googleJobsCount: Int = 0,
    val multiSourceJobsCount: Int = 0,
    val indeedJobsCount: Int = 0,
    val allFetchedJobs: List<JobEntity> = emptyList()
)
