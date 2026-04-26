package com.example.campusfood.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusfood.ui.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileNumberDialog(
    userName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var mobileNumber by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val isValid = mobileNumber.length == 10 && mobileNumber.all { it.isDigit() }

    AlertDialog(
        onDismissRequest = { /* Cannot dismiss without providing number */ },
        icon = {
            Icon(
                Icons.Default.Phone,
                contentDescription = null,
                tint = OrangePrimary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "Welcome, $userName!",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Please provide your mobile number to complete your profile and receive order updates.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) mobileNumber = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Mobile Number") },
                    placeholder = { Text("Enter 10-digit number") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = "Phone",
                            tint = OrangePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    prefix = { Text("+91 ", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (isValid) {
                                onConfirm(mobileNumber)
                            }
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        focusedLabelColor = OrangePrimary
                    ),
                    supportingText = {
                        Text(
                            "${mobileNumber.length}/10",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(mobileNumber) },
                enabled = isValid,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary
                )
            ) {
                Text("Continue", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Skip for now")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
