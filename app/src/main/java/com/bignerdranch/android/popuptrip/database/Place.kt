package com.bignerdranch.android.popuptrip.database

import android.media.Image
import android.widget.ImageView
import java.util.*

data class Place(
    val id: UUID,
    val name: String,
    val detail: String,
//    val image: ImageView
)
