package com.bignerdranch.android.popuptrip.ui.home

import android.widget.ImageView
import androidx.lifecycle.ViewModel
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.database.Place
import java.util.*

class NearbyPlaceListViewModel: ViewModel() {

    val places = mutableListOf<Place>()

    init {
        for (i in 0 until 100) {
            val place = Place(
                id = UUID.randomUUID(),
                name ="Place #$i",
                detail = "Place details..."
            )
            places += place
        }
    }
}