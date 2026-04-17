plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

}

android {
    namespace = "com.example.financipline"
    compileSdk {
        version = release(36)
    }
    viewBinding{
        enable = true
    }
    defaultConfig {
        applicationId = "com.example.financipline"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("io.github.jan-tennert.supabase:auth-kt:3.0.1")

    implementation("io.github.jan-tennert.supabase:postgrest-kt:3.0.1")
    implementation("io.ktor:ktor-client-android:3.0.1")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("androidx.navigation:navigation-fragment-ktx:2.8.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.5")
}