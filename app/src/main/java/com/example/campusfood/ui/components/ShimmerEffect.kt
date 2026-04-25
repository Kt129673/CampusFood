package com.example.campusfood.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Shimmer placeholder for product cards while loading.
 * Provides a premium skeleton animation instead of a simple spinner.
 */
@Composable
fun ShimmerProductCard(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim.value - 400f, 0f),
        end = Offset(translateAnim.value, 0f)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image placeholder
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(brush)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Title
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(10.dp))
            // Description line 1
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(6.dp))
            // Description line 2
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(brush)
                )
                // Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(brush)
                )
            }
        }
    }
}

/**
 * Shimmer placeholder for order cards in admin and user order lists.
 */
@Composable
fun ShimmerOrderCard(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    )

    val transition = rememberInfiniteTransition(label = "shimmerOrder")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOrderTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim.value - 400f, 0f),
        end = Offset(translateAnim.value, 0f)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(brush)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(brush)
                )
            }
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(brush)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        // Items
        repeat(2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(brush)
            )
        }
    }
}
