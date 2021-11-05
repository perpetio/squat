package com.perpetio.squat.challenge.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.perpetio.squat.challenge.R
import com.perpetio.squat.challenge.databinding.FragmentResultBinding
import com.perpetio.squat.challenge.databinding.ViewBtnHomeBinding
import com.perpetio.squat.challenge.model.SquatViewModel
import com.perpetio.squat.challenge.util.ChallengeEnum

class ResultFragment : Fragment() {

    private val sharedViewModel: SquatViewModel by activityViewModels()
    var binding: FragmentResultBinding? = null
    var homeBinding: ViewBtnHomeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentResultBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        homeBinding = fragmentBinding.backContainer
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeBinding?.homeBtn?.setOnClickListener {
            findNavController().navigate(
                R.id.action_resultFragment_to_welcomeFragment
            )
        }
        binding?.leaderboardBtn?.setOnClickListener { findNavController().navigate(R.id.action_global_leaderBoardFragment) }
        binding?.quantity?.text = sharedViewModel.score.value.toString()
        when (sharedViewModel.type.value) {
            ChallengeEnum.SQUAT.challengeName -> {
                binding?.exerciseName?.text = requireActivity().resources.getQuantityString(
                    R.plurals.squatting,
                    sharedViewModel.score.value!!
                )
            }
            ChallengeEnum.JUMP.challengeName -> {
                binding?.exerciseName?.text = requireActivity().resources.getQuantityString(
                    R.plurals.jumping,
                    sharedViewModel.score.value!!
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}