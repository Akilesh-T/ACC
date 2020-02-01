package app.akilesh.qacc

import android.annotation.SuppressLint
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

        val AEX = listOf(
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

        val brandColors = listOf(
            Colour("#0099E5", contextConst.getString(R.string.fiveHundredpx)),
            Colour("#FF0000", contextConst.getString(R.string.adobe)),
            Colour("#FD5C63", contextConst.getString(R.string.airbnb)),
            Colour("#FF6A00", contextConst.getString(R.string.alibaba)),
            Colour("#0C3866", contextConst.getString(R.string.alienware)),
            Colour("#ED1C24", contextConst.getString(R.string.alphabet)),
            Colour("#FF9900", contextConst.getString(R.string.amazon)),
            Colour("#3DDC84", contextConst.getString(R.string.android)),
            Colour("#1793D1", contextConst.getString(R.string.archLinux)),
            Colour("#00A8E0", contextConst.getString(R.string.att)),
            Colour("#472F92", contextConst.getString(R.string.cadbury)),
            Colour("#ED1C16", contextConst.getString(R.string.cocacola)),
            Colour("#A80030", contextConst.getString(R.string.debian)),
            Colour("#0085C3", contextConst.getString(R.string.dell)),
            Colour("#7289DA", contextConst.getString(R.string.discord)),
            Colour("#3B5998", contextConst.getString(R.string.fb)),
            Colour("#2BB24C", contextConst.getString(R.string.feedly)),
            Colour("#E32119", contextConst.getString(R.string.ferrari)),
            Colour("#E66000", contextConst.getString(R.string.firefox)),
            Colour("#4078C0", contextConst.getString(R.string.github)),
            Colour("#4285F4", contextConst.getString(R.string.google_blue)),
            Colour("#34A853", contextConst.getString(R.string.google_green)),
            Colour("#FBBC05", contextConst.getString(R.string.google_yellow)),
            Colour("#EA4335", contextConst.getString(R.string.google_red)),
            Colour("#E1306C", contextConst.getString(R.string.instagram)),
            Colour("#DDB321", contextConst.getString(R.string.lamborghini)),
            Colour("#124191", contextConst.getString(R.string.nokia)),
            Colour("#EB0029", contextConst.getString(R.string.oneplus)),
            Colour("#003087", contextConst.getString(R.string.playStation)),
            Colour("#00FF00", contextConst.getString(R.string.razer)),
            Colour("#1428A0", contextConst.getString(R.string.samsung)),
            Colour("#1DB954", contextConst.getString(R.string.spotify)),
            Colour("#E20074", contextConst.getString(R.string.t_mobile)),
            Colour("#0088CC", contextConst.getString(R.string.telegram)),
            Colour("#CC0000", contextConst.getString(R.string.tesla)),
            Colour("#1DA1F2", contextConst.getString(R.string.twitter)),
            Colour("#DD4814", contextConst.getString(R.string.ubuntu)),
            Colour("#128C7E", contextConst.getString(R.string.whatsapp)),
            Colour("#0078D7", contextConst.getString(R.string.windows)),
            Colour("#F59714", contextConst.getString(R.string.xda))
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
        @SuppressLint("SdCardPath")
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
