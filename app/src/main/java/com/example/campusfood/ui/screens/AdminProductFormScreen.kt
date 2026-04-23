package com.example.campusfood.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusfood.model.Product
import com.example.campusfood.model.ProductRequest
import com.example.campusfood.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductFormScreen(
    adminViewModel: AdminViewModel,
    productId: Long? = null,
    onBack: () -> Unit
) {
    val isEditing = productId != null && productId > 0
    val selectedProduct by adminViewModel.selectedProduct.collectAsState()
    val actionState by adminViewModel.productActionState.collectAsState()

    // Load product for editing
    LaunchedEffect(productId) {
        if (isEditing && productId != null) {
            adminViewModel.loadProductById(productId)
        } else {
            adminViewModel.clearSelectedProduct()
        }
    }

    // Form state
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("100") }
    var available by remember { mutableStateOf(true) }
    var formInitialized by remember { mutableStateOf(false) }

    // Populate form when product loads (edit mode)
    LaunchedEffect(selectedProduct) {
        if (isEditing && selectedProduct != null && !formInitialized) {
            val p = selectedProduct!!
            name = p.name
            description = p.description ?: ""
            price = String.format("%.0f", p.price)
            category = p.category
            imageUrl = p.imageUrl ?: ""
            stock = "${p.stock ?: 0}"
            available = p.available
            formInitialized = true
        }
    }

    val categories = listOf("Snacks", "Beverages", "Hot Beverages", "Quick Bites", "Essentials", "Chocolates")
    var expandedCategory by remember { mutableStateOf(false) }

    val isFormValid = name.isNotBlank() && price.isNotBlank() && category.isNotBlank()
            && (price.toDoubleOrNull() ?: 0.0) > 0

    Scaffold(
        topBar = {
            Surface(
                color = Color.Transparent,
                shadowElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(Color(0xFF7B1FA2), Color(0xFF4A148C))))
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (isEditing) "Edit Product" else "Add Product",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Product Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Product Name *") },
                leadingIcon = { Icon(Icons.Default.Fastfood, null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(20.dp)) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = adminFieldColors(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(Modifier.height(12.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Description") },
                leadingIcon = { Icon(Icons.Default.Description, null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(20.dp)) },
                shape = RoundedCornerShape(12.dp),
                maxLines = 3,
                colors = adminFieldColors(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(Modifier.height(12.dp))

            // Price and Stock row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) price = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Price ₹ *") },
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(20.dp)) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = adminFieldColors(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    )
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = { if (it.all { c -> c.isDigit() }) stock = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Initial Stock") },
                    leadingIcon = { Icon(Icons.Default.Inventory, null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(20.dp)) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = adminFieldColors(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = it }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    label = { Text("Category *") },
                    leadingIcon = { Icon(Icons.Default.Category, null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(20.dp)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = adminFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Image URL
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Image URL") },
                leadingIcon = { Icon(Icons.Default.Image, null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(20.dp)) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = adminFieldColors(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Spacer(Modifier.height(12.dp))

            // Available toggle
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Available for sale",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Visible to customers",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = available,
                        onCheckedChange = { available = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Color(0xFF7B1FA2),
                            checkedThumbColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Submit button
            Button(
                onClick = {
                    val request = ProductRequest(
                        name = name.trim(),
                        description = description.trim().ifBlank { null },
                        price = price.toDoubleOrNull() ?: 0.0,
                        category = category.trim(),
                        imageUrl = imageUrl.trim().ifBlank { null },
                        available = available,
                        initialStock = stock.toIntOrNull() ?: 0
                    )
                    if (isEditing && productId != null) {
                        adminViewModel.updateProduct(productId, request, onBack)
                    } else {
                        adminViewModel.createProduct(request, onBack)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = isFormValid && actionState !is AdminProductActionState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
            ) {
                if (actionState is AdminProductActionState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Saving...", fontWeight = FontWeight.Bold, color = Color.White)
                } else {
                    Icon(
                        if (isEditing) Icons.Default.Save else Icons.Default.Add,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isEditing) "Update Product" else "Create Product",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Error message
            if (actionState is AdminProductActionState.Error) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = RedError.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = RedError, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            (actionState as AdminProductActionState.Error).message,
                            style = MaterialTheme.typography.bodySmall,
                            color = RedError,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun adminFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF7B1FA2),
    focusedLabelColor = Color(0xFF7B1FA2),
    cursorColor = Color(0xFF7B1FA2)
)
