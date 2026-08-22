package com.example

import com.example.network.GeminiApiClient
import com.example.network.RapidApiClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testScamVerification_noFlags() {
    val result = GeminiApiClient.performLocalJobVerification(
      title = "Senior Software Engineer",
      company = "Stripe",
      description = "We are seeking a senior engineer to join our team. Excellent skills in Kotlin and Jetpack Compose required. Apply via Stripe corporate site."
    )
    assertEquals(12, result.riskScore)
    assertEquals("Low Risk", result.riskLevel)
    assertTrue(result.redFlags.isEmpty())
  }

  @Test
  fun testScamVerification_withUpfrontFees() {
    val result = GeminiApiClient.performLocalJobVerification(
      title = "Data Entry Operator",
      company = "Global Visa Services",
      description = "Easy work from home. High salary. Requires $50 upfront registration deposit to verify bank account details before starting."
    )
    assertTrue(result.riskScore >= 45)
    assertTrue(result.redFlags.any { it.contains("Upfront Fees") })
  }

  @Test
  fun testScamVerification_withMultipleFlags() {
    val result = GeminiApiClient.performLocalJobVerification(
      title = "Logistics Assistant",
      company = "Apex Transports Ltd",
      description = "Earn $4000/week with no experience! Instant visa guaranteed. Recruitment is done chat-only via Telegram. Please send passport scans to recruitment.apex@gmail.com."
    )
    assertTrue(result.riskScore >= 95)
    assertEquals("High Risk", result.riskLevel)
    assertTrue(result.redFlags.size >= 3)
  }

  @Test
  fun testDocumentExpiryStatus_expiredDate() {
    val (status, text) = com.example.ui.calculateExpiryStatus("2020-01-01")
    assertEquals(com.example.ui.DocumentExpiryStatus.EXPIRED, status)
    assertTrue(text.contains("Expired"))
  }

  @Test
  fun testDocumentExpiryStatus_futureDate() {
    val (status, text) = com.example.ui.calculateExpiryStatus("2035-12-31")
    assertEquals(com.example.ui.DocumentExpiryStatus.VALID, status)
    assertTrue(text.contains("Valid"))
  }

  @Test
  fun testDestinationTaxModels_exist() {
    assertTrue(com.example.ui.destinationTaxModels.isNotEmpty())
    val uk = com.example.ui.destinationTaxModels.find { it.country == "United Kingdom" }
    assertNotNull(uk)
    assertEquals("£", uk?.currencySymbol)
    assertEquals("GBP", uk?.currencyCode)
  }

  @Test
  fun testFirebaseAuthUser_modelValidation() {
    val user = com.example.auth.AppUser(
      uid = "usr_test_123",
      email = "vincentmwangangi28@gmail.com",
      displayName = "Vincent Mwangangi",
      photoUrl = "https://lh3.googleusercontent.com/a/test",
      isGoogleLinked = true
    )
    assertEquals("usr_test_123", user.uid)
    assertEquals("vincentmwangangi28@gmail.com", user.email)
    assertEquals("Vincent Mwangangi", user.displayName)
    assertTrue(user.isGoogleLinked)
  }

  @Test
  fun testFirestoreJobAlert_modelCreation() {
    val alert = com.example.data.FirestoreJobAlert(
      id = "alert_abc_1",
      queryText = "Software Engineer",
      country = "Canada",
      userEmail = "vincentmwangangi28@gmail.com",
      isActive = true
    )
    assertEquals("alert_abc_1", alert.id)
    assertEquals("Software Engineer", alert.queryText)
    assertEquals("Canada", alert.country)
    assertTrue(alert.isActive)
  }

  @Test
  fun testSponsorRegistry_officialSponsorsLoaded() {
    val sponsors = com.example.ui.SponsorRegistryRepository.officialSponsors
    assertTrue(sponsors.isNotEmpty())
    val deepmind = sponsors.find { it.name.contains("DeepMind") }
    assertNotNull(deepmind)
    assertEquals("United Kingdom", deepmind?.country)
    assertTrue(deepmind?.approvalRatePercent ?: 0.0 >= 99.0)
    assertEquals(com.example.ui.SponsorLicenseStatus.PREMIUM_FAST_TRACK, deepmind?.licenseStatus)
  }

  @Test
  fun testSpouseDependant_pathwaysLoaded() {
    val pathways = com.example.ui.SpouseDependantRepository.pathways
    assertTrue(pathways.isNotEmpty())
    val uk = pathways.find { it.country == "United Kingdom" }
    assertNotNull(uk)
    assertTrue(uk?.spouseWorkRights?.contains("Unrestricted") == true)
    val germany = pathways.find { it.country == "Germany" }
    assertNotNull(germany)
    assertTrue(germany?.childrenEducationRights?.contains("Kindergeld") == true)
  }

  @Test
  fun testConsularQuestionBank_questionsExist() {
    val questions = com.example.ui.ConsularQuestionBank.sampleQuestions
    assertTrue(questions.isNotEmpty())
    assertTrue(questions.any { it.visaPathway.contains("Skilled Worker") })
    assertTrue(questions.any { it.visaPathway.contains("H-1B") })
  }

  @Test
  fun testEmployerReviews_repository() {
    val reviews = com.example.ui.EmployerReviewsRepository.sampleReviews
    assertTrue(reviews.isNotEmpty())
    val deepmindReview = reviews.find { it.companyName.contains("DeepMind") }
    assertNotNull(deepmindReview)
    assertEquals(4.9, deepmindReview?.overallImmigrationRating ?: 0.0, 0.01)
    assertEquals(7, deepmindReview?.cosOrLmiaSpeedDays)
  }

  @Test
  fun testTaxAndRemittance_treaties() {
    val treaties = com.example.ui.TaxRemittanceRepository.sampleTreaties
    assertTrue(treaties.isNotEmpty())
    val kenyaUk = treaties.find { it.originCountry == "Kenya" && it.destinationCountry == "United Kingdom" }
    assertNotNull(kenyaUk)
    assertTrue(kenyaUk?.hasDtaaTreaty == true)
  }

  @Test
  fun testRealTimeMobility_pulsesLoaded() {
    val pulses = com.example.ui.RealTimeMobilityRepository.initialPulses
    assertTrue(pulses.isNotEmpty())
    val deepmindPulse = pulses.find { it.companyName.contains("DeepMind") }
    assertNotNull(deepmindPulse)
    assertTrue(deepmindPulse?.isFastTrack == true)
    assertEquals("United Kingdom", deepmindPulse?.country)
  }

  @Test
  fun testRealTimeMobility_embassySlots() {
    val slots = com.example.ui.RealTimeMobilityRepository.sampleEmbassySlots
    assertTrue(slots.isNotEmpty())
    val nbo = slots.find { it.id.contains("nbo") }
    assertNotNull(nbo)
    assertEquals(com.example.ui.EmbassySlotStatus.SLOTS_OPEN_NOW, nbo?.status)
  }

  @Test
  fun testRealTimeMobility_fxRates() {
    val fx = com.example.ui.RealTimeMobilityRepository.sampleFxRates
    assertTrue(fx.isNotEmpty())
    val gbpKes = fx.find { it.currencyPair == "GBP/KES" }
    assertNotNull(gbpKes)
    assertTrue(gbpKes?.rate ?: 0.0 > 150.0)
  }

  @Test
  fun testRealTimeMobility_communityChat() {
    val chat = com.example.ui.RealTimeMobilityRepository.sampleCommunityChat
    assertTrue(chat.isNotEmpty())
    assertTrue(chat.any { it.isAiAdvisor })
  }
}

