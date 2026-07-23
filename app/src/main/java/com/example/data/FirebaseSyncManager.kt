package com.example.data

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

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
