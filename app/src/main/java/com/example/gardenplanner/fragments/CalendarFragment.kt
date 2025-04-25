package com.example.gardenplanner.fragments // Ensure package matches

import android.graphics.Color // Import Color for decorator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat // Use ContextCompat for color resources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.gardenplanner.R // Import R for resources like colors
import com.example.gardenplanner.data.PlantEntity
import com.example.gardenplanner.databinding.FragmentCalendarBinding // Your ViewBinding class
import com.example.gardenplanner.helpers.CalendarCalculator
import com.example.gardenplanner.helpers.CalendarEvent
import com.example.gardenplanner.helpers.EventDecorator // *** Import your EventDecorator ***
import com.example.gardenplanner.viewmodel.CalendarViewModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import kotlinx.coroutines.launch
import java.time.LocalDate

// Implement the listener interface for date selection
class CalendarFragment : Fragment(), OnDateSelectedListener {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!! // Use ViewBinding

    private val viewModel: CalendarViewModel by viewModels()
    private lateinit var calendarCalculator: CalendarCalculator

    // Map storing calculated events (Date -> List of descriptions) for detail view
    private val calendarEvents = mutableMapOf<LocalDate, MutableList<String>>()
    // Set to store just the unique dates that have events, used by the decorator
    private val eventDates = HashSet<CalendarDay>() // *** Added Set for decorator dates ***

    // TODO: Make lastFrostDate dynamic later
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

        calendarCalculator = CalendarCalculator()

        // --- Setup MaterialCalendarView Listener ---
        binding.materialCalendarView.setOnDateChangedListener(this) // Set listener

        // Set current day selection (optional, good UX)
        binding.materialCalendarView.selectedDate = CalendarDay.today()

        // Observe favorite plants from ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d("CalendarFragment", "Observing favorite plants...")
                viewModel.favoritePlants.collect { favoritesList ->
                    Log.d("CalendarFragment", "Favorites updated: ${favoritesList.size} items")

                    // *** Populate the event map AND the eventDates set ***
                    populateEventsAndDates(favoritesList, lastFrostDate)

                    // *** Apply the decorator to the calendar ***
                    updateCalendarDecorators()

                    // Refresh display for currently selected date after data load/update
                    refreshDisplayForSelection()
                }
            }
        }
        // Initial display update for today when view is first created
        refreshDisplayForSelection()
    }

    /**
     * Handles clicks on dates in the MaterialCalendarView.
     */
    override fun onDateSelected(
        widget: MaterialCalendarView,
        date: CalendarDay,
        selected: Boolean
    ) {
        if (selected) {
            // Convert selected CalendarDay to LocalDate and display events
            val selectedLocalDate = LocalDate.of(date.year, date.month, date.day)
            Log.d("CalendarFragment", "Date selected via listener: $selectedLocalDate")
            displayEventsForDate(selectedLocalDate)
        } else {
            // Optional: Clear the TextView if a date is deselected
            binding.textViewEventsDisplay.text = getString(R.string.calendar_select_date_prompt)
        }
    }

    /**
     * Populates both the calendarEvents map (for detail display) and
     * the eventDates set (for the decorator), based on the fetched plant list.
     * Renamed from populateCalendarEvents for clarity.
     */
    private fun populateEventsAndDates(plantList: List<PlantEntity>, frostDate: LocalDate) {
        // Clear previous data
        calendarEvents.clear()
        eventDates.clear() // *** Clear the set too ***
        Log.d("CalendarFragment", "Populating events and dates for ${plantList.size} plants.")

        if (plantList.isEmpty()) {
            Log.d("CalendarFragment", "Plant list is empty.")
            // Ensure decorators are cleared if list becomes empty
            // (updateCalendarDecorators will handle this after this function returns)
            return // Nothing more to do
        }

        // Calculate events for each favorited plant
        plantList.forEach { plantEntity ->
            try {
                val calculated = calendarCalculator.calculateEventsForPlant(plantEntity, frostDate)
                calculated.forEach { event ->
                    // Add description to map for the detail view
                    addEventDescription(event.date, "${event.plantName}: ${event.description}")
                    // *** Add date to set for the decorator ***
                    eventDates.add(CalendarDay.from(event.date.year, event.date.monthValue, event.date.dayOfMonth))
                }
            } catch (e: Exception) {
                Log.e("CalendarFragment", "Error calculating events for plant ${plantEntity.name}", e)
            }
        }
        Log.d("CalendarFragment", "Finished populating. ${calendarEvents.size} dates have events. ${eventDates.size} unique event dates.")
    }

    /**
     * Applies the EventDecorator to the MaterialCalendarView based on the eventDates set.
     * Should be called after populateEventsAndDates.
     */
    private fun updateCalendarDecorators() {
        // *** New function to apply decorators ***
        if (_binding == null) {
            Log.w("CalendarFragment", "updateCalendarDecorators called but binding is null")
            return // Check binding isn't null
        }

        // Define the color for the event dot (using a color resource is recommended)
        // Ensure R.color.event_dot_color exists in res/values/colors.xml
        val eventColor = ContextCompat.getColor(requireContext(), R.color.purple_500) // Example color
        // Or use a standard color: val eventColor = Color.BLUE

        // Remove ALL previous decorators before adding new ones to prevent duplicates
        // This is important if the set of event dates changes
        binding.materialCalendarView.removeDecorators()

        // Add the decorator only if there are dates with events
        if (eventDates.isNotEmpty()) {
            Log.d("CalendarFragment", "Adding decorator for ${eventDates.size} dates.")
            binding.materialCalendarView.addDecorator(EventDecorator(eventColor, eventDates))
        } else {
            Log.d("CalendarFragment", "No event dates, skipping decorator.")
        }
        // Invalidate the calendar view to ensure decorators are redrawn immediately
        binding.materialCalendarView.invalidateDecorators()
    }

    /**
     * Refreshes the event details TextView based on the currently selected date
     * in the MaterialCalendarView.
     */
    private fun refreshDisplayForSelection() {
        // *** New helper function to avoid code duplication ***
        if (_binding == null) return
        val selectedCalDay = binding.materialCalendarView.selectedDate
        // Use today() which correctly handles month indexing (1-12)
        val selectedLocalDate = selectedCalDay?.let {
            LocalDate.of(it.year, it.month, it.day)
        } ?: LocalDate.now() // Default to today if somehow null
        displayEventsForDate(selectedLocalDate)
    }

    /**
     * Displays the event descriptions for a given date in the TextView.
     */
    private fun displayEventsForDate(date: LocalDate) {
        if (_binding == null) return
        val eventsForDay = calendarEvents[date]
        val displayTextView = binding.textViewEventsDisplay

        if (eventsForDay.isNullOrEmpty()) {
            // Use a string resource for consistency and formatting
            displayTextView.text = getString(R.string.calendar_no_tasks, date.toString())
            // Example: Define in strings.xml: <string name="calendar_no_tasks">No scheduled tasks for %1$s.</string>
        } else {
            val eventsText = eventsForDay.joinToString("\n")
            Log.d("CalendarFragment", "Displaying events for $date")
            displayTextView.text = eventsText // Display only the events
        }
        // Keep TextView visible to show either events or "no tasks" message
        displayTextView.visibility = View.VISIBLE
    }

    /**
     * Helper to add an event description message to the map for a specific date.
     * Renamed from addEvent for clarity.
     */
    private fun addEventDescription(date: LocalDate, message: String) {
        val eventsForDate = calendarEvents.getOrPut(date) { mutableListOf() }
        eventsForDate.add(message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("CalendarFragment", "View destroyed, binding set to null")
    }
}
