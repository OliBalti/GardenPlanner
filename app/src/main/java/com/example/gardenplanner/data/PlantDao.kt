package com.example.gardenplanner.data // Ensure package matches

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the plant_definitions table.
 */
@Dao
interface PlantDao {

    /**
     * Gets all plants marked as favorite.
     * Assumes 'is_favorite' column stores 1 for true, 0 for false.
     * Orders results by plant name alphabetically.
     * @return A Flow emitting the list of favorite PlantEntity objects.
     */
    @Query("SELECT * FROM plant_definitions WHERE is_favorite = 1 ORDER BY name ASC")
    fun getFavoritePlants(): Flow<List<PlantEntity>>

    /**
     * Gets all plants from the table.
     * Orders results by plant name alphabetically.
     * @return A Flow emitting the list of all PlantEntity objects.
     */
    @Query("SELECT * FROM plant_definitions ORDER BY name ASC")
    fun getAllPlants(): Flow<List<PlantEntity>>

    /**
     * Gets a single plant by its unique ID.
     * @param plantId The ID of the plant to retrieve.
     * @return The PlantEntity object, or null if not found. Runs asynchronously.
     */
    @Query("SELECT * FROM plant_definitions WHERE id = :plantId")
    suspend fun getPlantById(plantId: Int): PlantEntity?

    /**
     * Updates an existing plant in the database.
     * Useful for toggling the 'isFavorite' status.
     * @param plant The PlantEntity object with updated values. Runs asynchronously.
     */
    @Update
    suspend fun updatePlant(plant: PlantEntity)

    /**
     * Inserts a list of plants into the database. If a plant with the same primary key
     * already exists, it will be replaced.
     * Useful for initial database population if not using createFromAsset.
     * @param plants The list of PlantEntity objects to insert. Runs asynchronously.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plants: List<PlantEntity>)

    // Add @Delete method if you need to remove plants
    // @Delete
    // suspend fun deletePlant(plant: PlantEntity)
}
