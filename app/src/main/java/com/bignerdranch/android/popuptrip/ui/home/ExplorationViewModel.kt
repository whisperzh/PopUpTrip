package com.bignerdranch.android.popuptrip.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.api.model.Place

private const val TAG = "ExplorationViewModel"
const val STARTING_POINT_NAME = "STARTING_POINT_NAME"
const val DESTINATION_NAME = "DESTINATION_NAME"
const val STARTING_PLACE = "STARTING_PLACE"
class ExplorationViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    var startingPointName: String
        get() = savedStateHandle.get<String>(STARTING_POINT_NAME) ?: ""
        set(value) = savedStateHandle.set(STARTING_POINT_NAME, value)

    var destinationName: String
        get() = savedStateHandle.get<String>(DESTINATION_NAME) ?: ""
        set(value) = savedStateHandle.set(DESTINATION_NAME, value)

    var startingPlace: Place?
        get() = savedStateHandle.get<Place>(STARTING_PLACE)
        set(value) = savedStateHandle.set(STARTING_PLACE, value)
}