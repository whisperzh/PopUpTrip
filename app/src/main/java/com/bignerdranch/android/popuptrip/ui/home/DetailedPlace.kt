package com.bignerdranch.android.popuptrip.ui.home
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import java.util.*

@Entity
data class DetailedPlace(
    @PrimaryKey var placeId: String,
    var placeLatLng: LatLng,
    var placeName: String,
    var placeRating: Float? = null,
    var placeVicinity: String,
    var photoReference: String? = null,
    var placeCategory: String? = null,
    var placeTypes: String? = null,
    var placeOpenNow: Boolean? = null,
    var placeImgBitmap: Bitmap? = null,
    var addedToPlan: Boolean = false,
) {
    // Default constructor
    constructor() : this(
        "dummy id",
        LatLng(0.0, 0.0),
        "",
        placeVicinity = "",)

    // Basic constructor
    constructor(placeId: String) : this (
        placeId = placeId,
        LatLng(0.0, 0.0),
        "",
        placeVicinity = "",)

}


