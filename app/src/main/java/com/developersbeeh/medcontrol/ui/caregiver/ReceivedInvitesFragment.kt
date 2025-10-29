package com.developersbeeh.medcontrol.ui.caregiver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // ou activityViewModels se for compartilhado
import com.developersbeeh.medcontrol.databinding.FragmentReceivedInvitesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReceivedInvitesFragment : Fragment() {

    private var _binding: FragmentReceivedInvitesBinding? = null
    private val binding get() = _binding!!

    // Usando by viewModels() cria uma instância deste ViewModel para este Fragment.
    // Se precisar compartilhar a mesma instância com ManageCaregiversFragment, use by activityViewModels().
    private val viewModel: ManageCaregiversViewModel by viewModels()

    private lateinit var receivedInvitesAdapter: ReceivedInviteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceivedInvitesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        // Ponto chave 1: Inicia o carregamento dos convites recebidos
        viewModel.loadReceivedInvites()
    }

    private fun setupRecyclerView() {
        // Ponto chave 2: Conecta os cliques dos botões às funções do ViewModel
        receivedInvitesAdapter = ReceivedInviteAdapter(
            onAcceptClick = { convite -> viewModel.acceptInvite(convite) },
            onDeclineClick = { convite -> viewModel.cancelInvite(convite) } // Reutiliza a função de cancelar
        )
        binding.recyclerViewReceivedInvites.adapter = receivedInvitesAdapter
    }

    private fun observeViewModel() {
        // Ponto chave 3: Observa a lista de convites e atualiza a UI
        viewModel.receivedInvites.observe(viewLifecycleOwner) { invites ->
            receivedInvitesAdapter.submitList(invites)
            val hasReceivedInvites = invites.isNotEmpty()

            binding.recyclerViewReceivedInvites.visibility = if (hasReceivedInvites) View.VISIBLE else View.GONE
            binding.emptyStateReceivedInvites.visibility = if (!hasReceivedInvites) View.VISIBLE else View.GONE
        }

        // Observa o feedback para exibir mensagens (Toast) ao usuário
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