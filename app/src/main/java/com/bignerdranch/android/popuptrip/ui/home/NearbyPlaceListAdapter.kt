package com.bignerdranch.android.popuptrip.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.popuptrip.database.Place
import com.bignerdranch.android.popuptrip.databinding.ListItemNearbyPlaceBinding

class NearbyPlaceHolder(
    val binding: ListItemNearbyPlaceBinding
) : RecyclerView.ViewHolder(binding.root) {
}

private const val TAG = "NearbyPlaceListAdapter"
class NearbyPlaceListAdapter (
    private val places: List<DetailedPlace>
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
//            Log.d(TAG, "viewHolder applied at position $position")
            binding.placeName.text = place.placeName
            binding.placeVicinity.text = place.placeVicinity
            binding.placeRating.rating = place.placeRating
        }
    }
    override fun getItemCount() = places.size
}
