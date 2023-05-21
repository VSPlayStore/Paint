plugins {
    id ("com.android.application")
    id ("com.google.gms.google-services")
    id ("kotlin-android")
}

val envFile = rootProject.file(".env").readText().trim().split("\n")
val env = LinkedHashMap<String, String>()
for (i in envFile) {
    env[i.trim().split("=")[0]] = i.trim().split("=")[1]
}

android {
    compileSdk = 33
    buildToolsVersion = "33.0.2"

    defaultConfig {
        applicationId = "com.vineelsai.paint"
        minSdk = 29
        targetSdk = 33
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        env["APP_ID"]?.let { resValue("string", "APP_ID", it) }
        env["AD_SAVE_ID"]?.let { resValue("string", "AD_SAVE_ID", it) }
    }
    buildFeatures {
        viewBinding = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
    }
    compileOptions {
        targetCompatibility = JavaVersion.VERSION_17
        sourceCompatibility = JavaVersion.VERSION_17
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.6"
    }
    bundle {
        storeArchive {
            enable = true
        }
    }
    dependenciesInfo {
        includeInApk = true
        includeInBundle = true
    }

    namespace ="com.vineelsai.paint"
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")

    // Core
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.preference:preference-ktx:1.2.0")

    // UI
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Firebase
    implementation("com.google.firebase:firebase-bom:32.0.0")
    implementation("com.google.firebase:firebase-analytics-ktx:21.2.2")

    // ADS
    implementation("com.google.android.gms:play-services-ads:22.0.0")

    // 3rd Party
    implementation("com.github.rtugeek:colorseekbar:1.7.7")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}