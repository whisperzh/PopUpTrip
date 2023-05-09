package com.bignerdranch.android.popuptrip.ui.itinerary

import DestinationItemViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.databinding.DestinationItemBinding
import com.bignerdranch.android.popuptrip.ui.destinationshown.DestinationItem

class ItineraryAdapter(private val destinations: MutableList<DestinationItem>) :
    RecyclerView.Adapter<DestinationItemViewHolder>() {

    // Inflate the layout for each item in the list and create a ViewHolder with the inflated view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationItemViewHolder {
        val binding = DestinationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DestinationItemViewHolder(binding)
    }

    // Bind the data from the current ItineraryItem to the ViewHolder's views
    override fun onBindViewHolder(holder: DestinationItemViewHolder, position: Int) {
        val currentItem = destinations[position]
        holder.bind(currentItem, position == destinations.size-1)
    }
    // Return the total number of items, including an extra item for the final destination
    override fun getItemCount(): Int = destinations.size

    // Add a new ItineraryItem to the list and update the adapter
    fun addDestination(destination: DestinationItem) {
        destinations.add(destination)
        notifyDataSetChanged()
    }
}