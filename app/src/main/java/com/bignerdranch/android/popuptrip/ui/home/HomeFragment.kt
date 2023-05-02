package com.bignerdranch.android.popuptrip.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.bignerdranch.android.popuptrip.R
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONArray
import org.json.JSONObject


private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val args: HomeFragmentArgs by navArgs()

    // Recycler list & dialog implementation
    private val nearbyPlaceListViewModel: NearbyPlaceListViewModel by viewModels()
    private lateinit var detailedPlaceDialog: AlertDialog
    private lateinit var placeRatingBar: RatingBar
    private lateinit var placeVicinityTextView: TextView
    private lateinit var placeTypesTextView: TextView
    private lateinit var placeImageView: ImageView
    private lateinit var placeIdToSend: String

    // destination autocomplete setup
    private lateinit var autoCompleteAdapter: PlacesAutoCompleteAdapter

    // current location for nearby places
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var currentLocationLatLng: LatLng
    private val permissionId = 2
    private val radius = 1500

    // user preference setup
    private val cultureCategories = arrayListOf<String>("art_gallery", "book_store", "library", "museum")
    private val foodCategories = arrayListOf<String>("bakery", "cafe", "restaurant")
    private val natureCategories = arrayListOf<String>("campground", "park")
    private val nightLifeCategories = arrayListOf<String>("bar", "night_club")
    private val entertainmentCategory = arrayListOf<String>("amusement_park", "aquarium", "movie_theater", "zoo")
    private var userPreferenceList: ArrayList<String>? = null

    private var destinationName = ""

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate called")
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i(TAG, "onCreateView called")

        // receive arguments from navigation
        val receivedName = args.destinationPlaceName
        Log.d(TAG, "OnCreateView called! Destination Place Name received in home: $receivedName")

        if(receivedName != null && receivedName != "null"){
            Log.d(TAG, "destination is set to receivedName in nav args")
            destinationName = receivedName
        }


        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // inflate Place Detailed Dialog
        val detailedPlaceDialogLayout = LayoutInflater.from(requireContext()).inflate(R.layout.detailed_place_dialog, null)

        placeRatingBar = detailedPlaceDialogLayout.findViewById<RatingBar>(R.id.detailed_place_dialog_rating)
        placeVicinityTextView = detailedPlaceDialogLayout.findViewById<TextView>(R.id.detailed_place_dialog_vicinity)
        placeImageView = detailedPlaceDialogLayout.findViewById<ImageView>(R.id.detailed_place_dialog_img)
        placeTypesTextView = detailedPlaceDialogLayout.findViewById(R.id.detailed_place_dialog_types)

        detailedPlaceDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(detailedPlaceDialogLayout)
            .setPositiveButton(R.string.detailed_place_dialog_select_button) { _, _ ->
                // Launch navigation to exploration page
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        findNavController().navigate(
                            HomeFragmentDirections.homeToExplorationAction(placeIdToSend)
                        )
                    }
                }
            }
            .setNeutralButton(R.string.back_button) { _, _ ->
                // Handle negative button click
            }
            .create()

        // outside click does not close the dialog
        detailedPlaceDialog.setCanceledOnTouchOutside(false)

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
        Log.i(TAG, "onViewCreated called")

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                //TODO: check if preferences are changed, if so, fetch. If not, use view model
                if (userPreferenceList==null){
                    // list of nearby places in viewModel is updated with fetch response
                    getCurrentLocationAndFetchPlaces()
                } else {
                    val tempUserPreferenceList = getPlaceTypePreference()
                    val sortedUserPreferenceList = userPreferenceList!!.sorted()
                    val sortedTempList = tempUserPreferenceList.sorted()
                    // no change in user preference, therefore no need to fetch
                    if(sortedUserPreferenceList==sortedTempList){
                        val nearbyPlaces = nearbyPlaceListViewModel.nearbyPlaces
                        recyclerViewItemClickSetup()
                    } else {
                        getCurrentLocationAndFetchPlaces()
                    }
                }
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

                            nearbyPlaceListViewModel.clearPlaceList()

                            // to store placeId of added places
                            val placesReturned = arrayListOf<String>()
                            fetchNearbyPlaces(
                                requireContext(),
                                currentLocationLatLng.latitude,
                                currentLocationLatLng.longitude,
                                radius,
                                MAPS_API_KEY,
                                onSuccess = { response ->
                                    Log.d(TAG, "Succeed to fetch nearby places")
                                    val jsonResponse = JSONObject(response)
                                    val resultsArray: JSONArray = jsonResponse.getJSONArray("results")

                                    for (i in 0 until resultsArray.length()) {
                                        val resultObject = resultsArray.getJSONObject(i)
                                        Log.d(TAG, "result $i: $resultObject")

                                        val placeId = resultObject.getString("place_id")
                                        // check if this place already exist in the recommended list
                                        if (!placesReturned.contains(placeId)){
                                            placesReturned.add(placeId)

                                            val geometry = resultObject.getJSONObject("geometry")
                                            val location = geometry.getJSONObject("location")
                                            val placeLatLng = LatLng(location.getDouble("lat"), location.getDouble("lng"))
                                            val placeName = resultObject.getString("name")
                                            val rating = resultObject.optString("rating", null)
                                            val placeRating = rating?.toFloat()

                                            val placeOpeningHours = resultObject.optJSONObject("opening_hours")
                                            val placeOpenNow: Boolean? =
                                                placeOpeningHours?.optString("open_now")?.toBoolean()

                                            val placeTypesTemp = jsonArrayToStringList(resultObject.optJSONArray("types"))

                                            // remove "point of interest", "establishment"
                                            val placeTypes = placeTypesTemp.dropLast(2)

//                                    Log.d(TAG, "place types: ${placeTypes.joinToString()}")

                                            val placeAddress = resultObject.getString("vicinity")

                                            val photo = resultObject.optJSONArray("photos")
                                            val photoReference: String? = if (photo != null && photo.length() > 0) {
                                                val photoObject = photo.getJSONObject(0)
                                                photoObject.optString("photo_reference", null)
                                            } else {
                                                null
                                            }

                                            val placeToAdd = DetailedPlace(placeId,
                                                placeLatLng,
                                                placeName,
                                                placeRating,
                                                placeAddress,
                                                photoReference,
                                                placeTypes = placeTypes.joinToString().replace("_", " "),
                                                placeOpenNow = placeOpenNow)

                                            nearbyPlaceListViewModel.updatePlaces(placeToAdd)
                                        } else {
                                            Log.d(TAG, "DUPLICATE PLACE")
                                        }
                                    }
                                    recyclerViewItemClickSetup()
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
        userPreferenceList = getPlaceTypePreference()

        Log.d(TAG, "Place Types: $userPreferenceList")
        for (i in 0 until userPreferenceList!!.size) {
            //TODO: fetch requests based on user preferred place type
            Log.d(TAG, "fetchNearbyPlaces() is called on place type: ${userPreferenceList!![i]}")
            val requestUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$latitude,$longitude&radius=$radius&type=${userPreferenceList!![i]}&key=$apiKey"

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
    }

    private fun jsonArrayToStringList(jsonArray: JSONArray): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getString(i))
        }
        return list
    }

    private fun getPlaceTypePreference(): ArrayList<String> {
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

        return placeTypes
    }

    private fun recyclerViewItemClickSetup(){
        val nearbyPlaces = nearbyPlaceListViewModel.nearbyPlaces
        binding.nearbyPlacesRecyclerView.adapter = NearbyPlaceListAdapter(nearbyPlaces) {position ->
            // Handle item click here
            val clickedPlace = nearbyPlaces[position]
            placeIdToSend = clickedPlace.placeId
            detailedPlaceDialog.setTitle(clickedPlace.placeName)
            if(clickedPlace.placeRating==null){
                placeRatingBar.visibility = GONE
            } else {
                placeRatingBar.visibility = VISIBLE
                placeRatingBar.rating = clickedPlace.placeRating!!
            }
            placeVicinityTextView.text = clickedPlace.placeVicinity

            if(clickedPlace.placeTypes!=null){
                placeTypesTextView.text = clickedPlace.placeTypes
            } else {
                placeTypesTextView.visibility = GONE
            }

            if(clickedPlace.placeImgBitmap!=null){
                placeImageView.setImageBitmap(clickedPlace.placeImgBitmap)
            } else {
                placeImageView.setImageResource(R.drawable.no_available_img)
            }

            detailedPlaceDialog.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView called")
        _binding = null
    }

}