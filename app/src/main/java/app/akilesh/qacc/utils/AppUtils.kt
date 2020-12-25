package app.akilesh.qacc.utils

import android.app.Activity
import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.os.Build.VERSION_CODES.Q
import android.util.Log
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.use
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.navigation.navOptions
import androidx.palette.graphics.Palette
import app.akilesh.qacc.Const.Colors.nokiaBlue
import app.akilesh.qacc.Const.Paths.backupFolder
import app.akilesh.qacc.Const.Paths.busyBox
import app.akilesh.qacc.Const.Paths.overlayPath
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.FragmentColorPickerBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.model.Colour
import app.akilesh.qacc.utils.XmlUtils.createColors
import app.akilesh.qacc.utils.XmlUtils.createOverlayManifest
import app.akilesh.qacc.utils.signing.CryptoUtils.readCertificate
import app.akilesh.qacc.utils.signing.CryptoUtils.readPrivateKey
import app.akilesh.qacc.utils.signing.JarMap
import app.akilesh.qacc.utils.signing.SignAPK
import com.google.android.material.snackbar.Snackbar
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFileInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*


object AppUtils {
    private const val lightMode  = "light"
    private const val darkMode = "dark"
    private const val batterySaverMode = "battery"
    const val default = "default"

    fun applyTheme(theme: String?) {
        when (theme) {
            lightMode -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            darkMode -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            batterySaverMode -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
            }

            default -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

   /* fun getDesaturatedColor(color: Int, ratio: Float): String {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)

        hsv[1] = hsv[1] / 1 * ratio + 0.2f * (1.0f - ratio)

        return toHex(Color.HSVToColor(hsv))
    }*/

    fun toHex(@ColorInt color: Int) = String.format("#%06X", (0xFFFFFF and color))

    @ColorInt
    fun Context.getColorAccent(): Int {
        @AttrRes val attr = android.R.attr.colorAccent

        val typedArray = if (SDK_INT >= Q)
            obtainStyledAttributes(android.R.style.ThemeOverlay_DeviceDefault_Accent_DayNight, intArrayOf(attr))
        else obtainStyledAttributes(android.R.style.Theme_DeviceDefault, intArrayOf(attr))

        return typedArray.use {
            it.getColor(0, 0)
        }
    }

    @ColorInt
    fun Context.getThemeColor(@AttrRes themeAttrRes: Int) =
        obtainStyledAttributes(R.style.Theme_Acc_DayNight, intArrayOf(themeAttrRes)).use {
            it.getColor(0, 0)
        }

    fun Activity.showSnackBar(text: String) {
        val snackBar = Snackbar.make(
            findViewById(R.id.root),
            text,
            Snackbar.LENGTH_SHORT
        )
        snackBar.isAnchorViewLayoutListenerEnabled = true
        snackBar.anchorView = findViewById(R.id.fab)
        if (SDK_INT >= P) {
            snackBar.setAction(getString(R.string.reboot)) {
                Shell.su("/system/bin/svc power reboot || /system/bin/reboot")
                    .submit()
            }
        }
        snackBar.show()
    }

    val navAnim = navOptions {
        anim  {
            enter  = R.anim.fragment_enter
            exit = R.anim.fragment_exit
            popEnter = R.anim.fragment_enter_pop
            popExit = R.anim.fragment_exit_pop
        }
    }

    fun setPreview(
        binding: FragmentColorPickerBinding,
        colour: Colour?,
        isSystem: Boolean = false
    ) {

        if (colour != null) {
            val previewColor = Color.parseColor(colour.hex)
            val colorStateList = ColorStateList.valueOf(previewColor)

            binding.include.apply {
                previewColorQs0Bg.backgroundTintList = colorStateList
                previewColorQs1Bg.backgroundTintList = colorStateList
                previewColorQs2Bg.backgroundTintList = colorStateList

                previewSeekbar.thumbTintList = colorStateList
                previewSeekbar.progressTintList = colorStateList
                previewSeekbar.progressBackgroundTintList = colorStateList

                previewCheckSelected.buttonTintList = colorStateList
                previewRadioSelected.buttonTintList = colorStateList
                previewToggleSelected.buttonTintList = colorStateList
                previewToggleSelected.thumbTintList = colorStateList
                previewToggleSelected.trackTintList = colorStateList.withAlpha(127)
            }

            binding.apply {
                TextViewCompat.setCompoundDrawableTintList(mdcColorsText, colorStateList)
                TextViewCompat.setCompoundDrawableTintList(customText, colorStateList)
                TextViewCompat.setCompoundDrawableTintList(presetText, colorStateList)
                TextViewCompat.setCompoundDrawableTintList(wallColorsText, colorStateList)
                TextViewCompat.setCompoundDrawableTintList(brandColorsText, colorStateList)
                textInputLayout.apply {
                    setBoxStrokeColorStateList(colorStateList)
                    hintTextColor = colorStateList
                }
                if (SDK_INT >= Q) {
                    name.apply {
                        textCursorDrawable?.setTintList(colorStateList)
                        textSelectHandle?.setTintList(colorStateList)
                        textSelectHandleLeft?.setTintList(colorStateList)
                        textSelectHandleRight?.setTintList(colorStateList)
                    }
                }
                if (textInputLayout.isVisible) name.setText(colour.name)
                navBar.apply {
                    previous.apply {
                        setTextColor(previewColor)
                        rippleColor = colorStateList
                    }
                    next.apply {
                        visibility = if (colour.hex.isBlank() || isSystem)
                            View.INVISIBLE else View.VISIBLE
                        backgroundTintList = colorStateList
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    fun Context.getWallpaperColors(): MutableList<Colour> {
        val wallpaperManager = WallpaperManager.getInstance(this)
        val bitmap = if (wallpaperManager.wallpaperInfo == null)
            wallpaperManager.drawable.toBitmap()
        else
            wallpaperManager.wallpaperInfo.loadThumbnail(packageManager).toBitmap()
        val wallColors = WallpaperColors.fromBitmap(bitmap)

        val primary = wallColors.primaryColor.toArgb()
        val secondary = wallColors.secondaryColor?.toArgb()
        val tertiary = wallColors.tertiaryColor?.toArgb()


        val primaryHex = toHex(primary)
        val wallpaperColours = mutableListOf(Colour(primaryHex, getString(R.string.wallpaper_primary)))
        if (secondary != null) {
            val secondaryHex = toHex(secondary)
            wallpaperColours.add(Colour(secondaryHex, getString(R.string.wallpaer_secondary)))
        }
        if (tertiary != null) {
            val tertiaryHex = toHex(tertiary)
            wallpaperColours.add(Colour(tertiaryHex, getString(R.string.wallpaper_tertiary)))
        }

        val palette = Palette.from(bitmap).generate()
        val defaultColor =
            ResourcesCompat.getColor(resources, android.R.color.transparent, null)

        val vibrant = palette.getVibrantColor(defaultColor)
        if (vibrant != defaultColor) wallpaperColours.add(
            Colour(
                toHex(vibrant),
                "Vibrant"
            )
        )

        val darkVibrant = palette.getDarkVibrantColor(defaultColor)
        if (darkVibrant != defaultColor) wallpaperColours.add(
            Colour(
                toHex(
                    darkVibrant
                ), "Dark Vibrant"
            )
        )

        val lightVibrant = palette.getLightVibrantColor(defaultColor)
        if (lightVibrant != defaultColor) wallpaperColours.add(
            Colour(
                toHex(
                    lightVibrant
                ), "Light Vibrant"
            )
        )

        val muted = palette.getMutedColor(defaultColor)
        if (muted != defaultColor) wallpaperColours.add(
            Colour(
                toHex(muted),
                "Muted"
            )
        )

        val darkMuted = palette.getDarkMutedColor(defaultColor)
        if (darkMuted != defaultColor) wallpaperColours.add(
            Colour(
                toHex(darkMuted),
                "Dark Muted"
            )
        )

        val lightMuted = palette.getLightMutedColor(defaultColor)
        if (lightMuted != defaultColor) wallpaperColours.add(
            Colour(
                toHex(lightMuted),
                "Light Muted"
            )
        )
        return wallpaperColours
    }


    fun createAccent(context: Context, accent: Accent): Boolean {
        var created = false
        val appName = accent.pkgName.substringAfter(prefix)
        val filesDir = context.filesDir
        filesDir.listFiles { file ->
            file.delete()
        }
        val aapt = File(filesDir, "aapt")
        val zipalign = File(filesDir, "zipalign")
        val aaptOverlay = File(filesDir, "qacc.apk")
        val signedOverlay = File(filesDir, "signed.apk")
        val alignedOverlay = File(filesDir, "aligned.apk")

        val manifest = File(filesDir, "AndroidManifest.xml")
        val source = File(filesDir, "src")
        val valuesDir = File(source, "values")
        if(valuesDir.exists().not()) valuesDir.mkdirs()
        val colors = File(valuesDir, "colors.xml")

        symLinkBinaries(context.applicationInfo.nativeLibraryDir, aapt, zipalign)
        manifest.createNewFile()
        colors.createNewFile()
        createOverlayManifest(manifest, accent.pkgName, accent.name)
        createColors(colors, accent.colorLight, accent.colorDark, hasNokiaBlue(context))

        if (manifest.exists() && colors.exists()) {
            aapt.setExecutable(true)
            val aaptResult = Shell.su(
                "${aapt.absolutePath} p -f -M ${manifest.absolutePath} -I  /system/framework/framework-res.apk -S ${source.absolutePath} -F ${aaptOverlay.absolutePath}"
            ).exec()
            Log.d("aapt-code", aaptResult.code.toString())
            Log.d("aapt-out", aaptResult.out.toString())

            if (aaptResult.isSuccess  && aaptOverlay.exists()) {

                val certFile = context.assets.open("testkey.x509.pem")
                val keyFile = context.assets.open("testkey.pk8")
                val cert = readCertificate(certFile)
                val key = readPrivateKey(keyFile)
                val jar = JarMap.open(SuFileInputStream(aaptOverlay), true)
                val out = FileOutputStream(signedOverlay.path)
                SignAPK.sign(cert, key, jar, out.buffered())

                if (signedOverlay.exists()) {
                    signedOverlay.setReadable(true)
                    zipalign.setExecutable(true)
                    val zipalignResult = Shell.su(
                        "${zipalign.absolutePath} -f -v -p 4 ${signedOverlay.absolutePath} ${alignedOverlay.absolutePath}"
                    ).exec()
                    Log.d("zipalign-code", zipalignResult.code.toString())
                    Log.d("zipalign-out", zipalignResult.out.toString())


                    if (alignedOverlay.exists() && zipalignResult.isSuccess) {

                        if (SDK_INT >= P) {
                            Shell.su("mkdir -p $overlayPath").exec()
                            Shell.su(context.resources.openRawResource(R.raw.create_module)).exec()
                            val result = Shell.su(
                                "cp -f ${alignedOverlay.absolutePath} $overlayPath/$appName.apk"
                            ).exec()
                            Log.d("inject", result.code.toString())
                            Shell.su("chmod 644 $overlayPath/$appName.apk").exec()

                            if (result.isSuccess) {
                                created = true
                                aaptOverlay.delete()
                                signedOverlay.delete()
                                alignedOverlay.delete()
                            }
                        } else {
                            val result = Shell.su(
                                "chmod 644 ${alignedOverlay.absolutePath}",
                                "pm install -r ${alignedOverlay.absolutePath}"
                            ).exec()
                            Log.d("pm-install-code", result.code.toString())
                            Log.d("pm-install-out", result.out.toString())

                            if (result.isSuccess) {
                                created = true
                                aaptOverlay.delete()
                                signedOverlay.delete()
                                alignedOverlay.delete()
                            }
                        }
                    }
                }
            }
            else {
                Log.e("aapt-e", aaptResult.out.toString())
            }
        }
        return created
    }

    private fun hasNokiaBlue(context: Context): Boolean {
        val packageName = "android"
        val packageManager = context.packageManager
        val resources = packageManager.getResourcesForApplication(packageName)
        val nokiaBlue = resources.getIdentifier(nokiaBlue, "color", packageName)
        Log.d("FIH", nokiaBlue.toString())
        return nokiaBlue != 0
    }

    fun createBackup(context: Context, isAuto: Boolean): Boolean {
        var isCreated = false

        Shell.su("mkdir -p $backupFolder").exec()
        if (SDK_INT >= P) {
            if (Shell.su("[ \"$(ls -A $overlayPath)\" ]").exec().isSuccess)
                isCreated = compress(overlayPath, isAuto)
        }
        else {
            val installedAccents: MutableList<String> = Shell.su(
                "pm list packages -f $prefix | sed s/package://"
            ).exec().out

            if (installedAccents.isNotEmpty()) {
                context.cacheDir.deleteRecursively()
                installedAccents.forEach {
                    val path = it.substringBeforeLast('=')
                    val pkgName = it.substringAfterLast('=')
                    val apkName = pkgName.substringAfter(prefix)
                    Shell.su(
                        "cp -f $path ${context.cacheDir.absolutePath}/$apkName.apk"
                    ).exec()
                }
                isCreated = compress(context.cacheDir.absolutePath, isAuto)
            }
        }
        return isCreated
    }

    private fun compress(path: String, isAuto: Boolean): Boolean {
        var date = Calendar.getInstance().time.toString()
        date = date.replace("\\s".toRegex(), "-")
        val fileName = if (isAuto) "Auto-$date.tar.gz" else "$date.tar.gz"
        val result = Shell.su(
            ".$busyBox tar c -zv -f $backupFolder/$fileName -C $path ."
        ).exec()
        Log.d("compress", result.out.toString())
        return result.isSuccess
    }

    private fun symLinkBinaries(nativeLibraryDir: String, aapt: File, zipalign: File) {
        if (aapt.exists()) aapt.delete()
        if (zipalign.exists()) zipalign.delete()
        val aaptLib = File(nativeLibraryDir, "libaapt.so")
        val zipalignLib = File(nativeLibraryDir, "libzipalign.so")
        Shell.su(
            "ln -sf ${aaptLib.absolutePath} ${aapt.absolutePath}",
            "ln -sf ${zipalignLib.absolutePath} ${zipalign.absolutePath}"
        ).exec()
    }

}
