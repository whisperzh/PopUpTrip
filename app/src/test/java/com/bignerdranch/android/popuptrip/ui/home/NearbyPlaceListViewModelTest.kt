package com.bignerdranch.android.popuptrip.ui.home

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NearbyPlaceListViewModelTest {
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: NearbyPlaceListViewModel

    @Before
    fun setUp() {
        savedStateHandle = SavedStateHandle()
        viewModel = NearbyPlaceListViewModel(savedStateHandle)
    }

    @Test
    fun testUserPreference() {
        val userPref = arrayListOf("bakery", "museum")
        viewModel.userPreferenceList = userPref

        assertEquals(userPref, viewModel.userPreferenceList)
    }

    @Test
    fun testNearbyPlaces() {
        val nearbyPlaces = mutableListOf(DetailedPlace(), DetailedPlace())
        viewModel.nearbyPlaces = nearbyPlaces

        assertEquals(nearbyPlaces, viewModel.nearbyPlaces)
        assertEquals(nearbyPlaces.size, viewModel.nearbyPlaces.size)
    }

    @Test
    fun testNearbyPlacesClear() {
        val nearbyPlaces = mutableListOf(DetailedPlace(), DetailedPlace())
        viewModel.nearbyPlaces = nearbyPlaces

        assertEquals(nearbyPlaces, viewModel.nearbyPlaces)
        assertEquals(nearbyPlaces.size, viewModel.nearbyPlaces.size)

        viewModel.clearPlaceList()

        assertEquals(viewModel.nearbyPlaces.size, 0)
    }

    @Test
    fun testUpdatePlace() {
        val nearbyPlaces = mutableListOf(DetailedPlace(), DetailedPlace())
        viewModel.nearbyPlaces = nearbyPlaces

        assertEquals(nearbyPlaces, viewModel.nearbyPlaces)
        assertEquals(nearbyPlaces.size, viewModel.nearbyPlaces.size)

        viewModel.updatePlaces(DetailedPlace())
        assertEquals(viewModel.nearbyPlaces.size, 3)
    }
}