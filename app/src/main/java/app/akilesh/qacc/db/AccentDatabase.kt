package app.akilesh.qacc.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import app.akilesh.qacc.db.dao.AccentDao
import app.akilesh.qacc.model.Accent

@Database(entities = [Accent::class], version = 2, exportSchema = false)
abstract class AccentDatabase: RoomDatabase() {

    abstract fun accentDao(): AccentDao

    companion object {

        @Volatile
        private var INSTANCE: AccentDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE accent_colors ADD COLUMN color_dark TEXT NOT NULL DEFAULT ''")
            }
        }

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
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}