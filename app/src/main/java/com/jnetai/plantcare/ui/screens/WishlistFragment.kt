package com.jnetai.plantcare.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jnetai.plantcare.PlantCareApp
import com.jnetai.plantcare.R
import com.jnetai.plantcare.data.entity.Plant
import com.jnetai.plantcare.data.entity.WishlistItem
import com.jnetai.plantcare.databinding.FragmentWishlistBinding
import com.jnetai.plantcare.notification.WaterReminderReceiver
import com.jnetai.plantcare.ui.components.WishlistAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WishlistFragment : Fragment() {

    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: WishlistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = WishlistAdapter(
            onMoveToPlants = { item -> moveToPlants(item) },
            onDelete = { item -> deleteItem(item) }
        )
        binding.recyclerWishlist.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerWishlist.adapter = adapter

        binding.btnAddWishlist.setOnClickListener { addWishlistItem() }

        val db = (requireActivity().application as PlantCareApp).database
        lifecycleScope.launch {
            db.wishlistDao().getAllItems().collect { items ->
                adapter.submitList(items)
                binding.txtEmptyWishlist.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerWishlist.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun addWishlistItem() {
        val name = binding.etWishlistName.text.toString().trim()
        if (name.isBlank()) {
            binding.tilWishlistName.error = "Name is required"
            return
        }
        binding.tilWishlistName.error = null

        val notes = binding.etWishlistNotes.text.toString().trim()
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val db = (requireActivity().application as PlantCareApp).database
                db.wishlistDao().insert(WishlistItem(name = name, notes = notes))
            }
            binding.etWishlistName.text?.clear()
            binding.etWishlistNotes.text?.clear()
        }
    }

    private fun moveToPlants(item: WishlistItem) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val db = (requireActivity().application as PlantCareApp).database
                val newPlant = Plant(
                    name = item.name,
                    notes = "From wishlist: ${item.notes}"
                )
                val newId = db.plantDao().insert(newPlant)
                WaterReminderReceiver.schedule(
                    requireContext(), newId, newPlant.name, newPlant.wateringIntervalDays
                )
                db.wishlistDao().delete(item)
            }
        }
    }

    private fun deleteItem(item: WishlistItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete")
            .setMessage("Remove ${item.name} from wishlist?")
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        (requireActivity().application as PlantCareApp).database.wishlistDao().delete(item)
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}