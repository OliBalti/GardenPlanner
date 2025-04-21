package com.example.gardenplanner.helpers

import com.example.gardenplanner.model.Plant
import java.time.LocalDate
import java.time.Year

/**
 * Simple data class to hold results of calendar calculations.
 */
data class CalendarEvent(
    val date: LocalDate,
    val plantName: String,
    val description: String,
    val plantId: Int
)

/**
 * Calculates specific planting/harvesting dates based on relative data stored in Plant objects.
 */
class CalendarCalculator {

    // Default average last frost date for calculations (e.g., May 1st for Zurich region).
    // Uses the current year automatically. Can be overridden when calling calculateEventsForPlant.
    private val defaultLastFrostDate: LocalDate = LocalDate.of(Year.now().value, 5, 1)

    /**
     * Calculates all relevant calendar events for a single plant for the current year.
     * Uses the plant's stored intervals (days relative to last frost / planting).
     *
     * @param plant The plant data object.
     * @param lastFrostDate The reference last frost date (defaults to May 1st of current year).
     * @return A list of calculated CalendarEvent objects.
     */
    fun calculateEventsForPlant(
        plant: Plant,
        lastFrostDate: LocalDate = defaultLastFrostDate.withYear(Year.now().value)
    ): List<CalendarEvent> {

        val events = mutableListOf<CalendarEvent>()
        val currentYear = lastFrostDate.year
        var plantingDate: LocalDate? = null // Tracks when plant goes in ground (transplant or direct sow)

        // Calculate Indoor Start date (if applicable)
        // `?.let` runs the block only if the property is not null.
        plant.startIndoorsDaysBeforeLastFrost?.let { daysBefore ->
            val startDate = lastFrostDate.minusDays(daysBefore.toLong())
            events.add(CalendarEvent(startDate, plant.name, "Start ${plant.name} seeds indoors", plant.id))
        }

        // Calculate Transplant date (if applicable and started indoors)
        plant.transplantDaysAfterLastFrost?.let { daysAfter ->
            if (plant.startIndoors || plant.startIndoorsDaysBeforeLastFrost != null) {
                val transplantDate = lastFrostDate.plusDays(daysAfter.toLong())
                events.add(CalendarEvent(transplantDate, plant.name, "Transplant ${plant.name} seedlings", plant.id))
                plantingDate = transplantDate // Set planting date for harvest calc
            }
        }

        // Calculate Direct Sow date (if applicable and not transplanted)
        plant.directSowDaysAfterLastFrost?.let { daysAfter ->
            if (plantingDate == null) { // Only direct sow if not transplanted
                val directSowDate = lastFrostDate.plusDays(daysAfter.toLong())
                events.add(CalendarEvent(directSowDate, plant.name, "Direct sow ${plant.name} seeds", plant.id))
                plantingDate = directSowDate // Set planting date for harvest calc
            }
        }

        // Calculate Harvest Window (requires a planting date)
        plantingDate?.let { plantDate -> // Run only if plantingDate was set
            plant.harvestStartDaysAfterPlanting?.let { daysAfter ->
                val harvestStartDate = plantDate.plusDays(daysAfter.toLong())
                events.add(CalendarEvent(harvestStartDate, plant.name, "Begin harvesting ${plant.name}", plant.id))

                // Optional: Calculate harvest end date
                plant.harvestEndDaysAfterPlanting?.let { endDays ->
                    if (endDays > daysAfter) {
                        val harvestEndDate = plantDate.plusDays(endDays.toLong())
                        events.add(CalendarEvent(harvestEndDate, plant.name, "End harvest window for ${plant.name}", plant.id))
                    }
                }
            }
        }

        // Return only events for the current year
        return events.filter { it.date.year == currentYear }
    }

    // TODO: Add functions for batch processing or filtering if needed later
}