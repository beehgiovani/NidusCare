package com.developersbeeh.medcontrol.ui.searchmedicamentos

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.developersbeeh.medcontrol.databinding.FragmentSearchMedicamentosBinding
import com.developersbeeh.medcontrol.ui.addmedicamento.MedicamentoDatabaseAdapter

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchMedicamentosFragment : Fragment() {

    private var _binding: FragmentSearchMedicamentosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchMedicamentosViewModel by viewModels()
    private lateinit var adapter: MedicamentoDatabaseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchMedicamentosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MedicamentoDatabaseAdapter { medicamentoDatabase ->
            val medicamentoParaNavegar = medicamentoDatabase.toMedicamentoUsuario()
            val action = SearchMedicamentosFragmentDirections
                .actionSearchMedicamentosFragmentToMedicamentoDetailFragment(medicamentoParaNavegar)
            findNavController().navigate(action)
        }

        binding.recyclerViewMedicamentos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMedicamentos.adapter = adapter

        binding.editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchMedicamentos()
                true
            } else false
        }

        // --- MUDANÇA 1: Chips agora refazem a busca automaticamente ---
        binding.chipNome.setOnClickListener {
            viewModel.setSearchType(SearchType.NOME)
            searchMedicamentos()
        }
        binding.chipPrincipioAtivo.setOnClickListener {
            viewModel.setSearchType(SearchType.PRINCIPIO_ATIVO)
            searchMedicamentos()
        }
        binding.chipClasseTerapeutica.setOnClickListener {
            viewModel.setSearchType(SearchType.CLASSE_TERAPEUTICA)
            searchMedicamentos()
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                adapter.submitList(state.medicamentos)
                binding.textViewResultados.text = "${state.medicamentos.size} resultados"

                when (state.searchType) {
                    SearchType.NOME -> binding.chipGroup.check(binding.chipNome.id)
                    SearchType.PRINCIPIO_ATIVO -> binding.chipGroup.check(binding.chipPrincipioAtivo.id)
                    SearchType.CLASSE_TERAPEUTICA -> binding.chipGroup.check(binding.chipClasseTerapeutica.id)
                }
            }
        }

        // --- MUDANÇA 2: Foco automático no campo de busca e teclado visível ---
        focusSearchAndShowKeyboard()
    }

    private fun searchMedicamentos() {
        val query = binding.editTextSearch.text.toString()
        // A busca só acontece se houver texto, para não buscar com o campo vazio
        if (query.isNotBlank()) {
            viewModel.searchMedicamentos(query)
        }
    }

    // --- NOVA FUNÇÃO ADICIONADA ---
    private fun focusSearchAndShowKeyboard() {
        binding.editTextSearch.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(binding.editTextSearch, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}