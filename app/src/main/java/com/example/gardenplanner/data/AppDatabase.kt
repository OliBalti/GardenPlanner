package com.example.gardenplanner.data // Ensure package matches

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.util.Log
import java.io.IOException // Import IOException

@Database(entities = [PlantEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun plantDao(): PlantDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val DATABASE_NAME = "plants.db"
        private const val ASSET_DB_PATH = "database/plant_database_v1.db" // Verify this path

        private const val TAG = "AppDatabase" // Tag for logging

        fun getDatabase(context: Context): AppDatabase {
            // Use existing instance if available
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            // If instance is null, create it synchronized
            synchronized(this) {
                // Double-check inside synchronized block
                val existingInstance = INSTANCE
                if (existingInstance != null) {
                    return existingInstance
                }

                Log.i(TAG, "Database instance is null, attempting to build...")
                Log.d(TAG, "Asset path: $ASSET_DB_PATH")
                Log.d(TAG, "Internal DB name: $DATABASE_NAME")

                // *** Add check for asset existence ***
                try {
                    context.applicationContext.assets.open(ASSET_DB_PATH).close()
                    Log.i(TAG, "Asset file '$ASSET_DB_PATH' found successfully.")
                } catch (ioException: IOException) {
                    Log.e(TAG, "!!! Asset file '$ASSET_DB_PATH' not found or cannot be opened !!!", ioException)
                    // Optionally throw a more specific error here if needed
                    // throw RuntimeException("Database asset not found at $ASSET_DB_PATH", ioException)
                }
                // *** End asset check ***

                Log.d(TAG, "Calling Room.databaseBuilder...")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .createFromAsset(ASSET_DB_PATH)
                    .fallbackToDestructiveMigration()
                    // Add logging callback for more insight (optional but helpful)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.i(TAG, "RoomDatabase.Callback: onCreate called (DB was created, asset likely copied). DB path: ${db.path}")
                        }
                        override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Log.i(TAG, "RoomDatabase.Callback: onOpen called (Existing DB opened). DB path: ${db.path}, Version: ${db.version}")
                        }
                    })
                    .build()
                Log.i(TAG, "Room.databaseBuilder finished.")

                INSTANCE = instance
                return instance
            }
        }
    }
}
    