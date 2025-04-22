package com.example.gardenplanner.data // Ensure package matches

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.util.Log // For logging

/**
 * The Room database class for the application.
 * Contains the plant_definitions table.
 */
// Make sure PlantEntity is listed in entities
// Increment version number if you change the schema OR the asset file content
@Database(entities = [PlantEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Abstract function for Room to provide the DAO implementation
    abstract fun plantDao(): PlantDao

    companion object {
        // Singleton prevents multiple instances of the database opening concurrently.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Name for the database file created in the app's private storage
        private const val DATABASE_NAME = "plants.db"
        // Path WITHIN the assets folder to your pre-populated database file
        private const val ASSET_DB_PATH = "database/plant_database_v1.db" // Verify this path is correct

        private const val TAG = "AppDatabase" // Tag for logging

        fun getDatabase(context: Context): AppDatabase {
            // Return existing instance if available, otherwise create it synchronized
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Use application context
                    AppDatabase::class.java,
                    DATABASE_NAME // The name of the DB file Room will manage
                )
                    // ** Crucial: Copy the database from assets the first time it's created **
                    // Room handles checking if the DB already exists. It only copies from assets
                    // if the database file specified by DATABASE_NAME doesn't exist in the app's
                    // internal storage yet.
                    .createFromAsset(ASSET_DB_PATH)
                    // ** Migration Strategy **
                    // If you update the database schema (change PlantEntity or @Database version),
                    // Room needs to know how to handle existing user data.
                    // Option A (Development): Destroys and recreates the DB. User data is lost.
                    .fallbackToDestructiveMigration()
                    // Option B (Production): Provide Migration objects to preserve data.
                    // .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Define these elsewhere
                    .build()
                Log.i(TAG, "Database instance created or retrieved.")
                INSTANCE = instance
                // Return the instance
                instance
            }
        }
    }
}
