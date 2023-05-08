package com.bignerdranch.android.popuptrip.ui.itinerary

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.ui.destinationshown.DestinationItem
import com.bignerdranch.android.popuptrip.ui.home.DetailedPlace
import com.bignerdranch.android.popuptrip.ui.home.ExplorationFragmentArgs
import com.bignerdranch.android.popuptrip.ui.itinerary.ItineraryItem



class ItineraryFragment : Fragment() {
    private val TAG:String="class ItineraryFragment : Fragment()"
    private lateinit var itineraryAdapter: ItineraryAdapter
    private val args: ItineraryFragmentArgs by navArgs()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_itinerary, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.itinerary_recycler_view)

        val destinations = listOf(
            ItineraryItem("Current Location", "15 mins"),
            ItineraryItem("Destination 1", "30 mins"),
            ItineraryItem("Destination 2", "45 mins"),
            ItineraryItem("Destination 3", "1 hour")
        )


        itineraryAdapter = ItineraryAdapter(destinations)
        recyclerView.adapter = itineraryAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        if (args != null) {
            Log.d(TAG, args.itineraryId)
        } else {
            Log.d(TAG, "NO args")

        }
        return view
    }
}
