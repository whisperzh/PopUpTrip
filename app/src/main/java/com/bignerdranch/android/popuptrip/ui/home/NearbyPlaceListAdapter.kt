package com.bignerdranch.android.popuptrip.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.popuptrip.database.Place
import com.bignerdranch.android.popuptrip.databinding.ListItemNearbyPlaceBinding

class NearbyPlaceHolder(
    val binding: ListItemNearbyPlaceBinding
) : RecyclerView.ViewHolder(binding.root) {
}

class NearbyPlaceListAdapter (
    private val places: List<Place>
) : RecyclerView.Adapter<NearbyPlaceHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) : NearbyPlaceHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemNearbyPlaceBinding.inflate(inflater, parent, false)
        return NearbyPlaceHolder(binding)
    }
    override fun onBindViewHolder(holder: NearbyPlaceHolder, position: Int) {
        val place = places[position]
        holder.apply {
            binding.placeName.text = place.name
            binding.placeDescription.text = place.detail
        }
    }
    override fun getItemCount() = places.size
}
