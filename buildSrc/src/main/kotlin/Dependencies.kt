const val kotlinVersion = "1.4.21"
const val navigationVersion = "2.3.2"


object BuildPlugins {

    object Versions {
        const val gradleBuildToolsVersion = "4.1.1"
    }

    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.gradleBuildToolsVersion}"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    const val navigationSafeArgsGradlePlugin = "androidx.navigation:navigation-safe-args-gradle-plugin:$navigationVersion"

    const val androidApplication = "com.android.application"
    const val kotlinAndroid = "kotlin-android"
    const val kotlinParcelize = "kotlin-parcelize"
    const val kotlinKapt = "kotlin-kapt"
    const val navigationSafeArgs  = "androidx.navigation.safeargs.kotlin"

}

object AndroidSdk {
    const val min = 26
    const val compile = 30
    const val target = 30
}

object Libraries {
    private object Versions {
        const val jetpack = "1.2.0"
        const val constraintLayout = "2.0.4"
        const val core = "1.5.0-alpha05"
        const val preference = "1.1.1"
        const val palette = "1.0.0"
        const val room = "2.3.0-alpha04"
        const val lifecycle = "2.2.0"
        const val coroutines = "1.4.2"
        const val fragment = "1.3.0-rc01"
        const val activity = "1.2.0-rc01"
        const val material = "1.3.0-beta01"
        const val libSu = "3.0.2"
        const val assent = "3.0.0-RC4"
        const val appUpdater = "2.7"
        const val whatTheStack = "0.2.0"
        const val workManager = "2.4.0"
        const val recyclerView = "1.1.0"
        const val recyclerViewSelection = "1.1.0-rc03"
        const val viewpager2 = "1.0.0"
        const val andColorPicker = "0.5.0"
        const val bouncyCastleProvider = "1.67"
        const val bouncyCastlePKIX = "1.67"
        const val paging = "3.0.0-alpha11"
    }

    const val kotlinStdLib        = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"
    const val appCompat           = "androidx.appcompat:appcompat:${Versions.jetpack}"
    const val constraintLayout    = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    const val coreKtx             = "androidx.core:core-ktx:${Versions.core}"
    const val activityKtx         = "androidx.activity:activity-ktx:${Versions.activity}"
    const val fragmentKtx         = "androidx.fragment:fragment-ktx:${Versions.fragment}"
    const val preferenceKtx       = "androidx.preference:preference-ktx:${Versions.preference}"
    const val paletteKtx          = "androidx.palette:palette-ktx:${Versions.palette}"

    const val roomRuntime         = "androidx.room:room-runtime:${Versions.room}"
    const val roomKtx             = "androidx.room:room-ktx:${Versions.room}"
    const val roomCompiler        = "androidx.room:room-compiler:${Versions.room}"

    const val lifecycleCommon     = "androidx.lifecycle:lifecycle-common-java8:${Versions.lifecycle}"
    const val lifecycleLiveData   = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"
    const val lifecycleViewModel  = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"

    const val coroutines          = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"

    const val navigationFragment  = "androidx.navigation:navigation-fragment-ktx:${navigationVersion}"
    const val navigationUI        = "androidx.navigation:navigation-ui-ktx:${navigationVersion}"

    const val materialComponents  = "com.google.android.material:material:${Versions.material}"

    const val libSuCore           = "com.github.topjohnwu.libsu:core:${Versions.libSu}"
    const val libsuIO             = "com.github.topjohnwu.libsu:io:${Versions.libSu}"

    const val assentCore          = "com.afollestad.assent:core:${Versions.assent}"
    const val assentRationales    = "com.afollestad.assent:rationales:${Versions.assent}"
    const val assentCoroutines    = "com.afollestad.assent:coroutines:${Versions.assent}"

    const val appUpdater          = "com.github.javiersantos:AppUpdater:${Versions.appUpdater}"

    const val whatTheStack        = "com.github.haroldadmin:WhatTheStack:${Versions.whatTheStack}"

    const val workManager         = "androidx.work:work-runtime-ktx:${Versions.workManager}"

    const val recyclerView        = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"
    const val rvSelection         = "androidx.recyclerview:recyclerview-selection:${Versions.recyclerViewSelection}"

    const val viewpager2          = "androidx.viewpager2:viewpager2:${Versions.viewpager2}"

    const val andColorPicker      = "codes.side:andcolorpicker:${Versions.andColorPicker}"

    const val bcProvider          = "org.bouncycastle:bcprov-jdk15on:${Versions.bouncyCastleProvider}"
    const val bcPKIX              = "org.bouncycastle:bcpkix-jdk15on:${Versions.bouncyCastlePKIX}"

    const val paging              = "androidx.paging:paging-runtime-ktx:${Versions.paging}"
}