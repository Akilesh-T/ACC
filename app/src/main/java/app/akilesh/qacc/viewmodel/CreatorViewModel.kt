package app.akilesh.qacc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.*
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.utils.workers.CreateAllWorker
import app.akilesh.qacc.utils.workers.CreateWorker

class CreatorViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)
    internal val outputWorkInfo: LiveData<List<WorkInfo>>
    val tag: String = "create"
    init {
        outputWorkInfo = workManager.getWorkInfosByTagLiveData(tag)
    }

    internal fun createAll() {
        workManager.enqueue(OneTimeWorkRequest.from(CreateAllWorker::class.java))
    }

    internal fun create(accent: Accent) {
        val data = workDataOf(
            "pkg" to accent.pkgName,
            "name" to accent.name,
            "light" to accent.colorLight,
            "dark" to accent.colorDark
        )
        val createRequest = OneTimeWorkRequestBuilder<CreateWorker>()
            .setInputData(data)
            .addTag(tag)
            .build()
        workManager.enqueue(createRequest)
    }
}
