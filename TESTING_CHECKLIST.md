# Testing Checklist - After Fixes

Use this checklist to verify both fixes are working correctly.

---

## 🔧 Pre-Testing Setup

### Backend Setup
- [ ] AWS credentials set as environment variables
  - [ ] `AWS_ACCESS_KEY` is set
  - [ ] `AWS_SECRET_KEY` is set
- [ ] Backend server starts without errors
- [ ] Backend accessible at http://localhost:5000
- [ ] Database connection successful
- [ ] S3 bucket `campusfood` exists in region `ap-south-1`

### Frontend Setup
- [ ] SHA-1 fingerprint registered in Google Cloud Console
- [ ] Package name is `com.example.campusfood`
- [ ] App built and installed on device/emulator
- [ ] Device has Google Play Services
- [ ] Device has internet connection

---

## 🖼️ Test 1: Image Upload (Admin)

### Test Case 1.1: Successful Image Upload

**Steps:**
1. [ ] Open CampusFood app
2. [ ] Switch to "Admin" tab on login screen
3. [ ] Login with credentials:
   - Mobile: `9999999999`
   - Password: `admin123`
4. [ ] Navigate to Admin Dashboard
5. [ ] Click "Products" tab
6. [ ] Click "Add Product" button (+ icon)
7. [ ] Fill in product details:
   - Name: `Test Product`
   - Description: `Test Description`
   - Price: `99.99`
   - Category: `SNACKS`
8. [ ] Click "Select Image" button
9. [ ] Choose an image from gallery (< 15MB, JPEG/PNG/WebP/GIF)
10. [ ] Wait for upload to complete

**Expected Result:**
- ✅ Image uploads successfully
- ✅ Image URL appears in the form
- ✅ No error messages
- ✅ Loading indicator shows during upload
- ✅ Success message appears

**Actual Result:**
- [ ] Pass / [ ] Fail
- Notes: _______________________________________________

---

### Test Case 1.2: Image Upload Without AWS Credentials

**Steps:**
1. [ ] Stop backend server
2. [ ] Remove AWS credentials:
   ```powershell
   Remove-Item Env:\AWS_ACCESS_KEY
   Remove-Item Env:\AWS_SECRET_KEY
   ```
3. [ ] Try to start backend server

**Expected Result:**
- ✅ Backend fails to start
- ✅ Clear error message: "AWS credentials are required for S3 operations"
- ✅ Application doesn't start (fail-fast behavior)

**Actual Result:**
- [ ] Pass / [ ] Fail
- Notes: _______________________________________________

---

### Test Case 1.3: Image Upload with Invalid Credentials

**Steps:**
1. [ ] Set invalid AWS credentials:
   ```powershell
   $env:AWS_ACCESS_KEY="invalid-key"
   $env:AWS_SECRET_KEY="invalid-secret"
   ```
2. [ ] Start backend server
3. [ ] Try to upload an image (follow Test Case 1.1 steps 1-9)

**Expected Result:**
- ✅ Backend starts successfully
- ✅ Image upload fails with clear error message
- ✅ Error message mentions S3 or AWS
- ✅ Error is logged in backend console

**Actual Result:**
- [ ] Pass / [ ] Fail
- Notes: _______________________________________________

---

### Test Case 1.4: Image Upload with Invalid File Type

**Steps:**
1. [ ] Ensure valid AWS credentials are set
2. [ ] Follow Test Case 1.1 steps 1-8
3. [ ] Try to upload a non-image file (e.g., .txt, .pdf, .mp4)

**Expected Result:**
- ✅ Upload fails with error message
- ✅ Error message: "Invalid file type. Allowed types: JPEG, PNG, WebP, GIF"

**Actual Result:**
- [ ] Pass / [ ] Fail
- Notes: _______________________________________________

---

### Test Case 1.5: Image Upload with Large File

**Steps:**
1. [ ] Follow Test Case 1.1 steps 1-8
2. [ ] Try to upload an image > 15MB

**Expected Result:**
- ✅ Upload fails with error message
- ✅ Error message: "File size exceeds maximum allowed size of 15MB"

**Actual Result:**
- [ ] Pass / [ ] Fail
- Notes: _______________________________________________

---

## 🔐 Test 2: Google Sign-In (Student)

### Test Case 2.1: First-Time Google Sign-In

**Steps:**
1. [ ] Open CampusFood app
2. [ ] Ensure "Student" tab is selected on login screen
3. [ ] Click "Continue with Google" button
4. [ ] Select a Google account that has NOT been used before
5. [ ] Wait for sign-in to complete

**Expected Result:**
- ✅ Google account picker appears
- ✅ Sign-in completes successfully
- ✅ New user account is created in backend
- ✅ User is logged in automatically
- ✅ Navigates to menu screen
- ✅ User role is "CUSTOMER"
- ✅ User name matches Google account name
- ✅ User email matches Google account email

**Actual Result:**
- [ ] Pass / [ ] Fail
- Notes: _______________________________________________

---

### Test Case 2.2: Returning Google User Sign-In

**Steps:**
1. [ ] Logout from the app (if logged in)
2. [ ] Click "Continue with Google" button
3. [ ] Select the SAME Google account used in Test Case 2.1
4. [ ] Wait for sign-in to complete

**Expected Result:**
- ✅ Sign-in completes successfully
- ✅ Existing user account is used (not duplicated)
- ✅ User is logged in automatically
- ✅ Navigates to menu screen
- ✅ User data is preserved from previous session

**Actual Result:**
- [ ] Pass / [ ] Fail
- Notes: _______________________________________________

---

### Test Case 2.3: Google Sign-In Cancellation

**Steps:**
1. [ ] Logout from the app
2. [ ] Click "Continue with Google" button
3. [ ] When account picker appears, press back button or cancel

**Expected Result:**
- ✅ Returns to login screen
- ✅ Error message: "Google sign-in was canceled or failed to start"
- ✅ No crash or unexpected behavior

**Actual Result:**
- [ ] Pass / [ ] Fail
- Notes: _______________________________________________

---

### Test Case 2.4: Google Sign-In Without SHA-1 Registration

**Prerequisites:**
- [ ] Remove SHA-1 from Google Cloud Console (or use different package name)

**Steps:**
1. [ ] Rebuild and install app
2. [ ] Click "Continue with Google" button
3. [ ] Select a Google account

**Expected Result:**
- ✅ Error message appears
- ✅ Error mentions "Code 10" or "SHA-1 not registered"
- ✅ Clear guidance on how to fix

**Actual Result:**
- [ ] Pass / [ ] Fail
- Notes: _______________________________________________

---

### Test Case 2.5: Google Sign-In Without Internet

**Steps:**
1. [ ] Disable internet connection on device
2. [ ] Click "Continue with Google" button

**Expected Result:**
- ✅ Error message appears
- ✅ Error mentions connection or network issue
- ✅ No crash

**Actual Result:**
- [ ] Pass / [ ] Fail
- Notes: _______________________________________________

---

## 🔄 Test 3: Integration Tests

### Test Case 3.1: Complete User Flow with Google Sign-In

**Steps:**
1. [ ] Sign in with Google (new account)
2. [ ] Browse menu items
3. [ ] Add items to cart
4. [ ] Place an order
5. [ ] View order history
6. [ ] Logout
7. [ ] Sign in again with same Google account
8. [ ] Verify order history is preserved

**Expected Result:**
- ✅ All steps complete successfully
- ✅ User data persists across sessions
- ✅ Orders are associated with correct user

**Actual Result:**
- [ ] Pass / [ ] Fail
- Notes: _______________________________________________

---

### Test Case 3.2: Admin Product Management with Images

**Steps:**
1. [ ] Login as admin
2. [ ] Create new product with image
3. [ ] Verify product appears in menu
4. [ ] Edit product and change image
5. [ ] Verify updated image appears
6. [ ] Delete product
7. [ ] Verify product is removed from menu

**Expected Result:**
- ✅ All CRUD operations work correctly
- ✅ Images upload and display properly
- ✅ Old images are cleaned up when replaced

**Actual Result:**
- [ ] Pass / [ ] Fail
- Notes: _______________________________________________

---

## 📊 Test Summary

### Image Upload Tests
- Test 1.1: [ ] Pass / [ ] Fail
- Test 1.2: [ ] Pass / [ ] Fail
- Test 1.3: [ ] Pass / [ ] Fail
- Test 1.4: [ ] Pass / [ ] Fail
- Test 1.5: [ ] Pass / [ ] Fail

**Image Upload Score:** _____ / 5

### Google Sign-In Tests
- Test 2.1: [ ] Pass / [ ] Fail
- Test 2.2: [ ] Pass / [ ] Fail
- Test 2.3: [ ] Pass / [ ] Fail
- Test 2.4: [ ] Pass / [ ] Fail
- Test 2.5: [ ] Pass / [ ] Fail

**Google Sign-In Score:** _____ / 5

### Integration Tests
- Test 3.1: [ ] Pass / [ ] Fail
- Test 3.2: [ ] Pass / [ ] Fail

**Integration Score:** _____ / 2

---

## 🎯 Overall Result

**Total Score:** _____ / 12

- [ ] All tests passed ✅
- [ ] Some tests failed (see notes above) ⚠️
- [ ] Major issues found ❌

---

## 📝 Notes and Issues

Document any issues found during testing:

1. _______________________________________________
2. _______________________________________________
3. _______________________________________________
4. _______________________________________________
5. _______________________________________________

---

## ✅ Sign-Off

**Tested By:** _____________________
**Date:** _____________________
**Environment:**
- Backend Version: _____________________
- App Version: _____________________
- Device/Emulator: _____________________
- Android Version: _____________________

**Status:** [ ] Approved / [ ] Needs Fixes

---

**Next Steps:**
- If all tests pass: Deploy to production ✅
- If tests fail: Review FIXES_APPLIED.md and troubleshoot ⚠️
- For help: Check error messages and documentation 📚
