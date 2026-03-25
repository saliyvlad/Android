plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.calculator"
    compileSdk = 34 // Рекомендую снизить до 34, если на 36 есть проблемы со стабильностью API, но 36 тоже ок, если стоят все патчи. Оставь 36, если уверен.
    // compileSdk = 36

    defaultConfig {
        applicationId = "com.example.calculator"
        minSdk = 33
        targetSdk = 34 // Тоже лучше синхронизировать, но можно оставить 36
        // targetSdk = 36
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
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ZeroMQ
    implementation(libs.zeromq)

    // Coroutines (Явное подключение, раз libs не сработал)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    // Версию 1.8.0 иногда ругает на старых Gradle, 1.7.3 самая стабильная. Если хочешь 1.8.0 - оставь как было.

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}