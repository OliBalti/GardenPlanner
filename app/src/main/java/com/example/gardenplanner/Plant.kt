package com.example.gardenplanner

data class Plant(
    val id: Int,               // Unique ID for the plant
    val name: String,          // Name of the plant
    val image: ByteArray?,     // Image of the plant as a byte array (nullable)
    val description: String = "",  // Description of the plant (optional)
    val seedingWindow: String ="", // Seeding time window (optional)
    val startIndoors: Boolean = false, // Can it be started indoors? (true/false)
    val indoorsWindow: String ="", // Time window for starting indoors (optional)
    val transplantWindow: String ="", // Time window for transplanting (optional)
    val directSowWindow: String ="",  // Time window for direct sowing (optional)
    val harvestWindow: String =""     // Time window for harvesting (optional)
)
