package app.akilesh.qacc.utils

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.os.Build.VERSION_CODES.Q
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.ColorUtils
import androidx.navigation.navOptions
import app.akilesh.qacc.Const.Paths.overlayPath
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.ColorPickerFragmentBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.signing.ByteArrayStream
import app.akilesh.qacc.signing.JarMap
import app.akilesh.qacc.signing.SignAPK
import app.akilesh.qacc.utils.XmlUtils.createColors
import app.akilesh.qacc.utils.XmlUtils.createOverlayManifest
import app.akilesh.qacc.viewmodel.AccentViewModel
import com.google.android.material.snackbar.Snackbar
import com.topjohnwu.superuser.Shell
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import java.io.*
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec


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

        Snackbar.make(
            view,
            text,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAnchorView(R.id.x_fab)
            .setAction("Reboot") {
                Shell.su("/system/bin/svc power reboot || /system/bin/reboot")
                    .submit()
            }
            .show()
    }

    val navAnim = navOptions {
        anim  {
            enter  = R.anim.fragment_enter
            exit = R.anim.fragment_exit
            popEnter = R.anim.fragment_enter_pop
            popExit = R.anim.fragment_exit_pop
        }
    }

    val installedAccents: MutableList<String> = Shell.su(
        "pm list packages -f $prefix | sed s/package://"
    ).exec().out

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
            previewToggleSelected.trackTintList = ColorStateList.valueOf(ColorUtils.setAlphaComponent(accentColor, 51))
        }

        binding.apply {
            brandColorsText.compoundDrawableTintList = accentTintList
            customText.compoundDrawableTintList = accentTintList
            presetText.compoundDrawableTintList = accentTintList
            wallColorsText.compoundDrawableTintList = accentTintList
            textInputLayout.setBoxStrokeColorStateList(accentTintList)
            if (SDK_INT >= Q) {
                name.textCursorDrawable?.setTintList(accentTintList)
                name.textSelectHandle?.setTintList(accentTintList)
                name.textSelectHandleLeft?.setTintList(accentTintList)
                name.textSelectHandleRight?.setTintList(accentTintList)
            }
            buttonPrevious.setTextColor(accentColor)
            buttonPrevious.rippleColor = accentTintList
            buttonNext.backgroundTintList = accentTintList
        }
    }

    fun createAccent(context: Context, accentViewModel: AccentViewModel, accent: Accent): Boolean {
        var created = false
        val appName = accent.pkgName.substringAfter(prefix)
        val filesDir = context.filesDir

        val manifest = File(filesDir, "AndroidManifest.xml")
        val values = File(filesDir, "/src/values")
        val colors = File(values, "colors.xml")
        manifest.createNewFile()
        colors.createNewFile()

        createOverlayManifest(manifest, accent.pkgName, accent.name)
        createColors(colors, accent.colorLight, accent.colorDark)

        if (manifest.exists() && colors.exists()) {
            Shell.su("cd ${filesDir.absolutePath}").exec()
            val aaptResult = Shell.su(
                "chmod +x aapt",
                "./aapt p -f -M AndroidManifest.xml -I  /system/framework/framework-res.apk -S src -F qacc.apk"
            ).exec()
            Log.d("aapt", aaptResult.code.toString())

            if (aaptResult.isSuccess && File("$filesDir/qacc.apk").exists()) {
                val certFile = context.assets.open("testkey.x509.pem")
                val keyFile = context.assets.open("testkey.pk8")
                val out = FileOutputStream(File("$filesDir/signed.apk").path)

                val cert = readCertificate(certFile)
                val key = readPrivateKey(keyFile)

                val jar = JarMap.open(File(filesDir, "qacc.apk").path)

                SignAPK.sign(cert, key, jar, out.buffered())

                if (File(filesDir, "signed.apk").exists())
                    Shell.su(context.resources.openRawResource(R.raw.zipalign)).exec()
                else
                    Toast.makeText(context, "Signed overlay not created", Toast.LENGTH_SHORT).show()

                if (File(filesDir, "aligned.apk").exists()) {

                    if (SDK_INT >= P) {
                        Shell.su("mkdir -p $overlayPath").exec()
                        Shell.su(context.resources.openRawResource(R.raw.create_module)).exec()
                        val result = Shell.su(
                            "cp -f $filesDir/aligned.apk $overlayPath/$appName.apk"
                        ).exec()
                        Log.d("inject", result.code.toString())
                        Shell.su("chmod 644 $overlayPath/$appName.apk").exec()

                        if (result.isSuccess) {
                            created = true
                            val createdApks = listOf("qacc.apk", "signed.apk", "aligned.apk")
                            createdApks.forEach {
                                File(filesDir, it).delete()
                            }
                            accentViewModel.insert(accent)
                        }
                    }
                    else {
                        val result = Shell.su(
                            "chmod 644 $filesDir/aligned.apk",
                            "pm install -r $filesDir/aligned.apk"
                        ).exec()
                        Log.d("pm-install", result.code.toString())

                        if (result.isSuccess) {
                            created = true
                            val createdApks = listOf("qacc.apk", "signed.apk", "aligned.apk")
                            createdApks.forEach {
                                File(filesDir, it).delete()
                            }
                            accentViewModel.insert(accent)
                        }
                        else Log.e("pm-install", result.err.toString())
                    }
                }
            }
            else {
                Toast.makeText(context, context.getString(R.string.aapt_error), Toast.LENGTH_SHORT).show()
                Log.e("aapt-e", aaptResult.err.toString())
            }
        }
        return created
    }


    @Throws(IOException::class, GeneralSecurityException::class)
    fun readCertificate(inputStream: InputStream): X509Certificate {
        inputStream.use { stream ->
            val cf = CertificateFactory.getInstance("X.509")
            return cf.generateCertificate(stream) as X509Certificate
        }
    }


    @Throws(IOException::class, GeneralSecurityException::class)
    fun readPrivateKey(inputStream: InputStream): PrivateKey {
        inputStream.use { stream ->
            val buf = ByteArrayStream()
            buf.readFrom(stream)
            val bytes = buf.toByteArray()
            // Check to see if this is in an EncryptedPrivateKeyInfo structure.
            val spec = PKCS8EncodedKeySpec(bytes)
            /*
             * Now it's in a PKCS#8 PrivateKeyInfo structure. Read its Algorithm
             * OID and use that to construct a KeyFactory.
             */
            val bIn = ASN1InputStream(ByteArrayInputStream(spec.encoded))
            val pki = PrivateKeyInfo.getInstance(bIn.readObject())
            val algOid = pki.privateKeyAlgorithm.algorithm.id
            return KeyFactory.getInstance(algOid).generatePrivate(spec)
        }
    }

}
