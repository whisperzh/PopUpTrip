# PopUpTrip

PopUpTrip is an Android travel planning app that simplifies the process of organizing trips and discovering nearby attractions. 
With the help of this app, users can easily create itineraries and explore the area around them.

## Features
* User authentication with sign-in and sign-up functionality.
* User authentication with reset password and profile data migration functionality.
* Customize your experiences based on your preferences under personal profile.
* Travel history and user preferences are stored in a cloud database associated with the user account.
* Discover nearby attractions and points of interest using the integrated Google Maps API.
* Create your travel itineraries with an easy-to-use interface.
* Optimize itineraries' travelling time to allow users to travel the places they want to in the shortest time. 
* Get detailed information about places, such as ratings, photos, and descriptions.
* Search for specific locations using the autocomplete search feature.
* Receive real-time updates on nearby attractions as you move around.


## Installation
App is not deployed to Google Play Store, so to install the app:

1. Clone the repository: https://github.com/whisperzh/PopUpTrip
2. Open the project in Android Studio.
3. Ensure that the branch is set to main.
4. Build and run the app on an Android device or emulator.
- minSdk 30
- targetSdk 33
- compileSdk 33

Make sure to include the required API keys for Google Maps and Places SDK.\
In `local.properties`:\
MAPS_API_KEY = xxxxxxxxxxxxxx

## Dependencies

PopUpTrip uses the following dependencies:
* [Google Maps Android API](https://developers.google.com/maps/documentation/android-sdk/start)
* [Places SDK for Android](https://developers.google.com/maps/documentation/places/android-sdk/start)
* [Google Material 3](https://m3.material.io/)
* [Firebase](https://firebase.google.com/)
* [Android Architecture Components](https://developer.android.com/topic/libraries/architecture)

## Backend code repository
The github repo for the backend code: https://github.com/lianghuanjia/501Backend\
The code in this repository does not need to be manually run.

