package com.bignerdranch.android.popuptrip.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

const val DESTINATION_ENTERED = "DESTINATION_ENTERED"
const val STARTING_POINT_ENTERED = "STARTING_POINT_ENTERED"

class ExplorationViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    var destinationEntered: String
        get() = savedStateHandle[DESTINATION_ENTERED] ?: ""
        set(value) = savedStateHandle.set(DESTINATION_ENTERED, value)

    var startingPointEntered: String
        get() = savedStateHandle[STARTING_POINT_ENTERED] ?: ""
        set(value) = savedStateHandle.set(STARTING_POINT_ENTERED, value)
}