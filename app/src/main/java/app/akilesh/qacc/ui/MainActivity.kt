package app.akilesh.qacc.ui

import android.content.res.ColorStateList
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.forEach
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.ActivityMainBinding
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.getThemeColor
import app.akilesh.qacc.utils.AppUtils.navAnim
import app.akilesh.qacc.utils.updates.DownloadUtils.download
import app.akilesh.qacc.utils.updates.InstallApkViewModel
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.afollestad.assent.rationale.createDialogRationale
import com.github.javiersantos.appupdater.AppUpdaterUtils
import com.github.javiersantos.appupdater.enums.AppUpdaterError
import com.github.javiersantos.appupdater.enums.UpdateFrom.JSON
import com.github.javiersantos.appupdater.objects.Update
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appUpdaterUtils: AppUpdaterUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)


        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
        val color =  if (useSystemAccent) getColorAccent() else getThemeColor(R.attr.colorPrimary)
        setColor(color)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        val navController = navHostFragment.navController


        // Hide bottom app bar & ext. fab while creating an accent
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id) {
                R.id.home, R.id.info, R.id.settings -> {
                    binding.apply {
                        bottomAppBar.visibility = View.VISIBLE
                        fab.show()
                        bottomAppBar.performShow()
                    }
                }
                else -> {
                    binding.apply {
                        bottomAppBar.performHide()
                        fab.hide()
                        bottomAppBar.visibility = View.GONE
                    }
                }
            }
        }

        binding.fab.setOnClickListener {
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
                            showUpdateDialog(update, url)
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

    private fun showUpdateDialog(
        update: Update,
        url: String
    ) {
        MaterialAlertDialogBuilder(this)
            .setTitle("What's new in v${update.latestVersion}:")
            .setMessage(update.releaseNotes)
            .setPositiveButton(getString(R.string.dl_and_install)) { _, _ ->
                val viewModel: InstallApkViewModel by viewModels()
                var hasPerms = false
                if (SDK_INT < Q) {
                    val rationaleHandler = createDialogRationale(R.string.app_name) {
                        onPermission(
                            Permission.WRITE_EXTERNAL_STORAGE,
                            getString(R.string.write_storage_permission_rationale)
                        )
                    }
                    askForPermissions(
                        Permission.WRITE_EXTERNAL_STORAGE,
                        rationaleHandler = rationaleHandler
                    ) {
                        hasPerms = it.isAllGranted()
                    }
                } else hasPerms = true

                if (hasPerms) {
                    download(this, url, update.latestVersion, viewModel)
                    Toast.makeText(
                        this,
                        "Downloading v${update.latestVersion}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .create()
            .show()
    }

    private fun setColor(
        colorAccent: Int
    ) {
        val colorStateList = ColorStateList.valueOf(colorAccent)
        binding.apply {
            updateCard.strokeColor = colorAccent
            update.iconTint = colorStateList
            update.setTextColor(colorAccent)
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
