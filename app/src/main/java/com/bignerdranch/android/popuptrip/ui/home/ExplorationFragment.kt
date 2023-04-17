package com.bignerdranch.android.popuptrip.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.databinding.FragmentExplorationBinding
import com.google.android.libraries.places.widget.AutocompleteSupportFragment

private const val TAG = "ExplorationFragment"
class ExplorationFragment: Fragment() {

    private var _binding: FragmentExplorationBinding? = null

    private val binding get() = _binding!!
    private val args: ExplorationFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]
        val dest = args.destination
        Log.d(TAG, dest)
//        val explorationMapFragment = childFragmentManager.findFragmentById(R.id.explorationMapFragmentContainerView)
//        explorationMapFragment.
        _binding = FragmentExplorationBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}