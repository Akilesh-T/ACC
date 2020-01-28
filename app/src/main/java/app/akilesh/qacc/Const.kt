package app.akilesh.qacc

import android.content.Context
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Environment.DIRECTORY_DOCUMENTS
import app.akilesh.qacc.model.Colour
import com.topjohnwu.superuser.Shell

object Const {

    lateinit var contextConst : Context
    fun setContext(appContext: Context) {
        contextConst = appContext
    }
    //Credits to AEX
    object Colors {

        val presets = listOf(
            Colour("#FFC107", contextConst.getString(R.string.amber)),
            Colour("#448AFF", contextConst.getString(R.string.blue)),
            Colour("#607D8B", contextConst.getString(R.string.blue_grey)),
            Colour("#795548", contextConst.getString(R.string.brown)),
            Colour("#FF1744", contextConst.getString(R.string.candy_red)),
            Colour("#00BCD4", contextConst.getString(R.string.cyan)),
            Colour("#FF5722", contextConst.getString(R.string.deep_orange)),
            Colour("#7C4DFF", contextConst.getString(R.string.deep_purple)),
            Colour("#47AE84", contextConst.getString(R.string.elegant_green)),
            Colour("#21EF8B", contextConst.getString(R.string.extended_green)),
            Colour("#9E9E9E", contextConst.getString(R.string.grey)),
            Colour("#536DFE", contextConst.getString(R.string.indigo)),
            Colour("#9ABC98", contextConst.getString(R.string.jade_green)),
            Colour("#03A9F4", contextConst.getString(R.string.light_blue)),
            Colour("#8BC34A", contextConst.getString(R.string.light_green)),
            Colour("#CDDC39", contextConst.getString(R.string.lime)),
            Colour("#FF9800", contextConst.getString(R.string.orange)),
            Colour("#A1B6ED", contextConst.getString(R.string.pale_blue)),
            Colour("#F05361", contextConst.getString(R.string.pale_red)),
            Colour("#FF4081", contextConst.getString(R.string.pink)),
            Colour("#FF5252", contextConst.getString(R.string.red)),
            Colour("#009688", contextConst.getString(R.string.teal)),
            Colour("#FFEB3B", contextConst.getString(R.string.yellove))
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

    object Paths {
        const val modPath = "/data/adb/modules/qacc-mobile"
        val overlayPath = if (SDK_INT == Q) "$modPath/system/product/overlay"
        else "$modPath/system/vendor/overlay"
        val backupFolder = "/sdcard/${DIRECTORY_DOCUMENTS}/${contextConst.getString(R.string.app_name_short)}/backups"
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
