package com.bignerdranch.android.popuptrip.ui.home

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
        get() = savedStateHandle.get<Place>(STARTING_PLACE) ?: initStartPlace()
        set(value) = savedStateHandle.set(STARTING_PLACE, value)

    var destination: Place
        get() = savedStateHandle.get<Place>(DESTINATION_PLACE) ?: initDestination()
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

    // The following 2 functions were inspired by
    // https://developers.google.com/maps/documentation/places/android-sdk/reference/com/google/android/libraries/places/api/model/Place.Builder
    private fun initStartPlace(): Place {
        val placeBuilder = Place.builder()
            .setAddress("655 Commonwealth Ave, Boston, MA 02215, USA")
            .setId("ChIJ-dKkUfd544kR5cY9D2MncuM")
            // set LatLng to 0, 0 so that start place is updated with current location, i.e. if LatLng refers to init start place -> placeholder used, need to get current location
            .setLatLng(LatLng(0.0, 0.0))
//            .setLatLng(LatLng(42.34993389999999,-71.1027624))
            .setName("655 Commonwealth Ave")

        return placeBuilder.build()
    }

    private fun initDestination(): Place {
        val placeBuilder = Place.builder()
            .setAddress("575 Memorial Dr, Cambridge, MA 02139, USA")
            .setId("ChIJEU9Qpft544kRJ4DIMqb2VhA")
            .setLatLng(LatLng(42.3544178,-71.1057659))
            .setName("575 Memorial Dr")

        return placeBuilder.build()
    }

}