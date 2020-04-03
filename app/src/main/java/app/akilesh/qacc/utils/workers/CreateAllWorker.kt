package app.akilesh.qacc.utils.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.Const.selected
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.utils.AppUtils.createAccent
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class CreateAllWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            val jobs = mutableListOf<Deferred<Int>>()
            selected.forEach { colour ->
                jobs.add(async(start = CoroutineStart.LAZY) {

                    val pkgName = prefix + "hex_" + colour.hex.removePrefix("#")
                    val accent = Accent(pkgName, colour.name, colour.hex, colour.hex)
                    val isCreated = createAccent(applicationContext, accent)
                    if (isCreated)
                        Log.d("CreateAll-Worker", "${colour.name} created")
                    else
                        Log.w("CreateAll-Worker", "${colour.name} not created")
                })
            }
            jobs.forEach {
                it.await()
            }
            Result.success()
        }
        catch (throwable: Throwable) {
            Log.e("CreateAll-Worker", throwable.message, throwable)
            Result.failure()
        }
    }
}
