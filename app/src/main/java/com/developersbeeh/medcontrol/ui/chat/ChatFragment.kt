// src/main/java/com/developersbeeh/medcontrol/ui/chat/ChatFragment.kt

package com.developersbeeh.medcontrol.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.developersbeeh.medcontrol.databinding.FragmentChatBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()
    private val args: ChatFragmentArgs by navArgs()
    private lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Passa também o nome do dependente
        viewModel.initialize(args.dependentId, args.dependentName)

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Assistente de Saúde"

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter()
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.recyclerViewChat.adapter = adapter
    }

    private fun setupListeners() {
        binding.buttonSend.setOnClickListener {
            sendMessage()
        }

        binding.editTextMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
    }

    private fun sendMessage() {
        val text = binding.editTextMessage.text.toString().trim()
        if (text.isNotEmpty()) {
            viewModel.sendMessage(text)
            binding.editTextMessage.text?.clear()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ChatUiState.Loading -> {
                    // Pode adicionar um ProgressBar aqui se desejar
                }
                is ChatUiState.Success -> {
                    // A lista é enviada para o adapter.
                    adapter.submitList(state.messages)

                    // A rolagem é postada para a fila de UI para garantir que execute após a atualização.
                    binding.recyclerViewChat.post {
                        if (adapter.itemCount > 0) {
                            binding.recyclerViewChat.scrollToPosition(adapter.itemCount - 1)
                        }
                    }
                }
                is ChatUiState.Error -> {
                    // Pode exibir um toast ou uma mensagem de erro na tela
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}