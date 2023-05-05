package com.bignerdranch.android.popuptrip

import android.os.Bundle
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bignerdranch.android.popuptrip.databinding.ActivityMainBinding
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var doNotLogout:Boolean=false

    override fun onStart() {
        super.onStart()
        Toast.makeText(
            this,
            "You've been logged in.",
            Toast.LENGTH_SHORT,
        ).show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Places.isInitialized()) {
           Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY);
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard,
                R.id.navigation_profile, R.id.navigation_settings
            )
        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
//        navView.setOnItemSelectedListener {
//            when (it.itemId) {
//                R.id.navigation_settings -> {
//                    navController
//                        .navigate(R.id.navigation_settings)
//                }
//                R.id.navigation_profile -> {
//                    navController
//                        .navigate(R.id.navigation_profile)
//                }
//                R.id.navigation_home -> {
//                    navController
//                        .navigate(R.id.navigation_home)
//                }
//                R.id.navigation_dashboard -> {
//                    navController
//                        .navigate(R.id.navigation_dashboard)
//                }
//            }
//            true
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(doNotLogout)
        {
            doNotLogout=false
            return
        }
        Firebase.auth.signOut()
        Toast.makeText(this,"You have been logged out", Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()

    }
}