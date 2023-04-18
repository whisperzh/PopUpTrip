package com.bignerdranch.android.popuptrip.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

const val LATITUDE = "LATITUDE"
const val LONGITUDE = "LONGITUDE"
const val DESTINATION_MAP = "DESTINATION_MAP"

class ExplorationMapViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {
    var latitude: Double
        get() = savedStateHandle[LATITUDE] ?: -34.0
        set(value) = savedStateHandle.set(LATITUDE, value)

    var longitude: Double
        get() = savedStateHandle[LONGITUDE] ?: 151.0
        set(value) = savedStateHandle.set(LONGITUDE, value)

    var destinationMap: String
        get() = savedStateHandle[DESTINATION_MAP] ?: "Sydney"
        set(value) = savedStateHandle.set(DESTINATION_MAP, value)
}