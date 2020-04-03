package app.akilesh.qacc.utils.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.akilesh.qacc.R

object WorkerUtils {

    private const val NOTIFICATION_CHANNEL_NAME = "WorkManager Notifications"
    private const val NOTIFICATION_CHANNEL_DESCRIPTION = "Notification shown when work starts"
    private const val CHANNEL_ID = "Notification"

    fun makeStatusNotification(context: Context, onGoing: Boolean) {

        val title = context.getString(R.string.restoring_accents)
        val name = NOTIFICATION_CHANNEL_NAME
        val description = NOTIFICATION_CHANNEL_DESCRIPTION
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_round_settings_backup_restore)
            .setContentTitle(title)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        if (onGoing) {
            builder.setProgress(100, 50, true)
            NotificationManagerCompat.from(context).notify(1, builder.build())
        }
        else {
            builder.setProgress(100, 100, false)
                .setContentText(context.getString(R.string.accents_restored))
            NotificationManagerCompat.from(context).notify(1, builder.build())
            notificationManager.cancel(1)
        }
    }
}
