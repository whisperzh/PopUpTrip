package com.bignerdranch.android.popuptrip.ui.home
import android.widget.ImageView
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import java.util.*

@Entity
data class DetailedPlace(
    @PrimaryKey val placeId: String,
    val placeLatLng: LatLng,
    val placeName: String,
    val placeRating: Float? = null,
    val placeVicinity: String,
    val photoReference: String? = null,
    var placeCategory: String? = null,
    var placeTypes: String? = null,
    var placeOpenNow: Boolean? = null,
) {
    // Default constructor
    constructor() : this(
        "ChIJ-dKkUfd544kR5cY9D2MncuM",
        LatLng(0.0, 0.0),
        "dummy name",
        0.0f,
        "",
        "")
}


