package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.DashboardScreen
import com.example.ui.JobViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: JobViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleDeepLinkIntent(intent)
        setContent {
            MyApplicationTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        val uri: Uri? = intent?.data
        if (uri != null) {
            val scheme = uri.scheme
            val host = uri.host
            if (scheme == "globalvisajobs" && host == "linkedin-callback") {
                viewModel.handleLinkedInOAuthCallback(uri)
            } else if (scheme == "https" && host == "globalvisajobs.com" && uri.path?.contains("linkedin/callback") == true) {
                viewModel.handleLinkedInOAuthCallback(uri)
            }
        }
    }
}
