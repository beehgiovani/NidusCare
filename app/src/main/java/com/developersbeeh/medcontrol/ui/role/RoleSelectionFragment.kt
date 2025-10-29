package com.developersbeeh.medcontrol.ui.role

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.FragmentRoleSelectionBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoleSelectionFragment : Fragment() {

    private var _binding: FragmentRoleSelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoleSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonCaregiver.setOnClickListener {
            findNavController().navigate(R.id.action_roleSelectionFragment_to_loginFragment)
        }

        binding.buttonDependent.setOnClickListener {
            findNavController().navigate(R.id.action_roleSelectionFragment_to_linkDependentFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
