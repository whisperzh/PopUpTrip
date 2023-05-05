package com.bignerdranch.android.popuptrip.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker

private const val TAG = "ExplorationViewModel"

const val OLD_TEXT_STRING = "OLD_TEXT_STRING"
const val MAP_BOUNDS = "MAP_BOUNDS"
const val MAX_SW_BOUNDS = "MAX_SW_BOUNDS"
const val MAX_NE_BOUNDS = "MAX_NE_BOUNDS"
const val PLACES_TO_ADD_TO_ROUTE = "PLACES_TO_ADD_TO_ROUTE"
const val MARKERS_TO_ADD = "MARKERS_TO_ADD"
const val POLYLINE = "POLYLINE"
const val STARTING_POINT = "STARTING_POINT"
const val DESTINATION_POINT = "DESTINATION_POINT"
const val PLACES_TO_ADD_POINTS = "PLACES_TO_ADD_POINTS"
const val NEED_TO_FETCH = "NEED_TO_FETCH"
const val PLACE_TYPES = "PLACE_TYPES"

class ExplorationViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    // include entire world
    private var mapBound: LatLngBounds = LatLngBounds.builder()
        .include(LatLng(-90.0, -180.0))
        .include(LatLng(90.0, 180.0))
        .build()

    private var maxSWBound: LatLng = LatLng(0.0, 0.0)
    private var maxNEBound: LatLng = LatLng(0.0, 0.0)

    var needToFetch: Boolean?
        get() = savedStateHandle.get<Boolean>(NEED_TO_FETCH) ?: true
        set(value) = savedStateHandle.set(NEED_TO_FETCH, value)

    private var _destinationPlace = DetailedPlace()
    val destinationPlace: DetailedPlace get() = _destinationPlace

    private var _startingPlace = DetailedPlace()
    val startingPlace: DetailedPlace get() = _startingPlace

    var oldText: String?
        get() = savedStateHandle.get<String>(OLD_TEXT_STRING)
        set(value) = savedStateHandle.set(OLD_TEXT_STRING, value)

    var mapBounds: LatLngBounds
        get() = savedStateHandle.get<LatLngBounds>(MAP_BOUNDS) ?: mapBound
        set(value) = savedStateHandle.set(MAP_BOUNDS, value)

    var maxSWBounds: LatLng
        get() = savedStateHandle.get<LatLng>(MAX_SW_BOUNDS) ?: maxSWBound
        set(value) = savedStateHandle.set(MAX_SW_BOUNDS, value)

    var maxNEBounds: LatLng
        get() = savedStateHandle.get<LatLng>(MAX_NE_BOUNDS) ?: maxNEBound
        set(value) = savedStateHandle.set(MAX_NE_BOUNDS, value)

    var placesToAddToRoute: ArrayList<DetailedPlace>
        get() = savedStateHandle.get<ArrayList<DetailedPlace>>(PLACES_TO_ADD_TO_ROUTE) ?: ArrayList()
        set(value) = savedStateHandle.set(PLACES_TO_ADD_TO_ROUTE, value)

    var markersAdded: ArrayList<Marker>
        get() = savedStateHandle.get<ArrayList<Marker>>(MARKERS_TO_ADD) ?: ArrayList()
        set(value) = savedStateHandle.set(MARKERS_TO_ADD, value)

    var startingPoint: ArrayList<Any>
        get() = savedStateHandle.get<ArrayList<Any>>(STARTING_POINT) ?: arrayListOf("655 Commonwealth Ave", 0.0, 0.0)
        set(value) = savedStateHandle.set(STARTING_POINT, value)

    var destinationPoint: ArrayList<Any>
        get() = savedStateHandle.get<ArrayList<Any>>(DESTINATION_POINT) ?: arrayListOf("575 Memorial Dr", 0.0, 0.0)
        set(value) = savedStateHandle.set(DESTINATION_POINT, value)

    var placesToAddPoints: ArrayList<Any>
        get() = savedStateHandle.get<ArrayList<Any>>(PLACES_TO_ADD_POINTS) ?: ArrayList()
        set(value) = savedStateHandle.set(PLACES_TO_ADD_POINTS, value)

    var polyline: ArrayList<String>
        get() = savedStateHandle.get<ArrayList<String>>(POLYLINE) ?: ArrayList()
        set(value) = savedStateHandle.set(POLYLINE, value)

    var placeTypes: ArrayList<String>
        get() = savedStateHandle.get<ArrayList<String>>(PLACE_TYPES) ?: ArrayList()
        set(value) = savedStateHandle.set(PLACE_TYPES, value)

    fun updateDestinationPlace(place: DetailedPlace){
        _destinationPlace = place
    }

    fun updateStartingPlace(place: DetailedPlace){
        _startingPlace = place
    }

    fun resetStartingPlace(){
        _startingPlace = DetailedPlace()
    }

}