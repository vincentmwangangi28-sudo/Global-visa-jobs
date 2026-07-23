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
}
