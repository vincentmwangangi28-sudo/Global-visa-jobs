package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.auth.LinkedInOAuthService
import com.example.auth.LinkedInOAuthState
import com.example.auth.LinkedInProfileData
import com.example.data.*
import com.example.network.GeminiApiClient
import com.example.network.JobVerificationResult
import com.example.network.LinkedInJobCountResult
import com.example.network.RapidApiClient
import com.example.network.ParsedResumeResult
import com.example.network.ResumeGapAnalysisResult

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class JobViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "visajobs-db"
    ).fallbackToDestructiveMigration().build()

    private val repository = JobRepository(db, application)

    // Mode Toggle: "Jobseeker", "Employer", "Admin"
    private val _appMode = MutableStateFlow("Jobseeker")
    val appMode: StateFlow<String> = _appMode.asStateFlow()

    // Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCountry = MutableStateFlow("All")
    val selectedCountry: StateFlow<String> = _selectedCountry.asStateFlow()

    private val _selectedIndustry = MutableStateFlow("All")
    val selectedIndustry: StateFlow<String> = _selectedIndustry.asStateFlow()

    private val _selectedExperience = MutableStateFlow("All")
    val selectedExperience: StateFlow<String> = _selectedExperience.asStateFlow()

    private val _selectedSponsorship = MutableStateFlow("All")
    val selectedSponsorship: StateFlow<String> = _selectedSponsorship.asStateFlow()

    // Database flow bindings
    val allJobs: StateFlow<List<JobEntity>> = repository.allJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val customAlerts: StateFlow<List<CustomAlertEntity>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val visaApplications: StateFlow<List<VisaApplicationEntity>> = repository.allVisaApplications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val relocationTasks: StateFlow<List<RelocationTaskEntity>> = repository.allRelocationTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<JobNotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI States
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchStatus = MutableStateFlow<String?>(null)
    val searchStatus: StateFlow<String?> = _searchStatus.asStateFlow()

    private val _resumeResult = MutableStateFlow<String?>(null)
    val resumeResult: StateFlow<String?> = _resumeResult.asStateFlow()

    private val _isGeneratingResume = MutableStateFlow(false)
    val isGeneratingResume: StateFlow<Boolean> = _isGeneratingResume.asStateFlow()

    private val _compatibilityDetails = MutableStateFlow<Pair<Int, String>?>(null)
    val compatibilityDetails: StateFlow<Pair<Int, String>?> = _compatibilityDetails.asStateFlow()

    private val _isAnalyzingCompatibility = MutableStateFlow(false)
    val isAnalyzingCompatibility: StateFlow<Boolean> = _isAnalyzingCompatibility.asStateFlow()

    private val _jobsCompatibilityMap = MutableStateFlow<Map<String, Pair<Int, String>>>(emptyMap())
    val jobsCompatibilityMap: StateFlow<Map<String, Pair<Int, String>>> = _jobsCompatibilityMap.asStateFlow()

    private val _analyzingJobIds = MutableStateFlow<Set<String>>(emptySet())
    val analyzingJobIds: StateFlow<Set<String>> = _analyzingJobIds.asStateFlow()

    private val _jobsGapAnalysisMap = MutableStateFlow<Map<String, ResumeGapAnalysisResult>>(emptyMap())
    val jobsGapAnalysisMap: StateFlow<Map<String, ResumeGapAnalysisResult>> = _jobsGapAnalysisMap.asStateFlow()

    private val _isAnalyzingGap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val isAnalyzingGap: StateFlow<Map<String, Boolean>> = _isAnalyzingGap.asStateFlow()

    // Global Salary Range States (RapidAPI)
    private val _salaryInsight = MutableStateFlow<RapidApiClient.SalaryInsight?>(null)
    val salaryInsight: StateFlow<RapidApiClient.SalaryInsight?> = _salaryInsight.asStateFlow()

    private val _isQueryingSalary = MutableStateFlow(false)
    val isQueryingSalary: StateFlow<Boolean> = _isQueryingSalary.asStateFlow()

    private val _salaryError = MutableStateFlow<String?>(null)
    val salaryError: StateFlow<String?> = _salaryError.asStateFlow()

    // Job Verification States
    private val _jobVerifications = MutableStateFlow<Map<String, JobVerificationResult>>(emptyMap())
    val jobVerifications: StateFlow<Map<String, JobVerificationResult>> = _jobVerifications.asStateFlow()

    private val _isVerifyingJob = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val isVerifyingJob: StateFlow<Map<String, Boolean>> = _isVerifyingJob.asStateFlow()

    // LinkedIn Job Count States
    private val _linkedInJobCount = MutableStateFlow<LinkedInJobCountResult?>(null)
    val linkedInJobCount: StateFlow<LinkedInJobCountResult?> = _linkedInJobCount.asStateFlow()

    private val _isFetchingLinkedInCount = MutableStateFlow(false)
    val isFetchingLinkedInCount: StateFlow<Boolean> = _isFetchingLinkedInCount.asStateFlow()

    private val _linkedInCountError = MutableStateFlow<String?>(null)
    val linkedInCountError: StateFlow<String?> = _linkedInCountError.asStateFlow()

    // Indeed Company Jobs States
    private val _isFetchingIndeedJobs = MutableStateFlow(false)
    val isFetchingIndeedJobs: StateFlow<Boolean> = _isFetchingIndeedJobs.asStateFlow()

    private val _indeedError = MutableStateFlow<String?>(null)
    val indeedError: StateFlow<String?> = _indeedError.asStateFlow()

    // First-search Profile Completion Reminder Toast State
    private val _showProfileReminderToast = MutableStateFlow(false)
    val showProfileReminderToast: StateFlow<Boolean> = _showProfileReminderToast.asStateFlow()

    private var hasSearchedOnce = false

    fun checkAndTriggerFirstSearchReminder() {
        if (!hasSearchedOnce) {
            hasSearchedOnce = true
            viewModelScope.launch {
                val profile = repository.getUserProfile()
                if (profile == null || profile.nationality.isBlank() || profile.education.isBlank()) {
                    _showProfileReminderToast.value = true
                }
            }
        }
    }

    fun dismissProfileReminderToast() {
        _showProfileReminderToast.value = false
    }

    init {
        viewModelScope.launch {
            repository.initializeWithDefaultJobs()
        }
    }

    fun setAppMode(mode: String) {
        _appMode.value = mode
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCountry(country: String) {
        _selectedCountry.value = country
    }

    fun selectIndustry(industry: String) {
        _selectedIndustry.value = industry
    }

    fun selectExperience(exp: String) {
        _selectedExperience.value = exp
    }

    fun selectSponsorship(sponsorship: String) {
        _selectedSponsorship.value = sponsorship
    }

    /**
     * Trigger Live Web Scrape Search powered by Google Search Grounding & Gemini 3.5-flash
     */
    fun performLiveScrapeSearch(query: String, country: String) {
        checkAndTriggerFirstSearchReminder()
        viewModelScope.launch {
            _isSearching.value = true
            _searchStatus.value = "Initiating global visa-sponsored job search..."
            val success = repository.searchAndScrapeJobs(query, country)
            if (success) {
                _searchStatus.value = "Successfully aggregated and verified live jobs."
            } else {
                _searchStatus.value = "No live results found. Displaying fallback verified jobs database."
            }
            _isSearching.value = false
        }
    }

    fun clearSearchStatus() {
        _searchStatus.value = null
    }

    fun toggleBookmark(job: JobEntity) {
        viewModelScope.launch {
            repository.toggleBookmark(job)
        }
    }

    fun flagAsFraud(job: JobEntity) {
        viewModelScope.launch {
            repository.flagAsFraud(job)
        }
    }

    fun deleteJob(jobId: String) {
        viewModelScope.launch {
            repository.deleteJob(jobId)
        }
    }

    fun createCustomJob(
        title: String,
        company: String,
        country: String,
        location: String,
        description: String,
        salary: String,
        visaType: String,
        relocation: Boolean,
        contractType: String,
        industry: String,
        experienceLevel: String,
        applicationUrl: String
    ) {
        viewModelScope.launch {
            val newJob = JobEntity(
                id = "employer_" + System.currentTimeMillis(),
                title = title,
                company = company,
                country = country,
                location = location,
                description = description,
                salary = salary,
                visaType = visaType,
                confidenceScore = 100,
                confidenceReason = "Direct verified employer listing.",
                relocationAssistance = relocation,
                contractType = contractType,
                industry = industry,
                experienceLevel = experienceLevel,
                applicationUrl = applicationUrl,
                datePosted = "2026-06-27",
                isBookmarked = false,
                isCustomPosted = true
            )
            repository.postCustomJob(newJob)
        }
    }

    fun updateProfile(
        fullName: String,
        nationality: String,
        currentCountry: String,
        passportCountry: String,
        education: String,
        skills: String,
        languages: String,
        experience: String,
        desiredCountries: String,
        preferredOccupations: String,
        salaryExpectations: String
    ) {
        viewModelScope.launch {
            val current = repository.getUserProfile() ?: UserProfileEntity()
            val updated = current.copy(
                fullName = fullName,
                nationality = nationality,
                currentCountry = currentCountry,
                passportCountry = passportCountry,
                education = education,
                skills = skills,
                languages = languages,
                experience = experience,
                desiredCountries = desiredCountries,
                preferredOccupations = preferredOccupations,
                salaryExpectations = salaryExpectations
            )
            repository.saveProfile(updated)
        }
    }

    /**
     * Generate tailor ATS resume or country Cover Letter with specified tone
     */
    fun generateResumeOrCoverLetter(role: String, type: String, tone: String = "Professional") {
        viewModelScope.launch {
            val profile = repository.getUserProfile()
            if (profile == null) {
                _resumeResult.value = "Please fill in your Profile first before generating materials."
                return@launch
            }
            _isGeneratingResume.value = true
            _resumeResult.value = "AI is drafting your customized $type ($tone tone)..."
            val result = GeminiApiClient.generateResumeHelper(
                fullName = profile.fullName.ifEmpty { "Applicant" },
                nationality = profile.nationality.ifEmpty { "Global Candidate" },
                education = profile.education.ifEmpty { "High School / College" },
                experience = profile.experience.ifEmpty { "Relevant work history" },
                skills = profile.skills.ifEmpty { "General labor/office skills" },
                targetCountry = profile.desiredCountries.split(",").firstOrNull()?.trim() ?: "Canada",
                role = role,
                type = type,
                tone = tone
            )
            _resumeResult.value = result
            _isGeneratingResume.value = false
        }
    }

    fun clearResumeResult() {
        _resumeResult.value = null
    }

    /**
     * Analyze compatibility
     */
    fun analyzeJobCompatibility(job: JobEntity) {
        viewModelScope.launch {
            val profile = repository.getUserProfile()
            if (profile == null) {
                _compatibilityDetails.value = Pair(50, "Fill in your Profile details in the Match tab to enable AI matching.")
                return@launch
            }
            _isAnalyzingCompatibility.value = true
            _analyzingJobIds.value = _analyzingJobIds.value + job.id
            val (score, explanation) = GeminiApiClient.getCompatibilityExplanation(
                jobTitle = job.title,
                jobCompany = job.company,
                jobDescription = job.description,
                userSkills = profile.skills,
                userExp = profile.experience,
                userEducation = profile.education
            )
            _compatibilityDetails.value = Pair(score, explanation)
            _jobsCompatibilityMap.value = _jobsCompatibilityMap.value + (job.id to Pair(score, explanation))
            _analyzingJobIds.value = _analyzingJobIds.value - job.id
            _isAnalyzingCompatibility.value = false
        }
    }

    /**
     * Analyze gaps between parsed resume (or fall back to user profile) and a job opening.
     */
    fun analyzeResumeGap(job: JobEntity) {
        viewModelScope.launch {
            _isAnalyzingGap.value = _isAnalyzingGap.value + (job.id to true)
            try {
                val parsed = _parsedResumeResult.value
                val profile = repository.getUserProfile()
                
                val skills: List<String>
                val exp: String
                val edu: String
                
                if (parsed != null && (parsed.skills.isNotEmpty() || parsed.experience.isNotEmpty())) {
                    skills = parsed.skills
                    exp = parsed.experience
                    edu = parsed.education
                } else if (profile != null) {
                    skills = profile.skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    exp = profile.experience
                    edu = profile.education
                } else {
                    skills = emptyList()
                    exp = ""
                    edu = ""
                }
                
                val result = GeminiApiClient.getResumeGapAnalysis(
                    jobTitle = job.title,
                    jobCompany = job.company,
                    jobDescription = job.description,
                    resumeSkills = skills,
                    resumeExp = exp,
                    resumeEducation = edu
                )
                _jobsGapAnalysisMap.value = _jobsGapAnalysisMap.value + (job.id to result)
            } catch (e: Exception) {
                Log.e("JobViewModel", "Error in analyzeResumeGap", e)
            } finally {
                _isAnalyzingGap.value = _isAnalyzingGap.value + (job.id to false)
            }
        }
    }

    fun clearCompatibilityDetails() {
        _compatibilityDetails.value = null
    }

    fun clearAllCompatibilityScores() {
        _jobsCompatibilityMap.value = emptyMap()
        _compatibilityDetails.value = null
    }

    fun addCustomAlert(queryText: String, country: String, email: Boolean, push: Boolean, telegram: Boolean) {
        viewModelScope.launch {
            repository.addAlert(queryText, country, email, push, telegram)
        }
    }

    fun deleteAlert(id: Int) {
        viewModelScope.launch {
            repository.deleteAlert(id)
        }
    }

    // Notification Actions
    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    // Visa Tracking Actions
    fun saveVisaApplication(app: VisaApplicationEntity) {
        viewModelScope.launch {
            repository.saveVisaApplication(app)
        }
    }

    fun deleteVisaApplication(jobId: String) {
        viewModelScope.launch {
            repository.deleteVisaApplication(jobId)
        }
    }

    // Relocation Checklist Actions
    fun saveRelocationTask(task: RelocationTaskEntity) {
        viewModelScope.launch {
            repository.saveRelocationTask(task)
        }
    }

    fun updateRelocationTask(task: RelocationTaskEntity) {
        viewModelScope.launch {
            repository.updateRelocationTask(task)
        }
    }

    fun deleteRelocationTask(id: Int) {
        viewModelScope.launch {
            repository.deleteRelocationTask(id)
        }
    }

    fun clearAllRelocationTasks() {
        viewModelScope.launch {
            repository.clearAllRelocationTasks()
        }
    }

    fun prePopulateRelocationTasks(country: String) {
        viewModelScope.launch {
            repository.prePopulateRelocationTasks(country)
        }
    }

    /**
     * Query salary range insights from RapidAPI
     */
    fun querySalaryRange(query: String, countryCode: String) {
        viewModelScope.launch {
            _isQueryingSalary.value = true
            _salaryError.value = null
            try {
                val result = RapidApiClient.getSalaryRange(query, countryCode)
                _salaryInsight.value = result
            } catch (e: Exception) {
                _salaryError.value = e.message ?: "Failed to query salary data"
            } finally {
                _isQueryingSalary.value = false
            }
        }
    }

    fun clearSalaryInsight() {
        _salaryInsight.value = null
        _salaryError.value = null
    }

    /**
     * Executes the Gemini-powered job listing scam & red-flag verification.
     */
    fun verifyJobListing(job: JobEntity) {
        viewModelScope.launch {
            _isVerifyingJob.value = _isVerifyingJob.value + (job.id to true)
            try {
                val result = GeminiApiClient.verifyJobListing(
                    title = job.title,
                    company = job.company,
                    description = job.description,
                    country = job.country
                )
                _jobVerifications.value = _jobVerifications.value + (job.id to result)
            } catch (e: Exception) {
                Log.e("JobViewModel", "Error verifying job listing: ${job.id}", e)
            } finally {
                _isVerifyingJob.value = _isVerifyingJob.value + (job.id to false)
            }
        }
    }

    /**
     * Executes the LinkedIn live job count query.
     */
    fun fetchLinkedInJobCount(title: String, country: String, timeFrame: String = "24h") {
        viewModelScope.launch {
            _isFetchingLinkedInCount.value = true
            _linkedInCountError.value = null
            try {
                // Format location parameter for LinkedIn Job Search API
                val location = when (country) {
                    "All" -> "\"United States\" OR \"United Kingdom\""
                    else -> "\"$country\""
                }
                val queryTitle = title.trim().ifEmpty { "Data Engineer" }
                val result = RapidApiClient.getActiveJobsCount(
                    title = queryTitle,
                    location = location,
                    timeFrame = timeFrame
                )
                _linkedInJobCount.value = result
            } catch (e: Exception) {
                Log.e("JobViewModel", "Error fetching LinkedIn job count", e)
                _linkedInCountError.value = e.message ?: "Failed to contact LinkedIn Job Search API"
            } finally {
                _isFetchingLinkedInCount.value = false
            }
        }
    }

    fun clearLinkedInCount() {
        _linkedInJobCount.value = null
        _linkedInCountError.value = null
    }

    /**
     * Executes the Indeed company job search lookup.
     */
    fun fetchIndeedCompanyJobs(companyName: String, locality: String = "us", start: Int = 1) {
        viewModelScope.launch {
            _isFetchingIndeedJobs.value = true
            _indeedError.value = null
            _searchStatus.value = "Fetching live Indeed job listings for '$companyName'..."
            try {
                val results = repository.fetchAndSaveIndeedJobs(companyName, locality, start)
                if (results.isNotEmpty()) {
                    _searchStatus.value = "Successfully fetched ${results.size} jobs from Indeed for $companyName."
                } else {
                    _indeedError.value = "No job listings found for '$companyName' on Indeed."
                    _searchStatus.value = "No Indeed job listings found for $companyName."
                }
            } catch (e: Exception) {
                Log.e("JobViewModel", "Error fetching indeed jobs", e)
                _indeedError.value = e.message ?: "Failed to fetch jobs from Indeed"
                _searchStatus.value = "Indeed lookup failed: ${e.message}"
            } finally {
                _isFetchingIndeedJobs.value = false
            }
        }
    }

    fun clearIndeedError() {
        _indeedError.value = null
    }

    // Multi-Source Jobs (RapidAPI getjobs_excel) States
    private val _isFetchingMultiSource = MutableStateFlow(false)
    val isFetchingMultiSource: StateFlow<Boolean> = _isFetchingMultiSource.asStateFlow()

    private val _multiSourceError = MutableStateFlow<String?>(null)
    val multiSourceError: StateFlow<String?> = _multiSourceError.asStateFlow()

    fun fetchMultiSourceJobs(searchTerm: String, location: String, countryIndeed: String = "USA", resultsWanted: Int = 5) {
        viewModelScope.launch {
            _isFetchingMultiSource.value = true
            _multiSourceError.value = null
            _searchStatus.value = "Initiating multi-source scrape via PR Labs Job Search API..."
            try {
                val results = RapidApiClient.getMultiSourceJobs(searchTerm, location, countryIndeed, resultsWanted)
                if (results.isNotEmpty()) {
                    db.jobDao().insertJobs(results)
                    _searchStatus.value = "Successfully aggregated ${results.size} multi-source jobs."
                } else {
                    _multiSourceError.value = "No jobs found for '$searchTerm' in '$location'."
                    _searchStatus.value = "Multi-source API query completed with no matches."
                }
            } catch (e: Exception) {
                Log.e("JobViewModel", "Error fetching multi-source jobs", e)
                _multiSourceError.value = e.message ?: "Failed to fetch multi-source jobs"
                _searchStatus.value = "Multi-source query failed: ${e.message}"
            } finally {
                _isFetchingMultiSource.value = false
            }
        }
    }

    fun clearMultiSourceError() {
        _multiSourceError.value = null
    }

    // Google Jobs Scraper States
    private val _isFetchingGoogleJobs = MutableStateFlow(false)
    val isFetchingGoogleJobs: StateFlow<Boolean> = _isFetchingGoogleJobs.asStateFlow()

    private val _googleJobsError = MutableStateFlow<String?>(null)
    val googleJobsError: StateFlow<String?> = _googleJobsError.asStateFlow()

    fun fetchGoogleJobs(
        query: String,
        location: String,
        country: String = "US",
        domain: String = "com",
        maxRows: Int = 20
    ) {
        viewModelScope.launch {
            _isFetchingGoogleJobs.value = true
            _googleJobsError.value = null
            _searchStatus.value = "Scraping Google Jobs via RapidAPI..."
            try {
                val results = RapidApiClient.getGoogleJobsScraper(query, location, country, domain, maxRows)
                if (results.isNotEmpty()) {
                    db.jobDao().insertJobs(results)
                    _searchStatus.value = "Successfully parsed ${results.size} Google Jobs."
                } else {
                    _googleJobsError.value = "No Google jobs found for '$query' in '$location'."
                    _searchStatus.value = "Google Jobs Scraper returned no results."
                }
            } catch (e: Exception) {
                Log.e("JobViewModel", "Error fetching Google jobs", e)
                _googleJobsError.value = e.message ?: "Failed to fetch jobs from Google Jobs Scraper"
                _searchStatus.value = "Google Jobs Scraper failed: ${e.message}"
            } finally {
                _isFetchingGoogleJobs.value = false
            }
        }
    }

    fun clearGoogleJobsError() {
        _googleJobsError.value = null
    }

    // Resume Parsing States
    private val _isParsingResume = MutableStateFlow(false)
    val isParsingResume: StateFlow<Boolean> = _isParsingResume.asStateFlow()

    private val _parsedResumeResult = MutableStateFlow<ParsedResumeResult?>(null)
    val parsedResumeResult: StateFlow<ParsedResumeResult?> = _parsedResumeResult.asStateFlow()

    private val _parseResumeError = MutableStateFlow<String?>(null)
    val parseResumeError: StateFlow<String?> = _parseResumeError.asStateFlow()

    fun parseResume(resumeText: String) {
        viewModelScope.launch {
            _isParsingResume.value = true
            _parseResumeError.value = null
            _parsedResumeResult.value = null
            try {
                val result = RapidApiClient.parseResumeWithResumeOptimizerPro(resumeText)
                if (result.error != null) {
                    _parseResumeError.value = result.error
                } else {
                    _parsedResumeResult.value = result
                }
            } catch (e: Exception) {
                Log.e("JobViewModel", "Error parsing resume", e)
                _parseResumeError.value = e.message ?: "Failed to parse resume"
            } finally {
                _isParsingResume.value = false
            }
        }
    }

    fun clearParsedResumeResult() {
        _parsedResumeResult.value = null
        _parseResumeError.value = null
    }

    // Market Forecast States
    private val _marketForecast = MutableStateFlow<String?>(null)
    val marketForecast: StateFlow<String?> = _marketForecast.asStateFlow()

    private val _isGeneratingForecast = MutableStateFlow(false)
    val isGeneratingForecast: StateFlow<Boolean> = _isGeneratingForecast.asStateFlow()

    fun generateMarketForecast(countryName: String) {
        viewModelScope.launch {
            _isGeneratingForecast.value = true
            _marketForecast.value = "AI is analyzing regional immigration streams, wage curves, and economic files for $countryName..."
            try {
                val result = GeminiApiClient.generateMarketForecast(countryName)
                _marketForecast.value = result
            } catch (e: Exception) {
                _marketForecast.value = "Failed to generate market forecast: ${e.message}"
            } finally {
                _isGeneratingForecast.value = false
            }
        }
    }

    fun clearMarketForecast() {
        _marketForecast.value = null
    }

    // Visa Interview Prep States
    private val _interviewPrepQuestions = MutableStateFlow<List<com.example.network.VisaInterviewQuestion>>(emptyList())
    val interviewPrepQuestions: StateFlow<List<com.example.network.VisaInterviewQuestion>> = _interviewPrepQuestions.asStateFlow()

    private val _isGeneratingInterviewPrep = MutableStateFlow(false)
    val isGeneratingInterviewPrep: StateFlow<Boolean> = _isGeneratingInterviewPrep.asStateFlow()

    fun generateVisaInterviewPrep(targetRole: String, targetCountry: String, visaType: String) {
        viewModelScope.launch {
            _isGeneratingInterviewPrep.value = true
            try {
                val questions = GeminiApiClient.generateVisaInterviewPrep(targetRole, targetCountry, visaType)
                _interviewPrepQuestions.value = questions
            } catch (e: Exception) {
                Log.e("JobViewModel", "Error in interview prep", e)
            } finally {
                _isGeneratingInterviewPrep.value = false
            }
        }
    }

    // Recruiter Cold Outreach Email States
    private val _recruiterColdEmailResult = MutableStateFlow<com.example.network.RecruiterOutreachResult?>(null)
    val recruiterColdEmailResult: StateFlow<com.example.network.RecruiterOutreachResult?> = _recruiterColdEmailResult.asStateFlow()

    private val _isGeneratingColdEmail = MutableStateFlow(false)
    val isGeneratingColdEmail: StateFlow<Boolean> = _isGeneratingColdEmail.asStateFlow()

    fun generateRecruiterColdEmail(
        candidateName: String,
        candidateSkills: String,
        targetCompany: String,
        targetRole: String,
        targetCountry: String,
        tone: String = "Professional & Persuasive"
    ) {
        viewModelScope.launch {
            _isGeneratingColdEmail.value = true
            try {
                val result = GeminiApiClient.generateRecruiterColdEmail(
                    candidateName, candidateSkills, targetCompany, targetRole, targetCountry, tone
                )
                _recruiterColdEmailResult.value = result
            } catch (e: Exception) {
                Log.e("JobViewModel", "Error in cold email", e)
            } finally {
                _isGeneratingColdEmail.value = false
            }
        }
    }

    fun clearRecruiterColdEmail() {
        _recruiterColdEmailResult.value = null
    }

    // Relocation & Net Salary Insight States
    private val _relocationSalaryInsight = MutableStateFlow<com.example.network.RelocationSalaryInsightResult?>(null)
    val relocationSalaryInsight: StateFlow<com.example.network.RelocationSalaryInsightResult?> = _relocationSalaryInsight.asStateFlow()

    private val _isCalculatingRelocationSalary = MutableStateFlow(false)
    val isCalculatingRelocationSalary: StateFlow<Boolean> = _isCalculatingRelocationSalary.asStateFlow()

    fun calculateRelocationCostAndNetSalary(offeredSalaryText: String, targetCountry: String, familyMembersCount: Int = 1) {
        viewModelScope.launch {
            _isCalculatingRelocationSalary.value = true
            try {
                val result = GeminiApiClient.calculateRelocationCostAndNetSalary(offeredSalaryText, targetCountry, familyMembersCount)
                _relocationSalaryInsight.value = result
            } catch (e: Exception) {
                Log.e("JobViewModel", "Error calculating relocation salary insight", e)
            } finally {
                _isCalculatingRelocationSalary.value = false
            }
        }
    }

    fun clearRelocationSalaryInsight() {
        _relocationSalaryInsight.value = null
    }

    // ==========================================
    // LinkedIn OAuth2 & Verification Architecture
    // ==========================================
    private val _linkedInOAuthState = MutableStateFlow<LinkedInOAuthState>(LinkedInOAuthState.Idle)
    val linkedInOAuthState: StateFlow<LinkedInOAuthState> = _linkedInOAuthState.asStateFlow()

    private val _linkedInImportPreview = MutableStateFlow<LinkedInProfileData?>(null)
    val linkedInImportPreview: StateFlow<LinkedInProfileData?> = _linkedInImportPreview.asStateFlow()

    private val _isUnlinkingLinkedIn = MutableStateFlow(false)
    val isUnlinkingLinkedIn: StateFlow<Boolean> = _isUnlinkingLinkedIn.asStateFlow()

    /**
     * Prepares OAuth2 authorization URL and launches LinkedIn Login.
     */
    fun startLinkedInOAuthFlow(context: android.content.Context) {
        try {
            _linkedInOAuthState.value = LinkedInOAuthState.Authorizing
            val (authUrl, _) = LinkedInOAuthService.createAuthorizationUrl()
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(authUrl)).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("JobViewModel", "Failed to launch LinkedIn OAuth URL", e)
            _linkedInOAuthState.value = LinkedInOAuthState.Error("Could not open browser: ${e.message}")
        }
    }

    /**
     * Handles the deep link redirect callback from LinkedIn with authorization code and state token.
     */
    fun handleLinkedInOAuthCallback(uri: android.net.Uri) {
        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")
        val error = uri.getQueryParameter("error")
        val errorDescription = uri.getQueryParameter("error_description")

        if (error != null) {
            _linkedInOAuthState.value = LinkedInOAuthState.Error(errorDescription ?: error)
            return
        }

        if (code.isNullOrEmpty() || state.isNullOrEmpty()) {
            _linkedInOAuthState.value = LinkedInOAuthState.Error("Missing authorization code or state token.")
            return
        }

        viewModelScope.launch {
            _linkedInOAuthState.value = LinkedInOAuthState.ExchangingToken()
            val result = LinkedInOAuthService.exchangeCodeAndFetchProfile(code, state)
            result.onSuccess { profileData ->
                _linkedInOAuthState.value = LinkedInOAuthState.Success(profileData)
                _linkedInImportPreview.value = profileData
            }.onFailure { ex ->
                _linkedInOAuthState.value = LinkedInOAuthState.Error(ex.message ?: "Authentication failed.")
            }
        }
    }

    /**
     * Connects with Sandbox Verified LinkedIn profile for instant testing and demonstration.
     */
    fun connectLinkedInSandbox(
        customName: String = "Sarah Jenkins",
        customHeadline: String = "Lead Cloud Architect & AI Specialist | Open to UK Skilled Worker / EU Blue Card Sponsorship",
        customEmail: String = "sarah.jenkins.verified@example.com"
    ) {
        viewModelScope.launch {
            _linkedInOAuthState.value = LinkedInOAuthState.FetchingProfile("Simulating OAuth2 PKCE handshake with LinkedIn...")
            kotlinx.coroutines.delay(800) // Realistic UX handshake animation
            val profile = LinkedInOAuthService.createMockVerifiedProfile(
                customName = customName,
                customHeadline = customHeadline,
                customEmail = customEmail
            )
            _linkedInOAuthState.value = LinkedInOAuthState.Success(profile)
            _linkedInImportPreview.value = profile
        }
    }

    /**
     * Applies imported LinkedIn data to the user profile in Room database.
     */
    fun applyLinkedInImport(data: LinkedInProfileData, autoImportToProfile: Boolean = true) {
        viewModelScope.launch {
            repository.linkLinkedInProfile(data, autoImportToProfile)
            _linkedInOAuthState.value = LinkedInOAuthState.Idle
            _linkedInImportPreview.value = null
        }
    }

    /**
     * Unlinks LinkedIn account and revokes verification badge.
     */
    fun unlinkLinkedInAccount() {
        viewModelScope.launch {
            _isUnlinkingLinkedIn.value = true
            repository.unlinkLinkedInProfile()
            _linkedInOAuthState.value = LinkedInOAuthState.Idle
            _linkedInImportPreview.value = null
            _isUnlinkingLinkedIn.value = false
        }
    }

    fun dismissLinkedInDialog() {
        _linkedInOAuthState.value = LinkedInOAuthState.Idle
        _linkedInImportPreview.value = null
    }
}


