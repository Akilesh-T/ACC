package app.akilesh.qacc.db.dao

import androidx.room.*
import app.akilesh.qacc.model.Accent
import kotlinx.coroutines.flow.Flow

@Dao
interface AccentDao {

    @Query("SELECT * from accent_colors ORDER BY name ASC")
    fun getAll(): Flow<MutableList<Accent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(accent: Accent)

    @Delete
    suspend fun delete(accent: Accent)

}