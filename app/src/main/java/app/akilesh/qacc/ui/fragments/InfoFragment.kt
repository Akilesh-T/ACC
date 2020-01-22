package app.akilesh.qacc.ui.fragments

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import androidx.core.content.res.ResourcesCompat
import app.akilesh.qacc.Const.Links.githubReleases
import app.akilesh.qacc.Const.Links.githubRepo
import app.akilesh.qacc.Const.Links.telegramChannel
import app.akilesh.qacc.Const.Links.telegramGroup
import app.akilesh.qacc.Const.Links.xdaThread
import app.akilesh.qacc.R
import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import com.danielstone.materialaboutlibrary.MaterialAboutFragment
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList

class InfoFragment: MaterialAboutFragment() {

    override fun getTheme(): Int {
        var theme: Int = R.style.AppTheme
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                theme =  R.style.AppTheme_AboutCardLight
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                theme = R.style.AppTheme_AboutCardDark
            }
        }
        return theme
    }

    override fun getMaterialAboutList(context: Context?): MaterialAboutList {

        val appInfoCard = MaterialAboutCard.Builder()
            .addItem(
                MaterialAboutTitleItem.Builder()
                    .text(context!!.resources.getString(R.string.app_name))
                    .desc(getString(R.string.about_title_desc))
                    .icon(R.mipmap.ic_launcher)
                    .build()
            )
            .addItem(
                ConvenienceBuilder.createVersionActionItem(context,
                    ResourcesCompat.getDrawable(context.resources, R.drawable.ic_outline_info, context.theme),
                    getString(R.string.version),
                    false
                    )
            )
            .build()


        val linksCard = MaterialAboutCard.Builder()
            .title(getString(R.string.links))
            .titleColor(ResourcesCompat.getColor(context.resources, R.color.colorPrimary, context.theme))
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(getString(R.string.github))
                    .icon(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_github, context.theme))
                    .setOnClickAction(
                        ConvenienceBuilder.createWebsiteOnClickAction(context, Uri.parse(githubRepo))
                    )
                    .build()
            )
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(getString(R.string.telegram_group))
                    .icon(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_outline_group, context.theme))
                    .setOnClickAction(
                        ConvenienceBuilder.createWebsiteOnClickAction(context, Uri.parse(telegramGroup))
                    )
                    .build()
            )
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(getString(R.string.xda))
                    .icon(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_xda, context.theme))
                    .setOnClickAction(
                        ConvenienceBuilder.createWebsiteOnClickAction(context, Uri.parse(xdaThread))
                    )
                    .build()
            )
            .build()

        val downloadsCard = MaterialAboutCard.Builder()
            .title(getString(R.string.downloads))
            .titleColor(ResourcesCompat.getColor(context.resources, R.color.colorPrimary, context.theme))
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(getString(R.string.github_releases))
                    .icon(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_outline_get_app, context.theme))
                    .setOnClickAction(
                        ConvenienceBuilder.createWebsiteOnClickAction(context, Uri.parse(githubReleases))
                    )
                    .build()
            )
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(getString(R.string.telegram_channel))
                    .icon(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_outline_get_app, context.theme))
                    .setOnClickAction(
                        ConvenienceBuilder.createWebsiteOnClickAction(context, Uri.parse(telegramChannel))
                    )
                    .build()
            )
            .build()


        return MaterialAboutList.Builder()
            .addCard(appInfoCard)
            .addCard(linksCard)
            .addCard(downloadsCard)
            .build()
    }
}