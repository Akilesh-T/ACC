package app.akilesh.qacc.utils.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.utils.AppUtils.createAccent
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
            val job = async {
                isCreated = createAccent(applicationContext, accent)
                Log.d("isCreated", isCreated.toString())
            }
            job.await()
            if (isCreated) Result.success() else Result.failure()
        }
        catch (throwable: Throwable) {
            Log.e("Create-Worker", throwable.message, throwable)
            Result.failure()
        }
    }

}