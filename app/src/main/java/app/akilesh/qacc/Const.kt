package app.akilesh.qacc

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Environment.DIRECTORY_DOCUMENTS
import android.os.Environment.DIRECTORY_DOWNLOADS
import app.akilesh.qacc.model.Colour
import java.io.File

object Const {

    lateinit var contextConst : Context
    fun setContext(appContext: Context) {
        contextConst = appContext
    }
    //Credits to AEX
    object Colors {

        val AEX = listOf(
            Colour("#FFC107", contextConst.getString(R.string.amber)),
            Colour("#448AFF", contextConst.getString(R.string.title_blue)),
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
            Colour("#FF5252", contextConst.getString(R.string.title_red)),
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

        private val mdColors = mapOf(
            0 to contextConst.getString(R.string.title_red),
            1 to contextConst.getString(R.string.pink),
            2 to contextConst.getString(R.string.purple),
            3 to contextConst.getString(R.string.deep_purple),
            4 to contextConst.getString(R.string.indigo),
            5 to contextConst.getString(R.string.title_blue),
            6 to contextConst.getString(R.string.light_blue),
            7 to contextConst.getString(R.string.cyan),
            8 to contextConst.getString(R.string.teal),
            9 to contextConst.getString(R.string.title_green),
            10 to contextConst.getString(R.string.light_green),
            11 to contextConst.getString(R.string.lime),
            12 to contextConst.getString(R.string.yellove),
            13 to contextConst.getString(R.string.amber),
            14 to contextConst.getString(R.string.orange),
            15 to contextConst.getString(R.string.deep_orange),
            16 to contextConst.getString(R.string.brown),
            17 to contextConst.getString(R.string.grey),
            18 to contextConst.getString(R.string.blue_grey)
        )
        val mdColorPalette = mapOf(
            0 to listOf(
                Colour("#FFEBEE", "${mdColors[0]} 50"),
                Colour("#FFCDD2", "${mdColors[0]} 100"),
                Colour("#EF9A9A", "${mdColors[0]} 200"),
                Colour("#E57373", "${mdColors[0]} 300"),
                Colour("#EF5350", "${mdColors[0]} 400"),
                Colour("#F44336", "${mdColors[0]} 500"),
                Colour("#E53935", "${mdColors[0]} 600"),
                Colour("#D32F2F", "${mdColors[0]} 700"),
                Colour("#C62828", "${mdColors[0]} 800"),
                Colour("#B71C1C", "${mdColors[0]} 900"),
                Colour("#FF8A80", "${mdColors[0]} A100"),
                Colour("#FF5252", "${mdColors[0]} A200"),
                Colour("#FF1744", "${mdColors[0]} A400"),
                Colour("#D50000", "${mdColors[0]} A700")
            ),
            1 to listOf(
                Colour("#FCE4EC", "${mdColors[1]} 50"),
                Colour("#F8BBD0", "${mdColors[1]} 100"),
                Colour("#F48FB1", "${mdColors[1]} 200"),
                Colour("#F06292", "${mdColors[1]} 300"),
                Colour("#EC407A", "${mdColors[1]} 400"),
                Colour("#E91E63", "${mdColors[1]} 500"),
                Colour("#D81B60", "${mdColors[1]} 600"),
                Colour("#C2185B", "${mdColors[1]} 700"),
                Colour("#AD1457", "${mdColors[1]} 800"),
                Colour("#880E4F", "${mdColors[1]} 900"),
                Colour("#FF80AB", "${mdColors[1]} A100"),
                Colour("#FF4081", "${mdColors[1]} A200"),
                Colour("#F50057", "${mdColors[1]} A400"),
                Colour("#C51162", "${mdColors[1]} A700")
            ),
            2 to listOf(
                Colour("#F3E5F5", "${mdColors[2]} 50"),
                Colour("#E1BEE7", "${mdColors[2]} 100"),
                Colour("#CE93D8", "${mdColors[2]} 200"),
                Colour("#BA68C8", "${mdColors[2]} 300"),
                Colour("#AB47BC", "${mdColors[2]} 400"),
                Colour("#9C27B0", "${mdColors[2]} 500"),
                Colour("#8E24AA", "${mdColors[2]} 600"),
                Colour("#7B1FA2", "${mdColors[2]} 700"),
                Colour("#6A1B9A", "${mdColors[2]} 800"),
                Colour("#4A148C", "${mdColors[2]} 900"),
                Colour("#EA80FC", "${mdColors[2]} A100"),
                Colour("#E040FB", "${mdColors[2]} A200"),
                Colour("#D500F9", "${mdColors[2]} A400"),
                Colour("#AA00FF", "${mdColors[2]} A700")
            ),
            3 to listOf(
                Colour("#EDE7F6", "${mdColors[3]} 50"),
                Colour("#D1C4E9", "${mdColors[3]} 100"),
                Colour("#B39DDB", "${mdColors[3]} 200"),
                Colour("#9575CD", "${mdColors[3]} 300"),
                Colour("#7E57C2", "${mdColors[3]} 400"),
                Colour("#673AB7", "${mdColors[3]} 500"),
                Colour("#5E35B1", "${mdColors[3]} 600"),
                Colour("#512DA8", "${mdColors[3]} 700"),
                Colour("#4527A0", "${mdColors[3]} 800"),
                Colour("#311B92", "${mdColors[3]} 900"),
                Colour("#B388FF", "${mdColors[3]} A100"),
                Colour("#7C4DFF", "${mdColors[3]} A200"),
                Colour("#651FFF", "${mdColors[3]} A400"),
                Colour("#6200EA", "${mdColors[3]} A700")
            ),
            4 to listOf(
                Colour("#E8EAF6", "${mdColors[4]} 50"),
                Colour("#C5CAE9", "${mdColors[4]} 100"),
                Colour("#9FA8DA", "${mdColors[4]} 200"),
                Colour("#7986CB", "${mdColors[4]} 300"),
                Colour("#5C6BC0", "${mdColors[4]} 400"),
                Colour("#3F51B5", "${mdColors[4]} 500"),
                Colour("#3949AB", "${mdColors[4]} 600"),
                Colour("#303F9F", "${mdColors[4]} 700"),
                Colour("#283593", "${mdColors[4]} 800"),
                Colour("#1A237E", "${mdColors[4]} 900"),
                Colour("#8C9EFF", "${mdColors[4]} A100"),
                Colour("#536DFE", "${mdColors[4]} A200"),
                Colour("#3D5AFE", "${mdColors[4]} A400"),
                Colour("#304FFE", "${mdColors[4]} A700")
            ),
            5 to listOf(
                Colour("#E3F2FD", "${mdColors[5]} 50"),
                Colour("#BBDEFB", "${mdColors[5]} 100"),
                Colour("#90CAF9", "${mdColors[5]} 200"),
                Colour("#64B5F6", "${mdColors[5]} 300"),
                Colour("#42A5F5", "${mdColors[5]} 400"),
                Colour("#2196F3", "${mdColors[5]} 500"),
                Colour("#1E88E5", "${mdColors[5]} 600"),
                Colour("#1976D2", "${mdColors[5]} 700"),
                Colour("#1565C0", "${mdColors[5]} 800"),
                Colour("#0D47A1", "${mdColors[5]} 900"),
                Colour("#82B1FF", "${mdColors[5]} A100"),
                Colour("#448AFF", "${mdColors[5]} A200"),
                Colour("#2979FF", "${mdColors[5]} A400"),
                Colour("#2962FF", "${mdColors[5]} A700")
            ),
            6 to listOf(
                Colour("#E1F5FE", "${mdColors[6]} 50"),
                Colour("#B3E5FC", "${mdColors[6]} 100"),
                Colour("#81D4FA", "${mdColors[6]} 200"),
                Colour("#4FC3F7", "${mdColors[6]} 300"),
                Colour("#29B6F6", "${mdColors[6]} 400"),
                Colour("#03A9F4", "${mdColors[6]} 500"),
                Colour("#039BE5", "${mdColors[6]} 600"),
                Colour("#0288D1", "${mdColors[6]} 700"),
                Colour("#0277BD", "${mdColors[6]} 800"),
                Colour("#01579B", "${mdColors[6]} 900"),
                Colour("#80D8FF", "${mdColors[6]} A100"),
                Colour("#40C4FF", "${mdColors[6]} A200"),
                Colour("#00B0FF", "${mdColors[6]} A400"),
                Colour("#0091EA", "${mdColors[6]} A700")
            ),
            7 to listOf(
                Colour("#E0F7FA", "${mdColors[7]} 50"),
                Colour("#B2EBF2", "${mdColors[7]} 100"),
                Colour("#80DEEA", "${mdColors[7]} 200"),
                Colour("#4DD0E1", "${mdColors[7]} 300"),
                Colour("#26C6DA", "${mdColors[7]} 400"),
                Colour("#00BCD4", "${mdColors[7]} 500"),
                Colour("#00ACC1", "${mdColors[7]} 600"),
                Colour("#0097A7", "${mdColors[7]} 700"),
                Colour("#00838F", "${mdColors[7]} 800"),
                Colour("#006064", "${mdColors[7]} 900"),
                Colour("#84FFFF", "${mdColors[7]} A100"),
                Colour("#18FFFF", "${mdColors[7]} A200"),
                Colour("#00E5FF", "${mdColors[7]} A400"),
                Colour("#00B8D4", "${mdColors[7]} A700")
            ),
            8 to listOf(
                Colour("#E0F2F1", "${mdColors[8]} 50"),
                Colour("#B2DFDB", "${mdColors[8]} 100"),
                Colour("#80CBC4", "${mdColors[8]} 200"),
                Colour("#4DB6AC", "${mdColors[8]} 300"),
                Colour("#26A69A", "${mdColors[8]} 400"),
                Colour("#009688", "${mdColors[8]} 500"),
                Colour("#00897B", "${mdColors[8]} 600"),
                Colour("#00796B", "${mdColors[8]} 700"),
                Colour("#00695C", "${mdColors[8]} 800"),
                Colour("#004D40", "${mdColors[8]} 900"),
                Colour("#A7FFEB", "${mdColors[8]} A100"),
                Colour("#64FFDA", "${mdColors[8]} A200"),
                Colour("#1DE9B6", "${mdColors[8]} A400"),
                Colour("#00BFA5", "${mdColors[8]} A700")
            ),
            9 to listOf(
                Colour("#E8F5E9", "${mdColors[9]} 50"),
                Colour("#C8E6C9", "${mdColors[9]} 100"),
                Colour("#A5D6A7", "${mdColors[9]} 200"),
                Colour("#81C784", "${mdColors[9]} 300"),
                Colour("#66BB6A", "${mdColors[9]} 400"),
                Colour("#4CAF50", "${mdColors[9]} 500"),
                Colour("#43A047", "${mdColors[9]} 600"),
                Colour("#388E3C", "${mdColors[9]} 700"),
                Colour("#2E7D32", "${mdColors[9]} 800"),
                Colour("#1B5E20", "${mdColors[9]} 900"),
                Colour("#B9F6CA", "${mdColors[9]} A100"),
                Colour("#69F0AE", "${mdColors[9]} A200"),
                Colour("#00E676", "${mdColors[9]} A400"),
                Colour("#00C853", "${mdColors[9]} A700")
            ),
            10 to listOf(
                Colour("#F1F8E9", "${mdColors[10]} 50"),
                Colour("#DCEDC8", "${mdColors[10]} 100"),
                Colour("#C5E1A5", "${mdColors[10]} 200"),
                Colour("#AED581", "${mdColors[10]} 300"),
                Colour("#9CCC65", "${mdColors[10]} 400"),
                Colour("#8BC34A", "${mdColors[10]} 500"),
                Colour("#7CB342", "${mdColors[10]} 600"),
                Colour("#689F38", "${mdColors[10]} 700"),
                Colour("#558B2F", "${mdColors[10]} 800"),
                Colour("#33691E", "${mdColors[10]} 900"),
                Colour("#CCFF90", "${mdColors[10]} A100"),
                Colour("#B2FF59", "${mdColors[10]} A200"),
                Colour("#76FF03", "${mdColors[10]} A400"),
                Colour("#64DD17", "${mdColors[10]} A700")
            ),
            11 to listOf(
                Colour("#F9FBE7", "${mdColors[11]} 50"),
                Colour("#F0F4C3", "${mdColors[11]} 100"),
                Colour("#E6EE9C", "${mdColors[11]} 200"),
                Colour("#DCE775", "${mdColors[11]} 300"),
                Colour("#D4E157", "${mdColors[11]} 400"),
                Colour("#CDDC39", "${mdColors[11]} 500"),
                Colour("#C0CA33", "${mdColors[11]} 600"),
                Colour("#AFB42B", "${mdColors[11]} 700"),
                Colour("#9E9D24", "${mdColors[11]} 800"),
                Colour("#827717", "${mdColors[11]} 900"),
                Colour("#F4FF81", "${mdColors[11]} A100"),
                Colour("#EEFF41", "${mdColors[11]} A200"),
                Colour("#C6FF00", "${mdColors[11]} A400"),
                Colour("#AEEA00", "${mdColors[11]} A700")
            ),
            12 to listOf(
                Colour("#FFFDE7", "${mdColors[12]} 50"),
                Colour("#FFF9C4", "${mdColors[12]} 100"),
                Colour("#FFF59D", "${mdColors[12]} 200"),
                Colour("#FFF176", "${mdColors[12]} 300"),
                Colour("#FFEE58", "${mdColors[12]} 400"),
                Colour("#FFEB3B", "${mdColors[12]} 500"),
                Colour("#FDD835", "${mdColors[12]} 600"),
                Colour("#FBC02D", "${mdColors[12]} 700"),
                Colour("#F9A825", "${mdColors[12]} 800"),
                Colour("#F57F17", "${mdColors[12]} 900"),
                Colour("#FFFF8D", "${mdColors[12]} A100"),
                Colour("#FFFF00", "${mdColors[12]} A200"),
                Colour("#FFEA00", "${mdColors[12]} A400"),
                Colour("#FFD600", "${mdColors[12]} A700")
            ),
            13 to listOf(
                Colour("#FFF8E1", "${mdColors[13]} 50"),
                Colour("#FFECB3", "${mdColors[13]} 100"),
                Colour("#FFE082", "${mdColors[13]} 200"),
                Colour("#FFD54F", "${mdColors[13]} 300"),
                Colour("#FFCA28", "${mdColors[13]} 400"),
                Colour("#FFC107", "${mdColors[13]} 500"),
                Colour("#FFB300", "${mdColors[13]} 600"),
                Colour("#FFA000", "${mdColors[13]} 700"),
                Colour("#FF8F00", "${mdColors[13]} 800"),
                Colour("#FF6F00", "${mdColors[13]} 900"),
                Colour("#FFE57F", "${mdColors[13]} A100"),
                Colour("#FFD740", "${mdColors[13]} A200"),
                Colour("#FFC400", "${mdColors[13]} A400"),
                Colour("#FFAB00", "${mdColors[13]} A700")
            ),
            14 to listOf(
                Colour("#FFF3E0", "${mdColors[14]} 50"),
                Colour("#FFE0B2", "${mdColors[14]} 100"),
                Colour("#FFCC80", "${mdColors[14]} 200"),
                Colour("#FFB74D", "${mdColors[14]} 300"),
                Colour("#FFA726", "${mdColors[14]} 400"),
                Colour("#FF9800", "${mdColors[14]} 500"),
                Colour("#FB8C00", "${mdColors[14]} 600"),
                Colour("#F57C00", "${mdColors[14]} 700"),
                Colour("#EF6C00", "${mdColors[14]} 800"),
                Colour("#E65100", "${mdColors[14]} 900"),
                Colour("#FFD180", "${mdColors[14]} A100"),
                Colour("#FFAB40", "${mdColors[14]} A200"),
                Colour("#FF9100", "${mdColors[14]} A400"),
                Colour("#FF6D00", "${mdColors[14]} A700")
            ),
            15 to listOf(
                Colour("#FBE9E7", "${mdColors[15]} 50"),
                Colour("#FFCCBC", "${mdColors[15]} 100"),
                Colour("#FFAB91", "${mdColors[15]} 200"),
                Colour("#FF8A65", "${mdColors[15]} 300"),
                Colour("#FF7043", "${mdColors[15]} 400"),
                Colour("#FF5722", "${mdColors[15]} 500"),
                Colour("#F4511E", "${mdColors[15]} 600"),
                Colour("#E64A19", "${mdColors[15]} 700"),
                Colour("#D84315", "${mdColors[15]} 800"),
                Colour("#BF360C", "${mdColors[15]} 900"),
                Colour("#FF9E80", "${mdColors[15]} A100"),
                Colour("#FF6E40", "${mdColors[15]} A200"),
                Colour("#FF3D00", "${mdColors[15]} A400"),
                Colour("#DD2C00", "${mdColors[15]} A700")
            ),
            16 to listOf(
                Colour("#EFEBE9", "${mdColors[16]} 50"),
                Colour("#D7CCC8", "${mdColors[16]} 100"),
                Colour("#BCAAA4", "${mdColors[16]} 200"),
                Colour("#A1887F", "${mdColors[16]} 300"),
                Colour("#8D6E63", "${mdColors[16]} 400"),
                Colour("#795548", "${mdColors[16]} 500"),
                Colour("#6D4C41", "${mdColors[16]} 600"),
                Colour("#5D4037", "${mdColors[16]} 700"),
                Colour("#4E342E", "${mdColors[16]} 800"),
                Colour("#3E2723", "${mdColors[16]} 900")
            ),
            17 to listOf(
                Colour("#FAFAFA", "${mdColors[17]} 50"),
                Colour("#F5F5F5", "${mdColors[17]} 100"),
                Colour("#EEEEEE", "${mdColors[17]} 200"),
                Colour("#E0E0E0", "${mdColors[17]} 300"),
                Colour("#BDBDBD", "${mdColors[17]} 400"),
                Colour("#9E9E9E", "${mdColors[17]} 500"),
                Colour("#757575", "${mdColors[17]} 600"),
                Colour("#616161", "${mdColors[17]} 700"),
                Colour("#424242", "${mdColors[17]} 800"),
                Colour("#212121", "${mdColors[17]} 900")
            ),
            18 to listOf(
                Colour("#ECEFF1", "${mdColors[18]} 50"),
                Colour("#CFD8DC", "${mdColors[18]} 100"),
                Colour("#B0BEC5", "${mdColors[18]} 200"),
                Colour("#90A4AE", "${mdColors[18]} 300"),
                Colour("#78909C", "${mdColors[18]} 400"),
                Colour("#607D8B", "${mdColors[18]} 500"),
                Colour("#546E7A", "${mdColors[18]} 600"),
                Colour("#455A64", "${mdColors[18]} 700"),
                Colour("#37474F", "${mdColors[18]} 800"),
                Colour("#263238", "${mdColors[18]} 900")
            )
        )
        const val nokiaBlue = "zzz_nokia_blue"

        val colorSpaces = listOf(
            contextConst.getString(R.string.color_space_rgb),
            contextConst.getString(R.string.color_space_hsl)
            //contextConst.getString(R.string.color_space_lab)
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
        val backupFolder = File.separatorChar + "sdcard/${DIRECTORY_DOCUMENTS}/${contextConst.getString(R.string.app_name_short)}/backups"
        val updatesFolder = DIRECTORY_DOWNLOADS + File.separatorChar + "${contextConst.getString(R.string.app_name)} updates"
    }

    const val prefix = "com.android.theme.color.custom."

    const val busyBox = "/data/adb/magisk/busybox"

    var selected = setOf<Colour>()

}
