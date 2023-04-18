package com.bignerdranch.android.popuptrip.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.databinding.FragmentExplorationBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

private const val TAG = "ExplorationFragment"
class ExplorationFragment: Fragment(), OnMapReadyCallback {

    private var _binding: FragmentExplorationBinding? = null
    private val binding get() = _binding!!

    private val args: ExplorationFragmentArgs by navArgs()

//    private val explorationViewModel: ExplorationViewModel by viewModels()

    private lateinit var mMap: GoogleMap
    private lateinit var DestinationId: String
//    private lateinit var DestPlace: Place
//    private lateinit var StartingPointId: String
//    private lateinit var StartPlace: Place

    private lateinit var autoCompleteAdapter: PlacesAutoCompleteAdapter
    private lateinit var startingPointAddressInputEditText: TextInputEditText
    private lateinit var statingPointListView: ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        // input arguments from navigation
        DestinationId = args.destinationPlaceId
        Log.d(TAG, "Destination ID received in exploration: $DestinationId")

        _binding = FragmentExplorationBinding.inflate(inflater, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // to implement autocomplete for starting point search box
        startingPointAddressInputEditText = view.findViewById(R.id.startingTextInputTextfield)
        statingPointListView = view.findViewById(R.id.explorationStartAutoCompleteListView)

        val token = AutocompleteSessionToken.newInstance()

        startingPointAddressInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { newText ->
                    // Create a request for place predictions
                    val request = FindAutocompletePredictionsRequest.builder()
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(newText.toString())
                        .build()

                    context?.let { context ->
                        Places.createClient(context).findAutocompletePredictions(request).addOnSuccessListener { response ->
                            val predictions = response.autocompletePredictions
                            autoCompleteAdapter = PlacesAutoCompleteAdapter(context, predictions)
                            statingPointListView.adapter = autoCompleteAdapter
                            Log.i(TAG, "Visibility of listView is set to VISIBLE")
                            statingPointListView.visibility = View.VISIBLE
                        }.addOnFailureListener { _ ->
                            Log.i(TAG, "onTextChangedListener error")
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        statingPointListView.setOnItemClickListener { _, _, position, _ ->
            val selectedPrediction = autoCompleteAdapter.getItem(position)
            Log.i(TAG, "Visibility of listView is set to GONE")
            statingPointListView.visibility = View.GONE

            selectedPrediction?.placeId?.let { placeId ->

                // once a starting address is selected in the list
                val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
                val fetchPlaceRequest = FetchPlaceRequest.newInstance(placeId, placeFields)

                context?.let { context ->
                    Places.createClient(context).fetchPlace(fetchPlaceRequest).addOnSuccessListener { response ->
                        val place = response.place
                        startingPointAddressInputEditText.setText(place.name)
                        Log.i(TAG, "Starting Point Selected: ${place.name}, ${place.id}, ${place.latLng}")

                    }.addOnFailureListener { exception ->
                        if (exception is ApiException) {
                            Log.e(TAG, "Place not found: " + exception.statusCode)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // information fields we want to fetch from Google Map API
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        // create the fetch request
        val fetchPlaceRequest = FetchPlaceRequest.newInstance(DestinationId, placeFields)

        Places.createClient(requireContext()).fetchPlace(fetchPlaceRequest).addOnSuccessListener { response ->
            val destPlace = response.place
            Log.i(TAG, "Destination Place Selected: ${destPlace.name}, ${destPlace.id}, ${destPlace.latLng}")

            binding.destTextInputTextfield.setText(destPlace.name)

            mMap.addMarker(MarkerOptions().position(destPlace.latLng).title(destPlace.name))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destPlace.latLng, 15f))

        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                Log.e(TAG, "Place not found: " + exception.statusCode)
            }
        }
    }
}