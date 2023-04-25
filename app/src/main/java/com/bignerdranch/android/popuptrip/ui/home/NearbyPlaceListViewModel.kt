package com.bignerdranch.android.popuptrip.ui.home

import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.ViewModel
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.database.Place
import java.util.*

private const val TAG = "NearbyPlaceListViewModel"
class NearbyPlaceListViewModel: ViewModel() {

    val nearbyPlaces = mutableListOf<DetailedPlace>()

    init {
//        for (i in 0 until 100) {
//            val place = Place(
//                id = UUID.randomUUID(),
//                name ="Place #$i",
//                detail = "Place details..."
//            )
//            places += place
//        }

//        nearbyPlaces.clear()
        Log.d(TAG, "nearbyPlaceViewModel init is called, places size: ${nearbyPlaces.size}")
    }

    fun updatePlaces(newPlace: DetailedPlace){
        // fetch the request to obtain the nearby place
//        Log.d(TAG, "updatePlaceIds() is called with placeId ${newPlace.placeId}")

        // add place to nearbyPlaces
        nearbyPlaces.add(newPlace)

//        Log.d(TAG, "new nearbyPlaceViewModel size: ${nearbyPlaces.size}")
    }
}