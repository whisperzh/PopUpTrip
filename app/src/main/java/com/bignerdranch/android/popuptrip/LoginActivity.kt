package com.bignerdranch.android.popuptrip

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.bignerdranch.android.popuptrip.databinding.ActivityLoginBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    // See: https://developer.android.com/training/basics/intents/result
    private lateinit var auth: FirebaseAuth
    override fun onStart() {
        super.onStart()
        auth = Firebase.auth

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityLoginBinding.inflate(layoutInflater)
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val mode = prefs.getInt("mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(mode) //read the previous setting for dark mode
        setContentView(binding.root)

        // Register the permissions callback, which handles the user's response to the
        // system permissions dialog. Save the return value, an instance of
        // ActivityResultLauncher. You can use either a val, as shown in this snippet,
        // or a lateinit var in your onAttach() or onCreate() method.
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                    //Toast.makeText(this,"Sorry, you cannot login without permission",Toast.LENGTH_LONG).show()
                    //Write dialog
                    val alertDialog = AlertDialog.Builder(this)
                    alertDialog.setTitle("Permission denied")
                        .setMessage("Sorry, you cannot login without permission.")
                        .setPositiveButton("OK") { _, _ ->
                        }
                        .create()

                    alertDialog.show()
                }
            }

        requestPermissionLauncher.launch(
            Manifest.permission.ACCESS_NETWORK_STATE)
        requestPermissionLauncher.launch(
            Manifest.permission.INTERNET)
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_NETWORK_STATE
            ) == PackageManager.PERMISSION_GRANTED
                    &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.INTERNET
            ) == PackageManager.PERMISSION_GRANTED
            -> {
                // You can use the API that requires the permission.
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_NETWORK_STATE) -> {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected, and what
            // features are disabled if it's declined. In this UI, include a
            // "cancel" or "no thanks" button that lets the user continue
            // using your app without granting the permission.
//            showInContextUI(...)
                val alertDialog = AlertDialog.Builder(this)
                    .setTitle("Network State Permission Required")
                    .setMessage("This app requires access to the network state to provide a better user experience. " +
                            "Granting this permission allows the app to check if your device is connected to the internet before login.")
                    .setPositiveButton("OK") { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_NETWORK_STATE)
                    }
                    .setNegativeButton("No Thanks") { _, _ ->
                        //TODO:user don't give permission
                    }
                    .create()

                alertDialog.show()
        }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_NETWORK_STATE)
                requestPermissionLauncher.launch(
                    Manifest.permission.INTERNET)
            }
        }

    }




}