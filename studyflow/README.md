# Studyflow

Studyflow is a native Android application written in Kotlin with Jetpack Compose. It uses the Android Gradle Plugin, Gradle wrapper, and JDK 17. No React Native or Expo runtime is required.

The project currently targets Android API 36 and supports Android 8.0 (API 26) and newer.

## Build

Open this directory in Android Studio, set the Android SDK location, and build the `app` module. From a terminal with `JAVA_HOME` pointing to JDK 17 and `ANDROID_HOME` pointing to an Android SDK:

```bash
./gradlew testDebugUnitTest assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.
