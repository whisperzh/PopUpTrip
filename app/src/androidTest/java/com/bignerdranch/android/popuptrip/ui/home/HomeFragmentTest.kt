package com.bignerdranch.android.popuptrip.ui.home

import androidx.fragment.app.testing.FragmentScenario
import com.bignerdranch.android.popuptrip.R

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeFragmentTest {

    @Before
    fun setUp() {
        launchFragmentInContainer<HomeFragment>(
            fragmentArgs = null,
            themeResId = R.style.Theme_PopUpTrip,
            initialState = Lifecycle.State.RESUMED,
            factory = null
        )
    }

    @Test
    fun checkViewsVisibility() {
        launchFragmentInContainer<HomeFragment>()

        // Check if the home_search_box is visible
        onView(withId(R.id.home_search_box))
            .check(matches(isDisplayed()))

//        // Check if the text_nearby is visible
//        onView(withId(R.id.text_nearby))
//            .check(matches(isDisplayed()))
//
//        // Check if the nearby_places_recycler_view is visible
//        onView(withId(R.id.nearby_places_recycler_view))
//            .check(matches(isDisplayed()))
//
//        // Make sure the homeAutoCompleteListView is not visible right after launching
//        onView(withId(R.id.homeAutoCompleteListView))
//            .check(matches(withEffectiveVisibility(Visibility.GONE)))

    }

    @After
    fun tearDown() {
//        scenario.moveToState(Lifecycle.State.DESTROYED)
    }
}