plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "uz.safar.ttsproxy"
    compileSdk = 34

    defaultConfig {
        applicationId = "uz.safar.ttsproxy"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // Release APK'ni haqiqiy kalit bilan imzolash sozlamalari. Parollar va
    // kalit fayli HECH QACHON kodga yozilmaydi - faqat muhit
    // o'zgaruvchilaridan o'qiladi (GitHub Actions Secrets orqali beriladi).
    // Agar bu o'zgaruvchilar mavjud bo'lmasa (masalan mahalliy sinov
    // uchun), release build oddiy debug kaliti bilan imzolanadi - bu
    // build'ni sindirmaydi, faqat "ishonchli" imzo bo'lmaydi.
    val releaseKeystorePath = System.getenv("RELEASE_KEYSTORE_PATH")
    val releaseKeystorePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
    val releaseKeyAlias = System.getenv("RELEASE_KEY_ALIAS")
    val releaseKeyPassword = System.getenv("RELEASE_KEY_PASSWORD")
    val hasReleaseSigningEnv = !releaseKeystorePath.isNullOrBlank() &&
        !releaseKeystorePassword.isNullOrBlank() &&
        !releaseKeyAlias.isNullOrBlank() &&
        !releaseKeyPassword.isNullOrBlank()

    signingConfigs {
        if (hasReleaseSigningEnv) {
            create("release") {
                storeFile = file(releaseKeystorePath!!)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasReleaseSigningEnv) {
                signingConfig = signingConfigs.getByName("release")
            }
            // hasReleaseSigningEnv=false bo'lsa, Android Gradle Plugin
            // release'ni standart debug kaliti bilan imzolaydi - build
            // baribir muvaffaqiyatli tugaydi, faqat imzo "ishonchli" emas.
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
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
}
