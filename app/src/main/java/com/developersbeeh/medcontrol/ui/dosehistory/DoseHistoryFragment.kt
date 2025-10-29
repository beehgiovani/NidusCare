package com.developersbeeh.medcontrol.ui.dosehistory

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.FragmentDoseHistoryBinding
import com.developersbeeh.medcontrol.databinding.LayoutEmptyStateBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ✅ CORREÇÃO: As classes 'DoseHistoryListItem' e 'DoseHistoryAdapter' foram removidas daqui.

@AndroidEntryPoint
class DoseHistoryFragment : Fragment() {

    private var _binding: FragmentDoseHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DoseHistoryViewModel by viewModels()
    private val args: DoseHistoryFragmentArgs by navArgs()
    private val adapter by lazy { DoseHistoryAdapter() }
    private var medicationList: List<Pair<String, String>> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoseHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.dose_history_title)
        viewModel.initialize(args.dependentId)

        setupRecyclerView()
        observeViewModel()
        setupMenu()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewDoseHistory.adapter = adapter
        binding.recyclerViewDoseHistory.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.historyPagerFlow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        adapter.addLoadStateListener { loadState ->
            val isListEmpty = loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0
            binding.emptyStateLayout.root.isVisible = isListEmpty
            binding.recyclerViewDoseHistory.isVisible = !isListEmpty

            if (isListEmpty) {
                LayoutEmptyStateBinding.bind(binding.emptyStateLayout.root).apply {
                    lottieAnimationView.setAnimation(R.raw.empty_list)
                    tvEmptyTitle.text = getString(R.string.empty_state_no_dose_history_title)
                    tvEmptyMessage.text = getString(R.string.empty_state_no_dose_history_subtitle)
                    btnEmptyAction.isVisible = false
                }
            }

            binding.progressBar.isVisible = loadState.refresh is LoadState.Loading
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allMedications.collectLatest { meds ->
                medicationList = meds.map { it.id to it.nome }
                requireActivity().invalidateOptionsMenu()
            }
        }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.dose_history_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_filter_history -> {
                        showFilterDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showFilterDialog() {
        if (medicationList.isEmpty()){
            return
        }
        val medNames = arrayOf(getString(R.string.all_medications)) + medicationList.map { it.second }.toTypedArray()
        var selectedIndex = 0

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.filter_by_medication))
            .setSingleChoiceItems(medNames, selectedIndex) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton(getString(R.string.filter_button)) { dialog, _ ->
                val medicationId = if (selectedIndex == 0) null else medicationList[selectedIndex - 1].first
                viewModel.applyFilter(medicationId)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}