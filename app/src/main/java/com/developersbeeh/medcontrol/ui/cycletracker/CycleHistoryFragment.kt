package com.developersbeeh.medcontrol.ui.cycletracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.FragmentCycleHistoryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CycleHistoryFragment : Fragment() {

    private var _binding: FragmentCycleHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CycleTrackerViewModel by activityViewModels()
    private lateinit var historyAdapter: CycleHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCycleHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupToolbar() {
        // ✅ REATORADO: Define o título da toolbar
        binding.toolbar.title = getString(R.string.cycle_history_title)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        historyAdapter = CycleHistoryAdapter()
        binding.recyclerViewHistory.adapter = historyAdapter
    }

    private fun observeViewModel() {
        viewModel.cycleHistory.observe(viewLifecycleOwner) { historyList ->
            val isEmpty = historyList.isNullOrEmpty()
            // ✅ REATORADO: Define o texto do estado vazio
            binding.textViewEmptyState.text = getString(R.string.cycle_history_empty)
            binding.textViewEmptyState.isVisible = isEmpty
            binding.recyclerViewHistory.isVisible = !isEmpty

            historyAdapter.submitList(historyList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}