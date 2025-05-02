    plugins {
        alias(libs.plugins.android.application)
        alias(libs.plugins.kotlin.android)
        alias(libs.plugins.kotlin.compose)
        id("com.google.gms.google-services")
    }

    android {
        namespace = "com.aliumitalgan.remindup"
        compileSdk = 35
        ndkVersion = "27.2.12479018"

        defaultConfig {
            applicationId = "com.aliumitalgan.remindup"
            minSdk = 25
            targetSdk = 35
            versionCode = 3
            versionName = "1.0.2"

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            manifestPlaceholders["web_client_id"] = "117825801795-d70srog7cr7m6hjengcjrl411har5u63.apps.googleusercontent.com"


        }

        signingConfigs {
            create("release") {
                storeFile = file("C:/Users/hp/my-release-key.jks") // Keystore dosyasının yolu
                storePassword = "kafadayoksalak44"  // Keystore şifresi
                keyAlias = "mykey"  // Key alias
                keyPassword = "kafadayoksalak44"  // Anahtar şifresi
            }
        }

        buildTypes {
            getByName("release") {
                signingConfig = signingConfigs.getByName("release")  // Release için imzalama işlemi
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
        }
    }


    dependencies {
        // Material Icons
        implementation(libs.androidx.datastore.preferences)

        // Coroutines (DataStore akışını kullanmak için)
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
        implementation("androidx.datastore:datastore-preferences:1.1.5")
        implementation("androidx.compose.material:material-icons-extended:1.7.8")
        implementation ("androidx.compose.material:material:1.5.0")
        // Google Auth
        implementation("com.google.android.gms:play-services-auth:20.7.0")

        // Firebase
        implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
        implementation("com.google.firebase:firebase-analytics-ktx")
        implementation("com.google.firebase:firebase-firestore-ktx")
        implementation("com.google.firebase:firebase-auth-ktx")

        // Navigation
        implementation("androidx.navigation:navigation-compose:2.7.7")

        // AppCompat
        implementation("androidx.appcompat:appcompat:1.6.1")

        // Core Android dependencies
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)

        // Compose
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.material3)

        // Testing
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.ui.test.junit4)
        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)
    }