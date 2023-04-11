package com.bignerdranch.android.popuptrip.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

const val DESTINATION_ENTERED = "DESTINATION_ENTERED"
const val STARTING_POINT_ENTERED = "STARTING_POINT_ENTERED"

class ExplorationViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    var destination_entered: String
        get() = savedStateHandle[DESTINATION_ENTERED] ?: ""
        set(value) = savedStateHandle.set(DESTINATION_ENTERED, value)

    var starting_point_entered: String
        get() = savedStateHandle[STARTING_POINT_ENTERED] ?: ""
        set(value) = savedStateHandle.set(STARTING_POINT_ENTERED, value)
}