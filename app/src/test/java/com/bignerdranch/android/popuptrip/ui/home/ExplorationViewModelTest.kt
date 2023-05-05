package com.bignerdranch.android.popuptrip.ui.home

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExplorationViewModelTest {

    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: ExplorationViewModel

    @Before
    fun setUp() {
        savedStateHandle = SavedStateHandle()
        viewModel = ExplorationViewModel(savedStateHandle)
    }

    @Test
    fun setOldTextTest() {
        val text = "test text"
        viewModel.oldText = text
        assertEquals(text, viewModel.oldText)
    }

    @Test
    fun updatePlacesTest() {
        // test place that has placeId 123
        val testPlace = DetailedPlace("123")
        viewModel.updateStartingPlace(testPlace)
        assertEquals(testPlace.placeId, viewModel.startingPlace.placeId)

        val testPlace2 = DetailedPlace("456")
        viewModel.updateDestinationPlace(testPlace2)
        assertEquals(testPlace2.placeId, viewModel.destinationPlace.placeId)
    }

    @Test
    fun resetStartPlaceTest() {
        val testPlace = DetailedPlace()
        viewModel.resetStartingPlace()
        assertEquals(testPlace, viewModel.startingPlace)
    }


}