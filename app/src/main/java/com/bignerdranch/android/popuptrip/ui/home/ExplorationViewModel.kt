package com.bignerdranch.android.popuptrip.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.api.model.Place

const val DEST_NAME = "DEST_NAME"
private const val TAG = "ExplorationViewModel"
class ExplorationViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private var destName: String
        get() = savedStateHandle.get(DEST_NAME) ?: ""
        set(value) = savedStateHandle.set(DEST_NAME, value)


}