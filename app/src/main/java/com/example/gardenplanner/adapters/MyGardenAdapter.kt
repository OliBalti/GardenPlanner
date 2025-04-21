package com.example.gardenplanner.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gardenplanner.model.Plant
import com.example.gardenplanner.R

class MyGardenAdapter(
    private val plants: List<Plant>,
    private val onRemoveClick: (Plant) -> Unit,
    private val onItemClick: (Plant) -> Unit // NEW: callback for plant popup
) : RecyclerView.Adapter<MyGardenAdapter.PlantViewHolder>() {

    inner class PlantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.text_plant_name)
        val image: ImageView = itemView.findViewById(R.id.image_plant)
        val removeButton: Button = itemView.findViewById(R.id.btn_remove_plant)

        init {
            itemView.setOnClickListener {
                val plant = plants[adapterPosition]
                onItemClick(plant) // Call the popup callback
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plant_my_garden, parent, false)
        return PlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val plant = plants[position]
        holder.name.text = plant.name

        // Convert ByteArray to Bitmap and set it to ImageView
        val bitmap = byteArrayToBitmap(plant.image)
        holder.image.setImageBitmap(bitmap)

        // Remove button logic
        holder.removeButton.setOnClickListener {
            onRemoveClick(plant)
        }
    }

    override fun getItemCount(): Int = plants.size
}
