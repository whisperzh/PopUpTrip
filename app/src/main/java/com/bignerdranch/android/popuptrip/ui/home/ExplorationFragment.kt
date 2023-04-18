package com.bignerdranch.android.popuptrip.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.bignerdranch.android.popuptrip.databinding.FragmentExplorationBinding

private const val TAG = "ExplorationFragment"
class ExplorationFragment: Fragment() {

    private var _binding: FragmentExplorationBinding? = null

    private val binding get() = _binding!!
    private val args: ExplorationFragmentArgs by navArgs()
    private var latitude = ""
    private var longitude = ""
    private var destName = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        val dest = args.destination
        Log.d(TAG, "Destination passed from home: $dest")

        //        val explorationStartDestViewModel: ExplorationStartDestViewModel by viewModels()
//        val explorationMapViewModel: ExplorationMapViewModel by viewModels()
        destName = dest.split("=")[12].split(",")[0]
        latitude = dest.split("=")[11].split(" ")[1].split(",")[0].drop(1)
        longitude = dest.split("=")[11].split(" ")[1].split(",")[1].dropLast(1)
        Log.d(TAG, "Exploration Dest: $destName")
        Log.d(TAG, "Exploration Latitude: $latitude")
        Log.d(TAG, "Exploration Longitude: $longitude")
//        explorationStartDestViewModel.destination = destName
//        explorationMapViewModel.latitude = latitude.toDouble()
//        explorationMapViewModel.longitude = longitude.toDouble()
//        explorationMapViewModel.destinationMap = destName

//        ExplorationMapFragment().getLatLong(latitude, longitude, destName)

        _binding = FragmentExplorationBinding.inflate(inflater, container, false)

        return binding.root
    }

    //    fun getLat(): Double {
//        Log.d(TAG, "Return Exploration Latitude: $latitude")
//        return if (latitude == null) {
//            -34.0
//        } else {
//            latitude.toDouble()
//        }
//    }
//
//    fun getLon(): Double {
//        Log.d(TAG, "Return Exploration Longitude: $longitude")
//        return if (longitude == null) {
//            151.0
//        } else {
//            longitude.toDouble()
//        }
//    }
//
//    fun getDest(): String {
//        Log.d(TAG, "Return Exploration Dest: $destName")
//        return if (destName == null) {
//            "Sydney"
//        } else {
//            destName
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}