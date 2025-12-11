plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.mariamolina"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.mariamolina"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // NOTA: Para publicar en Google Play, debes crear un keystore y configurar estas variables
            // en el archivo gradle.properties o en variables de entorno:
            // KEYSTORE_FILE=path/to/your/keystore.jks
            // KEYSTORE_PASSWORD=your_keystore_password
            // KEY_ALIAS=your_key_alias
            // KEY_PASSWORD=your_key_password

            // Ejemplo de configuración (descomentar y ajustar cuando tengas el keystore):
            // storeFile = file(System.getenv("KEYSTORE_FILE") ?: project.property("KEYSTORE_FILE") as String)
            // storePassword = System.getenv("KEYSTORE_PASSWORD") ?: project.property("KEYSTORE_PASSWORD") as String
            // keyAlias = System.getenv("KEY_ALIAS") ?: project.property("KEY_ALIAS") as String
            // keyPassword = System.getenv("KEY_PASSWORD") ?: project.property("KEY_PASSWORD") as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Descomentar cuando tengas el keystore configurado:
            // signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isDebuggable = true
            // applicationIdSuffix eliminado para evitar conflicto con Firebase
            // Firebase espera el package name 'com.example.mariamolina' sin sufijos
            versionNameSuffix = "-debug"
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

    implementation(libs.coil.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview )
    implementation(libs.androidx.compose.material3)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.animation:animation:1.7.0")
    implementation("androidx.compose.material:material-icons-extended-android:1.6.8")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("com.github.mkergall:osmbonuspack:6.9.0")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    
    // ML Kit Translation para traducción automática
    implementation("com.google.mlkit:translate:17.0.3")
}