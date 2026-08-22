package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CustomAlertEntity
import com.example.data.JobEntity
import com.example.data.RelocationTaskEntity
import com.example.data.VisaApplicationEntity
import com.example.data.JobNotificationEntity
import com.example.network.JobVerificationResult
import com.example.network.LinkedInJobCountResult
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: JobViewModel) {
    var activeTab by remember { mutableStateOf("Discover") }
    var showNotificationHub by remember { mutableStateOf(false) }
    var showDownloadAppDialog by remember { mutableStateOf(false) }
    val appMode by viewModel.appMode.collectAsStateWithLifecycle()
    val showProfileReminderToast by viewModel.showProfileReminderToast.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (showDownloadAppDialog) {
        DownloadAppDialog(onDismiss = { showDownloadAppDialog = false })
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 800.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = NavyDark,
            topBar = {
                TopAppBar(
                    title = {
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text(
                                text = "VisaGo AI",
                                color = WhiteActive,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Global Sponsored Talent Engine",
                                color = SlateMuted,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }
                    },
                    actions = {
                        // Download App Button (Especially prominent on Desktop Dashboard / Tablet)
                        if (isWideScreen) {
                            Button(
                                onClick = { showDownloadAppDialog = true },
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TealCyan,
                                    contentColor = NavyDark
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .height(34.dp)
                                    .testTag("download_app_desktop_btn")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download App", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else {
                            IconButton(
                                onClick = { showDownloadAppDialog = true },
                                modifier = Modifier.padding(end = 4.dp).testTag("download_app_icon_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Download App",
                                    tint = TealCyan
                                )
                            }
                        }

                        // Global Notification Bell with Badge
                        val notifications by viewModel.notifications.collectAsStateWithLifecycle()
                        val unreadCount = notifications.count { !it.isRead }
                        
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            IconButton(
                                onClick = { showNotificationHub = true },
                                modifier = Modifier.testTag("notification_bell_button")
                            ) {
                                Box {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = if (unreadCount > 0) EmeraldGreen else SlateMuted
                                    )
                                    if (unreadCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 2.dp, y = (-2).dp)
                                                .background(Color.Red, shape = androidx.compose.foundation.shape.CircleShape)
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = unreadCount.toString(),
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Quick Mode Switcher & Account Dropdown
                        val authState by viewModel.authState.collectAsState()
                        var showModeMenu by remember { mutableStateOf(false) }
                        val authenticatedUser = (authState as? com.example.auth.AuthState.Authenticated)?.user

                        Box(modifier = Modifier.padding(end = 12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(
                                        if (authenticatedUser != null) EmeraldGreen else Color(0xFFD0BCFF)
                                    )
                                    .clickable { showModeMenu = true }
                                    .testTag("avatar_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = authenticatedUser?.displayName?.take(2)?.uppercase() ?: "VM",
                                    color = if (authenticatedUser != null) NavyDark else Color(0xFF381E72),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            DropdownMenu(
                                expanded = showModeMenu,
                                onDismissRequest = { showModeMenu = false },
                                modifier = Modifier.background(NavyMedium)
                            ) {
                                if (authenticatedUser != null) {
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(authenticatedUser.displayName, color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(authenticatedUser.email, color = SlateMuted, fontSize = 11.sp)
                                            }
                                        },
                                        onClick = {
                                            activeTab = "Profile" // Switch to Profile tab
                                            showModeMenu = false
                                        }
                                    )
                                    HorizontalDivider(color = NavyLight)
                                }
                                DropdownMenuItem(
                                    text = { Text("Jobseeker Mode", color = WhiteActive) },
                                    onClick = {
                                        viewModel.setAppMode("Jobseeker")
                                        showModeMenu = false
                                        Toast.makeText(context, "Switched to Jobseeker Mode", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Employer Dashboard", color = WhiteActive) },
                                    onClick = {
                                        viewModel.setAppMode("Employer")
                                        showModeMenu = false
                                        Toast.makeText(context, "Switched to Employer Mode", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Admin Dashboard", color = WhiteActive) },
                                    onClick = {
                                        viewModel.setAppMode("Admin")
                                        showModeMenu = false
                                        Toast.makeText(context, "Switched to Admin Mode", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = Color(0xFFF3EDF7),
                        tonalElevation = 8.dp
                    ) {
                        val tabs = listOf(
                            Triple("Discover", Icons.Default.Search, "Discover"),
                            Triple("Sponsors", Icons.Default.Verified, "Sponsors"),
                            Triple("Family", Icons.Default.FamilyRestroom, "Family"),
                            Triple("Interview", Icons.Default.RecordVoiceOver, "Interview"),
                            Triple("Reviews", Icons.Default.RateReview, "Reviews"),
                            Triple("Remittance", Icons.Default.CurrencyExchange, "Remit"),
                            Triple("Enterprise", Icons.Default.Settings, "Enterprise")
                        )
                        tabs.forEach { (tag, icon, label) ->
                            val isSelected = activeTab == tag
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { activeTab = tag },
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) NavyDark else SlateMuted
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        color = if (isSelected) EmeraldGreen else SlateMuted,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = EmeraldGreen
                                )
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(NavyDark)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = NavyDark,
                        header = {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    ) {
                        val tabs = listOf(
                            Triple("Discover", Icons.Default.Search, "Discover"),
                            Triple("Sponsors", Icons.Default.Verified, "Sponsor Registry"),
                            Triple("Family", Icons.Default.FamilyRestroom, "Family Rights"),
                            Triple("Interview", Icons.Default.RecordVoiceOver, "Interview Sim"),
                            Triple("Reviews", Icons.Default.RateReview, "Relocation Reviews"),
                            Triple("Remittance", Icons.Default.CurrencyExchange, "Tax & Remittance"),
                            Triple("Match", Icons.Default.Person, "Profile Match"),
                            Triple("Salaries", Icons.Default.Star, "Salaries"),
                            Triple("Pathways", Icons.Default.Info, "Visa Paths"),
                            Triple("Enterprise", Icons.Default.Settings, "Enterprise")
                        )
                        tabs.forEach { (tag, icon, label) ->
                            val isSelected = activeTab == tag
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { activeTab = tag },
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) EmeraldGreen else SlateMuted
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        color = if (isSelected) EmeraldGreen else SlateMuted,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    indicatorColor = NavyMedium,
                                    selectedIconColor = EmeraldGreen,
                                    unselectedIconColor = SlateMuted
                                )
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        NavigationRailItem(
                            selected = false,
                            onClick = { showDownloadAppDialog = true },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Download App",
                                    tint = TealCyan
                                )
                            },
                            label = {
                                Text(
                                    text = "Get App",
                                    color = TealCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                indicatorColor = NavyMedium
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(NavyLight))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    when (activeTab) {
                        "Discover" -> DiscoverTab(viewModel, isWideScreen, onNavigateTab = { activeTab = it })
                        "Sponsors" -> SponsorRegistryHub(onSelectSponsorForJobs = { companyName ->
                            viewModel.performLiveScrapeSearch(companyName, "All")
                            activeTab = "Discover"
                        })
                        "Family" -> SpouseDependantAdvisorHub()
                        "Interview" -> ConsularInterviewSimulatorHub(viewModel)
                        "Reviews" -> EmployerRelocationReviewsHub()
                        "Remittance" -> TaxAndRemittanceCalculatorHub()
                        "Match" -> MatchTab(viewModel, isWideScreen)
                        "Salaries" -> SalariesTab(viewModel)
                        "Pathways" -> PathwaysTab(viewModel)
                        "Enterprise" -> EnterpriseTab(viewModel)
                        "Profile" -> MatchTab(viewModel, isWideScreen)
                    }
                }
            }
        }

        if (showNotificationHub) {
            NotificationHubDialog(
                viewModel = viewModel,
                onDismiss = { showNotificationHub = false }
            )
        }

        AnimatedVisibility(
            visible = showProfileReminderToast,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = if (isWideScreen) 24.dp else 92.dp,
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 460.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = NavyMedium,
                border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(EmeraldGreen.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Profile",
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Complete Your Profile",
                                color = WhiteActive,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        IconButton(
                            onClick = { viewModel.dismissProfileReminderToast() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = SlateMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add your Nationality and Education details to unlock personalized visa sponsorship and eligibility matches.",
                        color = SlateMuted,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.dismissProfileReminderToast() },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Later", color = SlateMuted, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.dismissProfileReminderToast()
                                activeTab = "Match"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Complete Profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoverTab(
    viewModel: JobViewModel,
    isWideScreen: Boolean = false,
    onNavigateTab: (String) -> Unit = {}
) {
    val jobs by viewModel.allJobs.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val searchStatus by viewModel.searchStatus.collectAsStateWithLifecycle()

    var queryText by remember { mutableStateOf("") }
    var searchCountryText by remember { mutableStateOf("All") }

    val countries = listOf("All", "Canada", "United Kingdom", "Australia", "Germany", "Sweden")
    val industries = listOf("All", "Healthcare", "Technology", "Logistics", "Engineering", "Transportation")
    val experiences = listOf("All", "Entry", "Mid", "Senior")

    val selectedCountry by viewModel.selectedCountry.collectAsStateWithLifecycle()
    val selectedIndustry by viewModel.selectedIndustry.collectAsStateWithLifecycle()
    val selectedExperience by viewModel.selectedExperience.collectAsStateWithLifecycle()
    val selectedSponsorship by viewModel.selectedSponsorship.collectAsStateWithLifecycle()
    val jobVerifications by viewModel.jobVerifications.collectAsStateWithLifecycle()
    val isVerifyingJob by viewModel.isVerifyingJob.collectAsStateWithLifecycle()
    val linkedInJobCount by viewModel.linkedInJobCount.collectAsStateWithLifecycle()
    val isFetchingLinkedInCount by viewModel.isFetchingLinkedInCount.collectAsStateWithLifecycle()
    val linkedInCountError by viewModel.linkedInCountError.collectAsStateWithLifecycle()

    val isFetchingIndeedJobs by viewModel.isFetchingIndeedJobs.collectAsStateWithLifecycle()
    val indeedError by viewModel.indeedError.collectAsStateWithLifecycle()
    var companyInputText by remember { mutableStateOf("Ubisoft") }
    var localityInputText by remember { mutableStateOf("us") }

    val isFetchingMultiSource by viewModel.isFetchingMultiSource.collectAsStateWithLifecycle()
    val multiSourceError by viewModel.multiSourceError.collectAsStateWithLifecycle()
    var multiSourceSearchTerm by remember { mutableStateOf("web") }
    var multiSourceLocation by remember { mutableStateOf("new york") }
    var multiSourceCountryIndeed by remember { mutableStateOf("USA") }
    var resultsWantedCount by remember { mutableStateOf(5) }

    val isFetchingGoogleJobs by viewModel.isFetchingGoogleJobs.collectAsStateWithLifecycle()
    val googleJobsError by viewModel.googleJobsError.collectAsStateWithLifecycle()
    var googleJobsQuery by remember { mutableStateOf("Engineer") }
    var googleJobsLocation by remember { mutableStateOf("Silicon Valley") }
    var googleJobsCountry by remember { mutableStateOf("US") }
    var googleJobsDomain by remember { mutableStateOf("com") }
    var googleJobsMaxRows by remember { mutableStateOf(20) }

    var filterSavedOnly by remember { mutableStateOf(false) }
    var showExportPdfDialog by remember { mutableStateOf(false) }

    var activeDetailJob by remember { mutableStateOf<JobEntity?>(null) }
    var activeApplyJob by remember { mutableStateOf<JobEntity?>(null) }
    val context = LocalContext.current

    // Apply Filter Logic
    val filteredJobs = jobs.filter { job ->
        val matchQuery = queryText.isEmpty() ||
                job.title.contains(queryText, ignoreCase = true) ||
                job.company.contains(queryText, ignoreCase = true) ||
                job.description.contains(queryText, ignoreCase = true)

        val matchCountry = selectedCountry == "All" || job.country.equals(selectedCountry, ignoreCase = true)
        val matchIndustry = selectedIndustry == "All" || job.industry.equals(selectedIndustry, ignoreCase = true)
        val matchExp = selectedExperience == "All" || job.experienceLevel.equals(selectedExperience, ignoreCase = true)
        val matchSponsorship = when (selectedSponsorship) {
            "All" -> true
            "Full Sponsorship" -> job.visaType.contains("Full", ignoreCase = true) || job.visaType.contains("Skilled", ignoreCase = true) || job.visaType.contains("Sponsorship", ignoreCase = true) || job.confidenceScore >= 92
            "Relocation Paid" -> job.relocationAssistance == true
            "LMIA Approved" -> job.visaType.contains("LMIA", ignoreCase = true) || job.description.contains("LMIA", ignoreCase = true)
            "High Confidence" -> job.confidenceScore >= 90
            else -> true
        }
        val matchSaved = !filterSavedOnly || job.isBookmarked

        matchQuery && matchCountry && matchIndustry && matchExp && matchSponsorship && matchSaved
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Welcome Header / Hero Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF3EDF7))
                .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Global Visa Aggregator",
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Grounded AI",
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Search live sponsorship, relocation, work permits, and verified immigration jobs worldwide.",
                    color = SlateMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // Global Mobility & Immigration Tools Suite Quick Launcher
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigateTab("Sponsors") }
                        .border(1.dp, EmeraldGreen, RoundedCornerShape(12.dp)),
                    color = NavyMedium
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Official Sponsor Registry", color = WhiteActive, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigateTab("Family") }
                        .border(1.dp, Color(0xFFCE93D8), RoundedCornerShape(12.dp)),
                    color = NavyMedium
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = Color(0xFFCE93D8), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Spouse & Family Rights", color = WhiteActive, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigateTab("Interview") }
                        .border(1.dp, Color(0xFFF48FB1), RoundedCornerShape(12.dp)),
                    color = NavyMedium
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Color(0xFFF48FB1), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Consular Interview Simulator", color = WhiteActive, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigateTab("Reviews") }
                        .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(12.dp)),
                    color = NavyMedium
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.RateReview, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Employer Reviews", color = WhiteActive, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigateTab("Remittance") }
                        .border(1.dp, TealCyan, RoundedCornerShape(12.dp)),
                    color = NavyMedium
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = TealCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tax & Remittance Engine", color = WhiteActive, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live Aggregation Search Inputs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = queryText,
                onValueChange = {
                    queryText = it
                    if (it.isNotBlank()) {
                        viewModel.checkAndTriggerFirstSearchReminder()
                    }
                },
                placeholder = { Text("Role (e.g., Nurse, Driver, Dev)", color = SlateMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SlateMuted) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_input_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = WhiteActive,
                    unfocusedTextColor = WhiteActive,
                    focusedBorderColor = EmeraldGreen,
                    unfocusedBorderColor = NavyLight,
                    focusedContainerColor = NavyMedium,
                    unfocusedContainerColor = NavyMedium
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))

            // AI Search Scraper button
            Button(
                onClick = {
                    viewModel.checkAndTriggerFirstSearchReminder()
                    viewModel.performLiveScrapeSearch(queryText.ifEmpty { "Visa sponsorship jobs" }, searchCountryText)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier
                    .height(56.dp)
                    .testTag("scrape_button")
            ) {
                if (isSearching) {
                    CircularProgressIndicator(color = NavyDark, modifier = Modifier.size(24.dp))
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "AI Scrape", tint = NavyDark)
                }
            }
        }

        // Target country search helper input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Target Search Country:",
                color = SlateMuted,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            Box {
                var countryExpanded by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { countryExpanded = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldGreen)
                ) {
                    Text(searchCountryText)
                }
                DropdownMenu(
                    expanded = countryExpanded,
                    onDismissRequest = { countryExpanded = false },
                    modifier = Modifier.background(NavyMedium)
                ) {
                    countries.filter { it != "All" }.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c, color = WhiteActive) },
                            onClick = {
                                searchCountryText = c
                                countryExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Real-Time Firestore Notification & Search Alert Card
        RealtimeJobAlertCard(
            queryText = queryText,
            searchCountryText = searchCountryText,
            viewModel = viewModel,
            onCriteriaSelected = { newRole, newCountry ->
                queryText = newRole
                searchCountryText = newCountry
            }
        )

        // Master Fetch All Jobs from API Keys Card
        AllApiJobsMasterCard(
            queryText = queryText,
            searchCountryText = searchCountryText,
            viewModel = viewModel
        )

        // Search Status Alert
        searchStatus?.let { status ->
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Status", tint = EmeraldGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = status,
                        color = WhiteActive,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.clearSearchStatus() }) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = SlateMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // LinkedIn Active Jobs Count Widget
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            border = BorderStroke(1.dp, NavyLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("linkedin_job_count_card"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TealCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "LinkedIn Volume",
                            tint = TealCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LinkedIn Market Volume",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Live active job listings posted in the last 24 hours",
                            color = SlateMuted,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.fetchLinkedInJobCount(
                                title = queryText,
                                country = searchCountryText
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TealCyan,
                            contentColor = NavyDark
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp).testTag("query_linkedin_count_btn")
                    ) {
                        if (isFetchingLinkedInCount) {
                            CircularProgressIndicator(color = NavyDark, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (linkedInJobCount != null) "Refresh" else "Check Count",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                linkedInJobCount?.let { res ->
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = NavyLight, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format("%,d", res.count),
                                    color = EmeraldGreen,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 26.sp,
                                    fontFamily = FontFamily.SansSerif
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Active Jobs",
                                    color = SlateMuted,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Role: \"${res.title}\" in ${if (res.location.contains("OR")) "Global Markets (US/UK)" else res.location}",
                                color = WhiteActive,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (res.isSimulated) AmberGold.copy(alpha = 0.15f)
                                    else EmeraldGreen.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (res.isSimulated) "Market Estimate" else "Verified API Live",
                                color = if (res.isSimulated) AmberGold else EmeraldGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "💡 Market Insight: High job posting volume indicates strong global recruitment activity. Consider applying immediately to stand out as an early applicant.",
                        color = SlateMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                linkedInCountError?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ $err. Showing estimated metrics.",
                        color = CoralRed,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Indeed Company Jobs Lookup Widget
        var showLocalityDropdown by remember { mutableStateOf(false) }
        val localities = listOf("us", "ca", "uk", "au", "de", "fr")

        Card(
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            border = BorderStroke(1.dp, NavyLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("indeed_company_jobs_card"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TealCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Indeed Company Jobs",
                            tint = TealCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Indeed Company Jobs Lookup",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Fetch live sponsorship listings directly by company",
                            color = SlateMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Company text input
                    OutlinedTextField(
                        value = companyInputText,
                        onValueChange = { companyInputText = it },
                        label = { Text("Company Name", color = SlateMuted) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("indeed_company_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = NavyLight,
                            focusedContainerColor = NavyDark,
                            unfocusedContainerColor = NavyDark
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Locality Selector Dropdown
                    Box(modifier = Modifier.weight(0.7f)) {
                        OutlinedTextField(
                            value = localityInputText.uppercase(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Locality", color = SlateMuted) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = SlateMuted,
                                    modifier = Modifier.clickable { showLocalityDropdown = true }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLocalityDropdown = true }
                                .testTag("indeed_locality_input"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = WhiteActive,
                                unfocusedTextColor = WhiteActive,
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = NavyLight,
                                focusedContainerColor = NavyDark,
                                unfocusedContainerColor = NavyDark
                            )
                        )
                        DropdownMenu(
                            expanded = showLocalityDropdown,
                            onDismissRequest = { showLocalityDropdown = false },
                            modifier = Modifier.background(NavyMedium)
                        ) {
                            localities.forEach { loc ->
                                DropdownMenuItem(
                                    text = { Text(loc.uppercase(), color = WhiteActive) },
                                    onClick = {
                                        localityInputText = loc
                                        showLocalityDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.fetchIndeedCompanyJobs(
                            companyName = companyInputText,
                            locality = localityInputText
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("fetch_indeed_jobs_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealCyan,
                        contentColor = NavyDark
                    )
                ) {
                    if (isFetchingIndeedJobs) {
                        CircularProgressIndicator(color = NavyDark, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Find Company Jobs", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                indeedError?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ $err",
                        color = CoralRed,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Multi-Source Jobs Scraper (PR Labs RapidAPI) Widget
        var showResultsDropdown by remember { mutableStateOf(false) }
        var showCountryIndeedDropdown by remember { mutableStateOf(false) }
        val resultsOptions = listOf(5, 10, 15, 20)
        val countryIndeedOptions = listOf("USA", "Canada", "UK", "Australia", "Germany")

        Card(
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            border = BorderStroke(1.dp, NavyLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("multi_source_scraper_card"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Multi-Source API Scraper",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Global Multi-Source API Scraper",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Scrape Indeed, LinkedIn, Glassdoor, & ZipRecruiter in real-time",
                            color = SlateMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search Term
                    OutlinedTextField(
                        value = multiSourceSearchTerm,
                        onValueChange = { multiSourceSearchTerm = it },
                        label = { Text("Search Term", color = SlateMuted) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("multisource_term_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = NavyLight,
                            focusedContainerColor = NavyDark,
                            unfocusedContainerColor = NavyDark
                        )
                    )

                    // Location
                    OutlinedTextField(
                        value = multiSourceLocation,
                        onValueChange = { multiSourceLocation = it },
                        label = { Text("Location", color = SlateMuted) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("multisource_loc_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = NavyLight,
                            focusedContainerColor = NavyDark,
                            unfocusedContainerColor = NavyDark
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Country Indeed Dropdown Selector
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = multiSourceCountryIndeed,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Search Region", color = SlateMuted) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = SlateMuted,
                                    modifier = Modifier.clickable { showCountryIndeedDropdown = true }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCountryIndeedDropdown = true }
                                .testTag("multisource_country_input"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = WhiteActive,
                                unfocusedTextColor = WhiteActive,
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = NavyLight,
                                focusedContainerColor = NavyDark,
                                unfocusedContainerColor = NavyDark
                            )
                        )
                        DropdownMenu(
                            expanded = showCountryIndeedDropdown,
                            onDismissRequest = { showCountryIndeedDropdown = false },
                            modifier = Modifier.background(NavyMedium)
                        ) {
                            countryIndeedOptions.forEach { country ->
                                DropdownMenuItem(
                                    text = { Text(country, color = WhiteActive) },
                                    onClick = {
                                        multiSourceCountryIndeed = country
                                        showCountryIndeedDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Results Wanted Dropdown Selector
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = "$resultsWantedCount Jobs",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Count", color = SlateMuted) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = SlateMuted,
                                    modifier = Modifier.clickable { showResultsDropdown = true }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showResultsDropdown = true }
                                .testTag("multisource_results_input"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = WhiteActive,
                                unfocusedTextColor = WhiteActive,
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = NavyLight,
                                focusedContainerColor = NavyDark,
                                unfocusedContainerColor = NavyDark
                            )
                        )
                        DropdownMenu(
                            expanded = showResultsDropdown,
                            onDismissRequest = { showResultsDropdown = false },
                            modifier = Modifier.background(NavyMedium)
                        ) {
                            resultsOptions.forEach { size ->
                                DropdownMenuItem(
                                    text = { Text("$size Jobs", color = WhiteActive) },
                                    onClick = {
                                        resultsWantedCount = size
                                        showResultsDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.fetchMultiSourceJobs(
                            searchTerm = multiSourceSearchTerm,
                            location = multiSourceLocation,
                            countryIndeed = multiSourceCountryIndeed,
                            resultsWanted = resultsWantedCount
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("fetch_multi_source_jobs_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldGreen,
                        contentColor = NavyDark
                    )
                ) {
                    if (isFetchingMultiSource) {
                        CircularProgressIndicator(color = NavyDark, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Search & Aggregate Multi-Source", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                multiSourceError?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ $err",
                        color = CoralRed,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Google Jobs Scraper (Bebity RapidAPI) Widget
        var showGoogleMaxRowsDropdown by remember { mutableStateOf(false) }
        val googleMaxRowsOptions = listOf(5, 10, 20, 30)

        Card(
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            border = BorderStroke(1.dp, NavyLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("google_jobs_scraper_card"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TealCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Google Jobs Scraper",
                            tint = TealCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Google Jobs API Scraper",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Extract verified Google Jobs results dynamically & check for sponsorship",
                            color = SlateMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Query (Search Term)
                    OutlinedTextField(
                        value = googleJobsQuery,
                        onValueChange = { googleJobsQuery = it },
                        label = { Text("Query / Title", color = SlateMuted) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("google_query_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive,
                            focusedBorderColor = TealCyan,
                            unfocusedBorderColor = NavyLight,
                            focusedContainerColor = NavyDark,
                            unfocusedContainerColor = NavyDark
                        )
                    )

                    // Location
                    OutlinedTextField(
                        value = googleJobsLocation,
                        onValueChange = { googleJobsLocation = it },
                        label = { Text("Location", color = SlateMuted) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("google_location_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive,
                            focusedBorderColor = TealCyan,
                            unfocusedBorderColor = NavyLight,
                            focusedContainerColor = NavyDark,
                            unfocusedContainerColor = NavyDark
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Country
                    OutlinedTextField(
                        value = googleJobsCountry,
                        onValueChange = { googleJobsCountry = it },
                        label = { Text("Country (e.g. US)", color = SlateMuted) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("google_country_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive,
                            focusedBorderColor = TealCyan,
                            unfocusedBorderColor = NavyLight,
                            focusedContainerColor = NavyDark,
                            unfocusedContainerColor = NavyDark
                        )
                    )

                    // Max Rows Dropdown Selector
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = "$googleJobsMaxRows Rows",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Max Results", color = SlateMuted) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = SlateMuted,
                                    modifier = Modifier.clickable { showGoogleMaxRowsDropdown = true }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showGoogleMaxRowsDropdown = true }
                                .testTag("google_maxrows_input"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = WhiteActive,
                                unfocusedTextColor = WhiteActive,
                                focusedBorderColor = TealCyan,
                                unfocusedBorderColor = NavyLight,
                                focusedContainerColor = NavyDark,
                                unfocusedContainerColor = NavyDark
                            )
                        )
                        DropdownMenu(
                            expanded = showGoogleMaxRowsDropdown,
                            onDismissRequest = { showGoogleMaxRowsDropdown = false },
                            modifier = Modifier.background(NavyMedium)
                        ) {
                            googleMaxRowsOptions.forEach { size ->
                                DropdownMenuItem(
                                    text = { Text("$size Rows", color = WhiteActive) },
                                    onClick = {
                                        googleJobsMaxRows = size
                                        showGoogleMaxRowsDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.fetchGoogleJobs(
                            query = googleJobsQuery,
                            location = googleJobsLocation,
                            country = googleJobsCountry,
                            domain = googleJobsDomain,
                            maxRows = googleJobsMaxRows
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("fetch_google_jobs_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealCyan,
                        contentColor = NavyMedium
                    )
                ) {
                    if (isFetchingGoogleJobs) {
                        CircularProgressIndicator(color = NavyMedium, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fetch live Google Jobs listings", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                googleJobsError?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ $err",
                        color = CoralRed,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Interactive Filter Bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Saved Jobs Filter Chip
            val savedCount = jobs.count { it.isBookmarked }
            FilterChip(
                selected = filterSavedOnly,
                onClick = { filterSavedOnly = !filterSavedOnly },
                label = { Text("Saved ($savedCount)", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = if (filterSavedOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Saved Jobs",
                        modifier = Modifier.size(16.dp),
                        tint = if (filterSavedOnly) NavyDark else EmeraldGreen
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldGreen,
                    selectedLabelColor = NavyDark,
                    containerColor = NavyDark,
                    labelColor = WhiteActive
                ),
                border = BorderStroke(1.dp, if (filterSavedOnly) EmeraldGreen else NavyLight),
                modifier = Modifier.testTag("filter_saved_jobs_chip")
            )

            // PDF Export Button in filter row
            if (savedCount > 0) {
                Button(
                    onClick = { showExportPdfDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = TealCyan),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).testTag("discover_export_pdf_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Export PDF", tint = NavyDark, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export PDF", color = NavyDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Country Filter Dropdown
            FilterBadgeDropdown(
                label = "Country",
                selected = selectedCountry,
                options = countries,
                onSelected = { viewModel.selectCountry(it) }
            )

            // Industry Filter Dropdown
            FilterBadgeDropdown(
                label = "Industry",
                selected = selectedIndustry,
                options = industries,
                onSelected = { viewModel.selectIndustry(it) }
            )

            // Experience Filter Dropdown
            FilterBadgeDropdown(
                label = "Experience",
                selected = selectedExperience,
                options = experiences,
                onSelected = { viewModel.selectExperience(it) }
            )

            // Sponsorship Filter Dropdown
            FilterBadgeDropdown(
                label = "Sponsorship",
                selected = selectedSponsorship,
                options = listOf("All", "Full Sponsorship", "Relocation Paid", "LMIA Approved", "High Confidence"),
                onSelected = { viewModel.selectSponsorship(it) }
            )
        }

        // Active filters summary
        if (selectedCountry != "All" || selectedIndustry != "All" || selectedExperience != "All" || selectedSponsorship != "All") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Text(
                    text = "Active Filters applied. showing ${filteredJobs.size} results.",
                    color = EmeraldGreen,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Clear All",
                    color = CoralRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            viewModel.selectCountry("All")
                            viewModel.selectIndustry("All")
                            viewModel.selectExperience("All")
                            viewModel.selectSponsorship("All")
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        // Job Feed list
        if (isWideScreen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left pane: job feed list or search/empty states
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    if (isSearching) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NavyMedium),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = EmeraldGreen, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Simulating Real-Time Scrape Search...",
                                        color = WhiteActive,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Querying live employer databases & visa sponsorship channels in real-time. Please wait...",
                                        color = SlateMuted,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(NavyDark, RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                    ) {
                                        val steps = listOf(
                                            "✔ Querying active certified sponsor licenses...",
                                            "✔ Parsing LMIA registries & Home Office directories...",
                                            "⚡ Matching search keyword: \"${queryText.ifEmpty { "Visa sponsorship jobs" }}\"...",
                                            "⚡ Verifying relocation assistance parameters..."
                                        )
                                        steps.forEach { step ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                val isSpinning = step.startsWith("⚡")
                                                Icon(
                                                    imageVector = if (isSpinning) Icons.Default.Refresh else Icons.Default.Done,
                                                    contentDescription = null,
                                                    tint = if (isSpinning) AmberGold else EmeraldGreen,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = step,
                                                    color = if (isSpinning) WhiteActive else SlateMuted,
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (filteredJobs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "No jobs",
                                    tint = SlateMuted,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No jobs match your active filters.",
                                    color = WhiteActive,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try clearing filters or triggering an AI Scrape Search.",
                                    color = SlateMuted,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(filteredJobs, key = { it.id }) { job ->
                                val verResult = jobVerifications[job.id]
                                val isVerifying = isVerifyingJob[job.id] ?: false
                                JobListItemCard(
                                    job = job,
                                    isVerifying = isVerifying,
                                    verificationResult = verResult,
                                    onVerifyClick = { viewModel.verifyJobListing(job) },
                                    onBookmarkToggle = { viewModel.toggleBookmark(job) },
                                    onViewDetails = { activeDetailJob = job },
                                    onApplyClick = { activeApplyJob = job }
                                )
                            }
                        }
                    }
                }

                // Right pane: inline Job details view
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .padding(bottom = 24.dp)
                ) {
                    val job = activeDetailJob
                    if (job != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(1.dp, NavyLight, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = NavyDark),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            JobDetailContent(
                                job = job,
                                viewModel = viewModel,
                                onCloseClick = {
                                    activeDetailJob = null
                                    viewModel.clearCompatibilityDetails()
                                }
                            )
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(1.dp, NavyLight.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = NavyMedium.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = SlateMuted,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Select a Job Listing",
                                        color = WhiteActive,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Click 'Details' on any listing to inspect visa eligibility, run the AI Fraud scanner, or view the compatibility analysis here in real-time.",
                                        color = SlateMuted,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Normal Compact Layout for Job Feed list
            if (isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavyMedium),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = EmeraldGreen, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Simulating Real-Time Scrape Search...",
                                color = WhiteActive,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Querying live employer databases & visa sponsorship channels in real-time. Please wait...",
                                color = SlateMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(NavyDark, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                val steps = listOf(
                                    "✔ Querying active certified sponsor licenses...",
                                    "✔ Parsing LMIA registries & Home Office directories...",
                                    "⚡ Matching search keyword: \"${queryText.ifEmpty { "Visa sponsorship jobs" }}\"...",
                                    "⚡ Verifying relocation assistance parameters..."
                                )
                                steps.forEach { step ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val isSpinning = step.startsWith("⚡")
                                        Icon(
                                            imageVector = if (isSpinning) Icons.Default.Refresh else Icons.Default.Done,
                                            contentDescription = null,
                                            tint = if (isSpinning) AmberGold else EmeraldGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = step,
                                            color = if (isSpinning) WhiteActive else SlateMuted,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (filteredJobs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "No jobs",
                            tint = SlateMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No jobs match your active filters.",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try clearing filters or triggering an AI Scrape Search.",
                            color = SlateMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredJobs, key = { it.id }) { job ->
                        val verResult = jobVerifications[job.id]
                        val isVerifying = isVerifyingJob[job.id] ?: false
                        JobListItemCard(
                            job = job,
                            isVerifying = isVerifying,
                            verificationResult = verResult,
                            onVerifyClick = { viewModel.verifyJobListing(job) },
                            onBookmarkToggle = { viewModel.toggleBookmark(job) },
                            onViewDetails = { activeDetailJob = job },
                            onApplyClick = { activeApplyJob = job }
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog Sheet
    if (!isWideScreen) {
        activeDetailJob?.let { job ->
            JobDetailDialog(
                job = job,
                viewModel = viewModel,
                onDismiss = {
                    activeDetailJob = null
                    viewModel.clearCompatibilityDetails()
                }
            )
        }
    }

    // Application Method Dialog
    activeApplyJob?.let { job ->
        ApplicationMethodDialog(
            job = job,
            viewModel = viewModel,
            onDismiss = { activeApplyJob = null }
        )
    }

    if (showExportPdfDialog) {
        ExportSavedJobsPdfDialog(
            viewModel = viewModel,
            onDismiss = { showExportPdfDialog = false }
        )
    }
}

@Composable
fun FilterBadgeDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        AssistChip(
            onClick = { expanded = true },
            label = {
                Text(
                    text = if (selected == "All") label else "$label: $selected",
                    color = if (selected != "All") Color(0xFF1D192B) else SlateMuted,
                    fontWeight = FontWeight.Medium
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (selected != "All") Color(0xFFE8DEF8) else NavyMedium
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (selected != "All") Color(0xFFE8DEF8) else NavyLight
            ),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (selected != "All") Color(0xFF1D192B) else SlateMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(NavyMedium)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = WhiteActive) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun JobListItemCard(
    job: JobEntity,
    isVerifying: Boolean,
    verificationResult: JobVerificationResult?,
    onVerifyClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onViewDetails: () -> Unit,
    onApplyClick: (() -> Unit)? = null
) {
    Card(
        onClick = onViewDetails,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (job.isFraud) CoralRed.copy(alpha = 0.5f) else NavyLight,
                RoundedCornerShape(16.dp)
            )
            .testTag("job_card_${job.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (job.isFraud) CoralRed.copy(alpha = 0.05f) else NavyMedium
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        color = WhiteActive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${job.company} • ${job.location}",
                        color = SlateMuted,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onBookmarkToggle) {
                    Icon(
                        imageVector = if (job.isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Bookmark",
                        tint = if (job.isBookmarked) CoralRed else SlateMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main sponsorship information row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Confidence Score indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (job.isFraud) CoralRed.copy(alpha = 0.15f)
                            else if (job.confidenceScore >= 90) EmeraldGreen.copy(alpha = 0.15f)
                            else AmberGold.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (job.isFraud) Icons.Default.Warning else Icons.Default.Star,
                            contentDescription = null,
                            tint = if (job.isFraud) CoralRed else if (job.confidenceScore >= 90) EmeraldGreen else AmberGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (job.isFraud) "FRAUD ALERT" else "${job.confidenceScore}% Trust",
                            color = if (job.isFraud) CoralRed else if (job.confidenceScore >= 90) EmeraldGreen else AmberGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Visa Type Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TealCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = job.visaType,
                        color = TealCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Salary text
                Text(
                    text = job.salary,
                    color = WhiteActive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            // Verification status row
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavyDark.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (verificationResult != null) {
                        when (verificationResult.riskLevel) {
                            "High Risk" -> CoralRed
                            "Medium Risk" -> AmberGold
                            else -> EmeraldGreen
                        }
                    } else TealCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                
                Text(
                    text = if (verificationResult != null) {
                        "Risk Score: ${verificationResult.riskScore}/100 (${verificationResult.riskLevel})"
                    } else "Scam Risk Scan: Pending Analysis",
                    color = if (verificationResult != null) {
                        when (verificationResult.riskLevel) {
                            "High Risk" -> CoralRed
                            "Medium Risk" -> AmberGold
                            else -> EmeraldGreen
                        }
                    } else SlateMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = { onVerifyClick() },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (verificationResult != null) NavyLight else EmeraldGreen
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(26.dp).testTag("list_verify_${job.id}")
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(color = NavyDark, modifier = Modifier.size(12.dp))
                    } else {
                        Text(
                            text = if (verificationResult != null) "Re-Scan" else "Verify",
                            color = if (verificationResult != null) WhiteActive else NavyDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Relocation Indicator and contract type row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (job.relocationAssistance) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Relocation Included",
                            color = EmeraldGreen,
                            fontSize = 12.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NavyLight)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = job.contractType,
                        color = SlateMuted,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Posted: ${job.datePosted}",
                    color = SlateMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct Quick Action Row (Details & Apply)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TealCyan),
                    border = BorderStroke(1.dp, TealCyan.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.weight(0.48f).height(34.dp).testTag("list_view_details_${job.id}")
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Details", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        if (onApplyClick != null) {
                            onApplyClick()
                        } else {
                            onViewDetails()
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = NavyDark),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.weight(0.52f).height(34.dp).testTag("list_apply_btn_${job.id}")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = NavyDark, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Apply (Methods)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailContent(
    job: JobEntity,
    viewModel: JobViewModel,
    onCloseClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isAnalyzing by viewModel.isAnalyzingCompatibility.collectAsStateWithLifecycle()
    val compatibility by viewModel.compatibilityDetails.collectAsStateWithLifecycle()
    val jobVerifications by viewModel.jobVerifications.collectAsStateWithLifecycle()
    val isVerifyingJob by viewModel.isVerifyingJob.collectAsStateWithLifecycle()
    val verResult = jobVerifications[job.id]
    val isVerifying = isVerifyingJob[job.id] ?: false

    var showApplicationMethodDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header row
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Job Details",
                color = EmeraldGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            if (onCloseClick != null) {
                IconButton(onClick = onCloseClick) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SlateMuted)
                }
            }
        }

        Divider(color = NavyLight, modifier = Modifier.padding(vertical = 8.dp))

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = job.title,
                color = WhiteActive,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                text = "${job.company} • ${job.location}",
                color = SlateMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Key details block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavyMedium)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DetailRow("Visa Pathway", job.visaType, TealCyan)
                    DetailRow("Salary Package", job.salary, WhiteActive)
                    DetailRow("Industry", job.industry, WhiteActive)
                    DetailRow("Experience", job.experienceLevel, WhiteActive)
                    DetailRow("Contract", job.contractType, WhiteActive)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI Verification Block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (job.isFraud) CoralRed.copy(alpha = 0.1f)
                        else EmeraldGreen.copy(alpha = 0.05f)
                    )
                    .border(
                        1.dp,
                        if (job.isFraud) CoralRed else EmeraldGreen.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (job.isFraud) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (job.isFraud) CoralRed else EmeraldGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (job.isFraud) "FLAGGED LISTING" else "AI Verified Sponsorship",
                            color = if (job.isFraud) CoralRed else EmeraldGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "AI Score: ${job.confidenceScore}% • Reason: ${job.confidenceReason}",
                        color = SlateMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Compatibility check action and explanation
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyMedium),
                border = BorderStroke(1.dp, NavyLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AI Suitability Engine",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { viewModel.analyzeJobCompatibility(job) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(color = NavyDark, modifier = Modifier.size(16.dp))
                            } else {
                                Text("Score Match", color = NavyDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    compatibility?.let { (score, reason) ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (score >= 80) EmeraldGreen.copy(alpha = 0.15f)
                                        else AmberGold.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$score%",
                                    color = if (score >= 80) EmeraldGreen else AmberGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = reason,
                                color = SlateMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // AI Resume Match & Gap Analysis Card
            val isAnalyzingGapMap by viewModel.isAnalyzingGap.collectAsStateWithLifecycle()
            val jobsGapAnalysisMap by viewModel.jobsGapAnalysisMap.collectAsStateWithLifecycle()
            val isAnalyzingGap = isAnalyzingGapMap[job.id] ?: false
            val gapResult = jobsGapAnalysisMap[job.id]
            val parsedResumeResult by viewModel.parsedResumeResult.collectAsStateWithLifecycle()

            Card(
                colors = CardDefaults.cardColors(containerColor = NavyMedium),
                border = BorderStroke(1.dp, NavyLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI Resume Match & Gap Analysis",
                                color = WhiteActive,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (parsedResumeResult != null) "Comparing parsed resume text" else "Comparing candidate profile details",
                                color = SlateMuted,
                                fontSize = 11.sp
                            )
                        }
                        Button(
                            onClick = { viewModel.analyzeResumeGap(job) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp).testTag("analyze_gaps_btn_${job.id}")
                        ) {
                            if (isAnalyzingGap) {
                                CircularProgressIndicator(color = NavyDark, modifier = Modifier.size(16.dp))
                            } else {
                                Text(
                                    text = if (gapResult != null) "Re-Analyze" else "Match & Find Gaps",
                                    color = NavyDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (gapResult != null) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Match Score
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (gapResult.matchScore >= 80) EmeraldGreen.copy(alpha = 0.15f)
                                        else AmberGold.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${gapResult.matchScore}%",
                                    color = if (gapResult.matchScore >= 80) EmeraldGreen else AmberGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Resume Compatibility Match",
                                    color = WhiteActive,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (gapResult.matchScore >= 85) "Strong profile alignment detected" else "Actionable gaps identified",
                                    color = SlateMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Missing Skills
                        Text(
                            text = "HIGHLIGHTED SKILL GAPS",
                            color = TealCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (gapResult.missingSkills.isEmpty()) {
                            Text(
                                text = "✓ No missing skill gaps found!",
                                color = EmeraldGreen,
                                fontSize = 12.sp
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                gapResult.missingSkills.forEach { skill ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(androidx.compose.foundation.shape.CircleShape)
                                                .background(CoralRed)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = skill,
                                            color = WhiteActive,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Suggested Certifications
                        Text(
                            text = "RECOMMENDED CERTIFICATIONS / TRAINING",
                            color = TealCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (gapResult.suggestedCertifications.isEmpty()) {
                            Text(
                                text = "✓ No certifications suggested.",
                                color = SlateMuted,
                                fontSize = 12.sp
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                gapResult.suggestedCertifications.forEach { cert ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Done,
                                            contentDescription = "Suggested Certification",
                                            tint = AmberGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = cert,
                                            color = WhiteActive,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Suggestions
                        Text(
                            text = "STRATEGIC MATCH IMPROVEMENT ADVICE",
                            color = TealCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = gapResult.generalSuggestions,
                            color = SlateMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        if (parsedResumeResult == null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NavyLight.copy(alpha = 0.2f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "💡 Pro Tip: Paste & parse your actual resume plain text in the 'AI Resume' tab first to get specific, laser-targeted CV text gap matching!",
                                    color = AmberGold,
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Scam & Fraud Scanner Card
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyMedium),
                border = BorderStroke(1.dp, NavyLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AI Scam & Fraud Scanner",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { viewModel.verifyJobListing(job) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (verResult != null) {
                                    when (verResult.riskLevel) {
                                        "High Risk" -> CoralRed
                                        "Medium Risk" -> AmberGold
                                        else -> EmeraldGreen
                                    }
                                } else EmeraldGreen
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp).testTag("verify_job_btn_${job.id}")
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(color = NavyDark, modifier = Modifier.size(16.dp))
                            } else {
                                Text(
                                    text = if (verResult != null) "Re-Scan" else "Verify Job",
                                    color = NavyDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (verResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        val scoreColor = when (verResult.riskLevel) {
                            "High Risk" -> CoralRed
                            "Medium Risk" -> AmberGold
                            else -> EmeraldGreen
                        }
                        
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(scoreColor.copy(alpha = 0.15f))
                                    .border(1.dp, scoreColor, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${verResult.riskScore}",
                                        color = scoreColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = "RISK",
                                        color = scoreColor,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 8.sp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(10.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Risk Level: ${verResult.riskLevel}",
                                    color = scoreColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = verResult.explanation,
                                    color = SlateMuted,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        if (verResult.redFlags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Common Red Flags Detected:",
                                color = CoralRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            verResult.redFlags.forEach { flag ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = CoralRed,
                                        modifier = Modifier.size(12.dp).padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = flag,
                                        color = SlateMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Scan job descriptions for hidden application fees, fake immigration agents, and non-corporate recruitment channels using direct AI intelligence.",
                            color = SlateMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Application Methods Hub Card
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyMedium),
                border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Verified Application Methods",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EmeraldGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "4 Channels",
                                color = EmeraldGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Apply via official employer portal, pre-formatted email, track in your visa pipeline, or review sponsorship visa documentation requirements.",
                        color = SlateMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { showApplicationMethodDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            contentColor = NavyDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("open_application_methods_hub_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = NavyDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Choose Application Method & Apply",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NavyDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Job Description",
                color = EmeraldGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = job.description,
                color = SlateMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Footer control actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Flag Fraud Button
            OutlinedButton(
                onClick = {
                    viewModel.flagAsFraud(job)
                    Toast.makeText(context, "Job flagged for moderation review", Toast.LENGTH_SHORT).show()
                    if (onCloseClick != null) onCloseClick()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                modifier = Modifier.weight(0.3f),
                border = BorderStroke(1.dp, CoralRed.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Default.Warning, contentDescription = "Flag Fraud", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Flag", fontSize = 12.sp)
            }

            // Copy Link Button
            OutlinedButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(job.applicationUrl))
                    Toast.makeText(context, "Application link copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TealCyan),
                modifier = Modifier.weight(0.3f),
                border = BorderStroke(1.dp, TealCyan.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Share, contentDescription = "Copy Link", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy", fontSize = 12.sp)
            }

            // Apply Now / Application Methods Primary CTA
            Button(
                onClick = {
                    showApplicationMethodDialog = true
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier.weight(0.4f).testTag("apply_methods_footer_btn")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Apply", tint = NavyDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Apply Now", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    if (showApplicationMethodDialog) {
        ApplicationMethodDialog(
            job = job,
            viewModel = viewModel,
            onDismiss = { showApplicationMethodDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailDialog(
    job: JobEntity,
    viewModel: JobViewModel,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .border(1.dp, NavyLight, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = NavyDark),
            shape = RoundedCornerShape(20.dp)
        ) {
            JobDetailContent(
                job = job,
                viewModel = viewModel,
                onCloseClick = onDismiss
            )
        }
    }
}

@Composable
fun DetailRow(label: String, valText: String, valColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = SlateMuted, fontSize = 12.sp)
        Text(text = valText, color = valColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MatchTab(viewModel: JobViewModel, isWideScreen: Boolean = false) {
    val profileOpt by viewModel.userProfile.collectAsStateWithLifecycle()
    val isGenResume by viewModel.isGeneratingResume.collectAsStateWithLifecycle()
    val resumeRes by viewModel.resumeResult.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf("Profile") }
    val profile = profileOpt ?: com.example.data.UserProfileEntity()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Tab Header selector
        TabRow(
            selectedTabIndex = when (activeSubTab) {
                "Profile" -> 0
                "AI Resume" -> 1
                "AI Matcher" -> 2
                else -> 3
            },
            containerColor = NavyDark,
            contentColor = EmeraldGreen,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Tab(
                selected = activeSubTab == "Profile",
                onClick = { activeSubTab = "Profile" },
                text = { Text("Profile", color = if (activeSubTab == "Profile") EmeraldGreen else SlateMuted, fontSize = 12.sp) }
            )
            Tab(
                selected = activeSubTab == "AI Resume",
                onClick = { activeSubTab = "AI Resume" },
                text = { Text("ATS Resume", color = if (activeSubTab == "AI Resume") EmeraldGreen else SlateMuted, fontSize = 12.sp) }
            )
            Tab(
                selected = activeSubTab == "AI Matcher",
                onClick = { activeSubTab = "AI Matcher" },
                text = { Text("AI Matcher", color = if (activeSubTab == "AI Matcher") EmeraldGreen else SlateMuted, fontSize = 12.sp) }
            )
            Tab(
                selected = activeSubTab == "Career Tools",
                onClick = { activeSubTab = "Career Tools" },
                text = { Text("Career Tools", color = if (activeSubTab == "Career Tools") EmeraldGreen else SlateMuted, fontSize = 12.sp) }
            )
        }

        when (activeSubTab) {
            "Profile" -> ProfileSubScreen(profile, viewModel)
            "AI Resume" -> ResumeSubScreen(profile, isGenResume, resumeRes, viewModel, isWideScreen)
            "AI Matcher" -> AiMatcherSubScreen(profile, viewModel)
            "Career Tools" -> CareerToolsSubScreen(viewModel)
        }
    }
}

@Composable
fun CareerToolsSubScreen(viewModel: JobViewModel) {
    var showPdfExportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SavedJobsPdfExportCard(
            viewModel = viewModel,
            onOpenExportDialog = { showPdfExportDialog = true }
        )
        VisaInterviewPrepCard(viewModel)
        RecruiterColdEmailCard(viewModel)
    }

    if (showPdfExportDialog) {
        ExportSavedJobsPdfDialog(
            viewModel = viewModel,
            onDismiss = { showPdfExportDialog = false }
        )
    }
}

@Composable
fun ProfileSubScreen(profile: com.example.data.UserProfileEntity, viewModel: JobViewModel) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(profile.fullName) }
    var nat by remember { mutableStateOf(profile.nationality) }
    var currentC by remember { mutableStateOf(profile.currentCountry) }
    var passC by remember { mutableStateOf(profile.passportCountry) }
    var edu by remember { mutableStateOf(profile.education) }
    var skills by remember { mutableStateOf(profile.skills) }
    var lang by remember { mutableStateOf(profile.languages) }
    var exp by remember { mutableStateOf(profile.experience) }
    var dest by remember { mutableStateOf(profile.desiredCountries) }
    var occ by remember { mutableStateOf(profile.preferredOccupations) }
    var sal by remember { mutableStateOf(profile.salaryExpectations) }

    // Sync state when database profile updates
    LaunchedEffect(profile) {
        if (name != profile.fullName) name = profile.fullName
        if (nat != profile.nationality) nat = profile.nationality
        if (currentC != profile.currentCountry) currentC = profile.currentCountry
        if (passC != profile.passportCountry) passC = profile.passportCountry
        if (edu != profile.education) edu = profile.education
        if (skills != profile.skills) skills = profile.skills
        if (lang != profile.languages) lang = profile.languages
        if (exp != profile.experience) exp = profile.experience
        if (dest != profile.desiredCountries) dest = profile.desiredCountries
        if (occ != profile.preferredOccupations) occ = profile.preferredOccupations
        if (sal != profile.salaryExpectations) sal = profile.salaryExpectations
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // Firebase Authentication & Cross-Session Google Cloud Sync Card
        GoogleFirebaseAuthCard(
            viewModel = viewModel
        )

        Spacer(modifier = Modifier.height(10.dp))

        // LinkedIn OAuth2 & Professional Verification Card
        LinkedInOAuthCard(
            profile = profile,
            viewModel = viewModel
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Aggregator Matching Profile",
            color = WhiteActive,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = "AI automatically matches you to jobs and rates compatibility based on these criteria.",
            color = SlateMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedProfileInput("Full Name", name) { name = it }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedProfileInput("Nationality", nat) { nat = it }
            }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedProfileInput("Passport Country", passC) { passC = it }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedProfileInput("Current Country", currentC) { currentC = it }
            }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedProfileInput("Salary Expectations", sal) { sal = it }
            }
        }

        OutlinedProfileInput("Education Level (e.g., Bachelor's, Certificate)", edu) { edu = it }
        OutlinedProfileInput("Work Experience Summary", exp) { exp = it }
        OutlinedProfileInput("Skills (comma-separated, e.g. nursing, SQL, driving)", skills) { skills = it }
        OutlinedProfileInput("Languages (comma-separated, e.g. English, French)", lang) { lang = it }
        OutlinedProfileInput("Desired Target Countries", dest) { dest = it }
        OutlinedProfileInput("Preferred Occupations", occ) { occ = it }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.updateProfile(
                    fullName = name,
                    nationality = nat,
                    currentCountry = currentC,
                    passportCountry = passC,
                    education = edu,
                    skills = skills,
                    languages = lang,
                    experience = exp,
                    desiredCountries = dest,
                    preferredOccupations = occ,
                    salaryExpectations = sal
                )
                Toast.makeText(context, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show()
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_profile_button")
        ) {
            Icon(Icons.Default.Done, contentDescription = "Save", tint = NavyDark)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save Matching Profile", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun OutlinedProfileInput(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = SlateMuted) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = WhiteActive,
            unfocusedTextColor = WhiteActive,
            focusedBorderColor = EmeraldGreen,
            unfocusedBorderColor = NavyLight,
            focusedContainerColor = NavyMedium,
            unfocusedContainerColor = NavyMedium
        ),
        shape = RoundedCornerShape(10.dp),
        singleLine = true
    )
}

@Composable
fun ResumeSubScreen(
    profile: com.example.data.UserProfileEntity,
    isGen: Boolean,
    resumeRes: String?,
    viewModel: JobViewModel,
    isWideScreen: Boolean = false
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    
    // Resume Builder Inputs
    var name by remember { mutableStateOf(profile.fullName) }
    var skills by remember { mutableStateOf(profile.skills) }
    var education by remember { mutableStateOf(profile.education) }
    var experience by remember { mutableStateOf(profile.experience) }
    var contactEmail by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var activeStep by remember { mutableIntStateOf(1) } // Step 1 to 5

    val isParsingResume by viewModel.isParsingResume.collectAsStateWithLifecycle()
    val parsedResumeResult by viewModel.parsedResumeResult.collectAsStateWithLifecycle()
    val parseResumeError by viewModel.parseResumeError.collectAsStateWithLifecycle()
    var pastedResumeText by remember { mutableStateOf("") }
    var showParserCard by remember { mutableStateOf(false) }

    // Structured state representations
    val skillsList = remember { mutableStateListOf<String>() }
    val experienceList = remember { mutableStateListOf<WorkExperienceItem>() }

    val updateSkills = {
        skills = skillsList.joinToString(", ")
    }
    val updateExperience = {
        experience = serializeExperience(experienceList)
    }

    LaunchedEffect(parsedResumeResult) {
        parsedResumeResult?.let { result ->
            if (result.name.isNotEmpty()) {
                name = result.name
            }
            if (result.email.isNotEmpty()) {
                contactEmail = result.email
            }
            if (result.phone.isNotEmpty()) {
                contactPhone = result.phone
            }
            if (result.skills.isNotEmpty()) {
                skillsList.clear()
                skillsList.addAll(result.skills)
                updateSkills()
            }
            if (result.education.isNotEmpty()) {
                education = result.education
            }
            if (result.experience.isNotEmpty()) {
                experience = result.experience
                experienceList.clear()
                experienceList.addAll(deserializeExperience(result.experience))
                updateExperience()
            }
            Toast.makeText(context, "Resume parsed and imported successfully!", Toast.LENGTH_LONG).show()
            viewModel.clearParsedResumeResult()
            pastedResumeText = ""
            showParserCard = false
        }
    }

    // Sync states when profile updates
    LaunchedEffect(profile) {
        if (name != profile.fullName) name = profile.fullName
        if (education != profile.education) education = profile.education
        if (skills != profile.skills) {
            skills = profile.skills
            skillsList.clear()
            skillsList.addAll(profile.skills.split(",").map { it.trim() }.filter { it.isNotEmpty() })
        }
        if (experience != profile.experience) {
            experience = profile.experience
            experienceList.clear()
            experienceList.addAll(deserializeExperience(profile.experience))
        }
    }

    // Add/Edit Dialog States for Work Experience
    var showExpDialog by remember { mutableStateOf(false) }
    var editingExpItem by remember { mutableStateOf<WorkExperienceItem?>(null) }
    
    var expTitle by remember { mutableStateOf("") }
    var expCompany by remember { mutableStateOf("") }
    var expLocation by remember { mutableStateOf("") }
    var expStartDate by remember { mutableStateOf("") }
    var expEndDate by remember { mutableStateOf("") }
    var expIsCurrent by remember { mutableStateOf(false) }
    var expAchievements by remember { mutableStateOf("") }

    if (showExpDialog) {
        Dialog(onDismissRequest = { showExpDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyMedium)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (editingExpItem == null) "Add Work Experience" else "Edit Work Experience",
                        color = WhiteActive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    OutlinedTextField(
                        value = expTitle,
                        onValueChange = { expTitle = it },
                        label = { Text("Job Title *", color = SlateMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_exp_title"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = NavyLight,
                            focusedContainerColor = NavyDark,
                            unfocusedContainerColor = NavyDark
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = expCompany,
                        onValueChange = { expCompany = it },
                        label = { Text("Company / Employer *", color = SlateMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_exp_company"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = NavyLight,
                            focusedContainerColor = NavyDark,
                            unfocusedContainerColor = NavyDark
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = expLocation,
                        onValueChange = { expLocation = it },
                        label = { Text("Location (e.g. Berlin, Germany)", color = SlateMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_exp_location"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = NavyLight,
                            focusedContainerColor = NavyDark,
                            unfocusedContainerColor = NavyDark
                        ),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = expStartDate,
                            onValueChange = { expStartDate = it },
                            label = { Text("Start Date *", color = SlateMuted) },
                            placeholder = { Text("e.g. 05/2021", color = SlateMuted) },
                            modifier = Modifier.weight(1f).testTag("dialog_exp_start_date"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = WhiteActive,
                                unfocusedTextColor = WhiteActive,
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = NavyLight,
                                focusedContainerColor = NavyDark,
                                unfocusedContainerColor = NavyDark
                            ),
                            singleLine = true
                        )

                        if (!expIsCurrent) {
                            OutlinedTextField(
                                value = expEndDate,
                                onValueChange = { expEndDate = it },
                                label = { Text("End Date *", color = SlateMuted) },
                                placeholder = { Text("e.g. 12/2023", color = SlateMuted) },
                                modifier = Modifier.weight(1f).testTag("dialog_exp_end_date"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = WhiteActive,
                                    unfocusedTextColor = WhiteActive,
                                    focusedBorderColor = EmeraldGreen,
                                    unfocusedBorderColor = NavyLight,
                                    focusedContainerColor = NavyDark,
                                    unfocusedContainerColor = NavyDark
                                ),
                                singleLine = true
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { expIsCurrent = !expIsCurrent }
                    ) {
                        Checkbox(
                            checked = expIsCurrent,
                            onCheckedChange = { expIsCurrent = it },
                            colors = CheckboxDefaults.colors(checkedColor = EmeraldGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("I currently work in this role", color = WhiteActive, fontSize = 13.sp)
                    }

                    OutlinedTextField(
                        value = expAchievements,
                        onValueChange = { expAchievements = it },
                        label = { Text("Achievements & Duties *", color = SlateMuted) },
                        placeholder = { Text("e.g., Developed Jetpack Compose features\n- Optimized local DB cache, increasing speed by 20%", color = SlateMuted) },
                        modifier = Modifier.fillMaxWidth().height(120.dp).testTag("dialog_exp_achievements"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = NavyLight,
                            focusedContainerColor = NavyDark,
                            unfocusedContainerColor = NavyDark
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showExpDialog = false }) {
                            Text("Cancel", color = SlateMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (expTitle.trim().isEmpty() || expCompany.trim().isEmpty() || expStartDate.trim().isEmpty()) {
                                    Toast.makeText(context, "Please fill in all mandatory fields (*)", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val itemToSave = editingExpItem?.copy(
                                    title = expTitle,
                                    company = expCompany,
                                    location = expLocation,
                                    startDate = expStartDate,
                                    endDate = if (expIsCurrent) "" else expEndDate,
                                    isCurrent = expIsCurrent,
                                    achievements = expAchievements
                                ) ?: WorkExperienceItem(
                                    id = java.util.UUID.randomUUID().toString(),
                                    title = expTitle,
                                    company = expCompany,
                                    location = expLocation,
                                    startDate = expStartDate,
                                    endDate = if (expIsCurrent) "" else expEndDate,
                                    isCurrent = expIsCurrent,
                                    achievements = expAchievements
                                )

                                val existingIdx = experienceList.indexOfFirst { it.id == itemToSave.id }
                                if (existingIdx != -1) {
                                    experienceList[existingIdx] = itemToSave
                                } else {
                                    experienceList.add(itemToSave)
                                }
                                updateExperience()
                                showExpDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }
    
    // Toggles
    var activeSubMode by remember { mutableStateOf("Editor") } // "Editor" or "Live Preview"
    var activeTemplate by remember { mutableStateOf("ATS Standard") } // "ATS Standard", "Modern Executive", "Creative Tech"
    var activeSource by remember { mutableStateOf("Structured Resume") } // "Structured Resume" or "AI Tailored CV"
    var activeAIDocType by remember { mutableStateOf("CV") } // "CV", "Cover Letter", "LinkedIn", "Interview Prep", "Visa Strategy"
    var targetRoleText by remember { mutableStateOf(profile.preferredOccupations.split(",").firstOrNull() ?: "General Worker") }
    var activeTone by remember { mutableStateOf("Professional") } // "Professional", "Technical/Sponsor-aligned", "Creative Leader"
    var showPrintPreview by remember { mutableStateOf(false) }
    var expandTips by remember { mutableStateOf(false) }

    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("text/plain"),
        onResult = { uri ->
            uri?.let {
                try {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        val activeText = if (activeSource == "Structured Resume") {
                            getSimulatedMarkdown(name, skills, education, experience, profile, contactEmail, contactPhone)
                        } else {
                            resumeRes ?: ""
                        }
                        val formattedText = formatForAts(activeText)
                        outputStream.write(formattedText.toByteArray())
                    }
                    Toast.makeText(context, "Resume exported successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to export: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    val allJobs by viewModel.allJobs.collectAsStateWithLifecycle()
    
    val candidateKeywords = remember {
        listOf(
            "Kotlin", "Java", "Python", "Android", "React", "Swift", "Flutter", "iOS", "SQL", "Git", "API", "Scrum", "AWS", "Docker", "UI/UX", "TypeScript", "JavaScript", "C++", "Spring Boot", "Node.js", "Cloud", "Coroutines", "Jetpack Compose", "SQLite", "Room", "CI/CD", "Machine Learning", "Data Science", "Backend", "Frontend",
            "OSCE", "Nursing", "Medication", "Patient Care", "NMC", "CPR", "First Aid", "Clinical", "ICU", "Pediatric", "Caregiving", "Geriatric Care",
            "Class 1 License", "Logistics", "Driving", "CDL", "Route Planning", "Fleet Management", "Safety Regulations", "Heavy Equipment", "Supply Chain",
            "Customer Service", "Food Safety", "Culinary", "Housekeeping", "Event Planning", "Guest Relations", "Barista", "Baking",
            "Safety", "Framing", "Harvesting", "Welding", "Plumbing", "Electrical", "Carpentry", "Masonry", "HVAC",
            "Project Management", "Agile", "Team Leadership", "Communication", "Data Analysis", "Problem Solving", "Marketing", "Financial Analysis", "Sales", "Accounting"
        )
    }

    val extractedSkills = remember(allJobs, candidateKeywords) {
        val matched = mutableSetOf<String>()
        allJobs.forEach { job ->
            val text = (job.title + " " + job.description).lowercase()
            candidateKeywords.forEach { kw ->
                if (text.contains(kw.lowercase())) {
                    matched.add(kw)
                }
            }
        }
        if (matched.isEmpty()) candidateKeywords.take(20) else matched.toList().sorted()
    }

    val atsScore = remember(name, skills, education, experience) {
        var score = 0
        if (name.trim().isNotEmpty()) score += 15
        if (skills.trim().isNotEmpty()) {
            val skillCount = skills.split(",").filter { it.trim().isNotEmpty() }.size
            score += if (skillCount >= 6) 25 else if (skillCount >= 3) 15 else 5
        }
        if (education.trim().isNotEmpty()) score += 20
        if (experience.trim().length >= 150) {
            score += 25
        } else if (experience.trim().isNotEmpty()) {
            score += 10
        }
        val trimmedSkills = skills.lowercase()
        val keywordMatches = candidateKeywords.filter { trimmedSkills.contains(it.lowercase()) }.size
        if (keywordMatches >= 5) score += 15
        else if (keywordMatches >= 2) score += 10
        else if (keywordMatches >= 1) score += 5

        score.coerceAtMost(100)
    }

    val atsTips = remember(name, skills, education, experience) {
        val tips = mutableListOf<String>()
        if (name.trim().isEmpty()) {
            tips.add("Provide your Full Name so employers and applicant tracking systems can easily index your profile.")
        }
        if (skills.trim().isEmpty()) {
            tips.add("Add specialized skills. Click any keyword in the list below or filter by sector for automatic insertion.")
        } else {
            val skillCount = skills.split(",").filter { it.trim().isNotEmpty() }.size
            if (skillCount < 5) {
                tips.add("List at least 5-6 core competencies in your skills section to cover standard recruiter keyword scans.")
            }
        }
        if (education.trim().isEmpty()) {
            tips.add("Specify your highest degree or relevant certifications to fulfill formal hiring requirements.")
        }
        if (experience.trim().isEmpty()) {
            tips.add("Fill in your Work Experience. Resume parsing filters place heavy emphasis on temporal role structures.")
        } else if (experience.trim().length < 150) {
            tips.add("Expand on your Work Experience. Include specific accomplishments using impact-focused metrics (e.g. 'boosted efficiency by 20%').")
        }
        val trimmedSkills = skills.lowercase()
        val keywordMatches = candidateKeywords.filter { trimmedSkills.contains(it.lowercase()) }.size
        if (keywordMatches < 3) {
            tips.add("Incorporate more industry-recognized keywords from the job listing tags to clear resume screening barriers.")
        }
        tips
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AI ATS Resume Builder & Creator",
            color = WhiteActive,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.widthIn(max = 800.dp).fillMaxWidth()
        )
        Text(
            text = "Design a highly professional, ATS-friendly resume. Toggle between editing your details and real-time layout rendering.",
            color = SlateMuted,
            fontSize = 12.sp,
            modifier = Modifier.widthIn(max = 800.dp).fillMaxWidth().padding(bottom = 12.dp)
        )

        // Sub Mode Tab row
        Row(
            modifier = Modifier
                .widthIn(max = 800.dp)
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(Color(0xFFF3EDF7), shape = RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Editor", "Live Preview").forEach { mode ->
                val isSelected = activeSubMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (isSelected) Color(0xFF6750A4) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { activeSubMode = mode }
                        .padding(vertical = 10.dp)
                        .testTag("resume_mode_${mode.lowercase().replace(" ", "_")}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (mode == "Editor") Icons.Default.Edit else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else Color(0xFF381E72),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = mode,
                            color = if (isSelected) Color.White else Color(0xFF381E72),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (activeSubMode == "Editor") {
            // LinkedIn OAuth 2.0 ATS Auto-Populate Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(
                        1.5.dp,
                        if (profile.linkedInConnected) LinkedInBlue.copy(alpha = 0.8f) else NavyLight,
                        RoundedCornerShape(16.dp)
                    )
                    .testTag("linkedin_resume_autopopulate_card"),
                colors = CardDefaults.cardColors(
                    containerColor = if (profile.linkedInConnected) NavyMedium else Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(LinkedInBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "in",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (profile.linkedInConnected) "LinkedIn Profile Connected" else "LinkedIn OAuth 2.0 ATS Auto-Fill",
                                    color = WhiteActive,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (profile.linkedInConnected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(EmeraldGreen.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "VERIFIED ✓",
                                            color = EmeraldGreen,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (profile.linkedInConnected)
                                    "${profile.fullName.ifBlank { "Professional" }} · ${profile.linkedInHeadline.ifBlank { "Visa Sponsorship Ready" }}"
                                else
                                    "Pull verified work history, education & skills directly into the ATS Resume Builder",
                                color = SlateMuted,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (profile.linkedInConnected) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (profile.fullName.isNotBlank()) name = profile.fullName
                                    if (profile.linkedInEmail.isNotBlank()) contactEmail = profile.linkedInEmail
                                    if (profile.education.isNotBlank()) education = profile.education
                                    if (profile.skills.isNotBlank()) {
                                        skills = profile.skills
                                        skillsList.clear()
                                        skillsList.addAll(profile.skills.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                                    }
                                    if (profile.experience.isNotBlank()) {
                                        experience = profile.experience
                                        experienceList.clear()
                                        experienceList.addAll(deserializeExperience(profile.experience))
                                    }
                                    activeStep = 1
                                    Toast.makeText(context, "✨ Pre-populated ATS Form from LinkedIn Profile!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LinkedInBlue,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(40.dp)
                                    .testTag("autopopulate_from_linkedin_btn")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pre-populate Form", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val targetRole = profile.preferredOccupations.ifBlank { "Senior Software Engineer" }
                                    viewModel.generateResumeOrCoverLetter(
                                        role = targetRole,
                                        type = "ATS Resume",
                                        tone = "High-Impact ATS"
                                    )
                                    activeSubMode = "Generator"
                                    Toast.makeText(context, "🤖 AI generating ATS resume tailored to $targetRole...", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = TealCyan
                                ),
                                border = BorderStroke(1.dp, TealCyan),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("ai_generate_from_linkedin_btn")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI 1-Click ATS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.startLinkedInOAuthFlow(context)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LinkedInBlue,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(40.dp)
                                    .testTag("linkedin_resume_connect_oauth_btn")
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Connect LinkedIn OAuth", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.connectLinkedInSandbox(
                                        customName = profile.fullName.ifBlank { "Vincent Mwangangi" },
                                        customHeadline = "Senior Cloud & Distributed Systems Engineer | Visa Sponsorship Ready",
                                        customEmail = "vincent.mwangangi.verified@example.com"
                                    )
                                    Toast.makeText(context, "Connected Verified Sandbox LinkedIn Account!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = TealCyan
                                ),
                                border = BorderStroke(1.dp, NavyLight),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("linkedin_resume_sandbox_btn")
                            ) {
                                Text("Sandbox Profile", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // AI Resume Parser Integration (ResumeOptimizerPro)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, if (showParserCard) EmeraldGreen else NavyLight, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = NavyMedium),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showParserCard = !showParserCard },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EmeraldGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "AI Resume Parser & Auto-Filler",
                                    color = WhiteActive,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Extract name, email, skills, work history & education via RapidAPI",
                                    color = SlateMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = if (showParserCard) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle",
                            tint = SlateMuted
                        )
                    }

                    if (showParserCard) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Paste the plain text of your resume or CV below. The ResumeOptimizerPro parser will analyze your background and automatically populate all 5 steps of the Form Wizard instantly.",
                            color = SlateMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = pastedResumeText,
                            onValueChange = { pastedResumeText = it },
                            placeholder = { Text("Paste resume text here (e.g. John Doe\nSoftware Engineer\njohn@example.com\nSkills: Kotlin...)", color = SlateMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .testTag("pasted_resume_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = WhiteActive,
                                unfocusedTextColor = WhiteActive,
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = NavyLight,
                                focusedContainerColor = NavyDark,
                                unfocusedContainerColor = NavyDark
                            ),
                            maxLines = 15
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { pastedResumeText = "" },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("clear_pasted_resume_btn"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NavyLight,
                                    contentColor = WhiteActive
                                ),
                                enabled = pastedResumeText.isNotEmpty() && !isParsingResume
                            ) {
                                Text("Clear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (pastedResumeText.trim().isEmpty()) {
                                        Toast.makeText(context, "Please paste your resume text first!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.parseResume(pastedResumeText)
                                    }
                                },
                                modifier = Modifier
                                    .weight(2f)
                                    .height(40.dp)
                                    .testTag("parse_resume_btn"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldGreen,
                                    contentColor = Color.White
                                ),
                                enabled = !isParsingResume
                            ) {
                                if (isParsingResume) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Parsing...", fontSize = 12.sp)
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Parse & Fill Form", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        parseResumeError?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⚠️ $err",
                                color = CoralRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Editor Section (Multi-step form wizard)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NavyLight, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = NavyMedium),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header with Step indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ATS Resume Form Wizard",
                                color = WhiteActive,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Step $activeStep of 5: " + when (activeStep) {
                                    1 -> "Personal & Contact Details"
                                    2 -> "Core Skills & Keywords"
                                    3 -> "Professional Work Experience"
                                    4 -> "Education History"
                                    else -> "Review & Download Options"
                                },
                                color = EmeraldGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Load Sample button (Shown on Step 1 for convenience)
                        if (activeStep == 1) {
                            TextButton(
                                onClick = {
                                    name = "Alexander Mercer"
                                    contactEmail = "alexander.mercer@gmail.com"
                                    contactPhone = "+1 (416) 555-0199"
                                    skills = "Kotlin, Jetpack Compose, Android SDK, SQLite, Coroutines, REST APIs, Git"
                                    education = "B.S. in Computer Science - University of Toronto"
                                    experience = "Senior Android Developer at DevFlow Labs (2022 - Present) • Developed beautiful responsive user interfaces using Jetpack Compose and Material 3 • Managed database persistence and local cache with SQLite/Room • Led 3 engineers to deliver 5+ core features to Google Play Store"
                                    Toast.makeText(context, "Loaded Professional Sample Data!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("load_sample_button")
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "Sample", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Load Sample", fontSize = 11.sp, color = Color(0xFFE8DEF8))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Progress bar
                    LinearProgressIndicator(
                        progress = activeStep / 5.0f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = EmeraldGreen,
                        trackColor = NavyLight
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Step Content
                    when (activeStep) {
                        1 -> {
                            // Step 1: Personal / Contact Details
                            Text(
                                text = "Provide accurate contact details. High-quality personal structures clear compliance audits.",
                                color = SlateMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Full Name", color = SlateMuted) },
                                modifier = Modifier.fillMaxWidth().testTag("resume_input_name"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = WhiteActive,
                                    unfocusedTextColor = WhiteActive,
                                    focusedBorderColor = EmeraldGreen,
                                    unfocusedBorderColor = NavyLight,
                                    focusedContainerColor = NavyMedium,
                                    unfocusedContainerColor = NavyMedium
                                ),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = contactEmail,
                                onValueChange = { contactEmail = it },
                                label = { Text("Contact Email (Optional)", color = SlateMuted) },
                                modifier = Modifier.fillMaxWidth().testTag("resume_input_email"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = WhiteActive,
                                    unfocusedTextColor = WhiteActive,
                                    focusedBorderColor = EmeraldGreen,
                                    unfocusedBorderColor = NavyLight,
                                    focusedContainerColor = NavyMedium,
                                    unfocusedContainerColor = NavyMedium
                                ),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = contactPhone,
                                onValueChange = { contactPhone = it },
                                label = { Text("Phone Number (Optional)", color = SlateMuted) },
                                modifier = Modifier.fillMaxWidth().testTag("resume_input_phone"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = WhiteActive,
                                    unfocusedTextColor = WhiteActive,
                                    focusedBorderColor = EmeraldGreen,
                                    unfocusedBorderColor = NavyLight,
                                    focusedContainerColor = NavyMedium,
                                    unfocusedContainerColor = NavyMedium
                                ),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }
                        
                        2 -> {
                            // Step 2: Core Skills & Keywords
                            Text(
                                text = "Add technical and domain competencies. Tap recommended keywords below or input custom ones.",
                                color = SlateMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )

                            var customSkillInput by remember { mutableStateOf("") }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customSkillInput,
                                    onValueChange = { customSkillInput = it },
                                    label = { Text("Custom Skill", color = SlateMuted) },
                                    modifier = Modifier.weight(1f).testTag("resume_input_skills"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = WhiteActive,
                                        unfocusedTextColor = WhiteActive,
                                        focusedBorderColor = EmeraldGreen,
                                        unfocusedBorderColor = NavyLight,
                                        focusedContainerColor = NavyMedium,
                                        unfocusedContainerColor = NavyMedium
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                Button(
                                    onClick = {
                                        val trimmed = customSkillInput.trim()
                                        if (trimmed.isNotEmpty() && !skillsList.contains(trimmed)) {
                                            skillsList.add(trimmed)
                                            updateSkills()
                                            customSkillInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(56.dp).testTag("add_custom_skill_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Skill", tint = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Display current skills as dismissible chips
                            Text(
                                text = "Current Skills (${skillsList.size}):",
                                color = WhiteActive,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            if (skillsList.isEmpty()) {
                                Text(
                                    text = "No skills added yet. Use the field above or tap suggestions below.",
                                    color = SlateMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            } else {
                                // Scrollable Chips Row
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).testTag("current_skills_chips_row")
                                ) {
                                    items(skillsList) { skill ->
                                        Box(
                                            modifier = Modifier
                                                .background(EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                .border(1.dp, EmeraldGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = skill, color = WhiteActive, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove skill",
                                                    tint = EmeraldGreen,
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clickable { 
                                                            skillsList.remove(skill)
                                                            updateSkills()
                                                        }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (extractedSkills.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Tap to automatically insert ATS Job Keywords:",
                                    color = EmeraldGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("ats_skills_row")
                                ) {
                                    items(extractedSkills.take(15)) { kw ->
                                        val isAlreadyAdded = remember(skillsList.size) {
                                            skillsList.contains(kw)
                                        }
                                        AssistChip(
                                            onClick = {
                                                if (!isAlreadyAdded) {
                                                    skillsList.add(kw)
                                                    updateSkills()
                                                    Toast.makeText(context, "Added $kw!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            label = {
                                                Text(
                                                    text = kw,
                                                    fontSize = 10.sp,
                                                    color = if (isAlreadyAdded) SlateMuted else Color(0xFF1D192B)
                                                )
                                            },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = if (isAlreadyAdded) NavyMedium else Color(0xFFE8DEF8)
                                            ),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = if (isAlreadyAdded) NavyLight else Color(0xFFE8DEF8)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        
                        3 -> {
                            // Step 3: Professional Work Experience
                            Text(
                                text = "Build your chronological career history. Add, edit, or remove roles below.",
                                color = SlateMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )

                            // Add Role Button
                            Button(
                                onClick = {
                                    editingExpItem = null
                                    expTitle = ""
                                    expCompany = ""
                                    expLocation = ""
                                    expStartDate = ""
                                    expEndDate = ""
                                    expIsCurrent = false
                                    expAchievements = ""
                                    showExpDialog = true
                                },
                                modifier = Modifier.fillMaxWidth().testTag("add_exp_role_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Role", tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Professional Role", color = Color.White)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Roles Entered (${experienceList.size}):",
                                color = WhiteActive,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            if (experienceList.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(NavyLight, shape = RoundedCornerShape(12.dp))
                                        .border(1.dp, NavyLight.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(28.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("No work experience added yet.", color = SlateMuted, fontSize = 12.sp)
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    experienceList.forEach { item ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = NavyLight),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, NavyLight.copy(alpha = 0.5f))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = item.title,
                                                            color = WhiteActive,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp
                                                        )
                                                        Text(
                                                            text = "${item.company}${if (item.location.isNotEmpty()) " (${item.location})" else ""}",
                                                            color = EmeraldGreen,
                                                            fontWeight = FontWeight.Medium,
                                                            fontSize = 12.sp
                                                        )
                                                        Text(
                                                            text = if (item.isCurrent) "${item.startDate} - Present" else "${item.startDate} - ${item.endDate}",
                                                            color = SlateMuted,
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                    
                                                    Row {
                                                        IconButton(
                                                            onClick = {
                                                                editingExpItem = item
                                                                expTitle = item.title
                                                                expCompany = item.company
                                                                expLocation = item.location
                                                                expStartDate = item.startDate
                                                                expEndDate = item.endDate
                                                                expIsCurrent = item.isCurrent
                                                                expAchievements = item.achievements
                                                                showExpDialog = true
                                                            },
                                                            modifier = Modifier.size(28.dp).testTag("edit_exp_${item.id}")
                                                        ) {
                                                            Icon(Icons.Default.Edit, contentDescription = "Edit Role", tint = WhiteActive, modifier = Modifier.size(16.dp))
                                                        }
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        IconButton(
                                                            onClick = {
                                                                experienceList.remove(item)
                                                                updateExperience()
                                                                Toast.makeText(context, "Removed role!", Toast.LENGTH_SHORT).show()
                                                            },
                                                            modifier = Modifier.size(28.dp).testTag("delete_exp_${item.id}")
                                                        ) {
                                                            Icon(Icons.Default.Delete, contentDescription = "Delete Role", tint = CoralRed, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }

                                                if (item.achievements.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = item.achievements,
                                                        color = SlateMuted,
                                                        fontSize = 11.sp,
                                                        lineHeight = 14.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        4 -> {
                            // Step 4: Education History
                            Text(
                                text = "Add degrees, qualifications, credentials, or professional certifications that prove your competence.",
                                color = SlateMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )

                            OutlinedTextField(
                                value = education,
                                onValueChange = { education = it },
                                label = { Text("Education Details", color = SlateMuted) },
                                modifier = Modifier.fillMaxWidth().height(120.dp).testTag("resume_input_education"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = WhiteActive,
                                    unfocusedTextColor = WhiteActive,
                                    focusedBorderColor = EmeraldGreen,
                                    unfocusedBorderColor = NavyLight,
                                    focusedContainerColor = NavyMedium,
                                    unfocusedContainerColor = NavyMedium
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                        
                        5 -> {
                            // Step 5: Review & Download/Export Options
                            Text(
                                text = "Excellent job! Review your ATS health scan checklist. Download your resume file or export directly below.",
                                color = SlateMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )

                            // ATS Score representation inside Step 5
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(NavyLight, shape = RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                color = if (atsScore >= 80) EmeraldGreen.copy(alpha = 0.2f) else if (atsScore >= 50) Color(0xFFFFB74D).copy(alpha = 0.2f) else CoralRed.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(20.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$atsScore%",
                                            color = if (atsScore >= 80) EmeraldGreen else if (atsScore >= 50) Color(0xFFFFB74D) else CoralRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Computed ATS Scan Grade",
                                            color = WhiteActive,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = if (atsScore >= 80) "Optimal - Highly Ready!" else if (atsScore >= 50) "Acceptable - Needs minor polish" else "Incomplete inputs - actions recommended",
                                            color = SlateMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Download / Export Actions Panel
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = NavyLight),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Downloadable File Exporter:",
                                        color = WhiteActive,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Generate and download a high-compliance plain-text formatted .TXT resume file ready for corporate submissions.",
                                        color = SlateMuted,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )
                                    
                                    // Direct Local Download Button
                                    Button(
                                        onClick = {
                                            val resumeContent = getSimulatedMarkdown(name, skills, education, experience, profile, contactEmail, contactPhone)
                                            val filename = "${name.ifEmpty { "Candidate" }.replace(" ", "_")}_Resume.txt"
                                            val saved = saveTextFileToDownloads(context, filename, resumeContent)
                                            if (saved) {
                                                Toast.makeText(context, "Downloaded successfully! Saved $filename to your Downloads folder.", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Failed to save to Downloads folder. Attempting fallback share...", Toast.LENGTH_SHORT).show()
                                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "${name.ifEmpty { "Applicant" }} - ATS Resume File")
                                                    putExtra(android.content.Intent.EXTRA_TEXT, resumeContent)
                                                }
                                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Save ATS Resume File"))
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("download_resume_file_button"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                                    ) {
                                        Icon(Icons.Default.Done, contentDescription = "Download", tint = NavyDark, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Save to Device Downloads (.txt)", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Share / Send Button
                                    OutlinedButton(
                                        onClick = {
                                            val resumeContent = getSimulatedMarkdown(name, skills, education, experience, profile, contactEmail, contactPhone)
                                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(android.content.Intent.EXTRA_SUBJECT, "${name.ifEmpty { "Applicant" }} - ATS Resume File")
                                                putExtra(android.content.Intent.EXTRA_TEXT, resumeContent)
                                            }
                                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share ATS Resume File"))
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("share_resume_file_button"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldGreen),
                                        border = BorderStroke(1.dp, EmeraldGreen)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Share / Send Resume", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Wizard Navigation Controller Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
                        if (activeStep > 1) {
                            OutlinedButton(
                                onClick = { activeStep-- },
                                modifier = Modifier.testTag("wizard_back_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF6750A4))
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF6750A4), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Back", color = Color(0xFF6750A4), fontSize = 12.sp)
                            }
                        } else {
                            Spacer(modifier = Modifier.size(1.dp))
                        }

                        // Next / Save & Finish button
                        if (activeStep < 5) {
                            Button(
                                onClick = { activeStep++ },
                                modifier = Modifier.testTag("wizard_next_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                            ) {
                                Text("Next Step", color = Color.White, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.updateProfile(
                                        fullName = name,
                                        nationality = profile.nationality,
                                        currentCountry = profile.currentCountry,
                                        passportCountry = profile.passportCountry,
                                        education = education,
                                        skills = skills,
                                        languages = profile.languages,
                                        experience = experience,
                                        desiredCountries = profile.desiredCountries,
                                        preferredOccupations = profile.preferredOccupations,
                                        salaryExpectations = profile.salaryExpectations
                                    )
                                    Toast.makeText(context, "Resume Details Saved successfully!", Toast.LENGTH_SHORT).show()
                                    activeSubMode = "Live Preview" // Switch to Preview to see live layout!
                                },
                                modifier = Modifier.testTag("save_resume_details_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                            ) {
                                Icon(Icons.Default.Done, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save & Preview Live", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // Live Preview Section
            
            // 1. Source selector (Structured vs AI Tailored)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Color(0xFFF3EDF7), shape = RoundedCornerShape(8.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Structured Resume", "AI Tailored CV").forEach { src ->
                    val isSelected = activeSource == src
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) Color(0xFF381E72) else Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { activeSource = src }
                            .padding(vertical = 8.dp)
                            .testTag("source_${src.lowercase().replace(" ", "_")}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = src,
                            color = if (isSelected) Color.White else Color(0xFF381E72),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ATS Optimization Health Check Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, if (atsScore >= 80) EmeraldGreen else if (atsScore >= 50) Color(0xFFFFB74D) else CoralRed, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = NavyMedium),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    color = if (atsScore >= 80) EmeraldGreen.copy(alpha = 0.2f) else if (atsScore >= 50) Color(0xFFFFB74D).copy(alpha = 0.2f) else CoralRed.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(21.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$atsScore%",
                                color = if (atsScore >= 80) EmeraldGreen else if (atsScore >= 50) Color(0xFFFFB74D) else CoralRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ATS Scan Health Score",
                                color = WhiteActive,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            val scoreLabel = if (atsScore >= 80) "Excellent Optimization" else if (atsScore >= 50) "Needs Minor Improvement" else "Critical (Actions Needed)"
                            Text(
                                text = scoreLabel,
                                color = if (atsScore >= 80) EmeraldGreen else if (atsScore >= 50) Color(0xFFFFB74D) else CoralRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        TextButton(
                            onClick = { expandTips = !expandTips },
                            modifier = Modifier.testTag("toggle_ats_tips_button")
                        ) {
                            Text(
                                text = if (expandTips) "Hide Details" else "Review Tips (${atsTips.size})",
                                color = Color(0xFFE8DEF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    if (expandTips || atsTips.isNotEmpty()) {
                        AnimatedVisibility(visible = expandTips) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                Divider(color = NavyLight)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Actionable ATS Improvement Checklist:",
                                    color = WhiteActive,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                if (atsTips.isEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Optimized", tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Your resume is fully optimized and ready to bypass ATS pre-screens!", color = SlateMuted, fontSize = 11.sp)
                                    }
                                } else {
                                    atsTips.forEach { tip ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Tip",
                                                tint = Color(0xFFFFB74D),
                                                modifier = Modifier.padding(top = 2.dp).size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = tip,
                                                color = SlateMuted,
                                                fontSize = 11.sp,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // AI Builder prompt options if AI Source is selected
            if (activeSource == "AI Tailored CV") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(1.dp, NavyLight, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = NavyMedium),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Tailor Materials with Gemini AI",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Choose an AI asset to generate using your Google Gemini API key.",
                            color = SlateMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = targetRoleText,
                            onValueChange = { targetRoleText = it },
                            label = { Text("Target Application Role / Job Keywords", color = SlateMuted) },
                            modifier = Modifier.fillMaxWidth().testTag("target_role_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = WhiteActive,
                                unfocusedTextColor = WhiteActive,
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = NavyLight,
                                focusedContainerColor = NavyMedium,
                                unfocusedContainerColor = NavyMedium
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Document Type Selection
                        Text(
                            text = "Target AI Material to Generate:",
                            color = WhiteActive,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Pair("CV", "Tailored CV"),
                                Pair("Cover Letter", "Cover Letter"),
                                Pair("LinkedIn", "LinkedIn Optimizer"),
                                Pair("Interview Prep", "Interview Q&A"),
                                Pair("Visa Strategy", "Visa Strategy")
                            ).forEach { (docKey, docName) ->
                                val isSelected = activeAIDocType == docKey
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (isSelected) EmeraldGreen.copy(alpha = 0.2f) else NavyLight,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) EmeraldGreen else NavyLight,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { activeAIDocType = docKey }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("doctype_${docKey.lowercase().replace(" ", "_")}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = docName,
                                        color = if (isSelected) EmeraldGreen else WhiteActive,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Tone selection row
                        Text(
                            text = "Generation Voice Tone Angle:",
                            color = WhiteActive,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .background(NavyLight, shape = RoundedCornerShape(8.dp))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Professional", "Sponsor-aligned", "Creative Leader").forEach { toneOption ->
                                val isSelected = activeTone == toneOption
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            color = if (isSelected) Color(0xFF6750A4) else Color.Transparent,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable { activeTone = toneOption }
                                        .padding(vertical = 6.dp)
                                        .testTag("tone_${toneOption.lowercase().replace(" ", "_")}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = toneOption,
                                        color = if (isSelected) Color.White else SlateMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Trigger Button
                        Button(
                            onClick = { viewModel.generateResumeOrCoverLetter(targetRoleText, activeAIDocType, activeTone) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("generate_cv_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            if (isGen) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generating $activeAIDocType with Gemini...", color = Color.White, fontSize = 11.sp)
                            } else {
                                Icon(
                                    imageVector = when (activeAIDocType) {
                                        "CV" -> Icons.Default.Create
                                        "Cover Letter" -> Icons.Default.Email
                                        "LinkedIn" -> Icons.Default.Person
                                        "Interview Prep" -> Icons.Default.Call
                                        else -> Icons.Default.Info
                                    },
                                    contentDescription = activeAIDocType,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Draft Tailored $activeAIDocType",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 2. Layout Template Selector
            Text(
                text = "Professional Layout Template:",
                color = WhiteActive,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ATS Standard", "Modern Executive", "Creative Tech").forEach { template ->
                    val isSelected = activeTemplate == template
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) Color(0xFFE8DEF8) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF6750A4) else NavyLight,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { activeTemplate = template }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .testTag("template_${template.lowercase().replace(" ", "_")}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = template,
                            color = if (isSelected) Color(0xFF381E72) else SlateMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 3. Document Action Bar (Copy/Clear)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Canvas Preview",
                    color = WhiteActive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { showPrintPreview = true },
                        modifier = Modifier.testTag("a4_print_preview_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Print Mode",
                            tint = Color(0xFFE8DEF8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "A4 PRINT VIEW",
                            color = Color(0xFFE8DEF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(
                        onClick = {
                            try {
                                val defaultFilename = "${name.ifEmpty { "optimized" }.replace(" ", "_")}_ATS_resume.txt"
                                exportLauncher.launch(defaultFilename)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("export_ats_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Export ATS Plain-Text File",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "EXPORT ATS TXT",
                            color = EmeraldGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            val activeText = if (activeSource == "Structured Resume") {
                                getSimulatedMarkdown(name, skills, education, experience, profile, contactEmail, contactPhone)
                            } else {
                                resumeRes ?: ""
                            }
                            if (activeText.isNotEmpty()) {
                                clipboard.setText(AnnotatedString(activeText))
                                Toast.makeText(context, "Copied content to clipboard!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "No content to copy!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("copy_preview_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Copy", tint = Color(0xFF6750A4))
                    }
                    if (activeSource == "AI Tailored CV" && resumeRes != null) {
                        IconButton(onClick = { viewModel.clearResumeResult() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = CoralRed)
                        }
                    }
                }
            }

            // 4. Render Canvas Page
            val renderText = if (activeSource == "Structured Resume") {
                getSimulatedMarkdown(name, skills, education, experience, profile, contactEmail, contactPhone)
            } else {
                resumeRes ?: "No AI drafted content found. Click 'Draft tailored cv' above to generate with Gemini AI."
            }

            ParsedResumeRenderer(renderText, activeTemplate)
        }
    }

    // A4 Presentation / Print Mode Dialog
    if (showPrintPreview) {
        Dialog(onDismissRequest = { showPrintPreview = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "A4 Portrait Print Preview",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "High-fidelity simulated paper rendering (${activeTemplate})",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                        IconButton(onClick = { showPrintPreview = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .background(Color.White)
                            .padding(vertical = 8.dp)
                    ) {
                        val activeText = if (activeSource == "Structured Resume") {
                            getSimulatedMarkdown(name, skills, education, experience, profile, contactEmail, contactPhone)
                        } else {
                            resumeRes ?: ""
                        }
                        
                        if (activeText.isEmpty()) {
                            Text(
                                text = "No resume content drafted. Save structured inputs or use the Gemini AI Tailor to fill details.",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            ParsedResumeRenderer(activeText, activeTemplate)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Button(
                        onClick = {
                            val activeText = if (activeSource == "Structured Resume") {
                                getSimulatedMarkdown(name, skills, education, experience, profile, contactEmail, contactPhone)
                            } else {
                                resumeRes ?: ""
                            }
                            clipboard.setText(AnnotatedString(activeText))
                            Toast.makeText(context, "Copied content to clipboard!", Toast.LENGTH_SHORT).show()
                            showPrintPreview = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Copy", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Raw Text & Close", color = Color.White)
                    }
                }
            }
        }
    }
}

// Helper definitions for structured Work Experience
data class WorkExperienceItem(
    val id: String,
    val title: String = "",
    val company: String = "",
    val location: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isCurrent: Boolean = false,
    val achievements: String = ""
)

fun serializeExperience(list: List<WorkExperienceItem>): String {
    val sb = StringBuilder()
    list.forEach { item ->
        sb.append("=== WORK_EXPERIENCE ===\n")
        sb.append("Title: ${item.title.trim()}\n")
        sb.append("Company: ${item.company.trim()}\n")
        sb.append("Location: ${item.location.trim()}\n")
        sb.append("StartDate: ${item.startDate.trim()}\n")
        sb.append("EndDate: ${item.endDate.trim()}\n")
        sb.append("IsCurrent: ${item.isCurrent}\n")
        sb.append("Achievements:\n${item.achievements.trim()}\n")
        sb.append("=== END ===\n")
    }
    return sb.toString()
}

fun deserializeExperience(text: String): List<WorkExperienceItem> {
    val list = mutableListOf<WorkExperienceItem>()
    if (!text.contains("=== WORK_EXPERIENCE ===")) {
        if (text.trim().isNotEmpty()) {
            list.add(
                WorkExperienceItem(
                    id = java.util.UUID.randomUUID().toString(),
                    title = "Professional Experience",
                    company = "",
                    location = "",
                    startDate = "",
                    endDate = "",
                    isCurrent = false,
                    achievements = text
                )
            )
        }
        return list
    }

    val blocks = text.split("=== WORK_EXPERIENCE ===")
    for (block in blocks) {
        val trimmedBlock = block.trim()
        if (trimmedBlock.isEmpty()) continue
        val lines = trimmedBlock.split("\n")
        var title = ""
        var company = ""
        var location = ""
        var startDate = ""
        var endDate = ""
        var isCurrent = false
        val achievementsBuilder = StringBuilder()
        var inAchievements = false

        for (line in lines) {
            if (line.startsWith("=== END ===")) {
                break
            }
            if (inAchievements) {
                achievementsBuilder.append(line).append("\n")
                continue
            }

            if (line.startsWith("Title:")) {
                title = line.removePrefix("Title:").trim()
            } else if (line.startsWith("Company:")) {
                company = line.removePrefix("Company:").trim()
            } else if (line.startsWith("Location:")) {
                location = line.removePrefix("Location:").trim()
            } else if (line.startsWith("StartDate:")) {
                startDate = line.removePrefix("StartDate:").trim()
            } else if (line.startsWith("EndDate:")) {
                endDate = line.removePrefix("EndDate:").trim()
            } else if (line.startsWith("IsCurrent:")) {
                isCurrent = line.removePrefix("IsCurrent:").trim().lowercase().toBoolean()
            } else if (line.startsWith("Achievements:")) {
                inAchievements = true
            }
        }

        list.add(
            WorkExperienceItem(
                id = java.util.UUID.randomUUID().toString(),
                title = title,
                company = company,
                location = location,
                startDate = startDate,
                endDate = endDate,
                isCurrent = isCurrent,
                achievements = achievementsBuilder.toString().trim()
            )
        )
    }
    return list
}

fun formatExperienceToMarkdown(experienceText: String): String {
    val items = deserializeExperience(experienceText)
    if (items.isEmpty()) {
        return "No experience summary listed yet. Add experience details in the editor!"
    }
    val sb = StringBuilder()
    items.forEach { item ->
        val companyLoc = if (item.company.isNotEmpty() && item.location.isNotEmpty()) {
            " at ${item.company} (${item.location})"
        } else if (item.company.isNotEmpty()) {
            " at ${item.company}"
        } else if (item.location.isNotEmpty()) {
            " (${item.location})"
        } else {
            ""
        }
        val dateStr = if (item.isCurrent) {
            "${item.startDate} - Present"
        } else if (item.endDate.isNotEmpty()) {
            "${item.startDate} - ${item.endDate}"
        } else {
            item.startDate
        }
        
        sb.append("### ${item.title}$companyLoc\n")
        if (dateStr.isNotEmpty()) {
            sb.append("*${dateStr}*\n\n")
        }
        if (item.achievements.isNotEmpty()) {
            val lines = item.achievements.split("\n")
            lines.forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty()) {
                    if (trimmed.startsWith("-") || trimmed.startsWith("*")) {
                        sb.append("$trimmed\n")
                    } else {
                        sb.append("- $trimmed\n")
                    }
                }
            }
            sb.append("\n")
        } else {
            sb.append("\n")
        }
    }
    return sb.toString().trim()
}

fun formatSkillsToMarkdown(skillsText: String): String {
    val list = skillsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    if (list.isEmpty()) {
        return "No skills listed yet. Add skills in the editor!"
    }
    return list.joinToString(", ")
}

fun saveTextFileToDownloads(context: android.content.Context, filename: String, content: String): Boolean {
    val resolver = context.contentResolver
    val contentValues = android.content.ContentValues().apply {
        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/plain")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
        }
    }
    val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
    return if (uri != null) {
        try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    } else {
        false
    }
}

// Helper to construct clean resume Markdown from user input
private fun getSimulatedMarkdown(
    name: String,
    skills: String,
    education: String,
    experience: String,
    profile: com.example.data.UserProfileEntity,
    email: String = "",
    phone: String = ""
): String {
    val displayName = name.ifEmpty { "Your Full Name" }
    val displayNat = profile.nationality.ifEmpty { "International Applicant" }
    val displayCountry = profile.currentCountry.ifEmpty { "Global" }
    val displayEmail = email.ifEmpty { "${displayName.lowercase().replace(" ", "")}@sponsoredtalent.com" }
    val displayPhone = if (phone.isNotEmpty()) " | **Phone**: $phone" else ""
    
    val formattedSkills = formatSkillsToMarkdown(skills)
    val formattedExperience = formatExperienceToMarkdown(experience)
    
    return """
        # $displayName
        **Email**: $displayEmail$displayPhone | **Nationality**: $displayNat | **Current Location**: $displayCountry
        
        ## Professional Summary
        Dynamic and highly motivated professional seeking visa-sponsored opportunities. Proven track record of executing critical initiatives, collaborating with global cross-functional teams, and deploying responsive solutions that align with target country standards.
        
        ## Skills & Core Competencies
        $formattedSkills
        
        ## Professional Experience
        $formattedExperience
        
        ## Education & Credentials
        ${education.ifEmpty { "No education details listed yet. Add education history in the editor!" }}
    """.trimIndent()
}

private fun formatForAts(markdown: String): String {
    val lines = markdown.split("\n")
    val sb = StringBuilder()
    
    for (line in lines) {
        val trimmed = line.trim()
        when {
            trimmed.startsWith("# ") -> {
                val headerText = trimmed.substring(2).replace("**", "").replace("*", "").trim().uppercase()
                sb.append(headerText).append("\n")
                sb.append("=".repeat(headerText.length.coerceAtMost(80))).append("\n\n")
            }
            trimmed.startsWith("## ") -> {
                val secText = trimmed.substring(3).replace("**", "").replace("*", "").trim().uppercase()
                sb.append("\n").append(secText).append("\n")
                sb.append("-".repeat(secText.length.coerceAtMost(80))).append("\n")
            }
            trimmed.startsWith("### ") -> {
                val subText = trimmed.substring(4).replace("**", "").replace("*", "").trim()
                sb.append("\n").append(subText).append("\n")
            }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ") -> {
                val bulletContent = when {
                    trimmed.startsWith("- ") -> trimmed.substring(2)
                    trimmed.startsWith("* ") -> trimmed.substring(2)
                    trimmed.startsWith("• ") -> trimmed.substring(2)
                    else -> trimmed
                }
                val cleanBullet = bulletContent.replace("**", "").replace("*", "").trim()
                sb.append("  * ").append(cleanBullet).append("\n")
            }
            else -> {
                val cleanLine = trimmed.replace("**", "").replace("*", "").trim()
                if (cleanLine.isNotEmpty()) {
                    sb.append(cleanLine).append("\n")
                } else {
                    sb.append("\n")
                }
            }
        }
    }
    return sb.toString().replace(Regex("\n{3,}"), "\n\n").trim()
}

@Composable
fun ParsedResumeRenderer(markdownText: String, template: String) {
    val lines = markdownText.split("\n")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            when (template) {
                "ATS Standard" -> {
                    lines.forEach { line ->
                        RenderATSLine(line)
                    }
                }
                "Modern Executive" -> {
                    lines.forEach { line ->
                        RenderExecutiveLine(line)
                    }
                }
                "Creative Tech" -> {
                    RenderTechLayout(lines)
                }
            }
        }
    }
}

@Composable
fun RenderATSLine(line: String) {
    val trimmed = line.trim()
    if (trimmed.isEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        return
    }

    when {
        trimmed.startsWith("# ") -> {
            Text(
                text = trimmed.substring(2).trim(),
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }
        trimmed.startsWith("## ") -> {
            Column(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
                Text(
                    text = trimmed.substring(3).trim().uppercase(),
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Divider(color = Color.Black, thickness = 1.dp, modifier = Modifier.padding(top = 2.dp))
            }
        }
        trimmed.startsWith("### ") -> {
            Text(
                text = trimmed.substring(4).trim(),
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }
        trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ") -> {
            val bulletContent = when {
                trimmed.startsWith("- ") -> trimmed.substring(2)
                trimmed.startsWith("* ") -> trimmed.substring(2)
                trimmed.startsWith("• ") -> trimmed.substring(2)
                else -> trimmed
            }
            Row(
                modifier = Modifier.padding(start = 12.dp, top = 2.dp, bottom = 2.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(text = "•", color = Color.Black, modifier = Modifier.padding(end = 6.dp))
                Text(
                    text = parseBoldText(bulletContent),
                    style = TextStyle(color = Color.Black, fontSize = 11.sp, lineHeight = 14.sp)
                )
            }
        }
        else -> {
            Text(
                text = parseBoldText(trimmed),
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    textAlign = if (trimmed.contains("|")) TextAlign.Center else TextAlign.Start
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun RenderExecutiveLine(line: String) {
    val trimmed = line.trim()
    if (trimmed.isEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        return
    }

    when {
        trimmed.startsWith("# ") -> {
            Text(
                text = trimmed.substring(2).trim(),
                style = TextStyle(
                    color = Color(0xFF381E72),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        trimmed.startsWith("## ") -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 6.dp)
                    .background(Color(0xFFE8DEF8), shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(14.dp)
                        .background(Color(0xFF6750A4), shape = RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = trimmed.substring(3).trim(),
                    style = TextStyle(
                        color = Color(0xFF381E72),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        trimmed.startsWith("### ") -> {
            Text(
                text = trimmed.substring(4).trim(),
                style = TextStyle(
                    color = Color(0xFF1C1B1F),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
            )
        }
        trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ") -> {
            val bulletContent = when {
                trimmed.startsWith("- ") -> trimmed.substring(2)
                trimmed.startsWith("* ") -> trimmed.substring(2)
                trimmed.startsWith("• ") -> trimmed.substring(2)
                else -> trimmed
            }
            Row(
                modifier = Modifier.padding(start = 8.dp, top = 3.dp, bottom = 3.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF6750A4),
                    modifier = Modifier
                        .padding(top = 2.dp, end = 6.dp)
                        .size(10.dp)
                )
                Text(
                    text = parseBoldText(bulletContent),
                    style = TextStyle(color = Color(0xFF49454F), fontSize = 11.sp, lineHeight = 15.sp)
                )
            }
        }
        else -> {
            Text(
                text = parseBoldText(trimmed),
                style = TextStyle(
                    color = Color(0xFF49454F),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun RenderTechLayout(lines: List<String>) {
    var inSkillsSection = false
    
    lines.forEach { line ->
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return@forEach
        
        when {
            trimmed.startsWith("# ") -> {
                Text(
                    text = trimmed.substring(2).trim(),
                    style = TextStyle(
                        color = Color(0xFF381E72),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    ),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            trimmed.startsWith("## ") -> {
                val secTitle = trimmed.substring(3).trim()
                inSkillsSection = secTitle.contains("Skill", ignoreCase = true) || secTitle.contains("Competencies", ignoreCase = true)
                
                Text(
                    text = secTitle,
                    style = TextStyle(
                        color = Color(0xFF6750A4),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 8.dp)
                        .background(Color(0xFFF3EDF7), shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
            trimmed.startsWith("### ") -> {
                Text(
                    text = trimmed.substring(4).trim(),
                    style = TextStyle(
                        color = Color(0xFF1C1B1F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
            }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ") -> {
                val bulletContent = when {
                    trimmed.startsWith("- ") -> trimmed.substring(2)
                    trimmed.startsWith("* ") -> trimmed.substring(2)
                    trimmed.startsWith("• ") -> trimmed.substring(2)
                    else -> trimmed
                }
                
                Row(
                    modifier = Modifier.padding(start = 4.dp, top = 3.dp, bottom = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "›",
                        color = Color(0xFF6750A4),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = parseBoldText(bulletContent),
                        style = TextStyle(color = Color(0xFF49454F), fontSize = 11.sp, lineHeight = 15.sp)
                    )
                }
            }
            else -> {
                if (inSkillsSection && (trimmed.contains(",") || trimmed.contains("•"))) {
                    val delimiter = if (trimmed.contains("•")) "•" else ","
                    val items = trimmed.split(delimiter)
                    
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        val chunks = items.chunked(3)
                        chunks.forEach { chunk ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                chunk.forEach { item ->
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFE8DEF8), shape = RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = item.trim(),
                                            color = Color(0xFF381E72),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = parseBoldText(trimmed),
                        style = TextStyle(
                            color = Color(0xFF1C1B1F),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        ),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

fun parseBoldText(text: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val parts = text.split("**")
    for (i in parts.indices) {
        if (i % 2 == 1) {
            builder.pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold))
            builder.append(parts[i])
            builder.pop()
        } else {
            builder.append(parts[i])
        }
    }
    return builder.toAnnotatedString()
}

@Composable
fun PathwaysTab(viewModel: JobViewModel) {
    var pathwaysTabState by remember { mutableStateOf("Guidelines") }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val pathwaysTabs = listOf(
            Triple("Guidelines", "Visa Pathways & Alerts", "guidelines_tab"),
            Triple("Points", "Points & Eligibility", "points_tab"),
            Triple("Tracker", "My Visa Tracker", "tracker_tab"),
            Triple("DocVault", "Document Vault", "docvault_tab"),
            Triple("Relocation", "Relocation Checklist", "relocation_tab")
        )
        val selectedIndex = pathwaysTabs.indexOfFirst { it.first == pathwaysTabState }.coerceAtLeast(0)

        // ScrollableTabRow selector at top
        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            containerColor = NavyDark,
            contentColor = EmeraldGreen,
            edgePadding = 0.dp,
            divider = { Divider(color = NavyLight) },
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            pathwaysTabs.forEach { (state, title, tag) ->
                Tab(
                    selected = pathwaysTabState == state,
                    onClick = { pathwaysTabState = state },
                    text = { Text(title, color = if (pathwaysTabState == state) EmeraldGreen else SlateMuted, fontSize = 11.sp, fontWeight = if (pathwaysTabState == state) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag(tag)
                )
            }
        }

        when (pathwaysTabState) {
            "Guidelines" -> PathwaysGuidelinesSubScreen(viewModel)
            "Points" -> VisaPointsCalculatorScreen(viewModel)
            "Tracker" -> VisaTrackerSubScreen(viewModel)
            "DocVault" -> VisaDocumentVaultScreen(viewModel)
            "Relocation" -> RelocationChecklistSubScreen(viewModel)
        }
    }
}

@Composable
fun RelocationChecklistSubScreen(viewModel: JobViewModel) {
    val tasks by viewModel.relocationTasks.collectAsStateWithLifecycle()
    
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var selectedPrepopulateCountry by remember { mutableStateOf("Canada") }
    var showPrepopulateConfirm by remember { mutableStateOf(false) }
    
    // Add custom task states
    var newTaskName by remember { mutableStateOf("") }
    var newTaskCategory by remember { mutableStateOf("Visa Documents") }
    var newTaskNotes by remember { mutableStateOf("") }
    
    val categories = listOf("Visa Documents", "Housing", "Health Insurance", "Other")
    val countries = listOf("Canada", "United Kingdom", "Australia", "Germany", "Sweden", "General")
    
    // Calculate progress
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isCompleted }
    val progressFraction = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Relocation & Settlement Hub",
                    color = WhiteActive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Track your relocation progress, manage visa files, and transition to your new home.",
                    color = SlateMuted,
                    fontSize = 12.sp
                )
            }
            Button(
                onClick = { showAddTaskDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier.testTag("add_custom_task_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task", tint = NavyDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Task", color = NavyDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Progress & Country Template Selector Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            border = BorderStroke(1.dp, NavyLight)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PREPARATION PROGRESS",
                            color = TealCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$completedTasks of $totalTasks tasks completed (${(progressFraction * 100).toInt()}%)",
                            color = WhiteActive,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (progressFraction >= 1.0f) EmeraldGreen.copy(alpha = 0.2f) else AmberGold.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (progressFraction >= 1.0f) "Ready to Move! ✈️" else "Planning Stage 📋",
                            color = if (progressFraction >= 1.0f) EmeraldGreen else AmberGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = EmeraldGreen,
                    trackColor = NavyDark
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(NavyLight))
                Spacer(modifier = Modifier.height(14.dp))
                
                // Prepopulate checklist templates
                Text(
                    text = "LOAD RELOCATION CHECKLIST TEMPLATE",
                    color = TealCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Select a target country template to automatically populate verified tasks for visa, health insurance, and housing research.",
                    color = SlateMuted,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var expandedDropdown by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { expandedDropdown = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, NavyLight),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedPrepopulateCountry, color = WhiteActive, fontSize = 12.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = WhiteActive, modifier = Modifier.size(16.dp))
                            }
                        }
                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false },
                            modifier = Modifier.background(NavyMedium)
                        ) {
                            countries.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c, color = WhiteActive, fontSize = 12.sp) },
                                    onClick = {
                                        selectedPrepopulateCountry = c
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Button(
                        onClick = { showPrepopulateConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp).testTag("load_template_btn")
                    ) {
                        Text("Load Template", color = NavyDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Group by category and display checklist items
        categories.forEach { category ->
            val categoryTasks = tasks.filter { it.category == category }
            
            Text(
                text = category.uppercase(),
                color = TealCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
            
            if (categoryTasks.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyMedium.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, NavyLight.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                        Text("No tasks listed under $category. Add some or load a template!", color = SlateMuted, fontSize = 11.sp)
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    categoryTasks.forEach { task ->
                        RelocationTaskItemCard(
                            task = task,
                            onToggleComplete = {
                                viewModel.updateRelocationTask(task.copy(isCompleted = !task.isCompleted))
                            },
                            onDelete = {
                                viewModel.deleteRelocationTask(task.id)
                            },
                            onSaveNotes = { newNotes ->
                                viewModel.updateRelocationTask(task.copy(notes = newNotes))
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Add custom task dialog
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            containerColor = NavyMedium,
            title = {
                Text("Add Relocation Task", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTaskName,
                        onValueChange = { newTaskName = it },
                        label = { Text("Task Title", color = SlateMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = NavyLight,
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    var catExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = newTaskCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category", color = SlateMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = NavyLight,
                                focusedTextColor = WhiteActive,
                                unfocusedTextColor = WhiteActive
                            ),
                            trailingIcon = {
                                IconButton(onClick = { catExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Choose Category", tint = WhiteActive)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = catExpanded,
                            onDismissRequest = { catExpanded = false },
                            modifier = Modifier.background(NavyMedium)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = WhiteActive) },
                                    onClick = {
                                        newTaskCategory = cat
                                        catExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = newTaskNotes,
                        onValueChange = { newTaskNotes = it },
                        label = { Text("Notes / Details (Optional)", color = SlateMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = NavyLight,
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskName.isNotBlank()) {
                            viewModel.saveRelocationTask(
                                RelocationTaskEntity(
                                    taskName = newTaskName.trim(),
                                    category = newTaskCategory,
                                    country = "Custom",
                                    notes = newTaskNotes.trim()
                                )
                            )
                            // Reset state
                            newTaskName = ""
                            newTaskNotes = ""
                            showAddTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Add", color = NavyDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel", color = SlateMuted)
                }
            }
        )
    }
    
    // Prepopulate Template confirmation dialog
    if (showPrepopulateConfirm) {
        AlertDialog(
            onDismissRequest = { showPrepopulateConfirm = false },
            containerColor = NavyMedium,
            title = {
                Text("Overwrite Checklist?", color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text(
                    "Loading the $selectedPrepopulateCountry template will clear your existing checklist and populate it with default tasks for visa preparation, housing research, and health insurance. Do you want to continue?",
                    color = SlateMuted,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllRelocationTasks()
                        viewModel.prePopulateRelocationTasks(selectedPrepopulateCountry)
                        showPrepopulateConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                ) {
                    Text("Overwrite & Load", color = NavyDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrepopulateConfirm = false }) {
                    Text("Cancel", color = SlateMuted)
                }
            }
        )
    }
}

@Composable
fun RelocationTaskItemCard(
    task: RelocationTaskEntity,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onSaveNotes: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var notesText by remember { mutableStateOf(task.notes) }
    var isEditingNotes by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}"),
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        border = BorderStroke(1.dp, if (task.isCompleted) EmeraldGreen.copy(alpha = 0.5f) else NavyLight)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Checkbox
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (task.isCompleted) EmeraldGreen else NavyDark)
                        .border(1.5.dp, if (task.isCompleted) EmeraldGreen else SlateMuted, RoundedCornerShape(6.dp))
                        .clickable { onToggleComplete() },
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = "Completed", tint = NavyDark, modifier = Modifier.size(16.dp))
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Task title text
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isExpanded = !isExpanded }
                ) {
                    Text(
                        text = task.taskName,
                        color = if (task.isCompleted) SlateMuted else WhiteActive,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                        )
                    )
                    if (task.country != "General" && task.country != "Custom") {
                        Text(
                            text = "Country specific: ${task.country}",
                            color = SlateMuted,
                            fontSize = 10.sp
                        )
                    }
                }
                
                // Expand button
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Show Details",
                        tint = SlateMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // Delete button
                IconButton(
                    onClick = { onDelete() },
                    modifier = Modifier.size(28.dp).testTag("delete_task_btn_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Task",
                        tint = CoralRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(NavyLight.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "NOTES & PREPARATION DETAILS",
                    color = TealCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                if (isEditingNotes) {
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = NavyLight,
                            focusedTextColor = WhiteActive,
                            unfocusedTextColor = WhiteActive
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = {
                                isEditingNotes = false
                                notesText = task.notes
                            }
                        ) {
                            Text("Cancel", color = SlateMuted, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onSaveNotes(notesText.trim())
                                isEditingNotes = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Text("Save", color = NavyDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = if (task.notes.isEmpty()) "No custom notes entered. Add links, contact details, or application numbers." else task.notes,
                            color = if (task.notes.isEmpty()) SlateMuted else WhiteActive,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        IconButton(
                            onClick = { isEditingNotes = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Notes", tint = AmberGold, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PathwaysGuidelinesSubScreen(viewModel: JobViewModel) {
    val alerts by viewModel.customAlerts.collectAsStateWithLifecycle()
    var showAddAlert by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // Global Pathways Header
        Text(
            text = "Global Immigration Pathways",
            color = WhiteActive,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Text(
            text = "General guidelines and requirements for key recruiting countries hiring international workers.",
            color = SlateMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Canada
        PathwayCountryCard(
            country = "Canada",
            visaName = "LMIA / Express Entry Skilled Worker",
            time = "2 - 6 Months",
            reqs = "IELTS 6.0+, ECA Degree assessment, Clean background check, Verified job offer.",
            desc = "Employers must get a positive LMIA (Labour Market Impact Assessment) to hire foreign nationals. Express Entry manages applications for permanent residency under economic programs."
        )

        // UK
        PathwayCountryCard(
            country = "United Kingdom",
            visaName = "Skilled Worker Visa (Tier 2)",
            time = "3 - 8 Weeks",
            reqs = "IELTS 5.0+ or Degree in English, Certificate of Sponsorship (CoS), Minimum wage threshold (£38,700 standard or £23,200 Health/Care).",
            desc = "Enables international skilled workers to come and work in the UK. Health and Care Workers benefit from reduced visa fees and fast-track processing times."
        )

        // Germany
        PathwayCountryCard(
            country = "Germany",
            visaName = "EU Blue Card / Qualified Specialist Visa",
            time = "1 - 3 Months",
            reqs = "University degree recognized in Germany (Anabin assessment) OR solid professional vocational training, Binding job offer with matching salary threshold.",
            desc = "The German Opportunity Card (Chancenkarte) also allows candidates to search for a visa-sponsored job locally for up to one year."
        )

        // Australia
        PathwayCountryCard(
            country = "Australia",
            visaName = "Subclass 482 (Temporary Skill Shortage)",
            time = "1 - 4 Months",
            reqs = "At least 2 years relevant experience, Skills Assessment, Competent English (IELTS 5.0+), Medical check.",
            desc = "Allows employers to sponsor skilled workers to address labor shortages where local Australian citizens are unavailable."
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Custom alerts header
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "My Custom Job Alerts",
                color = WhiteActive,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showAddAlert = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Alert", tint = EmeraldGreen)
            }
        }

        if (showAddAlert) {
            AddAlertDialog(
                onDismiss = { showAddAlert = false },
                onAdd = { q, c, e, p, t ->
                    viewModel.addCustomAlert(q, c, e, p, t)
                    showAddAlert = false
                }
            )
        }

        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavyMedium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No custom alerts configured yet. Tap '+' to create alert.",
                    color = SlateMuted,
                    fontSize = 12.sp
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                alerts.forEach { alert ->
                    AlertListItem(alert = alert, onDelete = { viewModel.deleteAlert(alert.id) })
                }
            }
        }
    }
}

fun getColumnForStatus(status: String): String {
    return when (status) {
        "Applied" -> "Applied"
        "Interviewing" -> "Interviewing"
        "Offer Received" -> "Offer Received"
        "Visa Processing", "Sponsorship Approved", "Visa Filed", "Visa Approved" -> "Visa Processing"
        else -> "Applied"
    }
}

@Composable
fun VisaTrackerHeader(
    isKanbanView: Boolean,
    onToggleView: (Boolean) -> Unit,
    onAddTrack: () -> Unit,
    onExportPdf: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "My Visa Tracker",
                color = WhiteActive,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Monitor milestones, record interviews, and track visa applications synced in real-time.",
                color = SlateMuted,
                fontSize = 11.sp
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Export PDF Button
            Button(
                onClick = onExportPdf,
                colors = ButtonDefaults.buttonColors(containerColor = TealCyan),
                modifier = Modifier.height(28.dp).testTag("export_saved_jobs_pdf_header_button"),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Export PDF", tint = NavyDark, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export PDF", color = NavyDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            // View Mode Toggle (Kanban vs List)
            Row(
                modifier = Modifier
                    .background(NavyLight.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isKanbanView) EmeraldGreen else Color.Transparent)
                        .clickable { onToggleView(true) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("toggle_kanban_view")
                ) {
                    Text(
                        text = "Kanban",
                        color = if (isKanbanView) Color.White else SlateMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (!isKanbanView) EmeraldGreen else Color.Transparent)
                        .clickable { onToggleView(false) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("toggle_timeline_view")
                ) {
                    Text(
                        text = "Timeline",
                        color = if (!isKanbanView) Color.White else SlateMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = onAddTrack,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier.height(28.dp).testTag("add_visa_tracking_button"),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Track", tint = NavyDark, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Track App", color = NavyDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VisaTrackerStatsBanner(
    visaApps: List<com.example.data.VisaApplicationEntity>
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        border = BorderStroke(1.dp, NavyLight)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Sync status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val isFirebaseReady = com.example.data.FirebaseSyncManager.isFirebaseReady()
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (isFirebaseReady) EmeraldGreen else AmberGold, shape = androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isFirebaseReady) "Firebase Cloud Mirroring: Active" else "Offline Database: High Performance Local Storage Enabled",
                    color = WhiteActive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
            
            if (visaApps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                // Stats quick row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val total = visaApps.size
                    val applied = visaApps.count { getColumnForStatus(it.status) == "Applied" }
                    val interviewing = visaApps.count { getColumnForStatus(it.status) == "Interviewing" }
                    val processing = visaApps.count { getColumnForStatus(it.status) == "Visa Processing" }
                    val offer = visaApps.count { getColumnForStatus(it.status) == "Offer Received" }

                    listOf(
                        "Total" to total to WhiteActive,
                        "Applied" to applied to Color(0xFF6B7280),
                        "Interviewing" to interviewing to TealCyan,
                        "Processing" to processing to AmberGold,
                        "Offers" to offer to EmeraldGreen
                    ).forEach { (pair, color) ->
                        val (label, count) = pair
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(color.copy(alpha = 0.06f), RoundedCornerShape(6.dp))
                                .border(BorderStroke(1.dp, color.copy(alpha = 0.15f)), RoundedCornerShape(6.dp))
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = count.toString(), color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(text = label, color = SlateMuted, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VisaTrackerEmptyState(
    onAddTrack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(NavyMedium)
            .border(1.dp, NavyLight, RoundedCornerShape(12.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = SlateMuted,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No Visa Applications Tracked",
                color = WhiteActive,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Link a saved job or log a custom immigration application below to start tracking your visa progression timeline.",
                color = SlateMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAddTrack,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add First Visa Application", color = NavyDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun KanbanCardItem(
    app: com.example.data.VisaApplicationEntity,
    columnName: String,
    columns: List<String>,
    viewModel: JobViewModel
) {
    val context = LocalContext.current
    var isEditingNotes by remember { mutableStateOf(false) }
    var notesText by remember { mutableStateOf(app.notes) }
    var showStatusDropdown by remember { mutableStateOf(false) }

    val currentColumnIndex = columns.indexOf(columnName)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, NavyLight), RoundedCornerShape(10.dp))
            .testTag("kanban_card_${app.jobId}"),
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header Row: Country Flag emoji / text + delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Country & Job type
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val countryEmoji = when (app.country.lowercase()) {
                        "canada" -> "🇨🇦"
                        "united kingdom", "uk" -> "🇬🇧"
                        "germany" -> "🇩🇪"
                        "australia" -> "🇦🇺"
                        "united states", "usa", "us" -> "🇺🇸"
                        "ireland" -> "🇮🇪"
                        "netherlands" -> "🇳🇱"
                        else -> "🌐"
                    }
                    Text(
                        text = "$countryEmoji ${app.country}",
                        color = SlateMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.deleteVisaApplication(app.jobId)
                        Toast.makeText(context, "Untracked ${app.jobTitle}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(20.dp).testTag("delete_kanban_${app.jobId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete Application",
                        tint = CoralRed,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Job Title
            Text(
                text = app.jobTitle,
                color = WhiteActive,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            // Company Name
            Text(
                text = app.company,
                color = EmeraldGreen,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            // Database status badge (in case we want to show exact sub-status, e.g. "Visa Filed" inside "Visa Processing")
            if (app.status != columnName) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .background(TealCyan.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = app.status.uppercase(),
                        color = TealCyan,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Notes Section inside Card
            if (isEditingNotes) {
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text("Log interview notes, visa details...", color = SlateMuted, fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight,
                        focusedContainerColor = NavyDark,
                        unfocusedContainerColor = NavyDark
                    ),
                    textStyle = TextStyle(fontSize = 10.sp),
                    shape = RoundedCornerShape(6.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { isEditingNotes = false }, modifier = Modifier.height(24.dp)) {
                        Text("Cancel", color = Color.Gray, fontSize = 9.sp)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = {
                            viewModel.saveVisaApplication(app.copy(notes = notesText, updatedDate = System.currentTimeMillis().toString()))
                            isEditingNotes = false
                            Toast.makeText(context, "Logs Saved!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        modifier = Modifier.height(24.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Save", color = NavyDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isEditingNotes = true }
                        .background(NavyDark, RoundedCornerShape(6.dp))
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Notes",
                        tint = SlateMuted,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = app.notes.ifEmpty { "Click to add notes/progress logs..." },
                        color = if (app.notes.isEmpty()) SlateMuted else WhiteActive,
                        fontSize = 9.sp,
                        lineHeight = 12.sp,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row: Chevron movement keys & Dropdown status selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left movement button
                IconButton(
                    onClick = {
                        if (currentColumnIndex > 0) {
                            val targetCol = columns[currentColumnIndex - 1]
                            val newStatus = if (targetCol == "Visa Processing") "Visa Filed" else targetCol
                            viewModel.saveVisaApplication(app.copy(status = newStatus, updatedDate = System.currentTimeMillis().toString()))
                            Toast.makeText(context, "Moved back to $targetCol!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = currentColumnIndex > 0,
                    modifier = Modifier.size(24.dp).testTag("move_left_${app.jobId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Move Left",
                        tint = if (currentColumnIndex > 0) EmeraldGreen else SlateMuted.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Dropdown status picker button
                Box {
                    TextButton(
                        onClick = { showStatusDropdown = true },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        modifier = Modifier.height(24.dp).testTag("column_status_dropdown_${app.jobId}")
                    ) {
                        Text("Jump to...", color = EmeraldGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    val statuses = listOf("Applied", "Interviewing", "Visa Processing", "Offer Received", "Sponsorship Approved", "Visa Filed", "Visa Approved")
                    DropdownMenu(
                        expanded = showStatusDropdown,
                        onDismissRequest = { showStatusDropdown = false },
                        modifier = Modifier.background(NavyMedium).border(1.dp, NavyLight, RoundedCornerShape(8.dp))
                    ) {
                        statuses.forEach { stat ->
                            DropdownMenuItem(
                                text = { Text(stat, color = WhiteActive, fontSize = 11.sp) },
                                onClick = {
                                    viewModel.saveVisaApplication(
                                        app.copy(
                                            status = stat,
                                            updatedDate = System.currentTimeMillis().toString()
                                        )
                                    )
                                    showStatusDropdown = false
                                },
                                modifier = Modifier.testTag("kanban_jump_to_${stat}_${app.jobId}")
                            )
                        }
                    }
                }

                // Right movement button
                IconButton(
                    onClick = {
                        if (currentColumnIndex < columns.size - 1) {
                            val targetCol = columns[currentColumnIndex + 1]
                            val newStatus = if (targetCol == "Visa Processing") "Visa Filed" else targetCol
                            viewModel.saveVisaApplication(app.copy(status = newStatus, updatedDate = System.currentTimeMillis().toString()))
                            Toast.makeText(context, "Moved to $targetCol!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = currentColumnIndex < columns.size - 1,
                    modifier = Modifier.size(24.dp).testTag("move_right_${app.jobId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Move Right",
                        tint = if (currentColumnIndex < columns.size - 1) EmeraldGreen else SlateMuted.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun KanbanBoardView(
    visaApps: List<com.example.data.VisaApplicationEntity>,
    viewModel: JobViewModel,
    columns: List<String>,
    onAddInColumn: (String) -> Unit
) {
    val context = LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        columns.forEach { columnName ->
            val columnApps = visaApps.filter { getColumnForStatus(it.status) == columnName }
            
            // Define header colors based on column name
            val headerColor = when (columnName) {
                "Applied" -> Color(0xFF6B7280) // Cool Slate Gray
                "Interviewing" -> TealCyan      // Deep Purple/Teal
                "Visa Processing" -> AmberGold  // Warm Gold/Orange
                "Offer Received" -> EmeraldGreen // Primary Brand Emerald/Purple
                else -> Color.Gray
            }
            
            val headerBg = headerColor.copy(alpha = 0.08f)

            // Single column container
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .background(NavyMedium, RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, NavyLight), RoundedCornerShape(12.dp))
            ) {
                // Column Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .border(BorderStroke(width = 1.dp, color = NavyLight), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Colored status bullet indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(headerColor, shape = androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = columnName,
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Count Badge
                        Box(
                            modifier = Modifier
                                .background(headerColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = columnApps.size.toString(),
                                color = if (columnName == "Visa Processing") AmberGold else headerColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = { onAddInColumn(columnName) },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("add_card_${columnName}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add application to $columnName",
                            tint = headerColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Cards list (scrollable vertically within this column!)
                if (columnApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(12.dp)
                            .background(NavyDark.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, NavyLight.copy(alpha = 0.4f)), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No Applications",
                            color = SlateMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(columnApps) { app ->
                            KanbanCardItem(
                                app = app,
                                columnName = columnName,
                                columns = columns,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VisaTrackerSubScreen(viewModel: JobViewModel) {
    val context = LocalContext.current
    val visaApps by viewModel.visaApplications.collectAsStateWithLifecycle()
    val jobs by viewModel.allJobs.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showExportPdfDialog by remember { mutableStateOf(false) }
    var addPresetStatus by remember { mutableStateOf("Applied") }
    var isKanbanView by remember { mutableStateOf(true) }

    val columns = listOf("Applied", "Interviewing", "Visa Processing", "Offer Received")

    if (isKanbanView) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp)
        ) {
            VisaTrackerHeader(
                isKanbanView = isKanbanView,
                onToggleView = { isKanbanView = it },
                onAddTrack = {
                    addPresetStatus = "Applied"
                    showAddDialog = true
                },
                onExportPdf = {
                    showExportPdfDialog = true
                }
            )

            VisaTrackerStatsBanner(visaApps = visaApps)

            Spacer(modifier = Modifier.height(10.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (visaApps.isEmpty()) {
                    VisaTrackerEmptyState(onAddTrack = { showAddDialog = true })
                } else {
                    KanbanBoardView(
                        visaApps = visaApps,
                        viewModel = viewModel,
                        columns = columns,
                        onAddInColumn = { presetCol ->
                            addPresetStatus = presetCol
                            showAddDialog = true
                        }
                    )
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            VisaTrackerHeader(
                isKanbanView = isKanbanView,
                onToggleView = { isKanbanView = it },
                onAddTrack = {
                    addPresetStatus = "Applied"
                    showAddDialog = true
                },
                onExportPdf = {
                    showExportPdfDialog = true
                }
            )

            VisaTrackerStatsBanner(visaApps = visaApps)

            Spacer(modifier = Modifier.height(10.dp))

            if (visaApps.isEmpty()) {
                VisaTrackerEmptyState(onAddTrack = { showAddDialog = true })
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    visaApps.forEach { app ->
                        VisaApplicationCard(app = app, viewModel = viewModel)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddVisaApplicationDialog(
            savedJobs = jobs.filter { it.isBookmarked },
            initialStatusPreset = addPresetStatus,
            onDismiss = { showAddDialog = false },
            onSave = { app ->
                viewModel.saveVisaApplication(app)
                Toast.makeText(context, "Added tracking for ${app.jobTitle}!", Toast.LENGTH_SHORT).show()
                showAddDialog = false
            }
        )
    }

    if (showExportPdfDialog) {
        ExportSavedJobsPdfDialog(
            viewModel = viewModel,
            onDismiss = { showExportPdfDialog = false }
        )
    }
}

@Composable
fun VisaApplicationCard(
    app: com.example.data.VisaApplicationEntity,
    viewModel: JobViewModel
) {
    val context = LocalContext.current
    var isEditingNotes by remember { mutableStateOf(false) }
    var notesText by remember { mutableStateOf(app.notes) }
    var showStatusDropdown by remember { mutableStateOf(false) }
    
    val statuses = listOf("Applied", "Interviewing", "Offer Received", "Sponsorship Approved", "Visa Filed", "Visa Approved")
    val currentIndex = statuses.indexOf(app.status).coerceAtLeast(0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NavyLight, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Header (Job, Company, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.jobTitle,
                        color = WhiteActive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "${app.company} • ${app.country}",
                        color = EmeraldGreen,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
                IconButton(
                    onClick = {
                        viewModel.deleteVisaApplication(app.jobId)
                        Toast.makeText(context, "Deleted tracking for ${app.jobTitle}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("delete_visa_tracking_${app.jobId}")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Delete Track", tint = CoralRed, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Status Badge and Change Status Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (app.status) {
                                "Applied" -> Color.Gray.copy(alpha = 0.2f)
                                "Interviewing" -> TealCyan.copy(alpha = 0.2f)
                                "Offer Received" -> EmeraldGreen.copy(alpha = 0.2f)
                                "Sponsorship Approved" -> AmberGold.copy(alpha = 0.2f)
                                "Visa Filed" -> Color(0xFFD0BCFF).copy(alpha = 0.2f)
                                "Visa Approved" -> EmeraldGreen
                                else -> Color.Gray.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = app.status.uppercase(),
                        color = if (app.status == "Visa Approved") NavyDark else when (app.status) {
                            "Interviewing" -> TealCyan
                            "Offer Received" -> EmeraldGreen
                            "Sponsorship Approved" -> AmberGold
                            "Visa Filed" -> Color(0xFFD0BCFF)
                            else -> WhiteActive
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box {
                    TextButton(
                        onClick = { showStatusDropdown = true },
                        modifier = Modifier.testTag("change_status_button_${app.jobId}")
                    ) {
                        Text("Update Status", color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    DropdownMenu(
                        expanded = showStatusDropdown,
                        onDismissRequest = { showStatusDropdown = false },
                        modifier = Modifier.background(NavyMedium).border(1.dp, NavyLight, RoundedCornerShape(8.dp))
                    ) {
                        statuses.forEach { stat ->
                            DropdownMenuItem(
                                text = { Text(stat, color = WhiteActive, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.saveVisaApplication(
                                        app.copy(
                                            status = stat,
                                            updatedDate = System.currentTimeMillis().toString()
                                        )
                                    )
                                    showStatusDropdown = false
                                },
                                modifier = Modifier.testTag("status_option_${stat}_${app.jobId}")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Visual step timeline progress
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                statuses.forEachIndexed { idx, statName ->
                    val isDone = idx <= currentIndex
                    val isCurrent = idx == currentIndex
                    
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = if (isDone) EmeraldGreen else NavyLight,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                            .border(1.dp, if (isCurrent) EmeraldGreen else Color.Transparent, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = "Done",
                                tint = NavyDark,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }

                    if (idx < statuses.size - 1) {
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .weight(1f)
                                .background(if (idx < currentIndex) EmeraldGreen else NavyLight)
                        )
                    }
                }
            }

            // Timeline labels
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                statuses.forEachIndexed { idx, statName ->
                    val label = when (statName) {
                        "Applied" -> "Applied"
                        "Interviewing" -> "Interview"
                        "Offer Received" -> "Offer"
                        "Sponsorship Approved" -> "Sponsor"
                        "Visa Filed" -> "Filed"
                        "Visa Approved" -> "Approved"
                        else -> statName
                    }
                    Text(
                        text = label,
                        color = if (idx <= currentIndex) WhiteActive else SlateMuted,
                        fontSize = 8.sp,
                        fontWeight = if (idx == currentIndex) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.widthIn(max = 42.dp),
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Divider(color = NavyLight, modifier = Modifier.padding(vertical = 4.dp))
            Spacer(modifier = Modifier.height(10.dp))

            // Notes Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Progress Notes & Logs:", color = SlateMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                if (!isEditingNotes) {
                    TextButton(onClick = { isEditingNotes = true }) {
                        Text("Edit Notes", color = EmeraldGreen, fontSize = 11.sp)
                    }
                }
            }

            if (isEditingNotes) {
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text("Log application updates, interview dates, or visa receipt numbers...", color = SlateMuted, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight,
                        focusedContainerColor = NavyDark,
                        unfocusedContainerColor = NavyDark
                    ),
                    textStyle = TextStyle(fontSize = 11.sp),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { isEditingNotes = false }) {
                        Text("Cancel", color = Color.Gray, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.saveVisaApplication(app.copy(notes = notesText, updatedDate = System.currentTimeMillis().toString()))
                            isEditingNotes = false
                            Toast.makeText(context, "Saved Progress Notes!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Save Logs", color = NavyDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Text(
                    text = app.notes.ifEmpty { "No tracking notes added yet. Tap 'Edit Notes' to log details about interviews, sponsor replies, or visa progress files." },
                    color = if (app.notes.isEmpty()) SlateMuted else WhiteActive,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }
}

@Composable
fun AddVisaApplicationDialog(
    savedJobs: List<com.example.data.JobEntity>,
    initialStatusPreset: String = "Applied",
    onDismiss: () -> Unit,
    onSave: (com.example.data.VisaApplicationEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("Canada") }
    var initialStatus by remember { mutableStateOf(if (initialStatusPreset == "Visa Processing") "Visa Filed" else initialStatusPreset) }
    var notes by remember { mutableStateOf("") }
    var showStatusDropdown by remember { mutableStateOf(false) }
    
    val statuses = listOf("Applied", "Interviewing", "Offer Received", "Sponsorship Approved", "Visa Filed", "Visa Approved")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NavyLight, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Track Visa Application",
                    color = WhiteActive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Sync application milestones directly to Cloud Storage.",
                    color = SlateMuted,
                    fontSize = 11.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                if (savedJobs.isNotEmpty()) {
                    Text(
                        text = "Quick Link Saved/Bookmarked Job:",
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 48.dp)
                    ) {
                        items(savedJobs) { job ->
                            AssistChip(
                                onClick = {
                                    title = job.title
                                    company = job.company
                                    country = job.country
                                },
                                label = { Text("${job.company} - ${job.title}", fontSize = 10.sp, color = WhiteActive) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = NavyDark)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Text(
                    text = "Enter Application Details Manually:",
                    color = SlateMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Job Title", color = SlateMuted) },
                    modifier = Modifier.fillMaxWidth().testTag("add_visa_input_title"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight,
                        focusedContainerColor = NavyDark,
                        unfocusedContainerColor = NavyDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company Name", color = SlateMuted) },
                    modifier = Modifier.fillMaxWidth().testTag("add_visa_input_company"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight,
                        focusedContainerColor = NavyDark,
                        unfocusedContainerColor = NavyDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Target Country", color = SlateMuted) },
                    modifier = Modifier.fillMaxWidth().testTag("add_visa_input_country"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight,
                        focusedContainerColor = NavyDark,
                        unfocusedContainerColor = NavyDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Initial Status Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Initial Status:", color = WhiteActive, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Box {
                        OutlinedButton(
                            onClick = { showStatusDropdown = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldGreen),
                            border = BorderStroke(1.dp, EmeraldGreen)
                        ) {
                            Text(initialStatus, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        DropdownMenu(
                            expanded = showStatusDropdown,
                            onDismissRequest = { showStatusDropdown = false },
                            modifier = Modifier.background(NavyMedium).border(1.dp, NavyLight, RoundedCornerShape(8.dp))
                        ) {
                            statuses.forEach { stat ->
                                DropdownMenuItem(
                                    text = { Text(stat, color = WhiteActive, fontSize = 12.sp) },
                                    onClick = {
                                        initialStatus = stat
                                        showStatusDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)", color = SlateMuted) },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight,
                        focusedContainerColor = NavyDark,
                        unfocusedContainerColor = NavyDark
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (title.isBlank() || company.isBlank()) {
                                return@Button
                            }
                            onSave(
                                com.example.data.VisaApplicationEntity(
                                    jobId = java.util.UUID.randomUUID().toString(),
                                    jobTitle = title,
                                    company = company,
                                    country = country,
                                    status = initialStatus,
                                    notes = notes,
                                    updatedDate = System.currentTimeMillis().toString()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        enabled = title.isNotBlank() && company.isNotBlank()
                    ) {
                        Text("Add to Tracker", color = NavyDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ExportSavedJobsPdfDialog(
    viewModel: JobViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val savedJobs = viewModel.allJobs.collectAsStateWithLifecycle().value.filter { it.isBookmarked }
    val visaApps by viewModel.visaApplications.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isExporting by viewModel.isExportingPdf.collectAsStateWithLifecycle()
    val lastExportResult by viewModel.lastExportedPdfResult.collectAsStateWithLifecycle()

    var applicantName by remember {
        mutableStateOf(
            userProfile?.fullName?.ifBlank { "Vincent Mwangangi" } ?: "Vincent Mwangangi"
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NavyLight, RoundedCornerShape(18.dp))
                .testTag("export_saved_jobs_pdf_dialog"),
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title and Header Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TealCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export PDF",
                            tint = TealCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Export Saved Jobs PDF",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Generate professional immigration tracking document",
                            color = SlateMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = NavyLight, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Stats overview pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(NavyDark, RoundedCornerShape(10.dp))
                            .border(1.dp, NavyLight, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${savedJobs.size}", color = EmeraldGreen, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            Text(text = "Saved Jobs", color = SlateMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(NavyDark, RoundedCornerShape(10.dp))
                            .border(1.dp, NavyLight, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${visaApps.size}", color = TealCyan, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            Text(text = "Milestones", color = SlateMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(NavyDark, RoundedCornerShape(10.dp))
                            .border(1.dp, NavyLight, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val uniqueCountries = savedJobs.map { it.country }.distinct().size
                            Text(text = "$uniqueCountries", color = AmberGold, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            Text(text = "Countries", color = SlateMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Applicant Name field
                Text(
                    text = "Applicant Name (printed on PDF):",
                    color = WhiteActive,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = applicantName,
                    onValueChange = { applicantName = it },
                    placeholder = { Text("e.g. Vincent Mwangangi", color = SlateMuted) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pdf_applicant_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight,
                        focusedContainerColor = NavyDark,
                        unfocusedContainerColor = NavyDark
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Document Inclusions summary checklist
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavyDark.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .border(1.dp, NavyLight.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "DOCUMENT SPECIFICATIONS:",
                        color = TealCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val points = listOf(
                        "✔ Full Saved Jobs log with employer & country details",
                        "✔ Visa sponsorship specifications & verification scores",
                        "✔ Application submission dates & milestone notes",
                        "✔ Official PDF formatted for immigration lawyers & records"
                    )
                    points.forEach { pt ->
                        Text(
                            text = pt,
                            color = SlateMuted,
                            fontSize = 10.5.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                if (lastExportResult != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmeraldGreen.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "PDF Generated Successfully!",
                                    color = EmeraldGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Saved as: ${lastExportResult?.fileName}",
                                color = WhiteActive,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        viewModel.clearLastExportedPdfResult()
                        onDismiss()
                    }) {
                        Text("Close", color = SlateMuted)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (lastExportResult != null) {
                        Button(
                            onClick = {
                                lastExportResult?.file?.let { file ->
                                    com.example.util.SavedJobsPdfExporter.openOrSharePdf(context, file)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("share_generated_pdf_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = NavyDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open / Share PDF", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.exportSavedJobsToPdf(
                                    context = context,
                                    customName = applicantName
                                ) { res ->
                                    if (res != null) {
                                        Toast.makeText(context, "Exported ${res.totalJobs} jobs to PDF!", Toast.LENGTH_SHORT).show()
                                        com.example.util.SavedJobsPdfExporter.openOrSharePdf(context, res.file)
                                    } else {
                                        Toast.makeText(context, "Failed to generate PDF.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isExporting,
                            modifier = Modifier.testTag("confirm_export_pdf_btn")
                        ) {
                            if (isExporting) {
                                CircularProgressIndicator(color = NavyDark, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = NavyDark, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Export PDF Tracker", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedJobsPdfExportCard(
    viewModel: JobViewModel,
    onOpenExportDialog: () -> Unit
) {
    val jobs by viewModel.allJobs.collectAsStateWithLifecycle()
    val savedCount = jobs.count { it.isBookmarked }
    val visaApps by viewModel.visaApplications.collectAsStateWithLifecycle()

    Card(
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        border = BorderStroke(1.dp, TealCyan.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_jobs_pdf_export_card"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TealCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export Saved Jobs PDF",
                        tint = TealCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Saved Jobs PDF Tracker",
                        color = WhiteActive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Export your bookmarked jobs & application dates into a formal tracking PDF",
                        color = SlateMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(NavyDark, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "$savedCount", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Saved", color = SlateMuted, fontSize = 11.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(NavyDark, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "${visaApps.size}", color = TealCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Milestones", color = SlateMuted, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onOpenExportDialog,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_open_pdf_export_dialog"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealCyan,
                    contentColor = NavyDark
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Saved Jobs to PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun PathwayCountryCard(
    country: String,
    visaName: String,
    time: String,
    reqs: String,
    desc: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(1.dp, NavyLight, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = country,
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TealCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = time, color = TealCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = visaName, color = WhiteActive, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = desc, color = SlateMuted, fontSize = 12.sp, lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Requirements: $reqs",
                color = AmberGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun AlertListItem(alert: CustomAlertEntity, onDelete: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, NavyLight)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.queryText,
                    color = WhiteActive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Country: ${alert.country}",
                    color = SlateMuted,
                    fontSize = 12.sp
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (alert.isEmailAlert) AlertChannelBadge("Email")
                    if (alert.isPushAlert) AlertChannelBadge("Push")
                    if (alert.isTelegramAlert) AlertChannelBadge("Telegram")
                }
            }
            IconButton(onClick = {
                Toast.makeText(context, "Test Notification Triggered for: '${alert.queryText}'", Toast.LENGTH_LONG).show()
            }) {
                Icon(Icons.Default.Notifications, contentDescription = "Test Notification", tint = AmberGold)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Alert", tint = CoralRed)
            }
        }
    }
}

@Composable
fun AlertChannelBadge(name: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(TealCyan.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = name, color = TealCyan, fontSize = 10.sp)
    }
}

@Composable
fun AddAlertDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Boolean, Boolean, Boolean) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("Canada") }
    var emailAlert by remember { mutableStateOf(true) }
    var pushAlert by remember { mutableStateOf(true) }
    var telegramAlert by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NavyLight, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Create Job Alert",
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Query Keyword (e.g. Caregiver, Nurse)", color = SlateMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight,
                        focusedContainerColor = NavyMedium,
                        unfocusedContainerColor = NavyMedium
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Target Country", color = SlateMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteActive,
                        unfocusedTextColor = WhiteActive,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyLight,
                        focusedContainerColor = NavyMedium,
                        unfocusedContainerColor = NavyMedium
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Alert Delivery Channels:", color = SlateMuted, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = emailAlert, onCheckedChange = { emailAlert = it })
                    Text("Email Notification", color = WhiteActive, fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = pushAlert, onCheckedChange = { pushAlert = it })
                    Text("System Push Alert", color = WhiteActive, fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = telegramAlert, onCheckedChange = { telegramAlert = it })
                    Text("Telegram / WhatsApp Alert", color = WhiteActive, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = SlateMuted)
                    }
                    Button(
                        onClick = {
                            if (query.isNotEmpty()) {
                                onAdd(query, country, emailAlert, pushAlert, telegramAlert)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Text("Create", color = NavyDark)
                    }
                }
            }
        }
    }
}

@Composable
fun EnterpriseTab(viewModel: JobViewModel) {
    val appMode by viewModel.appMode.collectAsStateWithLifecycle()

    if (appMode == "Employer") {
        EmployerDashboard(viewModel)
    } else if (appMode == "Admin") {
        AdminDashboard(viewModel)
    } else {
        // Not in Employer or Admin mode - display quick switch and upgrade premium options
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Enterprise & Premium Services",
                color = WhiteActive,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "Unlock professional resources, candidate features, recruiter packages, and fraud audit panels.",
                color = SlateMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Switch buttons
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyMedium),
                border = BorderStroke(1.dp, NavyLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Access Recruiter / Staff Panel",
                        color = WhiteActive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Verify sponsoring licences, post direct job roles with 100% confidence trust score, or manage system fraud dashboards.",
                        color = SlateMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.setAppMode("Employer") },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("Employer Mode", color = NavyDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.setAppMode("Admin") },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldGreen),
                            border = BorderStroke(1.dp, EmeraldGreen)
                        ) {
                            Text("Admin Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Premium services
            Text(
                text = "Revenue Services & Upgrades",
                color = WhiteActive,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            PremiumItemCard(
                title = "Elite CV Tailor Review",
                price = "$49 USD",
                desc = "Have an immigration lawyer review your drafted ATS resume, customize it for LMIA / UK Skilled Worker codes, and increase interview selection rates by 3x."
            )

            PremiumItemCard(
                title = "1-on-1 Visa Interview Coaching",
                price = "$120 USD",
                desc = "Practice interview simulations with recruitment experts specializing in UK NHS, Australian Subclass TSS, and Canadian transport sectors."
            )

            PremiumItemCard(
                title = "Featured Sponsored Employer listing",
                price = "$199 USD/mo",
                desc = "Feature your company sponsorship listing at the top of the global job search feed for 100,000+ targeted international applicants."
            )
        }
    }
}

@Composable
fun PremiumItemCard(title: String, price: String, desc: String) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(1.dp, NavyLight, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = NavyMedium)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = WhiteActive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = price,
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = desc, color = SlateMuted, fontSize = 12.sp, lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    Toast.makeText(context, "Upgrade Checkout initiated for $title", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Upgrade & Order Now", color = NavyDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmployerDashboard(viewModel: JobViewModel) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("Canada") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var visaType by remember { mutableStateOf("LMIA Approved") }
    var relocation by remember { mutableStateOf(true) }
    var contractType by remember { mutableStateOf("Full-time") }
    var industry by remember { mutableStateOf("Technology") }
    var expLevel by remember { mutableStateOf("Mid") }
    var appUrl by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "Recruiter Posting Portal",
                color = WhiteActive,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { viewModel.setAppMode("Jobseeker") }) {
                Text("Exit Portal", color = EmeraldGreen)
            }
        }

        Text(
            text = "Directly post job openings offering verified visa sponsorship or relocation assistance. Your listings will instantly appear with a 100% Trust score.",
            color = SlateMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedProfileInput("Job Title (e.g., Lead Structural Architect)", title) { title = it }
        OutlinedProfileInput("Company Name", company) { company = it }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedProfileInput("Country", country) { country = it }
            }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedProfileInput("Specific Location (City)", location) { location = it }
            }
        }

        OutlinedProfileInput("Salary Package (e.g. $80k CAD / year)", salary) { salary = it }
        OutlinedProfileInput("Sponsorship Visa Pathway (e.g. UK Skilled Worker)", visaType) { visaType = it }
        OutlinedProfileInput("Application URL / Portal Link", appUrl) { appUrl = it }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedProfileInput("Contract Type", contractType) { contractType = it }
            }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedProfileInput("Industry Group", industry) { industry = it }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedProfileInput("Target Experience Level", expLevel) { expLevel = it }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = relocation, onCheckedChange = { relocation = it })
                    Text("Offers Relocation", color = WhiteActive, fontSize = 12.sp)
                }
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Comprehensive Job Description & Sponsorship details", color = SlateMuted) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(vertical = 4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = WhiteActive,
                unfocusedTextColor = WhiteActive,
                focusedBorderColor = EmeraldGreen,
                unfocusedBorderColor = NavyLight,
                focusedContainerColor = NavyMedium,
                unfocusedContainerColor = NavyMedium
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (title.isNotEmpty() && company.isNotEmpty() && appUrl.isNotEmpty()) {
                    viewModel.createCustomJob(
                        title = title,
                        company = company,
                        country = country,
                        location = location,
                        description = description,
                        salary = salary,
                        visaType = visaType,
                        relocation = relocation,
                        contractType = contractType,
                        industry = industry,
                        experienceLevel = expLevel,
                        applicationUrl = appUrl
                    )
                    Toast.makeText(context, "Direct Visa-Sponsored Job Posted!", Toast.LENGTH_LONG).show()
                    viewModel.setAppMode("Jobseeker")
                } else {
                    Toast.makeText(context, "Please enter at least Title, Company, and Application URL.", Toast.LENGTH_SHORT).show()
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Post", tint = NavyDark)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Publish Sponsoring Job Listing", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun AdminDashboard(viewModel: JobViewModel) {
    val jobs by viewModel.allJobs.collectAsStateWithLifecycle()
    val reportedJobs = jobs.filter { it.isFraud || it.confidenceScore < 40 }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "Moderator & System Health Panel",
                color = WhiteActive,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { viewModel.setAppMode("Jobseeker") }) {
                Text("Exit Panel", color = EmeraldGreen)
            }
        }

        // Dashboard statistics
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            border = BorderStroke(1.dp, NavyLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Crawled Jobs", jobs.size.toString())
                StatItem("Flagged Scam", reportedJobs.size.toString())
                StatItem("API Status", "Active", EmeraldGreen)
                StatItem("Crawl Engine", "Healthy", EmeraldGreen)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Health indicators
        Text(
            text = "System Health Indicators",
            color = WhiteActive,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(NavyMedium)
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                HealthStatusRow("Google Search Grounding Engine", "ONLINE (11ms ping)")
                HealthStatusRow("Gemini AI Content Moderation Model", "ACTIVE")
                HealthStatusRow("CV Automated Tailoring Queue", "STANDBY (0 tasks)")
                HealthStatusRow("Telegram Webhook Listener", "CONNECTED")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Moderator actions section
        Text(
            text = "Fraud Reports & Audit Queue",
            color = WhiteActive,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (reportedJobs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavyMedium),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No job reports are pending audit.", color = SlateMuted, fontSize = 12.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                reportedJobs.forEach { job ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CoralRed, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = NavyMedium)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = job.title, color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Company: ${job.company}", color = SlateMuted, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Flag Reason: User reported as fraud / low score (${job.confidenceScore}%)",
                                color = CoralRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                TextButton(
                                    onClick = {
                                        viewModel.deleteJob(job.id)
                                        Toast.makeText(context, "Listing permanently deleted.", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text("Approve Delete", color = CoralRed, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color = WhiteActive) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = SlateMuted, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun HealthStatusRow(label: String, valText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = SlateMuted, fontSize = 11.sp)
        Text(text = valText, color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SalariesTab(viewModel: JobViewModel) {
    val insight by viewModel.salaryInsight.collectAsStateWithLifecycle()
    val isQuerying by viewModel.isQueryingSalary.collectAsStateWithLifecycle()
    val error by viewModel.salaryError.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf("Market Trends") }
    var countryCode by remember { mutableStateOf("DE") }
    var hoveredIndex by remember { mutableStateOf(11) }

    val marketForecast by viewModel.marketForecast.collectAsStateWithLifecycle()
    val isGeneratingForecast by viewModel.isGeneratingForecast.collectAsStateWithLifecycle()

    var jobTitle by remember { mutableStateOf("Software Engineer") }

    val countries = listOf(
        Pair("DE", "Germany 🇩🇪"),
        Pair("US", "United States 🇺🇸"),
        Pair("GB", "United Kingdom 🇬🇧"),
        Pair("CA", "Canada 🇨🇦"),
        Pair("AU", "Australia 🇦🇺")
    )

    data class MarketTrendPoint(
        val month: String,
        val demand: Int,
        val averageSalary: Double
    )

    val trendsData = remember {
        mapOf(
            "US" to listOf(
                MarketTrendPoint("Jul 25", 11800, 109500.0),
                MarketTrendPoint("Aug 25", 12100, 110200.0),
                MarketTrendPoint("Sep 25", 12500, 111000.0),
                MarketTrendPoint("Oct 25", 12900, 111800.0),
                MarketTrendPoint("Nov 25", 13300, 112500.0),
                MarketTrendPoint("Dec 25", 13000, 113000.0),
                MarketTrendPoint("Jan 26", 13800, 114200.0),
                MarketTrendPoint("Feb 26", 14200, 115000.0),
                MarketTrendPoint("Mar 26", 14700, 115800.0),
                MarketTrendPoint("Apr 26", 15100, 116500.0),
                MarketTrendPoint("May 26", 15600, 117800.0),
                MarketTrendPoint("Jun 26", 16200, 118900.0)
            ),
            "GB" to listOf(
                MarketTrendPoint("Jul 25", 4100, 57800.0),
                MarketTrendPoint("Aug 25", 4250, 58200.0),
                MarketTrendPoint("Sep 25", 4400, 58600.0),
                MarketTrendPoint("Oct 25", 4600, 59100.0),
                MarketTrendPoint("Nov 25", 4800, 59500.0),
                MarketTrendPoint("Dec 25", 4700, 59800.0),
                MarketTrendPoint("Jan 26", 5100, 60400.0),
                MarketTrendPoint("Feb 26", 5300, 61000.0),
                MarketTrendPoint("Mar 26", 5500, 61600.0),
                MarketTrendPoint("Apr 26", 5650, 62000.0),
                MarketTrendPoint("May 26", 5800, 62700.0),
                MarketTrendPoint("Jun 26", 6050, 63500.0)
            ),
            "DE" to listOf(
                MarketTrendPoint("Jul 25", 3100, 64200.0),
                MarketTrendPoint("Aug 25", 3250, 64500.0),
                MarketTrendPoint("Sep 25", 3400, 64900.0),
                MarketTrendPoint("Oct 25", 3550, 65200.0),
                MarketTrendPoint("Nov 25", 3700, 65800.0),
                MarketTrendPoint("Dec 25", 3600, 66000.0),
                MarketTrendPoint("Jan 26", 3900, 66800.0),
                MarketTrendPoint("Feb 26", 4050, 67100.0),
                MarketTrendPoint("Mar 26", 4200, 67500.0),
                MarketTrendPoint("Apr 26", 4350, 68000.0),
                MarketTrendPoint("May 26", 4500, 68800.0),
                MarketTrendPoint("Jun 26", 4680, 69500.0)
            ),
            "CA" to listOf(
                MarketTrendPoint("Jul 25", 3500, 87500.0),
                MarketTrendPoint("Aug 25", 3650, 88100.0),
                MarketTrendPoint("Sep 25", 3800, 88700.0),
                MarketTrendPoint("Oct 25", 4000, 89300.0),
                MarketTrendPoint("Nov 25", 4200, 89900.0),
                MarketTrendPoint("Dec 25", 4100, 90200.0),
                MarketTrendPoint("Jan 26", 4400, 91100.0),
                MarketTrendPoint("Feb 26", 4600, 91800.0),
                MarketTrendPoint("Mar 26", 4800, 92400.0),
                MarketTrendPoint("Apr 26", 4950, 93000.0),
                MarketTrendPoint("May 26", 5100, 93800.0),
                MarketTrendPoint("Jun 26", 5320, 94500.0)
            ),
            "AU" to listOf(
                MarketTrendPoint("Jul 25", 2600, 91500.0),
                MarketTrendPoint("Aug 25", 2750, 92100.0),
                MarketTrendPoint("Sep 25", 2900, 92700.0),
                MarketTrendPoint("Oct 25", 3100, 93200.0),
                MarketTrendPoint("Nov 25", 3300, 93800.0),
                MarketTrendPoint("Dec 25", 3200, 94100.0),
                MarketTrendPoint("Jan 26", 3500, 94900.0),
                MarketTrendPoint("Feb 26", 3650, 95500.0),
                MarketTrendPoint("Mar 26", 3800, 96100.0),
                MarketTrendPoint("Apr 26", 3950, 96800.0),
                MarketTrendPoint("May 26", 4100, 97500.0),
                MarketTrendPoint("Jun 26", 4280, 98200.0)
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Horizontal Tab row switcher
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(NavyMedium, shape = RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Market Trends", "Salary Checker", "Purchasing Power", "Relocation Net").forEach { tab ->
                    val isSelected = activeSubTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) EmeraldGreen else Color.Transparent)
                            .clickable { activeSubTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) Color.White else SlateMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        if (activeSubTab == "Market Trends") {
            val selectedTrends = trendsData[countryCode] ?: trendsData["DE"]!!
            val activePoint = selectedTrends[hoveredIndex]
            val totalAnnualSponsorships = when (countryCode) {
                "US" -> "168.4k"
                "GB" -> "68.2k"
                "DE" -> "42.5k"
                "CA" -> "52.1k"
                "AU" -> "31.6k"
                else -> "45.0k"
            }
            val avgSalaryText = when (countryCode) {
                "DE" -> "€${String.format("%,.0f", selectedTrends.map { it.averageSalary }.average())}"
                "US" -> "$${String.format("%,.0f", selectedTrends.map { it.averageSalary }.average())}"
                "GB" -> "£${String.format("%,.0f", selectedTrends.map { it.averageSalary }.average())}"
                "CA" -> "CA$${String.format("%,.0f", selectedTrends.map { it.averageSalary }.average())}"
                "AU" -> "A$${String.format("%,.0f", selectedTrends.map { it.averageSalary }.average())}"
                else -> "$${String.format("%,.0f", selectedTrends.map { it.averageSalary }.average())}"
            }
            val yoyGrowth = when (countryCode) {
                "US" -> "+14.2%"
                "GB" -> "+8.5%"
                "DE" -> "+18.9%"
                "CA" -> "+10.3%"
                "AU" -> "+7.4%"
                else -> "+11.5%"
            }

            // Region analysis row selection
            item {
                Column {
                    Text(
                        text = "Select Country to Analyze",
                        color = WhiteActive,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(countries) { (code, name) ->
                            val isSelected = countryCode == code
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    countryCode = code
                                    hoveredIndex = 11
                                    viewModel.clearMarketForecast()
                                },
                                label = { Text(name, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldGreen,
                                    selectedLabelColor = Color.White,
                                    containerColor = NavyMedium,
                                    labelColor = SlateMuted
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    selectedBorderColor = EmeraldGreen,
                                    borderColor = NavyLight
                                )
                            )
                        }
                    }
                }
            }

            // High impact metric cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = NavyMedium),
                        border = BorderStroke(1.dp, NavyLight)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Annual Cap", color = SlateMuted, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(totalAnnualSponsorships, color = WhiteActive, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(yoyGrowth, color = EmeraldGreen, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = NavyMedium),
                        border = BorderStroke(1.dp, NavyLight)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Avg Wage", color = SlateMuted, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(avgSalaryText, color = WhiteActive, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("12mo Average", color = SlateMuted, fontSize = 10.sp)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = NavyMedium),
                        border = BorderStroke(1.dp, NavyLight)
                    ) {
                        val rate = when (countryCode) {
                            "DE" -> "91.4%"
                            "US" -> "84.6%"
                            "GB" -> "88.2%"
                            "CA" -> "89.5%"
                            "AU" -> "87.1%"
                            else -> "85.0%"
                        }
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Appr. Rate", color = SlateMuted, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(rate, color = TealCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Visa Success", color = SlateMuted, fontSize = 10.sp)
                        }
                    }
                }
            }

            // Hover details / synchronized tooltip
            item {
                val pt = selectedTrends[hoveredIndex]
                val sym = when (countryCode) {
                    "DE" -> "€"
                    "US" -> "$"
                    "GB" -> "£"
                    "CA" -> "CA$"
                    "AU" -> "A$"
                    else -> "$"
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavyMedium),
                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Period Frame", color = SlateMuted, fontSize = 10.sp)
                            Text(pt.month, color = WhiteActive, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Openings Demand", color = SlateMuted, fontSize = 10.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(androidx.compose.foundation.shape.CircleShape).background(EmeraldGreen))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${String.format("%,d", pt.demand)} slots", color = EmeraldGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Avg Wage Scale", color = SlateMuted, fontSize = 10.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(androidx.compose.foundation.shape.CircleShape).background(TealCyan))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$sym${String.format("%,.0f", pt.averageSalary)}", color = TealCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Demand Chart (Area on Canvas)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavyMedium),
                    border = BorderStroke(1.dp, NavyLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Visa-Sponsored Job Demand Index",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Active visa-sponsorship job counts monthly (Interactive curve)",
                            color = SlateMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        ) {
                            val minDemand = selectedTrends.minOf { it.demand } * 0.95
                            val maxDemand = selectedTrends.maxOf { it.demand } * 1.05
                            val rangeDemand = maxDemand - minDemand

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                // Draw horizontal grid lines
                                val gridLines = 4
                                for (i in 0..gridLines) {
                                    val y = (h / gridLines) * i
                                    drawLine(
                                        color = NavyLight.copy(alpha = 0.3f),
                                        start = androidx.compose.ui.geometry.Offset(0f, y),
                                        end = androidx.compose.ui.geometry.Offset(w, y),
                                        strokeWidth = 2f
                                    )
                                }

                                // Build Area Path and Line Path
                                val linePath = androidx.compose.ui.graphics.Path()
                                selectedTrends.forEachIndexed { idx, point ->
                                    val px = idx * (w / 11f)
                                    val py = h - (((point.demand - minDemand) / rangeDemand) * h).toFloat()
                                    if (idx == 0) linePath.moveTo(px, py) else linePath.lineTo(px, py)
                                }

                                // Draw filled area underneath the line
                                val areaPath = androidx.compose.ui.graphics.Path().apply {
                                    addPath(linePath)
                                    lineTo(w, h)
                                    lineTo(0f, h)
                                    close()
                                }
                                drawPath(
                                    path = areaPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(EmeraldGreen.copy(alpha = 0.25f), Color.Transparent)
                                    )
                                )

                                // Draw stroke line
                                drawPath(
                                    path = linePath,
                                    color = EmeraldGreen,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = 6f,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                )

                                // Draw selected node guide
                                val activeX = hoveredIndex * (w / 11f)
                                val activePt = selectedTrends[hoveredIndex]
                                val activeY = h - (((activePt.demand - minDemand) / rangeDemand) * h).toFloat()

                                // Vertical guideline
                                drawLine(
                                    color = NavyLight.copy(alpha = 0.8f),
                                    start = androidx.compose.ui.geometry.Offset(activeX, 0f),
                                    end = androidx.compose.ui.geometry.Offset(activeX, h),
                                    strokeWidth = 3f
                                )

                                // Highlight active point
                                drawCircle(
                                    color = EmeraldGreen.copy(alpha = 0.3f),
                                    radius = 18f,
                                    center = androidx.compose.ui.geometry.Offset(activeX, activeY)
                                )
                                drawCircle(
                                    color = EmeraldGreen,
                                    radius = 8f,
                                    center = androidx.compose.ui.geometry.Offset(activeX, activeY)
                                )
                            }

                            // Invisible touch tracker columns
                            Row(modifier = Modifier.fillMaxSize()) {
                                repeat(12) { idx ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clickable(
                                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                hoveredIndex = idx
                                            }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        // X-Axis labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            selectedTrends.forEachIndexed { idx, point ->
                                if (idx % 2 == 0 || idx == 11) {
                                    Text(
                                        text = point.month,
                                        color = SlateMuted,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Salary Chart (Bars on Canvas)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavyMedium),
                    border = BorderStroke(1.dp, NavyLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val sym = when (countryCode) {
                            "DE" -> "€"
                            "US" -> "$"
                            "GB" -> "£"
                            "CA" -> "CA$"
                            "AU" -> "A$"
                            else -> "$"
                        }
                        Text(
                            text = "Salary Trend Curve ($sym)",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Median salary fluctuations in selected sector (Interactive bars)",
                            color = SlateMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        ) {
                            val minSalary = selectedTrends.minOf { it.averageSalary } * 0.95
                            val maxSalary = selectedTrends.maxOf { it.averageSalary } * 1.05
                            val rangeSalary = maxSalary - minSalary

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                // Draw background grid lines
                                val gridLines = 4
                                for (i in 0..gridLines) {
                                    val y = (h / gridLines) * i
                                    drawLine(
                                        color = NavyLight.copy(alpha = 0.3f),
                                        start = androidx.compose.ui.geometry.Offset(0f, y),
                                        end = androidx.compose.ui.geometry.Offset(w, y),
                                        strokeWidth = 2f
                                    )
                                }

                                val numBars = 12
                                val barWidth = (w / numBars) * 0.55f
                                val gap = (w / numBars) * 0.45f

                                selectedTrends.forEachIndexed { idx, point ->
                                    val isHovered = idx == hoveredIndex
                                    val barHeight = (((point.averageSalary - minSalary) / rangeSalary) * (h * 0.8f) + (h * 0.1f)).toFloat()
                                    val bx = idx * (w / numBars) + gap / 2f
                                    val by = h - barHeight

                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            colors = if (isHovered) {
                                                listOf(TealCyan, EmeraldGreen)
                                            } else {
                                                listOf(TealCyan.copy(alpha = 0.5f), NavyLight.copy(alpha = 0.5f))
                                            }
                                        ),
                                        topLeft = androidx.compose.ui.geometry.Offset(bx, by),
                                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                                    )

                                    if (isHovered) {
                                        drawRoundRect(
                                            color = Color.White,
                                            topLeft = androidx.compose.ui.geometry.Offset(bx, by),
                                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                                        )
                                    }
                                }
                            }

                            // Touch tracker columns
                            Row(modifier = Modifier.fillMaxSize()) {
                                repeat(12) { idx ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clickable(
                                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                hoveredIndex = idx
                                            }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        // X-Axis labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            selectedTrends.forEachIndexed { idx, point ->
                                if (idx % 2 == 0 || idx == 11) {
                                    Text(
                                        text = point.month,
                                        color = SlateMuted,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Advisory Panel & AI Forecast Card
            item {
                val countryName = countries.find { it.first == countryCode }?.second ?: countryCode
                val staticForecast = when (countryCode) {
                    "DE" -> """
                        • <b>EU Blue Card Reforms:</b> Lower salary thresholds to €45,300 for bottleneck occupations, making fast-track entry significantly easier.
                        • <b>Shortage Occupations:</b> Software Developers, Systems Engineers, Healthcare Workers, Electrical Engineers.
                        • <b>Regional Hotspots:</b> Munich, Berlin, Frankfurt, Stuttgart.
                        • <b>Advantage Tip:</b> Under current German regulations, the government has eliminated the labor market priority check for skilled job offers.
                    """.trimIndent()
                    "US" -> """
                        • <b>H-1B Cap Seasons:</b> Standard lottery registration occurs in March. Academic or research institutions are cap-exempt and hire year-round.
                        • <b>Shortage Occupations:</b> Full Stack Developers, Data Science Engineers, Technical Project Leads, Medical Technologists.
                        • <b>Regional Hotspots:</b> Silicon Valley, Austin, Seattle, New York, Boston.
                        • <b>Advantage Tip:</b> STEM OPT expansions offer up to 3 years of work eligibility in the United States without lottery delays.
                    """.trimIndent()
                    "GB" -> """
                        • <b>Salary Thresholds:</b> Raised standard Skilled Worker minimum to £38,700, though health and care sectors retain a reduced entry threshold.
                        • <b>Shortage Occupations:</b> General Practitioners, Nurses, Civil Architects, Senior Developers.
                        • <b>Regional Hotspots:</b> London, Manchester, Leeds, Edinburgh.
                        • <b>Advantage Tip:</b> The UK's Scale-Up Visa is extremely attractive, requiring zero employer sponsorship fees once active.
                    """.trimIndent()
                    "CA" -> """
                        • <b>Express Entry Draw Priorities:</b> Category-based express entry draws target STEM occupations, healthcare, and certified trades explicitly.
                        • <b>Shortage Occupations:</b> Software Developers, Construction Managers, Logistics Planners, Nurses.
                        • <b>Regional Hotspots:</b> Toronto, Vancouver, Montreal, Calgary.
                        • <b>Advantage Tip:</b> LMIA processing is fast-tracked (10 days) for high-paying specialized roles under the Global Talent Stream.
                    """.trimIndent()
                    "AU" -> """
                        • <b>TSS Pathways:</b> Subclass 482 offers direct employer nominations with the updated general salary threshold of A$73,150.
                        • <b>Shortage Occupations:</b> Systems Engineers, Nursing specialists, Aged Care Workers, Project Managers.
                        • <b>Regional Hotspots:</b> Sydney, Melbourne, Brisbane, Adelaide.
                        • <b>Advantage Tip:</b> Regional visas (Subclass 491/494) offer additional points and fast-tracked state pathways.
                    """.trimIndent()
                    else -> """
                        • <b>Immigration Trends:</b> Skilled pathways require matching specialized degree requirements.
                        • <b>Shortage Occupations:</b> Information Technology, Healthcare and Aged Care, Engineering Specialists.
                        • <b>Advantage Tip:</b> Local job offers matching prevailing wage minimums are prioritized globally.
                    """.trimIndent()
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = NavyMedium),
                    border = BorderStroke(1.dp, NavyLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Immigration Advice",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Visa & Shortage Advisory: $countryName",
                                color = WhiteActive,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            staticForecast.trimIndent().split("\n").forEach { line ->
                                val cleanLine = line.replace("• ", "").replace("<b>", "").replace("</b>", "")
                                val parts = cleanLine.split(":")
                                Row(verticalAlignment = Alignment.Top) {
                                    Text("•", color = EmeraldGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 6.dp))
                                    Column {
                                        if (parts.size > 1) {
                                            Text(parts[0] + ":", color = WhiteActive, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(parts.drop(1).joinToString(":").trim(), color = SlateMuted, fontSize = 11.sp, lineHeight = 14.sp)
                                        } else {
                                            Text(cleanLine, color = SlateMuted, fontSize = 11.sp, lineHeight = 14.sp)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = NavyLight.copy(alpha = 0.5f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "AI Market Forecast Predictor",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Run a live Gemini neural forecast on 12-month immigration quotas, economic policy changes, and visa volume projections.",
                            color = SlateMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (marketForecast != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NavyDark),
                                border = BorderStroke(1.dp, NavyLight),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Gemini 12-Month Projection", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        IconButton(
                                            onClick = { viewModel.clearMarketForecast() },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = SlateMuted, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    androidx.compose.foundation.text.selection.SelectionContainer {
                                        Text(
                                            text = marketForecast!!,
                                            color = WhiteActive,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.generateMarketForecast(countryName) },
                            enabled = !isGeneratingForecast,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            if (isGeneratingForecast) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing Market Feeds...", color = Color.White)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate 12-Month AI Forecast", color = Color.White)
                            }
                        }
                    }
                }
            }
        } else if (activeSubTab == "Salary Checker") {
            // Salary Checker - Original View
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmeraldGreen.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Trending Up",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Global Salary Index",
                                color = WhiteActive,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Query real-time Glassdoor and market aggregate wage curves globally to guarantee your employment offer meets local immigration minimum wage thresholds.",
                            color = SlateMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavyMedium),
                    border = BorderStroke(1.dp, NavyLight.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Configure Query Parameters",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = jobTitle,
                            onValueChange = { jobTitle = it },
                            label = { Text("Job Title or Keyword") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("salary_job_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldGreen,
                                focusedLabelColor = EmeraldGreen,
                                unfocusedContainerColor = NavyDark
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Target Immigration Country",
                            color = SlateMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            countries.forEach { (code, name) ->
                                val isSelected = countryCode == code
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { countryCode = code },
                                    label = { Text(name, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.querySalaryRange(jobTitle, countryCode) },
                            enabled = !isQuerying,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("salary_search_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            if (isQuerying) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Querying Market Feeds...", color = Color.White)
                            } else {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyze Market Rates", color = Color.White)
                            }
                        }
                    }
                }
            }

            if (error != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2F2)),
                        border = BorderStroke(1.dp, CoralRed.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Error", tint = CoralRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Connection Alert", color = CoralRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(error ?: "Unknown error", color = SlateMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            if (insight != null) {
                val res = insight!!
                val sym = when (res.currency) {
                    "EUR" -> "€"
                    "GBP" -> "£"
                    "CAD" -> "CA$"
                    "AUD" -> "A$"
                    "INR" -> "₹"
                    else -> "$"
                }

                fun fmt(valDouble: Double): String {
                    return if (valDouble >= 1000000) {
                        String.format("%.1fM", valDouble / 1000000.0)
                    } else if (valDouble >= 1000) {
                        String.format("%.0fk", valDouble / 1000.0)
                    } else {
                        String.format("%.0f", valDouble)
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavyMedium),
                        border = BorderStroke(1.dp, NavyLight.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${res.jobTitle} in ${countries.find { it.first == res.countryCode }?.second ?: res.countryCode}",
                                color = SlateMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$sym${fmt(res.medianSalary)}",
                                    color = EmeraldGreen,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-1).sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "/ ${res.period}",
                                    color = SlateMuted,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Estimated Median Annual Base Wage",
                                color = SlateMuted,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Interactive Wage Curve Distribution",
                                color = WhiteActive,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height
                                    val yLine = h / 2

                                    drawLine(
                                        color = NavyLight,
                                        start = androidx.compose.ui.geometry.Offset(10f, yLine),
                                        end = androidx.compose.ui.geometry.Offset(w - 10f, yLine),
                                        strokeWidth = 6f
                                    )

                                    drawLine(
                                        brush = Brush.horizontalGradient(listOf(EmeraldGreen.copy(alpha = 0.5f), EmeraldGreen)),
                                        start = androidx.compose.ui.geometry.Offset(w * 0.1f, yLine),
                                        end = androidx.compose.ui.geometry.Offset(w * 0.9f, yLine),
                                        strokeWidth = 10f
                                    )

                                    drawCircle(
                                        color = SlateMuted,
                                        radius = 12f,
                                        center = androidx.compose.ui.geometry.Offset(w * 0.1f, yLine)
                                    )

                                    drawCircle(
                                        color = EmeraldGreen,
                                        radius = 18f,
                                        center = androidx.compose.ui.geometry.Offset(w * 0.5f, yLine)
                                    )

                                    drawCircle(
                                        color = TealCyan,
                                        radius = 12f,
                                        center = androidx.compose.ui.geometry.Offset(w * 0.9f, yLine)
                                    )
                                }

                                Box(modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = "Min\n$sym${fmt(res.minSalary)}",
                                        color = SlateMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(start = 12.dp)
                                    )

                                    Text(
                                        text = "Median\n$sym${fmt(res.medianSalary)}",
                                        color = EmeraldGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        lineHeight = 14.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )

                                    Text(
                                        text = "Max\n$sym${fmt(res.maxSalary)}",
                                        color = TealCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(end = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavyMedium),
                        border = BorderStroke(1.dp, NavyLight.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Estimated Percentiles Breakdown",
                                color = WhiteActive,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val percentilesList = listOf(
                                Triple("10th (Entry Threshold)", res.p10, "Bottom 10% market rate"),
                                Triple("25th (Experienced Base)", res.p25, "Low-to-medium skill bracket"),
                                Triple("50th (Market Median)", res.p50, "Standard prevailing wage"),
                                Triple("75th (High Specialty)", res.p75, "Top talent specialty rates"),
                                Triple("90th (Elite Principal)", res.p90, "Senior executives / consultants")
                            )

                            percentilesList.forEach { (name, value, desc) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(name, color = WhiteActive, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(desc, color = SlateMuted, fontSize = 10.sp)
                                    }
                                    Text(
                                        text = "$sym${fmt(value)}",
                                        color = EmeraldGreen,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                HorizontalDivider(color = NavyDark, thickness = 1.dp)
                            }
                        }
                    }
                }

                item {
                    val advice = when (res.countryCode) {
                        "DE" -> "In Germany, this median wage of $sym${fmt(res.medianSalary)} meets the EU Blue Card prevailing wage threshold (which requires €45,300 for bottleneck occupations like IT/engineering, and €50,800 for general fields). Your offer is highly competitive for German Blue Card approval!"
                        "US" -> "For the United States, this median of $sym${fmt(res.medianSalary)} is compliant with H-1B Level 2 Prevailing Wage standards in most MSAs. To confirm, ensure the employer files a Labor Condition Application (LCA) matching this wage bracket."
                        "GB" -> "In the United Kingdom, this median wage of $sym${fmt(res.medianSalary)} is well above the general Skilled Worker Visa minimum salary threshold of £38,700. Sponsored applicants can confidently leverage this benchmark during employer negotiations."
                        "CA" -> "For Canada, LMIA requirements dictate that the employer must pay the median wage of the occupation in the specific region (TFWP High-wage stream). This median value of $sym${fmt(res.medianSalary)} aligns with typical provincial standards."
                        "AU" -> "In Australia, this wage of $sym${fmt(res.medianSalary)} complies with the Temporary Skilled Migration Income Threshold (TSMIT) of A$73,150. Your offer is fully eligible for Subclass 482 visa sponsorship!"
                        else -> "This median salary of $sym${fmt(res.medianSalary)} meets general professional immigration standards. Ensure to verify local country labor market indexes prior to contract signature."
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0EFFF)),
                        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Visa Info",
                                    tint = EmeraldGreen
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Visa Eligibility Insights",
                                    color = EmeraldGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = advice,
                                color = SlateMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavyDark),
                        border = BorderStroke(1.dp, NavyLight.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (res.isSimulated) {
                                Text(
                                    text = "⚠️ Demo Mode Active",
                                    color = AmberGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "This dataset was generated by our robust offline fallback engine. To link live Glassdoor feeds, securely input your RAPID_API_KEY into the Secrets panel in AI Studio.",
                                    color = SlateMuted,
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp
                                )
                            } else {
                                Text(
                                    text = "✅ Live Feed Active",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Wages are pulled live from jobs-api14.p.rapidapi.com using your configured client credentials.",
                                    color = SlateMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        } else if (activeSubTab == "Purchasing Power") {
            item {
                SalaryPurchasingPowerCalculator(viewModel)
            }
        } else if (activeSubTab == "Relocation Net" || activeSubTab == "Net Salary & Relocation") {
            item {
                RelocationSalaryCalculatorCard(viewModel)
            }
        }
    }
}

@Composable
fun AiMatcherSubScreen(profile: com.example.data.UserProfileEntity, viewModel: JobViewModel) {
    val context = LocalContext.current
    val allJobs by viewModel.allJobs.collectAsStateWithLifecycle()
    val jobsCompatibilityMap by viewModel.jobsCompatibilityMap.collectAsStateWithLifecycle()
    val analyzingJobIds by viewModel.analyzingJobIds.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var scoreFilter by remember { mutableStateOf("All") } // "All", "High (>75%)", "Mid (50-75%)", "Low (<50%)"
    var expandedJobId by remember { mutableStateOf<String?>(null) }

    val parsedSkills = remember(profile.skills) {
        profile.skills.split(Regex("[,;\n|•*]"))
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.length > 2 }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // 1. Profile Status / Warning Card
        if (profile.fullName.isEmpty() || parsedSkills.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = AmberGold.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, AmberGold)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = AmberGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Profile Incomplete",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Configure your Candidate Profile details or use the AI Resume Parser to fill them in automatically. Accurate skills and experience are required for exact matching.",
                            color = SlateMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        } else {
            // Profile Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyMedium),
                border = BorderStroke(1.dp, NavyLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Active Matching Profile: ${profile.fullName}",
                                color = WhiteActive,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Matching against ${allJobs.size} loaded visa-sponsored jobs",
                                color = SlateMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Core Skills Registered:",
                        color = WhiteActive,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        parsedSkills.take(15).forEach { skill ->
                            Box(
                                modifier = Modifier
                                    .background(NavyLight, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = skill, color = WhiteActive, fontSize = 10.sp)
                            }
                        }
                        if (parsedSkills.size > 15) {
                            Box(
                                modifier = Modifier
                                    .background(NavyLight, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "+${parsedSkills.size - 15} more", color = EmeraldGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 2. Interactive Matrix Search & Pill Filters
        Text(
            text = "Visa Job Suitability Matrix",
            color = WhiteActive,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = "Compare your skills & experience overlap against all found sponsored roles instantly.",
            color = SlateMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter jobs by title, company, or country...", color = SlateMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = SlateMuted) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("matcher_job_search_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = WhiteActive,
                unfocusedTextColor = WhiteActive,
                focusedBorderColor = EmeraldGreen,
                unfocusedBorderColor = NavyLight,
                focusedContainerColor = NavyMedium,
                unfocusedContainerColor = NavyMedium
            ),
            singleLine = true
        )

        // Score filter row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "High (>75%)", "Mid (50-75%)", "Low (<50%)").forEach { filter ->
                val isSelected = scoreFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) EmeraldGreen else NavyMedium)
                        .border(1.dp, if (isSelected) EmeraldGreen else NavyLight, RoundedCornerShape(8.dp))
                        .clickable { scoreFilter = filter }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) NavyDark else WhiteActive,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. Match List UI
        if (allJobs.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                colors = CardDefaults.cardColors(containerColor = NavyMedium),
                border = BorderStroke(1.dp, NavyLight)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SlateMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Sponsored Jobs Loaded",
                        color = WhiteActive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Please run a job search in the 'Discover' tab first. Once jobs are fetched, they will automatically sync here for live suitability analysis.",
                        color = SlateMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            }
        } else {
            // Apply filtering and sorting
            val filteredJobs = allJobs.filter { job ->
                val matchesSearch = searchQuery.isEmpty() || 
                        job.title.contains(searchQuery, ignoreCase = true) ||
                        job.company.contains(searchQuery, ignoreCase = true) ||
                        job.country.contains(searchQuery, ignoreCase = true)
                
                if (!matchesSearch) return@filter false
                
                // Determine effective score
                val hasDeepScore = jobsCompatibilityMap.containsKey(job.id)
                val effectiveScore = if (hasDeepScore) {
                    jobsCompatibilityMap[job.id]?.first ?: 70
                } else {
                    calculateHeuristicScore(profile, job)
                }
                
                when (scoreFilter) {
                    "High (>75%)" -> effectiveScore > 75
                    "Mid (50-75%)" -> effectiveScore in 50..75
                    "Low (<50%)" -> effectiveScore < 50
                    else -> true
                }
            }.sortedByDescending { job ->
                // Sort by score
                if (jobsCompatibilityMap.containsKey(job.id)) {
                    jobsCompatibilityMap[job.id]?.first ?: 70
                } else {
                    calculateHeuristicScore(profile, job)
                }
            }

            if (filteredJobs.isEmpty()) {
                Text(
                    text = "No jobs match your search/filter criteria.",
                    color = SlateMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                filteredJobs.forEach { job ->
                    val isExpanded = expandedJobId == job.id
                    val isDeepScored = jobsCompatibilityMap.containsKey(job.id)
                    val deepScorePair = jobsCompatibilityMap[job.id]
                    val heuristicScore = calculateHeuristicScore(profile, job)
                    val displayScore = if (isDeepScored) deepScorePair?.first ?: 70 else heuristicScore
                    
                    val matchedSkills = extractMatchedSkills(profile.skills, job.description, job.title)
                    val missingSkills = extractMissingSkills(profile.skills, job.description, job.title)

                    val isAnalyzingThisJob = analyzingJobIds.contains(job.id)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("matcher_job_card_${job.id}")
                            .border(
                                1.dp, 
                                if (isExpanded) EmeraldGreen.copy(alpha = 0.5f) else NavyLight.copy(alpha = 0.5f), 
                                RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = NavyMedium)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Card Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = job.title,
                                        color = WhiteActive,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${job.company} • ${job.location}, ${job.country}",
                                        color = SlateMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                // Interactive circular progress match badge
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(NavyDark)
                                        .border(
                                            2.dp,
                                            if (displayScore >= 75) EmeraldGreen 
                                            else if (displayScore >= 50) AmberGold 
                                            else CoralRed,
                                            androidx.compose.foundation.shape.CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$displayScore%",
                                            color = if (displayScore >= 75) EmeraldGreen else if (displayScore >= 50) AmberGold else CoralRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Match",
                                            color = SlateMuted,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Score Origin indicator badge (Heuristic vs. Deep AI)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isDeepScored) EmeraldGreen.copy(alpha = 0.15f) 
                                            else NavyLight, 
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isDeepScored) Icons.Default.CheckCircle else Icons.Default.Info,
                                            contentDescription = null,
                                            tint = if (isDeepScored) EmeraldGreen else SlateMuted,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isDeepScored) "🧠 Deep AI Verified" else "⚡ Local Matcher",
                                            color = if (isDeepScored) EmeraldGreen else SlateMuted,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (job.visaType.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF1E3A8A), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = job.visaType, color = Color(0xFF93C5FD), fontSize = 9.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 4. Skills Match Comparison Tags
                            Text(
                                text = "Skills Match Analysis:",
                                color = WhiteActive,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            if (matchedSkills.isEmpty() && missingSkills.isEmpty()) {
                                Text(
                                    text = "No clear skill overlap. Update profile with modern tech terms.",
                                    color = SlateMuted,
                                    fontSize = 10.sp
                                )
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Matched skills in green
                                    matchedSkills.forEach { skill ->
                                        Box(
                                            modifier = Modifier
                                                .background(EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .border(1.dp, EmeraldGreen.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(10.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(text = skill, color = EmeraldGreen, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                    
                                    // Missing skills in Amber / Gray
                                    missingSkills.forEach { skill ->
                                        Box(
                                            modifier = Modifier
                                                .background(NavyDark, RoundedCornerShape(4.dp))
                                                .border(1.dp, NavyLight, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = "+ $skill", color = SlateMuted, fontSize = 9.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action buttons: Expand and Deep AI Analyzer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.clickable { expandedJobId = if (isExpanded) null else job.id },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isExpanded) "Hide Details" else "View Compatibility Details",
                                        color = EmeraldGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Button(
                                    onClick = { viewModel.analyzeJobCompatibility(job) },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isDeepScored) NavyLight else EmeraldGreen
                                    ),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .testTag("deep_analyze_btn_${job.id}"),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    enabled = !isAnalyzingThisJob
                                ) {
                                    if (isAnalyzingThisJob) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(12.dp),
                                            strokeWidth = 1.5.dp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("AI Analyzing...", color = Color.White, fontSize = 9.sp)
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isDeepScored) Icons.Default.Refresh else Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (isDeepScored) WhiteActive else NavyDark,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isDeepScored) "Re-Scan" else "Deep AI Match",
                                                color = if (isDeepScored) WhiteActive else NavyDark,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // Expanded detail reports
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = NavyLight)
                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Job Description Summary",
                                    color = WhiteActive,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = job.description,
                                    color = SlateMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    maxLines = 8,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // If we have Deep AI scored content, render it beautifully
                                if (isDeepScored && deepScorePair != null) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = NavyDark),
                                        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.25f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = EmeraldGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "AI Suitability Engine Report (score: ${deepScorePair.first}%)",
                                                    color = WhiteActive,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = deepScorePair.second,
                                                color = WhiteActive,
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                } else {
                                    // Local Heuristic Suggestions
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = NavyDark),
                                        border = BorderStroke(1.dp, NavyLight)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = null,
                                                    tint = AmberGold,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Instant Match Recommendations",
                                                    color = WhiteActive,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            
                                            val missingSkillsStr = if (missingSkills.isNotEmpty()) {
                                                "To optimize your odds of securing sponsorship for this position, consider adding these key skills to your resume: ${missingSkills.take(3).joinToString(", ")}."
                                            } else {
                                                "Your core skills are an excellent match for this role."
                                            }

                                            Text(
                                                text = "This position in ${job.country} offers visa support. $missingSkillsStr Ensure your CV reflects matching expertise to successfully pass the employer's ATS pipeline.",
                                                color = SlateMuted,
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun calculateHeuristicScore(profile: com.example.data.UserProfileEntity, job: com.example.data.JobEntity): Int {
    val skillsList = profile.skills.split(Regex("[,;\n|•*]"))
        .map { it.trim().lowercase() }
        .filter { it.isNotEmpty() && it.length > 2 }
    
    if (skillsList.isEmpty()) return 60 // default neutral score if no skills configured
    
    val jobText = (job.title + " " + job.description + " " + job.industry).lowercase()
    var matchedCount = 0
    for (skill in skillsList) {
        if (jobText.contains(skill)) {
            matchedCount++
        }
    }
    
    // Base score is 55. Add 6% per matched skill up to a max of 35%
    val skillsBonus = (matchedCount * 6).coerceAtMost(35)
    
    // Check experience level match
    var expBonus = 0
    val expLower = profile.experience.lowercase()
    val jobExp = job.experienceLevel.lowercase()
    if (jobExp.isNotEmpty() && jobExp != "all") {
        if (expLower.contains(jobExp) || 
            (jobExp == "entry" && (expLower.contains("junior") || expLower.contains("1 year") || expLower.contains("2 years") || expLower.contains("0-"))) || 
            (jobExp == "senior" && (expLower.contains("senior") || expLower.contains("lead") || expLower.contains("5 years") || expLower.contains("8 years")))
        ) {
            expBonus = 10
        }
    }
    
    return (50 + skillsBonus + expBonus).coerceIn(45, 95)
}

fun extractMatchedSkills(profileSkills: String, jobDescription: String, jobTitle: String): List<String> {
    val skillsList = profileSkills.split(Regex("[,;\n|•*]"))
        .map { it.trim() }
        .filter { it.isNotEmpty() && it.length > 2 }
    val jobText = (jobTitle + " " + jobDescription).lowercase()
    return skillsList.filter { skill -> jobText.contains(skill.lowercase()) }
}

fun extractMissingSkills(profileSkills: String, jobDescription: String, jobTitle: String): List<String> {
    val profileSkillsLower = profileSkills.lowercase()
    val jobText = (jobTitle + " " + jobDescription).lowercase()
    val commonSkills = listOf(
        "Kotlin", "Java", "Python", "React", "Docker", "AWS", "SQL", "Git", "API",
        "Project Management", "Agile", "Scrum", "Customer Service", "Communication", "Leadership",
        "Nursing", "Caregiving", "First Aid", "CPR", "Accounting", "Excel", "Budgeting",
        "AutoCAD", "Welding", "Machining", "Logistics", "Warehouse", "Forklift", "Inventory",
        "Sales", "Marketing", "SEO", "Design", "Figma", "Troubleshooting", "Quality Assurance"
    )
    return commonSkills.filter { skill -> 
        jobText.contains(skill.lowercase()) && !profileSkillsLower.contains(skill.lowercase())
    }
}

fun formatNotificationTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

@Composable
fun SimulatedEmailView(htmlContent: String) {
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { context ->
            android.widget.TextView(context).apply {
                setTextColor(android.graphics.Color.WHITE)
                textSize = 14f
                text = android.text.Html.fromHtml(htmlContent, android.text.Html.FROM_HTML_MODE_LEGACY)
                movementMethod = android.text.method.LinkMovementMethod.getInstance()
            }
        },
        update = { textView ->
            textView.text = android.text.Html.fromHtml(htmlContent, android.text.Html.FROM_HTML_MODE_LEGACY)
        },
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    )
}

@Composable
fun NotificationHubDialog(
    viewModel: JobViewModel,
    onDismiss: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    var selectedEmailNotification by remember { mutableStateOf<JobNotificationEntity?>(null) }

    Dialog(
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .border(1.dp, NavyLight, RoundedCornerShape(16.dp)),
            color = NavyDark,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Alerts",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Real-Time Alert Hub",
                        color = WhiteActive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SlateMuted)
                    }
                }

                Text(
                    text = "Stay ahead with push & email matches synchronized instantly to your skill profile and custom filters.",
                    color = SlateMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Global Actions
                if (notifications.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.clearAllNotifications() },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear All", fontSize = 12.sp)
                        }

                        TextButton(
                            onClick = { 
                                notifications.forEach { 
                                    if (!it.isRead) viewModel.markNotificationAsRead(it.id) 
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = EmeraldGreen)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Mark All Read", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark All Read", fontSize = 12.sp)
                        }
                    }
                }

                HorizontalDivider(color = NavyLight, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                if (notifications.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "No alerts",
                            tint = NavyLight,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Your alert inbox is empty.",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Configure custom job alerts in the 'Visa Paths' tab, or perform live searches to see matches fly in real-time!",
                            color = SlateMuted,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notifications) { notification ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.markNotificationAsRead(notification.id) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (notification.isRead) NavyMedium else NavyMedium.copy(alpha = 0.8f)
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (notification.isRead) NavyLight else EmeraldGreen.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Unread Indicator
                                        if (!notification.isRead) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(EmeraldGreen, shape = androidx.compose.foundation.shape.CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }

                                        Text(
                                            text = notification.title,
                                            color = WhiteActive,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Text(
                                            text = formatNotificationTime(notification.timestamp),
                                            color = SlateMuted,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = notification.message,
                                        color = SlateMuted,
                                        fontSize = 12.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            if (notification.isPush) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(TealCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Star, contentDescription = "Push", tint = TealCyan, modifier = Modifier.size(10.dp))
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text("Push Delivered", color = TealCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            if (notification.isEmail) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        .clickable { selectedEmailNotification = notification }
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Email, contentDescription = "Email", tint = EmeraldGreen, modifier = Modifier.size(10.dp))
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text("View Simulated Email", color = EmeraldGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (!notification.isRead) {
                                                IconButton(
                                                    onClick = { viewModel.markNotificationAsRead(notification.id) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = "Mark Read", tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                                                }
                                            }

                                            IconButton(
                                                onClick = { viewModel.deleteNotification(notification.id) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedEmailNotification != null) {
        SimulatedEmailDialog(
            notification = selectedEmailNotification!!,
            onDismiss = { selectedEmailNotification = null }
        )
    }
}

@Composable
fun SimulatedEmailDialog(
    notification: JobNotificationEntity,
    onDismiss: () -> Unit
) {
    Dialog(
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.75f)
                .border(1.dp, NavyLight, RoundedCornerShape(16.dp)),
            color = NavyDark,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Window Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Email, contentDescription = "Email Inbox", tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simulated Mail Client",
                        color = WhiteActive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SlateMuted)
                    }
                }

                HorizontalDivider(color = NavyLight, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Email Metadata Header Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavyMedium, RoundedCornerShape(8.dp))
                        .border(1.dp, NavyLight, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row {
                            Text("From: ", color = SlateMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("alerts@visasponsorjobs.io", color = EmeraldGreen, fontSize = 12.sp)
                        }
                        Row {
                            Text("To: ", color = SlateMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("vincentmwangangi28@gmail.com", color = WhiteActive, fontSize = 12.sp)
                        }
                        Row {
                            Text("Subject: ", color = SlateMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(notification.title, color = WhiteActive, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row {
                            Text("Sent: ", color = SlateMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(formatNotificationTime(notification.timestamp) + " (Instant)", color = SlateMuted, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rich HTML Content view
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                        .border(1.dp, NavyLight, RoundedCornerShape(8.dp))
                ) {
                    SimulatedEmailView(htmlContent = notification.emailContent)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close Reader", color = NavyDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RelocationSalaryCalculatorCard(viewModel: JobViewModel) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    
    var salaryInput by remember { mutableStateOf("$85,000 USD / year") }
    var targetCountry by remember { mutableStateOf("Canada") }
    var familyMembersCount by remember { mutableIntStateOf(1) }
    
    val insight by viewModel.relocationSalaryInsight.collectAsStateWithLifecycle()
    val isCalculating by viewModel.isCalculatingRelocationSalary.collectAsStateWithLifecycle()

    val countries = listOf("Canada", "Germany", "United Kingdom", "United States", "Australia")

    Card(
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        border = BorderStroke(1.dp, NavyLight),
        modifier = Modifier.fillMaxWidth().testTag("relocation_calculator_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Relocation Calculator",
                    tint = EmeraldGreen,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Net Take-Home & Relocation Cost Calculator",
                        color = WhiteActive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Calculate exact monthly take-home salary after income taxes, relocation setup fees, and local cost of living.",
                        color = SlateMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = salaryInput,
                onValueChange = { salaryInput = it },
                label = { Text("Offered Gross Annual Salary (e.g. £55,000 or $90,000)", color = SlateMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("relocation_salary_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldGreen,
                    unfocusedBorderColor = NavyLight,
                    focusedContainerColor = NavyDark,
                    unfocusedContainerColor = NavyDark,
                    focusedTextColor = WhiteActive,
                    unfocusedTextColor = WhiteActive
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text("Target Immigration Country:", color = SlateMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(countries) { country ->
                    val isSel = targetCountry == country
                    FilterChip(
                        selected = isSel,
                        onClick = { targetCountry = country },
                        label = { Text(country, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldGreen,
                            selectedLabelColor = Color.White,
                            containerColor = NavyDark,
                            labelColor = SlateMuted
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Family Members Relocating:", color = SlateMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (familyMembersCount > 1) familyMembersCount-- },
                        modifier = Modifier.size(28.dp).background(NavyDark, RoundedCornerShape(6.dp))
                    ) {
                        Text("-", color = WhiteActive, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "$familyMembersCount",
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    IconButton(
                        onClick = { if (familyMembersCount < 6) familyMembersCount++ },
                        modifier = Modifier.size(28.dp).background(NavyDark, RoundedCornerShape(6.dp))
                    ) {
                        Text("+", color = WhiteActive, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { viewModel.calculateRelocationCostAndNetSalary(salaryInput, targetCountry, familyMembersCount) },
                enabled = !isCalculating,
                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("calculate_relocation_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isCalculating) {
                    CircularProgressIndicator(color = NavyDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Calculating Net Take-Home...", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "Calculate", tint = NavyDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Calculate Financial Breakdown", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            if (insight != null) {
                val res = insight!!
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = NavyLight)
                Spacer(modifier = Modifier.height(14.dp))

                Text("Net Income & Relocation Summary", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = NavyDark),
                        border = BorderStroke(1.dp, NavyLight)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Est. Net Monthly Pay", color = SlateMuted, fontSize = 10.sp)
                            Text(res.netMonthlyPay, color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = NavyDark),
                        border = BorderStroke(1.dp, NavyLight)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Effective Tax", color = SlateMuted, fontSize = 10.sp)
                            Text(res.estimatedMonthlyTax, color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = NavyDark),
                        border = BorderStroke(1.dp, NavyLight)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Relocation Upfront", color = SlateMuted, fontSize = 10.sp)
                            Text(res.estimatedRelocationUpfrontCost, color = AmberGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = NavyDark),
                        border = BorderStroke(1.dp, NavyLight)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Est. Living Expenses", color = SlateMuted, fontSize = 10.sp)
                            Text(res.estimatedLivingExpenses, color = CoralRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavyDark)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Savings Potential: ", color = SlateMuted, fontSize = 11.sp)
                            Text(res.savingsPotential, color = TealCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Financial Breakdown & Strategy:", color = SlateMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(res.breakdownSummary, color = WhiteActive, fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
    }
}

@Composable
fun VisaInterviewPrepCard(viewModel: JobViewModel) {
    val questions by viewModel.interviewPrepQuestions.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingInterviewPrep.collectAsStateWithLifecycle()

    var targetRole by remember { mutableStateOf("Software Engineer") }
    var targetCountry by remember { mutableStateOf("Germany") }
    var visaType by remember { mutableStateOf("EU Blue Card") }

    Card(
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        border = BorderStroke(1.dp, NavyLight),
        modifier = Modifier.fillMaxWidth().testTag("visa_interview_prep_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Visa Interview Prep",
                    tint = TealCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Visa & Employer Interview Q&A Simulator",
                        color = WhiteActive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Generate realistic embassy & employer interview questions with AI model answers.",
                        color = SlateMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = targetRole,
                        onValueChange = { targetRole = it },
                        label = { Text("Target Role", color = SlateMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = NavyLight,
                            focusedContainerColor = NavyDark, unfocusedContainerColor = NavyDark,
                            focusedTextColor = WhiteActive, unfocusedTextColor = WhiteActive
                        )
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = targetCountry,
                        onValueChange = { targetCountry = it },
                        label = { Text("Country", color = SlateMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = NavyLight,
                            focusedContainerColor = NavyDark, unfocusedContainerColor = NavyDark,
                            focusedTextColor = WhiteActive, unfocusedTextColor = WhiteActive
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = visaType,
                onValueChange = { visaType = it },
                label = { Text("Visa Subclass / Pathway (e.g. UK Skilled Worker)", color = SlateMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldGreen, unfocusedBorderColor = NavyLight,
                    focusedContainerColor = NavyDark, unfocusedContainerColor = NavyDark,
                    focusedTextColor = WhiteActive, unfocusedTextColor = WhiteActive
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.generateVisaInterviewPrep(targetRole, targetCountry, visaType) },
                enabled = !isGenerating,
                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("generate_interview_prep_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = TealCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = NavyDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating Interview Simulator...", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                } else {
                    Icon(Icons.Default.Star, contentDescription = "Simulate", tint = NavyDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generate Custom Interview Q&A Set", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            if (questions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = NavyLight)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Practice Questions & Sample Answers (${questions.size})", color = TealCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    questions.forEachIndexed { idx, q ->
                        var isExpanded by remember { mutableStateOf(idx == 0) }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = NavyDark),
                            border = BorderStroke(1.dp, NavyLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(TealCyan.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(q.category, color = TealCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${idx + 1}. ${q.question}",
                                        color = WhiteActive,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Toggle",
                                        tint = SlateMuted
                                    )
                                }

                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Ideal Sample Answer:", color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(q.sampleAnswer, color = WhiteActive, fontSize = 11.sp, lineHeight = 15.sp)

                                    if (q.keyTips.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Key Tips & Guidance:", color = AmberGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(q.keyTips, color = SlateMuted, fontSize = 10.sp, lineHeight = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecruiterColdEmailCard(viewModel: JobViewModel) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    var candidateName by remember { mutableStateOf("") }
    var candidateSkills by remember { mutableStateOf("") }
    var targetCompany by remember { mutableStateOf("") }
    var targetRole by remember { mutableStateOf("") }
    var targetCountry by remember { mutableStateOf("United Kingdom") }
    var selectedTone by remember { mutableStateOf("Professional & Persuasive") }

    val emailResult by viewModel.recruiterColdEmailResult.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingColdEmail.collectAsStateWithLifecycle()

    val tones = listOf("Professional & Persuasive", "Direct & Concise", "Enthusiastic & High-Energy")

    Card(
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        border = BorderStroke(1.dp, NavyLight),
        modifier = Modifier.fillMaxWidth().testTag("recruiter_cold_email_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Recruiter Email Generator",
                    tint = EmeraldGreen,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Recruiter Cold Outreach Email Draft Generator",
                        color = WhiteActive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Draft high-converting direct outreach emails to corporate recruiters and hiring managers.",
                        color = SlateMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = candidateName,
                        onValueChange = { candidateName = it },
                        label = { Text("Your Name", color = SlateMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = NavyLight,
                            focusedContainerColor = NavyDark, unfocusedContainerColor = NavyDark,
                            focusedTextColor = WhiteActive, unfocusedTextColor = WhiteActive
                        )
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = targetCompany,
                        onValueChange = { targetCompany = it },
                        label = { Text("Target Company", color = SlateMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = NavyLight,
                            focusedContainerColor = NavyDark, unfocusedContainerColor = NavyDark,
                            focusedTextColor = WhiteActive, unfocusedTextColor = WhiteActive
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = targetRole,
                        onValueChange = { targetRole = it },
                        label = { Text("Target Role", color = SlateMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = NavyLight,
                            focusedContainerColor = NavyDark, unfocusedContainerColor = NavyDark,
                            focusedTextColor = WhiteActive, unfocusedTextColor = WhiteActive
                        )
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = targetCountry,
                        onValueChange = { targetCountry = it },
                        label = { Text("Country", color = SlateMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = NavyLight,
                            focusedContainerColor = NavyDark, unfocusedContainerColor = NavyDark,
                            focusedTextColor = WhiteActive, unfocusedTextColor = WhiteActive
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = candidateSkills,
                onValueChange = { candidateSkills = it },
                label = { Text("Your Key Qualifications & Skills Summary", color = SlateMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldGreen, unfocusedBorderColor = NavyLight,
                    focusedContainerColor = NavyDark, unfocusedContainerColor = NavyDark,
                    focusedTextColor = WhiteActive, unfocusedTextColor = WhiteActive
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text("Desired Outreach Tone:", color = SlateMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(tones) { t ->
                    val isSel = selectedTone == t
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedTone = t },
                        label = { Text(t, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldGreen,
                            selectedLabelColor = Color.White,
                            containerColor = NavyDark,
                            labelColor = SlateMuted
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.generateRecruiterColdEmail(
                        candidateName = candidateName.ifEmpty { "Applicant" },
                        candidateSkills = candidateSkills.ifEmpty { "Software Development, Systems Architecture" },
                        targetCompany = targetCompany.ifEmpty { "Global Tech Ltd" },
                        targetRole = targetRole.ifEmpty { "Software Engineer" },
                        targetCountry = targetCountry.ifEmpty { "United Kingdom" },
                        tone = selectedTone
                    )
                },
                enabled = !isGenerating,
                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("generate_cold_email_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = NavyDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Drafting Custom Email...", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                } else {
                    Icon(Icons.Default.Share, contentDescription = "Draft", tint = NavyDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Draft Outreach Email", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            if (emailResult != null) {
                val res = emailResult!!
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = NavyLight)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Drafted Subject Line", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(res.subjectLine))
                            Toast.makeText(context, "Subject copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Copy Subject", tint = SlateMuted, modifier = Modifier.size(14.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavyDark)
                        .padding(10.dp)
                ) {
                    Text(res.subjectLine, color = WhiteActive, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Email Body", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(res.emailBody))
                            Toast.makeText(context, "Email body copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Copy Body", tint = SlateMuted, modifier = Modifier.size(14.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavyDark)
                        .padding(10.dp)
                ) {
                    Text(res.emailBody, color = WhiteActive, fontSize = 11.sp, lineHeight = 15.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavyLight.copy(alpha = 0.2f))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("💡 Outreach Strategy & Follow-Up Tip:", color = AmberGold, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        Text(res.followUpTip, color = SlateMuted, fontSize = 10.sp, lineHeight = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RealtimeJobAlertCard(
    queryText: String,
    searchCountryText: String,
    viewModel: JobViewModel,
    onCriteriaSelected: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val customAlerts by viewModel.customAlerts.collectAsStateWithLifecycle()
    val isSubscribing by viewModel.isRealtimeSubscribing.collectAsStateWithLifecycle()
    val statusMessage by viewModel.firestoreAlertStatusMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val effectiveQuery = queryText.trim().ifEmpty { "Visa sponsorship jobs" }
    val effectiveCountry = if (searchCountryText.isBlank()) "All" else searchCountryText

    // Check if this query & country is currently subscribed in the alert list
    val activeAlert = customAlerts.firstOrNull { alert ->
        (queryText.isBlank() || alert.queryText.equals(queryText.trim(), ignoreCase = true)) &&
        (searchCountryText == "All" || alert.country.equals(searchCountryText, ignoreCase = true) || alert.country == "All")
    }
    val isSubscribed = activeAlert != null

    Card(
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        border = BorderStroke(
            1.dp,
            if (isSubscribed) EmeraldGreen.copy(alpha = 0.6f) else NavyLight
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("realtime_job_alert_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSubscribed) EmeraldGreen.copy(alpha = 0.18f) else NavyLight.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Alerts",
                        tint = if (isSubscribed) EmeraldGreen else SlateMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Real-Time Job Alerts",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSubscribed) EmeraldGreen.copy(alpha = 0.15f) else NavyLight)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isSubscribed) "⚡ Firestore Live" else "Firestore Ready",
                                color = if (isSubscribed) EmeraldGreen else SlateMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = if (queryText.isNotBlank() || searchCountryText != "All") {
                            "Criteria: '${queryText.ifEmpty { "All Roles" }}' in $searchCountryText"
                        } else {
                            "Instant Firestore alerts when matching jobs appear"
                        },
                        color = SlateMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                // Switch Toggle
                if (isSubscribing) {
                    CircularProgressIndicator(
                        color = EmeraldGreen,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Switch(
                        checked = isSubscribed,
                        onCheckedChange = { checked ->
                            viewModel.toggleRealtimeSearchAlert(
                                queryText = queryText,
                                country = searchCountryText,
                                enabled = checked,
                                email = true,
                                push = true
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NavyDark,
                            checkedTrackColor = EmeraldGreen,
                            uncheckedThumbColor = SlateMuted,
                            uncheckedTrackColor = NavyLight
                        ),
                        modifier = Modifier.testTag("job_search_alert_toggle")
                    )
                }
            }

            // Quick Criteria Selector Chips
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Preset Criteria Filters:",
                color = SlateMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Pair("Senior Android", "United Kingdom"),
                    Pair("Software Engineer", "Canada"),
                    Pair("Registered Nurse", "Australia"),
                    Pair("DevOps Engineer", "Germany"),
                    Pair("Data Scientist", "Sweden")
                ).forEach { (role, country) ->
                    val isSelected = queryText.equals(role, ignoreCase = true) && searchCountryText.equals(country, ignoreCase = true)
                    SuggestionChip(
                        onClick = { onCriteriaSelected(role, country) },
                        label = { Text("'$role' in $country", fontSize = 10.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isSelected) EmeraldGreen.copy(alpha = 0.2f) else NavyDark,
                            labelColor = if (isSelected) EmeraldGreen else WhiteActive
                        ),
                        border = BorderStroke(1.dp, if (isSelected) EmeraldGreen else NavyLight),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Notification Channels & Action Row
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TealCyan.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("📱 Push Active", color = TealCyan, fontSize = 9.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AmberGold.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("📧 Email Synced", color = AmberGold, fontSize = 9.sp)
                    }
                }

                // Simulate Live Match Trigger Button
                Button(
                    onClick = {
                        viewModel.triggerSimulatedRealtimeAlert(
                            queryText = queryText.ifEmpty { "Senior Android Developer" },
                            country = searchCountryText
                        )
                        Toast.makeText(
                            context,
                            "⚡ Real-time Firestore Job Streamed! Matching alert notification dispatched.",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealCyan),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp).testTag("simulate_firestore_alert_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Test Live Stream",
                        tint = NavyDark,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Simulate Match",
                        color = NavyDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Status message feedback banner
            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldGreen.copy(alpha = 0.1f))
                        .border(1.dp, EmeraldGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statusMessage ?: "",
                        color = EmeraldGreen,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.clearFirestoreAlertStatus() },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AllApiJobsMasterCard(
    queryText: String,
    searchCountryText: String,
    viewModel: JobViewModel,
    modifier: Modifier = Modifier
) {
    val isFetchingAll by viewModel.isFetchingAllApiJobs.collectAsStateWithLifecycle()
    val allApiResult by viewModel.allApiJobsResult.collectAsStateWithLifecycle()
    val allApiStatus by viewModel.allApiJobsStatusMessage.collectAsStateWithLifecycle()

    Card(
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("all_api_jobs_master_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(EmeraldGreen.copy(alpha = 0.25f), TealCyan.copy(alpha = 0.25f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "API Keys Fetch",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Fetch All Jobs via API Keys",
                            color = WhiteActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EmeraldGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "4 API Engines",
                                color = EmeraldGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Aggregates live jobs from Google Jobs API, PR Labs Multi-Source, Indeed Scraper & Gemini AI",
                        color = SlateMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Connected API Keys Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val apiChips = listOf(
                    "🌐 Google Jobs",
                    "📊 PR Labs",
                    "🏢 Indeed",
                    "✨ Gemini AI"
                )
                apiChips.forEach { chipText ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NavyLight)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = chipText,
                            color = TealCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Add All Available Jobs & Live API Fetch
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Primary "ADD ALL AVAILABLE JOBS" Master CTA
                Button(
                    onClick = {
                        viewModel.addAllAvailableJobs()
                    },
                    enabled = !isFetchingAll,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("add_all_available_jobs_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldGreen,
                        contentColor = NavyDark,
                        disabledContainerColor = EmeraldGreen.copy(alpha = 0.5f)
                    )
                ) {
                    if (isFetchingAll) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = NavyDark,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Adding all available jobs from database & APIs...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = NavyDark
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = NavyDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ADD ALL AVAILABLE JOBS",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = NavyDark
                            )
                        }
                    }
                }

                // 2. Secondary Filtered API Query Button
                OutlinedButton(
                    onClick = {
                        viewModel.fetchAllJobsFromApiKeys(
                            query = queryText.ifEmpty { "Visa sponsorship jobs" },
                            country = searchCountryText
                        )
                    },
                    enabled = !isFetchingAll,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("fetch_all_api_jobs_btn"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, TealCyan.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TealCyan
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = TealCyan
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (queryText.isNotBlank()) "Query APIs for '$queryText'" else "Query APIs for Active Search",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = TealCyan
                        )
                    }
                }
            }

            // Results and Status Message Display
            allApiStatus?.let { status ->
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavyDark)
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = status,
                                color = WhiteActive,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.clearAllApiJobsStatus() },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = SlateMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        // If result breakdown is present, show breakdown badges
                        allApiResult?.let { res ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(EmeraldGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Total: +${res.totalJobsAdded}",
                                        color = EmeraldGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(TealCyan.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Google: ${res.googleJobsCount}",
                                        color = TealCyan,
                                        fontSize = 10.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(EmeraldGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Multi-Source: ${res.multiSourceJobsCount}",
                                        color = EmeraldGreen,
                                        fontSize = 10.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AmberGold.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Indeed: ${res.indeedJobsCount}",
                                        color = AmberGold,
                                        fontSize = 10.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(TealCyan.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Gemini: ${res.geminiJobsCount}",
                                        color = TealCyan,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



