package com.jnetai.plantcare.ui.components

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jnetai.plantcare.data.entity.HealthLogEntry
import com.jnetai.plantcare.databinding.ItemHealthEntryBinding
import com.jnetai.plantcare.util.DateHelper

class HealthLogAdapter(
    private val onDelete: (HealthLogEntry) -> Unit,
    private val photoLoader: (String) -> Bitmap?
) : ListAdapter<HealthLogEntry, HealthLogAdapter.ViewHolder>(HealthLogDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHealthEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHealthEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: HealthLogEntry) {
            binding.txtHealthDate.text = DateHelper.formatDateTime(entry.date)
            binding.txtHealthNote.text = entry.note
            binding.btnDeleteEntry.setOnClickListener { onDelete(entry) }

            if (entry.photoPath.isNotBlank()) {
                val bitmap = photoLoader(entry.photoPath)
                if (bitmap != null) {
                    binding.imgHealthPhoto.setImageBitmap(bitmap)
                    binding.imgHealthPhoto.visibility = android.view.View.VISIBLE
                } else {
                    binding.imgHealthPhoto.visibility = android.view.View.GONE
                }
            } else {
                binding.imgHealthPhoto.visibility = android.view.View.GONE
            }
        }
    }

    class HealthLogDiffCallback : DiffUtil.ItemCallback<HealthLogEntry>() {
        override fun areItemsTheSame(oldItem: HealthLogEntry, newItem: HealthLogEntry): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: HealthLogEntry, newItem: HealthLogEntry): Boolean =
            oldItem == newItem
    }
}