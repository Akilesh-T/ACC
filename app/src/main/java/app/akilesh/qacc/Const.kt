package app.akilesh.qacc

import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import app.akilesh.qacc.model.Colour
import com.topjohnwu.superuser.Shell

object Const {

    //Credits to AEX
    object Colors {
        val presets = listOf(
            Colour("#FFC107", "Amber"),
            Colour("#448AFF", "Blue"),
            Colour("#607D8B", "Blue Grey"),
            Colour("#795548", "Brown"),
            Colour("#FF1744", "Candy Red"),
            Colour("#00BCD4", "Cyan"),
            Colour("#FF5722", "Deep Orange"),
            Colour("#7C4DFF", "Deep Purple"),
            Colour("#47AE84", "Elegant Green"),
            Colour("#21EF8B", "Extended Green"),
            Colour("#9E9E9E", "Grey"),
            Colour("#536DFE", "Indigo"),
            Colour("#9ABC98", "Jade Green"),
            Colour("#03A9F4", "Light Blue"),
            Colour("#8BC34A", "Light Green"),
            Colour("#CDDC39", "Lime"),
            Colour("#FF9800", "Orange"),
            Colour("#A1B6ED", "Pale Blue"),
            Colour("#F05361", "Pale Red"),
            Colour("#FF4081", "Pink"),
            Colour("#FF5252", "Red"),
            Colour("#009688", "Teal"),
            Colour("#FFEB3B", "Yellow")
        )

    }

    object Links {
        const val telegramGroup = "https://t.me/AccentColourCreator"
        const val xdaThread =
            "https://forum.xda-developers.com/android/apps-games/app-magisk-module-qacc-custom-accent-t4011747"
        const val githubRepo = "https://github.com/Akilesh-T/ACC"
        const val telegramChannel = "https://t.me/ACC_Releases"
        const val githubReleases = "$githubRepo/releases/latest"
    }

    object Module {
        private const val modPath = "/data/adb/modules/qacc-mobile"
        val overlayPath = if (SDK_INT == Q) "$modPath/system/product/overlay"
        else "$modPath/system/vendor/overlay"
    }

    const val prefix = "com.android.theme.color.custom."

    val isOOS = Shell.sh("getprop ro.oxygen.version").exec().out.component1().isNotBlank()

    fun getAssetFiles(): MutableList<String> {

        val assetFiles = mutableListOf<String>()

        val arch = if (listOf(Build.SUPPORTED_64_BIT_ABIS).isNotEmpty()) "arm64" else "arm"
        if (arch == "arm64")
            assetFiles.addAll(listOf("aapt64", "zipalign64"))
        else
            assetFiles.addAll(listOf("aapt", "zipalign"))

        return assetFiles
    }

}
