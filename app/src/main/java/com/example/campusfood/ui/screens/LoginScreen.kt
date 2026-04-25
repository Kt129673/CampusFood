package com.example.campusfood.ui.screens

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusfood.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    // Watch for successful auth
    LaunchedEffect(authState) {
        if (authState is AuthUiState.Success) {
            onLoginSuccess()
        }
    }

    // Google Sign-In launcher
    @Suppress("DEPRECATION")
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                val name = account?.displayName ?: "Campus User"
                val email = account?.email ?: ""
                if (email.isNotBlank()) {
                    authViewModel.loginWithGoogle(name, email)
                } else {
                    authViewModel.setError("Google account does not have an email address.")
                }
            } catch (e: ApiException) {
                Log.e("LoginScreen", "Google sign-in failed: ${e.statusCode}", e)
                if (e.statusCode == 10) {
                    authViewModel.setError("Google Sign-In Error (Code 10): App SHA-1 is not registered in Google Cloud Console.")
                } else {
                    authViewModel.setError("Google sign-in failed (Code: ${e.statusCode}).")
                }
            }
        } else {
            // User canceled or Google Play Services had an issue
            authViewModel.setError("Google sign-in was canceled or failed to start.")
        }
    }

    // State
    var isAdminMode by remember { mutableStateOf(false) }
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        OrangePrimary,
                        OrangePrimaryDark,
                        Color(0xFF1A0800)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Floating logo animation
            val infiniteTransition = rememberInfiniteTransition(label = "logoFloat")
            val logoOffset by infiniteTransition.animateFloat(
                initialValue = -4f,
                targetValue = 4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "logoOffset"
            )

            // Premium Logo – with floating animation and glow ring
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow ring
                Surface(
                    modifier = Modifier.size(110.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.06f)
                ) {}
                Surface(
                    modifier = Modifier
                        .size(100.dp)
                        .offset(y = logoOffset.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.14f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🍔", fontSize = 50.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Brand name – premium typography
            Text(
                "ANISHA",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
            Text(
                "CAMPUS FOOD",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Light,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Fresh food, delivered fast on campus",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.65f),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Login Card – premium with larger radius
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Toggle: Student / Admin – taller
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ToggleButton(
                            text = "Student",
                            icon = Icons.Default.School,
                            selected = !isAdminMode,
                            onClick = {
                                isAdminMode = false
                                authViewModel.resetError()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ToggleButton(
                            text = "Admin",
                            icon = Icons.Default.AdminPanelSettings,
                            selected = isAdminMode,
                            onClick = {
                                isAdminMode = true
                                authViewModel.resetError()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    AnimatedContent(
                        targetState = isAdminMode,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(300)) +
                                slideInHorizontally(
                                    initialOffsetX = { if (targetState) it / 3 else -it / 3 },
                                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                                )) togetherWith
                            (fadeOut(animationSpec = tween(200)) +
                                slideOutHorizontally(
                                    targetOffsetX = { if (targetState) -it / 3 else it / 3 },
                                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                                ))
                        },
                        label = "auth_content"
                    ) { isAdmin ->
                        if (isAdmin) {
                            // Admin Login Form
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Admin Login",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Enter your admin credentials",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Mobile field
                                OutlinedTextField(
                                    value = mobile,
                                    onValueChange = { if (it.length <= 10) mobile = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Mobile Number") },
                                    placeholder = { Text("Enter 10-digit mobile") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Phone, null, tint = OrangePrimary, modifier = Modifier.size(22.dp))
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Phone,
                                        imeAction = ImeAction.Next
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = OrangePrimary,
                                        focusedLabelColor = OrangePrimary
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Password field
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Password") },
                                    placeholder = { Text("Enter your password") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lock, null, tint = OrangePrimary, modifier = Modifier.size(22.dp))
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                if (passwordVisible) Icons.Default.VisibilityOff
                                                else Icons.Default.Visibility,
                                                contentDescription = "Toggle password"
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None
                                    else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = OrangePrimary,
                                        focusedLabelColor = OrangePrimary
                                    )
                                )

                                Spacer(modifier = Modifier.height(28.dp))

                                // Login Button – taller, premium
                                Button(
                                    onClick = {
                                        authViewModel.loginAdmin(mobile, password)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = OrangePrimary
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                    enabled = mobile.length == 10 && password.isNotBlank()
                                            && authState !is AuthUiState.Loading
                                ) {
                                    if (authState is AuthUiState.Loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color.White,
                                            strokeWidth = 2.5.dp
                                        )
                                    } else {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Login,
                                            null,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            "Login as Admin",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }

                                // Seed data hint
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = BlueInfo.copy(alpha = 0.08f)
                                ) {
                                    Text(
                                        "Demo: 9999999999 / admin123",
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = BlueInfo,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            // Student / Customer Google Sign-In
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Welcome, Student!",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Sign in with your Google account\nto start ordering campus food",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(36.dp))

                                // Google Sign-In Button – premium
                                @Suppress("DEPRECATION")
                                OutlinedButton(
                                    onClick = {
                                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                            .requestEmail()
                                            .requestProfile()
                                            .build()
                                        val client = GoogleSignIn.getClient(context, gso)
                                        // Sign out first to always show account picker
                                        client.signOut().addOnCompleteListener {
                                            googleSignInLauncher.launch(client.signInIntent)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    enabled = authState !is AuthUiState.Loading
                                ) {
                                    if (authState is AuthUiState.Loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.5.dp
                                        )
                                    } else {
                                        Text("G", fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp,
                                            color = Color(0xFF4285F4))
                                        Spacer(Modifier.width(14.dp))
                                        Text(
                                            "Continue with Google",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Divider
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HorizontalDivider(
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                    )
                                    Text(
                                        "  or use demo account  ",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Quick demo login
                                TextButton(
                                    onClick = {
                                        authViewModel.loginUser("9876543210", "pass123")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(
                                        "Login as Rahul (Demo Student)",
                                        color = OrangePrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    // Error message
                    AnimatedVisibility(visible = authState is AuthUiState.Error) {
                        if (authState is AuthUiState.Error) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 18.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = RedError.copy(alpha = 0.08f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        null,
                                        tint = RedError,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        (authState as AuthUiState.Error).message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = RedError,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "By continuing, you agree to our Terms of Service",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.45f),
                textAlign = TextAlign.Center,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ToggleButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(13.dp),
        color = if (selected) OrangePrimary else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp
            )
        }
    }
}
