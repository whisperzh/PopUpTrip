package com.bignerdranch.android.popuptrip.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

const val STARTING_POINT = "STARTING_POINT"
const val DESTINATION = "DESTINATION"

class ExplorationStartDestViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {
    var startingPoint: String
        get() = savedStateHandle[STARTING_POINT] ?: ""
        set(value) = savedStateHandle.set(STARTING_POINT, value)

    var destination: String
        get() = savedStateHandle[DESTINATION] ?: ""
        set(value) = savedStateHandle.set(DESTINATION, value)
}