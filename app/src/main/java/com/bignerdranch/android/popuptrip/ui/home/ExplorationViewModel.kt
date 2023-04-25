package com.bignerdranch.android.popuptrip.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.api.model.Place

private const val TAG = "ExplorationViewModel"
const val STARTING_POINT_NAME = "STARTING_POINT_NAME"
const val DESTINATION_NAME = "DESTINATION_NAME"
const val STARTING_PLACE = "STARTING_PLACE"
const val OLD_TEXT_STRING = "OLD_TEXT_STRING"
class ExplorationViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
//    private lateinit var startPlace: Place
//    private lateinit var destinationPlace: Place
//    private lateinit var startingPointId: String

    var startingPointName: String
        get() = savedStateHandle.get<String>(STARTING_POINT_NAME) ?: ""
        set(value) = savedStateHandle.set(STARTING_POINT_NAME, value)

    var destinationName: String
        get() = savedStateHandle.get<String>(DESTINATION_NAME) ?: ""
        set(value) = savedStateHandle.set(DESTINATION_NAME, value)

    var oldText: String?
        get() = savedStateHandle.get<String>(OLD_TEXT_STRING)
        set(value) = savedStateHandle.set(OLD_TEXT_STRING, value)

//    var startingPlace: Place
//        get() = savedStateHandle.get<Place>(STARTING_PLACE) ?: startPlace
//        set(value) = savedStateHandle.set(STARTING_PLACE, value)
}