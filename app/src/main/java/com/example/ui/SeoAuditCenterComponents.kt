package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Ahrefs Site Explorer & Site Audit Center
 * Tailored for: Globalvisajobs (https://globalvisajobs.website/)
 * Implements all 12 SEO & Growth Opportunities across Content, Links, and Technical categories.
 */

enum class SeoOpportunityCategory(val label: String, val iconColor: Color) {
    CONTENT("Content", TealCyan),
    LINKS("Links", EmeraldGreen),
    TECHNICAL("Technical", AmberGold)
}

enum class OpportunityStatus(val label: String, val badgeColor: Color) {
    OPEN("Open", CoralRed),
    IN_PROGRESS("In Progress", AmberGold),
    RESOLVED("Resolved", EmeraldGreen)
}

data class SeoOpportunityItem(
    val id: String,
    val title: String,
    val sourceTool: String,
    val category: SeoOpportunityCategory,
    val description: String,
    val impact: String,
    val currentPositionOrMetric: String,
    val targetUrl: String,
    val keyCompetitors: List<String> = emptyList(),
    val recommendedAction: String,
    val generatedSnippetOrCode: String,
    var status: OpportunityStatus = OpportunityStatus.OPEN
)

val defaultAhrefsOpportunities = listOf(
    // --- CONTENT OPPORTUNITIES (6 ITEMS) ---
    SeoOpportunityItem(
        id = "content_low_hanging",
        title = "Low-hanging fruit keywords",
        sourceTool = "Site Explorer",
        category = SeoOpportunityCategory.CONTENT,
        description = "Update existing pages or create new ones to rank higher than current positions of 4–15 on Google search.",
        impact = "High Traffic Lift",
        currentPositionOrMetric = "Positions 4–15",
        targetUrl = "https://globalvisajobs.website/uk-tier-2-skilled-worker-jobs",
        keyCompetitors = listOf("Relocate.me", "MyVisaJobs", "Otta"),
        recommendedAction = "Expand H2 subheadings to target secondary intent ('Tier-2 CoS eligible companies with visa fee reimbursement', 'UK shortage occupation list updates 2026'). Add interactive salary threshold widget to bump ranking into Top 3.",
        generatedSnippetOrCode = """
<!-- Optimized On-Page Meta & Header Structure for globalvisajobs.website -->
<title>UK Skilled Worker Visa Jobs 2026 | Verified Tier-2 Sponsor Directory (£38,700 Threshold)</title>
<meta name="description" content="Search 1,450+ verified UK companies offering Certificate of Sponsorship (CoS), flight allowances, and visa legal fee coverage. Fully updated for 2026 Home Office salary thresholds." />

<article class="job-guide">
  <h1>UK Skilled Worker Visa Sponsorship Directory (2026 Edition)</h1>
  <p class="lead-summary">Access verified UK employers licensed by the Home Office to issue Defined Certificates of Sponsorship (CoS) for foreign workers across IT, healthcare, engineering, and finance.</p>
  
  <h2>Top Tier-2 CoS Employers Hiring Today</h2>
  <!-- Dynamic Filterable Table with Live Application Deep-Links -->
  <div class="salary-benchmark-callout">
    <strong>2026 Home Office Minimum Threshold:</strong> £38,700/year (or going rate for your SOC 2020 code).
  </div>
</article>
        """.trimIndent()
    ),
    SeoOpportunityItem(
        id = "content_ai_overviews",
        title = "AI Overviews and featured snippets",
        sourceTool = "Site Explorer",
        category = SeoOpportunityCategory.CONTENT,
        description = "Update pages ranking for keywords with AI Overviews or featured snippets where target ranks in positions 2–8.",
        impact = "AI Snippet Dominance",
        currentPositionOrMetric = "Positions 2–8 (AI Eligible)",
        targetUrl = "https://globalvisajobs.website/guides/certificate-of-sponsorship-cos-explained",
        keyCompetitors = listOf("Gov.uk", "Relocate.me", "VanHack"),
        recommendedAction = "Structure the introductory response in an exact 42-word concise definition followed by a clean ordered step list. AI models (Gemini, Google AI Overviews, Bing Copilot) directly extract this structured format as the zero-click answer.",
        generatedSnippetOrCode = """
<!-- Google AI Overview & Featured Snippet Direct Answer Target -->
<div class="ai-overview-target" itemscope itemtype="https://schema.org/FAQPage">
  <div itemscope itemprop="mainEntity" itemtype="https://schema.org/Question">
    <h2 itemprop="name">How to get a UK Certificate of Sponsorship (CoS) without an agent?</h2>
    <div itemscope itemprop="acceptedAnswer" itemtype="https://schema.org/Answer">
      <div itemprop="text">
        <p><strong>A Certificate of Sponsorship (CoS) is an official electronic record issued by a licensed UK Home Office employer. To get one directly without agents:</strong></p>
        <ol>
          <li>Apply to Home Office-approved A-rated sponsor employers offering eligible SOC 2020 occupations.</li>
          <li>Receive an unconditional job offer meeting the £38,700 salary minimum threshold.</li>
          <li>Employer generates a Defined CoS reference number via the Sponsor Management System (SMS).</li>
          <li>Use your unique CoS reference code to submit your visa application online.</li>
        </ol>
      </div>
    </div>
  </div>
</div>
        """.trimIndent()
    ),
    SeoOpportunityItem(
        id = "content_gap_suggestions",
        title = "Top suggestions from Content gap",
        sourceTool = "Competitive Analysis",
        category = SeoOpportunityCategory.CONTENT,
        description = "Create new content for high-volume keywords that target’s top 10 competitors rank for, but globalvisajobs.website doesn’t.",
        impact = "New Keyword Influx",
        currentPositionOrMetric = "Competitor Gap: 24 Target Keywords",
        targetUrl = "https://globalvisajobs.website/healthcare/nhs-international-nurse-sponsorship",
        keyCompetitors = listOf("Relocate.me", "VanHack", "BrighterMonday", "MyVisaJobs"),
        recommendedAction = "Competitors capture 180K searches/month on 'NHS Band 5 nurse sponsorship requirements' and 'Canada LMIA pre-vetted tech talent with flights'. Publish dedicated guide pages bridging these specific high-intent clusters.",
        generatedSnippetOrCode = """
# Content Gap Bridge: NHS International Nurse & Midwife Sponsorship (2026)
Target Keywords: "NHS Tier 2 CoS nurse", "NMC PIN OSCE overseas registration", "Band 5 NHS visa sponsorship"
Search Volume: 140,000/mo | Difficulty: Medium (KD 34)

## Outline for globalvisajobs.website:
1. Complete Breakdown of NHS Trusts Sponsoring Health and Care Worker Visas (exempt from £38,700 general cap; £23,200 threshold applies).
2. NMC Overseas Registration Step-by-Step (CBT Exam -> OET/IELTS 7.0 -> OSCE in UK).
3. Comprehensive Package Inclusions:
   - Free NHS visa application fee (£284 per person)
   - Immigration Health Surcharge (IHS) full exemption
   - Relocation advance: Flights, 2 months free hospital accommodation, OSCE training fees paid.
4. Direct Application Links to Trust vacancy portals (Barts Health, Manchester University NHS, Guy's & St Thomas').
        """.trimIndent()
    ),
    SeoOpportunityItem(
        id = "content_declining_traffic",
        title = "Content with declining traffic",
        sourceTool = "Site Explorer",
        category = SeoOpportunityCategory.CONTENT,
        description = "Update pages with declining traffic in the last 6 months due to outdated 2024 visa policies and old salary tables.",
        impact = "Traffic Reclaim (-38%)",
        currentPositionOrMetric = "Declining 6-Month Trend",
        targetUrl = "https://globalvisajobs.website/guides/express-entry-crs-cutoffs",
        keyCompetitors = listOf("CanadaVisa", "Moving2Canada"),
        recommendedAction = "Update outdated 2024 rules with 2026 immigration standards: Replace £26,200 UK wage minimum with £38,700, integrate Canadian Category-Based Selection for French proficiency & STEM, and update German Opportunity Card (Chancenkarte) point scales.",
        generatedSnippetOrCode = """
// 2026 Freshness Update Checklist:
1. Update Headline: "Canada Express Entry CRS Cutoffs & Category Selection (Updated for 2026 Draws)"
2. Purge outdated 2024 cutoff references; insert live 2026 category selection draws (Healthcare, STEM, French-language speakers).
3. Add 'Last Updated' microdata: <meta property="article:modified_time" content="2026-03-01T08:00:00Z" />
4. Add internal CTA pointing to real-time Job Verification radar.
        """.trimIndent()
    ),
    SeoOpportunityItem(
        id = "content_pages_once",
        title = "Pages only published once",
        sourceTool = "Content Explorer",
        category = SeoOpportunityCategory.CONTENT,
        description = "Update old pages with low traffic that were published once and never updated with modern interactive modules.",
        impact = "Evergreen Activation",
        currentPositionOrMetric = "Published 1x (Stale)",
        targetUrl = "https://globalvisajobs.website/countries/ireland-critical-skills-permit",
        keyCompetitors = listOf("CitizensInformation.ie", "Relocate.me"),
        recommendedAction = "Enrich single-publish articles by embedding interactive widgets: Add Ireland Critical Skills salary checker (€38,000+), Stamp 4 fast-track to permanent residency explanation, and direct employer verification links.",
        generatedSnippetOrCode = """
<!-- Interactive Enhancement for Stale Evergreen Country Guides -->
<section class="interactive-permit-module">
  <h3>Ireland Critical Skills Employment Permit (CSEP) - 2026 Calculator</h3>
  <div class="csep-perks-grid">
    <div class="perk"><strong>Min. Base Salary:</strong> €38,000 (Relevant Degree) or €64,000 (Any Experience)</div>
    <div class="perk"><strong>Permanent Residency:</strong> Eligible for Stamp 4 PR after only 21 months</div>
    <div class="perk"><strong>Family Rights:</strong> Immediate spousal right to work with Stamp 1G</div>
  </div>
  <a href="/jobs?country=Ireland&sponsor=Verified" class="cta-primary">View Verified Irish Tech & MedTech Sponsors</a>
</section>
        """.trimIndent()
    ),
    SeoOpportunityItem(
        id = "content_cannibalization",
        title = "Potential cannibalization",
        sourceTool = "Site Explorer",
        category = SeoOpportunityCategory.CONTENT,
        description = "Avoid multiple pages with the same search intent ranking for the same keyword and splitting search engine ranking signals.",
        impact = "Equity Consolidation",
        currentPositionOrMetric = "2 Pages Competing (Split Ranks)",
        targetUrl = "https://globalvisajobs.website/uk-sponsored-jobs-2026 vs /uk-visa-jobs",
        keyCompetitors = listOf("Internal Conflict"),
        recommendedAction = "Consolidate '/uk-sponsored-jobs-2026' into master pillar page '/uk-visa-jobs' via a 301 Permanent Redirect, or differentiate search intent clearly (make one an exhaustive sponsor directory and the other a step-by-step visa application walkthrough).",
        generatedSnippetOrCode = """
# 301 Permanent Redirect for Intent Cannibalization:
# Add to .htaccess or Cloudflare Redirect Rules:
Redirect 301 /uk-sponsored-jobs-2026 https://globalvisajobs.website/uk-visa-jobs

# Master Hub Canonical Declaration on /uk-visa-jobs:
<link rel="canonical" href="https://globalvisajobs.website/uk-visa-jobs" />
        """.trimIndent()
    ),

    // --- LINKS OPPORTUNITIES (5 ITEMS) ---
    SeoOpportunityItem(
        id = "links_redirects_404",
        title = "Redirects to implement (404 Reclaim)",
        sourceTool = "Site Explorer",
        category = SeoOpportunityCategory.LINKS,
        description = "Reclaim valuable backlinks pointing to most-linked pages on globalvisajobs.website that currently return 404 status codes.",
        impact = "High Domain Authority Lift",
        currentPositionOrMetric = "14 High-PR Dead Links",
        targetUrl = "https://globalvisajobs.website/apply/vanhack-partner-2024 (404)",
        keyCompetitors = listOf("Lost Backlinks from TechCrunch & Reddit"),
        recommendedAction = "Implement server-level 301 redirects routing dead legacy URLs to active relevant directories, reclaiming 100% of accumulated PageRank and link equity.",
        generatedSnippetOrCode = """
# NGINX / Cloudflare 301 Redirect Rules Map for globalvisajobs.website:
location = /apply/vanhack-partner-2024 {
    return 301 https://globalvisajobs.website/sponsors/vanhack-verified;
}
location = /jobs/uk-sponsorship-2024 {
    return 301 https://globalvisajobs.website/uk-tier-2-skilled-worker-jobs;
}
location = /kenya-county-vacancies-old {
    return 301 https://globalvisajobs.website/kenya-counties/nairobi;
}
location = /canada-lmia-directory-archive {
    return 301 https://globalvisajobs.website/canada-lmia-jobs;
}
        """.trimIndent()
    ),
    SeoOpportunityItem(
        id = "links_internal_link_opps",
        title = "Internal link opportunities",
        sourceTool = "Site Audit",
        category = SeoOpportunityCategory.LINKS,
        description = "Build new internal links from high-ranking pages that mention top keywords by traffic to related sub-pages.",
        impact = "Crawl Depth & PageRank Flow",
        currentPositionOrMetric = "28 Unlinked Mentions",
        targetUrl = "https://globalvisajobs.website/kenya-counties/nairobi -> /uk-tier-2-skilled-worker-jobs",
        keyCompetitors = listOf("Self Audit"),
        recommendedAction = "On high-ranking Kenya County pages where 'Certificate of Sponsorship' or 'Relocation Packages' are mentioned, add anchor links pointing directly to the UK Sponsor Registry and Relocate.me perk filters.",
        generatedSnippetOrCode = """
<!-- Internal Linking Code Injection: -->
<p>
  While public service roles at Mama Lucy Hospital require county registration, international medical professionals often transition via the 
  <a href="https://globalvisajobs.website/uk-tier-2-skilled-worker-jobs" title="Browse UK NHS CoS Sponsoring Trusts">
    UK NHS Certificate of Sponsorship (CoS) Registry
  </a>
  which provides full visa fee waivers and flight allowances.
</p>
        """.trimIndent()
    ),
    SeoOpportunityItem(
        id = "links_link_intersect",
        title = "Top links from Link intersect",
        sourceTool = "Competitive Analysis",
        category = SeoOpportunityCategory.LINKS,
        description = "Build new links from authoritative domains linking to top 10 competitors (Relocate.me, MyVisaJobs, VanHack), but not yet to globalvisajobs.website.",
        impact = "Competitor Parity",
        currentPositionOrMetric = "18 Target Domains Identified",
        targetUrl = "Outreach Targets: Expatistan.com, KenyanWallStreet.com, Workpermit.com",
        keyCompetitors = listOf("Relocate.me", "VanHack", "MyVisaJobs"),
        recommendedAction = "Execute targeted data-backed outreach to migration portals and expat publishers showcasing globalvisajobs.website's real-time Home Office verification and localized African county job integrations.",
        generatedSnippetOrCode = """
Subject: Pitch: Real-time 2026 Visa Sponsorship Data Study for [Site Name]

Hi [Editor],

I noticed your excellent guide on international worker relocation cites Relocate.me and MyVisaJobs. 

Our engineering team at Global Visa Jobs (https://globalvisajobs.website) just released our 2026 Global Mobility Index analyzing 1,400+ Home Office licensed sponsors that pay flight tickets and 30-day accommodation for foreign talent.

Unlike static databases, our platform features automated API verification against official government registries (UK Home Office SMS, Canada LMIA, and Kenya KNOCS).

Would your readers benefit if we shared our free embeddable Sponsorship Salary Benchmark tool for your visa resource page?

Best regards,
Vincent Mwangangi | Global Visa Jobs Research Team
        """.trimIndent()
    ),
    SeoOpportunityItem(
        id = "links_unlinked_brand_mentions",
        title = "Unlinked brand mentions",
        sourceTool = "Content Explorer",
        category = SeoOpportunityCategory.LINKS,
        description = "Build new high-authority backlinks from domains that already mention 'Global Visa Jobs' or 'globalvisajobs.website' without a hyperlink.",
        impact = "Quick Backlink Win",
        currentPositionOrMetric = "31 Unlinked Brand Mentions",
        targetUrl = "Reddit r/IWantOut, Nairaland Career, Diaspora News Kenya",
        keyCompetitors = listOf("Unclaimed Authority"),
        recommendedAction = "Send gentle attribution outreach to forum moderators and bloggers thanking them for the shoutout and providing a clean canonical link to globalvisajobs.website.",
        generatedSnippetOrCode = """
Subject: Thanks for mentioning Global Visa Jobs in your expat relocation article!

Hi [Author],

Thank you so much for featuring Global Visa Jobs in your recent article on diaspora job opportunities!

I noticed there wasn't a clickable link to our registry. Would you mind adding a hyperlink to https://globalvisajobs.website so your readers can directly access the verified employer database and flight-covered job filters?

Thank you for supporting international jobseekers!
        """.trimIndent()
    ),
    SeoOpportunityItem(
        id = "links_descriptive_anchors",
        title = "Descriptive anchors",
        sourceTool = "Site Explorer",
        category = SeoOpportunityCategory.LINKS,
        description = "Replace generic anchor text ('click here', 'apply now', 'link') with rich, descriptive text explaining what the destination page offers.",
        impact = "Keyword Context Optimization",
        currentPositionOrMetric = "62% Generic Anchors",
        targetUrl = "https://globalvisajobs.website/jobs",
        keyCompetitors = listOf("Internal Link Structure"),
        recommendedAction = "Audit internal links and buttons. Transform vague labels into high-intent descriptive anchors that pass topical relevance to target search engines.",
        generatedSnippetOrCode = """
<!-- Descriptive Anchor Text Transformation Matrix: -->
<!-- ❌ Generic (Before): -->
<a href="/jobs/uk">Click here</a> to see jobs.
<a href="/sponsors">Read more</a> about visa sponsors.

<!-- ✅ Optimized & Descriptive (After): -->
<a href="/jobs/uk" title="Search UK Tier-2 CoS Software and Engineering Vacancies">
  Explore 1,200+ Verified UK Skilled Worker Roles With Flight Allowances
</a>
<a href="/sponsors" title="View Home Office Licensed Sponsor Directory">
  Browse Official Home Office Licensed UK & Canada LMIA Sponsor Registry
</a>
        """.trimIndent()
    ),

    // --- TECHNICAL OPPORTUNITIES (4 CRITICAL ISSUES) ---
    SeoOpportunityItem(
        id = "technical_critical_issues",
        title = "Critical issues: 4 Technical site audit errors",
        sourceTool = "Site Audit",
        category = SeoOpportunityCategory.TECHNICAL,
        description = "Resolve 4 critical site audit errors impacting crawl budget, Google Jobs indexation, and mobile rendering performance for globalvisajobs.website.",
        impact = "100% Health Score Fix",
        currentPositionOrMetric = "4 Critical Errors (Health: 96%)",
        targetUrl = "https://globalvisajobs.website/ (Site-Wide)",
        keyCompetitors = listOf("Site Audit Diagnostic"),
        recommendedAction = "Deploy Schema.org JobPosting JSON-LD on all vacancy pages, generate clean sitemap.xml index, enforce self-canonical URLs, and eliminate Core Web Vitals render-blocking CSS/JS.",
        generatedSnippetOrCode = """
<!-- Issue #1: Schema.org JobPosting JSON-LD for Google Jobs Carousel -->
<script type="application/ld+json">
{
  "@context": "https://schema.org/",
  "@type": "JobPosting",
  "title": "Senior Cloud Platform Engineer - UK Skilled Worker CoS",
  "description": "Full visa sponsorship provided. Defined Certificate of Sponsorship (CoS) issued upon contract signing. Includes £5,000 relocation allowance, flights, and 30-day temporary housing in London.",
  "identifier": {
    "@type": "PropertyValue",
    "name": "Global Visa Jobs",
    "value": "GVJ-UK-7891"
  },
  "datePosted": "2026-03-01T00:00:00Z",
  "validThrough": "2026-06-30T23:59:59Z",
  "employmentType": "FULL_TIME",
  "hiringOrganization": {
    "@type": "Organization",
    "name": "Monzo Bank Ltd (Home Office Licensed Sponsor)",
    "sameAs": "https://globalvisajobs.website",
    "logo": "https://globalvisajobs.website/logo.png"
  },
  "jobLocation": {
    "@type": "Place",
    "address": {
      "@type": "PostalAddress",
      "addressLocality": "London",
      "addressCountry": "GB"
    }
  },
  "baseSalary": {
    "@type": "MonetaryAmount",
    "currency": "GBP",
    "value": {
      "@type": "QuantitativeValue",
      "value": 85000,
      "unitText": "YEAR"
    }
  },
  "directApply": true
}
</script>

<!-- Issue #2: Dynamic sitemap.xml Index -->
<!-- Ensure https://globalvisajobs.website/sitemap.xml lists all valid 200 URLs with changefreq: weekly and priority: 0.9 -->

<!-- Issue #3: Self-Referential Canonical Tag -->
<link rel="canonical" href="https://globalvisajobs.website/jobs" />

<!-- Issue #4: Mobile Performance Core Web Vitals (LCP < 1.8s) -->
<!-- Enable brotli compression, HTTP/2 multiplexing, and async font loading -->
        """.trimIndent()
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeoAuditCenterHub(
    onNavigateBack: () -> Unit = {}
) {
    var opportunities by remember { mutableStateOf(defaultAhrefsOpportunities) }
    var selectedCategory by remember { mutableStateOf<SeoOpportunityCategory?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<OpportunityStatus?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedOpportunityId by remember { mutableStateOf<String?>("content_ai_overviews") }
    var showSnippetModal by remember { mutableStateOf<SeoOpportunityItem?>(null) }
    var showSchemaGeneratorDialog by remember { mutableStateOf(false) }
    var showRedirectsExportDialog by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // Calculate dynamic health score: 96% base + up to 4% for resolving issues
    val resolvedCount = opportunities.count { it.status == OpportunityStatus.RESOLVED }
    val healthScore = 96 + (resolvedCount * 4 / opportunities.size)

    val filteredOpportunities = remember(opportunities, selectedCategory, selectedStatusFilter, searchQuery) {
        opportunities.filter { item ->
            val matchCategory = selectedCategory == null || item.category == selectedCategory
            val matchStatus = selectedStatusFilter == null || item.status == selectedStatusFilter
            val matchSearch = searchQuery.isBlank() || 
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true) ||
                item.recommendedAction.contains(searchQuery, ignoreCase = true) ||
                item.targetUrl.contains(searchQuery, ignoreCase = true)
            matchCategory && matchStatus && matchSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .testTag("seo_audit_center_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Ahrefs Site Explorer Brand Header ---
        item {
            AhrefsDomainOverviewBanner(
                domain = "globalvisajobs.website/",
                project = "Globalvisajobs",
                healthScore = healthScore,
                resolvedCount = resolvedCount,
                totalCount = opportunities.size,
                onGenerateSchema = { showSchemaGeneratorDialog = true },
                onExportRedirects = { showRedirectsExportDialog = true },
                onCopyFullReport = {
                    val reportText = buildAuditReportMarkdown(opportunities, healthScore)
                    clipboardManager.setText(AnnotatedString(reportText))
                    Toast.makeText(context, "Full Ahrefs Audit Report copied to clipboard!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // --- 1B. Lighthouse 13.5.0 Web Vitals, Accessibility, Security & Agentic Browsing Suite ---
        item {
            LighthouseWebVitalsAndAgenticSuiteCard()
        }

        // --- 1C. Google AdSense Publisher Integration & CLS-Safe Loader (ca-pub-1375386376692976) ---
        item {
            GoogleAdSensePublisherIntegrationCard()
        }

        // --- 2. Category & Status Filter Chips ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Category Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All Opportunities (${opportunities.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldGreen,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("seo_cat_all")
                    )
                    FilterChip(
                        selected = selectedCategory == SeoOpportunityCategory.CONTENT,
                        onClick = { selectedCategory = SeoOpportunityCategory.CONTENT },
                        label = { 
                            Text(
                                "Content (6)", 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedCategory == SeoOpportunityCategory.CONTENT) Color.White else TealCyan
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TealCyan,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("seo_cat_content")
                    )
                    FilterChip(
                        selected = selectedCategory == SeoOpportunityCategory.LINKS,
                        onClick = { selectedCategory = SeoOpportunityCategory.LINKS },
                        label = { 
                            Text(
                                "Links (5)", 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedCategory == SeoOpportunityCategory.LINKS) Color.White else EmeraldGreen
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldGreen,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("seo_cat_links")
                    )
                    FilterChip(
                        selected = selectedCategory == SeoOpportunityCategory.TECHNICAL,
                        onClick = { selectedCategory = SeoOpportunityCategory.TECHNICAL },
                        label = { 
                            Text(
                                "Technical (4 Critical)", 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedCategory == SeoOpportunityCategory.TECHNICAL) Color.White else AmberGold
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberGold,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("seo_cat_technical")
                    )
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search 12 audit opportunities, keywords, URLs...", fontSize = 13.sp, color = SlateMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SlateMuted) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = SlateMuted)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("seo_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NavyMedium,
                        unfocusedContainerColor = NavyMedium,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight
                    ),
                    singleLine = true
                )
            }
        }

        // --- 3. Section Header ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ahrefs Actionable Opportunities (${filteredOpportunities.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhiteActive
                )
                Text(
                    text = "Audit Source: 2026 Live Crawl",
                    fontSize = 11.sp,
                    color = SlateMuted
                )
            }
        }

        // --- 4. Interactive List of 12 Opportunities ---
        items(filteredOpportunities, key = { it.id }) { item ->
            val isExpanded = expandedOpportunityId == item.id
            SeoOpportunityCard(
                item = item,
                isExpanded = isExpanded,
                onToggleExpand = {
                    expandedOpportunityId = if (isExpanded) null else item.id
                },
                onStatusChange = { newStatus ->
                    opportunities = opportunities.map {
                        if (it.id == item.id) it.copy(status = newStatus) else it
                    }
                    val msg = when (newStatus) {
                        OpportunityStatus.RESOLVED -> "Resolved! Health score increased."
                        OpportunityStatus.IN_PROGRESS -> "Status set to In Progress."
                        OpportunityStatus.OPEN -> "Status reset to Open."
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                },
                onViewCodeSnippet = {
                    showSnippetModal = item
                },
                onCopyFix = {
                    clipboardManager.setText(AnnotatedString(item.generatedSnippetOrCode))
                    Toast.makeText(context, "Implementation fix copied to clipboard!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // --- 5. Quick Tools & Generators Footer Card ---
        item {
            QuickToolsAndSchemaBox(
                onOpenSchemaDialog = { showSchemaGeneratorDialog = true },
                onOpenRedirectsDialog = { showRedirectsExportDialog = true },
                onOpenSnippets = {
                    expandedOpportunityId = "content_ai_overviews"
                    Toast.makeText(context, "Opened AI Overview Snippet card above", Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Modal: Full Code / Implementation Snippet Viewer
    showSnippetModal?.let { modalItem ->
        SeoCodeSnippetDialog(
            item = modalItem,
            onDismiss = { showSnippetModal = null }
        )
    }

    // Modal: Interactive Schema.org JobPosting Generator
    if (showSchemaGeneratorDialog) {
        JobPostingSchemaGeneratorDialog(
            onDismiss = { showSchemaGeneratorDialog = false }
        )
    }

    // Modal: 301 Redirects Export Generator
    if (showRedirectsExportDialog) {
        RedirectRulesExportDialog(
            onDismiss = { showRedirectsExportDialog = false }
        )
    }
}

/**
 * Domain Overview Banner styled after Ahrefs Site Explorer
 */
@Composable
fun AhrefsDomainOverviewBanner(
    domain: String,
    project: String,
    healthScore: Int,
    resolvedCount: Int,
    totalCount: Int,
    onGenerateSchema: () -> Unit,
    onExportRedirects: () -> Unit,
    onCopyFullReport: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ahrefs_domain_overview_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        border = BorderStroke(1.dp, NavyLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Brand & Domain
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(TealCyan.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Domain",
                            tint = TealCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = project,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = WhiteActive
                        )
                        Text(
                            text = domain,
                            fontSize = 12.sp,
                            color = EmeraldGreen,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Health Score Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (healthScore >= 98) EmeraldGreen.copy(alpha = 0.12f) else AmberGold.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (healthScore >= 98) EmeraldGreen else AmberGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Health: $healthScore%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (healthScore >= 98) EmeraldGreen else AmberGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = NavyLight)
            Spacer(modifier = Modifier.height(14.dp))

            // 3-Metric Summary Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricMiniItem(
                    label = "Opportunities",
                    value = "$totalCount Items",
                    subtext = "Content, Links, Tech",
                    color = WhiteActive
                )
                MetricMiniItem(
                    label = "Resolved",
                    value = "$resolvedCount / $totalCount",
                    subtext = "${(resolvedCount * 100) / totalCount}% Completed",
                    color = EmeraldGreen
                )
                MetricMiniItem(
                    label = "Critical Issues",
                    value = "4 Audit Errors",
                    subtext = "Schema, 404, LCP",
                    color = CoralRed
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onGenerateSchema,
                    modifier = Modifier.weight(1f).height(40.dp).testTag("btn_quick_schema"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, TealCyan),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = TealCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Job Schema", fontSize = 11.sp, color = TealCyan, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onExportRedirects,
                    modifier = Modifier.weight(1f).height(40.dp).testTag("btn_quick_redirects"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, AmberGold),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.AltRoute, contentDescription = null, tint = AmberGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("301 Redirects", fontSize = 11.sp, color = AmberGold, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onCopyFullReport,
                    modifier = Modifier.weight(1.1f).height(40.dp).testTag("btn_copy_audit_report"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Report", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MetricMiniItem(label: String, value: String, subtext: String, color: Color) {
    Column {
        Text(text = label, fontSize = 11.sp, color = SlateMuted)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = subtext, fontSize = 10.sp, color = SlateMuted)
    }
}

/**
 * Individual Interactive Opportunity Card with Expandable Detail View
 */
@Composable
fun SeoOpportunityCard(
    item: SeoOpportunityItem,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onStatusChange: (OpportunityStatus) -> Unit,
    onViewCodeSnippet: () -> Unit,
    onCopyFix: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onToggleExpand() }
            .testTag("seo_card_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        border = BorderStroke(
            1.dp,
            if (isExpanded) item.category.iconColor.copy(alpha = 0.5f) else NavyLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Category Tag, Source Tool, and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = item.category.iconColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = item.category.label.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = item.category.iconColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.sourceTool,
                        fontSize = 11.sp,
                        color = SlateMuted,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = item.status.badgeColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, item.status.badgeColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = item.status.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = item.status.badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Position Metric
            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = WhiteActive
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = AmberGold,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.currentPositionOrMetric,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AmberGold
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = TealCyan,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.impact,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TealCyan
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Brief Description
            Text(
                text = item.description,
                fontSize = 13.sp,
                color = SlateMuted,
                lineHeight = 18.sp
            )

            // Target URL Pill
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = NavyDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = SlateMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.targetUrl,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = WhiteActive,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Expanded Details Section
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    HorizontalDivider(color = NavyLight)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Ahrefs Actionable Remedy:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = WhiteActive
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.recommendedAction,
                        fontSize = 12.sp,
                        color = SlateMuted,
                        lineHeight = 18.sp
                    )

                    if (item.keyCompetitors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Top Competitors Ranking for this Intent:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = WhiteActive
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item.keyCompetitors.forEach { comp ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = NavyDark,
                                    border = BorderStroke(1.dp, NavyLight)
                                ) {
                                    Text(
                                        text = comp,
                                        fontSize = 11.sp,
                                        color = WhiteActive,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons Row: Copy Fix, View Full Code, Change Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onCopyFix,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier.height(36.dp).weight(1f).testTag("copy_fix_${item.id}"),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Fix", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = onViewCodeSnippet,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp).weight(1f).testTag("view_code_${item.id}"),
                            border = BorderStroke(1.dp, TealCyan),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(14.dp), tint = TealCyan)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Inspect Code", fontSize = 11.sp, color = TealCyan, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Switcher Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Change Status:", fontSize = 11.sp, color = SlateMuted)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OpportunityStatus.values().forEach { st ->
                                val isSelected = item.status == st
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onStatusChange(st) },
                                    label = { Text(st.label, fontSize = 10.sp) },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = st.badgeColor,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.height(28.dp).testTag("status_${st.name.lowercase()}_${item.id}")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Quick Tools Box for Schema.org and Redirects
 */
@Composable
fun QuickToolsAndSchemaBox(
    onOpenSchemaDialog: () -> Unit,
    onOpenRedirectsDialog: () -> Unit,
    onOpenSnippets: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        border = BorderStroke(1.dp, NavyLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ahrefs Resolution Toolkit for globalvisajobs.website",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = WhiteActive
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Use these built-in generators to directly deploy the required technical fixes, Google Jobs rich snippets, and server redirect maps.",
                fontSize = 12.sp,
                color = SlateMuted,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenSchemaDialog,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealCyan),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Text("JobPosting Schema", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onOpenRedirectsDialog,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Text("301 Redirect Rules", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Dialog displaying full generated code snippet
 */
@Composable
fun SeoCodeSnippetDialog(
    item: SeoOpportunityItem,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = WhiteActive
                )
                Text(
                    text = "Implementation Code & Markup",
                    fontSize = 12.sp,
                    color = SlateMuted
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.generatedSnippetOrCode,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFFE2E8F0),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(item.generatedSnippetOrCode))
                    Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Code")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = SlateMuted)
            }
        },
        containerColor = NavyMedium,
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Dialog to generate customized JobPosting JSON-LD Schema
 */
@Composable
fun JobPostingSchemaGeneratorDialog(
    onDismiss: () -> Unit
) {
    var jobTitle by remember { mutableStateOf("Senior Cloud Software Engineer - UK Skilled Worker CoS") }
    var companyName by remember { mutableStateOf("Monzo Bank Ltd") }
    var salary by remember { mutableStateOf("85000") }
    var currency by remember { mutableStateOf("GBP") }
    var location by remember { mutableStateOf("London, United Kingdom") }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val generatedSchema = remember(jobTitle, companyName, salary, currency, location) {
        """
<script type="application/ld+json">
{
  "@context": "https://schema.org/",
  "@type": "JobPosting",
  "title": "$jobTitle",
  "description": "Full visa sponsorship provided. Defined Certificate of Sponsorship (CoS) issued directly. Relocation allowance, flight tickets, and 30-day temporary accommodation included.",
  "identifier": {
    "@type": "PropertyValue",
    "name": "Global Visa Jobs",
    "value": "GVJ-SCHEMA-${System.currentTimeMillis() % 100000}"
  },
  "datePosted": "2026-03-01T00:00:00Z",
  "validThrough": "2026-06-30T23:59:59Z",
  "employmentType": "FULL_TIME",
  "hiringOrganization": {
    "@type": "Organization",
    "name": "$companyName",
    "sameAs": "https://globalvisajobs.website"
  },
  "jobLocation": {
    "@type": "Place",
    "address": {
      "@type": "PostalAddress",
      "addressLocality": "$location"
    }
  },
  "baseSalary": {
    "@type": "MonetaryAmount",
    "currency": "$currency",
    "value": {
      "@type": "QuantitativeValue",
      "value": ${salary.toLongOrNull() ?: 75000},
      "unitText": "YEAR"
    }
  },
  "directApply": true
}
</script>
        """.trimIndent()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Schema.org JobPosting Generator",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = WhiteActive
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Generates Google Jobs-compliant JSON-LD structured data to index job listings in Google SERP carousels.",
                    fontSize = 12.sp,
                    color = SlateMuted
                )

                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = { Text("Job Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Hiring Company (Licensed Sponsor)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = salary,
                        onValueChange = { salary = it },
                        label = { Text("Salary (Annual)") },
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text("Currency") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = generatedSchema,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFFE2E8F0),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(generatedSchema))
                    Toast.makeText(context, "Schema.org JSON-LD copied!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealCyan)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy JSON-LD")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = SlateMuted)
            }
        },
        containerColor = NavyMedium,
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Dialog to export 301 Redirect Rules
 */
@Composable
fun RedirectRulesExportDialog(
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val nginxRules = """
# ==============================================================================
# NGINX 301 Redirect Configuration for globalvisajobs.website
# Generated by Global Visa Jobs Ahrefs Site Explorer Resolution Toolkit
# ==============================================================================

# 1. Intent Cannibalization Consolidation
location = /uk-sponsored-jobs-2026 {
    return 301 https://globalvisajobs.website/uk-visa-jobs;
}

# 2. Reclaim High-Authority 404 Links
location = /apply/vanhack-partner-2024 {
    return 301 https://globalvisajobs.website/sponsors/vanhack-verified;
}
location = /jobs/uk-sponsorship-2024 {
    return 301 https://globalvisajobs.website/uk-tier-2-skilled-worker-jobs;
}
location = /kenya-county-vacancies-old {
    return 301 https://globalvisajobs.website/kenya-counties/nairobi;
}
location = /canada-lmia-directory-archive {
    return 301 https://globalvisajobs.website/canada-lmia-jobs;
}

# 3. Canonical Enforcement (Non-WWW to WWW or vice versa & SSL)
if (${'$'}host = 'globalvisajobs.com') {
    return 301 https://globalvisajobs.website${'$'}request_uri;
}
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "301 Redirect Rules (NGINX / Cloudflare)",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = WhiteActive
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Copy and paste into your NGINX server block or Cloudflare Page Rules to reclaim lost backlink equity and resolve 404 errors.",
                    fontSize = 12.sp,
                    color = SlateMuted
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = nginxRules,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFFE2E8F0),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(nginxRules))
                    Toast.makeText(context, "Redirect rules copied!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Rules")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = SlateMuted)
            }
        },
        containerColor = NavyMedium,
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Builds a markdown report of all 12 Ahrefs opportunities
 */
fun buildAuditReportMarkdown(items: List<SeoOpportunityItem>, healthScore: Int): String {
    val sb = StringBuilder()
    sb.appendLine("# Ahrefs Site Explorer & Site Audit Report")
    sb.appendLine("**Target:** Globalvisajobs (`https://globalvisajobs.website/`)")
    sb.appendLine("**Current Health Score:** $healthScore% (Base: 96%)")
    sb.appendLine("**Total Action Items:** ${items.size} Opportunities across Content, Links, and Technical\n")

    items.groupBy { it.category }.forEach { (category, categoryItems) ->
        sb.appendLine("## ${category.label} Opportunities (${categoryItems.size})")
        categoryItems.forEachIndexed { index, item ->
            sb.appendLine("### ${index + 1}. ${item.title}")
            sb.appendLine("- **Source:** ${item.sourceTool}")
            sb.appendLine("- **Metric:** ${item.currentPositionOrMetric}")
            sb.appendLine("- **Target URL:** ${item.targetUrl}")
            sb.appendLine("- **Impact:** ${item.impact}")
            sb.appendLine("- **Status:** ${item.status.label}")
            sb.appendLine("- **Remedy:** ${item.recommendedAction}")
            sb.appendLine()
        }
    }
    return sb.toString()
}
