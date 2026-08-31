package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.ScanDao
import com.example.data.local.entity.ScanRecordEntity

@Database(entities = [ScanRecordEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class LensFlowDatabase : RoomDatabase() {

    abstract fun scanDao(): ScanDao

    companion object {
        @Volatile
        private var INSTANCE: LensFlowDatabase? = null

        fun getDatabase(context: Context): LensFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LensFlowDatabase::class.java,
                    "lensflow_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
