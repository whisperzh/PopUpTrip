package com.bignerdranch.android.popuptrip.ui.home

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.bignerdranch.android.popuptrip.databinding.FragmentHomeSearchBoxBinding
import androidx.fragment.app.viewModels
import com.bignerdranch.android.popuptrip.BuildConfig.MAPS_API_KEY
import com.bignerdranch.android.popuptrip.R
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.util.*

private const val TAG = "HomeSearchBoxFragment"
class HomeSearchBoxFragment: Fragment() {
    private var _binding: FragmentHomeSearchBoxBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val homeSearchBoxViewModel: HomeSearchBoxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), MAPS_API_KEY, Locale.US);
        }
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

        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: ${place.name}, ${place.id}")
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//        inflater.inflate(R.menu.home_search_box_menu, menu)
//    }
}