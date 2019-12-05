package app.akilesh.qacc

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import app.akilesh.qacc.databinding.ActivityMainBinding
import app.akilesh.qacc.signing.ByteArrayStream
import app.akilesh.qacc.signing.JarMap
import app.akilesh.qacc.signing.SignAPK
import com.afollestad.assent.Permission
import com.afollestad.assent.rationale.createDialogRationale
import com.afollestad.assent.runWithPermissions
import com.google.android.material.snackbar.Snackbar
import com.topjohnwu.superuser.Shell
import me.priyesh.chroma.ChromaDialog
import me.priyesh.chroma.ColorMode
import me.priyesh.chroma.ColorSelectListener
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import java.io.*
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec


class MainActivity : AppCompatActivity(), View.OnClickListener {

    init {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR)
        Shell.Config.verboseLogging(true)
        Shell.Config.setTimeout(10)
    }

    private val assetFiles = listOf(
        "aapt",
        "AndroidManifest.xml",
        "src/values/colors.xml",
        "src/values/strings.xml",
        "xmlstarlet",
        "zipalign"
    )

    private lateinit var binding: ActivityMainBinding
    private var accentLight = ""
    private var accentDark = ""
    private var accentName = ""
    private var f1 = false
    private var f2 = false
    private var primaryHex = "#FF2800"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val decorView = window.decorView
        decorView.systemUiVisibility = FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS

        when(resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                val colorWhite = ContextCompat.getColor(this, R.color.white)
                window.navigationBarColor = colorWhite
                window.statusBarColor = colorWhite
                decorView.systemUiVisibility = SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                val colorDefaultDark = ContextCompat.getColor(this, R.color.colorDefaultDark)
                window.statusBarColor = colorDefaultDark
                window.navigationBarColor = colorDefaultDark
            }
        }

        assetFiles.forEach {
            if(!File(filesDir, it).exists())
            copyFile(it)
        }
        binding.light.setOnClickListener(this)
        binding.dark.setOnClickListener(this)
        binding.button.setOnClickListener(this)

        val rationaleHandler = createDialogRationale(R.string.app_name_full) {
            onPermission(Permission.READ_EXTERNAL_STORAGE, "Storage permission is required to get wallpaper colours.")
        }

        runWithPermissions(Permission.READ_EXTERNAL_STORAGE, rationaleHandler = rationaleHandler) {
            if (it.isAllGranted()) {
                val wallpaperManager = WallpaperManager.getInstance(this)
                val wallDrawable = wallpaperManager.drawable
                val wallColors = WallpaperColors.fromDrawable(wallDrawable)

                val primary = wallColors.primaryColor.toArgb()
                primaryHex = toHex(primary)
                val secondary = wallColors.secondaryColor?.toArgb()
                val secondaryHex = secondary?.let { it1 -> toHex(it1) }
                val tertiary = wallColors.tertiaryColor?.toArgb()
                val tertiaryHex = tertiary?.let { it1 -> toHex(it1) }

                binding.wallFrame.visibility = View.VISIBLE
                binding.wallColorPrimary.text =
                    String.format(resources.getString(R.string.color_wallpaper_primary), primaryHex)
                binding.wallColorSecondary.text = String.format(
                    resources.getString(R.string.color_wallpaper_secondary),
                    secondaryHex
                )
                binding.wallColorTertiary.text = String.format(
                    resources.getString(R.string.color_wallpaper_tertiary),
                    tertiaryHex
                )

                binding.previewPrimary.setColorFilter(primary)
                if (secondary != null)
                    binding.previewSecondary.setColorFilter(secondary)

                if (tertiary != null)
                    binding.previewTertiary.setColorFilter(tertiary)

            }
        }

        if (isOverlayInstalled()) {
            val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
            val currentAccent = sharedPref.getString(getString(R.string.accent_name), "")
            if (currentAccent != "") {
                binding.current.visibility = View.VISIBLE
                val currentAccentLight = sharedPref.getString(getString(R.string.accent_light), "")
                val currentAccentDark = sharedPref.getString(getString(R.string.accent_dark), "")
                binding.currentAccent.text =
                    String.format(resources.getString(R.string.current_accent_name), currentAccent)
                binding.currentAccent.append("\n")
                binding.currentAccent.append(
                    String.format(
                        resources.getString(R.string.current_accent_light),
                        currentAccentLight
                    )
                )
                binding.currentAccent.append("\n")
                binding.currentAccent.append(
                    String.format(
                        resources.getString(R.string.current_accent_dark),
                        currentAccentDark
                    )
                )
                binding.currentAccent.append("\n")
                binding.currentAccent.append(String.format(resources.getString(R.string.enable)))
                binding.enableAccent.setOnClickListener {
                    Shell.su("cmd overlay enable com.android.theme.color.custom").exec()
                }
            }
        }
    }

    private fun copyFile(filename: String) {
        if( !File("$filesDir/src/values").exists() )
            File("$filesDir/src/values").mkdirs()
        assets.open(filename).use { stream ->
            File("${filesDir}/$filename").outputStream().use {
                stream.copyTo(it)
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){

            binding.light.id -> {

                ChromaDialog.Builder()
                    .initialColor(Color.parseColor(primaryHex))
                    .colorMode(ColorMode.RGB)
                    .onColorSelected(object : ColorSelectListener {
                        override fun onColorSelected(color: Int) {
                            accentLight = toHex(color)
                            binding.lightText.text = accentLight
                            binding.previewLight.setColorFilter(color)
                            f1 = true
                        }
                    })
                    .create()
                    .show(supportFragmentManager, "ChromaDialogLight")

            }

            binding.dark.id -> {

                ChromaDialog.Builder()
                    .initialColor(Color.parseColor(primaryHex))
                    .colorMode(ColorMode.RGB)
                    .onColorSelected(object : ColorSelectListener {
                        override fun onColorSelected(color: Int) {
                            accentDark = toHex(color)
                            binding.darkText.text = accentDark
                            binding.previewDark.setColorFilter(color)
                            f2 = true
                        }
                    })
                    .create()
                    .show(supportFragmentManager, "ChromaDialogDark")

            }

            binding.button.id -> {
                accentName = binding.name.text.toString()
                if (f1 && f2 && accentName.isNotBlank()) {

                    if (!Shell.rootAccess())
                        Shell.su("cd /").exec()

                    val xmlRes = Shell.su(
                        "cd ${filesDir.absolutePath}",
                        "chmod +x xmlstarlet",
                        "./xmlstarlet ed -L -u '/resources/color[@name=\"accent_device_default_light\"]' -v \"$accentLight\" src/values/colors.xml",
                        "./xmlstarlet ed -L -u '/resources/color[@name=\"accent_device_default_dark\"]' -v \"$accentDark\" src/values/colors.xml",
                        "./xmlstarlet ed -L -u '/resources/string[@name=\"accent_color_custom_overlay\"]' -v \"$accentName\" src/values/strings.xml",
                        "cd /"
                    ).exec()

                    if (!xmlRes.isSuccess)
                        Toast.makeText(
                            this,
                            "Error: couldn't set new values",
                            Toast.LENGTH_SHORT
                        ).show()
                    else {
                        //Toast.makeText(this, "Building overlay apk", Toast.LENGTH_SHORT).show()

                        val ovrRes = Shell.su(resources.openRawResource(R.raw.create_overlay)).exec()
                        if (!ovrRes.isSuccess)
                            Toast.makeText(
                                this,
                                "Error: couldn't create overlay apk",
                                Toast.LENGTH_SHORT
                            ).show()
                        else {
                            val certFile = assets.open("testkey.x509.pem")
                            val keyFile = assets.open("testkey.pk8")
                            val out = FileOutputStream(File(filesDir, "signed.apk").absolutePath)

                            val cert = readCertificate(certFile)
                            val key = readPrivateKey(keyFile)

                            val jar = JarMap.open("$filesDir/qacc.apk")

                            SignAPK.sign(cert, key, jar, out.buffered())

                            val zipalignRes = Shell.su(resources.openRawResource(R.raw.zipalign)).exec()

                            if (!zipalignRes.isSuccess)
                                Toast.makeText(
                                    this,
                                    "Error: couldn't zip align apk",
                                    Toast.LENGTH_SHORT
                                ).show()
                            else {
                                //Toast.makeText(this, "Creating Magisk module", Toast.LENGTH_SHORT).show()
                                val result =
                                    Shell.su(resources.openRawResource(R.raw.create_module)).exec()
                                if (result.isSuccess) {
                                    filesDir.deleteRecursively()

                                    val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
                                    with (sharedPref.edit()) {
                                        putString(getString(R.string.accent_name), accentName)
                                        putString(getString(R.string.accent_light), accentLight)
                                        putString(getString(R.string.accent_dark), accentDark)
                                        commit()
                                    }

                                    Snackbar.make(
                                        binding.root,
                                        "$accentName created!",
                                        Snackbar.LENGTH_INDEFINITE
                                    )
                                        .setAction("Reboot") {
                                            Shell.su("/system/bin/svc power reboot || /system/bin/reboot")
                                                .submit()
                                        }
                                        .show()
                                }
                                else
                                    Toast.makeText(
                                        this,
                                        "Error: couldn't create Magisk module",
                                        Toast.LENGTH_SHORT
                                    ).show()
                            }
                        }

                    }
                }

                else {
                    if (!f1) Toast.makeText(this, "Accent color for light theme is not set", Toast.LENGTH_SHORT).show()
                    if (!f2) Toast.makeText(this, "Accent color for dark theme is not set", Toast.LENGTH_SHORT).show()
                    if (accentName.isBlank()) Toast.makeText(this, "Accent color name is not set", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun toHex(color: Int): String {
        return String.format("#%06X", (0xFFFFFF and color))
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    fun readCertificate(input: InputStream): X509Certificate {
        try {
            val cf = CertificateFactory.getInstance("X.509")
            return cf.generateCertificate(input) as X509Certificate
        } finally {
            input.close()
        }
    }


    @Throws(IOException::class, GeneralSecurityException::class)
    fun readPrivateKey(input: InputStream): PrivateKey {
        try {
            val buf = ByteArrayStream()
            buf.readFrom(input)
            val bytes = buf.toByteArray()
            /* Check to see if this is in an EncryptedPrivateKeyInfo structure. */
            val spec = PKCS8EncodedKeySpec(bytes)
            /*
             * Now it's in a PKCS#8 PrivateKeyInfo structure. Read its Algorithm
             * OID and use that to construct a KeyFactory.
             */
            val bIn = ASN1InputStream(ByteArrayInputStream(spec.encoded))
            val pki = PrivateKeyInfo.getInstance(bIn.readObject())
            val algOid = pki.privateKeyAlgorithm.algorithm.id
            return KeyFactory.getInstance(algOid).generatePrivate(spec)
        } finally {
            input.close()
        }
    }

    private fun isOverlayInstalled(): Boolean {
        return try {
            packageManager.getPackageInfo("com.android.theme.color.custom", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

}



