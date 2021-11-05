package com.perpetio.squat.challenge.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.perpetio.squat.challenge.R
import com.perpetio.squat.challenge.adapter.spinnerview.ExercisesAdapter
import com.perpetio.squat.challenge.adapter.recyclerview.LeaderBoardAdapter
import com.perpetio.squat.challenge.adapter.spinnerview.CustomAdapterRes
import com.perpetio.squat.challenge.databinding.FragmentLeaderboardBinding
import com.perpetio.squat.challenge.databinding.ViewBtnHomeBinding
import com.perpetio.squat.challenge.model.SquatViewModel
import com.perpetio.squat.challenge.util.ChallengeEnum

class LeaderBoardFragment : Fragment() {

    var binding: FragmentLeaderboardBinding? = null
    var bindingHomeBtn: ViewBtnHomeBinding? = null

    private val sharedViewModel: SquatViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        bindingHomeBtn = binding?.backContainer
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindingHomeBtn?.homeBtn?.setOnClickListener {
            findNavController().navigate(R.id.action_leaderBoardFragment_to_welcomeFragment)
        }
        val leaderBoardAdapter = LeaderBoardAdapter()
        binding?.apply {
            recyclerView.apply {
                adapter = leaderBoardAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                val dividerItemDecoration = DividerItemDecoration(
                    requireContext(),
                    LinearLayoutManager.VERTICAL
                )
                dividerItemDecoration.setDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.layer_decorator
                    )!!
                )
                addItemDecoration(dividerItemDecoration)
            }
        }
        sharedViewModel.getLeaderBoardList.observe(viewLifecycleOwner) { item ->
            leaderBoardAdapter.submitList(item)
        }
        val spinnerAdapter = ExercisesAdapter(
            requireContext(),
            CustomAdapterRes(
                R.layout.item_exercises_small,
                R.layout.item_header_exercises_spinner_small,
                R.layout.item_exercises_drop_down_small
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
        exerciseSpinner?.setSelection(ChallengeEnum.getAllExercises().indexOf(sharedViewModel.type.value) +1 )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}