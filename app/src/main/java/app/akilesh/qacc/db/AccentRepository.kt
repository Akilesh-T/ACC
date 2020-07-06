package app.akilesh.qacc.db

import app.akilesh.qacc.db.dao.AccentDao
import app.akilesh.qacc.model.Accent
import kotlinx.coroutines.flow.Flow

class AccentRepository(private val accentDao: AccentDao) {

    val allAccents: Flow<MutableList<Accent>> = accentDao.getAll()

    suspend fun insert(accent: Accent) {
        accentDao.insert(accent)
    }

    suspend fun delete(accent: Accent) {
        accentDao.delete(accent)
    }

}
