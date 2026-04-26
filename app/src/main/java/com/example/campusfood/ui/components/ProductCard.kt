package com.example.campusfood.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.campusfood.model.Product
import com.example.campusfood.ui.theme.GreenSuccess
import com.example.campusfood.ui.theme.OrangePrimary
import com.example.campusfood.ui.theme.OrangePrimaryDark
import com.example.campusfood.ui.theme.OrangePrimaryLight
import com.example.campusfood.ui.theme.OrangeAccentSoft
import com.example.campusfood.ui.theme.RedError

@Composable
fun ProductCard(
    product: Product,
    onAddToCart: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    var isAdded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    val scale by animateFloatAsState(
        targetValue = if (isAdded) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    val isOutOfStock = product.stock != null && product.stock <= 0
    val isLowStock = product.stock != null && product.stock in 1..10
    val priceFormatted = String.format(Locale.getDefault(), "%.0f", product.price)
    
    // Enhanced semantic description for accessibility
    val cardDescription = buildString {
        append(product.name)
        append(", ")
        append(product.category)
        append(", Price: ₹$priceFormatted")
        if (isOutOfStock) {
            append(", Out of stock")
        } else if (isLowStock) {
            append(", Only ${product.stock} left")
        }
        if (!product.description.isNullOrBlank()) {
            append(", ${product.description}")
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .scale(scale)
            .animateContentSize()
            .semantics {
                contentDescription = cardDescription
                role = Role.Button
            },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp,
            hoveredElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(OrangeAccentSoft)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl?.ifBlank { null })
                        .crossfade(400)
                        .build(),
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(OrangeAccentSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = OrangePrimary.copy(alpha = 0.4f),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        // Premium food icon fallback
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            OrangePrimary.copy(alpha = 0.06f),
                                            OrangePrimaryLight.copy(alpha = 0.12f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Fastfood,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = OrangePrimary.copy(alpha = 0.35f)
                            )
                        }
                    }
                )

                // Category badge – bottom left
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp),
                    shape = RoundedCornerShape(7.dp),
                    color = OrangePrimary,
                    shadowElevation = 3.dp
                ) {
                    Text(
                        text = product.category,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }

                // Low stock dot – top right
                if (product.stock != null && product.stock <= 10) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(8.dp)
                            .shadow(2.dp, CircleShape)
                            .background(RedError, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Product details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 2.dp)
            ) {
                // Name
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Description
                if (!product.description.isNullOrBlank()) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Price + Stock + Add button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "₹${String.format(Locale.getDefault(), "%.0f", product.price)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = OrangePrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        if (product.stock != null && product.stock <= 10) {
                            Text(
                                text = if (product.stock <= 0) "Out of stock" else "Only ${product.stock} left",
                                style = MaterialTheme.typography.labelSmall,
                                color = RedError,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Add button with haptic feedback
                    FilledIconButton(
                        onClick = {
                            if (product.stock == null || product.stock > 0) {
                                // Haptic feedback on successful add
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isAdded = true
                                onAddToCart(product)
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .semantics {
                                contentDescription = if (isAdded) "Added to cart" else "Add ${product.name} to cart"
                                role = Role.Button
                            },
                        shape = RoundedCornerShape(12.dp),
                        enabled = product.stock == null || product.stock > 0,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isAdded) GreenSuccess else OrangePrimary,
                            contentColor = Color.White,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        AnimatedContent(
                            targetState = isAdded,
                            transitionSpec = {
                                (scaleIn(animationSpec = tween(250)) + fadeIn()) togetherWith
                                        (scaleOut(animationSpec = tween(250)) + fadeOut())
                            },
                            label = "addButtonIcon"
                        ) { added ->
                            Icon(
                                imageVector = if (added) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = if (added) "Added" else "Add to cart",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Reset "Added" state after brief delay
    LaunchedEffect(isAdded) {
        if (isAdded) {
            kotlinx.coroutines.delay(2000)
            isAdded = false
        }
    }
}
