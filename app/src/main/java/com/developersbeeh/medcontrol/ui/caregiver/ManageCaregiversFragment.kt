package com.developersbeeh.medcontrol.ui.caregiver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.Convite
import com.developersbeeh.medcontrol.data.model.Usuario
import com.developersbeeh.medcontrol.databinding.FragmentManageCaregiversBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManageCaregiversFragment : Fragment() {

    private var _binding: FragmentManageCaregiversBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ManageCaregiversViewModel by viewModels()
    private val args: ManageCaregiversFragmentArgs by navArgs()

    private lateinit var caregiversAdapter: CaregiverAdapter
    private lateinit var invitesAdapter: PendingInviteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageCaregiversBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initialize(args.dependente)

        setupRecyclerViews()
        setupListeners()
        observeViewModel()

        args.dependente?.let {
            if (it.isSelfCareProfile) {
                binding.fabInviteCaregiver.visibility = View.GONE
                binding.textViewSelfCareInfo.visibility = View.VISIBLE
            } else {
                binding.fabInviteCaregiver.visibility = View.VISIBLE
                binding.textViewSelfCareInfo.visibility = View.GONE
            }
        }
    }

    private fun setupRecyclerViews() {
        // ✅ CORREÇÃO: Obtém o ID do usuário atual e o passa para o construtor do adapter.
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        caregiversAdapter = CaregiverAdapter(currentUserId) { caregiver ->
            showRemoveCaregiverConfirmationDialog(caregiver)
        }
        binding.recyclerViewCurrentCaregivers.adapter = caregiversAdapter

        invitesAdapter = PendingInviteAdapter { invite ->
            showCancelInviteConfirmationDialog(invite)
        }
        binding.recyclerViewPendingInvites.adapter = invitesAdapter
    }

    private fun setupListeners() {
        binding.fabInviteCaregiver.setOnClickListener {
            showInviteDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.selectedDependente.observe(viewLifecycleOwner) { dependente ->
            val title = if (dependente != null) {
                getString(R.string.manage_caregivers_of_dependent, dependente.nome)
            } else {
                getString(R.string.manage_caregivers_title)
            }
            (activity as? AppCompatActivity)?.supportActionBar?.title = title
        }

        viewModel.currentCaregivers.observe(viewLifecycleOwner) { caregivers ->
            caregiversAdapter.submitList(caregivers)
            binding.titleCurrentCaregivers.visibility = if (caregivers.isNotEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.pendingInvites.observe(viewLifecycleOwner) { invites ->
            invitesAdapter.submitList(invites)
            binding.titlePendingInvites.visibility = if (invites.isNotEmpty()) View.VISIBLE else View.GONE
            binding.textViewNoPendingInvites.visibility = if (invites.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.actionFeedback.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showInviteDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "email@exemplo.com"
            maxLines = 1
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }

        val container = android.widget.FrameLayout(requireContext()).apply {
            val padding = (20 * resources.displayMetrics.density).toInt()
            setPadding(padding, 0, padding, 0)
            addView(editText)
        }

        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.invite_new_caregiver))
            .setMessage(getString(R.string.invite_caregiver_message))
            .setView(container)
            .setPositiveButton(getString(R.string.send_invite)) { _, _ ->
                val email = editText.text.toString().trim()
                if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    viewModel.inviteCaregiver(email)
                } else {
                    Toast.makeText(requireContext(), getString(R.string.invalid_email_please_insert), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
            .show()
    }

    private fun showRemoveCaregiverConfirmationDialog(caregiver: Usuario) {
        val dependentName = args.dependente?.nome ?: "o dependente"
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.remove_caregiver_title))
            .setMessage(getString(R.string.remove_caregiver_message, caregiver.name, dependentName))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.remove)) { _, _ ->
                viewModel.removeCaregiver(caregiver)
            }
            .create()
            .show()
    }

    private fun showCancelInviteConfirmationDialog(invite: Convite) {
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.cancel_invite_title))
            .setMessage(getString(R.string.cancel_invite_message, invite.destinatarioEmail))
            .setNegativeButton(getString(R.string.dialog_option_no), null)
            .setPositiveButton(getString(R.string.dialog_option_yes_cancel)) { _, _ ->
                viewModel.cancelInvite(invite)
            }
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}