package com.bignerdranch.android.popuptrip.ui.itinerary

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bignerdranch.android.popuptrip.MainActivity
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.databinding.FragmentItineraryBinding
import com.bignerdranch.android.popuptrip.ui.destinationshown.DestinationItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import org.json.JSONArray


private const val TAG = "ItineraryDetailedFragment"
class ItineraryDetailedFragment : Fragment() {
//    private val TAG = "class ItineraryFragment : Fragment()"
    private lateinit var itineraryAdapter: ItineraryAdapter
    private val args: ItineraryDetailedFragmentArgs by navArgs()
    private var dbReference: DatabaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://popup-trip-default-rtdb.firebaseio.com/")

    private lateinit var binding: FragmentItineraryBinding

    // Declare a URL for getting itinerary data
    private var get_one_itinerary_url: String = "http://54.147.60.104/itinerary/get-one-itinerary/"
//"http://54.147.60.104/itinerary/get-one-itinerary/[itinerary_id]/[travel_preference]"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding=FragmentItineraryBinding.inflate(layoutInflater,container,false)

        // Get a reference to the RecyclerView
        val recyclerView = binding.itineraryRecyclerView
        binding.itDetailBackButton.setOnClickListener {
//            viewLifecycleOwner.lifecycleScope.launch {
//                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                    findNavController().navigate(
//                        ItineraryDetailedFragmentDirections.actionItineraryFragmentToNavigationDashboard()
//                    )
//                }
//            }
            requireActivity().onBackPressed()
        }

        // Initialize a list of ItineraryItems with some example data
        val destinations = mutableListOf<DestinationItem>()

        // Initialize the ItineraryAdapter with the list of ItineraryItems
        itineraryAdapter = ItineraryAdapter(destinations)
        // Set the RecyclerView's adapter to the ItineraryAdapter
        recyclerView.adapter = itineraryAdapter
        // Set the RecyclerView's layout manager to a LinearLayoutManager
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Check if there are arguments passed to this fragment
        if (args != null) {
            // Log the itineraryId argument
            Log.d(TAG, args.itineraryId)

            var travelM=(activity as MainActivity).user.travelMethod.toLowerCase()


            var userEmail:String = Firebase.auth.currentUser!!.email!!
            var wholeUrl:String=get_one_itinerary_url+args.itineraryId.toString()+"/"+travelM
            getVolley(wholeUrl)

        } else {
            // Log that there are no arguments
            Log.d(TAG, "NO args")
        }

        return  binding.root
    }

    // Define a function to add a new destination to the list and update the adapter
    private fun addDestination(destinationName: String, timeToNext: String, steps: String) {
        // Create a new ItineraryItem with the given destinationName and timeToNext
        val newDestination = DestinationItem(destinationName, timeToNext, steps)
        // Add the new ItineraryItem to the adapter's list
        itineraryAdapter.addDestination(newDestination)
    }

    private fun getVolley(url:String){
        showProgressIndicator()
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(context)
        // Request a string response from the provided URL.
        val stringReq = JsonObjectRequest(
            Request.Method.GET, url,
            null,{ response ->
                if(response.get("status code").toString().equals("200"))
                {
                    var k=response.getJSONArray("route")
                    var direction=response.getJSONArray("directions")



                    for(i in 0 until k.length())
                    {
                        var content=""
                        var singlePosition=k.get(i) as JSONArray
                        if(i<direction.length())
                        {
                            var steps=direction.get(i) as JSONArray

                            for(j in 0 until steps.length())
                            {
                                content+=steps.get(j).toString()+"\n"
                            }
                        }



                        addDestination(singlePosition.get(0).toString(),singlePosition.get(1).toString(),content )
                    }
//                    dashboardViewModel.setFlow(listOfItinerarys)
                }
                else
                {
                    Toast.makeText(activity, getString(R.string.itinerary_error), Toast.LENGTH_LONG).show()
                }
                Log.d("Uni-Api","Sam succeeded in providing data")
                hideProgressIndicator()
            },
            Response.ErrorListener {
                Log.d("API", "that didn't work")
                hideProgressIndicator()
            })
        queue.add(stringReq)
    }

    private fun showProgressIndicator() {
        Log.d(TAG, "progress indicator shown")
        binding.itineraryDetailLoadingProgressIndicator.visibility = View.VISIBLE
    }

    private fun hideProgressIndicator() {
        Log.d(TAG, "progress indicator hid")
        binding.itineraryDetailLoadingProgressIndicator.visibility = View.GONE
    }

    private fun createStepsDialog(listOfSteps:MutableList<String>){
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.instructionsForTraveling)


        builder.setItems(listOfSteps.toTypedArray(),
            DialogInterface.OnClickListener { dialog, which ->
//                when (which) {
//                    0, 1, 2, 3, 4 -> {}
//                }
            })

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}

