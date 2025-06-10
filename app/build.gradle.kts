import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "live.ditto.dittomigrationandroid"
    compileSdk = 35

    defaultConfig {
        applicationId = "live.ditto.dittomigrationandroid"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Load secret data into Build Config
        buildConfigField(
            "String",
            "EXISTING_APP_ID",
            getLocalProperty("existing_app_id")
        )

        buildConfigField(
            "String",
            "EXISTING_PLAYGROUND_TOKEN",
            getLocalProperty("existing_playground_token")
        )

        buildConfigField(
            "String",
            "EXISTING_WEBSOCKET_URL",
            getLocalProperty("existing_websocket_url")
        )

        buildConfigField(
            "String",
            "EXISTING_AUTH_URL",
            getLocalProperty("existing_auth_url")
        )

        buildConfigField(
            "String",
            "NEW_APP_ID",
            getLocalProperty("new_app_id")
        )

        buildConfigField(
            "String",
            "NEW_PLAYGROUND_TOKEN",
            getLocalProperty("new_playground_token")
        )

        buildConfigField(
            "String",
            "NEW_WEBSOCKET_URL",
            getLocalProperty("new_websocket_url")
        )

        buildConfigField(
            "String",
            "NEW_AUTH_URL",
            getLocalProperty("new_auth_url")
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Ditto SDK
    implementation(libs.ditto)
}

fun getLocalProperty(key: String, file: String = "local.properties"): String {
    val properties = Properties()
    val localProperties = File(file)
    if (localProperties.isFile) {
        InputStreamReader(FileInputStream(localProperties), Charsets.UTF_8).use { reader ->
            properties.load(reader)
        }
    } else {
        error("File not found")
    }

    return properties.getProperty(key)
}