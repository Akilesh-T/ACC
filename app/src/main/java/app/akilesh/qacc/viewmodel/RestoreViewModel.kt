package app.akilesh.qacc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import app.akilesh.qacc.utils.workers.RestoreWorker

class RestoreViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)
    internal val outputWorkInfo: LiveData<List<WorkInfo>>
    private val tag = "restore"
    init {
        outputWorkInfo = workManager.getWorkInfosByTagLiveData(tag)
    }

    internal fun restore(filesList: Array<String>) {
        val builder = Data.Builder()
        builder.putStringArray("files", filesList)
        val restoreRequest = OneTimeWorkRequestBuilder<RestoreWorker>()
            .setInputData(builder.build())
            .addTag(tag)
            .build()
        workManager.enqueue(restoreRequest)
    }
}
