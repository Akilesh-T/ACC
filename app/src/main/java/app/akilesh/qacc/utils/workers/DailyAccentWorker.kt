package app.akilesh.qacc.utils.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.akilesh.qacc.utils.OverlayUtils.enableAccent
import app.akilesh.qacc.utils.OverlayUtils.getInstalledOverlays
import kotlinx.coroutines.coroutineScope

class DailyAccentWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            val accents = getInstalledOverlays()
            val randomAccent = accents.random()
            Log.d("random-accent", randomAccent)
            enableAccent(randomAccent)
            Result.success()
        } catch (throwable: Throwable) {
            Log.e(DailyAccentWorker::class.java.simpleName, throwable.message, throwable)
            Result.failure()
        }
    }
}
