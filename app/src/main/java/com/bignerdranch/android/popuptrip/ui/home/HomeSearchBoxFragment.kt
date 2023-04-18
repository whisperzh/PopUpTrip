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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.databinding.FragmentHomeSearchBoxBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch


private const val TAG = "HomeSearchBoxFragment"

class HomeSearchBoxFragment: Fragment() {
    private var _binding: FragmentHomeSearchBoxBinding? = null
    private lateinit var autoCompleteAdapter: PlacesAutoCompleteAdapter
    private lateinit var addressInputEditText: TextInputEditText
    private lateinit var listView: ListView

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val homeSearchBoxViewModel: HomeSearchBoxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        if (!Places.isInitialized()) {
//            Places.initialize(requireContext(), MAPS_API_KEY);
//        }

        Log.d(TAG, "onCraete has been called")
//        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeSearchBoxBinding.inflate(inflater, container, false)
//        val destEntered = homeSearchBoxViewModel.dest
//        binding.homeSearchBar.setText(destEntered)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addressInputEditText = view.findViewById(R.id.home_search_box)
        listView = view.findViewById(R.id.autoCompleteListView)

        val token = AutocompleteSessionToken.newInstance()


        addressInputEditText.addTextChangedListener(object : TextWatcher {
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
                            listView.adapter = autoCompleteAdapter
                            listView.visibility = View.VISIBLE
                        }.addOnFailureListener { exception ->
                            Log.i(TAG, "onTextChangedListener error")
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedPrediction = autoCompleteAdapter.getItem(position)
            addressInputEditText.setText(selectedPrediction?.getFullText(null))
            listView.visibility = View.GONE

            selectedPrediction?.placeId?.let { placeId ->

                val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
                val fetchPlaceRequest = FetchPlaceRequest.newInstance(placeId, placeFields)

                context?.let { context ->
                    Places.createClient(context).fetchPlace(fetchPlaceRequest).addOnSuccessListener { response ->
                        val place = response.place
                        Log.i(TAG, "Place: ${place.name}, ${place.id}, ${place.latLng}")
                    }.addOnFailureListener { exception ->
                        if (exception is ApiException) {
                            Log.e(TAG, "Place not found: " + exception.statusCode)
                        }
                    }
                }
                // Launch navigation to exploration page
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        findNavController().navigate(
                            HomeFragmentDirections.homeToExplorationAction(placeFields.toString())
                        )
                    }
                }


            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}