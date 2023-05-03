package com.bignerdranch.android.popuptrip.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.*
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color.*
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpResponse
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bignerdranch.android.popuptrip.BuildConfig.MAPS_API_KEY
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.databinding.FragmentExplorationBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.lang.Double.max
import java.lang.Double.min
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.time.LocalDateTime
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val TAG = "ExplorationFragment"
class ExplorationFragment: Fragment(), OnMapReadyCallback {

    private var _binding: FragmentExplorationBinding? = null
    private val binding get() = _binding!!

    private val args: ExplorationFragmentArgs by navArgs()

    // FOLLOWING VARIABLES NEED TO BE STORED IN VIEW MODEL
    private var mMap: GoogleMap? = null
    private lateinit var mapBounds: LatLngBounds
    private lateinit var destinationPlace : DetailedPlace
    private lateinit var startingPlace: DetailedPlace
    private var oldText: String? = null
    private lateinit var maxSWBounds: LatLng
    private lateinit var maxNEBounds: LatLng
    private var placesToAdd: ArrayList<DetailedPlace> = ArrayList() // Places selected by user to add to route
    private var markersAdded: ArrayList<Marker> = ArrayList() // Markers for recommended places
    private lateinit var polyline: Polyline
    private var startingPoint: ArrayList<Any> = ArrayList()

    // information fields we want to fetch from Google Map API
    private val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

    // autocomplete input text fields setup
    private lateinit var autoCompleteAdapter: PlacesAutoCompleteAdapter
    private lateinit var startingPointAddressInputEditText: TextInputEditText
    private lateinit var startingPointListView: ListView
    private lateinit var destinationAddressInputEditText: TextInputEditText
    private lateinit var destinationListView: ListView

    // current location button setup
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var currentLocationLatLng: LatLng
    private val permissionId = 2
    private val cultureCategories = arrayListOf<String>("art_gallery", "book_store", "library", "museum")
    private val foodCategories = arrayListOf<String>("bakery", "cafe", "restaurant")
    private val natureCategories = arrayListOf<String>("campground", "park")
    private val nightLifeCategories = arrayListOf<String>("bar", "night_club")
    private val entertainmentCategory = arrayListOf<String>("amusement_park", "aquarium", "movie_theater", "zoo")
    private val distanceRadius = 2000 // 2000m or 2km
    private val locationBias = 1000 // 1000m or 1km
    private val maxDistanceAllowed = 20000 // 20000m or 20km

    // place detail dialog setup
    private lateinit var detailedPlaceDialog: AlertDialog
    private lateinit var placeRatingBar: RatingBar
    private lateinit var placeVicinityTextView: TextView
    private lateinit var placeTypesTextView: TextView
    private lateinit var placeImageView: ImageView
    private lateinit var userSelectedPlace: DetailedPlace

    private var mapFragment: SupportMapFragment? = null
    private lateinit var explorationViewModel: ExplorationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        explorationViewModel =
            ViewModelProvider(this).get(ExplorationViewModel::class.java)

        oldText = explorationViewModel.oldText
        startingPlace = explorationViewModel.startingPlace
        currentLocationLatLng = startingPlace.placeLatLng

        mapBounds = explorationViewModel.mapBounds
        maxSWBounds = explorationViewModel.maxSWBounds
        maxNEBounds = explorationViewModel.maxNEBounds
        placesToAdd = explorationViewModel.placesToAddToRoute
        markersAdded = explorationViewModel.markersAdded
        startingPoint = explorationViewModel.startingPoint

        // input arguments from navigation
        if (args != null && explorationViewModel.needToFetch == true) {
            Log.d(TAG, "Given arguments from homepage")
            destinationPlace = DetailedPlace(args.destinationPlaceId)
            explorationViewModel.updateDestinationPlace(destinationPlace)
            Log.d(TAG, "OnCreateView called! Destination ID received in exploration: ${destinationPlace.placeId}")
//            destinationId = args.destinationPlaceId
        } else {
            destinationPlace = explorationViewModel.destinationPlace
            Log.d(TAG, "Given information from viewModel, placename: ${destinationPlace.placeName}")
        }

        _binding = FragmentExplorationBinding.inflate(inflater, container, false)

        // inflate Place Detailed Dialog
        val detailedPlaceDialogLayout = LayoutInflater.from(requireContext()).inflate(R.layout.detailed_place_dialog, null)

        placeRatingBar = detailedPlaceDialogLayout.findViewById<RatingBar>(R.id.detailed_place_dialog_rating)
        placeVicinityTextView = detailedPlaceDialogLayout.findViewById<TextView>(R.id.detailed_place_dialog_vicinity)
        placeImageView = detailedPlaceDialogLayout.findViewById<ImageView>(R.id.detailed_place_dialog_img)
        placeTypesTextView = detailedPlaceDialogLayout.findViewById(R.id.detailed_place_dialog_types)

        detailedPlaceDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(detailedPlaceDialogLayout)
            .setPositiveButton(R.string.detailed_place_dialog_add_button, null)
            .setNeutralButton(R.string.back_button) { _, _ ->
                // Handle negative button click
            }
            .create()

        // outside click does not close the dialog
        detailedPlaceDialog.setCanceledOnTouchOutside(false)

        Log.d(TAG, "Recreating map in onCreateView")

        // launch the support map fragment
//        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)

        // Re-create the map e.g. if user switches from exploration to another tab
        // e.g. Personal Profile, and back to exploration by clicking the Home tab
        Log.d(TAG, "Launch support map fragment")
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance()
            childFragmentManager.beginTransaction().replace(R.id.map, mapFragment!!).commit()
        }
        mapFragment!!.getMapAsync(this)

        fusedLocationClient = activity?.let {
            LocationServices
                .getFusedLocationProviderClient(
                    it
                )
        }
        val fusedLocationClientIsNull = (fusedLocationClient == null)
        Log.d(TAG, "Fused Location Client null?: $fusedLocationClientIsNull")

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        val mapFragmentNull = (mapFragment == null)
        Log.d(TAG, "mapFragment is null? $mapFragmentNull")
//        mapFragment?.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // to implement autocomplete for starting point search box
        startingPointAddressInputEditText = view.findViewById(R.id.startingTextInputTextfield)
        startingPointListView = view.findViewById(R.id.explorationStartAutoCompleteListView)

        val token = AutocompleteSessionToken.newInstance()

        startingPointAddressInputEditText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { newText ->
                    Log.d(TAG, "New Text: $newText")
                    if (oldText == null) {
                        oldText = newText.toString()
                    }
                    Log.d(TAG, "starting point onTextChanged is triggered")
                    if (newText.toString() != startingPlace.placeName && newText.toString() != getString(R.string.current_location_title)) {
                        if (newText.toString() != "") {
                            mMap?.clear()
                        }
                        // Create a request for place predictions
                        Log.d(TAG, "Create request for place predictions")
                        val request = FindAutocompletePredictionsRequest.builder()
                            .setTypeFilter(TypeFilter.ADDRESS)
                            .setSessionToken(token)
                            .setQuery(newText.toString())
                            .build()

                        context?.let { context ->
                            Places.createClient(context).findAutocompletePredictions(request).addOnSuccessListener { response ->
                                val predictions = response.autocompletePredictions
                                autoCompleteAdapter = PlacesAutoCompleteAdapter(context, predictions)
                                startingPointListView.adapter = autoCompleteAdapter
                                Log.i(TAG, "Visibility of listView is set to VISIBLE")
                                startingPointListView.visibility = View.VISIBLE
                            }.addOnFailureListener { _ ->
                                Log.i(TAG, "onTextChangedListener error")
                            }
                        }
                        oldText = newText.toString()
                    } else if (newText.toString() == getString(R.string.current_location_title) && startingPlace.placeName != "" && startingPlace.placeName != newText.toString()) {
                        Log.d(TAG, "Start point changed to current location")
//                        polyline.remove()
                        mMap?.clear()
                        startingPlace = DetailedPlace()
                        startingPlace.placeName = getString(R.string.current_location_title)
                        startingPlace.placeLatLng = currentLocationLatLng
                        explorationViewModel.updateStartingPlace(startingPlace)

                        getLocation()
                        getDirections()
                        markDestination()
                        markStartingLocation(getString(R.string.current_location_title))
                        oldText = newText.toString()
                    } else if (newText.toString() != oldText) {
                        Log.d(TAG, "New Text not the same as Old Text")

                    } else if (newText.toString() == getString(R.string.current_location_title) && startingPlace.placeName == "") {
                        Log.d(TAG, "Start point set to current location")
//                        mMap.clear()
                        startingPlace = DetailedPlace()
                        startingPlace.placeName = getString(R.string.current_location_title)
                        startingPlace.placeLatLng = currentLocationLatLng
                        explorationViewModel.updateStartingPlace(startingPlace)

                        getLocation()
                        getDirections()
                        markDestination()
                        markStartingLocation(getString(R.string.current_location_title))
                        oldText = newText.toString()
                    } else {
                        Log.d(TAG, "New start is same as current")
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                Log.d(TAG, "After starting point text changed")
            }
        })

        // starting point selected from the list
        startingPointListView.setOnItemClickListener { _, _, position, _ ->

            Log.d(TAG, "starting point setOnItemClickListener is triggered")
            // to clear any previously selected locations
            mMap?.clear()
            markDestination()
            val selectedPrediction = autoCompleteAdapter.getItem(position)
            Log.i(TAG, "Visibility of listView is set to GONE")
            startingPointListView.visibility = View.GONE

            // Hide the keyboard
            val imm = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)

            // Remove cursor
//            binding.startingTextInputTextfield.isCursorVisible = false

            selectedPrediction?.placeId?.let { placeId ->

                // once a starting address is selected in the list
                startingPlace = DetailedPlace(placeId)
                Log.d(TAG, "Starting point ID: ${startingPlace.placeId}")
                val startFetchPlaceRequest = FetchPlaceRequest.newInstance(placeId, placeFields)

                context?.let { context ->
                    Places.createClient(context).fetchPlace(startFetchPlaceRequest).addOnSuccessListener { response ->
                        val temp = response.place
                        startingPlace.placeName = temp.name
                        startingPlace.placeLatLng = temp.latLng

                        currentLocationLatLng = startingPlace.placeLatLng
                        explorationViewModel.updateStartingPlace(startingPlace)

                        startingPoint.clear()
                        startingPoint.add(startingPlace.placeName)
                        startingPoint.add(startingPlace.placeLatLng)
                        explorationViewModel.startingPoint = startingPoint
                        Log.d(TAG, "Starting Point at specified location: $startingPoint")
                        startingPointAddressInputEditText.setText(startingPlace.placeName)
                        Log.d(TAG, "Starting Place selected in searchbox list: ${startingPlace.placeName}")
                        markStartingLocation(startingPlace.placeName)

                        getDirections()

                    }.addOnFailureListener { exception ->
                        if (exception is ApiException) {
                            Log.e(TAG, "Place not found: " + exception.statusCode)
                        }
                    }
                }
            }
        }

        // to implement autocomplete for destination search box
        destinationAddressInputEditText = view.findViewById(R.id.destTextInputTextfield)
        destinationListView = view.findViewById(R.id.explorationDestAutoCompleteListView)

        destinationAddressInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { newText ->
                    Log.d(TAG, "text inside the destination searchbox: $newText")
                    Log.d(TAG, "text from destinationName: ${destinationPlace.placeName}")
                    if (newText.toString() != destinationPlace.placeName){
                        Log.d(TAG, "destinationName: $destinationPlace.placeName")
                        Log.d(TAG, "new string entered: ${newText.toString()}")
                        Log.d(TAG, "destination onTextChanged is triggered")
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
                                destinationListView.adapter = autoCompleteAdapter
                                if (newText.toString() != destinationPlace.placeName){
                                    Log.i(TAG, "Visibility of destination listView is set to VISIBLE")
                                    destinationListView.visibility = View.VISIBLE
                                }
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

        // destination selected from the list
        destinationListView.setOnItemClickListener { _, _, position, _ ->

            val selectedPrediction = autoCompleteAdapter.getItem(position)
            Log.i(TAG, "Visibility of listView is set to GONE")
            destinationListView.visibility = View.GONE

            // Hide the keyboard
            val imm = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)

            selectedPrediction?.placeId?.let { placeId ->

                // once a new destination is selected in the list
                if(placeId != destinationPlace.placeId){
                    Log.d(TAG, "if statement in setOnItemClickListener for destination is triggered $placeId")
                    // create empty DetailedPlace to store new destination place information
                    destinationPlace = DetailedPlace(placeId)
                    Log.d(TAG, "LINE436 DestinationId is: ${destinationPlace.placeId}")
                    val destFetchPlaceRequest = FetchPlaceRequest.newInstance(destinationPlace.placeId, placeFields)

                    context?.let { context ->
                        Places.createClient(context).fetchPlace(destFetchPlaceRequest).addOnSuccessListener { response ->
                            val tempPlace = response.place
                            destinationPlace = DetailedPlace(placeId)
                            destinationPlace.placeName = tempPlace.name
                            destinationPlace.placeLatLng = tempPlace.latLng

                            destinationAddressInputEditText.setText(destinationPlace.placeName)

                            explorationViewModel.updateDestinationPlace(destinationPlace)
                            Log.d(TAG, "new destination place in viewModel: ${destinationPlace.placeName}, ${destinationPlace.placeId}, ${destinationPlace.placeLatLng}")

                            // to clear any previously selected locations
                            mMap?.clear()
                            Log.d(TAG, "On destination selected")

                            markDestination()

                            Log.d(TAG, "starting place name: ${startingPlace.placeName}")
                            // if starting place is initialized
                            if (startingPlace.placeName!=""){
                                Log.d(TAG, "currentLocationLatLng initialized to $currentLocationLatLng")
                                markStartingLocation(startingPlace.placeName)
                                // resize map bounds and draw the route
                                getDirections()
                            // starting place is not initialized, only plot destination
                            } else {
                                mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationPlace.placeLatLng, 15f))
                            }

                        }.addOnFailureListener { exception ->
                            if (exception is ApiException) {
                                Log.e(TAG, "Place not found: " + exception.statusCode)
                            }
                        }
                    }
                } else {
                    destinationAddressInputEditText.setText(destinationPlace.placeName)
                }
            }
        }

        // current location button implementation
        binding.useCurrentLocationButton.setOnClickListener{
            Log.d(TAG, "Current Location selected")
            // Hide the keyboard
            val imm = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
//            if (this::mapBounds.isInitialized){
//                resizeMapView()
//            }
            // to clear any previously selected locations
//            mMap.clear()
            getLocation()
        }

        binding.adjustMapBoundButton.setOnClickListener{
            resizeMapView()
        }

        binding.explorationBackButton.setOnClickListener {
            // empty out the starting point
            explorationViewModel.resetStartingPlace()
            val home = ExplorationFragmentDirections.explorationToHomeAction()
                .setDestinationPlaceName(destinationPlace.placeName)
//            // Launch navigation to home page
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    findNavController().navigate(home)
                }
            }
        }

        detailedPlaceDialog.setOnShowListener{
            val positiveButton = detailedPlaceDialog.getButton(AlertDialog.BUTTON_POSITIVE)

            if(!userSelectedPlace.addedToPlan){
                positiveButton.setText(R.string.detailed_place_dialog_add_button)
                positiveButton.setOnClickListener {
                    // Add the place to list of places to visit
                    userSelectedPlace.addedToPlan = true
                    addPlaceToRoute(userSelectedPlace)
                    detailedPlaceDialog.dismiss()
                }
            } else {
                positiveButton.setText(R.string.detailed_place_dialog_remove_button)
                positiveButton.setOnClickListener {
                    // Add the place to list of places to visit
                    userSelectedPlace.addedToPlan = false
                    removePlaceFromRoute(userSelectedPlace)
                    detailedPlaceDialog.dismiss()
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

        if(explorationViewModel.needToFetch == true){
            // create the fetch requests
            explorationViewModel.needToFetch = false
            Log.d(TAG, "Navigated from homepage, need to fetch destination")
            val destFetchPlaceRequest = FetchPlaceRequest.newInstance(destinationPlace.placeId, placeFields)

            // fetch destination place
            Places.createClient(requireContext()).fetchPlace(destFetchPlaceRequest).addOnSuccessListener { response ->
                val temp = response.place
                destinationPlace = DetailedPlace(destinationPlace.placeId)
                destinationPlace.placeName = temp.name
                destinationPlace.placeLatLng = temp.latLng
                explorationViewModel.updateDestinationPlace(destinationPlace)
                Log.d(TAG, "Destination Place: ${destinationPlace.placeName}")

                binding.destTextInputTextfield.setText(destinationPlace.placeName)

                markDestination()
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationPlace.placeLatLng, 15f))

            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: " + exception.statusCode)
                }
            }
        } else {
            // re-plotting destination and starting point after switching between tabs
            Log.d(TAG, "destination for replotting: ${destinationPlace.placeName}, ${destinationPlace.placeId}, ${destinationPlace.placeLatLng}")
            markDestination()
            Log.d(TAG, "starting place name to replot: ${startingPlace.placeName}")
            if(startingPlace.placeName!=""){
                Log.d(TAG, "starting place is replotted")
                markStartingLocation(startingPlace.placeName)
            }
        }

        setupMarkerClickListener(mMap!!)

        // Re-plot the markers (used when toggling away then coming back to Exploration page)
        Log.d(TAG, "Checking if anything to re-plot onMapReady")
        for (i in 0 until markersAdded.size) {
            Log.d(TAG, "Re-plotting markers in onMapReady")
            val marker: Marker = markersAdded[i]
            var markerTag: DetailedPlace = marker.tag as DetailedPlace
            val position: LatLng = marker.position
            if (placesToAdd.size > 0) {
                for (j in 0 until placesToAdd.size) {
                    val detailedPlace: DetailedPlace = placesToAdd[j]

                    if (detailedPlace.placeLatLng == position) {
                        val markerColor = when (detailedPlace.placeCategory) {
                            getString(R.string.category_title_entertainment) -> {
                                BitmapDescriptorFactory.HUE_ROSE
                            }
                            getString(R.string.category_title_culture) -> {
                                BitmapDescriptorFactory.HUE_BLUE
                            }
                            getString(R.string.category_title_food) -> {
                                BitmapDescriptorFactory.HUE_ORANGE
                            }
                            getString(R.string.category_title_nature) -> {
                                BitmapDescriptorFactory.HUE_GREEN
                            }
                            else -> { // Nightlife
                                BitmapDescriptorFactory.HUE_VIOLET
                            }
                        }
                        markersAdded.removeAt(i)
//                        marker.remove()
                        Log.d(TAG, "Place Category: ${detailedPlace.placeCategory}")
                        val updatedMarker = mMap!!.addMarker(MarkerOptions()
                            .position(position)
                            .title(detailedPlace.placeName)
                            .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                        )

                        if (updatedMarker != null) {
                            updatedMarker.tag = detailedPlace
                            markersAdded.add(i, updatedMarker)
                        }

                        break
                    }

                    // Not selected by user to be in the route
                    if (j == placesToAdd.size - 1) {
                        val markerColor = when (markerTag.placeCategory) {
                            getString(R.string.category_title_entertainment) -> {
                                BitmapDescriptorFactory.HUE_ROSE
                            }
                            getString(R.string.category_title_culture) -> {
                                BitmapDescriptorFactory.HUE_BLUE
                            }
                            getString(R.string.category_title_food) -> {
                                BitmapDescriptorFactory.HUE_ORANGE
                            }
                            getString(R.string.category_title_nature) -> {
                                BitmapDescriptorFactory.HUE_GREEN
                            }
                            else -> { // Nightlife
                                BitmapDescriptorFactory.HUE_VIOLET
                            }
                        }
                        markersAdded.removeAt(i)
//                        marker.remove()
                        Log.d(TAG, "Place Category (not selected by user): ${detailedPlace.placeCategory}")
                        val updatedMarker = mMap!!.addMarker(MarkerOptions()
                            .position(position)
                            .title(detailedPlace.placeName)
                            .alpha(0.6f)
                            .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                        )

                        if (updatedMarker != null) {
                            updatedMarker.tag = markerTag
                            markersAdded.add(i, updatedMarker)
                        }
                    }
                }
            } else {
                val markerTitle = marker.title
                markerTag = marker.tag as DetailedPlace
                val markerColor = when (markerTag.placeCategory) {
                    getString(R.string.category_title_entertainment) -> {
                        BitmapDescriptorFactory.HUE_ROSE
                    }
                    getString(R.string.category_title_culture) -> {
                        BitmapDescriptorFactory.HUE_BLUE
                    }
                    getString(R.string.category_title_food) -> {
                        BitmapDescriptorFactory.HUE_ORANGE
                    }
                    getString(R.string.category_title_nature) -> {
                        BitmapDescriptorFactory.HUE_GREEN
                    }
                    else -> { // Nightlife
                        BitmapDescriptorFactory.HUE_VIOLET
                    }
                }
                markersAdded.removeAt(i)
//                marker.remove()
                val updatedMarker = mMap!!.addMarker(MarkerOptions()
                    .position(position)
                    .title(markerTitle)
                    .alpha(0.6f)
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                )

                if (updatedMarker != null) {
                    updatedMarker.tag = markerTag
                    markersAdded.add(i, updatedMarker)
                }
            }
        }
        explorationViewModel.markersAdded = markersAdded
    }

    // The following 5 functions pertaining to getting the user's current location is obtained from
    // https://techpassmaster.com/get-current-location-in-android-studio-using-kotlin/
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            activity?.getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (activity?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }
    private fun requestPermissions() {
        activity?.let {
            ActivityCompat.requestPermissions(
                it,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                permissionId
            )
        }
    }
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        Log.d(TAG, "getLocation() is called")
        if (checkPermissions()) {
            Log.d(TAG, "Check Permission success")
            if (isLocationEnabled()) {
                Log.d(TAG, "Location is enabled")
                activity?.let {
                    Log.d(TAG, "Getting Current Location")
                    val fusedLocationClientIsNull = (fusedLocationClient == null)
                    Log.d(TAG, "Fused Location Client null?: $fusedLocationClientIsNull")
                    fusedLocationClient?.getCurrentLocation(PRIORITY_BALANCED_POWER_ACCURACY, object : CancellationToken() {
                        override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token

                        override fun isCancellationRequested() = false
                    })?.addOnSuccessListener { currentLocation: Location? ->
                        if (currentLocation == null)
                            Toast.makeText(activity, "Cannot get current location.", Toast.LENGTH_LONG).show()
                        else {
                            Log.d(TAG, "Current Latitude: " + (currentLocation).latitude)
                            Log.d(TAG, "Current Longitude: " + (currentLocation).longitude)
                            currentLocationLatLng = LatLng((currentLocation).latitude, (currentLocation).longitude)

                            binding.startingTextInputTextfield.setText(getString(R.string.current_location_title))
                            startingPoint.clear()
                            startingPoint.add(getString(R.string.current_location_title))
                            startingPoint.add(currentLocationLatLng)
                            explorationViewModel.startingPoint = startingPoint
                            Log.d(TAG, "Starting Point at Current Location: $startingPoint")
                        }
                    }
                }
            } else {
                Toast.makeText(activity, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun getSWBound(StartLatLng: LatLng, DestLatLng: LatLng): LatLng{
        val sBound = min(StartLatLng.latitude, DestLatLng.latitude)
        val wBound = min(StartLatLng.longitude, DestLatLng.longitude)
        return LatLng(sBound, wBound)
    }

    private fun getNEBound(StartLatLng: LatLng, DestLatLng: LatLng): LatLng{
        val nBound = max(StartLatLng.latitude, DestLatLng.latitude)
        val eBound = max(StartLatLng.longitude, DestLatLng.longitude)
        return LatLng(nBound, eBound)
    }

    // get route from startingPlace to destinationPlace
    private fun getDirections() {
        Log.d(TAG, "check currentLationLatLng is null?")
        if (!this::currentLocationLatLng.isInitialized) {
            Log.d(TAG, "Current location is null")
            getLocation()
        }
        Log.d(TAG, "getDirections() is called")
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val travelModes = listOf("WALKING", "TRANSIT", "DRIVING", "BICYCLING")
        val travelModeInt = prefs.getInt("SpinnerPosition", 2)
        val travelMode = travelModes[travelModeInt]

        var coordinates = arrayListOf<LatLng>()
        Log.d(TAG, "Travel Mode: $travelMode")
        val path: MutableList<List<LatLng>> = ArrayList()
        val distance = haversineDistance(currentLocationLatLng, destinationPlace.placeLatLng)
        Log.d(TAG, "Distance is: $distance")
        if (distance > maxDistanceAllowed) {
            Toast.makeText(
                context,
                "Distance between start point and destination is more than 20km!",
                Toast.LENGTH_LONG
            ).show()
            binding.startingTextInputTextfield.setText("")
            explorationViewModel.resetStartingPlace()
            startingPlace = explorationViewModel.startingPlace
            mMap?.clear()
            markDestination()
        } else {
            val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                    currentLocationLatLng.latitude.toString() + "," +
                    currentLocationLatLng.longitude.toString() +
                    "&destination=" + destinationPlace.placeLatLng.latitude.toString() + "," +
                    destinationPlace.placeLatLng.longitude.toString() +
                    "&mode=" + travelMode.lowercase() +
                    "&key=" + MAPS_API_KEY

            // Adapted from https://lwgmnz.me/google-maps-and-directions-api-using-kotlin/
            val directionsRequest =
                object : StringRequest(Method.GET, urlDirections, Response.Listener { response ->
                    val jsonResponse = JSONObject(response)
                    Log.d(TAG, "Response: $jsonResponse")
                    val status = jsonResponse.getString("status")
                    if (status == "ZERO_RESULTS") {
                        Toast.makeText(context, "No directions found!", Toast.LENGTH_LONG).show()
                    } else {
                        // Get routes
                        Log.d(TAG, "Plotting new directions")
                        val routes = jsonResponse.getJSONArray("routes")
                        val legs = routes.getJSONObject(0).getJSONArray("legs")
                        val steps = legs.getJSONObject(0).getJSONArray("steps")
                        for (i in 0 until steps.length()) {
                            val points =
                                steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                            //                Log.d(TAG, PolyUtil.decode(points).toString())
                            path.add(PolyUtil.decode(points))
                        }
                        // get the max SW and NE bounds so the map is zoomed out to the point where
                        // the route can be seen without having to manually move the map around
                        maxSWBounds =
                            getSWBound(currentLocationLatLng, destinationPlace.placeLatLng)
                        maxNEBounds =
                            getNEBound(currentLocationLatLng, destinationPlace.placeLatLng)

                        var point = path[0][0]
                        coordinates.add(path[0][0])
                        for (i in 0 until path.size) {
//                        Log.d(TAG, "Path $i: " + path[i].toString())
                            polyline =
                                mMap!!.addPolyline(PolylineOptions().addAll(path[i]).color(BLUE))
                            Log.d(TAG, "Polyline: $polyline")

                            // modify map bounds to include the route
                            for (j in 0 until path[i].size) {
//                            Log.d(TAG, "Path $i $j: " + path[i][j].toString())
                                maxSWBounds = getSWBound(maxSWBounds, path[i][j])
                                maxNEBounds = getNEBound(maxNEBounds, path[i][j])
                                val distance = haversineDistance(point, path[i][j])
                                if (distance >= distanceRadius) {
                                    coordinates.add(path[i][j])
                                    point = path[i][j]
                                }
                            }
                        }
                        Log.d(TAG, "coordinates are: $coordinates")
                        explorationViewModel.maxSWBounds = maxSWBounds
                        explorationViewModel.maxNEBounds = maxNEBounds
                        mapBounds = LatLngBounds(maxSWBounds, maxNEBounds)
                        explorationViewModel.mapBounds = mapBounds
                        mMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 240))
                        getRecommendations(coordinates)
                    }
                }, Response.ErrorListener { _ ->
                }) {}
            val requestQueue = Volley.newRequestQueue(activity)
            requestQueue.add(directionsRequest)
        }
    }

    // get recommended places based on user's preferences set in Personal Profile
    private fun getRecommendations(coordinates: ArrayList<LatLng>) {
        val placeTypes = arrayListOf<String>()
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val userChoiceFood = prefs.getString("food_selection", "")
        val userChoiceEntertainment = prefs.getString("enter_selection", "")
        val userChoiceCulture = prefs.getString("culture_selection", "")
        val userChoiceNature = prefs.getString("nature_selection", "")
        val userChoiceNightlife = prefs.getString("nightlife_selection", "")

        if (userChoiceFood != "" && userChoiceFood != null) {
            val array = userChoiceFood.split(",")

            for (element in array) {
                val value = element.lowercase().replace(" ", "_")
                placeTypes.add(value)
            }
        }

        if (userChoiceEntertainment != "" && userChoiceEntertainment != null) {
            val array = userChoiceEntertainment.split(",")

            for (element in array) {
                val value = element.lowercase().replace(" ", "_")
                placeTypes.add(value)
            }
        }

        if (userChoiceCulture != "" && userChoiceCulture != null) {
            val array = userChoiceCulture.split(",")

            for (element in array) {
                val value = element.lowercase().replace(" ", "_")
                placeTypes.add(value)
            }
        }

        if (userChoiceNature != "" && userChoiceNature != null) {
            val array = userChoiceNature.split(",")

            for (element in array) {
                val value = element.lowercase().replace(" ", "_")
                placeTypes.add(value)
            }
        }

        if (userChoiceNightlife != "" && userChoiceNightlife != null) {
            val array = userChoiceNightlife.split(",")

            for (element in array) {
                val value = element.lowercase().replace(" ", "_")
                placeTypes.add(value)
            }
        }

        if (placeTypes == null || placeTypes.size == 0) {
            placeTypes.addAll(entertainmentCategory)
            placeTypes.addAll(cultureCategories)
            placeTypes.addAll(foodCategories)
            placeTypes.addAll(natureCategories)
            placeTypes.addAll(nightLifeCategories)
        }

        Log.d(TAG, "Place Types: $placeTypes")
        markersAdded.clear()

        for (i in 0 until coordinates.size) {
            for (j in 0 until placeTypes.size) {
                var inputText = placeTypes[j]
                if (inputText.contains("_")) {
                    inputText = inputText.replace("_", " ")
                }

                val urlRecommendation = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json" +
                        "?fields=formatted_address%2Cname%2Cphoto%2Cplace_id%2Cgeometry%2Crating%2Copening_hours" +
                        "&input=" + inputText + "&inputtype=textquery" +
                        "&type=" + placeTypes[j] +
                        "&locationbias=circle%3A" + locationBias + "%40" +
                        coordinates[i].latitude.toString() + "%2C" + coordinates[i].longitude.toString() +
                        "&key=" + MAPS_API_KEY

                val recommendationsRequest =
                    object : StringRequest(Method.GET, urlRecommendation, Response.Listener { response ->
                        val jsonResponse = JSONObject(response)
                        val placesReturned = arrayListOf<LatLng>()
//                        Log.d(TAG, "Places Response: $jsonResponse")
                        val status = jsonResponse.getString("status")
                        if (status == "ZERO_RESULTS") {
                            Log.d(TAG, "No recommendations for " + placeTypes[j] + " found at " + coordinates[i])
                        } else {
                            // Get places
                            Log.d(TAG, "Get " + placeTypes[j] + " places")
//                            Log.d(TAG, placeTypes[j] + " Response at " + coordinates[i] + ": $jsonResponse")
                            val results: JSONArray = jsonResponse.getJSONArray("candidates")
                            Log.d(TAG, "There is/are ${results.length()} results(s)")
                            Log.d(TAG, placeTypes[j] + " Results at " + coordinates[i] + ": $results")

                            for (k in 0 until results.length()) {
                                Log.d(TAG,  "$k: ${results[k]::class.java.typeName}" + results[k])
                                val resultObject: JSONObject = results[k] as JSONObject
                                Log.d(TAG, "returned JSON object: $resultObject")

                                val placeId = resultObject.getString("place_id")
                                val placeName = resultObject.getString("name")
                                val geometry = resultObject.getJSONObject("geometry")
                                val location = geometry.getJSONObject("location")
                                val placeLatLng = LatLng(location.getDouble("lat"), location.getDouble("lng"))
                                val placeRating = resultObject.getString("rating").toFloat()
                                val placeAddress = resultObject.getString("formatted_address")

                                val photo = resultObject.optJSONArray("photos")
                                val photoReference: String? = if (photo != null && photo.length() > 0) {
                                    val photoObject = photo.getJSONObject(0)
                                    photoObject.optString("photo_reference", null)
                                } else {
                                    null
                                }
                                val placeOpeningHours = resultObject.optJSONObject("opening_hours")
                                val placeOpenNow: Boolean? =
                                    placeOpeningHours?.getString("open_now")?.toBoolean()

                                val placeCategory = if (placeTypes[j] in entertainmentCategory) {
                                    getString(R.string.category_title_entertainment)
                                } else if (placeTypes[j] in foodCategories) {
                                    getString(R.string.category_title_food)
                                } else if (placeTypes[j] in cultureCategories) {
                                    getString(R.string.category_title_culture)
                                } else if (placeTypes[j] in natureCategories) {
                                    getString(R.string.category_title_nature)
                                } else {
                                    getString(R.string.category_title_nightlife)
                                }

                                val placeToMark = DetailedPlace(placeId, placeLatLng, placeName, placeRating, placeAddress, photoReference.toString(), placeCategory = placeCategory, placeOpenNow = placeOpenNow)
                                val markerColor: Float

                                if (placeLatLng !in placesReturned) {
                                    maxSWBounds = getSWBound(maxSWBounds, placeLatLng)
                                    maxNEBounds = getNEBound(maxNEBounds, placeLatLng)
                                    placesReturned.add(placeLatLng)

                                    if (placeTypes[j] in entertainmentCategory) {
                                        markerColor = BitmapDescriptorFactory.HUE_ROSE
                                        placeToMark.placeCategory = getString(R.string.category_title_entertainment)
                                    } else if (placeTypes[j] in cultureCategories) {
                                        markerColor = BitmapDescriptorFactory.HUE_BLUE
                                        placeToMark.placeCategory = getString(R.string.category_title_culture)
                                    } else if (placeTypes[j] in foodCategories) {
                                        markerColor = BitmapDescriptorFactory.HUE_ORANGE
                                        placeToMark.placeCategory = getString(R.string.category_title_food)
                                    } else if (placeTypes[j] in natureCategories) {
                                        markerColor = BitmapDescriptorFactory.HUE_GREEN
                                        placeToMark.placeCategory = getString(R.string.category_title_nature)
                                    } else { // Nightlife
                                        markerColor = BitmapDescriptorFactory.HUE_VIOLET
                                        placeToMark.placeCategory = getString(R.string.category_title_nightlife)
                                    }

                                    val marker = mMap?.addMarker(MarkerOptions()
                                        .position(placeLatLng)
                                        .title(placeName)
                                        .alpha(0.6f)
                                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                                    )
                                    marker!!.tag = placeToMark

                                    if (marker != null) {
                                        Log.d(TAG, "Add Marker to markersAdded")
                                        markersAdded.add(marker)
                                    }
                                }
                            }
                            explorationViewModel.markersAdded = markersAdded
                            mapBounds = LatLngBounds(maxSWBounds, maxNEBounds)
                            explorationViewModel.maxSWBounds = maxSWBounds
                            explorationViewModel.maxNEBounds = maxNEBounds
                            explorationViewModel.mapBounds = mapBounds
                            mMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 180))
                        }
                    }, Response.ErrorListener { _ ->
                    }) {}
                val requestQueue = Volley.newRequestQueue(activity)
                requestQueue.add(recommendationsRequest)
            }
        }
    }

    // helper function for converting the starting point marker on displayed on the map
    private fun vectorToBitmapDescriptor(context: Context, @DrawableRes vectorDrawableResourceId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)
        val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun markDestination(){
        Log.d(TAG, "markDestination called")
        mMap?.addMarker(MarkerOptions()
            .position(destinationPlace.placeLatLng)
            .title(destinationPlace.placeName)
            .icon(vectorToBitmapDescriptor(requireContext(), R.drawable.ic_map_destination)))
    }

    private fun markStartingLocation(name: String){
        Log.d(TAG, "markStartingLocation called")
        mMap?.addMarker(MarkerOptions()
            .position(currentLocationLatLng)
            .title(name)
            .icon(vectorToBitmapDescriptor(requireContext(), R.drawable.ic_map_starting_point)))
    }

    private fun resizeMapView(){
        if (this::mapBounds.isInitialized){
            mMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 240))
        } else {
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationPlace.placeLatLng, 15f))
        }
    }

    // Obtained from
    // https://cloud.google.com/blog/products/maps-platform/how-calculate-distances-map-maps-javascript-api
    private fun haversineDistance(p1: LatLng, p2: LatLng): Double {
        val r = 6378137; // Earth’s mean radius in meter
        val distanceLatitude = rad(p2.latitude - p1.latitude)
        val distanceLongitude = rad(p2.longitude - p1.longitude)
        val a = sin(distanceLatitude / 2) * sin(distanceLatitude / 2) +
                cos(rad(p1.latitude)) * cos(rad(p2.latitude)) *
                sin(distanceLongitude / 2) * sin(distanceLongitude / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val d = r * c;
        return d // returns the distance in meter
    }

    private fun rad(x: Double): Double {
        return x * Math.PI / 180
    }

    private fun setupMarkerClickListener(googleMap: GoogleMap) {
        mMap?.setOnMarkerClickListener { marker ->
            val place = marker.tag as? DetailedPlace

            if (place != null) {
                //Place Detail UI dialog
                userSelectedPlace = place
                Log.d(TAG, "Clicked marker for place: ${place.placeName}")

                detailedPlaceDialog.setTitle(place.placeName)
                if(place.placeRating==null){
                    placeRatingBar.visibility = View.GONE
                } else {
                    placeRatingBar.visibility = View.VISIBLE
                    placeRatingBar.rating = place.placeRating!!
                }
                placeVicinityTextView.text = place.placeVicinity

                if(place.placeTypes!=null){
                    placeTypesTextView.text = place.placeTypes
                } else {
                    placeTypesTextView.visibility = View.GONE
                }

                val photoRef = place.photoReference
                if (photoRef!=null){
                    MainScope().launch {
                        val bitmap = fetchPlaceImage(photoRef, placeImageView.maxWidth, MAPS_API_KEY)
                        if (bitmap != null) {
                            place.placeImgBitmap = bitmap
                            placeImageView.setImageBitmap(bitmap)
                            Log.d(TAG, "ImageView fetch succeeded")
                        } else {
                            // Handle the error (e.g., show a placeholder or error image)
                            Log.d(TAG, "ImageView fetch failed")
                        }
                    }
                } else {
                    placeImageView.setImageResource(R.drawable.no_available_img)
                }

                // setup the button according to whether the place has been added or not
                val positiveButton = detailedPlaceDialog.getButton(AlertDialog.BUTTON_POSITIVE)

                detailedPlaceDialog.show()

    //                if(!place.addedToPlan){
    //                    positiveButton.setText(R.string.detailed_place_dialog_add_button)
    //                    positiveButton.setOnClickListener {
    //                        // Add the place to list of places to visit
    //                        place.addedToPlan = true
    //                        addPlaceToRoute(place)
    //                        detailedPlaceDialog.dismiss()
    //                    }
    //                } else {
    //                    positiveButton.setText(R.string.detailed_place_dialog_remove_button)
    //                    positiveButton.setOnClickListener {
    //                        // Add the place to list of places to visit
    //                        place.addedToPlan = false
    //                        removePlaceFromRoute(place)
    //                        detailedPlaceDialog.dismiss()
    //                    }
    //                }

            }

            false
        }
    }

    // When a detailed place is added to the route by the user
    private fun addPlaceToRoute(detailedPlace: DetailedPlace) {
        Log.d(TAG, "At addPlaceToRoute()")
        placesToAdd.add(detailedPlace)
        explorationViewModel.placesToAddToRoute = placesToAdd

        val markerColor = when (detailedPlace.placeCategory) {
            getString(R.string.category_title_entertainment) -> {
                BitmapDescriptorFactory.HUE_ROSE
            }
            getString(R.string.category_title_culture) -> {
                BitmapDescriptorFactory.HUE_BLUE
            }
            getString(R.string.category_title_food) -> {
                BitmapDescriptorFactory.HUE_ORANGE
            }
            getString(R.string.category_title_nature) -> {
                BitmapDescriptorFactory.HUE_GREEN
            }
            else -> { // Nightlife
                BitmapDescriptorFactory.HUE_VIOLET
            }
        }

        Log.d(TAG, "markersAdded size: ${markersAdded.size}")
        for (i in 0 until markersAdded.size) {
            Log.d(TAG, "In for-loop in atPlacesToAdd")
            val marker: Marker = markersAdded[i]
            val position: LatLng = marker.position
            if (marker.position == detailedPlace.placeLatLng) {
                Log.d(TAG, "Adding $marker to placesToAdd")
                markersAdded.removeAt(i)
                marker.remove()
                // Re-plot the marker
                val updatedMarker = mMap?.addMarker(MarkerOptions()
                    .position(position)
                    .title(detailedPlace.placeName)
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                )

                if (updatedMarker != null) {
                    updatedMarker.tag = detailedPlace
                    markersAdded.add(i, updatedMarker)
                }
            }
        }
        explorationViewModel.markersAdded = markersAdded
    }

    // When a detailed place is removed from the route by the user
    private fun removePlaceFromRoute(detailedPlace: DetailedPlace) {
        Log.d(TAG, "At removePlaceFromRoute()")
        placesToAdd.remove(detailedPlace)
        explorationViewModel.placesToAddToRoute = placesToAdd

        val markerColor = when (detailedPlace.placeCategory) {
            getString(R.string.category_title_entertainment) -> {
                BitmapDescriptorFactory.HUE_ROSE
            }
            getString(R.string.category_title_culture) -> {
                BitmapDescriptorFactory.HUE_BLUE
            }
            getString(R.string.category_title_food) -> {
                BitmapDescriptorFactory.HUE_ORANGE
            }
            getString(R.string.category_title_nature) -> {
                BitmapDescriptorFactory.HUE_GREEN
            }
            else -> { // Nightlife
                BitmapDescriptorFactory.HUE_VIOLET
            }
        }

        for (i in 0 until markersAdded.size) {
            val marker: Marker = markersAdded[i]
            val position: LatLng = marker.position
            if (marker.position == detailedPlace.placeLatLng) {
                Log.d(TAG, "Removing $marker from placesToAdd")
                markersAdded.removeAt(i)
                marker.remove()
                // Re-plot the marker
                val updatedMarker = mMap?.addMarker(MarkerOptions()
                    .position(position)
                    .title(detailedPlace.placeName)
                    .alpha(0.6f)
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                )

                if (updatedMarker != null) {
                    updatedMarker.tag = detailedPlace
                    markersAdded.add(i, updatedMarker)
                }
            }
        }
        explorationViewModel.markersAdded = markersAdded
    }

    // To send data to Itinerary
    private fun createPOSTRequestItinerary() {
        /** Params for POST request:
         * user_email
         * starting_location: starting_point: 1st arg is name of location, 2nd arg is LatLng of location
         * destination
         * places
         */

        if (placesToAdd.size > 0) {
            val jsonObject = JSONObject()
            jsonObject.put("user_email", "new_user@bu.edu")
            jsonObject.put("starting_location", startingPoint)
            jsonObject.put("destination", destinationPlace)
            jsonObject.put("places", placesToAdd)

            val url = "http://54.147.60.104:80/add-itinerary/"

            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonObject,
                { response ->
                    // Handle response
                    Log.d(TAG, "Response from Itinerary: $response")
                },
                { error ->
                    // Handle error
                    Log.d(TAG, "Error from Itinerary: $error")
                }
            )

            val queue = Volley.newRequestQueue(context)
            queue.add(jsonObjectRequest)
        } else {
            Toast.makeText(activity, "Please add some places of interest to itinerary!", Toast.LENGTH_LONG).show()
        }

    }

    private suspend fun fetchPlaceImage(photoReference: String, maxWidth: Int, apiKey: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            val apiUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=$maxWidth&photoreference=$photoReference&key=$apiKey"
            var bitmap: Bitmap? = null

            try {
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream: InputStream = connection.inputStream
                    bitmap = BitmapFactory.decodeStream(inputStream)
                }

                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            bitmap
        }
    }


}