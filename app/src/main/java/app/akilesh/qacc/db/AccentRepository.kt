package app.akilesh.qacc.db

import app.akilesh.qacc.db.dao.AccentDao
import app.akilesh.qacc.model.Accent

class AccentRepository(private val accentDao: AccentDao) {

    val allAccents get() = accentDao.getAll()

    suspend fun insert(accent: Accent) {
        accentDao.insert(accent)
    }

    suspend fun delete(accent: Accent) {
        accentDao.delete(accent)
    }

    suspend fun exists(pkgName: String) = accentDao.exists(pkgName)

}
