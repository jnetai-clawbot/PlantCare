package com.jnetai.plantcare.ui.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jnetai.plantcare.data.entity.WishlistItem
import com.jnetai.plantcare.databinding.ItemWishlistBinding

class WishlistAdapter(
    private val onMoveToPlants: (WishlistItem) -> Unit,
    private val onDelete: (WishlistItem) -> Unit
) : ListAdapter<WishlistItem, WishlistAdapter.ViewHolder>(WishlistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWishlistBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemWishlistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WishlistItem) {
            binding.txtWishlistName.text = item.name
            binding.txtWishlistNotes.text = item.notes.ifBlank { "" }
            binding.btnMoveToPlants.setOnClickListener { onMoveToPlants(item) }
            binding.btnDeleteWishlist.setOnClickListener { onDelete(item) }
        }
    }

    class WishlistDiffCallback : DiffUtil.ItemCallback<WishlistItem>() {
        override fun areItemsTheSame(oldItem: WishlistItem, newItem: WishlistItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: WishlistItem, newItem: WishlistItem): Boolean =
            oldItem == newItem
    }
}