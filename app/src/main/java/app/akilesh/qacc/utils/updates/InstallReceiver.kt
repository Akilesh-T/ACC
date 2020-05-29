package app.akilesh.qacc.utils.updates

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log


class InstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        when (val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)) {

            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val activityIntent =
                    intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                context.startActivity(activityIntent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }

            // PackageInstaller.STATUS_SUCCESS -> {}

            else -> {
                val msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                Log.d("AppInstaller", "received $status and $msg")
            }
        }
    }
}
