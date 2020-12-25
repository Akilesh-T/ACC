package app.akilesh.qacc.ui.preference

import android.app.Application
import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.work.*
import app.akilesh.qacc.utils.workers.AutoBackupWorker
import app.akilesh.qacc.utils.workers.DailyAccentWorker
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

    fun enableDailyAccentSwitcher() {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        dueDate.set(Calendar.HOUR_OF_DAY, 5)
        dueDate.set(Calendar.MINUTE, 0)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) dueDate.add(Calendar.HOUR_OF_DAY, 24)
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        Log.d("delay", "${TimeUnit.MILLISECONDS.toHours(timeDiff) % 24} hr ${TimeUnit.MILLISECONDS.toMinutes(timeDiff) % 60} min")
        val dailyAccentWorkRequest = PeriodicWorkRequestBuilder<DailyAccentWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "daily_accent",
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyAccentWorkRequest
        )
    }

    fun disableDailyAccentSwitcher() = workManager.cancelUniqueWork("daily_accent")
}
