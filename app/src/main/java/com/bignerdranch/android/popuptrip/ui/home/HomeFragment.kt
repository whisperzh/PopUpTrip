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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.databinding.FragmentHomeBinding
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val args: HomeFragmentArgs by navArgs()

    private val nearbyPlaceListViewModel: NearbyPlaceListViewModel by viewModels()

    // destination autocomplete setup
    private lateinit var autoCompleteAdapter: PlacesAutoCompleteAdapter

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

//        // receive arguments from navigation
//        val receivedName = args.destinationPlaceId
//        Log.d(TAG, "OnCreateView called! Destination ID received in home: $receivedName")
//
//        if(receivedName!=null){
//            destinationName = receivedName
//            binding.homeSearchBox.setText(destinationName)
//        }
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // to inflate nearby places list
        binding.nearbyPlacesRecyclerView.layoutManager = LinearLayoutManager(context)

        val places = nearbyPlaceListViewModel.places
        val adapter = NearbyPlaceListAdapter(places)
        binding.nearbyPlacesRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val token = AutocompleteSessionToken.newInstance()
        binding.homeSearchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { newText ->
                    if (newText.toString()!=destinationName){
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

//                val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
//                val fetchPlaceRequest = FetchPlaceRequest.newInstance(placeId, placeFields)
//
//                context?.let { context ->
//                    Places.createClient(context).fetchPlace(fetchPlaceRequest).addOnSuccessListener { response ->
//                        val place = response.place
////                        Log.i(TAG, "Place: ${place.name}, ${place.id}, ${place.latLng}")
//
//                    }.addOnFailureListener { exception ->
//                        if (exception is ApiException) {
//                            Log.e(TAG, "Place not found: " + exception.statusCode)
//                        }
//                    }
//                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}