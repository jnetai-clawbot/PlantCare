package com.jnetai.plantcare.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jnetai.plantcare.PlantCareApp
import com.jnetai.plantcare.R
import com.jnetai.plantcare.data.entity.Plant
import com.jnetai.plantcare.databinding.FragmentPlantListBinding
import com.jnetai.plantcare.ui.components.PlantAdapter
import com.jnetai.plantcare.util.DateHelper
import com.jnetai.plantcare.util.PhotoHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlantListFragment : Fragment() {

    private var _binding: FragmentPlantListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PlantAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlantListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = PlantAdapter(
            onClick = { plant ->
                val bundle = Bundle().apply { putLong("plantId", plant.id) }
                findNavController().navigate(R.id.plantDetailFragment, bundle)
            },
            onWaterClick = { plant -> waterPlant(plant) },
            photoLoader = { path -> PhotoHelper.loadPhoto(path) }
        )
        binding.recyclerPlants.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPlants.adapter = adapter

        val db = (requireActivity().application as PlantCareApp).database
        lifecycleScope.launch {
            db.plantDao().getAllPlants().collect { plants ->
                adapter.submitList(plants)
                binding.emptyState.visibility = if (plants.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerPlants.visibility = if (plants.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun waterPlant(plant: Plant) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val db = (requireActivity().application as PlantCareApp).database
                val updated = plant.copy(lastWatered = System.currentTimeMillis())
                db.plantDao().update(updated)
                // Reschedule reminder
                com.jnetai.plantcare.notification.WaterReminderReceiver.schedule(
                    requireContext(),
                    updated.id,
                    updated.name,
                    updated.wateringIntervalDays
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}