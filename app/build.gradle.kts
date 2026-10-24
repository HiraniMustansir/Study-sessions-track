import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val signingPropertiesFile = providers.environmentVariable("LESSGOMUSS_SIGNING_PROPERTIES")
    .orNull?.let(::file)
    ?: file("${System.getProperty("user.home")}/.local/share/lessgomuss/signing.properties")
val signingProperties = Properties().apply {
    if (signingPropertiesFile.isFile) signingPropertiesFile.inputStream().use { load(it) }
}

android {
    namespace = "com.lessgomuss.tracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.lessgomuss.tracker"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "2.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    if (signingPropertiesFile.isFile) {
        signingConfigs.create("personalRelease") {
            storeFile = file(signingProperties.getProperty("storeFile"))
            storePassword = signingProperties.getProperty("storePassword")
            keyAlias = signingProperties.getProperty("keyAlias")
            keyPassword = signingProperties.getProperty("keyPassword")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (signingPropertiesFile.isFile) signingConfig = signingConfigs.getByName("personalRelease")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    sourceSets.getByName("main").res.srcDir(layout.buildDirectory.dir("generated/res/appIcon"))
}

val localIcon = file("src/main/local-icon/ic_launcher_photo.png")
val fallbackIcon = file("src/main/icon-fallback/ic_launcher_photo.xml")
val generatedIcon = layout.buildDirectory.dir("generated/res/appIcon")
val prepareAppIcon by tasks.registering {
    inputs.file(if (localIcon.exists()) localIcon else fallbackIcon)
    outputs.dir(generatedIcon)
    doLast {
        val target = generatedIcon.get().asFile.resolve("drawable-nodpi")
        target.mkdirs()
        target.listFiles()?.forEach { it.delete() }
        val source = if (localIcon.exists()) localIcon else fallbackIcon
        source.copyTo(target.resolve(source.name), overwrite = true)
    }
}
tasks.matching { it.name == "preBuild" || it.name.startsWith("map") && it.name.endsWith("SourceSetPaths") }
    .configureEach { dependsOn(prepareAppIcon) }

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.08.00")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.12.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}
