package com.example.gardenplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PlantSelectionFragment : Fragment() {

    private lateinit var adapter: PlantAdapter
    private lateinit var plants: MutableList<Plant> // Full list of plants
    private lateinit var filteredPlants: MutableList<Plant> // Filtered list for RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_plant_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_plants)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Query the database for plants
        val dbHelper = PreloadedDatabaseHelper(requireContext())
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, name, image, description, seeding_window, transplant_window, harvest_window FROM Plants ORDER BY name ASC",
            null
        )

        plants = mutableListOf()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val image = cursor.getBlob(cursor.getColumnIndexOrThrow("image"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val seedingWindow = cursor.getString(cursor.getColumnIndexOrThrow("seeding_window"))
            val transplantWindow = cursor.getString(cursor.getColumnIndexOrThrow("transplant_window"))
            val harvestWindow = cursor.getString(cursor.getColumnIndexOrThrow("harvest_window"))

            plants.add(
                Plant(
                    id = id,
                    name = name,
                    image = image,
                    description = description,
                    seedingWindow = seedingWindow,
                    transplantWindow = transplantWindow,
                    harvestWindow = harvestWindow
                )
            )
        }
        cursor.close()

        // Set initial filteredPlants to the full list
        filteredPlants = plants.toMutableList()

        // Initialize the adapter with the filtered list
        adapter = PlantAdapter(filteredPlants) { plant ->
            // Show popup with plant details
            showPlantPopup(plant)
        }
        recyclerView.adapter = adapter

        // Set up the SearchView
        val searchView: SearchView = view.findViewById(R.id.search_bar)
        searchView.setOnClickListener {
            searchView.isIconified = false
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle search button press if needed (optional)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filter the list as the user types
                val query = newText?.lowercase() ?: ""
                filteredPlants.clear()
                filteredPlants.addAll(
                    plants.filter { it.name.lowercase().contains(query) }
                )
                adapter.notifyDataSetChanged()
                return true
            }
        })
    }

    private fun showPlantPopup(plant: Plant) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.popup_plant_details, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Populate the popup with plant data
        dialogView.findViewById<TextView>(R.id.popup_plant_name).text = plant.name
        dialogView.findViewById<TextView>(R.id.popup_plant_description).text = plant.description
        dialogView.findViewById<TextView>(R.id.popup_plant_seeding_window).text = plant.seedingWindow
        dialogView.findViewById<TextView>(R.id.popup_plant_transplant_window).text = plant.transplantWindow
        dialogView.findViewById<TextView>(R.id.popup_plant_harvest_window).text = plant.harvestWindow

        // Convert the byte array to a bitmap for the image
        val bitmap = byteArrayToBitmap(plant.image)
        dialogView.findViewById<ImageView>(R.id.popup_plant_image).setImageBitmap(bitmap)

        dialog.show()
    }
}