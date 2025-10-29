// src/main/java/com/developersbeeh/medcontrol/ui/achievements/AchievementsFragment.kt
package com.developersbeeh.medcontrol.ui.achievements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.developersbeeh.medcontrol.databinding.FragmentAchievementsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AchievementsFragment : Fragment() {

    private var _binding: FragmentAchievementsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AchievementsViewModel by viewModels()
    private val args: AchievementsFragmentArgs by navArgs()
    private lateinit var adapter: AchievementsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAchievementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Conquistas de ${args.dependentName}"

        setupRecyclerView()
        observeViewModel()

        // Inicializa o ViewModel por último, após a UI estar pronta
        viewModel.initialize(args.dependentId)
    }

    private fun setupRecyclerView() {
        adapter = AchievementsAdapter()
        binding.recyclerViewAchievements.adapter = adapter
        binding.recyclerViewAchievements.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeViewModel() {
        viewModel.achievements.observe(viewLifecycleOwner) { achievements ->
            binding.shimmerLayout.stopShimmer()
            binding.shimmerLayout.visibility = View.GONE
            binding.recyclerViewAchievements.visibility = View.VISIBLE
            adapter.submitList(achievements)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}