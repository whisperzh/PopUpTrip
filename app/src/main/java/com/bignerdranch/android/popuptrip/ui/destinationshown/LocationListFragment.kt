package com.bignerdranch.android.popuptrip.ui.destinationshown

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.popuptrip.R

class LocationListFragment : Fragment() {

    private lateinit var locationListAdapter: LocationListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_location_list, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)

        // 创建初始数据
        val locationData = listOf(
            LocationItem("location1", "2023-04-23 08:00", "2hour"),
            LocationItem("location2", "2023-04-23 10:30", "1hour30mins"),
            LocationItem("location3", "2023-04-23 13:00", "45mins"),
            LocationItem("location4", "2023-04-23 15:15", "1hour")
        )

        locationListAdapter = LocationListAdapter(locationData)
        recyclerView.adapter = locationListAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return view
    }
}
