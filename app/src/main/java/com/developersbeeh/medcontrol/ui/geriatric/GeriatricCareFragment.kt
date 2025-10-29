// src/main/java/com/developersbeeh/medcontrol/ui/geriatric/GeriatricCareFragment.kt
package com.developersbeeh.medcontrol.ui.geriatric

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.developersbeeh.medcontrol.databinding.FragmentGeriatricCareBinding
import com.developersbeeh.medcontrol.ui.education.EducationAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GeriatricCareFragment : Fragment() {

    private var _binding: FragmentGeriatricCareBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GeriatricCareViewModel by viewModels()
    private val args: GeriatricCareFragmentArgs by navArgs()
    private lateinit var adapter: EducationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeriatricCareBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initialize(args.dependentId)

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Cuidado Sênior"

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = EducationAdapter { artigo ->
            val action = GeriatricCareFragmentDirections.actionGeriatricCareFragmentToEducationDetailFragment(artigo)
            findNavController().navigate(action)
        }
        binding.recyclerViewGeriatricArticles.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewGeriatricArticles.adapter = adapter
    }

    private fun setupListeners() {
        binding.buttonAddPressureReminder.setOnClickListener {
            viewModel.addPredefinedReminder(args.dependentId, GeriatricReminderType.PRESSAO)
        }
        binding.buttonAddVitaminDReminder.setOnClickListener {
            viewModel.addPredefinedReminder(args.dependentId, GeriatricReminderType.VITAMINA_D)
        }
        binding.buttonAddActivityReminder.setOnClickListener {
            viewModel.addPredefinedReminder(args.dependentId, GeriatricReminderType.ATIVIDADE_LEVE)
        }
    }

    private fun observeViewModel() {
        viewModel.articles.observe(viewLifecycleOwner) { articles ->
            adapter.submitList(articles)
        }

        viewModel.vitalsState.observe(viewLifecycleOwner) { state ->
            val hasPressure = state.lastBloodPressure != null
            val hasSugar = state.lastBloodSugar != null

            binding.layoutVitalsContent.isVisible = hasPressure || hasSugar
            binding.textViewNoVitals.isVisible = !hasPressure && !hasSugar

            if (hasPressure) {
                val pressure = state.lastBloodPressure!!.values
                binding.textViewBloodPressure.text = "${pressure["systolic"]}/${pressure["diastolic"]} mmHg"
            }
            if (hasSugar) {
                val sugar = state.lastBloodSugar!!.values
                binding.textViewBloodSugar.text = "${sugar["sugarLevel"]} mg/dL"
            }
        }

        viewModel.actionFeedback.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}