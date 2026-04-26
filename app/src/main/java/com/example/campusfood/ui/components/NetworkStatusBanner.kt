package com.example.campusfood.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusfood.ui.theme.GreenSuccess
import com.example.campusfood.ui.theme.RedError

/**
 * Banner that shows network connectivity status.
 * Appears when offline, disappears when back online.
 */
@Composable
fun NetworkStatusBanner(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    var wasOffline by remember { mutableStateOf(false) }
    
    LaunchedEffect(isConnected) {
        if (!isConnected) {
            wasOffline = true
        }
    }

    AnimatedVisibility(
        visible = !isConnected || (wasOffline && isConnected),
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = if (isConnected) GreenSuccess else RedError,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (isConnected) Icons.Default.Wifi else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isConnected) "Back online!" else "No internet connection",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }

    // Auto-hide "Back online" message after 3 seconds
    LaunchedEffect(isConnected) {
        if (isConnected && wasOffline) {
            kotlinx.coroutines.delay(3000)
            wasOffline = false
        }
    }
}
