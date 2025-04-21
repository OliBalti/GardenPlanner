package com.example.gardenplanner.helpers

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class PreloadedDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "plants.db" // Name of the preloaded database
        private const val DATABASE_VERSION = 1
    }

    init {
        // Copy the database if it doesn't exist
        copyDatabaseIfNeeded()
    }

    private fun getDatabasePath(): String {
        return context.getDatabasePath(DATABASE_NAME).absolutePath
    }

    private fun copyDatabaseIfNeeded() {
        val dbPath = File(getDatabasePath())
        if (!dbPath.exists()) {
            dbPath.parentFile?.mkdirs()
            context.assets.open(DATABASE_NAME).use { inputStream ->
                FileOutputStream(dbPath).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            println("Database copied successfully to: ${dbPath.absolutePath}")
        } else {
            println("Database already exists, not overwriting.")
        }
    }


    override fun onCreate(db: SQLiteDatabase?) {
        // No need to create tables because the database is preloaded
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades here
    }

    override fun getReadableDatabase(): SQLiteDatabase {
        return SQLiteDatabase.openDatabase(getDatabasePath(), null, SQLiteDatabase.OPEN_READONLY)
    }

    override fun getWritableDatabase(): SQLiteDatabase {
        return SQLiteDatabase.openDatabase(getDatabasePath(), null, SQLiteDatabase.OPEN_READWRITE)
    }

    fun getFavoritePlantIds(): List<Int> {
        val ids = mutableListOf<Int>()
        val db = this.readableDatabase // Reading IDs is fine with readable DB
        // *** Adjust "MyGarden" and "plant_id" if your table/column names differ ***
        val tableName = "MyGarden"     // Table storing favorite plant IDs
        val idColumnName = "plant_id"  // Column name storing the plant ID in MyGarden table

        var cursor: Cursor? = null
        try {
            cursor = db.query(tableName, arrayOf(idColumnName), null, null, null, null, null)
            // Check if cursor is not null and can move to first row
            if (cursor != null && cursor.moveToFirst()) {
                // Check if the column exists before getting index
                val idColumnIndex = cursor.getColumnIndex(idColumnName)
                if (idColumnIndex != -1) { // Check if column exists
                    do {
                        ids.add(cursor.getInt(idColumnIndex))
                    } while (cursor.moveToNext())
                } else {
                    Log.e("PreloadedDbHelper", "Column '$idColumnName' not found in table '$tableName'.")
                }
            }
            Log.d("PreloadedDbHelper", "Found ${ids.size} favorite plant IDs.")
        } catch (e: Exception) {
            Log.e("PreloadedDbHelper", "Error getting favorite plant IDs", e)
        }
        finally {
            cursor?.close() // Ensure cursor is always closed
            // db.close() // Avoid closing DB if helper is potentially reused
        }
        return ids
    }
}
