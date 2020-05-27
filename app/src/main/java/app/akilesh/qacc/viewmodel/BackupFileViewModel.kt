package app.akilesh.qacc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.akilesh.qacc.model.BackupFile

class BackupFileViewModel: ViewModel() {

    val backupFiles: MutableLiveData<MutableList<BackupFile>> by lazy {
        MutableLiveData<MutableList<BackupFile>>()
    }
}