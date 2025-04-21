package com.example.gardenplanner.fragments // Correct package name

// Import necessary components
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast // For simple feedback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope // Import for coroutines
import com.example.gardenplanner.databinding.FragmentCalendarBinding // Use ViewBinding
import com.example.gardenplanner.helpers.CalendarCalculator
import com.example.gardenplanner.helpers.CalendarEvent
import com.example.gardenplanner.helpers.OriginalDatabaseHelper
import com.example.gardenplanner.helpers.PreloadedDatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

class CalendarFragment : Fragment() {

    // Use ViewBinding
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    // Database Helpers and Calculator
    private lateinit var preloadedDbHelper: PreloadedDatabaseHelper
    private lateinit var originalDbHelper: OriginalDatabaseHelper
    private lateinit var calendarCalculator: CalendarCalculator

    // Store calculated events (mapping Date to list of event descriptions for that day)
    private var allCalendarEvents: Map<LocalDate, List<String>> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using ViewBinding
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Helpers and Calculator
        // Ensure context is not null
        context?.let {
            preloadedDbHelper = PreloadedDatabaseHelper(it)
            // Make sure OriginalDatabaseHelper exists and is correctly initialized
            originalDbHelper = OriginalDatabaseHelper(it)
            calendarCalculator = CalendarCalculator()

            // Load events asynchronously
            loadCalendarEvents()
        } ?: run {
            Log.e("CalendarFragment", "Context was null during helper initialization.")
            Toast.makeText(requireContext(), "Error initializing calendar.", Toast.LENGTH_SHORT).show()
        }

        // --- UI Interaction ---
        // Handle date selection on the basic CalendarView
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth) // month is 0-indexed
            displayEventsForDate(selectedDate)
        }

        // TODO: Add logic here later to integrate with a better Calendar View library
        // e.g., setting decorators/markers on dates that have events in `allCalendarEvents`
        // setupAdvancedCalendarView()
    }

    private fun loadCalendarEvents() {
        // Use coroutines for background database operations
        lifecycleScope.launch(Dispatchers.IO) { // IO Dispatcher for DB access
            try {
                // Ensure PreloadedDatabaseHelper has getFavoritePlantIds method
                val favoritePlantIds = preloadedDbHelper.getFavoritePlantIds()
                if (favoritePlantIds.isEmpty()) {
                    Log.d("CalendarFragment", "No favorite plants found.")
                    withContext(Dispatchers.Main) {
                        binding.textViewEventsDisplay?.text = "Add plants to your garden to see calendar events." // Example TextView
                        binding.textViewEventsDisplay?.visibility = View.VISIBLE
                    }
                    return@launch
                }

                Log.d("CalendarFragment", "Found favorite plant IDs: $favoritePlantIds")
                // Ensure OriginalDatabaseHelper has getPlantsByIds method
                val favoritePlants = originalDbHelper.getPlantsByIds(favoritePlantIds)

                Log.d("CalendarFragment", "Fetched ${favoritePlants.size} favorite plants details.")
                val calculatedEvents = mutableListOf<CalendarEvent>()
                favoritePlants.forEach { plant ->
                    // Calculate events using the default last frost date in the calculator
                    calculatedEvents.addAll(calendarCalculator.calculateEventsForPlant(plant))
                }

                Log.d("CalendarFragment", "Calculated ${calculatedEvents.size} total events.")

                // Group events by date for easier lookup and display
                val groupedEvents = calculatedEvents.groupBy(
                    { it.date }, // Key is the LocalDate
                    { "${it.plantName}: ${it.description}" } // Value is the event description string
                )

                // Update the UI on the Main thread
                withContext(Dispatchers.Main) {
                    allCalendarEvents = groupedEvents
                    Log.d("CalendarFragment", "Events loaded and grouped for UI.")
                    Toast.makeText(context, "Calendar events loaded", Toast.LENGTH_SHORT).show()

                    // --- Trigger initial UI update for the calendar view ---
                    // TODO: Refresh your advanced calendar view here to show event markers/dots
                    // e.g., materialCalendarView.addDecorators(...)
                    // updateCalendarDecorators()

                    // Optionally display events for today initially
                    displayEventsForDate(LocalDate.now(ZoneId.systemDefault()))
                }

            } catch (e: Exception) {
                Log.e("CalendarFragment", "Error loading calendar events", e)
                // Show error message on UI thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error loading calendar data: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Example function to display events when a date is selected (for basic CalendarView)
    private fun displayEventsForDate(date: LocalDate) {
        // Ensure binding is not null
        if (_binding == null) return

        val eventsForDay = allCalendarEvents[date]
        val displayTextView = binding.textViewEventsDisplay // Assume you add this ID to your XML

        if (eventsForDay.isNullOrEmpty()) {
            Log.d("CalendarFragment", "No events for $date")
            displayTextView?.text = "No events for $date"
            displayTextView?.visibility = View.VISIBLE // Or GONE/INVISIBLE as needed
        } else {
            val eventsText = eventsForDay.joinToString("\n")
            Log.d("CalendarFragment", "Events for $date:\n$eventsText")
            displayTextView?.text = "Events for $date:\n$eventsText"
            displayTextView?.visibility = View.VISIBLE
        }
    }

    // Example placeholder for updating a better calendar view
    // private fun updateCalendarDecorators() {
    //    if (_binding == null) return // Check binding
    //    val datesWithEvents = allCalendarEvents.keys
    //    // Assuming you have a reference to your advanced calendar view
    //    // e.g., binding.materialCalendarView.removeDecorators()
    //    // binding.materialCalendarView.addDecorator(EventDecorator(datesWithEvents))
    // }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up ViewBinding reference to prevent memory leaks
    }
}

// --- REMINDER: You still need to add these methods (or similar) to your Database Helpers ---

// Add to PreloadedDatabaseHelper.kt:
/*
fun getFavoritePlantIds(): List<Int> { ... } // See previous response for example
*/

// Add to OriginalDatabaseHelper.kt:
/*
fun getPlantsByIds(ids: List<Int>): List<Plant> { ... } // See previous response for example
*/