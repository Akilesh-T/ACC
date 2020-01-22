package app.akilesh.qacc.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import app.akilesh.qacc.R
import app.akilesh.qacc.viewmodel.InstallApkViewModel
import java.io.File

object DownloadUtils {

    fun download(
        context: Context,
        url: String,
        version: String,
        model: InstallApkViewModel
    ) {

        val downloadsFolder = Environment.DIRECTORY_DOWNLOADS
        val subPath = "${context.getString(R.string.app_name)} updates/acc-v$version.apk"
        val file = File(downloadsFolder, subPath)

        val onComplete = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when(intent?.action) {
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                        if (file.exists()) {
                            model.install(Uri.fromFile(file))
                        }
                    }
                }
            }
        }

        val downloadManager: DownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        downloadManager.enqueue(
            DownloadManager.Request(Uri.parse(url))
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                .setTitle(context.resources.getString(R.string.app_name))
                .setDescription("Downloading v$version")
                .setDestinationInExternalPublicDir(downloadsFolder, subPath)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        )
    }
}

