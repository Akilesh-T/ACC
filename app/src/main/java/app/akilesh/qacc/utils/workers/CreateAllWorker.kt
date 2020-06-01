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
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.Const.selected
import app.akilesh.qacc.R
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.utils.AppUtils.createAccent
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class CreateAllWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager
    private val name = context.getString(R.string.create_all)
    private val notificationId = 47
    private val channelId = CreateAllWorker::class.java.simpleName
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

    override suspend fun doWork(): Result = coroutineScope {
        try {
            val jobs = mutableMapOf<String, Deferred<Int>>()
            selected.forEach { colour ->
                jobs[colour.name] = async(start = CoroutineStart.LAZY) {

                    val pkgName = prefix + "hex_" + colour.hex.removePrefix("#")
                    val accent = Accent(pkgName, colour.name, colour.hex, colour.hex)
                    val isCreated = createAccent(applicationContext, accent)
                    if (isCreated)
                        Log.d(CreateAllWorker::class.java.simpleName, "${colour.name} created")
                    else
                        Log.w(CreateAllWorker::class.java.simpleName, "${colour.name} not created")
                }
            }
            createNotificationChannel()
            val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.ic_round_build)
                .setProgress(100, 50, true)
                .setContentTitle(String.format(title, jobs.keys.first()))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build()

            val foregroundInfo = ForegroundInfo(notificationId, notification)
            setForeground(foregroundInfo)

            jobs.forEach {
                val updatedNotification = NotificationCompat.Builder(applicationContext, channelId)
                    .setSmallIcon(R.drawable.ic_round_build)
                    .setProgress(100, 50, true)
                    .setContentTitle(String.format(title, it.key))
                    .setOngoing(true)
                    .build()
                notificationManager.notify(notificationId, updatedNotification)
                it.value.await()
            }
            Result.success()
        }
        catch (throwable: Throwable) {
            Log.e("CreateAll-Worker", throwable.message, throwable)
            Result.failure()
        }
    }
}
