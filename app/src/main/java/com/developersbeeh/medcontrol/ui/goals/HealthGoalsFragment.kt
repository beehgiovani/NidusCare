// src/main/java/com/developersbeeh/medcontrol/ui/goals/HealthGoalsFragment.kt
package com.developersbeeh.medcontrol.ui.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.developersbeeh.medcontrol.databinding.FragmentHealthGoalsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HealthGoalsFragment : Fragment() {

    private var _binding: FragmentHealthGoalsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HealthGoalsViewModel by viewModels()
    private val args: HealthGoalsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Metas de Saúde"
        viewModel.loadDependent(args.dependentId)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.buttonSave.setOnClickListener {
            val weightGoal = binding.editTextWeightGoal.text.toString()
            val hydrationGoal = binding.editTextHydrationGoal.text.toString().toIntOrNull() ?: 2000
            val calorieGoal = binding.editTextCalorieGoal.text.toString().toIntOrNull() ?: 2000
            val activityGoal = binding.editTextActivityGoal.text.toString().toIntOrNull() ?: 30

            viewModel.saveGoals(weightGoal, hydrationGoal, calorieGoal, activityGoal)
        }
    }

    private fun observeViewModel() {
        viewModel.dependent.observe(viewLifecycleOwner) { dependent ->
            dependent?.let {
                binding.editTextWeightGoal.setText(it.pesoMeta)
                binding.editTextHydrationGoal.setText(it.metaHidratacaoMl.toString())
                binding.editTextCalorieGoal.setText(it.metaCaloriasDiarias.toString())
                binding.editTextActivityGoal.setText(it.metaAtividadeMinutos.toString())
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.buttonSave.isEnabled = !isLoading
        }

        viewModel.saveStatus.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                result.onSuccess {
                    Toast.makeText(context, "Metas salvas com sucesso!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }.onFailure {
                    Toast.makeText(context, "Erro ao salvar metas: ${it.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}