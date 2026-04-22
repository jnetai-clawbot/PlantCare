package com.jnetai.plantcare.ui.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.jnetai.plantcare.BuildConfig
import com.jnetai.plantcare.R
import com.jnetai.plantcare.databinding.FragmentAboutBinding
import com.jnetai.plantcare.util.ExportHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val versionName = try {
            val pkgInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            pkgInfo.versionName ?: "1.0.0"
        } catch (e: Exception) { "1.0.0" }

        binding.txtVersion.text = getString(R.string.about_version, versionName)

        binding.btnCheckUpdates.setOnClickListener { checkForUpdates(versionName) }

        binding.btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.about_share_text))
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.about_share)))
        }

        binding.btnExport.setOnClickListener { exportData() }
    }

    private fun checkForUpdates(currentVersion: String) {
        binding.txtUpdateStatus.visibility = View.VISIBLE
        binding.txtUpdateStatus.text = "Checking..."
        binding.btnCheckUpdates.isEnabled = false

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val url = URL("https://api.github.com/repos/jnetai-clawbot/PlantCare/releases/latest")
                    val connection = url.openConnection()
                    connection.setRequestProperty("Accept", "application/vnd.github+json")
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000
                    val json = connection.getInputStream().bufferedReader().readText()
                    val obj = JSONObject(json)
                    obj.optString("tag_name", "")
                }

                if (result.isNotBlank()) {
                    val latestVersion = result.removePrefix("v")
                    if (latestVersion != currentVersion) {
                        binding.txtUpdateStatus.text = getString(R.string.about_update_available, latestVersion)
                        binding.txtUpdateStatus.setTextColor(
                            resources.getColor(R.color.md_theme_dark_primary, null)
                        )
                    } else {
                        binding.txtUpdateStatus.text = getString(R.string.about_up_to_date)
                    }
                } else {
                    binding.txtUpdateStatus.text = getString(R.string.about_up_to_date)
                }
            } catch (e: Exception) {
                binding.txtUpdateStatus.text = getString(R.string.about_update_error)
            }
            binding.btnCheckUpdates.isEnabled = true
        }
    }

    private fun exportData() {
        binding.btnExport.isEnabled = false
        lifecycleScope.launch {
            try {
                val path = ExportHelper.exportToJson(requireContext())
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, android.net.Uri.fromFile(java.io.File(path)))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, getString(R.string.export_data)))
                Toast.makeText(requireContext(), getString(R.string.export_success), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.export_error), Toast.LENGTH_SHORT).show()
            }
            binding.btnExport.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}