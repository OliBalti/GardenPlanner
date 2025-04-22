package com.example.gardenplanner.data // Ensure package matches

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the 'plant_definitions' table in the database.
 * Column names MUST match the database schema exactly.
 */
@Entity(tableName = "plant_definitions")
data class PlantEntity(
    @PrimaryKey // Assuming 'id' is the primary key
    @ColumnInfo(name = "id")
    val id: Int, // Assuming non-auto-generating based on screenshot data

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB, name = "image") // Specify BLOB type
    val image: ByteArray?,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "seeding_window")
    val seedingWindow: String?,

    @ColumnInfo(name = "indoors_window")
    val indoorsWindow: String?,

    @ColumnInfo(name = "transplant_window")
    val transplantWindow: String?,

    @ColumnInfo(name = "direct_sow_window")
    val directSowWindow: String?,

    @ColumnInfo(name = "harvest_window")
    val harvestWindow: String?,

    @ColumnInfo(name = "start_indoors_days_before_last_frost") // Assuming full name
    val startIndoorsDaysBeforeFrost: Int?, // Nullable Integer

    @ColumnInfo(name = "transplant_days_after_last_frost") // Assuming full name
    val transplantDaysAfterLastFrost: Int?, // Nullable Integer

    @ColumnInfo(name = "direct_sow_days_after_last_frost") // Assuming full name
    val directSowDaysAfterLastFrost: Int?, // Nullable Integer

    @ColumnInfo(name = "harvest_start_days_after_planting") // Assuming full name
    val harvestStartDaysAfterPlanting: Int?, // Nullable Integer

    @ColumnInfo(name = "harvest_end_days_after_planting") // Assuming full name
    val harvestEndDaysAfterPlanting: Int?, // Nullable Integer

    @ColumnInfo(name = "Notes") // Assuming full name is 'Notes'
    val notes: String?,

    @ColumnInfo(name = "is_favorite") // Assuming full name is 'is_favorite'
    var isFavorite: Boolean // Room maps INTEGER (0/1) to Boolean
) {
    // Keep equals/hashCode overrides if needed, especially excluding ByteArray
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlantEntity

        if (id != other.id) return false
        if (name != other.name) return false
        // Compare other non-ByteArray fields
        if (description != other.description) return false
        if (seedingWindow != other.seedingWindow) return false
        if (indoorsWindow != other.indoorsWindow) return false
        if (transplantWindow != other.transplantWindow) return false
        if (directSowWindow != other.directSowWindow) return false
        if (harvestWindow != other.harvestWindow) return false
        if (startIndoorsDaysBeforeFrost != other.startIndoorsDaysBeforeFrost) return false
        if (transplantDaysAfterLastFrost != other.transplantDaysAfterLastFrost) return false
        if (directSowDaysAfterLastFrost != other.directSowDaysAfterLastFrost) return false
        if (harvestStartDaysAfterPlanting != other.harvestStartDaysAfterPlanting) return false
        if (harvestEndDaysAfterPlanting != other.harvestEndDaysAfterPlanting) return false
        if (notes != other.notes) return false
        if (isFavorite != other.isFavorite) return false

        // Image comparison is excluded for simplicity
        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (seedingWindow?.hashCode() ?: 0)
        result = 31 * result + (indoorsWindow?.hashCode() ?: 0)
        result = 31 * result + (transplantWindow?.hashCode() ?: 0)
        result = 31 * result + (directSowWindow?.hashCode() ?: 0)
        result = 31 * result + (harvestWindow?.hashCode() ?: 0)
        result = 31 * result + (startIndoorsDaysBeforeFrost ?: 0)
        result = 31 * result + (transplantDaysAfterLastFrost ?: 0)
        result = 31 * result + (directSowDaysAfterLastFrost ?: 0)
        result = 31 * result + (harvestStartDaysAfterPlanting ?: 0)
        result = 31 * result + (harvestEndDaysAfterPlanting ?: 0)
        result = 31 * result + (notes?.hashCode() ?: 0)
        result = 31 * result + isFavorite.hashCode()
        // Image hashcode is excluded
        return result
    }
}
