package com.bignerdranch.android.popuptrip.ui.home

import android.content.Context
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bignerdranch.android.popuptrip.BuildConfig.MAPS_API_KEY
import com.bignerdranch.android.popuptrip.R
import com.google.android.libraries.places.api.Places
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExplorationFragmentTest {

    @Before
    fun setUp() {
        // Adapted from
        // https://stackoverflow.com/questions/55770406/android-cannot-initialize-google-places
        val context = ApplicationProvider.getApplicationContext<Context>()
        Places.initialize(context, MAPS_API_KEY)

        launchFragmentInContainer<ExplorationFragment>(
            fragmentArgs = bundleOf("destinationPlaceId" to DetailedPlace().placeId,
                "destinationPlace" to DetailedPlace().toString()),
            themeResId = R.style.Theme_PopUpTrip,
            initialState = Lifecycle.State.RESUMED,
            factory = null
        )
    }

    @Test
    fun testExplorationView() {
        // check if use current location button is present
        onView(withId(R.id.use_current_location_button))
            .check(matches(isDisplayed()))

        // check if start text input layout is present
        onView(withId(R.id.startingTextInputLayout))
            .check(matches(isDisplayed()))

        // check if dest text input layout is present
        onView((withId(R.id.destTextInputLayout)))
            .check(matches(isDisplayed()))
    }

    @After
    fun tearDown() {
    }
}