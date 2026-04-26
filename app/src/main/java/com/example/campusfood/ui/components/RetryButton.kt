package com.example.campusfood.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusfood.ui.theme.OrangePrimary

/**
 * Retry button with loading animation.
 */
@Composable
fun RetryButton(
    onRetry: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    text: String = "Retry"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "retry_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Button(
        onClick = onRetry,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = OrangePrimary
        ),
        enabled = !isLoading
    ) {
        if (isLoading) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(rotation)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retrying...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        } else {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

/**
 * Enhanced error state with retry functionality.
 */
@Composable
fun EnhancedErrorState(
    message: String,
    onRetry: () -> Unit,
    isRetrying: Boolean = false,
    emoji: String = "⚠️",
    title: String = "Something went wrong",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(88.dp),
            shape = RoundedCornerShape(44.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 40.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(18.dp))
        
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        RetryButton(
            onRetry = onRetry,
            isLoading = isRetrying,
            modifier = Modifier.fillMaxWidth(0.7f)
        )
    }
}
