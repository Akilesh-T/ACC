package app.akilesh.qacc.ui.info

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import app.akilesh.qacc.Const.Links.githubReleases
import app.akilesh.qacc.Const.Links.githubRepo
import app.akilesh.qacc.Const.Links.telegramChannel
import app.akilesh.qacc.Const.Links.telegramGroup
import app.akilesh.qacc.Const.Links.xdaThread
import app.akilesh.qacc.Const.Paths.backupFolder
import app.akilesh.qacc.Const.Paths.updatesFolder
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.InfoFragmentBinding
import app.akilesh.qacc.model.Info
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import java.io.File

private lateinit var binding: InfoFragmentBinding

class InfoFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = InfoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
        val systemAccent = requireContext().getColorAccent()
        val appInfoIntent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
            .addCategory(Intent.CATEGORY_DEFAULT)
            .setData(Uri.parse("package:${requireContext().packageName}"))
        val versionName = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName

        binding.appInfo.appVersion.text = getString(R.string.version, versionName)
        val infoItems = mapOf(
            Info(getString(R.string.links), R.drawable.ic_round_link, null) to listOf(
                Info(getString(R.string.github_repo), R.drawable.ic_github, githubRepo),
                Info(getString(R.string.telegram_group), R.drawable.ic_telegram_app, telegramGroup),
                Info(getString(R.string.xda_thread), R.drawable.ic_xda, xdaThread)
            ),
            Info(getString(R.string.downloads), R.drawable.ic_downloads, null) to listOf(
                Info(getString(R.string.github_releases), R.drawable.ic_github, githubReleases),
                Info(getString(R.string.telegram_channel), R.drawable.ic_telegram_app, telegramChannel)
            ),
            Info(getString(R.string.Files), R.drawable.ic_twotone_folder, null) to listOf(
                Info(backupFolder.removePrefix(File.separatorChar + "sdcard/"), R.drawable.ic_outline_backup, null),
                Info(updatesFolder, R.drawable.ic_round_update, null)
            )
        )
        binding.aboutItem.adapter = InfoAdapter(infoItems, useSystemAccent, systemAccent)
        binding.appInfo.root.setOnClickListener {
            startActivity(appInfoIntent)
        }
    }
}
