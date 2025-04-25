package com.example.gardenplanner.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil // Import DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gardenplanner.data.PlantEntity // *** Use PlantEntity ***
import com.example.gardenplanner.databinding.ItemPlantMyGardenBinding // *** Import ViewBinding ***
import com.example.gardenplanner.R // Import R

/**
 * Adapter for the RecyclerView in MyGardenFragment (displaying favorite plants).
 * Uses ListAdapter for efficient updates and displays PlantEntity data.
 *
 * @param onItemClick Lambda function to execute when an item (excluding the button) is clicked.
 * @param onRemoveClick Lambda function to execute when the remove button is clicked.
 */
class MyGardenAdapter(
    private val onItemClick: (PlantEntity) -> Unit,
    private val onRemoveClick: (PlantEntity) -> Unit // Renamed for clarity (toggles favorite off)
) : ListAdapter<PlantEntity, MyGardenAdapter.PlantViewHolder>(PlantDiffCallback()) { // *** Extend ListAdapter, reuse DiffCallback ***

    /**
     * ViewHolder class using ViewBinding.
     */
    inner class PlantViewHolder(private val binding: ItemPlantMyGardenBinding) : // *** Use ViewBinding ***
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds PlantEntity data to the views.
         * @param plant The PlantEntity object for the current item.
         */
        fun bind(plant: PlantEntity) {
            binding.textPlantName.text = plant.name // Use binding reference

            // Set image using the utility function
            val bitmap = byteArrayToBitmap(plant.image)
            if (bitmap != null) {
                binding.imagePlant.setImageBitmap(bitmap) // Use binding reference
            } else {
                binding.imagePlant.setImageResource(R.drawable.placeholder_image) // Ensure placeholder exists
            }

            // Set click listener for the remove button
            // This button will now trigger the toggleFavorite function in the ViewModel
            binding.btnRemovePlant.setOnClickListener {
                onRemoveClick(plant)
            }

            // Set click listener for the entire item view
            binding.root.setOnClickListener {
                onItemClick(plant)
            }
        }
    }

    /**
     * Creates new ViewHolders (invoked by the layout manager).
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        // Inflate the layout using ViewBinding
        val binding = ItemPlantMyGardenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlantViewHolder(binding)
    }

    /**
     * Replaces the contents of a ViewHolder (invoked by the layout manager).
     */
    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val plant = getItem(position) // Get item using ListAdapter's method
        holder.bind(plant)
    }

    // NOTE: getItemCount is handled by ListAdapter automatically.
}

// We can reuse the same PlantDiffCallback defined in PlantAdapter.kt
// If you prefer, you can move PlantDiffCallback to its own file.
// class PlantDiffCallback : DiffUtil.ItemCallback<PlantEntity>() { ... }
