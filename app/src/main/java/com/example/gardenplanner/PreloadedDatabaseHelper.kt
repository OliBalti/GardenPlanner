package com.example.gardenplanner

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
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
        if (dbPath.exists()) {
            dbPath.delete() // Always delete the old database to ensure updates
        }
        dbPath.parentFile?.mkdirs() // Ensure the parent directory exists
        context.assets.open(DATABASE_NAME).use { inputStream ->
            FileOutputStream(dbPath).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        println("Database copied successfully to: ${dbPath.absolutePath}")
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
}
