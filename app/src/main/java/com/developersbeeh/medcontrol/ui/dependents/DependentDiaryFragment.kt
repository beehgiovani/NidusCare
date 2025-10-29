package com.developersbeeh.medcontrol.ui.dependents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.developersbeeh.medcontrol.databinding.FragmentDependentDiaryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DependentDiaryFragment : Fragment() {

    private var _binding: FragmentDependentDiaryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DependentDiaryViewModel by viewModels()
    private val args: DependentDiaryFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDependentDiaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initialize(args.dependentId)

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Diário de ${args.dependentName}"

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.buttonSave.setOnClickListener {
            val selectedMoodId = binding.chipGroupMood.checkedChipId
            val mood = when (selectedMoodId) {
                binding.chipMoodHappy.id -> "Bem"
                binding.chipMoodNeutral.id -> "Normal"
                binding.chipMoodSad.id -> "Mal"
                else -> null
            }
            val symptoms = binding.editTextSymptoms.text.toString().trim().takeIf { it.isNotEmpty() }
            val notes = binding.editTextNotes.text.toString().trim().takeIf { it.isNotEmpty() }

            if (mood == null && symptoms == null) {
                Toast.makeText(context, "Selecione um humor ou descreva um sintoma para salvar.", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.saveDiaryEntry(mood, symptoms, notes)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.saveStatus.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { success ->
                if (success) {
                    Toast.makeText(context, "Registro salvo com sucesso!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(context, "Nenhum dado para salvar.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}