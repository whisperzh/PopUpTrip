package com.bignerdranch.android.popuptrip.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bignerdranch.android.popuptrip.databinding.FragmentHomeSearchBoxBinding
import androidx.fragment.app.viewModels

private const val TAG = "HomeSearchBoxFragment"
class HomeSearchBoxFragment: Fragment() {
    private var _binding: FragmentHomeSearchBoxBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val homeSearchBoxViewModel: HomeSearchBoxViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeSearchBoxBinding.inflate(inflater, container, false)
        val destEntered = homeSearchBoxViewModel.dest
        binding.searchBar.setText(destEntered)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}