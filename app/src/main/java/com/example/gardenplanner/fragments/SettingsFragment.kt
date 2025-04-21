package com.example.gardenplanner.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.gardenplanner.R

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Find the "Profile Settings" button and set a click listener
        val profileButton = view.findViewById<Button>(R.id.btn_profile)
        profileButton.setOnClickListener {
            // Navigate to ProfileFragment
            findNavController().navigate(R.id.navigation_profile)
        }

        return view
    }
}
