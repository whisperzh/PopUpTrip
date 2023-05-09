package com.bignerdranch.android.popuptrip

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bignerdranch.android.popuptrip.databinding.ActivityMainBinding
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*


const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var doNotLogout:Boolean=false
    private val languageSettingList = listOf(Locale.ENGLISH,Locale.FRANCE,Locale.GERMAN,Locale.forLanguageTag("es"),Locale.SIMPLIFIED_CHINESE)
    private val languageTag= listOf("en","fr","de","es","zh")
    private lateinit var prefs:SharedPreferences
    var user:UserEntity= UserEntity()
    private lateinit var database: DatabaseReference
    private lateinit var loginToast:Toast

    override fun onStart() {
        super.onStart()
        loginToast = Toast.makeText(
            this,
            R.string.login_success,
            Toast.LENGTH_SHORT,
        )
        loginToast.show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "MainActivity onCreate")
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val selectedItem=prefs.getString("Language","")
        user.uid= prefs.getString("USER_ID","")!!

        database = Firebase.database.reference
        database.child("User_Table").child(user.uid).child("username").get().addOnSuccessListener {
            user.userName= it.value as String
            prefs.edit().putString("USER_NAME",user!!.userName).commit()
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
        when(selectedItem)
        {
            "English"->changeLanguageSetting(0)
            "Français"->changeLanguageSetting(1)
            "Deutsch"->changeLanguageSetting(2)
            "Español"->changeLanguageSetting(3)
            "简体中文"->changeLanguageSetting(4)
        }

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
        navView.setupWithNavController(navController)
        updateBottomNavigationMenu()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(doNotLogout) {
            doNotLogout=false
            return
        }
        Firebase.auth.signOut()
        loginToast.cancel()
//        Toast.makeText(this,getString(R.string.logged_out), Toast.LENGTH_SHORT).show()
    }

    fun updateSettingUI() {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navController
            .navigate(R.id.navigation_settings)
    }

    private fun changeLanguageSetting(token:Int){
        val locale = Locale(languageTag.get(token))
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(languageSettingList.get(token))
        val resources = this.resources
        val displayMetrics = resources.displayMetrics
        resources.updateConfiguration(config, displayMetrics)
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("xx-YY")
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    private fun updateBottomNavigationMenu() {
        val menu = findViewById<BottomNavigationView>(R.id.nav_view).menu
        menu.apply {
            findItem(R.id.navigation_home).setTitle(R.string.title_home)
            findItem(R.id.navigation_dashboard).setTitle(R.string.title_dashboard)
            findItem(R.id.navigation_profile).setTitle(R.string.title_profile)
            findItem(R.id.navigation_settings).setTitle(R.string.title_setting)
        }
    }
}