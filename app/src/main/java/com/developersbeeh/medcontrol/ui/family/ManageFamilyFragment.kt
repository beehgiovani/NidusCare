package com.developersbeeh.medcontrol.ui.family

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.FragmentManageFamilyBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManageFamilyFragment : Fragment() {

    private var _binding: FragmentManageFamilyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ManageFamilyViewModel by viewModels()
    private var adapter: FamilyMemberAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageFamilyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        binding.fabInviteMember.setOnClickListener {
            showInviteDialog()
        }
    }

    private fun setupRecyclerView() {
        // O adapter será inicializado quando tivermos os dados da família
        binding.recyclerViewFamilyMembers.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
    }

    private fun observeViewModel() {
        viewModel.familyDetails.observe(viewLifecycleOwner) { family ->
            if (family == null) return@observe

            viewModel.isCurrentUserOwner.observe(viewLifecycleOwner) { isOwner ->
                binding.fabInviteMember.visibility = if (isOwner) View.VISIBLE else View.GONE

                // Inicializa ou atualiza o adapter com os dados corretos
                if (adapter == null) {
                    adapter = FamilyMemberAdapter(
                        onRemoveClick = { member -> showRemoveConfirmationDialog(member) },
                        ownerId = family.ownerId,
                        isCurrentUserOwner = isOwner
                    )
                    binding.recyclerViewFamilyMembers.adapter = adapter
                }

                viewModel.familyMembers.observe(viewLifecycleOwner) { members ->
                    adapter?.submitList(members)
                    binding.emptyStateLayout.visibility = if (members.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        viewModel.actionFeedback.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showInviteDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "email@do.cuidador"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        val container = android.widget.FrameLayout(requireContext()).apply {
            val padding = (20 * resources.displayMetrics.density).toInt()
            setPadding(padding, 0, padding, 0)
            addView(editText)
        }

        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle("Convidar para Família")
            .setMessage("Digite o e-mail do cuidador que você deseja adicionar ao seu Plano Família.")
            .setView(container)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Convidar") { _, _ ->
                val email = editText.text.toString().trim()
                if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    viewModel.inviteMember(email)
                } else {
                    Toast.makeText(context, "Por favor, insira um e-mail válido.", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun showRemoveConfirmationDialog(member: com.developersbeeh.medcontrol.data.model.Usuario) {
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle("Remover Membro?")
            .setMessage("Tem certeza que deseja remover ${member.name} da sua família? Ele(a) perderá o acesso aos benefícios Premium.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Remover") { _, _ ->
                viewModel.removeMember(member)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}