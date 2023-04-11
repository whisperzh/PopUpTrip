package com.bignerdranch.android.popuptrip.ui.home

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.bignerdranch.android.popuptrip.databinding.FragmentHomeSearchBoxBinding
import androidx.fragment.app.viewModels
import com.bignerdranch.android.popuptrip.R

private const val TAG = "HomeSearchBoxFragment"
class HomeSearchBoxFragment: Fragment() {
    private var _binding: FragmentHomeSearchBoxBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val homeSearchBoxViewModel: HomeSearchBoxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCraete has been called")
//        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeSearchBoxBinding.inflate(inflater, container, false)
        val destEntered = homeSearchBoxViewModel.dest
        binding.homeSearchBar.setText(destEntered)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//        inflater.inflate(R.menu.home_search_box_menu, menu)
//    }
}