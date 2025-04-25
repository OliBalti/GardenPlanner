package com.example.gardenplanner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenplanner.data.AppDatabase
import com.example.gardenplanner.data.PlantDao
import com.example.gardenplanner.data.PlantEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the PlantSelectionFragment.
 * Provides the full list of plants, handles search filtering,
 * and handles toggling favorite status.
 */
@OptIn(ExperimentalCoroutinesApi::class) // Needed for flatMapLatest operator
class PlantSelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val plantDao: PlantDao = AppDatabase.getDatabase(application).plantDao()

    // --- Search Query State ---
    // Private MutableStateFlow to hold the current search query internally.
    private val _searchQuery = MutableStateFlow("")
    // Public immutable StateFlow exposed to the Fragment for observing the query.
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // --- Plant List State ---
    // Flow representing the full list of all plants from the database DAO.
    private val _allPlants: Flow<List<PlantEntity>> = plantDao.getAllPlants()

    // Filtered list exposed to the UI (Fragment).
    // It combines the latest search query and the latest full plant list.
    // flatMapLatest ensures that if the query changes, the previous filter mapping is cancelled
    // and a new one starts with the latest query.
    val filteredPlants: StateFlow<List<PlantEntity>> = searchQuery
        .flatMapLatest { query -> // Reacts to changes in searchQuery
            _allPlants.map { plants -> // Reacts to changes in the full plant list
                if (query.isBlank()) {
                    plants // If query is empty, return the full list
                } else {
                    // If query is not empty, filter the full list
                    plants.filter { plant ->
                        // Case-insensitive search on the plant name
                        plant.name.contains(query, ignoreCase = true)
                        // TODO: Optionally add filtering by description or other fields here
                        // || plant.description?.contains(query, ignoreCase = true) == true
                    }
                }
            }
        }
        .stateIn( // Convert the resulting Flow to a StateFlow for the UI
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList() // Initial value before flows emit
        )

    /**
     * Updates the internal search query StateFlow. Called by the Fragment's SearchView listener.
     * @param query The new search query string.
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Toggles the favorite status of a given plant in the database.
     * (Identical logic to MyGardenViewModel - consider moving to a Repository later).
     *
     * @param plant The PlantEntity whose favorite status needs to be toggled.
     */
    fun toggleFavorite(plant: PlantEntity) {
        viewModelScope.launch { // Launch background coroutine
            val updatedPlant = plant.copy(isFavorite = !plant.isFavorite)
            plantDao.updatePlant(updatedPlant)
            // The _allPlants flow (if observed directly) or dependent flows like
            // filteredPlants will automatically update due to the database change.
        }
    }
}
