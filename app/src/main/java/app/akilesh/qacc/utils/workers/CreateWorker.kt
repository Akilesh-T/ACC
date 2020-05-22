package app.akilesh.qacc.utils.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Build.VERSION_CODES.P
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import app.akilesh.qacc.R
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.utils.AppUtils.createAccent
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class CreateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            val pkgName = inputData.getString("pkg")
            val accentName = inputData.getString("name")
            val accentLight = inputData.getString("light")
            val accentDark = inputData.getString("dark")
            val accent = Accent(pkgName!!, accentName!!, accentLight!!, accentDark!!)
            Log.d("accent-r", accent.toString())
            var isCreated = false
            val job = async(start = CoroutineStart.LAZY) {
                isCreated = createAccent(applicationContext, accent)
                Log.d("isCreated", isCreated.toString())
            }
            if (SDK_INT < P) {
                createNotificationChannel()
                val notification = NotificationCompat.Builder(applicationContext, channelId)
                    .setSmallIcon(R.drawable.ic_round_build)
                    .setContentTitle(String.format(title, accentName))
                    .setProgress(100, 50, true)
                    .setOngoing(true)
                    .build()

                val foregroundInfo = ForegroundInfo(notificationId, notification)
                setForeground(foregroundInfo)
                notificationManager.notify(notificationId, notification)
            }
            job.await()
            if (isCreated) Result.success() else Result.failure()
        }
        catch (throwable: Throwable) {
            Log.e("Create-Worker", throwable.message, throwable)
            Result.failure()
        }
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager
    private val name = CreateWorker::class.java.name
    private val notificationId = 49
    private val channelId = "ForegroundWorker"
    private val title = context.getString(R.string.creating)

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
}