package com.bignerdranch.android.popuptrip.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bignerdranch.android.popuptrip.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import kotlinx.coroutines.launch
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bignerdranch.android.popuptrip.BuildConfig
import com.bignerdranch.android.popuptrip.BuildConfig.MAPS_API_KEY
import com.google.android.libraries.places.api.model.Place
import org.json.JSONArray
import org.json.JSONObject


private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val args: HomeFragmentArgs by navArgs()

    private val nearbyPlaceListViewModel: NearbyPlaceListViewModel by viewModels()

    // destination autocomplete setup
    private lateinit var autoCompleteAdapter: PlacesAutoCompleteAdapter

    // current location for nearby places
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var currentLocationLatLng: LatLng
    private val permissionId = 2
    private val radius = 2000
    private val preferenceType = mutableListOf<String>()

    private var destinationName = ""

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        // receive arguments from navigation
        val receivedName = args.destinationPlaceName
        Log.d(TAG, "OnCreateView called! Destination Place Name received in home: $receivedName")

        if(receivedName != null && receivedName != "null"){
            Log.d(TAG, "destination is set to receivedName in nav args")
            destinationName = receivedName
        }

        // TODO: get user preferred types of places for recommendation
        preferenceType.add("restaurant")

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // to inflate nearby places list
        binding.nearbyPlacesRecyclerView.layoutManager = LinearLayoutManager(context)

//        val places = nearbyPlaceListViewModel.nearbyPlaces
//        val adapter = NearbyPlaceListAdapter(places)
//        binding.nearbyPlacesRecyclerView.adapter = adapter

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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // list of nearby places in viewModel is updated with fetch response
                getCurrentLocationAndFetchPlaces()
//                val nearbyPlaces = nearbyPlaceListViewModel.nearbyPlaces
//                binding.nearbyPlacesRecyclerView.adapter = NearbyPlaceListAdapter(nearbyPlaces)
            }
        }

        if(destinationName != ""){
            binding.homeSearchBox.setText(destinationName)
        }
        val token = AutocompleteSessionToken.newInstance()
        binding.homeSearchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { newText ->
                    if (newText.toString() != destinationName){
                        Log.d(TAG, "newText: $newText")
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
                                binding.homeAutoCompleteListView.adapter = autoCompleteAdapter
                                binding.homeAutoCompleteListView.visibility = View.VISIBLE
                            }.addOnFailureListener { exception ->
                                Log.i(TAG, "onTextChangedListener error")
                                Log.i(TAG, exception.toString())
                            }
                        }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.homeAutoCompleteListView.setOnItemClickListener { _, _, position, _ ->
            val selectedPrediction = autoCompleteAdapter.getItem(position)
            destinationName = selectedPrediction?.getFullText(null).toString()
            binding.homeSearchBox.setText(destinationName)
//            addressInputEditText.setText(selectedPrediction?.getFullText(null))

            binding.homeAutoCompleteListView.visibility = View.GONE


            selectedPrediction?.placeId?.let { placeId ->

                Log.d(TAG, "Destination ID sent from home: $placeId")
                // Launch navigation to exploration page
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        findNavController().navigate(
                            HomeFragmentDirections.homeToExplorationAction(placeId)
                        )
                    }
                }
            }
        }

    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
                getCurrentLocationAndFetchPlaces()
            }
        }
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getCurrentLocationAndFetchPlaces() {
        Log.d(TAG, "getLocation() is called")
        if (checkPermissions()) {
            Log.d(TAG, "Check Permission success")
            if (isLocationEnabled()) {
                Log.d(TAG, "Location is enabled")
                activity?.let {
                    Log.d(TAG, "Getting Current Location")
                    val fusedLocationClientIsNull = (fusedLocationClient == null)
                    Log.d(TAG, "Fused Location Client null?: $fusedLocationClientIsNull")
                    fusedLocationClient?.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, object : CancellationToken() {
                        override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token

                        override fun isCancellationRequested() = false
                    })?.addOnSuccessListener { currentLocation: Location? ->
                        if (currentLocation == null)
                            Toast.makeText(activity, "Cannot get current location.", Toast.LENGTH_LONG).show()
                        else {
                            Log.d(TAG, "Current Latitude: " + (currentLocation).latitude)
                            Log.d(TAG, "Current Longitude: " + (currentLocation).longitude)
                            currentLocationLatLng = LatLng((currentLocation).latitude, (currentLocation).longitude)

                            fetchNearbyPlaces(
                            requireContext(),
                            currentLocationLatLng.latitude,
                            currentLocationLatLng.longitude,
                            radius,
                            MAPS_API_KEY,
                            onSuccess = { response ->
                                Log.i(TAG, "Succeed to fetch nearby places")
                                val jsonResponse = JSONObject(response)
                                val resultsArray: JSONArray = jsonResponse.getJSONArray("results")

                                for (i in 0 until resultsArray.length()) {
                                    val resultObject = resultsArray.getJSONObject(i)
                                    val placeId = resultObject.getString("place_id")
                                    val placeName = resultObject.getString("name")
                                    val geometry = resultObject.getJSONObject("geometry")
                                    val location = geometry.getJSONObject("location")
                                    val lat = location.getDouble("lat")
                                    val lng = location.getDouble("lng")

                                    val placeRating = resultObject.getString("rating").toFloat()

                                    val placeAddress = resultObject.getString("vicinity")
                                    val placeOpeningHours = resultObject.getJSONObject("opening_hours")
                                    val placeOpenNow = placeOpeningHours.getBoolean("open_now")

                                    val placeToAdd = DetailedPlace(placeId, LatLng(lat, lng), placeName, placeRating, placeAddress, placeOpenNow)

//                                    val placePhotos = resultObject.getString("photos")
//                                    val placeIcon = resultObject.getString("icon")
//                                    val placeIconColor = resultObject.getString("icon_background_color")
//                                    val placeIconMaskBaseUri = resultObject.getString("icon_mask_base_uri")



                //                    val place = Place(placeId, name, vicinity, lat, lng)
                //                    places.add(place)
                                    nearbyPlaceListViewModel.updatePlaces(placeToAdd)
                                }

                                val nearbyPlaces = nearbyPlaceListViewModel.nearbyPlaces
                                binding.nearbyPlacesRecyclerView.adapter = NearbyPlaceListAdapter(nearbyPlaces)
                            },
                            onError = { error ->
                                Log.i(TAG, "Failed to fetch nearby places $error")
                            }
                        )

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

    private fun fetchNearbyPlaces(
        context: Context,
        latitude: Double,
        longitude: Double,
        radius: Int,
        apiKey: String,
        onSuccess: (response: String) -> Unit,
        onError: (error: String) -> Unit
    ) {
        Log.d(TAG, "fetchNearbyPlaces() is called")
        val requestUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$latitude,$longitude&radius=$radius&type=restaurant&key=$apiKey"

        val requestQueue: RequestQueue = Volley.newRequestQueue(context)

        val stringRequest = StringRequest(
            Request.Method.GET, requestUrl,
            Response.Listener { response ->
                onSuccess(response)
            },
            Response.ErrorListener { error ->
                onError(error.toString())
            })

        requestQueue.add(stringRequest)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}