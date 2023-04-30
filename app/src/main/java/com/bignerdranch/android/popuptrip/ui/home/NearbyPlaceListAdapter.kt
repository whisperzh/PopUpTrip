package com.bignerdranch.android.popuptrip.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.popuptrip.databinding.ListItemNearbyPlaceBinding
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import com.bignerdranch.android.popuptrip.BuildConfig.MAPS_API_KEY
import kotlinx.coroutines.launch


class NearbyPlaceHolder(
    val binding: ListItemNearbyPlaceBinding,
    private val onPlaceClick: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        onPlaceClick(adapterPosition)
    }
}

private const val TAG = "NearbyPlaceListAdapter"
class NearbyPlaceListAdapter (
    private val places: List<DetailedPlace>,
    private val onPlaceClick: (Int) -> Unit
) : RecyclerView.Adapter<NearbyPlaceHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) : NearbyPlaceHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemNearbyPlaceBinding.inflate(inflater, parent, false)
        return NearbyPlaceHolder(binding, onPlaceClick)
    }
    override fun onBindViewHolder(holder: NearbyPlaceHolder, position: Int) {
        val place = places[position]

        holder.apply {
//            Log.d(TAG, "viewHolder applied at position $position")
            binding.placeName.text = place.placeName
            binding.placeVicinity.text = place.placeVicinity
            if(place.placeRating!=null){
                binding.placeRating.rating = place.placeRating
            } else {
                binding.placeRating.visibility = View.GONE
            }


            val photoRef = place.photoReference
            MainScope().launch {
                val bitmap = fetchPlaceImage(photoRef, binding.placeImg.maxWidth, MAPS_API_KEY)
                if (bitmap != null) {
                    binding.placeImg.setImageBitmap(bitmap)
                    Log.d(TAG, "ImageView fetch succeeded")
                } else {
                    // Handle the error (e.g., show a placeholder or error image)
                    Log.d(TAG, "ImageView fetch failed")
                }
            }

        }




    }
    override fun getItemCount() = places.size

    private suspend fun fetchPlaceImage(photoReference: String, maxWidth: Int, apiKey: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            val apiUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=$maxWidth&photoreference=$photoReference&key=$apiKey"
            var bitmap: Bitmap? = null

            try {
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream: InputStream = connection.inputStream
                    bitmap = BitmapFactory.decodeStream(inputStream)
                }

                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            bitmap
        }
    }
}
