package com.jnetai.plantcare.ui.components

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jnetai.plantcare.R
import com.jnetai.plantcare.data.entity.Plant
import com.jnetai.plantcare.databinding.ItemPlantBinding
import com.jnetai.plantcare.util.DateHelper

class PlantAdapter(
    private val onClick: (Plant) -> Unit,
    private val onWaterClick: (Plant) -> Unit,
    private val photoLoader: (String) -> Bitmap?
) : ListAdapter<Plant, PlantAdapter.ViewHolder>(PlantDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlantBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemPlantBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(plant: Plant) {
            binding.txtPlantName.text = plant.name
            binding.txtPlantSpecies.text = plant.species.ifBlank { "Unknown species" }

            // Water status
            val isOverdue = DateHelper.isOverdue(plant.lastWatered, plant.wateringIntervalDays)
            val daysSinceWater = ((System.currentTimeMillis() - plant.lastWatered) / (24 * 60 * 60 * 1000)).toInt()
            binding.txtWaterStatus.text = if (isOverdue) {
                "⚠️ Needs water! (${daysSinceWater}d ago)"
            } else {
                "💧 Watered ${DateHelper.formatRelative(plant.lastWatered)}"
            }

            // Photo
            val bitmap = photoLoader(plant.photoPath)
            if (bitmap != null) {
                binding.imgPlantPhoto.setImageBitmap(bitmap)
            } else {
                binding.imgPlantPhoto.setImageResource(android.R.drawable.ic_menu_myplaces)
            }

            binding.root.setOnClickListener { onClick(plant) }
            binding.chipWater.setOnClickListener { onWaterClick(plant) }
        }
    }

    class PlantDiffCallback : DiffUtil.ItemCallback<Plant>() {
        override fun areItemsTheSame(oldItem: Plant, newItem: Plant): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Plant, newItem: Plant): Boolean =
            oldItem == newItem
    }
}