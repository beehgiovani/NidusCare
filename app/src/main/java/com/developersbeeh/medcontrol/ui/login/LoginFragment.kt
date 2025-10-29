// src/main/java/com/developersbeeh/medcontrol/ui/login/LoginFragment.kt
package com.developersbeeh.medcontrol.ui.login

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.databinding.FragmentLoginBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val TAG = "LoginFragment"

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            viewModel.loginWithEmail(email, password)
        }

        binding.buttonGoogleSignIn.setOnClickListener {
            // Inicia o fluxo do Credential Manager
            signInWithGoogleOneTap()
        }

        binding.buttonForgotPassword.setOnClickListener {
            showPasswordResetDialog()
        }

        binding.buttonCreateAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun signInWithGoogleOneTap() {
        val credentialManager = CredentialManager.create(requireContext())
        val serverClientId = getString(R.string.default_web_client_id)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(requireActivity(), request)
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                viewModel.firebaseAuthWithGoogle(firebaseCredential)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "GetCredentialException", e)
                Toast.makeText(context, "Falha no login com Google: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.authStatus.observe(viewLifecycleOwner) { status ->
            binding.loadingOverlay.visibility = if (status == AuthStatus.LOADING) View.VISIBLE else View.GONE
        }

        viewModel.errorEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.successEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.navigateToHomeEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { user ->
                val userPreferences = UserPreferences(requireContext())
                userPreferences.saveIsCaregiver(true)
                userPreferences.saveUserName(user.displayName ?: "")
                userPreferences.saveUserEmail(user.email ?: "")
                userPreferences.saveUserPhotoUrl(user.photoUrl?.toString())
                findNavController().navigate(R.id.action_loginFragment_to_caregiverDashboardFragment)
            }
        }

        viewModel.navigateToCompleteProfileEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                findNavController().navigate(R.id.action_loginFragment_to_completeProfileFragment)
            }
        }
    }

    private fun showPasswordResetDialog() {
        val editText = EditText(requireContext()).apply {
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            hint = "email@exemplo.com"
        }
        val container = android.widget.FrameLayout(requireContext()).apply {
            val padding = (20 * resources.displayMetrics.density).toInt()
            setPadding(padding, 0, padding, 0)
            addView(editText)
        }

        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle("Redefinir Senha")
            .setMessage("Digite seu e-mail para enviarmos um link de redefinição.")
            .setView(container)
            .setPositiveButton("Enviar") { _, _ ->
                viewModel.sendPasswordResetEmail(editText.text.toString())
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}