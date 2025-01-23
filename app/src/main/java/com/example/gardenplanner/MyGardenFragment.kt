package com.example.gardenplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.gardenplanner.R

class MyGardenFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mygarden, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the button by its ID and set an OnClickListener
        val selectPlantsButton: Button = view.findViewById(R.id.btn_select_plants)
        selectPlantsButton.setOnClickListener {
            // Navigate to the PlantSelectionFragment
            findNavController().navigate(R.id.action_myGardenFragment_to_plantSelectionFragment)
        }
    }
}