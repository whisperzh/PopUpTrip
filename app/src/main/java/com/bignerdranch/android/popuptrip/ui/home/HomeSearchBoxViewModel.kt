package com.bignerdranch.android.popuptrip.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle

const val DEST_ENTERED = "DESTINATION_ENTERED"

class HomeSearchBoxViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {
    var dest: String
        get() = savedStateHandle[DEST_ENTERED] ?: ""
        set(value) = savedStateHandle.set(DEST_ENTERED, value)
}