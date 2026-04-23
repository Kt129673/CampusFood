# Google Sign-In Setup Guide

## 📋 Prerequisites

- Android Studio installed
- Google Cloud Console account
- CampusFood app source code

---

## 🔑 Step 1: Get SHA-1 Fingerprint

### For Debug Build (Development)

Open terminal and run:

```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Windows users:** Replace `~/.android/` with `%USERPROFILE%\.android\`

```cmd
keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android
```

### Expected Output:

```
Alias name: androiddebugkey
Creation date: ...
Entry type: PrivateKeyEntry
Certificate chain length: 1
Certificate[1]:
Owner: CN=Android Debug, O=Android, C=US
Issuer: CN=Android Debug, O=Android, C=US
Serial number: ...
Valid from: ... until: ...
Certificate fingerprints:
         SHA1: AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12  ← COPY THIS
         SHA256: ...
```

**📝 Copy the SHA1 value** (the line with colons)

### For Release Build (Production)

```bash
keytool -list -v -keystore /path/to/your/release.keystore -alias your-alias-name
```

You'll be prompted for the keystore password.

---

## ☁️ Step 2: Configure Google Cloud Console

### 2.1 Access Google Cloud Console

1. Go to https://console.cloud.google.com/
2. Sign in with your Google account

### 2.2 Create or Select Project

**Option A: Create New Project**
1. Click the project dropdown at the top
2. Click "New Project"
3. Enter project name: `CampusFood` (or your preferred name)
4. Click "Create"

**Option B: Use Existing Project**
1. Click the project dropdown
2. Select your existing project

### 2.3 Enable Google Sign-In API

1. In the left sidebar, go to **APIs & Services** → **Library**
2. Search for "Google Sign-In API" or "Google+ API"
3. Click on it and click **Enable** (if not already enabled)

### 2.4 Create OAuth 2.0 Client ID

1. Go to **APIs & Services** → **Credentials**
2. Click **+ CREATE CREDENTIALS** at the top
3. Select **OAuth client ID**

4. If prompted to configure consent screen:
   - Click **CONFIGURE CONSENT SCREEN**
   - Select **External** (for testing with any Google account)
   - Click **CREATE**
   - Fill in:
     - App name: `CampusFood`
     - User support email: Your email
     - Developer contact: Your email
   - Click **SAVE AND CONTINUE**
   - Skip "Scopes" (click **SAVE AND CONTINUE**)
   - Add test users if needed (click **SAVE AND CONTINUE**)
   - Click **BACK TO DASHBOARD**

5. Back to **Credentials** → **+ CREATE CREDENTIALS** → **OAuth client ID**

6. Select **Android** as Application type

7. Fill in the form:
   - **Name**: `CampusFood Android Client` (or any name)
   - **Package name**: `com.example.campusfood` ⚠️ **MUST MATCH EXACTLY**
   - **SHA-1 certificate fingerprint**: Paste the SHA-1 from Step 1

8. Click **CREATE**

### 2.5 Confirmation

You should see a dialog saying "OAuth client created"
- You don't need to download anything
- The client ID is automatically used by Google Sign-In SDK

---

## 📱 Step 3: Verify App Configuration

### 3.1 Check build.gradle.kts

Open `app/build.gradle.kts` and verify:

```kotlin
android {
    namespace = "com.example.campusfood"  // ← Must match package name
    // ...
}

dependencies {
    implementation("com.google.android.gms:play-services-auth:20.7.0")  // ← Must be present
    // ...
}
```

### 3.2 Check AndroidManifest.xml

Open `app/src/main/AndroidManifest.xml` and verify package:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.campusfood">  <!-- ← Must match -->
```

---

## 🔨 Step 4: Build and Test

### 4.1 Clean and Rebuild

In Android Studio:
1. **Build** → **Clean Project**
2. **Build** → **Rebuild Project**

Or via command line:
```bash
cd app
./gradlew clean build
```

### 4.2 Install on Device

**Requirements:**
- Real Android device OR emulator with Google Play Services
- Device must be connected via USB or emulator running

In Android Studio:
1. Click **Run** (green play button)
2. Select your device
3. Wait for installation

Or via command line:
```bash
./gradlew installDebug
```

### 4.3 Test Google Sign-In

1. Open the CampusFood app
2. On login screen, ensure **"Student"** tab is selected
3. Click **"Continue with Google"** button
4. Select a Google account from the picker
5. ✅ Should successfully sign in and navigate to menu screen

---

## 🐛 Troubleshooting

### Error: "Code 10: SHA-1 not registered"

**Cause:** SHA-1 fingerprint not registered or doesn't match

**Solution:**
1. Verify you copied the correct SHA-1 (check for typos)
2. Make sure you used the debug keystore SHA-1 for debug builds
3. Wait 5-10 minutes after registering (Google needs time to propagate)
4. Try uninstalling and reinstalling the app

### Error: "Developer Error"

**Cause:** Package name mismatch

**Solution:**
1. Verify package name in Google Cloud Console is exactly: `com.example.campusfood`
2. Check `build.gradle.kts` namespace matches
3. Check `AndroidManifest.xml` package matches

### Error: "Sign-in was canceled"

**Cause:** User canceled or Google Play Services issue

**Solution:**
1. Try again
2. Ensure Google Play Services is up to date
3. Check internet connection

### Error: "Google Play Services not available"

**Cause:** Emulator doesn't have Google Play Services

**Solution:**
1. Use an emulator with Google Play (not Google APIs)
2. Or use a real Android device

### Sign-in works but backend returns error

**Cause:** Backend not running or network issue

**Solution:**
1. Ensure backend is running on `http://localhost:5000`
2. Check `RetrofitInstance.kt` has correct base URL
3. If testing on real device, use your computer's IP instead of localhost

---

## 📊 Verification Checklist

- [ ] SHA-1 fingerprint obtained
- [ ] Google Cloud project created/selected
- [ ] OAuth 2.0 Client ID created for Android
- [ ] Package name is `com.example.campusfood`
- [ ] SHA-1 registered in Google Cloud Console
- [ ] App rebuilt after configuration
- [ ] Google Play Services available on test device
- [ ] Backend server running
- [ ] Google sign-in button appears on login screen
- [ ] Account picker shows when clicking button
- [ ] Successfully signs in and navigates to menu

---

## 🔐 Security Notes

### For Development:
- Debug keystore SHA-1 is fine
- Any Google account can sign in

### For Production:
1. Generate release keystore:
   ```bash
   keytool -genkey -v -keystore release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Get release SHA-1:
   ```bash
   keytool -list -v -keystore release.keystore -alias release
   ```

3. Add release SHA-1 to Google Cloud Console (same steps as debug)

4. Configure OAuth consent screen for production:
   - Add privacy policy URL
   - Add terms of service URL
   - Submit for verification if needed

---

## 📚 Additional Resources

- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android/start-integrating)
- [OAuth 2.0 Client IDs](https://developers.google.com/identity/protocols/oauth2)
- [Google Cloud Console](https://console.cloud.google.com/)
- [Android Keystore System](https://developer.android.com/training/articles/keystore)

---

## ✅ Success!

Once you see the menu screen after signing in with Google, you're all set! 🎉

The backend will automatically:
- Create a new user account for first-time Google users
- Login existing users who previously signed in with Google
- Generate a stable mobile number from email hash
- Assign CUSTOMER role to Google users

---

**Need help?** Check the error messages in Logcat (Android Studio → Logcat) for detailed debugging information.
