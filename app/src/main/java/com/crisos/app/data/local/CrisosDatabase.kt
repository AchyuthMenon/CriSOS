package com.crisos.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SosMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CrisosDatabase : RoomDatabase() {

    abstract fun sosMessageDao(): SosMessageDao

    companion object {
        @Volatile
        private var INSTANCE: CrisosDatabase? = null

        fun getInstance(context: Context): CrisosDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CrisosDatabase::class.java,
                    "crisos_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
