package com.bignerdranch.android.popuptrip.ui.setting

import android.R
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.setApplicationLocales
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.bignerdranch.android.popuptrip.MainActivity
import com.bignerdranch.android.popuptrip.databinding.FragmentSettingBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*
import com.bignerdranch.android.popuptrip.R as popR


class SettingFragment : Fragment() {
    private var lastSelectedItem: String? = null
    private var _binding: FragmentSettingBinding? = null
    private val dataList =  listOf("English","Français","Deutsch","Español","简体中文")
    private val languageSettingList = listOf(Locale.ENGLISH,Locale.FRANCE,Locale.GERMAN,Locale.forLanguageTag("es"),Locale.SIMPLIFIED_CHINESE)
    private val languageTag= listOf("en","fr","de","es","zh")
    private lateinit var overlayout: FrameLayout
    private var restart=false;

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchState = prefs.getBoolean("switchState", false)
        lastSelectedItem=prefs.getString("Language",null)
        binding.themeSwitch.isChecked = switchState
        val adapter =
            ArrayAdapter(requireContext(), R.layout.simple_spinner_item, dataList)//set adapter
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter//bind the adapter
        var applyChangeOnLanguage=false
        var position=0
        when(lastSelectedItem)
        {
            "English"->position=0
            "Français"->position=1
            "Deutsch"->position=2
            "Español"->position=3
            "简体中文"->position=4
        }
        binding.spinner.setSelection(position)
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(!applyChangeOnLanguage) {
                    applyChangeOnLanguage=true
                    return
                }
                val selectedItem = parent.getItemAtPosition(position) as String
                prefs.edit().putString("Language",selectedItem).commit()
                if (lastSelectedItem != null) {
                    val toast = Toast.makeText(
                        requireContext(),
                        "${getString(popR.string.choice)} $selectedItem",
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                    Log.d("MyFragment", "Selected item: $selectedItem")
                }
//                if (lastSelectedItem!=selectedItem){
                    var p=activity as MainActivity
                    p.doNotLogout=true

                    when(selectedItem)
                    {
                        "English"->changeLanguageSetting(0)
                        "Français"->changeLanguageSetting(1)
                        "Deutsch"->changeLanguageSetting(2)
                        "Español"->changeLanguageSetting(3)
                        "简体中文"->changeLanguageSetting(4)
                    }
                    (activity as MainActivity).updateSettingUI()

//                }
//
                lastSelectedItem = selectedItem

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
        val switch = binding.themeSwitch
        restart=false
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                prefs.edit().putBoolean("switchState", binding.themeSwitch.isChecked).apply()//save the switch status
                prefs.edit().putInt("mode", AppCompatDelegate.MODE_NIGHT_YES).apply()
                showRestartDialog()
            } else {
                prefs.edit().putBoolean("switchState", binding.themeSwitch.isChecked).apply()//save the switch status
                prefs.edit().putInt("mode", AppCompatDelegate.MODE_NIGHT_NO).apply()
                showRestartDialog()
            }
        }
//        val logoutButton=binding.logoutButton
    //    logoutButton.setOnClickListener(
      //      findNavController()
       // )
        //starButton.setOnClickListener(Navigation.createNavigateOnClickListener(
          //  popR.id.navigation_star,null
        //))   //wait for adapter&list
        binding.logoutButton.setOnClickListener {
            Firebase.auth.signOut()
            Toast.makeText(context,"You have been logged out",Toast.LENGTH_SHORT).show()
            activity?.finish()
        }
        val settingViewModel =
            ViewModelProvider(this).get(SettingViewModel::class.java)


//        val textView: TextView = binding.textSetting
//        settingViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return binding.root
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
        setApplicationLocales(appLocale)
        updateBottomNavigationMenu()
    }

    private fun showRestartDialog() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val mode = prefs.getInt("mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(com.bignerdranch.android.popuptrip.R.layout.dialog_restart, null)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setTitle(popR.string.title_restart)
            .setPositiveButton(popR.string.restart) { _, _ ->
                AppCompatDelegate.setDefaultNightMode(mode) //read the previous setting for dark mode
            }.setNegativeButton(popR.string.cancel,null)
            .create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }
    override fun onDestroyView() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        prefs.edit().putInt("SpinnerPosition", binding.spinner.selectedItemPosition).apply()//save spinner position
        super.onDestroyView()
        _binding = null
    }

    private fun updateBottomNavigationMenu() {
        val menu = activity?.findViewById<BottomNavigationView>(com.bignerdranch.android.popuptrip.R.id.nav_view)?.menu
        menu?.apply {
            findItem(com.bignerdranch.android.popuptrip.R.id.navigation_home).setTitle(com.bignerdranch.android.popuptrip.R.string.title_home)
            findItem(com.bignerdranch.android.popuptrip.R.id.navigation_dashboard).setTitle(com.bignerdranch.android.popuptrip.R.string.title_dashboard)
            findItem(com.bignerdranch.android.popuptrip.R.id.navigation_profile).setTitle(com.bignerdranch.android.popuptrip.R.string.title_profile)
            findItem(com.bignerdranch.android.popuptrip.R.id.navigation_settings).setTitle(com.bignerdranch.android.popuptrip.R.string.title_setting)
        }
    }
}