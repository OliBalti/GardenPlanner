package com.example.gardenplanner.helpers

import android.content.Context
import android.database.Cursor // Import Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log // Import Log
import java.io.File
import java.io.FileOutputStream
import com.example.gardenplanner.model.Plant // Import the Plant data class

class OriginalDatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "plants_original.db"
        // IMPORTANT: Increment this if you changed the schema in the asset DB
        private const val DATABASE_VERSION = 2 // Example: Incremented from 1
        private const val TAG = "OriginalDbHelper" // Tag for logging
    }

    init {
        // Check and copy the database when the helper is initialized
        // Note: The default SQLiteOpenHelper logic handles version checking.
        // You might need more robust logic here if the default behavior isn't
        // sufficient for replacing the database asset on version change.
        // For simple asset replacement, ensuring the version is incremented
        // might be enough if the default onCreate/onUpgrade triggers a fresh copy
        // or if you add explicit deletion/copy logic in onUpgrade.
        // However, since this DB is read-only, forcing a copy if the version
        // increases is a common pattern. Let's refine copyDatabaseIfNeeded
        // to check the version.

        // Let's call a check method first before calling getReadableDatabase etc.
        checkAndCopyDatabase()
    }

    private fun getDatabasePathString(): String {
        return context.getDatabasePath(DATABASE_NAME).path
    }

    // Renamed and slightly modified logic for clarity
    private fun checkAndCopyDatabase() {
        val dbFile = File(getDatabasePathString())

        if (!dbFile.exists()) {
            // Database does not exist, copy it
            copyDatabaseFromAssets(dbFile)
        } else {
            // Database exists, check version (optional but good practice for asset DBs)
            // This requires opening the existing DB briefly.
            var currentVersion = 0
            try {
                val existingDb = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
                currentVersion = existingDb.version
                existingDb.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error checking existing DB version", e)
                // Could force copy if version check fails
            }


            if (DATABASE_VERSION > currentVersion) {
                Log.i(TAG, "Database version mismatch (new: $DATABASE_VERSION, old: $currentVersion). Replacing database.")
                // Delete existing file and copy the new one
                if (dbFile.delete()) {
                    copyDatabaseFromAssets(dbFile)
                } else {
                    Log.e(TAG, "Failed to delete old database file.")
                    // Handle error - maybe throw exception or notify user
                }
            } else {
                Log.d(TAG, "Database version ($currentVersion) is up to date.")
            }
        }
    }


    private fun copyDatabaseFromAssets(destinationFile: File) {
        Log.i(TAG, "Copying database from assets to ${destinationFile.path}")
        try {
            destinationFile.parentFile?.mkdirs()
            context.assets.open(DATABASE_NAME).use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            // Set the version of the newly copied database
            // (Important if relying on version check)
            val copiedDb = SQLiteDatabase.openDatabase(destinationFile.path, null, SQLiteDatabase.OPEN_READWRITE)
            copiedDb.version = DATABASE_VERSION
            copiedDb.close()
            Log.i(TAG, "Database copied successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error copying database from assets", e)
            // Consider deleting the potentially incomplete file if copy failed
            if (destinationFile.exists()) {
                destinationFile.delete()
            }
            // Re-throw or handle error appropriately
            throw RuntimeException("Error copying database", e)
        }
    }


    // onCreate and onUpgrade are typically used for creating/migrating tables managed
    // directly by the app, not usually for pre-packaged asset databases unless
    // you have complex migration logic beyond simple replacement.
    override fun onCreate(db: SQLiteDatabase?) {
        // Usually left empty when using a pre-populated database asset,
        // unless there are additional tables created by the app itself.
        Log.d(TAG, "onCreate called (should usually be empty for asset DB)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // If you just replace the asset file on version change (handled in init/check),
        // this might also remain empty.
        // If you needed to perform data migration between versions, you'd add logic here.
        Log.w(TAG, "onUpgrade called from version $oldVersion to $newVersion (handling via file replacement)")
        // Example: Force delete and recopy if upgrade is called unexpectedly
        // context.deleteDatabase(DATABASE_NAME)
        // checkAndCopyDatabase()
    }

    // Override getReadableDatabase to ensure the check/copy logic runs if needed,
    // although the init block should typically handle it first.
    override fun getReadableDatabase(): SQLiteDatabase {
        // checkAndCopyDatabase() // Ensure DB exists and is up-to-date before opening
        return try {
            SQLiteDatabase.openDatabase(getDatabasePathString(), null, SQLiteDatabase.OPEN_READONLY)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening readable database. Trying to copy again.", e)
            // Attempt recovery by forcing a copy if opening failed
            val dbFile = File(getDatabasePathString())
            if (dbFile.exists()) dbFile.delete()
            copyDatabaseFromAssets(dbFile)
            // Try opening again
            SQLiteDatabase.openDatabase(getDatabasePathString(), null, SQLiteDatabase.OPEN_READONLY)
        }
    }

    // Added method to get multiple plants by their IDs
    fun getPlantsByIds(ids: List<Int>): List<Plant> {
        if (ids.isEmpty()) return emptyList()

        val plants = mutableListOf<Plant>()
        val db = this.readableDatabase // Use the overridden getter

        // Create a selection string like "id IN (?, ?, ?)"
        val placeholders = ids.joinToString(",") { "?" }
        val selection = "id IN ($placeholders)"
        val selectionArgs = ids.map { it.toString() }.toTypedArray()

        // Define all columns you need based on the Plant data class
        val columns = arrayOf(
            "id", "name", "image", "description", "seedingWindow", "startIndoors",
            "indoorsWindow", "transplantWindow", "directSowWindow", "harvestWindow",
            "start_indoors_days_before_last_frost", "transplant_days_after_last_frost",
            "direct_sow_days_after_last_frost", "harvest_start_days_after_planting",
            "harvest_end_days_after_planting"
        )

        var cursor: Cursor? = null
        try {
            cursor = db.query("Plants", columns, selection, selectionArgs, null, null, "name ASC") // Added ORDER BY name

            // Get column indices once before the loop for efficiency
            val idCol = cursor.getColumnIndexOrThrow("id")
            val nameCol = cursor.getColumnIndexOrThrow("name")
            val imageCol = cursor.getColumnIndex("image") // Allow -1 if column doesn't exist (safer)
            val descCol = cursor.getColumnIndexOrThrow("description")
            val seedWinCol = cursor.getColumnIndexOrThrow("seedingWindow")
            val startIndCol = cursor.getColumnIndexOrThrow("startIndoors")
            val indWinCol = cursor.getColumnIndexOrThrow("indoorsWindow")
            val transWinCol = cursor.getColumnIndexOrThrow("transplantWindow")
            val directSowWinCol = cursor.getColumnIndexOrThrow("directSowWindow")
            val harvestWinCol = cursor.getColumnIndexOrThrow("harvestWindow")
            // Use getColumnIndex to handle potentially missing columns gracefully if schema mismatch occurs
            val startIndDaysCol = cursor.getColumnIndex("start_indoors_days_before_last_frost")
            val transDaysCol = cursor.getColumnIndex("transplant_days_after_last_frost")
            val directSowDaysCol = cursor.getColumnIndex("direct_sow_days_after_last_frost")
            val harvestStartDaysCol = cursor.getColumnIndex("harvest_start_days_after_planting")
            val harvestEndDaysCol = cursor.getColumnIndex("harvest_end_days_after_planting")

            while (cursor.moveToNext()) { // Use while loop which is slightly safer
                val plant = Plant(
                    id = cursor.getInt(idCol),
                    name = cursor.getString(nameCol),
                    // Check index validity and nullity
                    image = if (imageCol != -1 && !cursor.isNull(imageCol)) cursor.getBlob(imageCol) else null,
                    description = cursor.getString(descCol),
                    seedingWindow = cursor.getString(seedWinCol),
                    startIndoors = cursor.getInt(startIndCol) == 1, // Assuming 1 for true, 0 for false in DB
                    indoorsWindow = cursor.getString(indWinCol),
                    transplantWindow = cursor.getString(transWinCol),
                    directSowWindow = cursor.getString(directSowWinCol),
                    harvestWindow = cursor.getString(harvestWinCol),
                    // Safely get nullable Ints, checking index validity first
                    startIndoorsDaysBeforeLastFrost = if (startIndDaysCol != -1 && !cursor.isNull(startIndDaysCol)) cursor.getInt(startIndDaysCol) else null,
                    transplantDaysAfterLastFrost = if (transDaysCol != -1 && !cursor.isNull(transDaysCol)) cursor.getInt(transDaysCol) else null,
                    directSowDaysAfterLastFrost = if (directSowDaysCol != -1 && !cursor.isNull(directSowDaysCol)) cursor.getInt(directSowDaysCol) else null,
                    harvestStartDaysAfterPlanting = if (harvestStartDaysCol != -1 && !cursor.isNull(harvestStartDaysCol)) cursor.getInt(harvestStartDaysCol) else null,
                    harvestEndDaysAfterPlanting = if (harvestEndDaysCol != -1 && !cursor.isNull(harvestEndDaysCol)) cursor.getInt(harvestEndDaysCol) else null
                )
                plants.add(plant)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting plants by IDs from original DB", e)
        } finally {
            cursor?.close()
            // Consider *not* closing the database here if the helper is used frequently.
            // Let the system manage the connection lifecycle.
            // db.close()
        }
        Log.d(TAG, "Fetched ${plants.size} plants for IDs: $ids")
        return plants
    }
}