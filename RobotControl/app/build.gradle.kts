plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") version "4.4.0" apply false
}

android {
    namespace = "com.example.robotcontrol"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.robotcontrol"
        minSdk = 29
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // RecyclerView for robot list
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    
    // ViewPager2 for education content
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    
    // Material Components (for TabLayout and modern UI)
    implementation("com.google.android.material:material:1.11.0")
    
    // Fragment support
    implementation("androidx.fragment:fragment:1.6.2")
    
    // Firebase for authentication and database
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    
    // Bluetooth and WiFi support
    implementation("androidx.core:core-ktx:1.12.0")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}