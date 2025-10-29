package com.developersbeeh.medcontrol.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.Sexo
import com.developersbeeh.medcontrol.data.model.TipoSanguineo
import com.developersbeeh.medcontrol.databinding.FragmentRegisterBinding
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels()
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdowns()
        setupListeners()
        observeViewModel()
    }

    private fun setupDropdowns() {
        val sexoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, Sexo.values().map { it.displayName })
        binding.autoCompleteSexo.setAdapter(sexoAdapter)

        val tipoSanguineoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, TipoSanguineo.values().map { it.displayName })
        binding.autoCompleteTipoSanguineo.setAdapter(tipoSanguineoAdapter)
    }

    private fun setupListeners() {
        binding.editTextDob.setOnClickListener { showDatePicker() }
        binding.tilDob.setEndIconOnClickListener { showDatePicker() }

        binding.buttonCreateAccount.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val pass = binding.editTextPassword.text.toString()
            val confirmPass = binding.editTextConfirmPassword.text.toString()

            // Coleta os novos dados de saúde
            val dataNascimento = binding.editTextDob.text.toString()
            val sexo = binding.autoCompleteSexo.text.toString()
            val tipoSanguineo = binding.autoCompleteTipoSanguineo.text.toString()
            val peso = binding.editTextWeight.text.toString()
            val altura = binding.editTextAltura.text.toString()
            val condicoes = binding.editTextConditions.text.toString()
            val alergias = binding.editTextAllergies.text.toString()

            viewModel.createAccount(
                name, email, pass, confirmPass,
                dataNascimento, sexo, tipoSanguineo, peso, altura, condicoes, alergias
            )
        }

        binding.buttonGoToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecione a Data de Nascimento")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            // ✅ CORREÇÃO AQUI
            val selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneOffset.UTC).toLocalDate()
            binding.editTextDob.setText(selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        }
        datePicker.show(childFragmentManager, "DATE_PICKER_REGISTER")
    }

    private fun observeViewModel() {
        viewModel.authStatus.observe(viewLifecycleOwner) { status ->
            binding.progressBar.visibility = if (status == AuthStatus.LOADING) View.VISIBLE else View.GONE
        }

        viewModel.errorEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.navigateToHomeEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { user ->
                userPreferences.saveIsCaregiver(true)
                userPreferences.saveUserName(binding.editTextName.text.toString().trim())
                userPreferences.saveUserEmail(user.email!!)
                findNavController().navigate(R.id.action_registerFragment_to_caregiverDashboardFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}