package app.akilesh.qacc.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import app.akilesh.qacc.model.Accent

@Dao
interface AccentDao {

    @Query("SELECT * from accent_colors ORDER BY name ASC")
    fun getAll(): LiveData<MutableList<Accent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(accent: Accent)

    @Delete
    suspend fun delete(accent: Accent)

}