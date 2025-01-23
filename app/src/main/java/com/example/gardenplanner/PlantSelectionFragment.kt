package com.example.gardenplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PlantSelectionFragment : Fragment() {

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
        val cursor = db.rawQuery("SELECT id, name, image FROM Plants", null)

        val plants = mutableListOf<Plant>()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val image = cursor.getBlob(cursor.getColumnIndexOrThrow("image"))

            plants.add(Plant(id = id, name = name, image = image))
        }
        cursor.close()

        // Set up the RecyclerView with the adapter
        val adapter = PlantAdapter(plants) { plant ->
            // Handle plant selection (highlight or save selection)
            println("Selected Plant: ${plant.name}")
        }
        recyclerView.adapter = adapter
    }
}
