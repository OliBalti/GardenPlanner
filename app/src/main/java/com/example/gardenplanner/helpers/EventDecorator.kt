package com.example.gardenplanner.helpers // Or your chosen package e.g., ui.decorators

import android.graphics.Color // Import standard Android Color
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan

/**
 * A decorator for MaterialCalendarView to add a colored dot below dates that have events.
 *
 * @param color The color of the dot (use Color.RED, Color.parseColor("#FFA500"), etc.).
 * @param dates A collection of CalendarDay objects that should receive the dot decoration.
 */
class EventDecorator(private val color: Int, dates: Collection<CalendarDay>) : DayViewDecorator {

    // Use a HashSet for efficient lookups in shouldDecorate
    private val datesToDecorate: HashSet<CalendarDay> = HashSet(dates)

    /**
     * Determines if a specific day should be decorated.
     * @param day The CalendarDay to check.
     * @return True if the day is in the set of dates provided to the constructor, false otherwise.
     */
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return datesToDecorate.contains(day)
    }

    /**
     * Applies the decoration (a colored dot) to the DayViewFacade.
     * This is called by the calendar view only for days where shouldDecorate returned true.
     * @param view The DayViewFacade for the specific day cell being decorated.
     */
    override fun decorate(view: DayViewFacade) {
        // Add a colored dot span below the date number.
        // DEFAULT_RADIUS is a predefined radius in DotSpan. You can specify a float radius too.
        view.addSpan(DotSpan(5f, color)) // 5f is the radius, adjust as needed
    }
}
