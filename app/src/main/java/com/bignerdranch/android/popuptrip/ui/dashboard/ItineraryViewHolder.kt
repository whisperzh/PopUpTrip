package com.bignerdranch.android.popuptrip.ui.dashboard

import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.popuptrip.databinding.ItineraryItemBinding
import java.util.*

class ItineraryViewHolder(val binding:ItineraryItemBinding):RecyclerView.ViewHolder(binding.root) {
    fun bind(itinerary: Itinerary, onItineraryClicked: (itineraryId:String) -> Unit) {
        binding.itineraryTitle.text = itinerary.itineraryName
        var startPoint=itinerary.itineraryContent.split(" TO ")[0]
        var endPoint="TO "+itinerary.itineraryContent.split(" TO ")[1]

        binding.itineraryStartPoint.text=startPoint
        binding.itineraryEndPoint.text=endPoint
        binding.root.setOnClickListener {
            onItineraryClicked(itinerary.itineraryId)
        }
    }
}