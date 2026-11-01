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

  @Test
  fun testDeriveJobPlatformMetadata_localKenyaCountyJob() {
    val countyJob = com.example.data.JobEntity(
      id = "kenya_nairobi_nurse",
      title = "Registered Critical Care Nurse",
      company = "Nairobi County Government",
      location = "Nairobi",
      country = "Kenya",
      visaType = "Local County Career",
      salary = "KSh 95,000 - 140,000",
      description = "Critical care nursing position at Mama Lucy Kibaki Hospital.",
      confidenceScore = 96,
      confidenceReason = "Verified Public Service",
      relocationAssistance = false,
      contractType = "Permanent & Pensionable",
      industry = "Healthcare",
      experienceLevel = "Mid",
      applicationUrl = "https://nairobi.go.ke/careers",
      datePosted = "2026-07-04"
    )

    val meta = com.example.ui.deriveJobPlatformMetadata(countyJob)
    assertTrue(meta.relocatePerks.any { it.contains("Medical") })
    assertTrue(meta.relocateAllowanceUsd.contains("Local Statutory"))
    assertTrue(meta.myVisaJobsApprovalRate.contains("Direct Verified Employer"))
    assertEquals("KRA / KNOCS Verified", meta.occupationCode)
    assertTrue(meta.ottaWhyApply.contains("East African employer"))
    assertTrue(meta.brighterMondayBadge.contains("BrighterMonday Recommended"))
  }

  @Test
  fun testDeriveJobPlatformMetadata_globalVisaSponsorshipJob() {
    val ukJob = com.example.data.JobEntity(
      id = "uk_nhs_nurse",
      title = "Staff Nurse - Emergency Dept (Band 5/6)",
      company = "NHS Foundation Trust",
      location = "Manchester",
      country = "United Kingdom",
      visaType = "Health and Care Worker Visa",
      salary = "£34,000 - £42,000",
      description = "Full NHS Tier 2 / Health & Care Certificate of Sponsorship provided.",
      confidenceScore = 98,
      confidenceReason = "Home Office A-Rated Sponsor",
      relocationAssistance = true,
      contractType = "Full-time",
      industry = "Healthcare",
      experienceLevel = "Mid",
      applicationUrl = "https://jobs.nhs.uk",
      datePosted = "2026-07-04"
    )

    val meta = com.example.ui.deriveJobPlatformMetadata(ukJob)
    assertTrue(meta.relocatePerks.any { it.contains("Flights Covered") })
    assertTrue(meta.relocatePerks.any { it.contains("Housing") })
    assertTrue(meta.relocateAllowanceUsd.contains("$8,500 Relocation Package"))
    assertTrue(meta.myVisaJobsApprovalRate.contains("98.2% Gov Petition Approval"))
    assertEquals("UK SOC 2020 Eligible", meta.occupationCode)
    assertTrue(meta.ottaWhyApply.contains("priority government healthcare track", ignoreCase = true))
    assertEquals("VanHack / Relocate.me Verified Sponsor", meta.brighterMondayBadge)
  }

  @Test
  fun testTopRankingSeoKeywordsAndTop5PlatformsIntegrity() {
    val keywords = com.example.ui.topRankingSeoKeywords
    assertTrue("Keywords list must have high volume entries", keywords.size >= 10)
    assertTrue(keywords.any { it.keyword.contains("UK Skilled Worker") })
    assertTrue(keywords.any { it.keyword.contains("Canada LMIA") })
    assertTrue(keywords.any { it.keyword.contains("Germany EU Blue Card") })
    assertTrue(keywords.any { it.keyword.contains("Nairobi County Jobs") })

    val platforms = com.example.ui.topFiveJobPlatforms
    assertEquals(5, platforms.size)
    val ids = platforms.map { it.id }
    assertTrue(ids.contains("relocate_me"))
    assertTrue(ids.contains("vanhack"))
    assertTrue(ids.contains("myvisajobs"))
    assertTrue(ids.contains("brightermonday"))
    assertTrue(ids.contains("otta"))
  }

  @Test
  fun testAhrefs12OpportunitiesIntegrity() {
    val items = com.example.ui.defaultAhrefsOpportunities
    assertEquals(12, items.size)

    val contentItems = items.filter { it.category == com.example.ui.SeoOpportunityCategory.CONTENT }
    val linkItems = items.filter { it.category == com.example.ui.SeoOpportunityCategory.LINKS }
    val techItems = items.filter { it.category == com.example.ui.SeoOpportunityCategory.TECHNICAL }

    assertEquals(6, contentItems.size)
    assertEquals(5, linkItems.size)
    assertEquals(1, techItems.size)

    // Verify key titles from the Ahrefs Site Explorer audit
    val titles = items.map { it.title }
    assertTrue(titles.any { it.contains("Low-hanging fruit keywords") })
    assertTrue(titles.any { it.contains("AI Overviews and featured snippets") })
    assertTrue(titles.any { it.contains("Top suggestions from Content gap") })
    assertTrue(titles.any { it.contains("Content with declining traffic") })
    assertTrue(titles.any { it.contains("Pages only published once") })
    assertTrue(titles.any { it.contains("Potential cannibalization") })
    assertTrue(titles.any { it.contains("Redirects to implement") })
    assertTrue(titles.any { it.contains("Internal link opportunities") })
    assertTrue(titles.any { it.contains("Top links from Link intersect") })
    assertTrue(titles.any { it.contains("Unlinked brand mentions") })
    assertTrue(titles.any { it.contains("Descriptive anchors") })
    assertTrue(titles.any { it.contains("Critical issues") })

    // Verify target URLs point to globalvisajobs.website
    assertTrue(items.all { it.targetUrl.contains("globalvisajobs.website") || it.targetUrl.contains("Reddit") || it.targetUrl.contains("Outreach") })
  }

  @Test
  fun testAhrefsReportMarkdownAndSchemaGenerator() {
    val items = com.example.ui.defaultAhrefsOpportunities
    val report = com.example.ui.buildAuditReportMarkdown(items, 96)
    assertTrue(report.contains("Ahrefs Site Explorer & Site Audit Report"))
    assertTrue(report.contains("globalvisajobs.website"))
    assertTrue(report.contains("96%"))
    assertTrue(report.contains("Content Opportunities"))
    assertTrue(report.contains("Links Opportunities"))
    assertTrue(report.contains("Technical Opportunities"))

    // Verify technical item generated snippet has Schema.org JobPosting
    val techItem = items.first { it.id == "technical_critical_issues" }
    assertTrue(techItem.generatedSnippetOrCode.contains("\"@type\": \"JobPosting\""))
    assertTrue(techItem.generatedSnippetOrCode.contains("\"directApply\": true"))
    assertTrue(techItem.generatedSnippetOrCode.contains("sitemap.xml"))
    assertTrue(techItem.generatedSnippetOrCode.contains("canonical"))

    // Verify AI Overviews direct snippet
    val aiOverviewItem = items.first { it.id == "content_ai_overviews" }
    assertTrue(aiOverviewItem.generatedSnippetOrCode.contains("Certificate of Sponsorship"))
    assertTrue(aiOverviewItem.generatedSnippetOrCode.contains("FAQPage"))
  }
}

