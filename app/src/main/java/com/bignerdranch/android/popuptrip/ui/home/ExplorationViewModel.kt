package com.bignerdranch.android.popuptrip.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.Place

private const val TAG = "ExplorationViewModel"
const val STARTING_POINT_NAME = "STARTING_POINT_NAME"
const val DESTINATION_NAME = "DESTINATION_NAME"
const val STARTING_PLACE = "STARTING_PLACE"
const val OLD_TEXT_STRING = "OLD_TEXT_STRING"
const val DESTINATION_PLACE = "DESTINATION_PLACE"
const val MAP_BOUNDS = "MAP_BOUNDS"
const val MAX_SW_BOUNDS = "MAX_SW_BOUNDS"
const val MAX_NE_BOUNDS = "MAX_NE_BOUNDS"
const val STARTING_POINT_ID = "STARTING_POINT_ID"
const val DESTINATION_POINT_ID = "DESTINATION_POINT_ID"

class ExplorationViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private var startPlace: Place = Place.builder().build()
    private var destinationPlace: Place = Place.builder().build()
    // include entire world
    private var mapBound: LatLngBounds = LatLngBounds.builder()
        .include(LatLng(-90.0, -180.0))
        .include(LatLng(90.0, 180.0))
        .build()
    private var maxSWBound: LatLng = LatLng(0.0, 0.0)
    private var maxNEBound: LatLng = LatLng(0.0, 0.0)
    // 655 Commonwealth Avenue
    private var startingId: String = "ChIJ-dKkUfd544kR5cY9D2MncuM"
    // 111 Huntington Avenue
    private var destinationId: String = "ChIJh6XMxhF644kRNS4bI8jjFC4"

    var startingPointName: String
        get() = savedStateHandle.get<String>(STARTING_POINT_NAME) ?: ""
        set(value) = savedStateHandle.set(STARTING_POINT_NAME, value)

    var destinationName: String
        get() = savedStateHandle.get<String>(DESTINATION_NAME) ?: ""
        set(value) = savedStateHandle.set(DESTINATION_NAME, value)

    var oldText: String?
        get() = savedStateHandle.get<String>(OLD_TEXT_STRING)
        set(value) = savedStateHandle.set(OLD_TEXT_STRING, value)

    var startingPlace: Place
        get() = savedStateHandle.get<Place>(STARTING_PLACE) ?: startPlace
        set(value) = savedStateHandle.set(STARTING_PLACE, value)

    var destination: Place
        get() = savedStateHandle.get<Place>(DESTINATION_PLACE) ?: destinationPlace
        set(value) = savedStateHandle.set(DESTINATION_PLACE, value)

    var mapBounds: LatLngBounds
        get() = savedStateHandle.get<LatLngBounds>(MAP_BOUNDS) ?: mapBound
        set(value) = savedStateHandle.set(MAP_BOUNDS, value)

    var maxSWBounds: LatLng
        get() = savedStateHandle.get<LatLng>(MAX_SW_BOUNDS) ?: maxSWBound
        set(value) = savedStateHandle.set(MAX_SW_BOUNDS, value)

    var maxNEBounds: LatLng
        get() = savedStateHandle.get<LatLng>(MAX_NE_BOUNDS) ?: maxNEBound
        set(value) = savedStateHandle.set(MAX_NE_BOUNDS, value)

    var startingPointId: String
        get() = savedStateHandle.get<String>(STARTING_POINT_ID) ?: startingId
        set(value) = savedStateHandle.set(STARTING_POINT_ID, value)

    var destinationPointId: String
        get() = savedStateHandle.get<String>(DESTINATION_POINT_ID) ?: destinationId
        set(value) = savedStateHandle.set(DESTINATION_POINT_ID, value)
}