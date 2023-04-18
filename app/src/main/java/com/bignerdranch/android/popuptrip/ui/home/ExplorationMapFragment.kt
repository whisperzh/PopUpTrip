package com.bignerdranch.android.popuptrip.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.databinding.FragmentExplorationMapBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest

private const val TAG = "ExplorationMapFragment"

class ExplorationMapFragment: Fragment(), OnMapReadyCallback {
    private var _binding: FragmentExplorationMapBinding? = null
    private val explorationViewModel: ExplorationViewModel by viewModels()
    private lateinit var mMap: GoogleMap

    private lateinit var DestinationId: String
    private lateinit var DestPlace: Place
    private lateinit var StartingPointId: String
    private lateinit var StartPlace: Place

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        val fetchPlaceRequest = FetchPlaceRequest.newInstance(DestinationId, placeFields)
        Log.d(TAG, "DestinationId: $DestinationId")
        Places.createClient(requireContext()).fetchPlace(fetchPlaceRequest).addOnSuccessListener { response ->
            DestPlace = response.place
            Log.i(TAG, "Place: ${DestPlace.name}, ${DestPlace.id}, ${DestPlace.latLng}")
            mMap.addMarker(MarkerOptions().position(DestPlace.latLng).title(DestPlace.name))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DestPlace.latLng, 15f))

        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                Log.e(TAG, "Place not found: " + exception.statusCode)
            }
        }
    }
}