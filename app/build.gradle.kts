plugins {
    id(BuildPlugins.androidApplication)
    id(BuildPlugins.kotlinAndroid)
    id(BuildPlugins.kotlinAndroidExtensions)
    id(BuildPlugins.kotlinKapt)
    id(BuildPlugins.navigationSafeArgs)
}

android {
    compileSdkVersion(AndroidSdk.compile)
    buildToolsVersion = "29.0.3"
    defaultConfig {
        applicationId = "app.akilesh.qacc"
        minSdkVersion(AndroidSdk.min)
        targetSdkVersion(AndroidSdk.target)
        versionCode = 11
        versionName = "1.70"
        vectorDrawables.useSupportLibrary = true

        javaCompileOptions {
            annotationProcessorOptions {
                arguments = mapOf("room.incremental" to "true")
            }
        }
    }
    buildFeatures {
        viewBinding = true
        compose = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    packagingOptions {
        exclude("META-INF/**")
        exclude( "/org/bouncycastle/**")
        exclude("/kotlin/**")
        exclude("/okhttp3/**")
    }
}

dependencies {
    implementation(fileTree(mapOf( "dir" to "libs", "include" to listOf("*.jar"))))
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.appCompat)
    implementation(Libraries.ktxCore)
    implementation(Libraries.constraintLayout)
    implementation(Libraries.KtxPreference)
    implementation(Libraries.KtxPalette)

    // Room components
    implementation(Libraries.roomRuntime)
    implementation(Libraries.roomKtx)
    kapt(Libraries.roomCompiler)

    // Lifecycle components
    implementation(Libraries.lifecycleLiveData)
    implementation(Libraries.lifecycleCommon)

    // ViewModel Kotlin support
    implementation(Libraries.lifecycleViewModel)

    // Coroutines
    api(Libraries.coroutines)

    // Navigation component
    implementation(Libraries.navigationFragment)
    implementation(Libraries.navigationUI)

    implementation(Libraries.ktxFragment)

    implementation(Libraries.materialComponents)

    implementation(Libraries.libSuCore)
    implementation(Libraries.libsuIO)

    implementation(Libraries.lawnchairChroma)

    implementation(Libraries.assentCore)
    implementation(Libraries.assentRationales)

    implementation(Libraries.materialAbout)

    implementation(Libraries.appUpdater)

    debugImplementation(Libraries.whatTheStack)

    implementation(Libraries.workManager)

    implementation(Libraries.recyclerView)
    implementation(Libraries.rvSelection)

}
