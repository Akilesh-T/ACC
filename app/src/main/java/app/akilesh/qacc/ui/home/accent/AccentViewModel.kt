package app.akilesh.qacc.ui.home.accent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import app.akilesh.qacc.db.AccentDatabase
import app.akilesh.qacc.db.AccentRepository
import app.akilesh.qacc.model.Accent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccentViewModel(application: Application): AndroidViewModel(application) {
    private val repository: AccentRepository

    init {
        val accentDao = AccentDatabase.getDatabase(application).accentDao()
        repository = AccentRepository(accentDao)
    }

    val allAccents= Pager(
        PagingConfig(
            pageSize = 8,
            enablePlaceholders = true,
            maxSize = 50
        )
    ) {
        repository.allAccents
    }.flow
        .cachedIn(viewModelScope)

    fun insert(accent: Accent) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(accent)
    }

    fun delete(accent: Accent) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(accent)
    }

    suspend fun accentExists(pkgName: String) = repository.exists(pkgName)

}