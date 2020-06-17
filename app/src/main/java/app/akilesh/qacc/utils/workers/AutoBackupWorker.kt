package app.akilesh.qacc.utils.workers

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.akilesh.qacc.Const.Paths.backupFolder
import app.akilesh.qacc.utils.AppUtils.createBackup
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFile
import kotlinx.coroutines.coroutineScope

class AutoBackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val deleteOld = sharedPreferences.getBoolean("delete_old", false)
            if (deleteOld) {
                SuFile(backupFolder).listFiles { file ->
                    if (file.name.startsWith("Auto", true))
                        Shell.su("rm -f ${file.absolutePath}").exec()
                    true
                }
            }
            createBackup(applicationContext, true)
            Result.success()
        } catch (throwable: Throwable) {
            Log.e(AutoBackupWorker::class.java.simpleName, throwable.message, throwable)
            Result.failure()
        }
    }
}
