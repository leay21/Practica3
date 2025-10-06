plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.practica3"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.practica3"
        minSdk = 24
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
    buildFeatures {
        compose = true
    }
}

dependencies {

    // --- LIBRERÍAS ESENCIALES PARA VISTAS/XML ---
    implementation(libs.androidx.core.ktx) // Core de Kotlin, se mantiene
    implementation("androidx.appcompat:appcompat:1.6.1") // Para AppCompatActivity y compatibilidad
    implementation("com.google.android.material:material:1.12.0") // Para componentes de Material Design
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // Para construir layouts complejos en XML
    implementation("androidx.recyclerview:recyclerview:1.3.2") // Para la lista de archivos

    // --- LIBRERÍAS DE ARQUITECTURA (MVVM) ---
    // Estas se mantienen igual
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.9.0")

    // --- LIBRERÍAS DE PRUEBAS ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}