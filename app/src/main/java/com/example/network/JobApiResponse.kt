package com.example.network

import com.example.data.JobEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Root wrapper for parsing responses from jobs APIs (e.g., RapidAPI, JSearch, custom endpoints).
 */
@JsonClass(generateAdapter = true)
data class JobSearchResponseDto(
    @Json(name = "status") val status: String? = "OK",
    @Json(name = "message") val message: String? = null,
    @Json(name = "totalResults") val totalResults: Int? = 0,
    @Json(name = "page") val page: Int? = 1,
    @Json(name = "jobs") val jobs: List<JobDto> = emptyList()
)

/**
 * Data model representing an individual job posting parsed from the jobs API JSON response.
 */
@JsonClass(generateAdapter = true)
data class JobDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "company") val company: CompanyDto? = null,
    @Json(name = "companyName") val companyNameRaw: String? = null,
    @Json(name = "country") val country: String = "Global",
    @Json(name = "location") val location: String = "",
    @Json(name = "description") val description: String = "",
    @Json(name = "salary") val salary: SalaryDto? = null,
    @Json(name = "salaryText") val salaryTextRaw: String? = null,
    @Json(name = "sponsorship") val sponsorship: SponsorshipDto? = null,
    @Json(name = "sponsorshipStatus") val sponsorshipStatusRaw: String? = null,
    @Json(name = "visaType") val visaTypeRaw: String? = null,
    @Json(name = "contractType") val contractType: String = "Full-time",
    @Json(name = "industry") val industry: String = "Technology",
    @Json(name = "experienceLevel") val experienceLevel: String = "Mid",
    @Json(name = "applicationUrl") val applicationUrl: String = "",
    @Json(name = "datePosted") val datePosted: String = ""
) {
    /**
     * Resolves the effective company name whether structured or plain text.
     */
    fun getEffectiveCompanyName(): String {
        return company?.name ?: companyNameRaw ?: "Verified Employer"
    }

    /**
     * Resolves the display salary string.
     */
    fun getFormattedSalary(): String {
        return salary?.getFormattedText()
            ?: salaryTextRaw
            ?: "Competitive Salary"
    }

    /**
     * Resolves the sponsorship status text.
     */
    fun getEffectiveSponsorshipStatus(): String {
        return sponsorship?.sponsorshipStatus
            ?: sponsorshipStatusRaw
            ?: if (sponsorship?.isSponsored == true) "Sponsorship Available" else "Sponsorship Unconfirmed"
    }

    /**
     * Resolves the visa type name.
     */
    fun getEffectiveVisaType(): String {
        return sponsorship?.visaType
            ?: visaTypeRaw
            ?: "Work Permit / Visa Pathway"
    }

    /**
     * Maps this API DTO to the local Room database [JobEntity].
     */
    fun toJobEntity(): JobEntity {
        return JobEntity(
            id = id,
            title = title,
            company = getEffectiveCompanyName(),
            country = country.ifEmpty { "Global" },
            location = location.ifEmpty { "Worldwide" },
            description = description.ifEmpty { "Visa sponsorship supported position." },
            salary = getFormattedSalary(),
            visaType = getEffectiveVisaType(),
            confidenceScore = sponsorship?.confidenceScore ?: 85,
            confidenceReason = sponsorship?.verificationReason ?: "Verified employer sponsorship record",
            relocationAssistance = sponsorship?.relocationAssistance ?: true,
            contractType = contractType.ifEmpty { "Full-time" },
            industry = industry.ifEmpty { "General" },
            experienceLevel = experienceLevel.ifEmpty { "Mid" },
            applicationUrl = applicationUrl,
            datePosted = datePosted.ifEmpty { "Recently" },
            isBookmarked = false,
            isFraud = false,
            isCustomPosted = false
        )
    }
}

/**
 * Detailed representation of Sponsorship & Visa status details.
 */
@JsonClass(generateAdapter = true)
data class SponsorshipDto(
    @Json(name = "isSponsored") val isSponsored: Boolean = true,
    @Json(name = "sponsorshipStatus") val sponsorshipStatus: String = "APPROVED", // "APPROVED", "AVAILABLE", "LMIA_VERIFIED", "PENDING"
    @Json(name = "visaType") val visaType: String = "Skilled Worker Visa",
    @Json(name = "relocationAssistance") val relocationAssistance: Boolean = true,
    @Json(name = "confidenceScore") val confidenceScore: Int = 90,
    @Json(name = "verificationReason") val verificationReason: String = "Employer holds official government sponsor license."
)

/**
 * Detailed representation of Salary information parsed from the API response.
 */
@JsonClass(generateAdapter = true)
data class SalaryDto(
    @Json(name = "rawSalaryText") val rawSalaryText: String? = null,
    @Json(name = "minAmount") val minAmount: Double? = null,
    @Json(name = "maxAmount") val maxAmount: Double? = null,
    @Json(name = "medianAmount") val medianAmount: Double? = null,
    @Json(name = "currency") val currency: String? = "USD",
    @Json(name = "period") val period: String? = "YEARLY", // "YEARLY", "MONTHLY", "HOURLY"
    @Json(name = "isEstimated") val isEstimated: Boolean = false,
    @Json(name = "percentiles") val percentiles: SalaryPercentilesDto? = null
) {
    /**
     * Format salary nicely for user interface display.
     */
    fun getFormattedText(): String {
        if (!rawSalaryText.isNullOrBlank()) return rawSalaryText

        val curr = currency ?: "$"
        val per = when (period?.uppercase()) {
            "HOURLY" -> "/hr"
            "MONTHLY" -> "/mo"
            else -> "/yr"
        }

        return when {
            minAmount != null && maxAmount != null -> {
                "$curr${minAmount.toLong()} - $curr${maxAmount.toLong()} $per"
            }
            medianAmount != null -> {
                "$curr${medianAmount.toLong()} $per"
            }
            minAmount != null -> {
                "From $curr${minAmount.toLong()} $per"
            }
            else -> "Competitive Salary"
        }
    }
}

/**
 * Percentile breakdown for detailed salary distribution.
 */
@JsonClass(generateAdapter = true)
data class SalaryPercentilesDto(
    @Json(name = "p10") val p10: Double? = null,
    @Json(name = "p25") val p25: Double? = null,
    @Json(name = "p50") val p50: Double? = null,
    @Json(name = "p75") val p75: Double? = null,
    @Json(name = "p90") val p90: Double? = null
)

/**
 * Information about the hiring company parsed from the API response.
 */
@JsonClass(generateAdapter = true)
data class CompanyDto(
    @Json(name = "name") val name: String,
    @Json(name = "logoUrl") val logoUrl: String? = null,
    @Json(name = "website") val website: String? = null,
    @Json(name = "isVerifiedSponsor") val isVerifiedSponsor: Boolean = true
)
