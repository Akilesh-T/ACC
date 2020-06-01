package app.akilesh.qacc.utils.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import app.akilesh.qacc.R
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.*

class RestoreWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager
    private val title = context.getString(R.string.restoring_accents)
    private val name = title
    private val notificationId = 48
    private val channelId = RestoreWorker::class.java.simpleName

    private fun createNotificationChannel() {
        if (SDK_INT >= O) {
            var notificationChannel =
                notificationManager.getNotificationChannel(channelId)
            if (notificationChannel == null) {
                notificationChannel = NotificationChannel(
                    channelId, name, NotificationManager.IMPORTANCE_LOW
                )
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }
    }

    override suspend fun doWork(): Result = coroutineScope {
        val files = inputData.getStringArray("files")
        val jobs = mutableListOf<Deferred<Int>>()
        try {
            files?.forEach {
                jobs.add(
                    async(start = CoroutineStart.LAZY) {
                        val result = Shell.su("pm install -r $it").exec()
                        Log.d("restore-pm",
                            it + '\n' + result.code.toString() + '\n' + result.out.toString())
                    })
            }
            createNotificationChannel()
            val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.ic_round_settings_backup_restore)
                .setContentTitle(title)
                .setProgress(100, 50, true)
                .setOngoing(true)
                .build()

            val foregroundInfo = ForegroundInfo(notificationId, notification)
            setForeground(foregroundInfo)
            notificationManager.notify(notificationId, notification)
            jobs.forEach {
                it.await()
            }
            Result.success()
        }
        catch (throwable: Throwable) {
            Log.e(RestoreWorker::class.java.simpleName, throwable.message, throwable)
            Result.failure()
        }
    }
}
