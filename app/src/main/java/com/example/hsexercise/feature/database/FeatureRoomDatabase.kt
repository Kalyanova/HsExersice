package com.example.hsexercise.feature.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FeatureModel::class], version = 1, exportSchema = false)
abstract class FeatureRoomDatabase : RoomDatabase() {

    abstract fun featureTableDao(): FeatureTableDao

    companion object {

        @Volatile
        private var INSTANCE: FeatureRoomDatabase? = null

        fun getDatabase(context: Context): FeatureRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                return Room.databaseBuilder(
                    context.applicationContext,
                    FeatureRoomDatabase::class.java,
                    "headspace_database"
                ).fallbackToDestructiveMigration().build().also {
                    INSTANCE = it
                }
            }
        }
    }
}