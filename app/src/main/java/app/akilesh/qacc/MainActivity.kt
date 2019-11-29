package app.akilesh.qacc

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

            R.id.light -> {

                ChromaDialog.Builder()
                    .initialColor(Color.parseColor("#FF2800"))
                    .colorMode(ColorMode.RGB)
                    .onColorSelected(object : ColorSelectListener {
                        override fun onColorSelected(color: Int) {
                            accentLight = String.format("#%06X", 0xFFFFFF and color)
                            binding.lightText.text = accentLight
                            binding.lightText.setTextColor(Color.parseColor(accentLight))
                            f1 = true
                        }
                    })
                    .create()
                    .show(supportFragmentManager, "ChromaDialogLight")

            }

            R.id.dark -> {

                ChromaDialog.Builder()
                    .initialColor(Color.parseColor("#FF2800"))
                    .colorMode(ColorMode.RGB)
                    .onColorSelected(object : ColorSelectListener {
                        override fun onColorSelected(color: Int) {
                            accentDark = String.format("#%06X", 0xFFFFFF and color)
                            binding.darkText.text = accentDark
                            binding.darkText.setTextColor(Color.parseColor(accentDark))
                            f2 = true
                        }
                    })
                    .create()
                    .show(supportFragmentManager, "ChromaDialogDark")

            }

            R.id.button -> {
                accentName = binding.name.text.toString()
                if (f1 && f2 && accentName.isNotBlank()) {

                    if (!Shell.rootAccess())
                        Shell.su("cd /").exec()

                    Shell.su(
                        "cd ${filesDir.absolutePath}",
                        "chmod +x xmlstarlet",
                        "./xmlstarlet ed -L -u '/resources/color[@name=\"accent_device_default_light\"]' -v \"$accentLight\" src/values/colors.xml",
                        "./xmlstarlet ed -L -u '/resources/color[@name=\"accent_device_default_dark\"]' -v \"$accentDark\" src/values/colors.xml",
                        "./xmlstarlet ed -L -u '/resources/string[@name=\"accent_color_custom_overlay\"]' -v \"$accentName\" src/values/strings.xml",
                        "cd /"
                    ).exec()

                    //Toast.makeText(this, "Building overlay apk", Toast.LENGTH_SHORT).show()

                    Shell.su(resources.openRawResource(R.raw.create_overlay)).exec()

                    val certFile = assets.open("testkey.x509.pem")
                    val keyFile = assets.open( "testkey.pk8")
                    val out = FileOutputStream(File(filesDir, "signed.apk").absolutePath)

                    val cert = readCertificate(certFile)
                    val key = readPrivateKey(keyFile)

                    val jar = JarMap.open("$filesDir/qacc.apk")

                    SignAPK.sign(cert, key, jar, out.buffered())

                    Shell.su(resources.openRawResource(R.raw.zipalign)).exec()

                    //Toast.makeText(this, "Creating Magisk module", Toast.LENGTH_SHORT).show()
                    val result = Shell.su(resources.openRawResource(R.raw.create_module)).exec()
                    if (result.isSuccess)
                        filesDir.deleteRecursively()
                        Snackbar.make(binding.root, "$accentName created!", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Reboot") {
                                    Shell.su("/system/bin/svc power reboot || /system/bin/reboot").submit()
                            }
                            .show()

                }

                else {
                    if (!f1) Toast.makeText(this, "Accent color for light theme is not set", Toast.LENGTH_SHORT).show()
                    if (!f2) Toast.makeText(this, "Accent color for dark theme is not set", Toast.LENGTH_SHORT).show()
                    if (accentName.isBlank()) Toast.makeText(this, "Accent color name is not set", Toast.LENGTH_SHORT).show()
                }
            }
        }
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

}


