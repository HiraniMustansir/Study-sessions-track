# Lessgo muss!

Lessgo muss! is a private, offline Android study timer. I made it to track my personal study sessions: how long I study, which subjects I work on, and how my routine changes over time. It is designed to keep that history on my phone without an account or hosted service.

The app is written in Kotlin with Jetpack Compose and JDK 17. The Android project lives at the repository root.

## What it does

- Start, pause, resume, finish, or discard a study session. A running timer survives app restarts.
- Organize time by subject, add subjects, and set a daily study goal.
- Review daily totals and session counts, a monthly calendar with each day's sessions, and weekly and monthly insights including longest session, average study time, and subject breakdown.
- Optionally receive a local daily summary notification at a chosen evening hour. Android battery settings may delay it.
- Export and import all subjects, finished sessions, goal, and reminder settings as a JSON backup.

## Data

The app saves a JSON record in Android's private app preferences at `shared_prefs/lessgomuss_v1.xml` inside the `com.lessgomuss.tracker` app's private storage. Other ordinary apps cannot read it. Each change is written to device storage before the app displays the new state. The timer's start time and elapsed time are saved, so closing and reopening the app does not reset it. No account, internet connection, or server is required. Automatic Android cloud backup and device transfer are disabled.

The local record remains across ordinary restarts and updates to **this same app ID**, as long as the update is signed compatibly. Uninstalling, clearing app storage, factory resetting, or losing the phone removes that local record. No app can guarantee permanent data on a single device, so make periodic backups.

### Back up and change phones

1. Finish or discard an active timer. Open **Settings → Export backup** and choose where to save the `.json` file.
2. Copy that file to a computer, USB drive, or another phone. A copy left only on the original phone will be lost with that phone. Keep at least one extra copy and export again regularly.
3. Install Lessgo muss! on the new phone, transfer the file there, then open **Settings → Import backup** and select it. The calendar and insights rebuild from the imported sessions.
4. Check the subject list and several days in the calendar before deleting the old phone's data. Import **replaces** the new phone's existing subjects, sessions, and settings; it does not merge them. Export the new phone first if it already has sessions.

The backup file is readable JSON. Keep it private; anyone with a copy can see the study history. You can store the copy offline. The APK itself contains no personal session history. Your personal portrait is ignored by Git and is not included in the repository; a checkout without that local image uses the included vector icon.

**Earlier builds:** This version uses Android app ID `com.lessgomuss.tracker`. Earlier builds used a different ID, so they are separate apps and their private data cannot be read automatically by this version. If an older build already contains sessions, keep it installed; a one-time migration build would be needed to export that data.

## Build

Install JDK 17 and Android SDK API 36 with Build Tools 35 or later. Set `JAVA_HOME` and `ANDROID_HOME`, or create `local.properties` with `sdk.dir=/path/to/Android/Sdk`.

```bash
./scripts/build-local.sh
./scripts/build-local.sh testDebugUnitTest assembleRelease
```

The signed release APK is written to `app/build/outputs/apk/release/app-release.apk`. Release signing uses `~/.local/share/lessgomuss/release-key.p12` and `~/.local/share/lessgomuss/signing.properties` on this computer. Both files are outside Git. **Back up both signing files securely**: future releases must use the same key to update an installed release app without uninstalling it. To use another location, set `LESSGOMUSS_SIGNING_PROPERTIES` to the signing properties file's absolute path. The APK itself is not committed.

The default command builds a debug APK for testing. A debug APK has a different signature from the release APK, so Android cannot install the release APK over it. If the debug app contains sessions, export a backup there first, uninstall the debug app, install the release APK, and import the backup. The script stages builds in a cache directory because this workspace path contains `!`, which the Kotlin compiler treats as a JAR separator. Android Studio users should open a checkout in a path without `!`.
