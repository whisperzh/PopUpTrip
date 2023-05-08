package com.bignerdranch.android.popuptrip.ui.Profile

import android.R
import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass.Device.Major
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.ktx.auth
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bignerdranch.android.popuptrip.MainActivity
import com.bignerdranch.android.popuptrip.databinding.FragmentPreferenceBinding
import com.bignerdranch.android.popuptrip.ui.home.ExplorationFragmentDirections
import com.bignerdranch.android.popuptrip.ui.setting.SettingViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import com.bignerdranch.android.popuptrip.R as popR

class PreferenceFragment : Fragment() {
    private var lastSelectedItem: String? = null
    private var _binding: FragmentPreferenceBinding? = null
    private val foodlist= listOf(popR.string.Bakery,popR.string.Cafe,popR.string.Restaurant)
    private val nllist= listOf(popR.string.Bar,popR.string.Night_club)
    private val naturelist= listOf(popR.string.Park,popR.string.Campground)
    private val culturelist= listOf(popR.string.Library,popR.string.Museum,popR.string.Art_Gallery,popR.string.BookStore)
    private val enterlist= listOf(popR.string.Aquarium,popR.string.Zoo,popR.string.AmusementPark,popR.string.MovieTheater)
    private val originfoodlist= listOf("Bakery","Cafe","Restaurant")
    private val originnllist= listOf("Bar","Night Club")
    private val originnaturelist= listOf("Park","Campground")
    private val originculturelist= listOf("Library","Museum","Art Gallery","Bookstore")
    private val originenterlist= listOf("Aquarium","Zoo","Amusement Park","Movie Theater")

    val foodarray = ArrayList<String>()
    val nlarray = ArrayList<String>()
    val naturearray = ArrayList<String>()
    val culturearray = ArrayList<String>()
    val enterarray=ArrayList<String>()
    var position = -1
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var dbReference: DatabaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://popup-trip-default-rtdb.firebaseio.com/")
    private lateinit var auth: FirebaseAuth
    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth= Firebase.auth
        _binding = FragmentPreferenceBinding.inflate(inflater,container,false)
        val dataList =  listOf(resources.getString(popR.string.walk_list), resources.getString(popR.string.transit_list), resources.getString(popR.string.driving_list),
            resources.getString(popR.string.bicycling_list))
        val originList=listOf("WALKING","TRANSIT","DRIVING","BICYCLING")
        val foodchips = listOf(binding.BakeryChip, binding.CafeChip, binding.RestaurantChip)
        val nlchips= listOf(binding.BarChip,binding.NightClubChip)
        val naturechips= listOf(binding.ParkChip,binding.CampgroundChip)
        val culturechips= listOf(binding.LibraryChip,binding.MuseumChip,binding.ArtGalleryChip,binding.BookStoreChip)
        val enterchips= listOf(binding.AquariumChip,binding.ZooChip,binding.AmusementParkChip,binding.MovieTheaterChip)
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        if ((activity as MainActivity).user.preference == "") {
            var food_selection = prefs.getString("food_selection", "")
            var nightlife_selection = prefs.getString("nightlife_selection", "")
            var nature_selection = prefs.getString("nature_selection", "")
            var culture_selection = prefs.getString("culture_selection", "")
            val enter_selection = prefs.getString("enter_selection", "")
            foodarray.addAll(TextUtils.split(food_selection, ","))
            nlarray.addAll(TextUtils.split(nightlife_selection, ","))
            naturearray.addAll(TextUtils.split(nature_selection, ","))
            culturearray.addAll(TextUtils.split(culture_selection, ","))
            enterarray.addAll(TextUtils.split(enter_selection, ","))

        }else{
            val Majorpreference=(activity as MainActivity).user.preference
            Log.d("Preference",Majorpreference)
            val preferenceList=TextUtils.split(Majorpreference,"#")
            //suppose it as #enter, s1, s2#nature, s3, s4
            for (preference in preferenceList) {
                val List = TextUtils.split(preference, ",")
                if (List.size > 1) {
                    val pri = List[0]
                    if (pri == "enter") {
                        enterarray.addAll(List.copyOfRange(1, List.size))
                    } else if (pri == "food") {
                        foodarray.addAll(List.copyOfRange(1, List.size))
                    } else if (pri == "nl") {
                        nlarray.addAll(List.copyOfRange(1, List.size))
                    } else {
                        naturearray.addAll(List.copyOfRange(1, List.size))
                    }
                }
            }
        }
        for (item in foodarray) {
            for (i in originfoodlist.indices) {
                if (item == originfoodlist[i]) {
                    foodchips[i].isChecked = true
                }
            }
        }
        for (item in enterarray) {
            for (i in originenterlist.indices) {
                if (item == originenterlist[i]) {
                    enterchips[i].isChecked = true
                }
            }
        }
        for (item in nlarray) {
            for (i in originnllist.indices) {
                if (item == originnllist[i]) {
                    nlchips[i].isChecked = true
                }
            }
        }
        for (item in naturearray) {
            for (i in originnaturelist.indices) {
                if (item == originnaturelist[i]) {
                    naturechips[i].isChecked = true
                }
            }
        }
        for (item in culturearray) {
            for (i in originculturelist.indices) {
                if (item == originculturelist[i]) {
                    culturechips[i].isChecked = true
                }
            }
        }
        for (i in foodchips.indices) {
            val chip = foodchips[i]
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!foodarray.contains(originfoodlist[i])) {
                        foodarray.add(originfoodlist[i])
                    }
                } else {
                    if (foodarray.contains(originfoodlist[i])) {
                        foodarray.remove(originfoodlist[i])
                    }

                }
            }
        }

        for (i in nlchips.indices) {
            val chip = nlchips[i]
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!nlarray.contains(originnllist[i])) {
                        nlarray.add(originnllist[i])
                    }

                } else {
                    if (nlarray.contains(originnllist[i])) {
                        nlarray.remove(originnllist[i])
                    }

                }
            }
        }

        for (i in naturechips.indices) {
            val chip = naturechips[i]
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!naturearray.contains(originnaturelist[i])) {
                        naturearray.add(originnaturelist[i])
                    }
                } else {
                    if (naturearray.contains(originnaturelist[i])) {
                        naturearray.remove(originnaturelist[i])
                    }
                }
            }
        }


        for (i in enterchips.indices) {
            val chip = enterchips[i]
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!enterarray.contains(originenterlist[i])) {
                        enterarray.add(originenterlist[i])
                    }
                }else {
                    if (enterarray.contains(originenterlist[i])) {
                        enterarray.remove(originenterlist[i])
                    }
                }
            }
        }
        for (i in culturechips.indices) {
            val chip = culturechips[i]
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!culturearray.contains(originculturelist[i])) {
                        culturearray.add(originculturelist[i])
                    }
                }else {
                    if (culturearray.contains(originculturelist[i])) {
                        culturearray.remove(originculturelist[i])
                    }
                }
            }
        }
        //
        val adapter =
            ArrayAdapter(requireContext(), R.layout.simple_spinner_item, dataList)//set adapter
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        if (!((activity as MainActivity).user.travelMethod.equals(""))){
            position=originList.indexOf((activity as MainActivity).user.travelMethod)
        }else {
            position = prefs.getInt("MethodSpinnerPosition", 0)
        }
        lastSelectedItem = dataList.get(0)
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
                        "${getString(popR.string.choice)} ${selectedItem}",
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

        val settingViewModel =
            ViewModelProvider(this).get(SettingViewModel::class.java)

        val savebutton=binding.savePreferenceButton
        savebutton.setOnClickListener{
            val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val food = foodarray.sorted().joinToString(separator = ",")
            val nl=nlarray.sorted().joinToString(separator = ",")
            val nature=naturearray.sorted().joinToString(separator = ",")
            val culture=culturearray.sorted().joinToString(separator = ",")
            val enter=enterarray.sorted().joinToString(separator = ",")
            Log.d("Nature",nature)
            var fooddb="#food"
            var nldb="#nl"
            var naturedb="#nature"
            var culturedb="#culture"
            var enterdb="#enter"
            Log.d("Prefernce",enter)
            if (food!=null) {
                prefs.edit().putString("food_selection", food).apply()
                if (food!="") {
                    fooddb = "#food,$food"
                }
            }
            if (nl!=null){
                prefs.edit().putString("nightlife_selection",nl).apply()
                if (nl!="") {
                    nldb = "#nl,$nl"
                }
            }
            if(nature!=null) {
                prefs.edit().putString("nature_selection", nature).apply()
                if (nature!="") {
                    naturedb = "#nature,$nature"
                }
            }
            if(culture!=null){
                prefs.edit().putString("culture_selection",culture).apply()
                if (culture!="") {
                    culturedb = "#culture,$culture"
                }
            }
            if(enter!=null){
                prefs.edit().putString("enter_selection",enter).apply()
                if (enter!="") {
                    enterdb = "#enter,$enter"
                }
            }
            val store=fooddb+naturedb+culturedb+nldb+enterdb
            Log.d("Preference","travel method "+originList[binding.methodSpinner.selectedItemPosition])
            (activity as MainActivity).user.travelMethod=originList[binding.methodSpinner.selectedItemPosition]
            prefs.edit().putString("TRAVEL_METHOD",originList[binding.methodSpinner.selectedItemPosition].toString()).commit()
            dbReference.child("User_Table").child(auth.currentUser!!.uid).child("travelMethod").setValue(
                originList[binding.methodSpinner.selectedItemPosition].toString()).addOnSuccessListener {
                Log.d("Preference", "Travel method stored in database successfully")
            }.addOnFailureListener {
                Log.e("Preference", "Travel method failed to store in firebase")
            }
            dbReference.child("User_Table").child(auth.currentUser!!.uid).child("preferencePlace").setValue(
                store.toString()
            ).addOnSuccessListener {
                Log.d("Preference","Preference place store successfully")
            }.addOnFailureListener {
                Log.e("Preference", "Preference place failed to store in firebase")
            }
            (activity as MainActivity).user.preference=store
            val toast = Toast.makeText(
                requireContext(),
                "${getString(popR.string.saved)}",
                Toast.LENGTH_SHORT
            )
            toast.show()
//            Navigation.createNavigateOnClickListener(com.bignerdranch.android.popuptrip.R.id.navigation_profile,null)
            val profile = PreferenceFragmentDirections.preferenceToProfileAction()
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    findNavController().navigate(profile)
                }
            }

        }
//        val textView: TextView = binding.textSetting
//        settingViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}