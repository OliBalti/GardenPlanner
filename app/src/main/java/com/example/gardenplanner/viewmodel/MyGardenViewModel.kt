package com.example.gardenplanner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenplanner.data.AppDatabase
import com.example.gardenplanner.data.PlantDao // Import DAO
import com.example.gardenplanner.data.PlantEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the MyGardenFragment.
 * Provides the list of favorite plants and handles toggling favorite status.
 */
class MyGardenViewModel(application: Application) : AndroidViewModel(application) {

    private val plantDao: PlantDao = AppDatabase.getDatabase(application).plantDao()

    // Expose the Flow of favorite plants as StateFlow
    // Fetches plants where isFavorite is true directly from the DAO
    val favoritePlants: StateFlow<List<PlantEntity>> = plantDao.getFavoritePlants()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keep active 5s after UI stops observing
            initialValue = emptyList() // Initial default value
        )

    // Expose a boolean StateFlow indicating if there are any favorites
    // Derived from the favoritePlants flow
    val hasFavorites: StateFlow<Boolean> = favoritePlants
        .map { it.isNotEmpty() } // Map the list to a boolean (true if not empty)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false // Assume no favorites initially
        )

    /**
     * Toggles the favorite status of a given plant in the database.
     * This is asynchronous and runs in the ViewModel's scope.
     *
     * @param plant The PlantEntity whose favorite status needs to be toggled.
     */
    fun toggleFavorite(plant: PlantEntity) {
        viewModelScope.launch {
            // Create a copy with the flipped favorite status
            val updatedPlant = plant.copy(isFavorite = !plant.isFavorite)
            // Update the database
            plantDao.updatePlant(updatedPlant)
        }
    }
}
