package com.perpetio.squat.challenge.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.perpetio.squat.challenge.R
import com.perpetio.squat.challenge.view.adapter.spinnerview.CustomAdapterRes
import com.perpetio.squat.challenge.view.adapter.spinnerview.ExercisesAdapter
import com.perpetio.squat.challenge.databinding.FragmentExercisesBinding
import com.perpetio.squat.challenge.model.SportViewModel
import com.perpetio.squat.challenge.util.ChallengeEnum.Companion.getAllExercises

class ExercisesFragment : Fragment() {

    private val sharedViewModel: SportViewModel by activityViewModels()
    var binding: FragmentExercisesBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentExercisesBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.backBtn?.setOnClickListener { findNavController().popBackStack() }
        initSpinner()
        binding?.nextBtn?.setOnClickListener {
            findNavController().navigate(R.id.action_exercisesFragment_to_challengeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun initSpinner() {
        val spinnerAdapter = ExercisesAdapter(
            requireContext(),
            CustomAdapterRes(
                R.layout.item_exercises,
                R.layout.item_header_exercises_spinner,
                R.layout.item_exercises_drop_down
            )
        )
        val exerciseSpinner = binding?.exerciseSpinner
        exerciseSpinner?.adapter = spinnerAdapter
        exerciseSpinner?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val item = parent?.adapter?.getItem(position)
                    if (item != null) {
                        val s = item as String
                        sharedViewModel.setType(s)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    //Ignore
                }
            }
        exerciseSpinner?.setSelection(getAllExercises().indexOf(sharedViewModel.type.value) + 1)
    }
}