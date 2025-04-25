package com.example.gardenplanner.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog // Use AppCompat AlertDialog
import androidx.core.view.isVisible // Extension function for visibility
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Delegate for ViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gardenplanner.R
import com.example.gardenplanner.adapters.MyGardenAdapter // Import updated adapter
import com.example.gardenplanner.adapters.byteArrayToBitmap // Import utility function
import com.example.gardenplanner.data.PlantEntity // Import Room Entity
import com.example.gardenplanner.databinding.FragmentMygardenBinding // Import ViewBinding
import com.example.gardenplanner.viewmodel.MyGardenViewModel // Import ViewModel
import kotlinx.coroutines.launch

/**
 * Fragment displaying the user's favorited plants ("My Garden").
 * Uses Room database via MyGardenViewModel and ListAdapter for the RecyclerView.
 */
class MyGardenFragment : Fragment() {

    // ViewBinding property delegate for safe access to views
    private var _binding: FragmentMygardenBinding? = null
    private val binding get() = _binding!! // Non-null assertion only between onCreateView and onDestroyView

    // Get instance of the ViewModel scoped to this Fragment's lifecycle
    private val viewModel: MyGardenViewModel by viewModels()

    // Adapter instance using the updated MyGardenAdapter (ListAdapter)
    private lateinit var myGardenAdapter: MyGardenAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using ViewBinding
        _binding = FragmentMygardenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView() // Set up the list display
        setupClickListeners() // Set up button/FAB clicks
        observeViewModel()  // Start observing data from the ViewModel
    }

    /**
     * Initializes the RecyclerView and the MyGardenAdapter.
     * The adapter is configured with callbacks for item clicks (show popup)
     * and remove clicks (toggle favorite status via ViewModel).
     */
    private fun setupRecyclerView() {
        // Initialize the adapter, providing lambdas for click handling
        myGardenAdapter = MyGardenAdapter(
            onItemClick = { plantEntity ->
                // When an item row is clicked, show the details popup
                showPlantPopup(plantEntity)
            },
            onRemoveClick = { plantEntity ->
                // When the remove button is clicked, tell the ViewModel to toggle the favorite status
                viewModel.toggleFavorite(plantEntity)
                // Provide immediate user feedback
                Toast.makeText(context, "${plantEntity.name} removed from My Garden", Toast.LENGTH_SHORT).show()
            }
        )

        // Configure the RecyclerView using the ViewBinding reference
        binding.recyclerViewMyGarden.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myGardenAdapter
            // Optional: Add ItemAnimator or ItemDecoration if desired
            // itemAnimator = DefaultItemAnimator()
            // addItemDecoration(...)
        }
        Log.d("MyGardenFragment", "RecyclerView setup complete.")
    }

    /**
     * Sets up click listeners for the empty state button and the FAB
     * to navigate to the plant selection screen.
     */
    private fun setupClickListeners() {
        binding.btnSelectPlants.setOnClickListener {
            navigateToPlantSelection()
        }
        binding.fabAddPlant.setOnClickListener {
            navigateToPlantSelection()
        }
    }

    /**
     * Navigates the user to the PlantSelectionFragment using NavController.
     * Ensure the action ID matches your navigation graph.
     */
    private fun navigateToPlantSelection() {
        // Use the navigation action defined in your res/navigation/mobile_navigation.xml
        findNavController().navigate(R.id.action_myGarden_to_plantSelection)
    }

    /**
     * Sets up observers for the StateFlows exposed by the MyGardenViewModel
     * to update the UI state (empty vs. list) and the RecyclerView list content.
     */
    private fun observeViewModel() {
        // Use viewLifecycleOwner.lifecycleScope to ensure coroutines are cancelled when the view is destroyed
        viewLifecycleOwner.lifecycleScope.launch {
            // repeatOnLifecycle ensures collection stops when the view is paused/stopped and restarts when resumed
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // --- Observe hasFavorites StateFlow ---
                // Controls the visibility of the empty state vs. the list and FAB
                launch {
                    viewModel.hasFavorites.collect { hasFavs ->
                        Log.d("MyGardenFragment", "Has Favorites state updated: $hasFavs")
                        // Use the isVisible extension property for cleaner visibility toggling
                        binding.btnSelectPlants.isVisible = !hasFavs // Show button only if NO favorites
                        binding.recyclerViewMyGarden.isVisible = hasFavs // Show list only if favorites exist
                        binding.fabAddPlant.isVisible = hasFavs // Show FAB only if favorites exist
                    }
                }

                // --- Observe favoritePlants StateFlow ---
                // Updates the list displayed in the RecyclerView
                launch {
                    viewModel.favoritePlants.collect { favoriteList ->
                        Log.d("MyGardenFragment", "Submitting ${favoriteList.size} favorites to adapter.")
                        // Submit the new list to the ListAdapter
                        // ListAdapter calculates the diff and animates changes efficiently
                        myGardenAdapter.submitList(favoriteList)
                    }
                }
            }
        }
    }

    /**
     * Shows the plant details popup using data from the PlantEntity.
     * (This logic is duplicated from PlantSelectionFragment - consider creating a shared utility function
     * or a custom DialogFragment for better code reuse later).
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
        val nameTextView: TextView = dialogView.findViewById(R.id.popup_plant_name)
        val descriptionTextView: TextView = dialogView.findViewById(R.id.popup_plant_description)
        val imageView: ImageView = dialogView.findViewById(R.id.popup_plant_image)
        // Find TextViews for the descriptive windows (assuming they exist in the layout)
        val seedingWindowTextView: TextView = dialogView.findViewById(R.id.popup_plant_seeding_window)
        val transplantWindowTextView: TextView = dialogView.findViewById(R.id.popup_plant_transplant_window)
        val harvestWindowTextView: TextView = dialogView.findViewById(R.id.popup_plant_harvest_window)
        // Add findViewById for indoors_window, direct_sow_window if they are in popup_plant_details.xml

        // Set data from PlantEntity, handling potential null values gracefully
        nameTextView.text = plant.name
        descriptionTextView.text = plant.description ?: "No description available."
        seedingWindowTextView.text = plant.seedingWindow ?: "N/A"
        transplantWindowTextView.text = plant.transplantWindow ?: "N/A"
        harvestWindowTextView.text = plant.harvestWindow ?: "N/A"
        // Set text for other windows if they exist in your layout

        // Set image using the utility function
        val bitmap = byteArrayToBitmap(plant.image)
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
        } else {
            imageView.setImageResource(R.drawable.placeholder_image) // Use placeholder
        }

        // Show the dialog
        dialog.show()
        Log.d("MyGardenFragment", "Showing popup for ${plant.name}")
    }

    /**
     * Cleans up the ViewBinding reference and RecyclerView adapter
     * when the fragment's view is destroyed to prevent memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        // Important: Clear the adapter to prevent memory leaks with RecyclerView
        binding.recyclerViewMyGarden.adapter = null
        _binding = null // Clear binding reference
        Log.d("MyGardenFragment", "View destroyed, binding cleared.")
    }
}
