# Fixes Applied - Image Upload & Google Sign-In

## Issues Fixed

### 1. Image Upload 500 Error ✅

**Root Cause:**
- AWS credentials were not configured (empty environment variables)
- S3Config was falling back to DefaultCredentialsProvider without proper permissions
- Generic error handling masked the actual S3 error messages

**Fixes Applied:**

1. **S3Config.java** - Added strict validation:
   - Now throws `IllegalStateException` if AWS credentials are missing
   - Clear error message: "AWS credentials are required for S3 operations"
   - Removed fallback to DefaultCredentialsProvider to fail fast

2. **GlobalExceptionHandler.java** - Added specific RuntimeException handler:
   - Detects S3/AWS-related errors
   - Returns detailed error messages for image upload failures
   - Better logging for debugging

**Setup Required:**

Set these environment variables before starting the backend:

```bash
# Windows (PowerShell)
$env:AWS_ACCESS_KEY="your-access-key-here"
$env:AWS_SECRET_KEY="your-secret-key-here"

# Windows (CMD)
set AWS_ACCESS_KEY=your-access-key-here
set AWS_SECRET_KEY=your-secret-key-here

# Linux/Mac
export AWS_ACCESS_KEY="your-access-key-here"
export AWS_SECRET_KEY="your-secret-key-here"
```

**Verify S3 Bucket Configuration:**
- Bucket name: `campusfood` (in application.properties)
- Region: `ap-south-1` (in application.properties)
- Ensure bucket exists and credentials have PutObject/DeleteObject permissions

---

### 2. Google Sign-In Not Working ✅

**Root Cause:**
- No dedicated backend endpoint for Google OAuth
- Frontend was using insecure workaround (email as password)
- Missing SHA-1 fingerprint registration in Google Cloud Console

**Fixes Applied:**

1. **Backend - AuthController.java**:
   - Added new `/api/auth/google` endpoint
   - Accepts email and name from Google account
   - Returns user session data

2. **Backend - UserService.java**:
   - Added `googleLogin()` method
   - Finds existing user by email or creates new one
   - Generates stable mobile number from email hash
   - Proper transaction handling

3. **Frontend - ApiService.kt**:
   - Added `googleLogin()` API call
   - Uses new backend endpoint

4. **Frontend - AuthModels.kt**:
   - Added `GoogleLoginRequest` data class

5. **Frontend - AuthViewModel.kt**:
   - Simplified `loginWithGoogle()` method
   - Now uses dedicated backend endpoint
   - Removed insecure email-as-password workaround

**Setup Required for Google Sign-In:**

#### Step 1: Get SHA-1 Fingerprint

```bash
# Debug keystore (for development)
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# Release keystore (for production)
keytool -list -v -keystore /path/to/your/release.keystore -alias your-alias
```

#### Step 2: Configure Google Cloud Console

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project or create a new one
3. Navigate to **APIs & Services** > **Credentials**
4. Click **Create Credentials** > **OAuth 2.0 Client ID**
5. Select **Android** as application type
6. Enter:
   - **Package name**: `com.example.campusfood`
   - **SHA-1 certificate fingerprint**: (paste from Step 1)
7. Click **Create**

#### Step 3: Verify app/build.gradle.kts

Ensure Google Sign-In dependency is present:

```kotlin
dependencies {
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    // ... other dependencies
}
```

#### Step 4: Test Google Sign-In

1. Build and install the app on a real device or emulator with Google Play Services
2. Click "Continue with Google" button
3. Select a Google account
4. Should successfully sign in and navigate to main screen

---

## Testing the Fixes

### Test Image Upload:

1. Start backend with AWS credentials configured
2. Login as admin (9999999999 / admin123)
3. Navigate to Admin Dashboard > Products
4. Click "Add Product" or edit existing product
5. Select an image file
6. Should upload successfully and show image URL

### Test Google Sign-In:

1. Ensure SHA-1 is registered in Google Cloud Console
2. Build and install app on device
3. On login screen, click "Continue with Google"
4. Select Google account
5. Should create/login user and navigate to menu screen

---

## Error Messages

### Image Upload Errors:

- **"AWS credentials are required for S3 operations"**: Set AWS_ACCESS_KEY and AWS_SECRET_KEY environment variables
- **"Failed to upload image to S3: [details]"**: Check S3 bucket permissions and configuration
- **"File size exceeds maximum allowed size of 15MB"**: Reduce image file size
- **"Invalid file type"**: Only JPEG, PNG, WebP, GIF are allowed

### Google Sign-In Errors:

- **"Google Sign-In Error (Code 10)"**: SHA-1 fingerprint not registered in Google Cloud Console
- **"Google sign-in was canceled"**: User canceled the sign-in flow
- **"Google sign-in failed"**: Check internet connection and Google Play Services

---

## Files Modified

### Backend:
- `backend/src/main/java/com/campusfood/config/S3Config.java`
- `backend/src/main/java/com/campusfood/exception/GlobalExceptionHandler.java`
- `backend/src/main/java/com/campusfood/controller/AuthController.java`
- `backend/src/main/java/com/campusfood/service/UserService.java`

### Frontend:
- `app/src/main/java/com/example/campusfood/network/ApiService.kt`
- `app/src/main/java/com/example/campusfood/model/AuthModels.kt`
- `app/src/main/java/com/example/campusfood/ui/screens/AuthViewModel.kt`

---

## Next Steps

1. **Set AWS credentials** as environment variables
2. **Register SHA-1 fingerprint** in Google Cloud Console
3. **Rebuild the app** to apply changes
4. **Test both features** thoroughly
5. Consider adding **proper OAuth token verification** for production (currently using email-based authentication)

---

## Security Notes

⚠️ **For Production:**
- Implement proper Google ID token verification on backend
- Use BCrypt for password hashing (currently using simple hash)
- Add rate limiting for authentication endpoints
- Enable HTTPS for all API calls
- Store AWS credentials in secure secret management service (not environment variables)
