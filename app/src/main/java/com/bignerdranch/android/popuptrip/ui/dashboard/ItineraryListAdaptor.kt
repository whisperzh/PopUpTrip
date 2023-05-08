package com.bignerdranch.android.popuptrip.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.popuptrip.databinding.ItineraryItemBinding
import java.util.*

class ItineraryListAdaptor (private val itineraries: MutableList<Itinerary>,
                            private val onItineraryClicked: (itineraryId: String)-> Unit
) : RecyclerView.Adapter<ItineraryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItineraryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItineraryItemBinding.inflate(inflater, parent, false)
        return ItineraryViewHolder(binding)
    }

    override fun getItemCount()=itineraries.size

    override fun onBindViewHolder(holder: ItineraryViewHolder, position: Int) {
        val it = itineraries[position]
        holder.bind(it,onItineraryClicked)
    }

}