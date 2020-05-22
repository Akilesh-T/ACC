package app.akilesh.qacc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.*
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.utils.workers.CreateAllWorker
import app.akilesh.qacc.utils.workers.CreateWorker
import java.util.*

class CreatorViewModel(application: Application) : AndroidViewModel(application) {

    val workManager = WorkManager.getInstance(application)
    internal var outputWorkInfo: LiveData<List<WorkInfo>>
    private val outputWorkInfoAll: LiveData<List<WorkInfo>>
    var createWorkerId: UUID? = null
    var createAllWorkerId: UUID? = null
    private val tag: String = "create"
    private val createAllTag = "create-all"
    init {
        outputWorkInfo = workManager.getWorkInfosByTagLiveData(tag)
        outputWorkInfoAll = workManager.getWorkInfosByTagLiveData(createAllTag)
    }

    internal fun createAll() {
        val createAllRequest = OneTimeWorkRequestBuilder<CreateAllWorker>()
            .addTag(createAllTag)
            .build()
        createAllWorkerId = createAllRequest.id
        workManager.enqueue(createAllRequest)
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
        createWorkerId = createRequest.id
        workManager.enqueue(createRequest)
    }
}
