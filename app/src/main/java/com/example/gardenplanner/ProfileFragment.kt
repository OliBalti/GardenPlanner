package com.example.gardenplanner.com.example.gardenplanner

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gardenplanner.R

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val locationInput = view.findViewById<EditText>(R.id.input_location)
        val saveButton = view.findViewById<Button>(R.id.btn_save_location)

        // Load saved location (if exists) and display it
        val sharedPref = requireActivity().getSharedPreferences("GardenPlannerPrefs", Context.MODE_PRIVATE)
        val savedLocation = sharedPref.getString("location", "")
        locationInput.setText(savedLocation)

        // Save location on button click
        saveButton.setOnClickListener {
            val location = locationInput.text.toString()
            if (location.isNotBlank()) {
                // Save location in SharedPreferences
                sharedPref.edit().putString("location", location).apply()
                Toast.makeText(requireContext(), "Location saved: $location", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please enter a location", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
