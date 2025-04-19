package com.example.gardenplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MyGardenFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mygarden, container, false)
    }

    override fun onResume() {
        super.onResume()
        refreshUIState()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnSelectPlants: Button = view.findViewById(R.id.btn_select_plants)
        val fabAddPlant: FloatingActionButton = view.findViewById(R.id.fab_add_plant)

        btnSelectPlants.setOnClickListener {
            findNavController().navigate(R.id.action_myGarden_to_plantSelection)
        }

        fabAddPlant.setOnClickListener {
            findNavController().navigate(R.id.action_myGarden_to_plantSelection)
        }

        // Ensure the UI state is refreshed when the fragment is created
        refreshUIState()
    }

    fun refreshUIState() {
        val btnSelectPlants: Button = view?.findViewById(R.id.btn_select_plants) ?: return
        val fabAddPlant: FloatingActionButton = view?.findViewById(R.id.fab_add_plant) ?: return

        val hasFavoritedPlants = hasFavoritedPlants()

        if (hasFavoritedPlants) {
            btnSelectPlants.visibility = View.GONE
            fabAddPlant.visibility = View.VISIBLE
            loadFavoritedItemsFragment()
        } else {
            btnSelectPlants.visibility = View.VISIBLE
            fabAddPlant.visibility = View.GONE
            clearFavoritedItemsFragment()
        }
    }

    private fun hasFavoritedPlants(): Boolean {
        val dbHelper = PreloadedDatabaseHelper(requireContext())
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM MyGarden", null)

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()

        return count > 0
    }

    private fun loadFavoritedItemsFragment() {
        val favoritedItemsFragment = FavoritedItemsFragment()
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.container_favorited_items, favoritedItemsFragment)
        transaction.commit()
    }

    private fun clearFavoritedItemsFragment() {
        val favoritedItemsFragment = childFragmentManager.findFragmentById(R.id.container_favorited_items)
        favoritedItemsFragment?.let {
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            transaction.remove(it)
            transaction.commit()
        }
    }

}
