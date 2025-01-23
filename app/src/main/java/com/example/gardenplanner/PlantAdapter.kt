package com.example.gardenplanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Bitmap
import android.graphics.BitmapFactory

// Utility function to convert ByteArray to Bitmap
private fun byteArrayToBitmap(byteArray: ByteArray?): Bitmap? {
    return if (byteArray != null) {
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    } else {
        null
    }
}

class PlantAdapter(
    private val plants: List<Plant>,
    private val onItemClick: (Plant) -> Unit
) : RecyclerView.Adapter<PlantAdapter.PlantViewHolder>() {

    // ViewHolder class to bind plant data
    class PlantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.text_plant_name)
        val image: ImageView = itemView.findViewById(R.id.image_plant)
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

        // Handle item click
        holder.itemView.setOnClickListener { onItemClick(plant) }
    }

    override fun getItemCount(): Int = plants.size
}
