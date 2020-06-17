package app.akilesh.qacc.ui.preferences

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.work.*
import app.akilesh.qacc.utils.workers.AutoBackupWorker
import java.util.concurrent.TimeUnit

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    val workManager = WorkManager.getInstance(application)
    private val storageConstraint = Constraints.Builder()
        .setRequiresStorageNotLow(true)
        .build()

    fun enableAutoBackup(interval: Long) {
        Log.d("Interval", interval.toString())
        val periodicBackupWork = PeriodicWorkRequestBuilder<AutoBackupWorker>(interval, TimeUnit.DAYS)
            .setInitialDelay(interval, TimeUnit.DAYS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, PeriodicWorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS, TimeUnit.MILLISECONDS)
            .setConstraints(storageConstraint)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "auto_backup",
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicBackupWork
        )
    }

    fun disableAutoBackup() = workManager.cancelUniqueWork("auto_backup")
}
