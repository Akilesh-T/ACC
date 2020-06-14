package app.akilesh.qacc.utils

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import android.content.res.ColorStateList
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
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.TextViewCompat
import androidx.navigation.navOptions
import androidx.palette.graphics.Palette
import app.akilesh.qacc.Const.Colors.nokiaBlue
import app.akilesh.qacc.Const.Paths.overlayPath
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.ColorPickerFragmentBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.model.Colour
import app.akilesh.qacc.utils.XmlUtils.createColors
import app.akilesh.qacc.utils.XmlUtils.createOverlayManifest
import com.google.android.material.snackbar.Snackbar
import com.topjohnwu.superuser.Shell
import java.io.File


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

    fun toHex(color: Int): String {
        return String.format("#%06X", (0xFFFFFF and color))
    }

    @ColorInt
    fun Context.getColorAccent(): Int {
        @AttrRes val attr = android.R.attr.colorAccent

        val ta = if (SDK_INT == Q) {
            obtainStyledAttributes(android.R.style.ThemeOverlay_DeviceDefault_Accent_DayNight, intArrayOf(attr))
        } else {
            obtainStyledAttributes(android.R.style.Theme_DeviceDefault, intArrayOf(attr))
        }
        @ColorInt val colorAccent = ta.getColor(0, 0)
        ta.recycle()
        return colorAccent
    }

    fun showSnackbar(view: View, text: String) {

        val snackbar = Snackbar.make(
            view,
            text,
            Snackbar.LENGTH_SHORT
        ).setAnchorView(R.id.x_fab)
        if (SDK_INT >= P) {
            snackbar.setAction(view.context.getString(R.string.reboot)) {
                Shell.su("/system/bin/svc power reboot || /system/bin/reboot")
                    .submit()
            }
        }
        snackbar.show()
    }

    val navAnim = navOptions {
        anim  {
            enter  = R.anim.fragment_enter
            exit = R.anim.fragment_exit
            popEnter = R.anim.fragment_enter_pop
            popExit = R.anim.fragment_exit_pop
        }
    }

    fun setPreview(binding: ColorPickerFragmentBinding, accentColor: Int) {

        val accentTintList = ColorStateList.valueOf(accentColor)

        binding.include.apply {
            previewColorQs0Bg.backgroundTintList = accentTintList
            previewColorQs1Bg.backgroundTintList = accentTintList
            previewColorQs2Bg.backgroundTintList = accentTintList

            previewSeekbar.thumbTintList = accentTintList
            previewSeekbar.progressTintList = accentTintList
            previewSeekbar.progressBackgroundTintList = accentTintList

            previewCheckSelected.buttonTintList = accentTintList
            previewRadioSelected.buttonTintList = accentTintList
            previewToggleSelected.buttonTintList = accentTintList
            previewToggleSelected.thumbTintList = accentTintList
            previewToggleSelected.trackTintList = ColorStateList.valueOf(ColorUtils.setAlphaComponent(accentColor, 127))
        }

        binding.apply {
            TextViewCompat.setCompoundDrawableTintList(mdcColorsText, accentTintList)
            TextViewCompat.setCompoundDrawableTintList(customText, accentTintList)
            TextViewCompat.setCompoundDrawableTintList(presetText, accentTintList)
            TextViewCompat.setCompoundDrawableTintList(wallColorsText, accentTintList)
            textInputLayout.setBoxStrokeColorStateList(accentTintList)
            if (SDK_INT >= Q) {
                name.textCursorDrawable?.setTintList(accentTintList)
                name.textSelectHandle?.setTintList(accentTintList)
                name.textSelectHandleLeft?.setTintList(accentTintList)
                name.textSelectHandleRight?.setTintList(accentTintList)
            }
            navBar.previous.setTextColor(accentColor)
            navBar.previous.rippleColor = accentTintList
            navBar.next.backgroundTintList = accentTintList
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

        val aapt = File(filesDir, "aapt")
        val zipalign = File(filesDir, "zipalign")
        val zipSigner = File(filesDir, "zipsigner")
        val zipSignerJar = File(filesDir, "zipsigner-3.0-dexed.jar")
        val aaptOverlay = File(filesDir, "qacc.apk")
        val signedOverlay = File(filesDir, "signed.apk")
        val alignedOverlay = File(filesDir, "aligned.apk")

        val manifest = File(filesDir, "AndroidManifest.xml")
        val source = File(filesDir, "src")
        val colors = File(source, "/values/colors.xml")
        manifest.createNewFile()
        colors.createNewFile()

        createOverlayManifest(manifest, accent.pkgName, accent.name)
        createColors(colors, accent.colorLight, accent.colorDark, hasNokiaBlue(context))

        if (manifest.exists() && colors.exists()) {
            aapt.setExecutable(true)
            val aaptResult = Shell.su(
                "./${aapt.absolutePath} p -f -M ${manifest.absolutePath} -I  /system/framework/framework-res.apk -S ${source.absolutePath} -F ${aaptOverlay.absolutePath}"
            ).exec()
            Log.d("aapt-code", aaptResult.code.toString())
            Log.d("aapt-out", aaptResult.out.toString())

            if (aaptResult.isSuccess  && aaptOverlay.exists()) {

                zipSigner.setExecutable(true)
                zipSignerJar.setReadable(true)
                Shell.su("cd $filesDir").exec()
                val signResult = Shell.su("./${zipSigner.name} ${aaptOverlay.name} ${signedOverlay.name}").exec()
                Log.d("sign-code", signResult.code.toString())
                Log.d("sign-out", signResult.out.toString())
                Shell.su("cd").exec()

                if (signedOverlay.exists()) {
                    signedOverlay.setReadable(true)
                    zipalign.setExecutable(true)
                    val zipalignResult = Shell.su(
                        "./${zipalign.absolutePath} -f -v -p 4 ${signedOverlay.absolutePath} ${alignedOverlay.absolutePath}"
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

}
