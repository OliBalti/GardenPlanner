package com.example.gardenplanner.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.view.Gravity
import android.graphics.drawable.ColorDrawable
import android.graphics.Color
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.gardenplanner.model.Plant
import com.example.gardenplanner.R

// Utility function to convert ByteArray to Bitmap
fun byteArrayToBitmap(byteArray: ByteArray?): Bitmap? {
    return if (byteArray != null) {
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    } else {
        null
    }
}

class PlantAdapter(
    private val plants: List<Plant>,
    private val onItemClick: (Plant) -> Unit,
    private val onAddToMyGardenClick: (Plant) -> Unit
) : RecyclerView.Adapter<PlantAdapter.PlantViewHolder>() {

    // ViewHolder class to bind plant data
    class PlantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.text_plant_name)
        val image: ImageView = itemView.findViewById(R.id.image_plant)
        val addButton: ImageButton = itemView.findViewById(R.id.btn_add_to_my_garden)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plant, parent, false)
        return PlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val plant = plants[position]
        holder.name.text = plant.name

        // Convert ByteArray to Bitmap and set to ImageView
        val bitmap = byteArrayToBitmap(plant.image)
        if (bitmap != null) {
            holder.image.setImageBitmap(bitmap)
        } else {
            // Use a placeholder image if no image is available
            holder.image.setImageResource(R.drawable.placeholder_image)
        }

        // Handle Plus button click
        holder.addButton.setOnClickListener {
            onAddToMyGardenClick(plant)
        }

        // Handle item click to show a pop-up
        holder.itemView.setOnClickListener {
            onItemClick(plant) // Pass the full Plant object to the click handler
        }

    }

    override fun getItemCount(): Int = plants.size

    // Step 2: Create a function to display a pop-up window
    private fun showPlantDetailsPopup(context: Context, plant: Plant) {
        // Inflate the popup layout
        val popupView = LayoutInflater.from(context).inflate(R.layout.popup_plant_details, null)

        // Create the popup window
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set the popup data
        val plantName: TextView = popupView.findViewById(R.id.popup_plant_name)
        val plantImage: ImageView = popupView.findViewById(R.id.popup_plant_image)
        val plantDescription: TextView = popupView.findViewById(R.id.popup_plant_description)
        val seedingWindow: TextView = popupView.findViewById(R.id.popup_plant_seeding_window)
        val harvestWindow: TextView = popupView.findViewById(R.id.popup_plant_harvest_window)

        plantName.text = plant.name
        plantImage.setImageBitmap(byteArrayToBitmap(plant.image))
        plantDescription.text = "Description: ${plant.description}"
        seedingWindow.text = "Seeding Window: ${plant.seedingWindow}"
        harvestWindow.text = "Harvest Window: ${plant.harvestWindow}"

        // Dim the background behind the popup
        val parentView = (context as AppCompatActivity).window.decorView
        val background = ColorDrawable(Color.BLACK)
        background.alpha = 160 // Adjust opacity (0-255)
        parentView.overlay.add(background)

        // Remove the dimmed background when the popup is dismissed
        popupWindow.setOnDismissListener {
            parentView.overlay.remove(background)
        }

        // Show the popup at the center
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
    }
}
