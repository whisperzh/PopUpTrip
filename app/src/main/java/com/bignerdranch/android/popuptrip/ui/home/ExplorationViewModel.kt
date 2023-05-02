package com.bignerdranch.android.popuptrip.ui.home

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
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
const val PLACES_TO_ADD_TO_ROUTE = "PLACES_TO_ADD_TO_ROUTE"
const val MARKERS_TO_ADD = "MARKERS_TO_ADD"
const val POLYLINE = "POLYLINE"
const val STARTING_POINT = "STARTING_POINT"
const val NEED_TO_FETCH = "NEED_TO_FETCH"
//const val MAP_VIEW = "MAP_VIEW"

class ExplorationViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    // include entire world
    private var mapBound: LatLngBounds = LatLngBounds.builder()
        .include(LatLng(-90.0, -180.0))
        .include(LatLng(90.0, 180.0))
        .build()
    private var maxSWBound: LatLng = LatLng(0.0, 0.0)
    private var maxNEBound: LatLng = LatLng(0.0, 0.0)

    // 655 Commonwealth Avenue
    private var startingId: String = "ChIJ-dKkUfd544kR5cY9D2MncuM"
    // 575 Memorial Drive
    private var destinationId: String = "ChIJEU9Qpft544kRJ4DIMqb2VhA"
//    private var polylineDefault: Polyline = ""

    var oldText: String?
        get() = savedStateHandle.get<String>(OLD_TEXT_STRING)
        set(value) = savedStateHandle.set(OLD_TEXT_STRING, value)

    var needToFetch: Boolean?
        get() = savedStateHandle.get<Boolean>(NEED_TO_FETCH) ?: true
        set(value) = savedStateHandle.set(NEED_TO_FETCH, value)

    private var _destinationPlace = DetailedPlace()
    val destinationPlace: DetailedPlace get() = _destinationPlace

    private var _startingPlace = DetailedPlace()
    val startingPlace: DetailedPlace get() = _startingPlace

    var mapBounds: LatLngBounds
        get() = savedStateHandle.get<LatLngBounds>(MAP_BOUNDS) ?: mapBound
        set(value) = savedStateHandle.set(MAP_BOUNDS, value)

    var maxSWBounds: LatLng
        get() = savedStateHandle.get<LatLng>(MAX_SW_BOUNDS) ?: maxSWBound
        set(value) = savedStateHandle.set(MAX_SW_BOUNDS, value)

    var maxNEBounds: LatLng
        get() = savedStateHandle.get<LatLng>(MAX_NE_BOUNDS) ?: maxNEBound
        set(value) = savedStateHandle.set(MAX_NE_BOUNDS, value)

//    var mapView: SupportMapFragment
//        get() = savedStateHandle.get<SupportMapFragment>(MAP_VIEW) ?: supportMapFragment
//        set(value) = savedStateHandle.set(MAP_VIEW, value)

    var placesToAddToRoute: ArrayList<DetailedPlace>
        get() = savedStateHandle.get<ArrayList<DetailedPlace>>(PLACES_TO_ADD_TO_ROUTE) ?: ArrayList()
        set(value) = savedStateHandle.set(PLACES_TO_ADD_TO_ROUTE, value)

    var markersAdded: ArrayList<Marker>
        get() = savedStateHandle.get<ArrayList<Marker>>(MARKERS_TO_ADD) ?: ArrayList()
        set(value) = savedStateHandle.set(MARKERS_TO_ADD, value)

    var startingPoint: ArrayList<Any>
        get() = savedStateHandle.get<ArrayList<Any>>(STARTING_POINT) ?: arrayListOf("655 Commonwealth Ave", LatLng(0.0, 0.0))
        set(value) = savedStateHandle.set(STARTING_POINT, value)

//    var polyline: Polyline
//        get() = savedStateHandle.get<Polyline>(POLYLINE) ?: polylineDefault
//        set(value) = savedStateHandle.set(POLYLINE, value)

    fun updateDestinationPlace(place: DetailedPlace){
        _destinationPlace = place
    }

    fun updateStartingPlace(place: DetailedPlace){
        _startingPlace = place
    }
    fun updatePlaceId(place: String, id: String){
        if (place=="s"){
            _startingPlace.placeId = id
        } else {
            _destinationPlace.placeId = id
        }
    }

    fun updatePlaceLatLng(place: String, latLng: LatLng){
        if (place=="s"){
            _startingPlace.placeLatLng ?: latLng
        } else {
            _destinationPlace.placeLatLng ?: latLng
        }
    }

    fun updatePlaceName(place: String, name: String){
        if (place=="s"){
            _startingPlace.placeName ?: name
        } else {
            _destinationPlace.placeName ?: name
        }
    }

    fun updatePlaceRating(place: String, rating: Float){
        if (place=="s"){
            startingPlace?.placeRating = rating
        } else {
            destinationPlace?.placeRating = rating
        }
    }

    fun updatePlaceVicinity(place: String, vicinity: String){
        if (place=="s"){
            startingPlace?.placeVicinity ?: vicinity
        } else {
            destinationPlace?.placeVicinity ?: vicinity
        }
    }

    fun updatePlacePhotoRef(place: String, photoRef: String){
        if (place=="s"){
            startingPlace?.photoReference = photoRef
        } else {
            destinationPlace?.photoReference = photoRef
        }
    }

    fun updatePlaceCategory(place: String, cat: String){
        if (place=="s"){
            _startingPlace.placeCategory = cat
        } else {
            _destinationPlace.placeCategory = cat
        }
    }

    fun updatePlaceTypes(place: String, types: String){
        if (place=="s"){
            startingPlace?.placeTypes = types
        } else {
            destinationPlace?.placeTypes = types
        }
    }

    fun updatePlaceOpen(place: String, isOpen: Boolean){
        if (place=="s"){
            startingPlace?.placeOpenNow = isOpen
        } else {
            destinationPlace?.placeOpenNow = isOpen
        }
    }

    fun updatePlaceImg(place: String, bitmap: Bitmap){
        if (place=="s"){
            startingPlace?.placeImgBitmap = bitmap
        } else {
            destinationPlace?.placeImgBitmap = bitmap
        }
    }

    fun updatePlaceAdded(place: String, added: Boolean){
        if (place=="s"){
            startingPlace?.addedToPlan ?: added
        } else {
            destinationPlace?.addedToPlan ?: added
        }
    }

}