package com.bignerdranch.android.popuptrip.ui.itinerary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.ui.destinationshown.DestinationItem


data class ItineraryItem(val destination: String, val timeToNext: String)

class ItineraryAdapter(private val destinations: List<ItineraryItem>) :
    RecyclerView.Adapter<ItineraryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val destinationNameTextView: TextView = itemView.findViewById(R.id.tv_destination_name)
        val timeToNextTextView: TextView = itemView.findViewById(R.id.tv_time_to_next)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.itinerary_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < destinations.size) {
            val currentItem = destinations[position]
            holder.destinationNameTextView.text = currentItem.destination
            holder.timeToNextTextView.text = currentItem.timeToNext
        } else {
            holder.destinationNameTextView.text = "Final Destination"
            holder.timeToNextTextView.text = "N/A"
        }
    }

    override fun getItemCount(): Int = destinations.size + 1

}







