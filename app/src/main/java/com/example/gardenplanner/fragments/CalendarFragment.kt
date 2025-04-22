package com.example.gardenplanner.fragments // Correct package name

// Import necessary components
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast // For simple feedback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Import for ViewModel delegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope // Import for coroutines
import androidx.lifecycle.repeatOnLifecycle
import com.example.gardenplanner.data.PlantEntity
import com.example.gardenplanner.databinding.FragmentCalendarBinding // Use ViewBinding
import com.example.gardenplanner.helpers.CalendarCalculator // Keep calculator
import com.example.gardenplanner.helpers.CalendarEvent // Keep event helper
// Removed Database Helper imports as ViewModel handles DB access
// import com.example.gardenplanner.helpers.OriginalDatabaseHelper
// import com.example.gardenplanner.helpers.PreloadedDatabaseHelper
import com.example.gardenplanner.viewmodel.CalendarViewModel
import kotlinx.coroutines.launch // Import launch
// Removed Dispatchers imports, ViewModel handles threading
// import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CalendarFragment : Fragment() {

    // Use ViewBinding
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    // --- Get instance of ViewModel ---
    private val viewModel: CalendarViewModel by viewModels()

    // --- Remove direct DB Helpers ---
    // private lateinit var preloadedDbHelper: PreloadedDatabaseHelper
    // private lateinit var originalDbHelper: OriginalDatabaseHelper

    // Keep calculator (will be used inside populateCalendarEvents)
    private lateinit var calendarCalculator: CalendarCalculator

    // Store calculated events locally in Fragment for display (populated by observing ViewModel)
    private val calendarEvents = mutableMapOf<LocalDate, MutableList<String>>()

    // Define Last Frost Date (temporary - fetch from ViewModel/Prefs later)
    // TODO: Make this configurable or fetch from settings/ViewModel
    private val lastFrostDate: LocalDate = LocalDate.of(LocalDate.now().year, 5, 15)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Calculator (can be done here or inside populateCalendarEvents)
        calendarCalculator = CalendarCalculator()

        // --- Remove direct loading call ---
        // loadCalendarEvents()

        // --- Setup Date Selection Listener ---
        setupCalendarViewListener()

        // --- Observe the favorite plants from the ViewModel ---
        viewLifecycleOwner.lifecycleScope.launch {
            // repeatOnLifecycle ensures collection stops when view is destroyed
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d("CalendarFragment", "Starting to collect favorite plants from ViewModel")
                viewModel.favoritePlants.collect { favoritesList ->
                    Log.d("CalendarFragment", "Received ${favoritesList.size} favorite plants from ViewModel.")
                    // Populate the local event map using the fetched favorites and frost date
                    populateCalendarEvents(favoritesList, lastFrostDate)

                    // Refresh the display for the currently selected/default date
                    // Get current selection or default to today
                    val currentDate = getCurrentSelectedDateOrDefault()
                    Log.d("CalendarFragment", "Refreshing display for date: $currentDate")
                    displayEventsForDate(currentDate)

                    // TODO: If using MaterialCalendarView, update decorators here
                    // updateCalendarDecorators()
                }
            }
        }
    }

    // --- Remove old loadCalendarEvents function ---
    // private fun loadCalendarEvents() { ... }

    /**
     * Populates the local calendarEvents map based on the list of favorite plants
     * fetched from the ViewModel and the last frost date.
     */
    private fun populateCalendarEvents(plantList: List<PlantEntity>, frostDate: LocalDate) {
        Log.d("CalendarFragment", "Populating calendar events map...")
        calendarEvents.clear() // Clear previous events

        if (plantList.isEmpty()) {
            Log.d("CalendarFragment", "Plant list is empty, no events to populate.")
            // Optionally update UI to show "No favorites selected"
            binding.textViewEventsDisplay?.text = "Add plants to your garden to see calendar events."
            binding.textViewEventsDisplay?.visibility = View.VISIBLE
            return
        }

        val calculatedEvents = mutableListOf<CalendarEvent>()
        plantList.forEach { plantEntity ->
            // Assuming CalendarCalculator expects PlantData or similar, convert PlantEntity
            // Use the specific frost date for calculations
            calculatedEvents.addAll(calendarCalculator.calculateEventsForPlant(plantEntity, frostDate))
        }
        Log.d("CalendarFragment", "Calculated ${calculatedEvents.size} total events.")

        // Group events by date and add to the map using the helper
        calculatedEvents.forEach { event ->
            addEvent(event.date, "${event.plantName}: ${event.description}")
        }
        Log.d("CalendarFragment", "Finished populating calendar events map.")
        // Optional: Show a toast only if needed, maybe not every time data updates
        // Toast.makeText(context, "Calendar events updated", Toast.LENGTH_SHORT).show()
    }

    // Renamed from getSelectedDateFromCalendarView for clarity
    private fun getCurrentSelectedDateOrDefault(): LocalDate {
        val milli = binding.calendarView.date // Get selected date in milliseconds
        return Instant.ofEpochMilli(milli).atZone(ZoneId.systemDefault()).toLocalDate()
        // Fallback if needed, though calendarView usually has a date selected
            ?: LocalDate.now(ZoneId.systemDefault())
    }


    private fun setupCalendarViewListener() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth) // month is 0-indexed
            displayEventsForDate(selectedDate)
        }
    }

    private fun displayEventsForDate(date: LocalDate) {
        if (_binding == null) {
            Log.w("CalendarFragment", "displayEventsForDate called but binding is null")
            return
        } // Check binding

        val eventsForDay = calendarEvents[date]
        val displayTextView = binding.textViewEventsDisplay // Assume ID exists in fragment_calendar.xml

        if (eventsForDay.isNullOrEmpty()) {
            // Log.d("CalendarFragment", "No events for $date")
            // Keep the previous text or set default message if TextView exists
            displayTextView?.text = "No scheduled tasks for $date."
            // displayTextView?.visibility = View.GONE // Or keep visible with message
        } else {
            val eventsText = eventsForDay.joinToString("\n")
            Log.d("CalendarFragment", "Displaying events for $date")
            displayTextView?.text = eventsText // Display only the events, maybe add date separately if needed
            displayTextView?.visibility = View.VISIBLE
        }
    }

    // Helper to add event to the local map (keep this)
    private fun addEvent(date: LocalDate, message: String) {
        val eventsForDate = calendarEvents.getOrPut(date) { mutableListOf() }
        eventsForDate.add(message)
    }

    // Placeholder for updating MaterialCalendarView decorators later
    // private fun updateCalendarDecorators() { ... }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("CalendarFragment", "View destroyed, binding set to null")
    }
}