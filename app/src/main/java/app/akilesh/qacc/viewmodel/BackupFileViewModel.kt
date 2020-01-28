package app.akilesh.qacc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BackupFileViewModel: ViewModel() {

    val backupFiles: MutableLiveData<MutableList<String>> by lazy {
        MutableLiveData<MutableList<String>>()
    }
}