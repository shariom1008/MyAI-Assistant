plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.myaiassistant"

    compileSdk = 34
buildFeatures {
    buildConfig = true
}

defaultConfig {
    applicationId = "com.example.myaiassistant"
    minSdk = 24
    targetSdk = 34
    versionCode = 6
    versionName = "6.0"

    buildConfigField(
        "String",
        "GEMINI_API_KEY",
        "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\""
    )

buildConfigField(
    "String",
    "YOUTUBE_API_KEY",
    "\"${project.findProperty("YOUTUBE_API_KEY") ?: ""}\""
)
}
    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
implementation(platform("com.google.firebase:firebase-bom:34.18.0"))
implementation("com.google.firebase:firebase-auth")
}
