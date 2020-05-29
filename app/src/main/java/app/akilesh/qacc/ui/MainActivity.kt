package app.akilesh.qacc.ui

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.forEach
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import app.akilesh.qacc.Const.getAssetFiles
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.ActivityMainBinding
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.navAnim
import app.akilesh.qacc.utils.updates.DownloadUtils.download
import app.akilesh.qacc.utils.updates.InstallApkViewModel
import com.afollestad.assent.Permission
import com.afollestad.assent.rationale.createDialogRationale
import com.afollestad.assent.runWithPermissions
import com.github.javiersantos.appupdater.AppUpdaterUtils
import com.github.javiersantos.appupdater.enums.AppUpdaterError
import com.github.javiersantos.appupdater.enums.UpdateFrom.JSON
import com.github.javiersantos.appupdater.objects.Update
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class MainActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appUpdaterUtils: AppUpdaterUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val decorView = window.decorView
        decorView.systemUiVisibility = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                window.navigationBarColor = Color.TRANSPARENT
            }
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
        val color = if (useSystemAccent) getColorAccent()
        else ResourcesCompat.getColor(resources, R.color.colorPrimary, theme)
        setColor(color)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        val navController = navHostFragment.navController


        // Hide bottom app bar & ext. fab while creating an accent
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id) {
                R.id.color_picker, R.id.dark_accent, R.id.customisation, R.id.create_all_fragment -> {
                    binding.bottomAppBar.visibility = View.GONE
                    binding.xFab.visibility = View.GONE
                    when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                        Configuration.UI_MODE_NIGHT_YES -> {
                            window.navigationBarColor = Color.TRANSPARENT
                        }
                    }
                }
                else -> {
                    binding.bottomAppBar.visibility = View.VISIBLE
                    binding.xFab.visibility = View.VISIBLE
                    when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                        Configuration.UI_MODE_NIGHT_YES -> {
                            window.navigationBarColor = Color.parseColor("#1E1E1E")
                        }
                    }
                }
            }
        }

        binding.xFab.setOnClickListener {
            navController.navigate(R.id.color_picker, null, navAnim)
        }

        binding.bottomAppBar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.settings -> navController.navigate(R.id.settings, null, navAnim)
                R.id.info -> navController.navigate(R.id.info, null, navAnim)
            }
            true
        }

        /*
         * Use navigation icon to navigate home.
         * May not be the correct way, but convenient.
         */
        binding.bottomAppBar.setNavigationOnClickListener {
            navController.navigate(R.id.home, null, navAnim)
        }

        copyAssets()

        appUpdaterUtils = AppUpdaterUtils(this)

        appUpdaterUtils
            .setUpdateFrom(JSON)
            .setUpdateJSON("https://raw.githubusercontent.com/Akilesh-T/ACC/master/app/update-changelog.json")
            .withListener(object : AppUpdaterUtils.UpdateListener {

                override fun onSuccess(update: Update?, isUpdateAvailable: Boolean?) {
                    if (isUpdateAvailable!!) {
                        binding.updateCard.visibility = View.VISIBLE
                        val url = "${update!!.urlToDownload}/download/acc-v${update.latestVersion}.apk"
                        binding.update.setOnClickListener {
                            MaterialAlertDialogBuilder(this@MainActivity)
                                .setTitle("What's new in v${update.latestVersion}:")
                                .setMessage(update.releaseNotes)
                                .setPositiveButton(getString(R.string.dl_and_install)) { _, _ ->
                                    val model = ViewModelProvider(this@MainActivity).get(
                                        InstallApkViewModel::class.java)
                                    if (SDK_INT < Q) {
                                        val rationaleHandler = createDialogRationale(R.string.app_name) {
                                            onPermission(
                                                Permission.WRITE_EXTERNAL_STORAGE,
                                                getString(R.string.write_storage_permission_rationale)
                                            )
                                        }
                                        runWithPermissions(Permission.WRITE_EXTERNAL_STORAGE, rationaleHandler = rationaleHandler) {
                                            if (it.isAllGranted()) {
                                                download(this@MainActivity, url, update.latestVersion,  model)
                                                Toast.makeText(this@MainActivity, "Downloading v${update.latestVersion}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                    else {
                                        download(this@MainActivity, url, update.latestVersion,  model)
                                        Toast.makeText(this@MainActivity, "Downloading v${update.latestVersion}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .create()
                                .show()
                        }
                    }
                    else binding.updateCard.visibility = View.GONE
                }

                override fun onFailed(error: AppUpdaterError?) {
                    Log.e("UpdaterError", error.toString())
                }
            }
            )
            .start()
    }

    private fun copyAssets() {
        if( !File(filesDir, "/src/values").exists() )
            File(filesDir, "/src/values").mkdirs()

        val assetFiles = getAssetFiles()
        assetFiles.forEach { file ->
            assets.open(file).use { stream ->
                File(filesDir, file.removeSuffix("64")).outputStream().use { fileOutputStream ->
                    stream.copyTo(fileOutputStream)
                }
            }
        }
    }

    private fun setColor(
        colorAccent: Int
    ) {
        val colorStateList = ColorStateList.valueOf(colorAccent)
        binding.apply {
            updateCard.strokeColor = colorAccent
            update.iconTint = colorStateList
            update.setTextColor(colorAccent)
            xFab.apply {
                strokeColor = colorStateList
                setTextColor(colorAccent)
            }
            bottomAppBar.navigationIcon?.setTintList(colorStateList)
            bottomAppBar.menu.forEach {
                it.iconTintList = colorStateList
            }
        }
    }

    override fun onStart() {
        super.onStart()
        appUpdaterUtils.start()
    }

    override fun onStop() {
        super.onStop()
        appUpdaterUtils.stop()
    }
}
