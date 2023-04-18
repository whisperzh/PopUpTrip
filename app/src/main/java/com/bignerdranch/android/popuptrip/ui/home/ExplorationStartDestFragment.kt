package com.bignerdranch.android.popuptrip.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bignerdranch.android.popuptrip.databinding.FragmentExplorationStartDestBinding

private const val TAG = "ExplorationStartDest"

class ExplorationStartDestFragment: Fragment() {
    private var _binding: FragmentExplorationStartDestBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val explorationStartDestViewModel: ExplorationStartDestViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExplorationStartDestBinding.inflate(inflater, container, false)
        val startPoint = explorationStartDestViewModel.startingPoint
        binding.startingTextField.editText?.setText(startPoint)

        val destination = explorationStartDestViewModel.destination
        binding.destTextField.editText?.setText(destination)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}