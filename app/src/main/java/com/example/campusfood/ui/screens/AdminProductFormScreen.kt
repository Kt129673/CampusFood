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
import java.util.Locale
import java.io.File
import java.io.FileOutputStream
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
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
        if (isEditing) {
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
    var submitted by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    var isUploading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            try {
                // Copy to temp file
                val extension = context.contentResolver.getType(it)?.split("/")?.lastOrNull() ?: "jpg"
                val tempFile = File.createTempFile("upload_", ".$extension", context.cacheDir)
                val inputStream = context.contentResolver.openInputStream(it)
                val outputStream = FileOutputStream(tempFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                val requestFile = tempFile.asRequestBody(context.contentResolver.getType(it)?.toMediaTypeOrNull() ?: "image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

                adminViewModel.uploadProductImage(body) { uploadedUrl ->
                    imageUrl = uploadedUrl
                    isUploading = false
                }
            } catch (e: Exception) {
                isUploading = false
            }
        }
    }

    // Populate form when product loads (edit mode)
    LaunchedEffect(selectedProduct) {
        if (isEditing && selectedProduct != null && !formInitialized) {
            val p = selectedProduct!!
            name = p.name
            description = p.description ?: ""
            price = String.format(Locale.getDefault(), "%.0f", p.price)
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
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(AdminPurple, AdminPurpleDark, Color(0xFF311B92))
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Product Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Product Name *") },
                leadingIcon = { Icon(Icons.Default.Fastfood, null, tint = AdminPurple, modifier = Modifier.size(22.dp)) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = adminFieldColors(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                isError = submitted && name.isBlank(),
                supportingText = if (submitted && name.isBlank()) {{ Text("Product name is required", color = RedError) }} else null
            )

            Spacer(Modifier.height(12.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Description") },
                leadingIcon = { Icon(Icons.Default.Description, null, tint = AdminPurple, modifier = Modifier.size(22.dp)) },
                shape = RoundedCornerShape(16.dp),
                maxLines = 3,
                colors = adminFieldColors(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(Modifier.height(16.dp))

            // Price and Stock row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) price = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Price ₹ *") },
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, null, tint = AdminPurple, modifier = Modifier.size(22.dp)) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = adminFieldColors(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    isError = submitted && (price.isBlank() || (price.toDoubleOrNull() ?: 0.0) <= 0),
                    supportingText = if (submitted && (price.isBlank() || (price.toDoubleOrNull() ?: 0.0) <= 0)) {{ Text("Required", color = RedError) }} else null
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = { if (it.all { c -> c.isDigit() }) stock = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Initial Stock") },
                    leadingIcon = { Icon(Icons.Default.Inventory, null, tint = AdminPurple, modifier = Modifier.size(22.dp)) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = adminFieldColors(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

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
                        .menuAnchor(MenuAnchorType.PrimaryEditable),
                    label = { Text("Category *") },
                    leadingIcon = { Icon(Icons.Default.Category, null, tint = AdminPurple, modifier = Modifier.size(22.dp)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = adminFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat, fontSize = 14.sp) },
                            onClick = {
                                category = cat
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Image URL
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Image URL") },
                    leadingIcon = { Icon(Icons.Default.Image, null, tint = AdminPurple, modifier = Modifier.size(22.dp)) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = adminFieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                FilledTonalButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.height(56.dp).padding(top = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = AdminPurple.copy(alpha = 0.08f))
                ) {
                    if (isUploading || actionState is AdminProductActionState.Loading && name.isEmpty()) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = AdminPurple, strokeWidth = 2.5.dp)
                    } else {
                        Icon(Icons.Default.Upload, contentDescription = "Upload", tint = AdminPurple, modifier = Modifier.size(22.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Available toggle – premium
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Available for sale",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            "Visible to customers",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = available,
                        onCheckedChange = { available = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = AdminPurple,
                            checkedThumbColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Submit button – premium
            Button(
                onClick = {
                    submitted = true
                    if (!isFormValid) return@Button
                    val request = ProductRequest(
                        name = name.trim(),
                        description = description.trim().ifBlank { null },
                        price = price.toDoubleOrNull() ?: 0.0,
                        category = category.trim(),
                        imageUrl = imageUrl.trim().ifBlank { null },
                        available = available,
                        initialStock = stock.toIntOrNull() ?: 0
                    )
                    if (isEditing) {
                        adminViewModel.updateProduct(productId, request, onBack)
                    } else {
                        adminViewModel.createProduct(request, onBack)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(18.dp),
                enabled = actionState !is AdminProductActionState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = AdminPurple),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (actionState is AdminProductActionState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Saving...", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                } else {
                    Icon(
                        if (isEditing) Icons.Default.Save else Icons.Default.Add,
                        null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (isEditing) "Update Product" else "Create Product",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }

            // Error message
            if (actionState is AdminProductActionState.Error) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = RedError.copy(alpha = 0.06f)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = RedError, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            (actionState as AdminProductActionState.Error).message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = RedError,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun adminFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AdminPurple,
    focusedLabelColor = AdminPurple,
    cursorColor = AdminPurple
)
