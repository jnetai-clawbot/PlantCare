package com.jnetai.plantcare.ui.screens

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.jnetai.plantcare.PlantCareApp
import com.jnetai.plantcare.R
import com.jnetai.plantcare.data.entity.Plant
import com.jnetai.plantcare.databinding.FragmentAddEditPlantBinding
import com.jnetai.plantcare.notification.WaterReminderReceiver
import com.jnetai.plantcare.util.PhotoHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AddEditPlantFragment : Fragment() {

    private var _binding: FragmentAddEditPlantBinding? = null
    private val binding get() = _binding!!
    private var plantId: Long = -1
    private var isEditing = false
    private var tempPhotoPath: String = ""
    private var selectedDatePlanted: Long = System.currentTimeMillis()

    companion object {
        private const val REQUEST_PHOTO = 2001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditPlantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sunlight dropdown
        val sunlightOptions = resources.getStringArray(R.array.sunlight_options)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sunlightOptions)
        binding.actvSunlight.setAdapter(adapter)
        binding.actvSunlight.setText(sunlightOptions[3], false) // Default: Indirect light

        // Date picker
        binding.etDatePlanted.setOnClickListener {
            showDatePicker()
        }

        // Photo picker
        binding.imgPlantPhoto.setOnClickListener {
            pickPhoto()
        }

        // Check if editing
        arguments?.getLong("plantId", -1)?.let { id ->
            if (id > 0) {
                plantId = id
                isEditing = true
                binding.btnDelete.visibility = View.VISIBLE
                loadPlant(id)
            }
        }

        // Save
        binding.btnSave.setOnClickListener { savePlant() }

        // Delete
        binding.btnDelete.setOnClickListener { confirmDelete() }
    }

    private fun loadPlant(id: Long) {
        lifecycleScope.launch {
            val plant = withContext(Dispatchers.IO) {
                (requireActivity().application as PlantCareApp).database.plantDao().getPlantById(id)
            }
            plant?.let {
                binding.etName.setText(it.name)
                binding.etSpecies.setText(it.species)
                binding.etLocation.setText(it.location)
                binding.etDatePlanted.setText(com.jnetai.plantcare.util.DateHelper.format(it.datePlanted))
                binding.etWaterInterval.setText(it.wateringIntervalDays.toString())
                binding.actvSunlight.setText(it.sunlight, false)
                binding.etNotes.setText(it.notes)
                selectedDatePlanted = it.datePlanted
                tempPhotoPath = it.photoPath
                if (it.photoPath.isNotBlank()) {
                    val bitmap = PhotoHelper.loadPhoto(it.photoPath)
                    if (bitmap != null) binding.imgPlantPhoto.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = selectedDatePlanted
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                cal.set(year, month, day)
                selectedDatePlanted = cal.timeInMillis
                binding.etDatePlanted.setText(
                    com.jnetai.plantcare.util.DateHelper.format(selectedDatePlanted)
                )
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun pickPhoto() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_PHOTO)
    }

    @Deprecated("Use Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PHOTO && resultCode == android.app.Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // Save to temp for now, will move when plant is saved
                val tempFile = java.io.File(
                    requireContext().filesDir,
                    "temp_plant_photo_${System.currentTimeMillis()}.jpg"
                )
                requireContext().contentResolver.openInputStream(uri)?.use { input ->
                    java.io.FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                tempPhotoPath = tempFile.absolutePath
                val bitmap = PhotoHelper.loadPhoto(tempPhotoPath)
                if (bitmap != null) binding.imgPlantPhoto.setImageBitmap(bitmap)
            }
        }
    }

    private fun savePlant() {
        val name = binding.etName.text.toString().trim()
        if (name.isBlank()) {
            binding.tilName.error = "Name is required"
            return
        }
        binding.tilName.error = null

        val species = binding.etSpecies.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val waterInterval = binding.etWaterInterval.text.toString().toIntOrNull() ?: 7
        val sunlight = binding.actvSunlight.text.toString()
        val notes = binding.etNotes.text.toString().trim()

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val db = (requireActivity().application as PlantCareApp).database

                if (isEditing) {
                    val existing = db.plantDao().getPlantById(plantId) ?: return@withContext
                    val updated = existing.copy(
                        name = name,
                        species = species,
                        location = location,
                        wateringIntervalDays = waterInterval,
                        sunlight = sunlight,
                        notes = notes,
                        datePlanted = selectedDatePlanted,
                        photoPath = if (tempPhotoPath != existing.photoPath) {
                            if (tempPhotoPath.isNotBlank()) {
                                PhotoHelper.copyPhotoForNewPlant(
                                    requireContext(), tempPhotoPath, plantId
                                )
                            } else {
                                PhotoHelper.deletePhoto(existing.photoPath)
                                ""
                            }
                        } else existing.photoPath
                    )
                    db.plantDao().update(updated)
                    // Reschedule reminder
                    WaterReminderReceiver.schedule(
                        requireContext(), updated.id, updated.name, updated.wateringIntervalDays
                    )
                } else {
                    val plant = Plant(
                        name = name,
                        species = species,
                        location = location,
                        wateringIntervalDays = waterInterval,
                        sunlight = sunlight,
                        notes = notes,
                        datePlanted = selectedDatePlanted,
                        lastWatered = System.currentTimeMillis()
                    )
                    val newId = db.plantDao().insert(plant)

                    // Move temp photo
                    if (tempPhotoPath.isNotBlank()) {
                        val finalPath = PhotoHelper.copyPhotoForNewPlant(
                            requireContext(), tempPhotoPath, newId
                        )
                        db.plantDao().update(plant.copy(id = newId, photoPath = finalPath))
                    }

                    // Schedule watering reminder
                    WaterReminderReceiver.schedule(
                        requireContext(), newId, name, waterInterval
                    )
                }
            }
            findNavController().popBackStack()
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
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val db = (requireActivity().application as PlantCareApp).database
                val plant = db.plantDao().getPlantById(plantId)
                if (plant != null) {
                    PhotoHelper.deletePhoto(plant.photoPath)
                    db.plantDao().delete(plant)
                    WaterReminderReceiver.cancel(requireContext(), plant.id)
                }
            }
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        // Clean up temp photo if not saved
        if (tempPhotoPath.isNotBlank() && tempPhotoPath.contains("temp_plant_photo")) {
            java.io.File(tempPhotoPath).takeIf { it.exists() }?.delete()
        }
        super.onDestroyView()
        _binding = null
    }
}