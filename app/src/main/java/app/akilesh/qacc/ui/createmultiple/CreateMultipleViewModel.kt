package app.akilesh.qacc.ui.createmultiple

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import app.akilesh.qacc.utils.workers.CreateAllWorker
import java.util.*

class CreateMultipleViewModel(application: Application) : AndroidViewModel(application) {

    val workManager = WorkManager.getInstance(application)
    var createAllWorkerId: UUID? = null

    internal fun createAll() {
        val createAllRequest = OneTimeWorkRequestBuilder<CreateAllWorker>()
            .build()
        createAllWorkerId = createAllRequest.id
        workManager.enqueue(createAllRequest)
    }
}
