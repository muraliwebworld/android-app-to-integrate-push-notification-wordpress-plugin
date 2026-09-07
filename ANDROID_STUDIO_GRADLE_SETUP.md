# ✅ Android Studio Gradle Setup Guide

## Overview
This project has been fully migrated to modern Gradle 8.6 with the latest Android build tools. Everything is ready to build in Android Studio.

---

## 📋 What's Been Updated

### Gradle Files
- ✅ **build.gradle** (root) - Migrated from buildscript to plugins block (modern style)
- ✅ **settings.gradle** - Cleaned up, removed non-existent module references
- ✅ **gradle.properties** - Updated with modern settings and caching options
- ✅ **gradle/wrapper/gradle-wrapper.properties** - Updated to Gradle 8.6
- ✅ **app/build.gradle** - Complete with all plugins, dependencies, and buildConfig enabled

### Key Updates
- Android Gradle Plugin: 8.1.2 (latest)
- Gradle Version: 8.6
- Kotlin: 1.9.10
- Java: 17
- Compile SDK: 34
- Target SDK: 34
- Min SDK: 24

---

## 🚀 Quick Start in Android Studio

### Step 1: Open Project

1. **File → Open**
2. Navigate to: `/Users/muralidharanramasamy/Documents/android_java_app_pnfpb_github/android-project`
3. Click **Open**

### Step 2: Wait for Gradle Sync

- Android Studio will automatically start Gradle sync
- You'll see: "Gradle: Build completed"
- Wait for all tasks to complete (usually 2-5 minutes on first sync)

### Step 3: Configure Domain (Important!)

1. Open: `app/build.gradle`
2. Find line ~24:
   ```gradle
   buildConfigField "String", "API_BASE_URL", '"https://www.muraliwebworld.com/"'
   ```
3. Replace with your WordPress domain:
   ```gradle
   buildConfigField "String", "API_BASE_URL", '"https://your-wordpress-site.com/"'
   ```
4. Click **Sync Now** (blue banner appears at top)

### Step 4: Rename MainActivityModern.kt to MainActivity.kt

1. In **Project Explorer** (left panel)
2. Navigate: `app → src → main → java → com.sample.pnfpbandroid`
3. Right-click `MainActivityModern.kt`
4. Select: **Refactor → Rename**
5. Type: `MainActivity.kt`
6. Click **Refactor** button

### Step 5: Build the Project

#### Method 1: Build Menu (GUI)
1. **Build → Make Project** (or Ctrl+F9 / Cmd+Shift+K)
2. Wait for build to complete
3. Look for: "Build completed successfully" in Build panel

#### Method 2: Gradle Console (Terminal)
1. **View → Tool Windows → Gradle**
2. Or use built-in Terminal: **View → Tool Windows → Terminal**
3. Run:
   ```bash
   ./gradlew clean assembleDebug
   ```

Expected output:
```
> Task :app:assembleDebug
BUILD SUCCESSFUL in XXs
```

---

## 🔨 Build Commands

### Build Debug APK
```bash
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK
```bash
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release.apk`

### Build App Bundle (for Play Store)
```bash
./gradlew bundleRelease
```
Output: `app/build/outputs/bundle/release/app-release.aab`

### Clean Build
```bash
./gradlew clean assembleDebug
```

### Run on Device/Emulator
```bash
./gradlew installDebug
```

### Check Gradle Tasks
```bash
./gradlew tasks
```

---

## 📱 Run on Device/Emulator

### Using Android Studio

1. **Device Setup**
   - Connect Android device via USB
   - Enable Developer Mode on device
   - Enable USB Debugging
   - Or start an Android Emulator

2. **Run App**
   - Click green **Run** button (▶) at top
   - Or: **Run → Run 'app'** (Shift+F10)
   - Select device/emulator

3. **View Logs**
   - **View → Tool Windows → Logcat**
   - Filter for app logs:
     ```
     I/MainActivity: WebView loading: https://your-wordpress-site.com/
     I/MainActivity: FCM Token obtained
     I/SubscriptionRepository: Token sent successfully
     ```

---

## ✅ Gradle Sync Issues & Solutions

### Issue 1: "Sync Now" button doesn't disappear
**Solution**:
1. **File → Invalidate Caches**
2. Click **Invalidate and Restart**
3. Android Studio will restart and re-sync

### Issue 2: Gradle download timeout
**Solution**:
1. Open: `gradle/wrapper/gradle-wrapper.properties`
2. Change to offline cache (if downloaded before):
   ```properties
   distributionUrl=file:///path/to/gradle-8.6-all.zip
   ```
3. Or disable gradle parallel processing in Android Studio:
   - **Android Studio Preferences → Build, Execution, Deployment → Gradle**
   - Uncheck "Run in parallel"

### Issue 3: Out of memory during build
**Solution**:
1. Increase Gradle heap in `gradle.properties`:
   ```properties
   org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m -Dfile.encoding=UTF-8
   ```
2. Restart Android Studio

### Issue 4: Plugin not found errors
**Solution**:
1. **File → Invalidate Caches → Invalidate and Restart**
2. Wait for full Gradle re-download (5-10 minutes)

---

## 🏗️ Gradle Project Structure

```
android-project/
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties (✅ Updated to 8.6)
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/sample/pnfpbandroid/
│   │   │   │   ├── MainActivity.kt (← Rename from MainActivityModern.kt)
│   │   │   │   ├── PushNotificationApplication.kt
│   │   │   │   ├── JavaScriptInterfaceModern.kt
│   │   │   │   └── ... (7 more Kotlin files)
│   │   │   ├── res/
│   │   │   ├── AndroidManifest.xml
│   │   │   └── assets/
│   │   ├── androidTest/
│   │   └── test/
│   ├── build/ (generated after build)
│   └── build.gradle (✅ Modern configuration)
├── build.gradle (✅ Updated with plugins block)
├── settings.gradle (✅ Cleaned up)
├── gradle.properties (✅ Modern settings)
├── gradlew (Gradle wrapper script - no changes needed)
├── gradlew.bat (Windows wrapper - no changes needed)
├── local.properties (Build variables)
└── ... (documentation files)
```

---

## 📊 Gradle Build Process

### What Happens During Build

1. **Task: assembleDebug**
   - Compiles Kotlin/Java code
   - Generates R.java from resources
   - Builds DEX files
   - Packages resources
   - Signs with debug key
   - Creates APK

2. **Key Tasks in Order**
   ```
   :app:preBuild
   :app:compileDebugKotlin
   :app:compileDebugJavaWithJavac
   :app:processDebugResources
   :app:mergeDebugDexes
   :app:packageDebug
   :app:assembleDebug
   BUILD SUCCESSFUL
   ```

3. **Output Location**
   - Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
   - Logs: `app/build/outputs/logs/`

---

## 🔍 Troubleshooting Gradle

### Check Gradle Version
```bash
./gradlew --version
```
Expected: `Gradle 8.6`

### Verify Project Configuration
```bash
./gradlew tasks
```
Should list all available tasks without errors

### Force Re-download Dependencies
```bash
rm -rf ~/.gradle/caches/
./gradlew clean
```

### Check Gradle Logs
```bash
./gradlew assembleDebug --debug 2>&1 | tee build.log
```

### Validate Gradle Wrapper
```bash
./gradlew wrapper --gradle-version=8.6
```

---

## 📝 Configuration Files Explained

### build.gradle (root)
```gradle
plugins {
    id 'com.android.application' version '8.1.2' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.10' apply false
    // ... other plugins
}
```
- Defines all plugins used by subprojects
- `apply false` = subprojects will apply when needed
- Modern replacement for buildscript block

### settings.gradle
```gradle
rootProject.name = "pnfpb-android"
include ':app'
```
- Defines which modules to include
- Cleaned up to only include :app
- Removed non-existent modules

### gradle.properties
```properties
org.gradle.jvmargs=-Xmx2048m
org.gradle.caching=true
org.gradle.daemon=true
android.useAndroidX=true
android.nonTransitiveRClass=true
```
- Global Gradle settings
- Performance optimizations
- Android-specific flags

### app/build.gradle
```gradle
android {
    compileSdk 34
    defaultConfig {
        applicationId "com.sample.pnfpbandroid"
        buildConfigField "String", "API_BASE_URL", ...
    }
    buildFeatures {
        buildConfig = true  // ✅ Enabled for buildConfigField
        viewBinding = true
    }
}
```
- App-specific configuration
- Compile/target SDK versions
- Build features and dependencies

---

## 🎯 Next Steps

1. ✅ Open project in Android Studio
2. ✅ Wait for Gradle sync to complete
3. ✅ Edit `app/build.gradle` line 24 with your domain
4. ✅ Rename `MainActivityModern.kt` → `MainActivity.kt`
5. ✅ Click **Build → Make Project**
6. ✅ Connect device and click **Run**
7. ✅ Check Logcat for success messages

---

## 🚀 You're Ready!

The project is fully configured for Android Studio with modern Gradle.

**Just three things needed:**
1. Update domain in `build.gradle`
2. Rename `MainActivityModern.kt` → `MainActivity.kt`
3. Click the green Run button!

---

## 📚 References

- [Android Gradle Plugin Guide](https://developer.android.com/build)
- [Gradle Documentation](https://docs.gradle.org/8.6/release-notes.html)
- [Kotlin Gradle Plugin](https://kotlinlang.org/docs/gradle.html)
- [Android Studio Build System](https://developer.android.com/studio/build)

---

**Status**: ✅ **GRADLE FULLY CONFIGURED** - Ready for Android Studio
