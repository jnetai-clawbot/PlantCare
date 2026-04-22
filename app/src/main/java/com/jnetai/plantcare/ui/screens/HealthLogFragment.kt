package com.jnetai.plantcare.ui.screens

import android.app.DatePickerDialog
import android.content.Intent
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
import com.jnetai.plantcare.data.entity.HealthLogEntry
import com.jnetai.plantcare.databinding.FragmentHealthLogBinding
import com.jnetai.plantcare.ui.components.HealthLogAdapter
import com.jnetai.plantcare.util.PhotoHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class HealthLogFragment : Fragment() {

    private var _binding: FragmentHealthLogBinding? = null
    private val binding get() = _binding!!
    private var plantId: Long = -1
    private lateinit var adapter: HealthLogAdapter
    private var tempHealthPhotoPath: String = ""

    companion object {
        private const val REQUEST_HEALTH_PHOTO = 2002
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        plantId = arguments?.getLong("plantId", -1) ?: -1
        if (plantId < 0) {
            findNavController().popBackStack()
            return
        }

        adapter = HealthLogAdapter(
            onDelete = { entry -> deleteEntry(entry) },
            photoLoader = { path -> PhotoHelper.loadPhoto(path) }
        )
        binding.recyclerHealth.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHealth.adapter = adapter

        binding.btnAddPhoto.setOnClickListener { pickHealthPhoto() }
        binding.btnAddEntry.setOnClickListener { addEntry() }

        observeEntries()
    }

    private fun observeEntries() {
        val db = (requireActivity().application as PlantCareApp).database
        lifecycleScope.launch {
            db.healthLogDao().getEntriesForPlant(plantId).collect { entries ->
                adapter.submitList(entries)
            }
        }
    }

    private fun pickHealthPhoto() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        startActivityForResult(intent, REQUEST_HEALTH_PHOTO)
    }

    @Deprecated("Use Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_HEALTH_PHOTO && resultCode == android.app.Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val tempFile = File(requireContext().filesDir, "temp_health_photo_${System.currentTimeMillis()}.jpg")
                requireContext().contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output -> input.copyTo(output) }
                }
                tempHealthPhotoPath = tempFile.absolutePath
                binding.imgHealthPhotoPreview.setImageBitmap(PhotoHelper.loadPhoto(tempHealthPhotoPath))
                binding.imgHealthPhotoPreview.visibility = View.VISIBLE
            }
        }
    }

    private fun addEntry() {
        val note = binding.etHealthNote.text.toString().trim()
        if (note.isBlank()) {
            binding.tilHealthNote.error = "Add a note"
            return
        }
        binding.tilHealthNote.error = null

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val db = (requireActivity().application as PlantCareApp).database
                val entry = HealthLogEntry(
                    plantId = plantId,
                    note = note,
                    photoPath = ""
                )
                val entryId = db.healthLogDao().insert(entry)

                // Move photo if exists
                if (tempHealthPhotoPath.isNotBlank()) {
                    val finalPath = PhotoHelper.saveHealthPhoto(
                        requireContext(),
                        android.net.Uri.fromFile(File(tempHealthPhotoPath)),
                        plantId,
                        entryId
                    )
                    db.healthLogDao().insert(entry.copy(id = entryId, photoPath = finalPath))
                }
            }
            binding.etHealthNote.text?.clear()
            tempHealthPhotoPath = ""
            binding.imgHealthPhotoPreview.visibility = View.GONE
        }
    }

    private fun deleteEntry(entry: HealthLogEntry) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                PhotoHelper.deletePhoto(entry.photoPath)
                (requireActivity().application as PlantCareApp).database.healthLogDao().delete(entry)
            }
        }
    }

    override fun onDestroyView() {
        if (tempHealthPhotoPath.isNotBlank()) {
            File(tempHealthPhotoPath).takeIf { it.exists() }?.delete()
        }
        super.onDestroyView()
        _binding = null
    }
}