package app.akilesh.qacc.ui.info

import android.content.Context
import android.net.Uri
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import app.akilesh.qacc.Const.Links.githubReleases
import app.akilesh.qacc.Const.Links.githubRepo
import app.akilesh.qacc.Const.Links.telegramChannel
import app.akilesh.qacc.Const.Links.telegramGroup
import app.akilesh.qacc.Const.Links.xdaThread
import app.akilesh.qacc.Const.Paths.backupFolder
import app.akilesh.qacc.Const.Paths.updatesFolder
import app.akilesh.qacc.R
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import com.danielstone.materialaboutlibrary.MaterialAboutFragment
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import java.io.File

class InfoFragment: MaterialAboutFragment() {

    override fun getMaterialAboutList(context: Context?): MaterialAboutList {

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
        val tintColor =
            if (useSystemAccent) requireContext().getColorAccent()
            else ResourcesCompat.getColor(requireContext().resources, R.color.colorPrimary, requireContext().theme)
        val icons = listOf(
            ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_outline_info, requireContext().theme),
            ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_github, requireContext().theme),
            ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_outline_group, requireContext().theme),
            ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_xda, requireContext().theme),
            ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_outline_get_app, requireContext().theme),
            ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_round_update, requireContext().theme),
            ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_outline_backup, requireContext().theme)
        )

        val appInfoCard = MaterialAboutCard.Builder()
            .addItem(
                MaterialAboutTitleItem.Builder()
                    .text(requireContext().resources.getString(R.string.app_name))
                    .desc(getString(R.string.about_title_desc))
                    .icon(R.mipmap.ic_launcher)
                    .build()
            )
            .addItem(
                ConvenienceBuilder.createVersionActionItem(context,
                    icons[0],
                    getString(R.string.version),
                    false
                )
            )
            .build()


        val linksCard = MaterialAboutCard.Builder()
            .title(getString(R.string.links))
            .titleColor(tintColor)
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(getString(R.string.github_repo))
                    .icon(icons[1])
                    .setOnClickAction(
                        ConvenienceBuilder.createWebsiteOnClickAction(context, Uri.parse(githubRepo))
                    )
                    .build()
            )
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(getString(R.string.telegram_group))
                    .icon(icons[2])
                    .setOnClickAction(
                        ConvenienceBuilder.createWebsiteOnClickAction(context, Uri.parse(telegramGroup))
                    )
                    .build()
            )
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(getString(R.string.xda_thread))
                    .icon(icons[3])
                    .setOnClickAction(
                        ConvenienceBuilder.createWebsiteOnClickAction(context, Uri.parse(xdaThread))
                    )
                    .build()
            )
            .build()

        val downloadsCard = MaterialAboutCard.Builder()
            .title(getString(R.string.downloads))
            .titleColor(tintColor)
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(getString(R.string.github_releases))
                    .icon(icons[4])
                    .setOnClickAction(
                        ConvenienceBuilder.createWebsiteOnClickAction(context, Uri.parse(githubReleases))
                    )
                    .build()
            )
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(getString(R.string.telegram_channel))
                    .icon(icons[4])
                    .setOnClickAction(
                        ConvenienceBuilder.createWebsiteOnClickAction(context, Uri.parse(telegramChannel))
                    )
                    .build()
            )
            .build()

        val filesCard = MaterialAboutCard.Builder()
            .title(getString(R.string.Files))
            .titleColor(tintColor)
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(getString(R.string.update_folder))
                    .subText(updatesFolder)
                    .icon(icons[5])
                    .build()
            )
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(getString(R.string.backup_folder))
                    .subText(backupFolder.removePrefix(File.separatorChar + "sdcard/"))
                    .icon(icons[6])
                    .build()
            )
            .build()

        return MaterialAboutList.Builder()
            .addCard(appInfoCard)
            .addCard(linksCard)
            .addCard(downloadsCard)
            .addCard(filesCard)
            .build()
    }
}