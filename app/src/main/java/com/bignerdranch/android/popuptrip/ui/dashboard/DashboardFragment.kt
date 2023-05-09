package com.bignerdranch.android.popuptrip.ui.dashboard

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bignerdranch.android.popuptrip.MainActivity
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.databinding.FragmentDashboardBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import org.json.JSONObject

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val dashboardViewModel: DashboardViewModel by viewModels()

    private var baseUrlPrefix:String="http://54.147.60.104/itinerary/get-a-user-itineraries/"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val dashboardViewModel =
//            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        binding.itineraryRecycleView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dashboardViewModel.itineraries.collect { itineraries ->
                    binding.itineraryRecycleView.adapter =
                        ItineraryListAdaptor(itineraries!!) { itineraryId ->
                            findNavController().navigate(
                                DashboardFragmentDirections.actionNavigationDashboardToItineraryFragment(
                                    itineraryId.toString()
                                )
                            )
                        }
                }
            }
        }

        var userEmail:String = Firebase.auth.currentUser!!.email!!
        var wholeUrl:String=baseUrlPrefix+userEmail
        getVolley(wholeUrl)

    }
    
    private fun getVolley(url:String){
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(context)
        // Request a string response from the provided URL.
        val stringReq = JsonObjectRequest(
            Request.Method.GET, url,
            null,{ response ->
                if(response.get("status code").toString().equals("200"))
                {
                    var k=response.getJSONArray("all itineraries")
                    var listOfItinerarys:MutableList<Itinerary> = mutableListOf()
                    for(i in 0 until k.length())
                    {
                        var singleItJsonObj=k.getJSONObject(i)
                        var singleItinerary=
                            Itinerary(singleItJsonObj.get("id").toString(),
                                "Itinerary"+singleItJsonObj.get("id").toString(),
                                singleItJsonObj.get("itinerary name").toString())
                        listOfItinerarys.add(singleItinerary)
                    }
                    dashboardViewModel.setFlow(listOfItinerarys)
                }
                else
                {
                    binding.DashBoardLog.visibility=View.VISIBLE
                }
                Log.d("Uni-Api","Sam succeeded in providing data")
            },
            Response.ErrorListener {Log.d("API", "that didn't work") })
        queue.add(stringReq)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}