package app.akilesh.qacc.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import app.akilesh.qacc.model.Accent

@Dao
interface AccentDao {

    @Query("SELECT * from accent_colors ORDER BY name ASC")
    fun getAll(): PagingSource<Int, Accent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(accent: Accent)

    @Delete
    suspend fun delete(accent: Accent)

    @Query("SELECT EXISTS(SELECT package_name FROM accent_colors where package_name = :pkgName)")
    suspend fun exists(pkgName: String): Boolean
}