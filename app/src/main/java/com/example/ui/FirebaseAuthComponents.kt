package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.auth.AppUser
import com.example.auth.AuthState
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val GoogleBlue = Color(0xFF4285F4)
private val GoogleRed = Color(0xFFEA4335)
private val GoogleYellow = Color(0xFFFBBC05)
private val GoogleGreen = Color(0xFF34A853)

@Composable
fun GoogleFirebaseAuthCard(
    viewModel: JobViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsState()
    val lastSyncTimestamp by viewModel.lastCloudSyncTimestamp.collectAsState()
    val syncStatusMessage by viewModel.cloudSyncStatusMessage.collectAsState()

    val rotationTransition = rememberInfiniteTransition(label = "sync_rotation")
    val syncRotation by rotationTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("firebase_auth_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            when (val state = authState) {
                is AuthState.Authenticated -> {
                    AuthenticatedUserView(
                        user = state.user,
                        isCloudSyncing = isCloudSyncing,
                        lastSyncTimestamp = lastSyncTimestamp,
                        syncStatusMessage = syncStatusMessage,
                        syncRotation = syncRotation,
                        onSyncNow = {
                            viewModel.performCloudSync { success, message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onSignOut = {
                            viewModel.signOut()
                            Toast.makeText(context, "Signed out of Google session", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                is AuthState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = GoogleBlue,
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Authenticating with Google & Firebase...",
                            color = WhiteActive,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                else -> {
                    UnauthenticatedUserView(
                        onGoogleSignIn = {
                            viewModel.signInWithGoogle(context) { success, message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onOneTapDemo = {
                            viewModel.signInWithDemoGoogleAccount(
                                email = "vincentmwangangi28@gmail.com",
                                name = "Vincent Mwangangi"
                            )
                            Toast.makeText(context, "Connected as Vincent Mwangangi via Google Auth", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedUserView(
    user: AppUser,
    isCloudSyncing: Boolean,
    lastSyncTimestamp: Long,
    syncStatusMessage: String?,
    syncRotation: Float,
    onSyncNow: () -> Unit,
    onSignOut: () -> Unit
) {
    val formattedTime = remember(lastSyncTimestamp) {
        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        sdf.format(Date(lastSyncTimestamp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Profile Avatar
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(GoogleBlue, GoogleGreen)
                    )
                )
                .border(2.dp, EmeraldGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (!user.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Text(
                    text = user.displayName.take(2).uppercase(),
                    color = WhiteActive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user.displayName,
                    color = WhiteActive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = EmeraldGreen.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Google",
                            color = EmeraldGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = user.email,
                color = SlateMuted,
                fontSize = 12.sp
            )

            Text(
                text = "UID: ${user.uid.take(12)}... • Firestore Synced",
                color = SlateMuted.copy(alpha = 0.8f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Realtime Sync Status Banner
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = NavyDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Cloud Status",
                    tint = if (isCloudSyncing) GoogleYellow else EmeraldGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isCloudSyncing) "Syncing with Firebase Cloud..." else (syncStatusMessage ?: "Cloud Persistence Active"),
                        color = if (isCloudSyncing) GoogleYellow else WhiteActive,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Last synced: $formattedTime",
                        color = SlateMuted,
                        fontSize = 10.sp
                    )
                }
            }

            // Sync Now Button
            IconButton(
                onClick = onSyncNow,
                enabled = !isCloudSyncing,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("sync_now_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sync Now",
                    tint = if (isCloudSyncing) GoogleYellow else EmeraldGreen,
                    modifier = Modifier
                        .size(18.dp)
                        .then(if (isCloudSyncing) Modifier.rotate(syncRotation) else Modifier)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Synced Data Types Chips
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SyncItemChip(icon = Icons.Default.Star, label = "Jobs", modifier = Modifier.weight(1f))
        SyncItemChip(icon = Icons.Default.Person, label = "Profile", modifier = Modifier.weight(1f))
        SyncItemChip(icon = Icons.Default.Check, label = "Visas", modifier = Modifier.weight(1f))
        SyncItemChip(icon = Icons.Default.Lock, label = "Vault", modifier = Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Action Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onSyncNow,
            enabled = !isCloudSyncing,
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .testTag("manual_sync_button"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = EmeraldGreen
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(listOf(EmeraldGreen, GoogleBlue))
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Sync Cloud", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .testTag("sign_out_button"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = SlateMuted
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(listOf(NavyLight, NavyLight))
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = SlateMuted
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Sign Out", fontSize = 12.sp, color = SlateMuted)
        }
    }
}

@Composable
private fun UnauthenticatedUserView(
    onGoogleSignIn: () -> Unit,
    onOneTapDemo: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(NavyDark)
                .border(1.dp, NavyLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Guest",
                tint = SlateMuted,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Firebase Cloud Account",
                color = WhiteActive,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "Persist matching profile, jobs & documents across sessions",
                color = SlateMuted,
                fontSize = 12.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Primary Google Sign In Button
    Button(
        onClick = onGoogleSignIn,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("google_sign_in_button"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            GoogleIconG(modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Sign in with Google",
                color = Color(0xFF1F1F1F),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Fast Connect button
    OutlinedButton(
        onClick = onOneTapDemo,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .testTag("fast_connect_google_button"),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = EmeraldGreen
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.horizontalGradient(listOf(NavyLight, NavyLight))
        )
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = EmeraldGreen,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Connect as vincentmwangangi28@gmail.com",
            color = WhiteActive,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SyncItemChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = NavyDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EmeraldGreen,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = WhiteActive,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun GoogleIconG(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Google",
            tint = GoogleBlue,
            modifier = Modifier.fillMaxSize()
        )
    }
}
