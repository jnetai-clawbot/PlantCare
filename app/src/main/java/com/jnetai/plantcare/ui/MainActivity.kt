package com.jnetai.plantcare.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.jnetai.plantcare.R
import com.jnetai.plantcare.databinding.ActivityMainBinding
import com.jnetai.plantcare.notification.NotificationHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationHelper.createChannels(this)
        requestNotificationPermission()

        val navController = findNavController(R.id.nav_host_fragment)
        binding.bottomNav.setupWithNavController(navController)

        // Track current destination to show/hide FAB
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_plants -> {
                    binding.fabAdd.show()
                    binding.fabAdd.setOnClickListener {
                        navController.navigate(R.id.addEditPlantFragment)
                    }
                }
                else -> {
                    binding.fabAdd.hide()
                }
            }
        }

        // Handle navigation to plant detail from notification
        intent?.getLongExtra("plantId", -1)?.let { plantId ->
            if (plantId > 0) {
                val bundle = Bundle().apply { putLong("plantId", plantId) }
                navController.navigate(R.id.plantDetailFragment, bundle)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Notifications won't work without permission", Toast.LENGTH_LONG).show()
        }
    }
}