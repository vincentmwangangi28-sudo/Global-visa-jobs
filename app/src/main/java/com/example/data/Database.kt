package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey val id: String,
    val title: String,
    val company: String,
    val country: String,
    val location: String,
    val description: String,
    val salary: String,
    val visaType: String,
    val confidenceScore: Int,
    val confidenceReason: String,
    val relocationAssistance: Boolean,
    val contractType: String, // Full-time, Part-time, Contract, Seasonal, etc.
    val industry: String,
    val experienceLevel: String, // Entry, Mid, Senior
    val applicationUrl: String,
    val datePosted: String,
    val isBookmarked: Boolean = false,
    val isFraud: Boolean = false,
    val isCustomPosted: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1, // Single-user profile
    val fullName: String = "",
    val nationality: String = "",
    val currentCountry: String = "",
    val passportCountry: String = "",
    val education: String = "",
    val skills: String = "",
    val languages: String = "",
    val experience: String = "",
    val desiredCountries: String = "",
    val preferredOccupations: String = "",
    val salaryExpectations: String = "",
    val resumeText: String = "",
    val coverLetterText: String = "",
    // LinkedIn OAuth2 & Professional Verification fields
    val linkedInConnected: Boolean = false,
    val linkedInMemberId: String = "",
    val linkedInEmail: String = "",
    val linkedInEmailVerified: Boolean = false,
    val linkedInHeadline: String = "",
    val linkedInProfilePicture: String = "",
    val linkedInVerifiedAt: Long = 0L,
    val linkedInVerificationHash: String = "",
    val linkedInTrustScore: Int = 0,
    val linkedInConnectionsCount: String = "",
    val linkedInIndustry: String = ""
)

@Entity(tableName = "custom_alerts")
data class CustomAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val queryText: String,
    val country: String,
    val isEmailAlert: Boolean = true,
    val isPushAlert: Boolean = true,
    val isTelegramAlert: Boolean = false
)

@Entity(tableName = "visa_applications")
data class VisaApplicationEntity(
    @PrimaryKey val jobId: String,
    val jobTitle: String,
    val company: String,
    val country: String,
    val status: String = "Applied", // "Applied", "Interviewing", "Offer Received", "Sponsorship Approved", "Visa Filed", "Visa Approved"
    val notes: String = "",
    val updatedDate: String = ""
)

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY datePosted DESC")
    fun getAllJobsFlow(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE id = :id")
    suspend fun getJobById(id: String): JobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<JobEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity)

    @Update
    suspend fun updateJob(job: JobEntity)

    @Query("DELETE FROM jobs WHERE id = :id")
    suspend fun deleteJobById(id: String)

    @Query("DELETE FROM jobs WHERE isCustomPosted = 0")
    suspend fun clearScrapedJobs()

    // Profile queries
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)

    // Alerts queries
    @Query("SELECT * FROM custom_alerts ORDER BY id DESC")
    fun getAllAlertsFlow(): Flow<List<CustomAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: CustomAlertEntity)

    @Query("DELETE FROM custom_alerts WHERE id = :id")
    suspend fun deleteAlertById(id: Int)

    // Visa Tracking queries
    @Query("SELECT * FROM visa_applications ORDER BY updatedDate DESC")
    fun getAllVisaApplicationsFlow(): Flow<List<VisaApplicationEntity>>

    @Query("SELECT * FROM visa_applications WHERE jobId = :jobId")
    suspend fun getVisaApplicationById(jobId: String): VisaApplicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisaApplication(application: VisaApplicationEntity)

    @Query("DELETE FROM visa_applications WHERE jobId = :jobId")
    suspend fun deleteVisaApplicationById(jobId: String)

    // Relocation Checklist queries
    @Query("SELECT * FROM relocation_tasks ORDER BY id ASC")
    fun getAllRelocationTasksFlow(): Flow<List<RelocationTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelocationTask(task: RelocationTaskEntity)

    @Update
    suspend fun updateRelocationTask(task: RelocationTaskEntity)

    @Query("DELETE FROM relocation_tasks WHERE id = :id")
    suspend fun deleteRelocationTaskById(id: Int)

    @Query("DELETE FROM relocation_tasks")
    suspend fun clearAllRelocationTasks()

    // Notification queries
    @Query("SELECT * FROM job_notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<JobNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: JobNotificationEntity)

    @Query("UPDATE job_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Int)

    @Query("DELETE FROM job_notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: Int)

    @Query("DELETE FROM job_notifications")
    suspend fun clearAllNotifications()
}

@Entity(tableName = "relocation_tasks")
data class RelocationTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskName: String,
    val category: String, // e.g. "Visa Documents", "Housing", "Health Insurance", "Other"
    val country: String, // e.g. "Canada", "United Kingdom", "Australia", "Germany", "Sweden", "General"
    val isCompleted: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "job_notifications")
data class JobNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val jobId: String,
    val isRead: Boolean = false,
    val isEmail: Boolean = false,
    val emailContent: String = "",
    val isPush: Boolean = false
)

@Database(entities = [JobEntity::class, UserProfileEntity::class, CustomAlertEntity::class, VisaApplicationEntity::class, RelocationTaskEntity::class, JobNotificationEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao
}
