package com.example.gardenplanner.viewmodel

import android.app.Application // Needed for simple DB access (better: use Dependency Injection)
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenplanner.data.AppDatabase // Import your Database class
import com.example.gardenplanner.data.PlantEntity // Import your Entity class
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// Extend AndroidViewModel to get Application context easily (or use ViewModel with Hilt/Koin for DI)
class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    // Get a reference to the DAO
    private val plantDao = AppDatabase.getDatabase(application).plantDao()

    // --- Expose the Flow of favorite plants as StateFlow ---
    // This Flow will automatically update when the database changes.
    // We convert it to StateFlow to make it easy for the Fragment to observe.
    val favoritePlants: StateFlow<List<PlantEntity>> = plantDao.getFavoritePlants()
        .stateIn(
            scope = viewModelScope, // Scope tied to ViewModel lifecycle
            started = SharingStarted.WhileSubscribed(5000), // Keep flow active for 5s after last observer disappears
            initialValue = emptyList() // Start with an empty list until DB emits
        )

    // You can add methods here later for updating favorites, getting frost date etc.
}