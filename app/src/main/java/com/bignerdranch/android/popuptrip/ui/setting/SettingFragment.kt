package com.bignerdranch.android.popuptrip.ui.setting

import android.R
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bignerdranch.android.popuptrip.R as popR
import com.bignerdranch.android.popuptrip.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {
    private var lastSelectedItem: String? = null
    private var _binding: FragmentSettingBinding? = null
    private val dataList =  listOf("English","French","German","Spanish","Simplified Chinese")
    private lateinit var overlayout: FrameLayout

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
                val selectedItem = parent.getItemAtPosition(position) as String
                if (lastSelectedItem != null) {
                    val toast = Toast.makeText(
                        requireContext(),
                        "You choose $selectedItem",
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                    Log.d("MyFragment", "Selected item: $selectedItem")
                }
                lastSelectedItem = selectedItem

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
        val switch = binding.themeSwitch

        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                prefs.edit().putInt("mode", AppCompatDelegate.MODE_NIGHT_YES).apply()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                prefs.edit().putInt("mode", AppCompatDelegate.MODE_NIGHT_NO).apply()
            }
        }
        val logoutButton=binding.logoutButton
    //    logoutButton.setOnClickListener(
      //      findNavController()
       // )
        //starButton.setOnClickListener(Navigation.createNavigateOnClickListener(
          //  popR.id.navigation_star,null
        //))   //wait for adapter&list
        val settingViewModel =
            ViewModelProvider(this).get(SettingViewModel::class.java)


//        val textView: TextView = binding.textSetting
//        settingViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return binding.root
    }

    override fun onDestroyView() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        prefs.edit().putBoolean("switchState", binding.themeSwitch.isChecked).apply()//save the switch status
        prefs.edit().putInt("mode", AppCompatDelegate.getDefaultNightMode()).apply()//save the mode
        prefs.edit().putInt("SpinnerPosition", binding.spinner.selectedItemPosition).apply()//save spinner position
        super.onDestroyView()
        _binding = null
    }
}