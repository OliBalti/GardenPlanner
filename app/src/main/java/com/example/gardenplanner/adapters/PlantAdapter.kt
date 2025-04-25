package com.example.gardenplanner.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gardenplanner.R
import com.example.gardenplanner.data.PlantEntity // *** Use PlantEntity ***
import com.example.gardenplanner.databinding.ItemPlantBinding // *** Import ViewBinding ***

// Utility function (can be moved to a helper file later)
fun byteArrayToBitmap(byteArray: ByteArray?): Bitmap? {
    return if (byteArray != null && byteArray.isNotEmpty()) {
        try {
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (e: Exception) {
            // Log error or return null if decoding fails
            null
        }
    } else {
        null
    }
}

/**
 * Adapter for the RecyclerView in PlantSelectionFragment.
 * Uses ListAdapter for efficient updates and displays PlantEntity data.
 *
 * @param onItemClick Lambda function to execute when an item (excluding the button) is clicked.
 * @param onToggleFavoriteClick Lambda function to execute when the favorite (+) button is clicked.
 */
class PlantAdapter(
    private val onItemClick: (PlantEntity) -> Unit,
    private val onToggleFavoriteClick: (PlantEntity) -> Unit
) : ListAdapter<PlantEntity, PlantAdapter.PlantViewHolder>(PlantDiffCallback()) { // *** Extend ListAdapter ***

    /**
     * ViewHolder class using ViewBinding.
     */
    inner class PlantViewHolder(private val binding: ItemPlantBinding) : // *** Use ViewBinding ***
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
                // Use a placeholder image if no image is available
                binding.imagePlant.setImageResource(R.drawable.placeholder_image) // Ensure placeholder exists
            }

            // Update the favorite button's appearance based on the plant's status
            updateFavoriteButton(binding.btnAddToMyGarden, plant.isFavorite)

            // Set click listener for the favorite button
            binding.btnAddToMyGarden.setOnClickListener {
                onToggleFavoriteClick(plant)
                // Optional: Update button immediately for better UX, though list update will follow
                // updateFavoriteButton(binding.btnAddToMyGarden, !plant.isFavorite)
            }

            // Set click listener for the entire item view (excluding the button)
            binding.root.setOnClickListener {
                onItemClick(plant)
            }
        }

        /**
         * Updates the visual state of the favorite button.
         * @param button The ImageButton view.
         * @param isFavorite The current favorite state of the plant.
         */
        private fun updateFavoriteButton(button: ImageButton, isFavorite: Boolean) {
            if (isFavorite) {
                // Set icon to indicate "favorited" (e.g., a filled heart or checkmark)
                button.setImageResource(R.drawable.ic_favorited) // *** Use appropriate drawable ***
                button.contentDescription = "Remove from My Garden" // Accessibility
            } else {
                // Set icon to indicate "not favorited" (e.g., plus sign or empty heart)
                button.setImageResource(R.drawable.ic_plus) // *** Use appropriate drawable ***
                button.contentDescription = "Add to My Garden" // Accessibility
            }
        }
    }

    /**
     * Creates new ViewHolders (invoked by the layout manager).
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        // Inflate the layout using ViewBinding
        val binding = ItemPlantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlantViewHolder(binding)
    }

    /**
     * Replaces the contents of a ViewHolder (invoked by the layout manager).
     */
    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val plant = getItem(position) // Get item using ListAdapter's method
        holder.bind(plant)
    }
}

/**
 * DiffUtil.ItemCallback for calculating differences between two PlantEntity lists.
 * This allows ListAdapter to perform efficient updates.
 */
class PlantDiffCallback : DiffUtil.ItemCallback<PlantEntity>() {
    /**
     * Called to check whether two items represent the same object.
     * If items have unique IDs, this check should be based on IDs.
     */
    override fun areItemsTheSame(oldItem: PlantEntity, newItem: PlantEntity): Boolean {
        return oldItem.id == newItem.id
    }

    /**
     * Called to check whether two items have the same data.
     * This check should compare all relevant fields that affect the UI representation.
     * The auto-generated equals method (excluding ByteArray) is suitable here.
     */
    override fun areContentsTheSame(oldItem: PlantEntity, newItem: PlantEntity): Boolean {
        // Use the data class's equals method (make sure it's correctly implemented,
        // especially regarding the ByteArray comparison if needed visually).
        return oldItem == newItem
    }
}
