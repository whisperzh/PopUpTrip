package com.bignerdranch.android.popuptrip.ui.Profile

import android.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.bignerdranch.android.popuptrip.databinding.FragmentPreferenceBinding
import com.bignerdranch.android.popuptrip.ui.setting.SettingViewModel
import com.bignerdranch.android.popuptrip.R as popR

class PreferenceFragment : Fragment() {
    private var lastSelectedItem: String? = null
    private var _binding: FragmentPreferenceBinding? = null
    private val foodlist= listOf(popR.string.Bakery,popR.string.Cafe,popR.string.Restaurant)
    private val nllist= listOf(popR.string.Bar,popR.string.Night_club)
    private val naturelist= listOf(popR.string.Aquarium,popR.string.Zoo,popR.string.Park,popR.string.Campground)
    private val culturelist= listOf(popR.string.Library,popR.string.Museum,popR.string.Art_Gallery,popR.string.BookStore)
    private val dataList =  listOf("WALKING", "TRANSIT", "DRIVING", "BICYCLING")
    val foodarray = ArrayList<String>()
    val nlarray = ArrayList<String>()
    val naturearray = ArrayList<String>()
    val culturearray = ArrayList<String>()
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPreferenceBinding.inflate(inflater,container,false)
        val foodchips = listOf(binding.BakeryChip, binding.CafeChip, binding.RestaurantChip)
        val nlchips= listOf(binding.BarChip,binding.NightClubChip)
        val naturechips= listOf(binding.AquariumChip,binding.ZooChip,binding.ParkChip,binding.CampgroundChip)
        val culturechips= listOf(binding.LibraryChip,binding.MuseumChip,binding.ArtGalleryChip,binding.BookStoreChip)
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        var food_selection=prefs.getString("food_selection","")
        var nightlife_selection=prefs.getString("nightlife_selection","")
        var nature_selection=prefs.getString("nature_selection","")
        var culture_selection=prefs.getString("culture_selection","")
        foodarray.addAll(TextUtils.split(food_selection, ","))
        nlarray.addAll(TextUtils.split(nightlife_selection, ","))
        naturearray.addAll(TextUtils.split(nature_selection, ","))
        culturearray.addAll(TextUtils.split(culture_selection, ","))

        for (item in foodarray) {
            for (i in foodlist.indices) {
                if (item == getString(foodlist[i])) {
                    foodchips[i].isChecked = true
                }
            }
        }
        for (item in nlarray) {
            for (i in nllist.indices) {
                if (item == getString(nllist[i])) {
                    nlchips[i].isChecked = true
                }
            }
        }
        for (item in naturearray) {
            for (i in naturelist.indices) {
                if (item == getString(naturelist[i])) {
                    naturechips[i].isChecked = true
                }
            }
        }
        for (item in culturearray) {
            for (i in culturelist.indices) {
                if (item == getString(culturelist[i])) {
                    culturechips[i].isChecked = true
                }
            }
        }
        for (i in foodchips.indices) {
            val chip = foodchips[i]
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!foodarray.contains(getString(foodlist.get(i)))) {
                        foodarray.add(getString(foodlist.get(i)))
                    }
                } else {
                    if (foodarray.contains(getString(foodlist.get(i)))) {
                        foodarray.remove(getString(foodlist.get(i)))
                    }

                }
            }
        }

        for (i in nlchips.indices) {
            val chip = nlchips[i]
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!nlarray.contains(getString(nllist.get(i)))) {
                        nlarray.add(getString(nllist.get(i)))
                    }

                } else {
                    if (nlarray.contains(getString(nllist.get(i)))) {
                        nlarray.remove(getString(nllist.get(i)))
                    }

                }
            }
        }

        for (i in naturechips.indices) {
            val chip = naturechips[i]
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!naturearray.contains(getString(naturelist.get(i)))) {
                        naturearray.add(getString(naturelist.get(i)))
                    }
                } else {
                    if (naturearray.contains(getString(naturelist.get(i)))) {
                        naturearray.remove(getString(naturelist.get(i)))
                    }
                }
            }
        }


        for (i in culturechips.indices) {
            val chip = culturechips[i]
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!culturearray.contains(getString(culturelist.get(i)))) {
                        culturearray.add(getString(culturelist.get(i)))
                    }
                }else {
                    if (culturearray.contains(getString(culturelist.get(i)))) {
                        culturearray.remove(getString(culturelist.get(i)))
                    }
                }
            }
        }
        val adapter =
            ArrayAdapter(requireContext(), R.layout.simple_spinner_item, dataList)//set adapter
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val position = prefs.getInt("MethodSpinnerPosition", 0)
        binding.methodSpinner.adapter = adapter//bind the adapter
        binding.methodSpinner.setSelection(position)
        binding.methodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                }
                lastSelectedItem = selectedItem
                prefs.edit().putInt("MethodSpinnerPosition", binding.methodSpinner.selectedItemPosition).apply()//save spinner position
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        val backButton=binding.PrefBackButton
        backButton.setOnClickListener(Navigation.createNavigateOnClickListener(com.bignerdranch.android.popuptrip.R.id.navigation_profile,null))

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
        val food = foodarray.joinToString(separator = ",")
        val nl=nlarray.joinToString(separator = ",")
        val nature=naturearray.joinToString(separator = ",")
        val culture=culturearray.joinToString(separator = ",")
        if (food!=null) {
            prefs.edit().putString("food_selection", food).apply()
        }
        if (nl!=null){
        prefs.edit().putString("nightlife_selection",nl).apply()}
        if(nature!=null) {
            prefs.edit().putString("nature_selection", nature).apply()
        }
        if(culture!=null){
        prefs.edit().putString("culture_selection",culture).apply()
        }
        super.onDestroyView()
        _binding = null
    }
}