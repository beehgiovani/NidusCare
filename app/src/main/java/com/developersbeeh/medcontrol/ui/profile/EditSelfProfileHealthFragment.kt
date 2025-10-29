    // src/main/java/com/developersbeeh/medcontrol/ui/profile/EditSelfProfileHealthFragment.kt
    package com.developersbeeh.medcontrol.ui.profile

    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.ArrayAdapter
    import android.widget.Toast
    import androidx.core.view.isVisible
    import androidx.fragment.app.Fragment
    import androidx.fragment.app.viewModels
    import androidx.navigation.fragment.findNavController
    import com.developersbeeh.medcontrol.data.model.Sexo
    import com.developersbeeh.medcontrol.data.model.TipoSanguineo
    import com.developersbeeh.medcontrol.databinding.FragmentEditSelfProfileHealthBinding
    import com.google.android.material.datepicker.MaterialDatePicker
    import dagger.hilt.android.AndroidEntryPoint
    import java.time.Instant
    import java.time.LocalDate
    import java.time.ZoneId
    import java.time.ZoneOffset
    import java.time.format.DateTimeFormatter

    @AndroidEntryPoint
    class EditSelfProfileHealthFragment : Fragment() {

        private var _binding: FragmentEditSelfProfileHealthBinding? = null
        private val binding get() = _binding!!

        private val viewModel: EditSelfProfileHealthViewModel by viewModels()

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentEditSelfProfileHealthBinding.inflate(inflater, container, false)
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

            binding.buttonSave.setOnClickListener {
                viewModel.saveHealthData(
                    dataNascimento = binding.editTextDob.text.toString(),
                    sexo = binding.autoCompleteSexo.text.toString(),
                    tipoSanguineo = binding.autoCompleteTipoSanguineo.text.toString(),
                    peso = binding.editTextWeight.text.toString(),
                    altura = binding.editTextAltura.text.toString(),
                    condicoes = binding.editTextConditions.text.toString(),
                    alergias = binding.editTextAllergies.text.toString()
                )
            }
        }

        private fun observeViewModel() {
            viewModel.userProfile.observe(viewLifecycleOwner) { user ->
                user?.let {
                    binding.editTextDob.setText(it.dataDeNascimento)
                    binding.editTextWeight.setText(it.peso)
                    binding.editTextAltura.setText(it.altura)
                    binding.editTextConditions.setText(it.condicoesPreexistentes)
                    binding.editTextAllergies.setText(it.alergias)

                    // ✅ CORREÇÃO APLICADA AQUI PARA CARREGAMENTO SEGURO DOS ENUMS
                    val sexo = Sexo.values().firstOrNull { enum -> enum.name == it.sexo || enum.displayName == it.sexo } ?: Sexo.NAO_INFORMADO
                    binding.autoCompleteSexo.setText(sexo.displayName, false)

                    val tipoSanguineo = TipoSanguineo.values().firstOrNull { enum -> enum.name == it.tipoSanguineo || enum.displayName == it.tipoSanguineo } ?: TipoSanguineo.NAO_SABE
                    binding.autoCompleteTipoSanguineo.setText(tipoSanguineo.displayName, false)
                }
            }

            viewModel.isLoading.observe(viewLifecycleOwner) {
                binding.progressBar.isVisible = it
                binding.buttonSave.isEnabled = !it
            }

            viewModel.updateStatus.observe(viewLifecycleOwner) { event ->
                event.getContentIfNotHandled()?.let { result ->
                    result.onSuccess {
                        Toast.makeText(context, "Dados de saúde atualizados!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }.onFailure {
                        Toast.makeText(context, "Erro ao salvar: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        private fun showDatePicker() {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecione a Data de Nascimento")
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneOffset.UTC).toLocalDate()
                binding.editTextDob.setText(selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            }
            datePicker.show(parentFragmentManager, "DATE_PICKER_HEALTH_PROFILE")
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }