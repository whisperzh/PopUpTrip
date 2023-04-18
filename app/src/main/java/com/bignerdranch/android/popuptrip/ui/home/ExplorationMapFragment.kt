package com.bignerdranch.android.popuptrip.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.databinding.FragmentExplorationMapBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

private const val TAG = "ExplorationMapFragment"

class ExplorationMapFragment: Fragment(), OnMapReadyCallback {
    private var _binding: FragmentExplorationMapBinding? = null
    private lateinit var mMap: GoogleMap
//    private val explorationMapViewModel: ExplorationMapViewModel by viewModels()
//    private var latitudeDouble = explorationMapViewModel.latitude
//    private var longitudeDouble = explorationMapViewModel.longitude
//    private var placeName = explorationMapViewModel.destinationMap

//    private var latitudeDouble = ExplorationFragment().getLat()
//    private var longitudeDouble = ExplorationFragment().getLon()
//    private var placeName = ExplorationFragment().getDest()

    private var latitudeDouble = -34.0
    private var longitudeDouble = 151.0
    private var placeName = "Boston Logan International Airport"

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExplorationMapBinding.inflate(inflater, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        Log.d(TAG, "LAT MAP: $latitudeDouble")
        Log.d(TAG, "LON MAP: $longitudeDouble")
        Log.d(TAG, "DESTINATION MAP: $placeName")
        val destinationEntered = LatLng(latitudeDouble, longitudeDouble)
        mMap.addMarker(MarkerOptions().position(destinationEntered).title(placeName))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(destinationEntered))
    }

//    fun getLatLong(latitude: String, longitude: String, destPlaceName: String) {
//        latitudeDouble = latitude.toDouble()
//        longitudeDouble = longitude.toDouble()
//        placeName = destPlaceName
//        Log.d(TAG, "LAT DOUBLE: $latitudeDouble")
//        Log.d(TAG, "LON DOUBLE: $longitudeDouble")
//        Log.d(TAG, "DESTINATION NAME: $placeName")
//    }
}