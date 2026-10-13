# Lessgo muss!

Lessgo muss! is a private, offline Android study timer I built to track my own study sessions. I wanted one place on my phone to see how long I studied, which subjects I worked on, and how my routine changes over time.

The app is written in Kotlin with Jetpack Compose and JDK 17. The Android project lives at the repository root.

## What it does

- Start, pause, resume, finish, or discard a study session. The timer survives app restarts.
- Organize time by subject and set a daily study goal.
- Review daily totals, session counts, a calendar, and weekly and monthly insights.
- Optionally receive a daily summary notification.

## Data

Sessions and settings are stored privately on this Android device. The app does not use a server or account. The data remains across normal app restarts and updates, but uninstalling the app, clearing its storage, or losing the phone removes the local copy. See the in-app Settings screen for data controls.

## Build

Install JDK 17 and Android SDK API 36 with Build Tools 35 or later. Set `JAVA_HOME` and `ANDROID_HOME`, or create `local.properties` with `sdk.dir=/path/to/Android/Sdk`.

```bash
./scripts/build-local.sh
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. The script stages builds in a cache directory because this workspace path contains `!`, which the Kotlin compiler treats as a JAR separator. Android Studio users should open a checkout in a path without `!`.
