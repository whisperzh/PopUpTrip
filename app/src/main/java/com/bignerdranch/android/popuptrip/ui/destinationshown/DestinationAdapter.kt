package com.bignerdranch.android.popuptrip.ui.itinerary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.popuptrip.R

// Define a data class for ItineraryItem with destination and timeToNext properties
data class DestinationItem(val destination: String, val timeToNext: String)

// Define the ItineraryAdapter class which extends RecyclerView.Adapter with a ViewHolder
class ItineraryAdapter(private val destinations: MutableList<DestinationItem>) :
    RecyclerView.Adapter<ItineraryAdapter.ViewHolder>() {

    // Define a ViewHolder class for the ItineraryAdapter, responsible for holding view references
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val destinationNameTextView: TextView = itemView.findViewById(R.id.tv_destination_name)
        val timeToNextTextView: TextView = itemView.findViewById(R.id.tv_time_to_next)
        val arrowTextView: TextView = itemView.findViewById(R.id.tv_arrow)
    }

    // Inflate the layout for each item in the list and create a ViewHolder with the inflated view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.destination_item, parent, false)
        return ViewHolder(itemView)
    }

    // Bind the data from the current ItineraryItem to the ViewHolder's views
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = destinations[position]
        holder.destinationNameTextView.text = currentItem.destination

        if (position < destinations.size-1) {
            holder.timeToNextTextView.text = currentItem.timeToNext
            holder.arrowTextView.visibility = View.VISIBLE
        } else {
            holder.timeToNextTextView.text = ""
            holder.arrowTextView.visibility = View.GONE
        }
    }
    // Return the total number of items, including an extra item for the final destination
    override fun getItemCount(): Int = destinations.size

    // Add a new ItineraryItem to the list and update the adapter
    fun addDestination(destination: DestinationItem) {
        destinations.add(destination)
        notifyDataSetChanged()
    }
}
