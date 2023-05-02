package com.bignerdranch.android.popuptrip.ui.home

import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.ViewModel
import java.util.*

private const val TAG = "NearbyPlaceListViewModel"
class NearbyPlaceListViewModel: ViewModel() {

    val nearbyPlaces = mutableListOf<DetailedPlace>()

    init {
        nearbyPlaces.clear()
    }

    fun clearPlaceList(){
        nearbyPlaces.clear()
    }
    fun updatePlaces(newPlace: DetailedPlace){
        // fetch the request to obtain the nearby place
//        Log.d(TAG, "updatePlaceIds() is called with placeId ${newPlace.placeId}")

        // add place to nearbyPlaces
        nearbyPlaces.add(newPlace)

//        Log.d(TAG, "new nearbyPlaceViewModel size: ${nearbyPlaces.size}")
    }
}