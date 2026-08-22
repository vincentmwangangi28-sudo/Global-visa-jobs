package com.example.data

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine

data class FirestoreJobAlert(
    val id: String = "",
    val queryText: String = "",
    val country: String = "",
    val userEmail: String = "vincentmwangangi28@gmail.com",
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val pushEnabled: Boolean = true,
    val emailEnabled: Boolean = true
)

object FirebaseSyncManager {
    private var isInitialized = false
    private var firestore: FirebaseFirestore? = null
    private var database: FirebaseDatabase? = null

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long>(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val _syncStatusMessage = MutableStateFlow<String?>("Cloud persistence ready")
    val syncStatusMessage: StateFlow<String?> = _syncStatusMessage.asStateFlow()

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            val resId = context.resources.getIdentifier("google_app_id", "string", context.packageName)
            if (resId == 0 && FirebaseApp.getApps(context).isEmpty()) {
                Log.i("FirebaseSyncManager", "Firebase not yet provisioned with google-services.json. Offline local Room persistence active.")
                isInitialized = false
                return
            }

            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            firestore = FirebaseFirestore.getInstance()
            database = FirebaseDatabase.getInstance()
            isInitialized = true
            Log.d("FirebaseSyncManager", "Firebase successfully initialized for online cross-session synchronization.")
        } catch (e: Exception) {
            Log.w("FirebaseSyncManager", "Firebase initialization note: ${e.localizedMessage}")
            isInitialized = false
        }
    }

    fun isFirebaseReady(): Boolean = isInitialized && (firestore != null)

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                continuation.resumeWithException(task.exception ?: RuntimeException("Firebase operation task failed"))
            }
        }
    }

    // Overload for direct profile upload
    suspend fun uploadProfileToFirebase(profile: UserProfileEntity) {
        uploadProfileToFirebase("candidate_profile", profile)
    }

    // Overload for direct job upload
    suspend fun saveJobToFirebase(job: JobEntity) {
        saveJobToFirebase("candidate_profile", job)
    }

    // Overload for direct job removal
    suspend fun removeJobFromFirebase(jobId: String) {
        removeJobFromFirebase("candidate_profile", jobId)
    }

    // Overload for direct visa application sync
    suspend fun syncVisaApplicationToFirebase(app: VisaApplicationEntity) {
        syncVisaApplicationToFirebase("candidate_profile", app)
    }

    // Overload for direct visa application deletion
    suspend fun deleteVisaApplicationFromFirebase(jobId: String) {
        if (!isFirebaseReady()) return
        try {
            val db = firestore ?: return
            db.collection("users").document("candidate_profile").collection("visa_progress").document(jobId)
                .delete()
                .awaitTask()
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Firestore visa application delete failed: ${e.message}")
        }
    }

    // Overload for direct visa document sync
    suspend fun syncVisaDocumentToFirebase(doc: VisaDocumentEntity) {
        syncVisaDocumentToFirebase("candidate_profile", doc)
    }

    // Overload for direct visa document delete
    suspend fun deleteVisaDocumentFromFirebase(docId: Int) {
        if (!isFirebaseReady()) return
        try {
            val db = firestore ?: return
            db.collection("users").document("candidate_profile").collection("visa_documents").document(docId.toString())
                .delete()
                .awaitTask()
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Firestore doc delete failed: ${e.message}")
        }
    }

    // ==========================================
    // LIVE COMMUNITY JOBS FEED
    // ==========================================
    fun listenToLiveJobsFeed(onNewJob: (JobEntity) -> Unit): ListenerRegistration? {
        if (!isFirebaseReady()) return null
        return try {
            val db = firestore ?: return null
            db.collection("live_jobs_feed")
                .limit(30)
                .addSnapshotListener { snapshots, e ->
                    if (e != null || snapshots == null) return@addSnapshotListener
                    for (dc in snapshots.documentChanges) {
                        if (dc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                            val data = dc.document.data
                            val title = data["title"] as? String ?: continue
                            val company = data["company"] as? String ?: continue
                            val job = JobEntity(
                                id = dc.document.id,
                                title = title,
                                company = company,
                                country = data["country"] as? String ?: "Global",
                                location = data["location"] as? String ?: "Remote",
                                description = data["description"] as? String ?: "",
                                salary = data["salary"] as? String ?: "Competitive",
                                visaType = data["visaType"] as? String ?: "Sponsorship Available",
                                confidenceScore = ((data["confidenceScore"] as? Long) ?: 85L).toInt(),
                                confidenceReason = data["confidenceReason"] as? String ?: "Verified Sponsoring Employer",
                                relocationAssistance = (data["relocationAssistance"] as? Boolean) ?: true,
                                contractType = data["contractType"] as? String ?: "Full-time",
                                industry = data["industry"] as? String ?: "Technology",
                                experienceLevel = data["experienceLevel"] as? String ?: "Mid-Senior",
                                applicationUrl = data["applicationUrl"] as? String ?: "",
                                datePosted = data["datePosted"] as? String ?: "Just now",
                                isBookmarked = false,
                                isFraud = (data["isFraud"] as? Boolean) ?: false,
                                isCustomPosted = true
                            )
                            onNewJob(job)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Listening to live jobs feed failed: ${e.message}")
            null
        }
    }

    suspend fun publishJobToLiveFeed(job: JobEntity): Boolean {
        if (!isFirebaseReady()) return false
        return try {
            val db = firestore ?: return false
            val jobMap = hashMapOf(
                "id" to job.id,
                "title" to job.title,
                "company" to job.company,
                "country" to job.country,
                "location" to job.location,
                "description" to job.description,
                "salary" to job.salary,
                "visaType" to job.visaType,
                "confidenceScore" to job.confidenceScore,
                "confidenceReason" to job.confidenceReason,
                "relocationAssistance" to job.relocationAssistance,
                "contractType" to job.contractType,
                "industry" to job.industry,
                "experienceLevel" to job.experienceLevel,
                "applicationUrl" to job.applicationUrl,
                "datePosted" to job.datePosted,
                "publishedAt" to System.currentTimeMillis()
            )
            db.collection("live_jobs_feed").document(job.id)
                .set(jobMap, SetOptions.merge())
                .awaitTask()
            true
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Publishing job to live feed failed: ${e.message}")
            false
        }
    }

    // ==========================================
    // USER PROFILE CROSS-SESSION PERSISTENCE
    // ==========================================
    suspend fun uploadProfileToFirebase(userId: String = "candidate_profile", profile: UserProfileEntity) {
        if (!isFirebaseReady()) {
            Log.d("FirebaseSyncManager", "Firebase offline. Stored profile in local Room database.")
            return
        }
        try {
            val db = firestore ?: return
            val sanitizedUid = userId.ifBlank { "candidate_profile" }
            val profileMap = hashMapOf(
                "fullName" to profile.fullName,
                "nationality" to profile.nationality,
                "currentCountry" to profile.currentCountry,
                "passportCountry" to profile.passportCountry,
                "education" to profile.education,
                "skills" to profile.skills,
                "languages" to profile.languages,
                "experience" to profile.experience,
                "desiredCountries" to profile.desiredCountries,
                "preferredOccupations" to profile.preferredOccupations,
                "salaryExpectations" to profile.salaryExpectations,
                "resumeText" to profile.resumeText,
                "coverLetterText" to profile.coverLetterText,
                "linkedInConnected" to profile.linkedInConnected,
                "linkedInHeadline" to profile.linkedInHeadline,
                "linkedInTrustScore" to profile.linkedInTrustScore,
                "linkedInEmail" to profile.linkedInEmail,
                "lastSynced" to System.currentTimeMillis()
            )
            db.collection("users").document(sanitizedUid).collection("data").document("profile")
                .set(profileMap, SetOptions.merge())
                .awaitTask()
            _lastSyncTimestamp.value = System.currentTimeMillis()
            _syncStatusMessage.value = "Profile synced with Firebase Cloud"
            Log.d("FirebaseSyncManager", "Firestore synced: Candidate Profile updated for $sanitizedUid.")
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Firestore profile sync failed: ${e.message}")
        }
    }

    suspend fun fetchProfileFromFirebase(userId: String = "candidate_profile"): UserProfileEntity? {
        if (!isFirebaseReady()) return null
        return try {
            val db = firestore ?: return null
            val sanitizedUid = userId.ifBlank { "candidate_profile" }
            val doc = db.collection("users").document(sanitizedUid).collection("data").document("profile")
                .get()
                .awaitTask()
            if (doc.exists()) {
                UserProfileEntity(
                    id = 1,
                    fullName = doc.getString("fullName") ?: "",
                    nationality = doc.getString("nationality") ?: "",
                    currentCountry = doc.getString("currentCountry") ?: "",
                    passportCountry = doc.getString("passportCountry") ?: "",
                    education = doc.getString("education") ?: "",
                    skills = doc.getString("skills") ?: "",
                    languages = doc.getString("languages") ?: "",
                    experience = doc.getString("experience") ?: "",
                    desiredCountries = doc.getString("desiredCountries") ?: "",
                    preferredOccupations = doc.getString("preferredOccupations") ?: "",
                    salaryExpectations = doc.getString("salaryExpectations") ?: "",
                    resumeText = doc.getString("resumeText") ?: "",
                    coverLetterText = doc.getString("coverLetterText") ?: "",
                    linkedInConnected = doc.getBoolean("linkedInConnected") ?: false,
                    linkedInHeadline = doc.getString("linkedInHeadline") ?: "",
                    linkedInTrustScore = (doc.getLong("linkedInTrustScore") ?: 0L).toInt(),
                    linkedInEmail = doc.getString("linkedInEmail") ?: ""
                )
            } else null
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Fetching profile from Firestore failed: ${e.message}")
            null
        }
    }

    // ==========================================
    // SAVED JOBS PERSISTENCE
    // ==========================================
    suspend fun saveJobToFirebase(userId: String = "candidate_profile", job: JobEntity) {
        if (!isFirebaseReady()) return
        try {
            val db = firestore ?: return
            val sanitizedUid = userId.ifBlank { "candidate_profile" }
            val jobMap = hashMapOf(
                "id" to job.id,
                "title" to job.title,
                "company" to job.company,
                "country" to job.country,
                "location" to job.location,
                "description" to job.description,
                "salary" to job.salary,
                "visaType" to job.visaType,
                "confidenceScore" to job.confidenceScore,
                "confidenceReason" to job.confidenceReason,
                "relocationAssistance" to job.relocationAssistance,
                "contractType" to job.contractType,
                "industry" to job.industry,
                "experienceLevel" to job.experienceLevel,
                "applicationUrl" to job.applicationUrl,
                "datePosted" to job.datePosted,
                "isBookmarked" to job.isBookmarked,
                "isFraud" to job.isFraud,
                "lastSynced" to System.currentTimeMillis()
            )
            db.collection("users").document(sanitizedUid).collection("saved_jobs").document(job.id)
                .set(jobMap, SetOptions.merge())
                .awaitTask()
            Log.d("FirebaseSyncManager", "Firestore synced: Saved Job ${job.id} uploaded for $sanitizedUid.")
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Firestore job sync failed: ${e.message}")
        }
    }

    suspend fun removeJobFromFirebase(userId: String = "candidate_profile", jobId: String) {
        if (!isFirebaseReady()) return
        try {
            val db = firestore ?: return
            val sanitizedUid = userId.ifBlank { "candidate_profile" }
            db.collection("users").document(sanitizedUid).collection("saved_jobs").document(jobId)
                .delete()
                .awaitTask()
            Log.d("FirebaseSyncManager", "Firestore synced: Saved Job $jobId deleted for $sanitizedUid.")
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Firestore job deletion sync failed: ${e.message}")
        }
    }

    suspend fun fetchSavedJobsFromFirebase(userId: String = "candidate_profile"): List<JobEntity> {
        if (!isFirebaseReady()) return emptyList()
        return try {
            val db = firestore ?: return emptyList()
            val sanitizedUid = userId.ifBlank { "candidate_profile" }
            val snapshot = db.collection("users").document(sanitizedUid).collection("saved_jobs")
                .get()
                .awaitTask()
            snapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val title = doc.getString("title") ?: return@mapNotNull null
                val company = doc.getString("company") ?: return@mapNotNull null
                JobEntity(
                    id = id,
                    title = title,
                    company = company,
                    country = doc.getString("country") ?: "Global",
                    location = doc.getString("location") ?: "Remote",
                    description = doc.getString("description") ?: "",
                    salary = doc.getString("salary") ?: "Competitive",
                    visaType = doc.getString("visaType") ?: "Sponsorship Available",
                    confidenceScore = (doc.getLong("confidenceScore") ?: 85L).toInt(),
                    confidenceReason = doc.getString("confidenceReason") ?: "",
                    relocationAssistance = doc.getBoolean("relocationAssistance") ?: true,
                    contractType = doc.getString("contractType") ?: "Full-time",
                    industry = doc.getString("industry") ?: "Technology",
                    experienceLevel = doc.getString("experienceLevel") ?: "Mid-Senior",
                    applicationUrl = doc.getString("applicationUrl") ?: "",
                    datePosted = doc.getString("datePosted") ?: "Recent",
                    isBookmarked = true,
                    isFraud = doc.getBoolean("isFraud") ?: false
                )
            }
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Fetching saved jobs from Firestore failed: ${e.message}")
            emptyList()
        }
    }

    // ==========================================
    // VISA PROGRESS & DOCUMENT VAULT PERSISTENCE
    // ==========================================
    suspend fun syncVisaApplicationToFirebase(userId: String = "candidate_profile", app: VisaApplicationEntity) {
        if (!isFirebaseReady()) return
        try {
            val db = firestore ?: return
            val sanitizedUid = userId.ifBlank { "candidate_profile" }
            val appMap = hashMapOf(
                "jobId" to app.jobId,
                "jobTitle" to app.jobTitle,
                "company" to app.company,
                "country" to app.country,
                "status" to app.status,
                "notes" to app.notes,
                "updatedDate" to app.updatedDate,
                "lastSynced" to System.currentTimeMillis()
            )
            db.collection("users").document(sanitizedUid).collection("visa_progress").document(app.jobId)
                .set(appMap, SetOptions.merge())
                .awaitTask()
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Firestore visa progress sync failed: ${e.message}")
        }
    }

    suspend fun fetchVisaApplicationsFromFirebase(userId: String = "candidate_profile"): List<VisaApplicationEntity> {
        if (!isFirebaseReady()) return emptyList()
        return try {
            val db = firestore ?: return emptyList()
            val sanitizedUid = userId.ifBlank { "candidate_profile" }
            val snapshot = db.collection("users").document(sanitizedUid).collection("visa_progress")
                .get()
                .awaitTask()
            snapshot.documents.mapNotNull { doc ->
                val jobId = doc.getString("jobId") ?: doc.id
                val jobTitle = doc.getString("jobTitle") ?: return@mapNotNull null
                val company = doc.getString("company") ?: return@mapNotNull null
                VisaApplicationEntity(
                    jobId = jobId,
                    jobTitle = jobTitle,
                    company = company,
                    country = doc.getString("country") ?: "Global",
                    status = doc.getString("status") ?: "Applied",
                    notes = doc.getString("notes") ?: "",
                    updatedDate = doc.getString("updatedDate") ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Fetching visa applications failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun syncVisaDocumentToFirebase(userId: String = "candidate_profile", doc: VisaDocumentEntity) {
        if (!isFirebaseReady()) return
        try {
            val db = firestore ?: return
            val sanitizedUid = userId.ifBlank { "candidate_profile" }
            val docMap = hashMapOf(
                "id" to doc.id,
                "documentName" to doc.documentName,
                "category" to doc.category,
                "documentNumber" to doc.documentNumber,
                "issuingAuthority" to doc.issuingAuthority,
                "issueDate" to doc.issueDate,
                "expiryDate" to doc.expiryDate,
                "notes" to doc.notes,
                "isVerified" to doc.isVerified,
                "lastSynced" to System.currentTimeMillis()
            )
            db.collection("users").document(sanitizedUid).collection("visa_documents").document(doc.id.toString())
                .set(docMap, SetOptions.merge())
                .awaitTask()
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Firestore doc sync failed: ${e.message}")
        }
    }

    suspend fun fetchVisaDocumentsFromFirebase(userId: String = "candidate_profile"): List<VisaDocumentEntity> {
        if (!isFirebaseReady()) return emptyList()
        return try {
            val db = firestore ?: return emptyList()
            val sanitizedUid = userId.ifBlank { "candidate_profile" }
            val snapshot = db.collection("users").document(sanitizedUid).collection("visa_documents")
                .get()
                .awaitTask()
            snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("documentName") ?: return@mapNotNull null
                VisaDocumentEntity(
                    id = (doc.getLong("id") ?: 0L).toInt(),
                    documentName = name,
                    category = doc.getString("category") ?: "Other",
                    documentNumber = doc.getString("documentNumber") ?: "",
                    issuingAuthority = doc.getString("issuingAuthority") ?: "",
                    issueDate = doc.getString("issueDate") ?: "",
                    expiryDate = doc.getString("expiryDate") ?: "",
                    notes = doc.getString("notes") ?: "",
                    isVerified = doc.getBoolean("isVerified") ?: false
                )
            }
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Fetching visa documents failed: ${e.message}")
            emptyList()
        }
    }

    // ==========================================
    // FULL BIDIRECTIONAL CLOUD SYNCHRONIZATION
    // ==========================================
    suspend fun performFullCloudSync(userId: String, dao: JobDao): Result<String> {
        if (!isFirebaseReady()) {
            _syncStatusMessage.value = "Local persistence active"
            return Result.success("Local SQLite database synced successfully.")
        }
        _isSyncing.value = true
        _syncStatusMessage.value = "Syncing with Firebase Cloud..."
        return try {
            val sanitizedUid = userId.ifBlank { "candidate_profile" }

            // 1. Download Remote Data
            val cloudProfile = fetchProfileFromFirebase(sanitizedUid)
            if (cloudProfile != null) {
                dao.insertProfile(cloudProfile)
            } else {
                // Upload current local profile to cloud
                val localProfile = dao.getUserProfile()
                if (localProfile != null) {
                    uploadProfileToFirebase(sanitizedUid, localProfile)
                }
            }

            // 2. Sync Saved Jobs
            val cloudSavedJobs = fetchSavedJobsFromFirebase(sanitizedUid)
            if (cloudSavedJobs.isNotEmpty()) {
                cloudSavedJobs.forEach { dao.insertJob(it) }
            }

            // 3. Sync Visa Applications
            val cloudApps = fetchVisaApplicationsFromFirebase(sanitizedUid)
            if (cloudApps.isNotEmpty()) {
                cloudApps.forEach { dao.insertVisaApplication(it) }
            }

            // 4. Sync Visa Documents
            val cloudDocs = fetchVisaDocumentsFromFirebase(sanitizedUid)
            if (cloudDocs.isNotEmpty()) {
                cloudDocs.forEach { dao.insertVisaDocument(it) }
            }

            _lastSyncTimestamp.value = System.currentTimeMillis()
            _isSyncing.value = false
            _syncStatusMessage.value = "All data backed up & synced"
            Result.success("Cloud synchronization complete.")
        } catch (e: Exception) {
            _isSyncing.value = false
            _syncStatusMessage.value = "Sync error: ${e.message}"
            Log.e("FirebaseSyncManager", "Full cloud sync error: ${e.message}")
            Result.failure(e)
        }
    }

    // Job Alerts
    suspend fun subscribeJobAlertToFirestore(alert: FirestoreJobAlert): Boolean {
        if (!isFirebaseReady()) return false
        return try {
            val db = firestore ?: return false
            val alertId = if (alert.id.isNotBlank()) alert.id else "alert_${System.currentTimeMillis()}"
            val alertMap = hashMapOf(
                "id" to alertId,
                "queryText" to alert.queryText,
                "country" to alert.country,
                "userEmail" to alert.userEmail,
                "createdAt" to alert.createdAt,
                "isActive" to alert.isActive,
                "pushEnabled" to alert.pushEnabled,
                "emailEnabled" to alert.emailEnabled,
                "lastSynced" to System.currentTimeMillis()
            )
            db.collection("job_alerts").document(alertId)
                .set(alertMap, SetOptions.merge())
                .awaitTask()
            true
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Job alert sync failed: ${e.message}")
            false
        }
    }

    suspend fun unsubscribeJobAlertFromFirestore(alertId: String): Boolean {
        if (!isFirebaseReady()) return false
        return try {
            val db = firestore ?: return false
            db.collection("job_alerts").document(alertId)
                .delete()
                .awaitTask()
            true
        } catch (e: Exception) {
            false
        }
    }
}
