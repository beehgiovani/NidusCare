package com.developersbeeh.medcontrol.ui.analysis // Pacote corrigido

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.developersbeeh.medcontrol.databinding.FragmentAnalysisHistoryBinding

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnalysisHistoryFragment : Fragment() {

    private var _binding: FragmentAnalysisHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AnalysisHistoryViewModel by viewModels()
    private val args: AnalysisHistoryFragmentArgs by navArgs()
    private lateinit var adapter: AnalysisHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.initialize(args.dependentId)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = AnalysisHistoryAdapter { history ->
            // --- CORREÇÃO: Argumentos e ação de navegação corrigidos ---
            val action = AnalysisHistoryFragmentDirections
                .actionAnalysisHistoryFragmentToAnalysisResultFragment(
                    prompt = history.symptomsPrompt,
                    analysisResult = history.analysisResult,
                    dependentId = args.dependentId,
                    dependentName = args.dependentName
                )
            findNavController().navigate(action)
        }
        binding.recyclerViewAnalysisHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewAnalysisHistory.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.analysisHistory.observe(viewLifecycleOwner) { historyList ->
            binding.emptyStateLayout.visibility = if (historyList.isEmpty()) View.VISIBLE else View.GONE
            adapter.submitList(historyList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}