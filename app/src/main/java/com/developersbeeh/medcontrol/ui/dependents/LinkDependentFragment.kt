package com.developersbeeh.medcontrol.ui.dependents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.developersbeeh.medcontrol.databinding.FragmentLinkDependentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LinkDependentFragment : Fragment() {

    private var _binding: FragmentLinkDependentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LinkDependentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLinkDependentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.buttonLink.setOnClickListener {
            val code = binding.editTextCode.text.toString()
            val password = binding.editTextPassword.text.toString()
            viewModel.onLoginClicked(code, password)
        }

        binding.editTextCode.addTextChangedListener { binding.tilCode.error = null }
        binding.editTextPassword.addTextChangedListener { binding.tilPassword.error = null }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonLink.isEnabled = !isLoading
        }

        viewModel.errorEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                binding.tilCode.error = " "
                binding.tilPassword.error = " "
            }
        }

        viewModel.loginResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { dependente ->
                Toast.makeText(requireContext(), "Bem-vindo(a), ${dependente.nome}!", Toast.LENGTH_SHORT).show()
                // CORREÇÃO: Navega para o novo Dashboard do Dependente ao invés da lista de medicamentos.
                val action = LinkDependentFragmentDirections.actionLinkDependentFragmentToDashboardDependenteFragment(
                    dependentId = dependente.id,
                    dependentName = dependente.nome
                )
                findNavController().navigate(action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}