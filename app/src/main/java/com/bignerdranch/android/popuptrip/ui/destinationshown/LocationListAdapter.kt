package com.bignerdranch.android.popuptrip.ui.destinationshown//import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.popuptrip.ui.destinationshown.LocationItem
import com.bignerdranch.android.popuptrip.R
import android.view.LayoutInflater
import android.widget.TextView


class LocationListAdapter(private val locationList: List<LocationItem>) : RecyclerView.Adapter<LocationListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLocationName: TextView = itemView.findViewById(R.id.tv_location_name)
        val tvStartTime: TextView = itemView.findViewById(R.id.tv_start_time)
        val tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_location_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val locationItem = locationList[position]
        holder.tvLocationName.text = locationItem.name
        holder.tvStartTime.text = locationItem.startTime
        holder.tvDuration.text = locationItem.duration
    }

    override fun getItemCount(): Int {
        return locationList.size
    }
}



