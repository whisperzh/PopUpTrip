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
import android.view.inputmethod.InputMethodManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.google.maps.android.PolyUtil
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bignerdranch.android.popuptrip.BuildConfig.MAPS_API_KEY
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.databinding.FragmentExplorationBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.material.textfield.TextInputEditText
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.Double.max
import java.lang.Double.min

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val explorationViewModel =
            ViewModelProvider(this).get(ExplorationViewModel::class.java)

        // input arguments from navigation
        destinationId = args.destinationPlaceId
        Log.d(TAG, "OnCreateView called! Destination ID received in exploration: $destinationId")

        _binding = FragmentExplorationBinding.inflate(inflater, container, false)

        // launch the support map fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

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
                        mMap.clear()
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
//            // Launch navigation to home page
//            viewLifecycleOwner.lifecycleScope.launch {
//                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                    findNavController().navigate(
//                        ExplorationFragmentDirections.explorationToHomeAction(destinationName)
//                    )
//                }
//            }
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

//                            Log.d(TAG, "In getLocation(), mark destination and current location")
//                            markDestination()
//                            markCurrentLocation("Your Location")
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
        if (!this::currentLocationLatLng.isInitialized) {
            Log.d(TAG, "Current location is null")
            getLocation()
        }
        Log.d(TAG, "getDirections() is called")
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val travelModes = listOf("WALKING", "TRANSIT", "DRIVING", "BICYCLING")
        val travelModeInt = prefs.getInt("SpinnerPosition", 2)
        val travelMode = travelModes[travelModeInt]
        var maxSWBounds: LatLng
        var maxNEBounds: LatLng
        Log.d(TAG, "Travel Mode: $travelMode")
        val path: MutableList<List<LatLng>> = ArrayList()
        Log.d(TAG, "")
        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                currentLocationLatLng.latitude.toString() + "," +
                currentLocationLatLng.longitude.toString() +
                "&destination=" + destinationPlace.latLng.latitude.toString() + "," +
                destinationPlace.latLng.longitude.toString() +
                "&mode=" + travelMode.lowercase() +
                "&key=" + MAPS_API_KEY
//        Log.d(TAG, "url: $urlDirections")

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

                    for (i in 0 until path.size) {
                        polyline = mMap.addPolyline(PolylineOptions().addAll(path[i]).color(BLUE))

                        // modify map bounds to include the route
                        for (j in 0 until path[i].size) {
                            maxSWBounds = getSWBound(maxSWBounds, path[i][j])
                            maxNEBounds = getNEBound(maxNEBounds, path[i][j])
                        }
                    }
//                    markCurrentionLocation(startingPointName)
//                    markDestination()

                    mapBounds = LatLngBounds(maxSWBounds, maxNEBounds)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 240))
                }
            }, Response.ErrorListener { _ ->
            }) {}
        val requestQueue = Volley.newRequestQueue(activity)
        requestQueue.add(directionsRequest)
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
}