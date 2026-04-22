package com.jnetai.plantcare.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.jnetai.plantcare.PlantCareApp
import com.jnetai.plantcare.R
import com.jnetai.plantcare.data.entity.Plant
import com.jnetai.plantcare.databinding.FragmentPlantDetailBinding
import com.jnetai.plantcare.notification.WaterReminderReceiver
import com.jnetai.plantcare.util.DateHelper
import com.jnetai.plantcare.util.PhotoHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlantDetailFragment : Fragment() {

    private var _binding: FragmentPlantDetailBinding? = null
    private val binding get() = _binding!!
    private var plant: Plant? = null
    private var plantId: Long = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlantDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        plantId = arguments?.getLong("plantId", -1) ?: -1
        if (plantId < 0) {
            findNavController().popBackStack()
            return
        }
        loadPlant()

        binding.btnWater.setOnClickListener { waterPlant() }
        binding.btnHealthLog.setOnClickListener {
            val bundle = Bundle().apply { putLong("plantId", plantId) }
            findNavController().navigate(R.id.healthLogFragment, bundle)
        }
        binding.btnEdit.setOnClickListener {
            val bundle = Bundle().apply { putLong("plantId", plantId) }
            findNavController().navigate(R.id.addEditPlantFragment, bundle)
        }
        binding.btnDelete.setOnClickListener { confirmDelete() }
    }

    private fun loadPlant() {
        lifecycleScope.launch {
            plant = withContext(Dispatchers.IO) {
                (requireActivity().application as PlantCareApp).database.plantDao().getPlantById(plantId)
            }
            plant?.let { p ->
                binding.txtName.text = p.name
                binding.txtSpecies.text = p.species.ifBlank { "Unknown species" }
                binding.txtLocation.text = if (p.location.isNotBlank()) "📍 ${p.location}" else ""
                binding.txtDatePlanted.text = if (p.datePlanted > 0)
                    "🌱 Planted ${DateHelper.format(p.datePlanted)}" else ""

                // Watering info
                binding.txtLastWatered.text = "Last watered: ${DateHelper.formatRelative(p.lastWatered)}"
                val nextWater = DateHelper.nextWaterDate(p.lastWatered, p.wateringIntervalDays)
                val isOverdue = DateHelper.isOverdue(p.lastWatered, p.wateringIntervalDays)
                binding.txtNextWater.text = if (isOverdue) {
                    "⚠️ Overdue by ${-DateHelper.daysUntil(nextWater)} days"
                } else {
                    "Next water: in ${DateHelper.daysUntil(nextWater)} days"
                }
                binding.txtWaterInterval.text = "Every ${p.wateringIntervalDays} days"

                // Sunlight
                binding.txtSunlight.text = "☀️ ${p.sunlight}"

                // Notes
                if (p.notes.isNotBlank()) {
                    binding.txtNotes.text = p.notes
                    binding.txtNotes.visibility = View.VISIBLE
                }

                // Photo
                if (p.photoPath.isNotBlank()) {
                    val bitmap = PhotoHelper.loadPhoto(p.photoPath)
                    if (bitmap != null) {
                        binding.imgPlantPhoto.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }

    private fun waterPlant() {
        plant?.let { p ->
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val db = (requireActivity().application as PlantCareApp).database
                    val updated = p.copy(lastWatered = System.currentTimeMillis())
                    db.plantDao().update(updated)
                    WaterReminderReceiver.schedule(
                        requireContext(), updated.id, updated.name, updated.wateringIntervalDays
                    )
                }
                loadPlant()
            }
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.plant_delete)
            .setMessage(R.string.plant_confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ -> deletePlant() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deletePlant() {
        plant?.let { p ->
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val db = (requireActivity().application as PlantCareApp).database
                    PhotoHelper.deletePhoto(p.photoPath)
                    db.plantDao().delete(p)
                    WaterReminderReceiver.cancel(requireContext(), p.id)
                }
                findNavController().popBackStack()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadPlant()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}