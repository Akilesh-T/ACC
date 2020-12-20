package app.akilesh.qacc.ui.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EdgeEffect
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.Const.Links.githubReleases
import app.akilesh.qacc.Const.Links.githubRepo
import app.akilesh.qacc.Const.Links.telegramChannel
import app.akilesh.qacc.Const.Links.telegramGroup
import app.akilesh.qacc.Const.Links.xdaThread
import app.akilesh.qacc.Const.Paths.backupFolder
import app.akilesh.qacc.Const.Paths.updatesFolder
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.InfoFragmentBinding
import app.akilesh.qacc.model.Info.Header
import app.akilesh.qacc.model.Info.InfoItem
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import java.io.File

private lateinit var binding: InfoFragmentBinding

class InfoFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = InfoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
        val systemAccent = requireContext().getColorAccent()
        val accentColor = Pair(useSystemAccent, systemAccent)

        val infoItems = mapOf(
            Header to null,
            InfoItem(getString(R.string.links), R.drawable.ic_round_link, null) to listOf(
                InfoItem(getString(R.string.github_repo), R.drawable.ic_github, githubRepo),
                InfoItem(getString(R.string.telegram_group), R.drawable.ic_telegram_app, telegramGroup),
                InfoItem(getString(R.string.xda_thread), R.drawable.ic_xda, xdaThread)
            ),
            InfoItem(getString(R.string.downloads), R.drawable.ic_downloads, null) to listOf(
                InfoItem(getString(R.string.github_releases), R.drawable.ic_github, githubReleases),
                InfoItem(getString(R.string.telegram_channel), R.drawable.ic_telegram_app, telegramChannel)
            ),
            InfoItem(getString(R.string.Files), R.drawable.ic_twotone_folder, null) to listOf(
                InfoItem(backupFolder.removePrefix(File.separatorChar + "sdcard/"), R.drawable.ic_outline_backup, null),
                InfoItem(updatesFolder, R.drawable.ic_round_update, null)
            )
        )

        binding.aboutItem.apply {
            adapter = InfoAdapter(infoItems, accentColor)
            if (useSystemAccent) {
                edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
                    override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
                        return EdgeEffect(view.context).apply {
                            color = systemAccent
                        }
                    }
                }
            }
        }
    }
}
