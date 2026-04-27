package com.example.campusfood.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.campusfood.ui.theme.OrangeAccentSoft

/**
 * Shimmer placeholder for product cards while loading.
 * Provides a premium skeleton animation instead of a simple spinner.
 */
@Composable
fun ShimmerProductCard(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1100,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerModifier = Modifier.drawBehind {
        val x = translateAnim
        val brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(x - 400f, 0f),
            end = Offset(x, 0f)
        )
        drawRect(brush = brush)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image placeholder
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(14.dp))
                .then(shimmerModifier)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Title
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .then(shimmerModifier)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Description
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .then(shimmerModifier)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price
                Box(
                    modifier = Modifier
                        .width(55.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .then(shimmerModifier)
                )
                // Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .then(shimmerModifier)
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
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    )

    val transition = rememberInfiniteTransition(label = "shimmerOrder")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1100,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOrderTranslate"
    )

    val shimmerModifier = Modifier.drawBehind {
        val x = translateAnim
        val brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(x - 400f, 0f),
            end = Offset(x, 0f)
        )
        drawRect(brush = brush)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .then(shimmerModifier)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(65.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .then(shimmerModifier)
                )
            }
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .then(shimmerModifier)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Items
        repeat(2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .then(shimmerModifier)
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .then(shimmerModifier)
            )
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .then(shimmerModifier)
            )
        }
    }
}
