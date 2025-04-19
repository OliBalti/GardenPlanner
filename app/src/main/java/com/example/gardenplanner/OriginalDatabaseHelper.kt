package com.example.gardenplanner

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream

class OriginalDatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "plants_original.db"
        private const val DATABASE_VERSION = 1
    }

    init {
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
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // No table creation needed
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // No upgrade logic yet
    }

    override fun getReadableDatabase(): SQLiteDatabase {
        return SQLiteDatabase.openDatabase(getDatabasePath(), null, SQLiteDatabase.OPEN_READONLY)
    }
}
