import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

/**
 * La API key nunca se hardcodea: se lee de local.properties, que no se sube a git.
 * Si falta, la app compila igual y avisa en pantalla en lugar de fallar con 401 a ciegas.
 */
val ferreteriaApiKey: String = run {
    val archivo = rootProject.file("local.properties")
    if (!archivo.exists()) return@run ""
    val props = Properties()
    archivo.inputStream().use { flujo -> props.load(flujo) }
    props.getProperty("FERRETERIA_API_KEY", "").trim()
}

android {
    namespace = "com.example.ferreteria"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.ferreteria"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "FERRETERIA_API_KEY", "\"$ferreteriaApiKey\"")
        buildConfigField(
            "String",
            "FERRETERIA_BASE_URL",
            "\"https://ferreteria-api.proxmox-lab.cc/api/\""
        )
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    // Los iconos ya no llegan de forma transitiva con material3: hay que pedirlos.
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp.logging.interceptor)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
