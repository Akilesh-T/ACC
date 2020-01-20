package app.akilesh.qacc

import android.content.Context
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import app.akilesh.qacc.model.Colour
import com.topjohnwu.superuser.Shell

object Const {

    private lateinit var context : Context
    fun setContext(appContext: Context) {
        context = appContext
    }
    //Credits to AEX
    object Colors {

        val presets = listOf(
            Colour("#FFC107", context.getString(R.string.amber)),
            Colour("#448AFF", context.getString(R.string.blue)),
            Colour("#607D8B", context.getString(R.string.blue_grey)),
            Colour("#795548", context.getString(R.string.brown)),
            Colour("#FF1744", context.getString(R.string.candy_red)),
            Colour("#00BCD4", context.getString(R.string.cyan)),
            Colour("#FF5722", context.getString(R.string.deep_orange)),
            Colour("#7C4DFF", context.getString(R.string.deep_purple)),
            Colour("#47AE84", context.getString(R.string.elegant_green)),
            Colour("#21EF8B", context.getString(R.string.extended_green)),
            Colour("#9E9E9E", context.getString(R.string.grey)),
            Colour("#536DFE", context.getString(R.string.indigo)),
            Colour("#9ABC98", context.getString(R.string.jade_green)),
            Colour("#03A9F4", context.getString(R.string.light_blue)),
            Colour("#8BC34A", context.getString(R.string.light_green)),
            Colour("#CDDC39", context.getString(R.string.lime)),
            Colour("#FF9800", context.getString(R.string.orange)),
            Colour("#A1B6ED", context.getString(R.string.pale_blue)),
            Colour("#F05361", context.getString(R.string.pale_red)),
            Colour("#FF4081", context.getString(R.string.pink)),
            Colour("#FF5252", context.getString(R.string.red)),
            Colour("#009688", context.getString(R.string.teal)),
            Colour("#FFEB3B", context.getString(R.string.yellove))
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
