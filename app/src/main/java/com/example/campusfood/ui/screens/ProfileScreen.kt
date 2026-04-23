package com.example.campusfood.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusfood.model.User
import com.example.campusfood.ui.theme.OrangePrimary
import com.example.campusfood.ui.theme.OrangePrimaryDark
import com.example.campusfood.ui.theme.RedError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User?,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    null,
                    tint = RedError
                )
            },
            title = {
                Text("Logout", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to logout?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedError)
                ) {
                    Text("Logout", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            // Compact header
            Surface(
                color = Color.Transparent,
                shadowElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(OrangePrimary, OrangePrimaryDark)
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        "My Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Compact avatar
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = OrangePrimary.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val initial = user?.name?.firstOrNull()?.uppercase() ?: "U"
                    Text(
                        initial,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Name
            Text(
                user?.name ?: "Guest User",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Role badge
            val roleLabel = when (user?.role) {
                "ADMIN" -> "Administrator"
                "DELIVERY" -> "Delivery Partner"
                else -> "Student"
            }
            val roleColor = when (user?.role) {
                "ADMIN" -> Color(0xFF9C27B0)
                "DELIVERY" -> Color(0xFF2196F3)
                else -> OrangePrimary
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = roleColor.copy(alpha = 0.12f)
            ) {
                Text(
                    roleLabel,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = roleColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Compact info cards
            if (!user?.mobile.isNullOrBlank()) {
                ProfileInfoRow(
                    icon = Icons.Default.Phone,
                    label = "Mobile",
                    value = user?.mobile ?: ""
                )
            }
            if (!user?.email.isNullOrBlank()) {
                ProfileInfoRow(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = user?.email ?: ""
                )
            }
            ProfileInfoRow(
                icon = Icons.Default.LocationOn,
                label = "Campus",
                value = "Main Campus"
            )
            ProfileInfoRow(
                icon = Icons.Default.Badge,
                label = "User ID",
                value = "#${user?.id ?: "—"}"
            )
            if (!user?.createdAt.isNullOrBlank()) {
                ProfileInfoRow(
                    icon = Icons.Default.CalendarMonth,
                    label = "Member Since",
                    value = formatProfileDate(user?.createdAt ?: "")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(2.dp)) {
                    ProfileMenuItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = "Order updates & offers"
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.LocationOn,
                        title = "Delivery Addresses",
                        subtitle = "Manage your addresses"
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                    )
                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Filled.Help,
                        title = "Help & Support",
                        subtitle = "Contact us, FAQ"
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.Info,
                        title = "About CampusFood",
                        subtitle = "Version 1.0.0"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Compact logout button
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = RedError
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Logout",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = RoundedCornerShape(8.dp),
                color = OrangePrimary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
                Text(
                    value,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * Formats an ISO date string to a display-friendly format for the profile.
 */
private fun formatProfileDate(isoString: String): String {
    return try {
        val parts = isoString.split("T")[0].split("-")
        if (parts.size == 3) {
            val months = listOf(
                "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )
            val monthIndex = parts[1].toIntOrNull() ?: 0
            val monthName = months.getOrElse(monthIndex) { parts[1] }
            "${parts[2]} $monthName ${parts[0]}"
        } else {
            isoString.split("T")[0]
        }
    } catch (_: Exception) {
        isoString
    }
}
