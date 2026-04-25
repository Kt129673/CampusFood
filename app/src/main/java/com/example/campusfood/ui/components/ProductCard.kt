package com.example.campusfood.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.example.campusfood.ui.theme.RedError

@Composable
fun ProductCard(
    product: Product,
    onAddToCart: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    var isAdded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isAdded) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 16.dp)
            .scale(scale)
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp,
            pressedElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image – with proper fallback
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
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
                        // Shimmer placeholder while loading
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = OrangePrimary.copy(alpha = 0.5f),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        // Premium food icon fallback instead of ugly X
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            OrangePrimary.copy(alpha = 0.08f),
                                            OrangePrimaryLight.copy(alpha = 0.15f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Fastfood,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = OrangePrimary.copy(alpha = 0.4f)
                            )
                        }
                    }
                )

                // Gradient overlay at bottom for category badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                            )
                        )
                )

                // Category badge on image – with proper readable background
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = OrangePrimary.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = product.category,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                // Low stock indicator dot
                if (product.stock != null && product.stock <= 10) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(10.dp),
                        shape = RoundedCornerShape(50),
                        color = RedError,
                        shadowElevation = 2.dp
                    ) {}
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                // Product name
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Description
                if (!product.description.isNullOrBlank()) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        maxLines = 2,
                        lineHeight = 18.sp,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Price and Add button row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Price – larger, premium
                    Column {
                        Text(
                            text = "₹${String.format(Locale.getDefault(), "%.0f", product.price)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = OrangePrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
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

                    // Premium "Add" button with animated icon
                    FilledIconButton(
                        onClick = {
                            if (product.stock == null || product.stock > 0) {
                                isAdded = true
                                onAddToCart(product)
                            }
                        },
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(14.dp),
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
                                (scaleIn(animationSpec = tween(300)) + fadeIn()) togetherWith
                                        (scaleOut(animationSpec = tween(300)) + fadeOut())
                            },
                            label = "addButtonIcon"
                        ) { added ->
                            Icon(
                                imageVector = if (added) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = if (added) "Added" else "Add to cart",
                                modifier = Modifier.size(22.dp)
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
            kotlinx.coroutines.delay(1800)
            isAdded = false
        }
    }
}
