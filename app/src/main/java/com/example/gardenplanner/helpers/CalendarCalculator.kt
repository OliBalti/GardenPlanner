package com.example.gardenplanner.helpers

// Import PlantEntity from your data package
import com.example.gardenplanner.data.PlantEntity
import java.time.LocalDate
import java.time.Year
import android.util.Log // Import Log for debugging

/**
 * Simple data class to hold results of calendar calculations.
 * (Keep this as is)
 */
data class CalendarEvent(
    val date: LocalDate,
    val plantName: String,
    val description: String,
    val plantId: Int
)

/**
 * Calculates specific planting/harvesting dates based on relative data stored in PlantEntity objects.
 */
class CalendarCalculator {

    // Default average last frost date for calculations (e.g., May 15th for Zurich region).
    // Uses the current year automatically. Can be overridden.
    // TODO: Consider making this default configurable globally or passed from settings.
    private val defaultLastFrostDate: LocalDate = LocalDate.of(Year.now().value, 5, 15)
    private val TAG = "CalendarCalculator" // Tag for logging

    /**
     * Calculates all relevant calendar events for a single plant for the current year.
     * Uses the plant's stored intervals (days relative to last frost / planting).
     *
     * @param plantEntity The Room entity object representing the plant.
     * @param lastFrostDate The reference last frost date (defaults to May 15th of current year).
     * @return A list of calculated CalendarEvent objects.
     */
    fun calculateEventsForPlant(
        plantEntity: PlantEntity,
        lastFrostDate: LocalDate = defaultLastFrostDate.withYear(Year.now().value)
    ): List<CalendarEvent> {

        val events = mutableListOf<CalendarEvent>()
        val currentYear = lastFrostDate.year
        var plantingDate: LocalDate? = null // Tracks when plant goes in ground (transplant or direct sow)

        Log.d(TAG, "Calculating events for ${plantEntity.name} (ID: ${plantEntity.id}) with frost date $lastFrostDate")

        // Calculate Indoor Start date (if applicable)
        plantEntity.startIndoorsDaysBeforeFrost?.let { daysBefore ->
            if (daysBefore > 0) { // Only calculate if days > 0
                val startDate = lastFrostDate.minusDays(daysBefore.toLong())
                Log.d(TAG, " -> Indoor Start Date: $startDate ($daysBefore days before frost)")
                events.add(CalendarEvent(startDate, plantEntity.name, "Start seeds indoors", plantEntity.id))
            }
        }

        // Calculate Transplant date (if applicable and started indoors)
        plantEntity.transplantDaysAfterLastFrost?.let { daysAfter ->
            if (plantEntity.startIndoorsDaysBeforeFrost != null && plantEntity.startIndoorsDaysBeforeFrost > 0) { // Check if started indoors
                val transplantDate = lastFrostDate.plusDays(daysAfter.toLong())
                Log.d(TAG, " -> Transplant Date: $transplantDate ($daysAfter days after frost)")
                events.add(CalendarEvent(transplantDate, plantEntity.name, "Transplant seedlings", plantEntity.id))
                plantingDate = transplantDate // Set planting date for harvest calc
            }
        }

        // Calculate Direct Sow date (if applicable and not transplanted)
        plantEntity.directSowDaysAfterLastFrost?.let { daysAfter ->
            if (plantingDate == null) { // Only direct sow if not transplanted
                val directSowDate = lastFrostDate.plusDays(daysAfter.toLong())
                Log.d(TAG, " -> Direct Sow Date: $directSowDate ($daysAfter days after frost)")
                events.add(CalendarEvent(directSowDate, plantEntity.name, "Direct sow seeds", plantEntity.id))
                plantingDate = directSowDate // Set planting date for harvest calc
            }
        }

        // Calculate Harvest Window (requires a planting date)
        plantingDate?.let { plantDate -> // Run only if plantingDate was set
            Log.d(TAG, " -> Planting Date (for harvest calc): $plantDate")
            // Use the correct field name for harvest start
            plantEntity.harvestStartDaysAfterPlanting?.let { startDays ->
                val harvestStartDate = plantDate.plusDays(startDays.toLong())
                Log.d(TAG, " -> Harvest Start Date: $harvestStartDate ($startDays days after planting)")
                events.add(CalendarEvent(harvestStartDate, plantEntity.name, "Begin harvesting", plantEntity.id))

                // *** ADDED: Calculate harvest end date using the correct field ***
                plantEntity.harvestEndDaysAfterPlanting?.let { endDays ->
                    // Ensure end date makes sense (is after start date)
                    if (endDays > startDays) {
                        val harvestEndDate = plantDate.plusDays(endDays.toLong())
                        Log.d(TAG, " -> Harvest End Date: $harvestEndDate ($endDays days after planting)")
                        events.add(CalendarEvent(harvestEndDate, plantEntity.name, "End harvest window", plantEntity.id))
                    } else {
                        Log.w(TAG, " -> Harvest End Date ($endDays days) is not after start date ($startDays days). Skipping end event.")
                    }
                } ?: Log.d(TAG, " -> No Harvest End Days specified.") // Log if end days field is null
            } ?: Log.d(TAG, " -> No Harvest Start Days specified.") // Log if start days field is null
        } ?: Log.d(TAG, " -> No Planting Date established, skipping harvest calculation.") // Log if no planting date

        // Return only events for the current year (optional filter)
        return events.filter { it.date.year == currentYear }
    }
}
