package com.example.gardenplanner

import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.gardenplanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout with ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up BottomNavigationView
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Define top-level destinations
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_mygarden,
                R.id.navigation_settings,
                R.id.navigation_notifications,
                R.id.navigation_calendar
            )
        )

        // Link NavController with the action bar
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Explicit navigation handling for BottomNavigationView
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_mygarden -> {
                    navController.navigate(R.id.navigation_mygarden)
                    true
                }
                R.id.navigation_settings -> {
                    navController.navigate(R.id.navigation_settings)
                    true
                }
                R.id.navigation_notifications -> {
                    navController.navigate(R.id.navigation_notifications)
                    true
                }
                R.id.navigation_calendar -> {
                    navController.navigate(R.id.navigation_calendar)
                    true
                }
                else -> false
            }
        }

        // Log for debugging
        Log.d("MainActivity", "Navigation setup completed successfully.")

        // Initialize and test the database
        // initializeAndTestDatabase()
    }

    // Handle Up navigation
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
