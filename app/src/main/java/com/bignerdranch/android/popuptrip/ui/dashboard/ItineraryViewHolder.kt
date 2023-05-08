package com.bignerdranch.android.popuptrip.ui.dashboard

import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.popuptrip.databinding.ItineraryItemBinding
import java.util.*

class ItineraryViewHolder(val binding:ItineraryItemBinding):RecyclerView.ViewHolder(binding.root) {
    fun bind(itinerary: Itinerary, onItineraryClicked: (itinerary:String) -> Unit) {
        binding.itineraryTitle.text = itinerary.itineraryName
        binding.itineraryCreateTime.text = itinerary.createTime
        binding.root.setOnClickListener {
            onItineraryClicked(itinerary.itineraryId)
        }
    }
}