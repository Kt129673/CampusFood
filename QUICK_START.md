# Quick Start Guide - After Fixes

## 🚀 Start Backend Server

### Option 1: Using Setup Script (Recommended)

**Windows PowerShell:**
```powershell
cd backend
.\setup-aws-credentials.ps1
.\mvnw.cmd spring-boot:run
```

**Linux/Mac:**
```bash
cd backend
source ./setup-aws-credentials.sh
./mvnw spring-boot:run
```

### Option 2: Manual Setup

**Windows PowerShell:**
```powershell
$env:AWS_ACCESS_KEY="your-access-key"
$env:AWS_SECRET_KEY="your-secret-key"
cd backend
.\mvnw.cmd spring-boot:run
```

**Linux/Mac:**
```bash
export AWS_ACCESS_KEY="your-access-key"
export AWS_SECRET_KEY="your-secret-key"
cd backend
./mvnw spring-boot:run
```

Backend will start on: http://localhost:5000

---

## 📱 Setup Android App for Google Sign-In

### 1. Get SHA-1 Fingerprint

```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Look for the SHA-1 line in the output:
```
SHA1: AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12
```

### 2. Register in Google Cloud Console

1. Go to https://console.cloud.google.com/
2. Select/Create project
3. Navigate to **APIs & Services** > **Credentials**
4. Click **Create Credentials** > **OAuth 2.0 Client ID**
5. Select **Android**
6. Enter:
   - Package name: `com.example.campusfood`
   - SHA-1: (paste from step 1)
7. Click **Create**

### 3. Build and Run App

```bash
cd app
# Open in Android Studio and run
# OR use Gradle
./gradlew installDebug
```

---

## ✅ Test the Fixes

### Test 1: Image Upload

1. Login as admin: `9999999999` / `admin123`
2. Go to Admin Dashboard → Products
3. Click "Add Product"
4. Fill in product details
5. Click "Select Image" and choose an image
6. Should upload successfully ✅

### Test 2: Google Sign-In

1. On login screen, ensure "Student" tab is selected
2. Click "Continue with Google"
3. Select your Google account
4. Should login successfully and show menu screen ✅

---

## 🔧 Troubleshooting

### Image Upload Issues

**Error: "AWS credentials are required"**
- Solution: Set AWS_ACCESS_KEY and AWS_SECRET_KEY environment variables

**Error: "Failed to upload image to S3"**
- Check S3 bucket exists: `campusfood`
- Check region matches: `ap-south-1`
- Verify IAM permissions: `s3:PutObject`, `s3:DeleteObject`

### Google Sign-In Issues

**Error: "Code 10: SHA-1 not registered"**
- Solution: Register SHA-1 fingerprint in Google Cloud Console (see above)

**Error: "Google sign-in was canceled"**
- User canceled the flow - try again

**Error: "Google sign-in failed"**
- Check internet connection
- Ensure Google Play Services is installed
- Verify package name matches: `com.example.campusfood`

---

## 📝 Demo Accounts

### Admin Account
- Mobile: `9999999999`
- Password: `admin123`

### Customer Account
- Mobile: `9876543210`
- Password: `pass123`

### Google Sign-In
- Use any Google account
- Will auto-create customer account

---

## 🔐 AWS S3 Configuration

Current settings in `application.properties`:
- Bucket: `campusfood`
- Region: `ap-south-1`
- Max file size: 15MB
- Allowed types: JPEG, PNG, WebP, GIF

To change bucket/region, edit:
```properties
aws.bucketName=your-bucket-name
aws.region=your-region
```

---

## 📚 Additional Resources

- Full fix details: See `FIXES_APPLIED.md`
- AWS IAM Setup: https://docs.aws.amazon.com/IAM/latest/UserGuide/
- Google Sign-In: https://developers.google.com/identity/sign-in/android
- Spring Boot: https://spring.io/projects/spring-boot

---

## 🎯 What Was Fixed

✅ Image upload 500 error - Now shows clear error messages and validates AWS credentials
✅ Google sign-in - New dedicated backend endpoint with proper user management
✅ Better error handling - Specific error messages for debugging
✅ Security improvements - Fail-fast on missing credentials

---

Need help? Check the error messages - they now provide clear guidance on what's wrong!
