package com.example.gardenplanner.model

data class Plant(
    val id: Int,               // Unique ID for the plant
    val name: String,          // Name of the plant
    val image: ByteArray?,     // Image of the plant as a byte array (nullable)
    val description: String = "",  // Description of the plant (optional)

    // Existing descriptive window fields (consider if these are still needed or replaced by calculations)
    val seedingWindow: String = "", // Seeding time window (optional)
    val startIndoors: Boolean = false, // Can it be started indoors? (true/false)
    val indoorsWindow: String = "", // Time window for starting indoors (optional)
    val transplantWindow: String = "", // Time window for transplanting (optional)
    val directSowWindow: String = "",  // Time window for direct sowing (optional)
    val harvestWindow: String = "",     // Time window for harvesting (optional)

    // NEW: Relative planting intervals in days (nullable integers)
    // These values are relative to the average last frost date
    val startIndoorsDaysBeforeLastFrost: Int? = null, // Days BEFORE last frost to start indoors
    val transplantDaysAfterLastFrost: Int? = null,    // Days AFTER last frost to transplant
    val directSowDaysAfterLastFrost: Int? = null,     // Days AFTER last frost to sow directly
    val harvestStartDaysAfterPlanting: Int? = null, // Days after planting/transplant to first harvest
    val harvestEndDaysAfterPlanting: Int? = null    // Days after planting/transplant for end of harvest window
) {
    // Optional: Override equals and hashCode if image comparison is not desired or needs custom handling
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Plant

        if (id != other.id) return false
        if (name != other.name) return false
        // Exclude image from default equals comparison if it's large or problematic
        // if (image != null) {
        //     if (other.image == null) return false
        //     if (!image.contentEquals(other.image)) return false
        // } else if (other.image != null) return false
        if (description != other.description) return false
        if (seedingWindow != other.seedingWindow) return false
        if (startIndoors != other.startIndoors) return false
        if (indoorsWindow != other.indoorsWindow) return false
        if (transplantWindow != other.transplantWindow) return false
        if (directSowWindow != other.directSowWindow) return false
        if (harvestWindow != other.harvestWindow) return false
        if (startIndoorsDaysBeforeLastFrost != other.startIndoorsDaysBeforeLastFrost) return false
        if (transplantDaysAfterLastFrost != other.transplantDaysAfterLastFrost) return false
        if (directSowDaysAfterLastFrost != other.directSowDaysAfterLastFrost) return false
        if (harvestStartDaysAfterPlanting != other.harvestStartDaysAfterPlanting) return false
        if (harvestEndDaysAfterPlanting != other.harvestEndDaysAfterPlanting) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        // Exclude image from default hashCode calculation
        // result = 31 * result + (image?.contentHashCode() ?: 0)
        result = 31 * result + description.hashCode()
        result = 31 * result + seedingWindow.hashCode()
        result = 31 * result + startIndoors.hashCode()
        result = 31 * result + indoorsWindow.hashCode()
        result = 31 * result + transplantWindow.hashCode()
        result = 31 * result + directSowWindow.hashCode()
        result = 31 * result + harvestWindow.hashCode()
        result = 31 * result + (startIndoorsDaysBeforeLastFrost ?: 0)
        result = 31 * result + (transplantDaysAfterLastFrost ?: 0)
        result = 31 * result + (directSowDaysAfterLastFrost ?: 0)
        result = 31 * result + (harvestStartDaysAfterPlanting ?: 0)
        result = 31 * result + (harvestEndDaysAfterPlanting ?: 0)
        return result
    }
}