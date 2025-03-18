import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}


android {
    namespace = "com.example.filmcataloge"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.filmcataloge"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        val tmdbApiKey = if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
            localProperties.getProperty("TMDB_API_KEY", "")
        } else {
            ""
        }
        buildConfigField("String", "TMDB_API_KEY", "\"$tmdbApiKey\"")


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
    buildFeatures{
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation ("com.google.code.gson:gson:2.10.1")

    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

    implementation ("androidx.datastore:datastore-preferences:1.1.2")
    
    annotationProcessor ("androidx.room:room-compiler:2.5.2")
    implementation("androidx.room:room-runtime:2.6.1")

    implementation ("androidx.core:core-splashscreen:1.0.0")

    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}