package com.developersbeeh.medcontrol.ui.profile

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.FragmentProfileBinding
import com.developersbeeh.medcontrol.databinding.ItemSettingBinding
import com.developersbeeh.medcontrol.ui.MainViewModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadProfileData()
        setupCards()
        observeViewModel()
    }

    private fun setupCards() {
        setupActionCard(
            binding.layoutEditProfile, binding.cardEditProfile, R.drawable.ic_edit, "Editar Perfil",
            "Atualize seu nome e foto", isDestructive = false
        ) { findNavController().navigate(R.id.action_profileFragment_to_profileEditFragment) }

        // ✅ NOVO CARD CONFIGURADO AQUI
        setupActionCard(
            binding.layoutMyHealthData, binding.cardMyHealthData, R.drawable.ic_file_document, "Meus Dados de Saúde",
            "Edite seu peso, altura, alergias e mais", isDestructive = false
        ) { findNavController().navigate(R.id.action_profileFragment_to_editSelfProfileHealthFragment) }

        setupActionCard(
            binding.layoutChangePassword, binding.cardChangePassword, R.drawable.ic_lock, "Alterar Senha",
            "Mantenha sua conta segura", isDestructive = false
        ) { showChangePasswordDialog() }

        setupActionCard(
            binding.layoutManageDependents, binding.cardManageDependents, R.drawable.ic_person, "Gerenciar Dependentes",
            "Adicione ou edite perfis de dependentes", isDestructive = false
        ) { findNavController().navigate(R.id.action_global_to_dashboard) }

        setupActionCard(
            binding.layoutDeleteAccount, binding.cardDeleteAccount, R.drawable.ic_delete, "Excluir Conta",
            "Esta ação é permanente", isDestructive = true
        ) { showDeleteConfirmationDialog() }
    }

    // ✅ FUNÇÃO ATUALIZADA PARA RECEBER OS BINDINGS DIRETAMENTE
    private fun setupActionCard(
        cardBinding: ItemSettingBinding,
        cardView: MaterialCardView,
        iconRes: Int,
        title: String,
        subtitle: String,
        isDestructive: Boolean,
        onClick: () -> Unit
    ) {
        cardBinding.imageViewIcon.setImageResource(iconRes)
        cardBinding.textViewTitle.text = title
        cardBinding.textViewSubtitle.text = subtitle

        if (isDestructive) {
            val errorColor = ContextCompat.getColor(requireContext(), R.color.md_theme_error)
            cardBinding.imageViewIcon.imageTintList = ColorStateList.valueOf(errorColor)
            cardBinding.textViewTitle.setTextColor(errorColor)
        }

        cardView.setOnClickListener { onClick() }
    }


    private fun observeViewModel() {
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.textViewUserName.text = it.name
                binding.textViewUserEmail.text = it.email
                binding.imageViewUserProfilePhoto.load(it.photoUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_person)
                    error(R.drawable.ic_person)
                    transformations(CircleCropTransformation())
                }
            }
        }

        viewModel.premiumStatusText.observe(viewLifecycleOwner) { statusText ->
            if (statusText != null) {
                binding.textViewPremiumStatus.text = statusText
                binding.textViewPremiumStatus.visibility = View.VISIBLE
            } else {
                binding.textViewPremiumStatus.visibility = View.GONE
            }
        }

        viewModel.actionFeedback.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.deleteAccountResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                result.onSuccess {
                    Toast.makeText(context, "Conta e todos os dados foram excluídos com sucesso.", Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_global_logout)
                }.onFailure {
                    Toast.makeText(context, "Erro ao excluir conta: ${it.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_change_password, null)
        val currentPasswordEt = dialogView.findViewById<EditText>(R.id.editTextCurrentPassword)
        val newPasswordEt = dialogView.findViewById<EditText>(R.id.editTextNewPassword)
        val confirmPasswordEt = dialogView.findViewById<EditText>(R.id.editTextConfirmPassword)

        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle("Alterar Senha")
            .setView(dialogView)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salvar", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val current = currentPasswordEt.text.toString()
                val new = newPasswordEt.text.toString()
                val confirm = confirmPasswordEt.text.toString()

                when {
                    current.isBlank() || new.isBlank() || confirm.isBlank() -> {
                        Toast.makeText(context, "Todos os campos são obrigatórios.", Toast.LENGTH_SHORT).show()
                    }
                    new.length < 6 -> {
                        Toast.makeText(context, "A nova senha deve ter pelo menos 6 caracteres.", Toast.LENGTH_SHORT).show()
                    }
                    new != confirm -> {
                        Toast.makeText(context, "As novas senhas não coincidem.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        viewModel.changePassword(current, new)
                        dialog.dismiss()
                    }
                }
            }
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle("Excluir Conta")
            .setMessage("Esta é uma ação irreversível. Todos os seus dados, incluindo os de seus dependentes, serão apagados permanentemente. Tem certeza que deseja continuar?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Continuar") { _, _ ->
                showFinalDeleteConfirmationDialog()
            }
            .create()

        dialog.show()
    }
    private fun showFinalDeleteConfirmationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reauthenticate_delete, null)
        val confirmTextEt = dialogView.findViewById<EditText>(R.id.editTextConfirmText)
        val passwordEt = dialogView.findViewById<EditText>(R.id.editTextPassword)
        val passwordTil = dialogView.findViewById<TextInputLayout>(R.id.tilPassword)

        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle("Confirmação Final e Reautenticação")
            .setView(dialogView)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Excluir Permanentemente", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = false

            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val isTextOk = confirmTextEt.text.toString().uppercase() == "EXCLUIR"
                    val isPasswordOk = passwordEt.text.isNotBlank()
                    positiveButton.isEnabled = isTextOk && isPasswordOk
                }
                override fun afterTextChanged(s: Editable?) {}
            }

            confirmTextEt.addTextChangedListener(textWatcher)
            passwordEt.addTextChangedListener(textWatcher)

            positiveButton.setOnClickListener {
                val password = passwordEt.text.toString()
                passwordTil.error = null
                viewModel.reauthenticateAndDeleteAccount(password)
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}