package app.akilesh.qacc.utils.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.*

class RestoreWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        val files = inputData.getStringArray("files")
        val jobs = mutableListOf<Deferred<Int>>()
        try {
            files?.forEach {
                jobs.add(
                    async {
                        val result = Shell.su("pm install -r $it").exec()
                        Log.d("restore-pm",
                            it + '\n' + result.code.toString() + '\n' + result.out.toString())
                    })
            }
            jobs.forEach {
                it.await()
            }
            Result.success()
        }
        catch (throwable: Throwable) {
            Log.e("Restore-Worker", throwable.message, throwable)
            Result.failure()
        }
    }
}
