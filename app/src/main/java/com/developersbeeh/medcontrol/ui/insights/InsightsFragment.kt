package com.developersbeeh.medcontrol.ui.insights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.developersbeeh.medcontrol.databinding.FragmentInsightsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InsightsFragment : Fragment() {

    private var _binding: FragmentInsightsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InsightsViewModel by viewModels()
    private val args: InsightsFragmentArgs by navArgs()
    private lateinit var adapter: InsightsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInsightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa o ViewModel com o ID do dependente
        viewModel.initialize(args.dependentId)

        // Marca todos os insights como lidos assim que o usuário entra na tela
        viewModel.markAllAsRead()

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Insights para ${args.dependentName}"

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = InsightsAdapter()
        binding.recyclerViewInsights.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewInsights.adapter = adapter
    }

    private fun observeViewModel() {
        // Mostra a animação de carregamento inicialmente
        binding.shimmerLayout.startShimmer()
        binding.shimmerLayout.visibility = View.VISIBLE

        viewModel.insights.observe(viewLifecycleOwner) { insights ->
            binding.shimmerLayout.stopShimmer()
            binding.shimmerLayout.visibility = View.GONE

            adapter.submitList(insights)

            // Gerencia a visibilidade da lista vs. o estado vazio
            binding.emptyStateLayout.visibility = if (insights.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerViewInsights.visibility = if (insights.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}