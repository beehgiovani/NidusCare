package com.developersbeeh.medcontrol.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.Sexo
import com.developersbeeh.medcontrol.data.model.TipoSanguineo
import com.developersbeeh.medcontrol.databinding.FragmentCompleteProfileBinding
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class CompleteProfileFragment : Fragment() {
    private var _binding: FragmentCompleteProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CompleteProfileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCompleteProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdowns()
        setupListeners()
        observeViewModel()
    }

    private fun setupDropdowns() {
        val sexoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, Sexo.values().map { it.displayName })
        binding.autoCompleteSexo.setAdapter(sexoAdapter)
        val tipoSanguineoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, TipoSanguineo.values().map { it.displayName })
        binding.autoCompleteTipoSanguineo.setAdapter(tipoSanguineoAdapter)
    }

    private fun setupListeners() {
        binding.editTextDob.setOnClickListener { showDatePicker() }
        binding.tilDob.setEndIconOnClickListener { showDatePicker() }

        binding.buttonSave.setOnClickListener {
            viewModel.saveProfile(
                password = binding.editTextPassword.text.toString(),
                confirmPass = binding.editTextConfirmPassword.text.toString(),
                dataNascimento = binding.editTextDob.text.toString(),
                sexo = binding.autoCompleteSexo.text.toString(),
                tipoSanguineo = binding.autoCompleteTipoSanguineo.text.toString(),
                peso = binding.editTextWeight.text.toString(),
                altura = binding.editTextAltura.text.toString(),
                condicoes = binding.editTextConditions.text.toString(),
                alergias = binding.editTextAllergies.text.toString()
            )
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE }

        viewModel.updateStatus.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                result.onSuccess {
                    Toast.makeText(context, "Perfil salvo com sucesso!", Toast.LENGTH_SHORT).show()
                    // ✅ CORREÇÃO: Usa a nova ação que limpa o histórico
                    findNavController().navigate(R.id.action_completeProfileFragment_to_caregiverDashboardFragment)
                }.onFailure {
                    Toast.makeText(context, "Erro: ${it.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker().build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            // ✅ CORREÇÃO AQUI
            val selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneOffset.UTC).toLocalDate()
            binding.editTextDob.setText(selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        }
        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}