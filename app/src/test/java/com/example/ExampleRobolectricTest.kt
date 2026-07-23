package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.network.RapidApiClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Global Visa Jobs", appName)
  }

  @Test
  fun testLinkedInJobCount_simulationFallback() = runBlocking {
    val result = RapidApiClient.getActiveJobsCount(
      title = "Data Engineer",
      location = "\"United States\" OR \"United Kingdom\""
    )
    assertNotNull(result)
    assertEquals("Data Engineer", result.title)
    assertTrue(result.count > 0)
  }

  @Test
  fun testIndeedCompanyJobs_simulationFallback() = runBlocking {
    val jobs = RapidApiClient.getIndeedCompanyJobs(
      companyName = "Ubisoft",
      locality = "us"
    )
    assertNotNull(jobs)
    assertTrue(jobs.isNotEmpty())
    val firstJob = jobs.first()
    assertEquals("Ubisoft", firstJob.company)
    assertTrue(firstJob.title.isNotEmpty())
    assertEquals("United States", firstJob.country)
  }

  @Test
  fun testRelocationTaskPrepopulation() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = androidx.room.Room.inMemoryDatabaseBuilder(context, com.example.data.AppDatabase::class.java).build()
    val dao = db.jobDao()
    
    val repository = com.example.data.JobRepository(db, context)
    repository.prePopulateRelocationTasks("Germany")
    
    val tasks = dao.getAllRelocationTasksFlow().first()
    assertTrue(tasks.isNotEmpty())
    
    val visaDocsTasks = tasks.filter { it.category == "Visa Documents" }
    val housingTasks = tasks.filter { it.category == "Housing" }
    val healthTasks = tasks.filter { it.category == "Health Insurance" }
    
    assertTrue(visaDocsTasks.isNotEmpty())
    assertTrue(housingTasks.isNotEmpty())
    assertTrue(healthTasks.isNotEmpty())
    
    assertEquals("Germany", visaDocsTasks.first().country)
    db.close()
  }

  @Test
  fun testRealTimeJobMatchingAndNotification() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = androidx.room.Room.inMemoryDatabaseBuilder(context, com.example.data.AppDatabase::class.java).build()
    val dao = db.jobDao()
    val repository = com.example.data.JobRepository(db, context)

    // Save a custom user profile with specific skills and desired countries
    val profile = com.example.data.UserProfileEntity(
      id = 1,
      fullName = "Vincent Mwangangi",
      skills = "Kotlin, Android, Compose",
      desiredCountries = "Germany, Canada"
    )
    dao.insertProfile(profile)

    // Simulate incoming jobs - one matching skills/country, one not matching
    val matchingJob = com.example.data.JobEntity(
      id = "test_job_match_101",
      title = "Senior Android Developer (Kotlin/Compose)",
      company = "TechCorp Munich",
      location = "Munich",
      country = "Germany",
      visaType = "EU Blue Card",
      salary = "€85,000",
      description = "We are seeking an Android engineer experienced in Kotlin, Jetpack Compose and modern UI architectures.",
      confidenceScore = 95,
      confidenceReason = "Verified Match",
      relocationAssistance = true,
      contractType = "Full-time",
      industry = "Tech",
      experienceLevel = "Senior",
      applicationUrl = "https://example.com/apply-munich",
      datePosted = "2026-07-04"
    )

    val nonMatchingJob = com.example.data.JobEntity(
      id = "test_job_no_match_102",
      title = "Frontend Engineer (React)",
      company = "WebCorp London",
      location = "London",
      country = "United Kingdom",
      visaType = "Skilled Worker Visa",
      salary = "£65,000",
      description = "Looking for a React JS developer with CSS skills.",
      confidenceScore = 80,
      confidenceReason = "Sponsor Likely",
      relocationAssistance = false,
      contractType = "Full-time",
      industry = "Tech",
      experienceLevel = "Mid",
      applicationUrl = "https://example.com/apply-london",
      datePosted = "2026-07-04"
    )

    // Run matching checks
    repository.checkAndTriggerNotifications(listOf(matchingJob, nonMatchingJob))

    // Retrieve triggered notifications
    val notifications = dao.getAllNotificationsFlow().first()
    assertEquals(1, notifications.size)

    val matchingNotification = notifications.first()
    assertTrue(matchingNotification.title.contains("Profile Match"))
    assertTrue(matchingNotification.message.contains("TechCorp Munich"))
    assertEquals("test_job_match_101", matchingNotification.jobId)
    assertTrue(matchingNotification.isEmail)
    assertTrue(matchingNotification.isPush)
    assertTrue(matchingNotification.emailContent.contains("TechCorp Munich"))

    db.close()
  }
}
