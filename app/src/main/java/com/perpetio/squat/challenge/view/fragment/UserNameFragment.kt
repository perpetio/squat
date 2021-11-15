package com.perpetio.squat.challenge.view.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.perpetio.squat.challenge.R
import com.perpetio.squat.challenge.databinding.FragmentUserNameBinding
import com.perpetio.squat.challenge.model.ChallengeViewModel

class UserNameFragment : Fragment() {

    var binding: FragmentUserNameBinding? = null
    private val sharedViewModel: ChallengeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentUserNameBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.backBtn?.setOnClickListener { findNavController().popBackStack() }
        binding?.etInputName?.doAfterTextChanged { text ->
            binding!!.nextBtn.isEnabled = !TextUtils.isEmpty(text) && text?.length!! > 2
        }
        binding?.etInputName?.setText(sharedViewModel.name.value)
        binding?.nextBtn?.setOnClickListener {
            sharedViewModel.setName(binding?.etInputName?.text.toString())
            findNavController().navigate(R.id.action_userNameFragment_to_exercisesFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}