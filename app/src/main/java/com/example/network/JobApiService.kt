package com.example.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service interface for querying jobs API endpoints with full sponsorship
 * and salary details.
 */
interface JobApiService {

    /**
     * Search jobs with filter parameters for query, location, visa type, sponsorship, and pagination.
     */
    @GET("jobs/search")
    suspend fun searchJobs(
        @Query("query") query: String? = null,
        @Query("country") country: String? = null,
        @Query("location") location: String? = null,
        @Query("visaType") visaType: String? = null,
        @Query("sponsorshipOnly") sponsorshipOnly: Boolean = true,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Header("X-Api-Key") apiKey: String? = null
    ): Response<JobSearchResponseDto>

    /**
     * Fetch detailed job posting information by ID.
     */
    @GET("jobs/{id}")
    suspend fun getJobById(
        @Path("id") jobId: String,
        @Header("X-Api-Key") apiKey: String? = null
    ): Response<JobDto>

    /**
     * Get list of verified visa sponsor employers.
     */
    @GET("sponsors/verified")
    suspend fun getVerifiedSponsors(
        @Query("country") country: String? = null,
        @Query("industry") industry: String? = null,
        @Header("X-Api-Key") apiKey: String? = null
    ): Response<List<CompanyDto>>
}
