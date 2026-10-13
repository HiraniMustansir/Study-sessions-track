# Studyflow native Android project

- This is a Kotlin/JDK Android application built with Gradle and Jetpack Compose. Do not add React Native, Expo, Node, JavaScript, or EAS files.
- Use the Gradle wrapper (`./gradlew`) for builds and tests. The project uses JDK 17 and compiles to JVM 17 bytecode.
- Keep study data on-device. Timer state must survive activity recreation and process restarts.
- Run `./gradlew testDebugUnitTest assembleDebug` before declaring changes complete.
- Commit each meaningful implementation stage to the workspace root Git repository. The project directory has no nested `.git`.
