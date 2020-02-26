package app.akilesh.qacc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import app.akilesh.qacc.db.AccentDatabase
import app.akilesh.qacc.db.AccentRepository
import app.akilesh.qacc.model.Accent
import kotlinx.coroutines.launch

class AccentViewModel(application: Application): AndroidViewModel(application) {
    private val repository: AccentRepository
    val allAccents: LiveData<MutableList<Accent>>

    init {
        val accentDao = AccentDatabase.getDatabase(application).accentDao()
        repository = AccentRepository(accentDao)
        allAccents = repository.allAccents
    }

    fun insert(accent: Accent) = viewModelScope.launch {
        repository.insert(accent)
    }

    fun delete(accent: Accent) = viewModelScope.launch {
        repository.delete(accent)
    }

}