package app.akilesh.qacc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CustomisationViewModel: ViewModel() {

    val lightAccent: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val darkAccent: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

}