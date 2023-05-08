package com.bignerdranch.android.popuptrip.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.LocalDate

class DashboardViewModel : ViewModel() {

    private var _itineraries: MutableStateFlow<MutableList<Itinerary>?> = MutableStateFlow(
        mutableListOf()
    )
    var itineraries: StateFlow<MutableList<Itinerary>?> = _itineraries.asStateFlow()


    init {
        viewModelScope.launch {
            var cach:MutableList<Itinerary> = mutableListOf()
            for(i in 1..10)
                cach.add(Itinerary(i.toString(),"it$i",LocalDate.now().toString()))
            _itineraries.value=cach
        }
    }
}