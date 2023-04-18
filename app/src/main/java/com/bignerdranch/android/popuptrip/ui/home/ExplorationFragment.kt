package com.bignerdranch.android.popuptrip.ui.home

import android.content.Context
import android.view.inputmethod.InputMethodManager
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
import com.google.android.gms.maps.model.LatLngBounds
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
    private lateinit var DestinationPlace: Place
    private lateinit var StartingPointId: String
    private lateinit var StartingPlace: Place

    // information fields we want to fetch from Google Map API
    private val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

    private lateinit var autoCompleteAdapter: PlacesAutoCompleteAdapter
    private lateinit var startingPointAddressInputEditText: TextInputEditText
    private lateinit var statingPointListView: ListView

    private var StartingPointName = ""

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
                    if (newText.toString()!=StartingPointName){
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
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        statingPointListView.setOnItemClickListener { _, _, position, _ ->
            val selectedPrediction = autoCompleteAdapter.getItem(position)
            Log.i(TAG, "Visibility of listView is set to GONE")
            statingPointListView.visibility = View.GONE

            // Hide the keyboard
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)

            // Remove cursor
//            binding.startingTextInputTextfield.isCursorVisible = false

            selectedPrediction?.placeId?.let { placeId ->

                // once a starting address is selected in the list
                StartingPointId = placeId
                val startFetchPlaceRequest = FetchPlaceRequest.newInstance(StartingPointId, placeFields)

                context?.let { context ->
                    Places.createClient(context).fetchPlace(startFetchPlaceRequest).addOnSuccessListener { response ->
                        StartingPlace = response.place
                        StartingPointName = StartingPlace.name
                        startingPointAddressInputEditText.setText(StartingPointName)
                        Log.i(TAG, "Starting Point Selected: ${StartingPlace.name}, ${StartingPlace.id}, ${StartingPlace.latLng}")

                        // Add markers of the starting point on the map
                        val mapBounds = LatLngBounds(
                            StartingPlace.latLng,
                            DestinationPlace.latLng
                        )

                        mMap.addMarker(MarkerOptions()
                            .position(StartingPlace.latLng)
                            .title(StartingPlace.name))
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(StartingPlace.latLng, 15f))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 120))

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

        // create the fetch requests
        val destFetchPlaceRequest = FetchPlaceRequest.newInstance(DestinationId, placeFields)

        // fetch destination place
        Places.createClient(requireContext()).fetchPlace(destFetchPlaceRequest).addOnSuccessListener { response ->
            DestinationPlace = response.place
            Log.i(TAG, "Destination Place Selected: ${DestinationPlace.name}, ${DestinationPlace.id}, ${DestinationPlace.latLng}")

            binding.destTextInputTextfield.setText(DestinationPlace.name)

            mMap.addMarker(MarkerOptions()
                .position(DestinationPlace.latLng)
                .title(DestinationPlace.name))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DestinationPlace.latLng, 15f))

        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                Log.e(TAG, "Place not found: " + exception.statusCode)
            }
        }
    }
}