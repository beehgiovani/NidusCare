// src/main/java/com/developersbeeh/medcontrol/ui/profile/ProfileEditFragment.kt

package com.developersbeeh.medcontrol.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.FragmentProfileEditBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileEditFragment : Fragment() {

    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileEditViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    // Launcher para selecionar uma imagem da galeria
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.imageViewProfilePhoto.load(it) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Permite que o usuário clique na imagem para trocá-la
        binding.imageViewProfilePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.buttonSave.setOnClickListener {
            val newName = binding.editTextName.text.toString().trim()
            if (newName.isNotEmpty()) {
                // Passa o nome e a nova imagem (se houver) para o ViewModel
                viewModel.updateProfile(newName, selectedImageUri)
            } else {
                binding.tilName.error = "O nome não pode ficar em branco."
            }
        }
    }

    private fun observeViewModel() {
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            binding.editTextName.setText(user.name)
            binding.editTextEmail.setText(user.email)

            // Carrega a foto de perfil atual do usuário
            binding.imageViewProfilePhoto.load(user.photoUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_person)
                error(R.drawable.ic_person)
                transformations(CircleCropTransformation())
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonSave.isEnabled = !isLoading
        }

        viewModel.updateStatus.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                result.onSuccess {
                    Toast.makeText(requireContext(), "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }.onFailure {
                    Toast.makeText(requireContext(), "Falha ao atualizar o perfil: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // CORREÇÃO: Observa o novo evento de erro e exibe uma mensagem para o usuário.
        viewModel.errorEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { errorMessage ->
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}