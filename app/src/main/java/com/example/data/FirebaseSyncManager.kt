package com.example.data

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
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

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            // Safely check if google-services.json generated resources are available
            val resId = context.resources.getIdentifier("google_app_id", "string", context.packageName)
            if (resId == 0) {
                Log.i("FirebaseSyncManager", "No google-services.json configuration found. Defaulting to safe, high-performance offline local SQLite database mode.")
                isInitialized = false
                return
            }

            // Safe initialization checking for active options
            FirebaseApp.initializeApp(context)
            firestore = FirebaseFirestore.getInstance()
            database = FirebaseDatabase.getInstance()
            isInitialized = true
            Log.d("FirebaseSyncManager", "Firebase successfully initialized for online synchronization.")
        } catch (e: Exception) {
            Log.w("FirebaseSyncManager", "Firebase initialization skipped: ${e.localizedMessage}. Entering safe offline local database mode.")
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

    suspend fun subscribeJobAlertToFirestore(alert: FirestoreJobAlert): Boolean {
        if (!isFirebaseReady()) {
            Log.d("FirebaseSyncManager", "Firebase not ready. Managed alert subscription locally.")
            return false
        }
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
                .set(alertMap)
                .awaitTask()
            Log.d("FirebaseSyncManager", "Firestore synced: Job Alert $alertId subscribed successfully.")
            true
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Firestore job alert sync failed: ${e.message}")
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
            Log.d("FirebaseSyncManager", "Firestore synced: Job Alert $alertId removed.")
            true
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Firestore unsubscribe failed: ${e.message}")
            false
        }
    }

    fun listenToRealtimeJobAlerts(
        userEmail: String,
        onAlertsChanged: (List<FirestoreJobAlert>) -> Unit
    ): ListenerRegistration? {
        if (!isFirebaseReady()) return null
        return try {
            val db = firestore ?: return null
            db.collection("job_alerts")
                .whereEqualTo("userEmail", userEmail)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.w("FirebaseSyncManager", "Listen to job alerts failed", error)
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        val alerts = snapshots.documents.mapNotNull { doc ->
                            try {
                                FirestoreJobAlert(
                                    id = doc.getString("id") ?: doc.id,
                                    queryText = doc.getString("queryText") ?: "",
                                    country = doc.getString("country") ?: "All",
                                    userEmail = doc.getString("userEmail") ?: userEmail,
                                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                    isActive = doc.getBoolean("isActive") ?: true,
                                    pushEnabled = doc.getBoolean("pushEnabled") ?: true,
                                    emailEnabled = doc.getBoolean("emailEnabled") ?: true
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        onAlertsChanged(alerts)
                    }
                }
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Failed to register snapshot listener for alerts", e)
            null
        }
    }

    fun listenToLiveJobsFeed(
        onNewJobReceived: (JobEntity) -> Unit
    ): ListenerRegistration? {
        if (!isFirebaseReady()) return null
        return try {
            val db = firestore ?: return null
            db.collection("live_jobs_feed")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.w("FirebaseSyncManager", "Listen to live jobs feed failed", error)
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        for (dc in snapshots.documentChanges) {
                            if (dc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                val doc = dc.document
                                try {
                                    val job = JobEntity(
                                        id = doc.getString("id") ?: doc.id,
                                        title = doc.getString("title") ?: "",
                                        company = doc.getString("company") ?: "",
                                        country = doc.getString("country") ?: "United Kingdom",
                                        location = doc.getString("location") ?: "",
                                        description = doc.getString("description") ?: "",
                                        salary = doc.getString("salary") ?: "Competitive",
                                        visaType = doc.getString("visaType") ?: "Full Visa Sponsorship",
                                        confidenceScore = doc.getLong("confidenceScore")?.toInt() ?: 95,
                                        confidenceReason = doc.getString("confidenceReason") ?: "Firestore Live Streamed Listing",
                                        relocationAssistance = doc.getBoolean("relocationAssistance") ?: true,
                                        contractType = doc.getString("contractType") ?: "Full-time",
                                        industry = doc.getString("industry") ?: "Technology",
                                        experienceLevel = doc.getString("experienceLevel") ?: "Senior",
                                        applicationUrl = doc.getString("applicationUrl") ?: "https://www.linkedin.com/jobs",
                                        datePosted = doc.getString("datePosted") ?: "Just now"
                                    )
                                    onNewJobReceived(job)
                                } catch (e: Exception) {
                                    Log.w("FirebaseSyncManager", "Parsing realtime live job failed", e)
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Failed to register live jobs feed listener", e)
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
                .set(jobMap)
                .awaitTask()
            true
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Publishing job to live feed failed: ${e.message}")
            false
        }
    }

    suspend fun uploadProfileToFirebase(profile: UserProfileEntity) {
        if (!isFirebaseReady()) {
            Log.d("FirebaseSyncManager", "Firebase not ready. Stored profile in local high-fidelity Room database.")
            return
        }
        try {
            val db = firestore ?: return
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
                "lastSynced" to System.currentTimeMillis()
            )
            db.collection("users").document("candidate_profile")
                .set(profileMap)
                .awaitTask()
            Log.d("FirebaseSyncManager", "Firestore synced: Candidate Profile updated successfully.")
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Firestore profile sync failed: ${e.message}")
        }
    }

    suspend fun saveJobToFirebase(job: JobEntity) {
        if (!isFirebaseReady()) {
            Log.d("FirebaseSyncManager", "Firebase not ready. Saved job ${job.id} locally in Room.")
            return
        }
        try {
            val db = firestore ?: return
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
            db.collection("saved_jobs").document(job.id)
                .set(jobMap)
                .awaitTask()
            Log.d("FirebaseSyncManager", "Firestore synced: Saved Job ${job.id} uploaded successfully.")
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Firestore job sync failed: ${e.message}")
        }
    }

    suspend fun removeJobFromFirebase(jobId: String) {
        if (!isFirebaseReady()) return
        try {
            val db = firestore ?: return
            db.collection("saved_jobs").document(jobId)
                .delete()
                .awaitTask()
            Log.d("FirebaseSyncManager", "Firestore synced: Saved Job $jobId deleted successfully.")
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Firestore job deletion sync failed: ${e.message}")
        }
    }

    suspend fun syncVisaApplicationToFirebase(app: VisaApplicationEntity) {
        if (!isFirebaseReady()) {
            Log.d("FirebaseSyncManager", "Firebase not ready. Logged application status changes locally.")
            return
        }
        try {
            val db = firestore ?: return
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
            db.collection("visa_progress").document(app.jobId)
                .set(appMap)
                .awaitTask()
            Log.d("FirebaseSyncManager", "Firestore synced: Visa application progress updated.")
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Firestore visa progress sync failed: ${e.message}")
        }
    }

    suspend fun deleteVisaApplicationFromFirebase(jobId: String) {
        if (!isFirebaseReady()) return
        try {
            val db = firestore ?: return
            db.collection("visa_progress").document(jobId)
                .delete()
                .awaitTask()
            Log.d("FirebaseSyncManager", "Firestore synced: Visa application record deleted.")
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Firestore visa deletion sync failed: ${e.message}")
        }
    }
}
