package app.akilesh.qacc.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.akilesh.qacc.db.dao.AccentDao
import app.akilesh.qacc.model.Accent

@Database(entities = [Accent::class], version = 1, exportSchema = false)
abstract class AccentDatabase: RoomDatabase() {

    abstract fun accentDao(): AccentDao

    companion object {

        @Volatile
        private var INSTANCE: AccentDatabase? = null

        fun getDatabase(context: Context): AccentDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AccentDatabase::class.java,
                    "accent_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}