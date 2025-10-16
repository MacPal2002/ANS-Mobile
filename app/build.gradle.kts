
fun getGitCommitCount(): Int {
    return try {
        "git rev-list --count HEAD".runCommand().toInt()
    } catch (e: Exception) {
        System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1
    }
}

fun getGitVersionName(): String {
    return try {
        "git describe --tags --abbrev=0".runCommand()
    } catch (e: Exception) {
        "0.1.0"
    }
}

fun String.runCommand(): String {
    val parts = this.split("\\s".toRegex())
    val process = ProcessBuilder(parts)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    process.waitFor()
    return process.inputStream.bufferedReader().readText().trim()
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.hilt.gradle.plugin)
    id("com.google.devtools.ksp") version "2.0.21-1.0.25"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
}


android {
    namespace = "com.example.test1"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.test1"
        minSdk = 29
        targetSdk = 35
        versionCode = getGitCommitCount()
        //versionCode = 1
        //versionName = "0.1.0"
        versionName = getGitVersionName()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    /*signingConfigs {
        create("release") {
            val storeFilePath = project.findProperty("RELEASE_STORE_FILE") as String?
            if (storeFilePath != null) {
                storeFile = file(storeFilePath)
            }
            storePassword = project.findProperty("RELEASE_STORE_PASSWORD") as String? ?: ""
            keyAlias = project.findProperty("RELEASE_KEY_ALIAS") as String? ?: "my-key-alias"
            keyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as String? ?: ""
        }
    }*/
    signingConfigs {
        create("release") {
            // Sprawdź, czy właściwość została przekazana przez GitHub Actions z flagą -P
            if (project.hasProperty("signing.keyStore.file")) {
                // Odczytaj ścieżkę do pliku .jks z właściwości projektu
                storeFile = file(project.property("signing.keyStore.file") as String)

                // Odczytaj resztę danych ze zmiennych środowiskowych (tak jak przekazuje je `env:` w workflow)
                storePassword = System.getenv("KEY_STORE_PASSWORD")
                keyAlias = System.getenv("ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            if (signingConfigs.getByName("release").storeFile != null) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            // signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            applicationVariants.all {
                outputs.all {
                    val outputFileName = "app-${versionName}.apk"
                    (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName = outputFileName
                }
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

}

dependencies {
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.functions.ktx)

    // AndroidX Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material.icons.extended.android)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.constraintlayout.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.gson)

    // Inne
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.material)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(platform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.7.3"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("com.github.javiersantos:AppUpdater:2.7"){
    }

    // Testy
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}