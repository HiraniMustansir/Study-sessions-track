# Lessgo muss!

Lessgo muss! is a native Android app written in Kotlin with Jetpack Compose. It requires JDK 17 and the Android SDK. There is no React Native, Expo, Node, or EAS dependency.

## Features

- Start, pause, resume, finish, and discard study sessions. The timer state is saved and elapsed time continues while the app is in the background.
- Organize sessions by subject, with custom colors and icons.
- Track today's total, session count, daily goal, and study streak.
- Browse a monthly activity calendar and the sessions on any day.
- View weekly and monthly focus time, longest session, active-day average, and subject breakdown.
- Opt in to a local daily summary notification. Android computes the summary from saved sessions when the reminder fires. Delivery can be delayed by battery restrictions.
- Store all data on the device. Uninstalling the app removes that data; backup and account sync are not yet implemented.

## Build

Install JDK 17 and Android SDK API 36 with Build Tools 35 or later. Set `JAVA_HOME` and `ANDROID_HOME`, or add a `local.properties` file with `sdk.dir=/path/to/Android/Sdk`.

```bash
./scripts/build-local.sh
```

That runs unit tests and builds a debug APK at `app/build/outputs/apk/debug/app-debug.apk`. You can also pass Gradle tasks, for example `./scripts/build-local.sh lintDebug`.

The current workspace folder contains `!!`. Kotlin's compiler treats `!` as a JAR path separator, so `scripts/build-local.sh` stages the source in a safe cache path before invoking the Gradle wrapper. In a normally named folder, the script calls `./gradlew` directly. Android Studio can open this project, but for IDE builds, use a checkout whose full path contains no `!`.

## Git

The workspace root is the Git repository. Changes to this Android project are committed there after each implementation stage.
