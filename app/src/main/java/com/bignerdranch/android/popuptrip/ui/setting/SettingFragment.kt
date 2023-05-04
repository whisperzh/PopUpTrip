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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bignerdranch.android.popuptrip.R as popR
import com.bignerdranch.android.popuptrip.databinding.FragmentSettingBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*

class SettingFragment : Fragment() {
    private var lastSelectedItem: String? = null
    private var _binding: FragmentSettingBinding? = null
    private val dataList =  listOf("English","Français","Deutsch","Español","简体中文")
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
        val starButton = binding.starButton
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val position = prefs.getInt("SpinnerPosition", 0)
        val switchState = prefs.getBoolean("switchState", false)
        lastSelectedItem=prefs.getString("Language",null)
        binding.themeSwitch.isChecked = switchState
        val adapter =
            ArrayAdapter(requireContext(), R.layout.simple_spinner_item, dataList)//set adapter
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter//bind the adapter
        binding.spinner.setSelection(position)
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val activity = requireActivity() as? AppCompatActivity
                val selectedItem = parent.getItemAtPosition(position) as String
                prefs.edit().putString("Language",selectedItem).apply()
                if (lastSelectedItem != null) {
                    val toast = Toast.makeText(
                        requireContext(),
                        "${getString(popR.string.choice)} $selectedItem",
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                    Log.d("MyFragment", "Selected item: $selectedItem")
                }
                if (lastSelectedItem!=selectedItem){
                    activity?.finish()
                }
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
}