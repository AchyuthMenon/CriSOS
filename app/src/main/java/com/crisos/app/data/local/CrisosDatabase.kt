package com.crisos.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [SosMessageEntity::class],
    version = 2,
    exportSchema = false
)
abstract class CrisosDatabase : RoomDatabase() {

    abstract fun sosMessageDao(): SosMessageDao

    companion object {
        @Volatile
        private var INSTANCE: CrisosDatabase? = null

        /**
         * Migration from version 1 to 2:
         * Adds [cloudSyncStatus] and [cloudSyncedAt] columns for MongoDB Atlas remote synchronization.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sos_messages ADD COLUMN cloudSyncStatus TEXT NOT NULL DEFAULT 'PENDING'")
                db.execSQL("ALTER TABLE sos_messages ADD COLUMN cloudSyncedAt INTEGER")
            }
        }

        fun getInstance(context: Context): CrisosDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CrisosDatabase::class.java,
                    "crisos_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
