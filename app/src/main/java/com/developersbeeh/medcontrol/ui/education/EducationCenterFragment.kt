package com.developersbeeh.medcontrol.ui.education

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.FragmentEducationCenterBinding
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EducationCenterFragment : Fragment() {

    private var _binding: FragmentEducationCenterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EducationViewModel by viewModels()
    private lateinit var adapter: EducationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEducationCenterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // A definição do título foi movida para onResume()
        setupRecyclerView()
        observeViewModel()
    }

    // ✅ ADIÇÃO: O método onResume é chamado sempre que o fragmento volta a ser exibido.
    override fun onResume() {
        super.onResume()
        // Isto garante que o título da toolbar seja sempre o correto para esta tela.
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.education_center_title)
    }

    private fun setupRecyclerView() {
        adapter = EducationAdapter { artigo ->
            val action = EducationCenterFragmentDirections.actionEducationCenterFragmentToEducationDetailFragment(artigo)
            findNavController().navigate(action)
        }
        binding.recyclerViewArticles.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewArticles.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.artigos)
            binding.emptyStateLayout.isVisible = state.artigos.isEmpty()

            // Popula os filtros de categoria dinamicamente
            setupCategoryFilters(state.categorias, state.filtroAtual)
        }
    }

    private fun setupCategoryFilters(categorias: List<String>, filtroAtual: String?) {
        val chipGroup = binding.chipGroupFilters
        if (chipGroup.childCount == categorias.size) return // Evita recriar os chips desnecessariamente

        chipGroup.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        categorias.forEach { categoria ->
            val chip = inflater.inflate(R.layout.single_filter_chip, chipGroup, false) as Chip
            chip.id = View.generateViewId()
            chip.text = categoria
            chip.isChecked = (filtroAtual == categoria) || (filtroAtual == null && categoria == "Todos")
            chip.setOnClickListener {
                viewModel.applyFilter(if (categoria == "Todos") null else categoria)
            }
            chipGroup.addView(chip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}