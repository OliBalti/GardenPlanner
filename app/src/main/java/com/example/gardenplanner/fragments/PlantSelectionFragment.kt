package com.example.gardenplanner.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog // Use AppCompat AlertDialog
import androidx.appcompat.widget.SearchView // Use AppCompat SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Delegate for ViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gardenplanner.R
import com.example.gardenplanner.adapters.PlantAdapter // Import updated adapter
import com.example.gardenplanner.adapters.byteArrayToBitmap // Import utility function
import com.example.gardenplanner.data.PlantEntity // Import Room Entity
import com.example.gardenplanner.databinding.FragmentPlantSelectionBinding // Import ViewBinding
import com.example.gardenplanner.viewmodel.PlantSelectionViewModel // Import ViewModel
import kotlinx.coroutines.launch

class PlantSelectionFragment : Fragment() {

    private var _binding: FragmentPlantSelectionBinding? = null
    private val binding get() = _binding!! // ViewBinding property

    // Get ViewModel instance scoped to this Fragment
    private val viewModel: PlantSelectionViewModel by viewModels()

    // Declare the adapter using the updated PlantAdapter
    private lateinit var plantAdapter: PlantAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate using ViewBinding
        _binding = FragmentPlantSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        observeViewModel()
    }

    /**
     * Sets up the RecyclerView with the PlantAdapter.
     */
    private fun setupRecyclerView() {
        // Initialize the adapter, passing lambda functions for click handling
        plantAdapter = PlantAdapter(
            onItemClick = { plantEntity ->
                showPlantPopup(plantEntity) // Call local function to show popup
            },
            onToggleFavoriteClick = { plantEntity ->
                viewModel.toggleFavorite(plantEntity) // Call ViewModel to handle DB update
                // Optional: Show immediate feedback, though list will update automatically
                val message = if (!plantEntity.isFavorite) "${plantEntity.name} added to My Garden" else "${plantEntity.name} removed from My Garden"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        )

        // Set up RecyclerView using ViewBinding
        binding.recyclerViewPlants.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = plantAdapter
            // Optional: Add item decoration
            // addItemDecoration(...)
        }
        Log.d("PlantSelectionFragment", "RecyclerView setup complete.")
    }

    /**
     * Sets up the SearchView listener to update the ViewModel's query.
     */
    private fun setupSearchView() {
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Hide keyboard maybe
                binding.searchBar.clearFocus()
                return true // Indicate query handled (optional)
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Update the ViewModel's search query as the user types
                viewModel.setSearchQuery(newText.orEmpty())
                return true // Indicate query handled
            }
        })
        // Optional: Make search view expanded by default
        // binding.searchBar.isIconified = false
        // binding.searchBar.onActionViewExpanded()
    }

    /**
     * Observes the StateFlows from the ViewModel and updates the UI accordingly.
     */
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe the filtered list of plants
                viewModel.filteredPlants.collect { plants ->
                    Log.d("PlantSelectionFragment", "Submitting ${plants.size} plants to adapter.")
                    // Submit the list to the ListAdapter for efficient updates
                    plantAdapter.submitList(plants)
                }
            }
        }
        // You could also observe viewModel.searchQuery if needed for other UI updates
    }

    /**
     * Shows the plant details popup using data from the PlantEntity.
     * @param plant The PlantEntity object to display details for.
     */
    private fun showPlantPopup(plant: PlantEntity) {
        // Inflate the custom layout for the popup.
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.popup_plant_details, null) // Use your popup layout file

        // Build the AlertDialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // --- Populate the views inside the popup ---
        // Find views using findViewById on the inflated dialogView
        val nameTextView: TextView = dialogView.findViewById(R.id.popup_plant_name)
        val descriptionTextView: TextView = dialogView.findViewById(R.id.popup_plant_description)
        val imageView: ImageView = dialogView.findViewById(R.id.popup_plant_image)
        // Find TextViews for the descriptive windows (you might remove these later)
        val seedingWindowTextView: TextView = dialogView.findViewById(R.id.popup_plant_seeding_window)
        val transplantWindowTextView: TextView = dialogView.findViewById(R.id.popup_plant_transplant_window)
        val harvestWindowTextView: TextView = dialogView.findViewById(R.id.popup_plant_harvest_window)
        // Add finds for indoorsWindow, directSowWindow if they are in the layout

        // Set data from PlantEntity
        nameTextView.text = plant.name
        descriptionTextView.text = plant.description ?: "No description available." // Handle null description
        seedingWindowTextView.text = plant.seedingWindow ?: "N/A"
        transplantWindowTextView.text = plant.transplantWindow ?: "N/A"
        harvestWindowTextView.text = plant.harvestWindow ?: "N/A"
        // Set other window TextViews if needed

        // Set image
        val bitmap = byteArrayToBitmap(plant.image)
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
        } else {
            imageView.setImageResource(R.drawable.placeholder_image) // Use placeholder
        }

        // Show the dialog
        dialog.show()
        Log.d("PlantSelectionFragment", "Showing popup for ${plant.name}")
    }


    /**
     * Cleans up the binding when the view is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewPlants.adapter = null // Prevent memory leaks from adapter
        _binding = null // Clear binding reference
        Log.d("PlantSelectionFragment", "View destroyed, binding cleared.")
    }
}
