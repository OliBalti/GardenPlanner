package com.example.gardenplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavoritedItemsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyGardenAdapter
    private lateinit var plants: MutableList<Plant> // Favorited plants

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorited_items, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view_favorited_items)

        // Fetch plants from MyGarden table
        plants = fetchMyGardenPlants()

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = MyGardenAdapter(
            plants,
            onRemoveClick = { plant -> removeFromMyGarden(plant) },
            onItemClick = { plant -> showPlantPopup(plant) }
        )
        recyclerView.adapter = adapter

        // Handle empty state
        if (plants.isEmpty()) {
            Toast.makeText(requireContext(), "No plants in My Garden", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchMyGardenPlants(): MutableList<Plant> {
        val myGardenDbHelper = PreloadedDatabaseHelper(requireContext()) // writable DB
        val originalDbHelper = OriginalDatabaseHelper(requireContext())  // read-only DB with full plant data

        val myGardenDb = myGardenDbHelper.readableDatabase
        val originalDb = originalDbHelper.readableDatabase

        val plants = mutableListOf<Plant>()

        // 1. Get all favorited plant IDs from MyGarden
        val idCursor = myGardenDb.rawQuery("SELECT id FROM MyGarden", null)
        while (idCursor.moveToNext()) {
            val plantId = idCursor.getInt(idCursor.getColumnIndexOrThrow("id"))

            // 2. Query full plant info from original database
            val detailCursor = originalDb.rawQuery(
                "SELECT id, name, image, description, seeding_window, transplant_window, harvest_window FROM Plants WHERE id = ?",
                arrayOf(plantId.toString())
            )

            if (detailCursor.moveToFirst()) {
                val name = detailCursor.getString(detailCursor.getColumnIndexOrThrow("name"))
                val image = detailCursor.getBlob(detailCursor.getColumnIndexOrThrow("image"))
                val description = detailCursor.getString(detailCursor.getColumnIndexOrThrow("description"))
                val seedingWindow = detailCursor.getString(detailCursor.getColumnIndexOrThrow("seeding_window"))
                val transplantWindow = detailCursor.getString(detailCursor.getColumnIndexOrThrow("transplant_window"))
                val harvestWindow = detailCursor.getString(detailCursor.getColumnIndexOrThrow("harvest_window"))

                plants.add(
                    Plant(
                        id = plantId,
                        name = name,
                        image = image,
                        description = description,
                        seedingWindow = seedingWindow,
                        transplantWindow = transplantWindow,
                        harvestWindow = harvestWindow
                    )
                )
            }

            detailCursor.close()
        }

        idCursor.close()
        myGardenDb.close()
        originalDb.close()

        return plants
    }



    private fun removeFromMyGarden(plant: Plant) {
        val dbHelper = PreloadedDatabaseHelper(requireContext())
        val db = dbHelper.writableDatabase
        try {
            db.execSQL("DELETE FROM MyGarden WHERE id = ?", arrayOf(plant.id))
            plants.remove(plant)
            adapter.notifyDataSetChanged()
            Toast.makeText(requireContext(), "${plant.name} removed from My Garden", Toast.LENGTH_SHORT).show()

            if (plants.isEmpty()) {
                (parentFragment as? MyGardenFragment)?.refreshUIState()
            }

        } finally {
            db.close()
        }
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

        val bitmap = byteArrayToBitmap(plant.image)
        dialogView.findViewById<ImageView>(R.id.popup_plant_image).setImageBitmap(bitmap)

        dialog.show()
    }
}
