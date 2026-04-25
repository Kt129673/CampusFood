package com.example.campusfood.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.campusfood.ui.theme.OrangePrimaryLight
import com.example.campusfood.ui.theme.RedError
import com.example.campusfood.ui.theme.AdminPurple
import com.example.campusfood.ui.theme.BlueInfo

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
                    colors = ButtonDefaults.buttonColors(containerColor = RedError),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Logout", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    val isAdmin = user?.role == "ADMIN"
    val headerGradient = if (isAdmin) {
        Brush.horizontalGradient(listOf(AdminPurple, Color(0xFF4A148C), Color(0xFF311B92)))
    } else {
        Brush.horizontalGradient(listOf(OrangePrimary, OrangePrimaryDark, Color(0xFFBF360C)))
    }
    val accentColor = if (isAdmin) AdminPurple else OrangePrimary

    Scaffold(
        topBar = {
            // Premium gradient header
            Surface(
                color = Color.Transparent,
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerGradient)
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        "My Profile",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp
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
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Premium avatar with gradient ring
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                // Gradient border ring
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .border(
                            width = 3.dp,
                            brush = Brush.linearGradient(
                                if (isAdmin)
                                    listOf(AdminPurple, Color(0xFFCE93D8), Color(0xFF4A148C))
                                else
                                    listOf(OrangePrimary, OrangePrimaryLight, OrangePrimaryDark)
                            ),
                            shape = CircleShape
                        )
                )
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val initial = user?.name?.firstOrNull()?.uppercase() ?: "U"
                        Text(
                            initial,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            fontSize = 44.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            Text(
                user?.name ?: "Guest User",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Role badge – premium pill with dynamic color
            val roleLabel = when (user?.role) {
                "ADMIN" -> "Administrator"
                "DELIVERY" -> "Delivery Partner"
                else -> "Student"
            }
            val roleColor = when (user?.role) {
                "ADMIN" -> AdminPurple
                "DELIVERY" -> BlueInfo
                else -> OrangePrimary
            }
            val roleIcon = when (user?.role) {
                "ADMIN" -> Icons.Default.AdminPanelSettings
                "DELIVERY" -> Icons.Default.DeliveryDining
                else -> Icons.Default.School
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = roleColor.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        roleIcon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = roleColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        roleLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = roleColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Info cards – bigger, more spacious
            if (!user?.mobile.isNullOrBlank()) {
                ProfileInfoRow(
                    icon = Icons.Default.Phone,
                    label = "Mobile",
                    value = user?.mobile ?: "",
                    accentColor = accentColor
                )
            }
            if (!user?.email.isNullOrBlank()) {
                ProfileInfoRow(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = user?.email ?: "",
                    accentColor = accentColor
                )
            }
            ProfileInfoRow(
                icon = Icons.Default.LocationOn,
                label = "Campus",
                value = "Main Campus",
                accentColor = accentColor
            )
            ProfileInfoRow(
                icon = Icons.Default.Badge,
                label = "User ID",
                value = "#${user?.id ?: "—"}",
                accentColor = accentColor
            )
            if (!user?.createdAt.isNullOrBlank()) {
                ProfileInfoRow(
                    icon = Icons.Default.CalendarMonth,
                    label = "Member Since",
                    value = formatProfileDate(user?.createdAt ?: ""),
                    accentColor = accentColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Settings section – more premium card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    ProfileMenuItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = "Order updates & offers"
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.LocationOn,
                        title = "Delivery Addresses",
                        subtitle = "Manage your addresses"
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Filled.Help,
                        title = "Help & Support",
                        subtitle = "Contact us, FAQ"
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.Info,
                        title = "About CampusFood",
                        subtitle = "Version 1.0.0"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Premium logout button
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = RedError
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = Brush.linearGradient(listOf(RedError.copy(alpha = 0.5f), RedError.copy(alpha = 0.3f)))
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Logout",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color = OrangePrimary
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.08f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    value,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
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
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
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
