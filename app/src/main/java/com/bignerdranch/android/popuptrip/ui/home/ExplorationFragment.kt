package com.bignerdranch.android.popuptrip.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.DrawableRes
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
import com.android.volley.Response
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
import com.google.android.material.textfield.TextInputEditText
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Double.max
import java.lang.Double.min
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


private const val TAG = "ExplorationFragment"
class ExplorationFragment: Fragment(), OnMapReadyCallback {

    private var _binding: FragmentExplorationBinding? = null
    private val binding get() = _binding!!

    private val args: ExplorationFragmentArgs by navArgs()

//    private val explorationViewModel: ExplorationViewModel by viewModels()

    private lateinit var mMap: GoogleMap
    // FOLLOWING VARIABLES NEED TO BE STORED IN VIEWMODEL
    private lateinit var mapBounds: LatLngBounds
    private lateinit var destinationId: String
    private lateinit var destinationPlace: Place
    private lateinit var startingPointId: String
    private lateinit var startingPlace: Place
    private var startingPointName = ""  // for making the autocomplete list disappear after click
    private var destinationName = ""
    private var oldText: String? = null
    private lateinit var maxSWBounds: LatLng
    private lateinit var maxNEBounds: LatLng

    // information fields we want to fetch from Google Map API
    private val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

    // autocomplete input textfields setup
    private lateinit var autoCompleteAdapter: PlacesAutoCompleteAdapter
    private lateinit var startingPointAddressInputEditText: TextInputEditText
    private lateinit var startingPointListView: ListView
    private lateinit var destinationAddressInputEditText: TextInputEditText
    private lateinit var destinationListView: ListView
    private lateinit var currentLocation: Button

    // current location button setup
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var currentLocationLatLng: LatLng
    private val permissionId = 2
    private lateinit var polyline: Polyline
    private val cultureCategories = arrayListOf<String>("art_gallery", "book_store", "library", "museum")
    private val foodCategories = arrayListOf<String>("bakery", "cafe", "restaurant")
    private val natureCategories = arrayListOf<String>("campground", "park")
    private val nightLifeCategories = arrayListOf<String>("bar", "night_club")
    private val entertainmentCategory = arrayListOf<String>("amusement_park", "aquarium", "movie_theater", "zoo")
    private val distanceRadius = 2000 // 2000m or 2km
    private val locationBias = 1000 // 1000m or 1km

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val explorationViewModel =
            ViewModelProvider(this).get(ExplorationViewModel::class.java)
        startingPointName = explorationViewModel.startingPointName
        destinationName = explorationViewModel.destinationName
        oldText = explorationViewModel.oldText
        startingPlace = explorationViewModel.startingPlace
//        currentLocationLatLng = startingPlace.latLng
        destinationPlace = explorationViewModel.destination
        mapBounds = explorationViewModel.mapBounds
        maxSWBounds = explorationViewModel.maxSWBounds
        maxNEBounds = explorationViewModel.maxNEBounds
        startingPointId = explorationViewModel.startingPointId

        // input arguments from navigation
        destinationId = if (args != null) {
            // TODO check if this changes if dest changed in exploration,
            //  switch to different tab and come back to exploration
            args.destinationPlaceId
        } else {
            explorationViewModel.destinationPointId
        }
        Log.d(TAG, "OnCreateView called! Destination ID received in exploration: $destinationId")

        _binding = FragmentExplorationBinding.inflate(inflater, container, false)

        Log.d(TAG, "Recreating map in onCreateView")

        // launch the support map fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

//        explorationViewModel.mapVIew = mapFragment

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
                    if (newText.toString() != startingPointName && newText.toString() != "Your Location") {
                        if (newText.toString() != "") {
                            mMap.clear()
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
                    } else if (newText.toString() == "Your Location" && startingPointName != "" && startingPointName != newText.toString()) {
                        Log.d(TAG, "Start point changed to current location")
//                        polyline.remove()
                        mMap.clear()
                        startingPointName = "Your Location"
                        getLocation()
                        getDirections()
                        markDestination()
                        markCurrentLocation("Your Location")
                        oldText = newText.toString()
                    } else if (newText.toString() != oldText) {
                        Log.d(TAG, "New Text not the same as Old Text")

                    } else if (newText.toString() == "Your Location" && startingPointName == "") {
                        Log.d(TAG, "Start point set to current location")
//                        mMap.clear()
                        startingPointName = "Your Location"
                        getLocation()
                        getDirections()
                        markDestination()
                        markCurrentLocation("Your Location")
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
            mMap.clear()
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
                startingPointId = placeId
                Log.d(TAG, "Starting point ID: $startingPointId")
                val startFetchPlaceRequest = FetchPlaceRequest.newInstance(startingPointId, placeFields)

                context?.let { context ->
                    Places.createClient(context).fetchPlace(startFetchPlaceRequest).addOnSuccessListener { response ->
                        startingPlace = response.place
                        startingPointName = startingPlace.name
                        currentLocationLatLng = startingPlace.latLng
                        startingPointAddressInputEditText.setText(startingPointName)
                        Log.i(TAG, "Starting Point Selected: ${startingPlace.name}, ${startingPlace.id}, ${startingPlace.latLng}")
                        Log.d(TAG, "On starting point selected")
                        markCurrentLocation(startingPointName)

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

                    if (newText.toString() != destinationName){
                        Log.d(TAG, "destinationName: $destinationName")
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
                                Log.i(TAG, "Visibility of destination listView is set to VISIBLE")
                                destinationListView.visibility = View.VISIBLE
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
                if(placeId != destinationId){
                    Log.d(TAG, "if statement in setOnItemClickListener for destination is triggered")
                    destinationId = placeId
                    val destFetchPlaceRequest = FetchPlaceRequest.newInstance(destinationId, placeFields)

                    context?.let { context ->
                        Places.createClient(context).fetchPlace(destFetchPlaceRequest).addOnSuccessListener { response ->
                            destinationPlace = response.place
                            destinationName = destinationPlace.name
                            destinationAddressInputEditText.setText(destinationName)
                            Log.i(TAG, "Starting Point Selected: ${destinationPlace.name}, ${destinationPlace.id}, ${destinationPlace.latLng}")

                            // to clear any previously selected locations
                            mMap.clear()
                            Log.d(TAG, "On destination selected")

                            markDestination()

                            if (this::currentLocationLatLng.isInitialized){
                                markCurrentLocation(startingPointName)
                                // resize map bounds and draw the route
                                getDirections()
                            } else {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationPlace.latLng, 15f))
                            }

                        }.addOnFailureListener { exception ->
                            if (exception is ApiException) {
                                Log.e(TAG, "Place not found: " + exception.statusCode)
                            }
                        }
                    }
                } else {
                    destinationAddressInputEditText.setText(destinationName)
                }
            }
        }

        // current location button implementation
        binding.useCurrentLocationButton.setOnClickListener{
            Log.d(TAG, "Current Location selected")
            // Hide the keyboard
            val imm = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            if (this::mapBounds.isInitialized){
                resizeMapView()
            }
            // to clear any previously selected locations
//            mMap.clear()
            getLocation()
        }
            if (this::currentLocationLatLng.isInitialized) {
                markDestination()
                markCurrentLocation("Your Location")
                getDirections()
        }

        binding.adjustMapBoundButton.setOnClickListener{
            resizeMapView()
        }

        binding.explorationBackButton.setOnClickListener {
            val home = ExplorationFragmentDirections.explorationToHomeAction()
                .setDestinationPlaceName(destinationName)
//            // Launch navigation to home page
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    findNavController().navigate(home)
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
        val destFetchPlaceRequest = FetchPlaceRequest.newInstance(destinationId, placeFields)

        // fetch destination place
        Places.createClient(requireContext()).fetchPlace(destFetchPlaceRequest).addOnSuccessListener { response ->
            destinationPlace = response.place
            destinationName = destinationPlace.name
            Log.i(TAG, "Destination Place Selected: ${destinationPlace.name}, ${destinationPlace.id}, ${destinationPlace.latLng}")

            binding.destTextInputTextfield.setText(destinationName)
            markDestination()
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationPlace.latLng, 15f))

        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                Log.e(TAG, "Place not found: " + exception.statusCode)
            }
        }

        setupMarkerClickListener(mMap)
    }

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
                            binding.startingTextInputTextfield.setText("Your Location")
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

        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                currentLocationLatLng.latitude.toString() + "," +
                currentLocationLatLng.longitude.toString() +
                "&destination=" + destinationPlace.latLng.latitude.toString() + "," +
                destinationPlace.latLng.longitude.toString() +
                "&mode=" + travelMode.lowercase() +
                "&key=" + MAPS_API_KEY

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
                    maxSWBounds = getSWBound(currentLocationLatLng, destinationPlace.latLng)
                    maxNEBounds = getNEBound(currentLocationLatLng, destinationPlace.latLng)

                    var point = path[0][0]
                    coordinates.add(path[0][0])
                    for (i in 0 until path.size) {
//                        Log.d(TAG, "Path $i: " + path[i].toString())
                        polyline = mMap.addPolyline(PolylineOptions().addAll(path[i]).color(BLUE))

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
                    mapBounds = LatLngBounds(maxSWBounds, maxNEBounds)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 240))
                    getRecommendations(coordinates)
                }
            }, Response.ErrorListener { _ ->
            }) {}
        val requestQueue = Volley.newRequestQueue(activity)
        requestQueue.add(directionsRequest)
    }

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

                                val placeId = resultObject.getString("place_id")
                                val placeName = resultObject.getString("name")
                                val geometry = resultObject.getJSONObject("geometry")
                                val location = geometry.getJSONObject("location")
                                val placeLatLng = LatLng(location.getDouble("lat"), location.getDouble("lng"))
                                val placeRating = resultObject.getString("rating").toFloat()
                                val placeAddress = resultObject.getString("formatted_address")

//                                val photo_refs = resultObject.getJSONArray("photos").getJSONObject(0).getString("photo_reference")

                                val photo = resultObject.optJSONArray("photos")
                                val photoReference: String? = if (photo != null && photo.length() > 0) {
                                    val photoObject = photo.getJSONObject(0)
                                    photoObject.optString("photo_reference", null)
                                } else {
                                    null
                                }
//                                Log.d(TAG, "resultObject photo_ref is: $photoReference")


                                val placeToMark = DetailedPlace(placeId, placeLatLng, placeName, placeRating, placeAddress, photoReference.toString())
                                val markerColor: Float

                                if (placeLatLng !in placesReturned) {
                                    maxSWBounds = getSWBound(maxSWBounds, placeLatLng)
                                    maxNEBounds = getNEBound(maxNEBounds, placeLatLng)
                                    placesReturned.add(placeLatLng)

                                    if (placeTypes[j] in entertainmentCategory) {
                                        markerColor = BitmapDescriptorFactory.HUE_ROSE
                                    } else if (placeTypes[j] in cultureCategories) {
                                        markerColor = BitmapDescriptorFactory.HUE_BLUE
                                    } else if (placeTypes[j] in foodCategories) {
                                        markerColor = BitmapDescriptorFactory.HUE_ORANGE
                                    } else if (placeTypes[j] in natureCategories) {
                                        markerColor = BitmapDescriptorFactory.HUE_GREEN
                                    } else { // Nightlife
                                        markerColor = BitmapDescriptorFactory.HUE_VIOLET
                                    }

                                    val marker = mMap.addMarker(MarkerOptions()
                                        .position(placeLatLng)
                                        .title(placeName)
                                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                                    )
                                    marker!!.tag = placeToMark
                                }
                            }
                            mapBounds = LatLngBounds(maxSWBounds, maxNEBounds)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 180))
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
        mMap.addMarker(MarkerOptions()
            .position(destinationPlace.latLng)
            .title(destinationPlace.name)
            .icon(vectorToBitmapDescriptor(requireContext(), R.drawable.ic_map_destination)))
    }

    private fun markCurrentLocation(name: String){
        Log.d(TAG, "markCurrentLocation called")
        mMap.addMarker(MarkerOptions()
            .position(currentLocationLatLng)
            .title(name)
            .icon(vectorToBitmapDescriptor(requireContext(), R.drawable.ic_map_starting_point)))
    }

    private fun resizeMapView(){
        if (this::mapBounds.isInitialized){
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 240))
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationPlace.latLng, 15f))
        }
    }

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
        mMap.setOnMarkerClickListener { marker ->
            val place = marker.tag as? DetailedPlace

            if (place != null) {
                //TODO: Place Detail UI dialog
                Log.d(TAG, "Clicked marker for place: ${place.placeName}")
            }

            false
        }
    }
}