import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.example.dispoahora"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.dispoahora"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", "\"${localProperties.getProperty("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProperties.getProperty("SUPABASE_ANON_KEY")}\"")
        buildConfigField("String", "WEB_GOOGLE_CLIENT_ID", "\"${localProperties.getProperty("WEB_GOOGLE_CLIENT_ID")}\"")
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
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // Eliminé la linea duplicada de 'libs.androidx.credentials' que tenías suelta

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // --- CORRECCIÓN DE SUPABASE (Versión 3.x) ---

    // 1. Usamos el BOM para gestionar versiones automáticamente (Evita conflictos)
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.2"))

    // 2. Módulos necesarios (Nota: Ya no ponemos la versión al final gracias al BOM)
    implementation("io.github.jan-tennert.supabase:postgrest-kt") // Base de datos
    implementation("io.github.jan-tennert.supabase:auth-kt")      // ¡FALTABA ESTE! (Login)
    implementation("io.github.jan-tennert.supabase:realtime-kt")  // Opcional, pero útil para DispoAhora

    // 3. Motor HTTP de Ktor (¡OBLIGATORIO para que funcione en Android!)
    implementation("io.ktor:ktor-client-android:3.0.1")

    // --- OTRAS DEPENDENCIAS ---

    // Serialización JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3") // Actualizado a una versión más reciente compatible

    // Lifecycle Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7") // Actualizado
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")       // Actualizado

    // Credential Manager (Para el Login con Google nativo)
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    implementation("androidx.navigation:navigation-compose:2.8.0")
}