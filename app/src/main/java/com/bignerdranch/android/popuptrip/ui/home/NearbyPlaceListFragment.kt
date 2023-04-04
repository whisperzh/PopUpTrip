package com.bignerdranch.android.popuptrip.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bignerdranch.android.popuptrip.databinding.FragmentNearbyPlaceListBinding


private const val TAG = "NearbyPlacesListFragment"

class NearbyPlaceListFragment: Fragment() {

    private var _binding: FragmentNearbyPlaceListBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val nearbyPlaceListViewModel: NearbyPlaceListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNearbyPlaceListBinding.inflate(inflater, container, false)

        binding.nearbyPlacesRecyclerView.layoutManager = LinearLayoutManager(context)

        val places = nearbyPlaceListViewModel.places
        val adapter = NearbyPlaceListAdapter(places)
        binding.nearbyPlacesRecyclerView.adapter = adapter

        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}