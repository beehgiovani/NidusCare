plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
    id("com.google.firebase.crashlytics")
}


android {
    namespace = "com.developersbeeh.medcontrol"
    compileSdk = 36


    defaultConfig {
        applicationId = "com.developersbeeh.medcontrol"
        minSdk = 24
        targetSdk = 36
        versionCode = 7
        versionName = "2.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
         }

    buildTypes {
        release {
            isMinifyEnabled = true
            ndk.debugSymbolLevel = "FULL"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    dependenciesInfo {
        includeInApk = true
        includeInBundle = true
    }
}


dependencies {
    implementation(libs.androidx.paging.runtime.ktx)

    // Desugaring para funcionalidades Java 8+
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Firebase Functions (se a análise for na nuvem)
    implementation(libs.firebase.functions)

    // Dependências AndroidX e UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material.v1130)

    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.navigation.fragment.ktx.v294)
    implementation(libs.androidx.navigation.ui.ktx.v294)

    // Dependências Lifecycle (ViewModel e LiveData)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))

    // Gson
    implementation("com.google.code.gson:gson:2.13.2")
    // Firebase Auth
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)

    // Firebase Firestore
    implementation(libs.google.firebase.firestore)

    // Firebase Realtime Database
    implementation(libs.firebase.database)

    // Firebase Storage para salvar as imagens

    implementation(libs.coil)

    // Firebase Crashlytics & Analytics

    // Firebase Crashlytics & Analytics
    implementation(libs.firebase.analytics)

    // Kotlin Coroutines para integração com Play Services
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // Hilt (Injeção de Dependência)
    implementation(libs.hilt.android)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.common)
    implementation("com.google.firebase:firebase-storage-ktx:21.0.0")
    implementation(libs.googleid)
    implementation(libs.androidx.paging.common)

    ksp(libs.google.hilt.compiler)

    // Desugaring para funcionalidades Java 8+
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Calendário
    implementation("com.kizitonwose.calendar:view:2.5.1")

    // Gráficos
    implementation(libs.mpandroidchart)

    // Mensagens (FCM)
    implementation(libs.firebase.messaging)

    // Retrofit (Rede) - VERSÕES CORRIGIDAS
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")


    // ✅ PARA ANÁLISE DE JSON DA IA (ANÁLISE DE REFEIÇÃO)
    implementation (libs.squareup.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    //animações
    implementation(libs.lottie)
    
    // In-App Updates
    implementation(libs.app.update.ktx)

    implementation(libs.taptargetview)


    implementation(libs.billing.ktx)

    // Firebase App Check
    implementation(libs.firebase.appcheck.playintegrity)

    implementation(libs.shimmer)

    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")

    implementation("com.github.mhiew:android-pdf-viewer:3.2.0-beta.3")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    implementation(libs.play.services.ads)

    implementation("com.google.android.gms:play-services-location:21.3.0")
    // ADICIONE ESTA LINHA PARA O MODO DE DEPURAÇÃO
    debugImplementation(libs.firebase.appcheck.debug)

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
