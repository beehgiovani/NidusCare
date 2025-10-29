// src/main/java/com/developersbeeh/medcontrol/ui/caregiver/AddEditDependentFragment.kt
package com.developersbeeh.medcontrol.ui.caregiver

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import coil.transform.CircleCropTransformation
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.PermissaoTipo
import com.developersbeeh.medcontrol.data.model.Sexo
import com.developersbeeh.medcontrol.data.model.TipoSanguineo
import com.developersbeeh.medcontrol.databinding.FragmentAddDependentStep1Binding
import com.developersbeeh.medcontrol.databinding.FragmentAddDependentStep2Binding
import com.developersbeeh.medcontrol.databinding.FragmentAddDependentStep3Binding
import com.developersbeeh.medcontrol.databinding.FragmentAddDependentStep4Binding
import com.developersbeeh.medcontrol.databinding.FragmentAddEditDependentBinding
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@AndroidEntryPoint
class AddEditDependentFragment : Fragment() {

    private var _binding: FragmentAddEditDependentBinding? = null
    private val binding get() = _binding!!

    private lateinit var step1Binding: FragmentAddDependentStep1Binding
    private lateinit var step2Binding: FragmentAddDependentStep2Binding
    private lateinit var step3Binding: FragmentAddDependentStep3Binding
    private lateinit var step4Binding: FragmentAddDependentStep4Binding

    private val viewModel: AddEditDependentViewModel by viewModels()
    private val args: AddEditDependentFragmentArgs by navArgs()
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            step1Binding.imageViewDependentPhoto.load(it) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditDependentBinding.inflate(inflater, container, false)
        step1Binding = FragmentAddDependentStep1Binding.inflate(inflater, binding.stepContainer, false)
        step2Binding = FragmentAddDependentStep2Binding.inflate(inflater, binding.stepContainer, false)
        step3Binding = FragmentAddDependentStep3Binding.inflate(inflater, binding.stepContainer, false)
        step4Binding = FragmentAddDependentStep4Binding.inflate(inflater, binding.stepContainer, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        args.dependente?.let { viewModel.loadDependent(it) }

        val title = args.dependente?.let { "Editar Dependente" } ?: "Adicionar Dependente"
        (activity as? AppCompatActivity)?.supportActionBar?.title = title

        setupDropdowns()
        setupListeners()
        observeViewModel()
    }

    private fun setupDropdowns() {
        val sexoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, Sexo.values().map { it.displayName })
        step1Binding.autoCompleteSexo.setAdapter(sexoAdapter)

        val tipoSanguineoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, TipoSanguineo.values().map { it.displayName })
        step2Binding.autoCompleteTipoSanguineo.setAdapter(tipoSanguineoAdapter)
    }

    private fun setupListeners() {
        step1Binding.imageViewDependentPhoto.setOnClickListener { pickImageLauncher.launch("image/*") }
        step1Binding.editTextDob.setOnClickListener { showDatePicker() }
        step1Binding.tilDob.setEndIconOnClickListener { showDatePicker() }
        step1Binding.editTextName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrBlank()) {
                    step1Binding.tilName.error = null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.buttonPrevious.setOnClickListener { viewModel.previousStep() }
    }

    private fun observeViewModel() {
        viewModel.currentStep.observe(viewLifecycleOwner) { step ->
            updateWizardUi(step)
        }

        viewModel.dependente.observe(viewLifecycleOwner) { dependent ->
            dependent?.let {
                step1Binding.editTextName.setText(it.nome)
                step1Binding.editTextDob.setText(it.dataDeNascimento)
                val sexo = try { Sexo.valueOf(it.sexo) } catch (e: Exception) { Sexo.NAO_INFORMADO }
                step1Binding.autoCompleteSexo.setText(sexo.displayName, false)
                step1Binding.imageViewDependentPhoto.load(it.photoUrl) {
                    crossfade(true); placeholder(R.drawable.ic_person); error(R.drawable.ic_person); transformations(CircleCropTransformation())
                }
                step2Binding.editTextWeight.setText(it.peso)
                step2Binding.editTextAltura.setText(it.altura)
                val tipoSanguineo = try { TipoSanguineo.valueOf(it.tipoSanguineo) } catch (e: Exception) { TipoSanguineo.NAO_SABE }
                step2Binding.autoCompleteTipoSanguineo.setText(tipoSanguineo.displayName, false)
                step2Binding.editTextConditions.setText(it.condicoesPreexistentes)
                step2Binding.editTextAllergies.setText(it.alergias)

                step3Binding.editTextHydrationGoal.setText(it.metaHidratacaoMl.toString())
                step3Binding.editTextActivityGoal.setText(it.metaAtividadeMinutos.toString())
                step3Binding.editTextCaloriesGoal.setText(it.metaCaloriasDiarias.toString())
                step3Binding.editTextWeightGoal.setText(it.pesoMeta)

                step4Binding.layoutPermissions.switchCanRegisterDose.isChecked = it.permissoes[PermissaoTipo.REGISTRAR_DOSE.key] ?: true
                step4Binding.layoutPermissions.switchCanRegisterNotes.isChecked = it.permissoes[PermissaoTipo.REGISTRAR_ANOTACOES.key] ?: true
                step4Binding.layoutPermissions.switchCanSeeDocuments.isChecked = it.permissoes[PermissaoTipo.VER_DOCUMENTOS.key] ?: true
                step4Binding.layoutPermissions.switchCanAddDocuments.isChecked = it.permissoes[PermissaoTipo.ADICIONAR_DOCUMENTOS.key] ?: true
                step4Binding.layoutPermissions.switchCanSeeAgenda.isChecked = it.permissoes[PermissaoTipo.VER_AGENDA.key] ?: true
                // ✅ LEITURA DAS NOVAS PERMISSÕES DA AGENDA
                step4Binding.layoutPermissions.switchCanAddSchedules.isChecked = it.permissoes[PermissaoTipo.ADICIONAR_AGENDAMENTOS.key] ?: true
                step4Binding.layoutPermissions.switchCanEditSchedules.isChecked = it.permissoes[PermissaoTipo.EDITAR_AGENDAMENTOS.key] ?: true
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.buttonNext.isEnabled = !isLoading
            binding.buttonPrevious.isEnabled = !isLoading
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                result.onSuccess {
                    Toast.makeText(context, "Dependente salvo com sucesso!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }.onFailure { error ->
                    Toast.makeText(context, "Erro ao salvar: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateWizardUi(step: WizardStep) {
        binding.stepContainer.removeAllViews()
        binding.stepProgressBar.max = WizardStep.values().size
        binding.stepProgressBar.setProgressCompat(step.ordinal + 1, true)

        when (step) {
            WizardStep.STEP_1 -> {
                binding.stepContainer.addView(step1Binding.root)
                binding.buttonPrevious.isVisible = false
                binding.buttonNext.text = "Avançar"
                binding.buttonNext.setOnClickListener {
                    if (validateStep1()) viewModel.nextStep()
                }
            }
            WizardStep.STEP_2 -> {
                binding.stepContainer.addView(step2Binding.root)
                binding.buttonPrevious.isVisible = true
                binding.buttonNext.text = "Avançar"
                binding.buttonNext.setOnClickListener { viewModel.nextStep() }
            }
            WizardStep.STEP_3 -> {
                binding.stepContainer.addView(step3Binding.root)
                binding.buttonPrevious.isVisible = true
                binding.buttonNext.text = "Avançar"
                binding.buttonNext.setOnClickListener { viewModel.nextStep() }
            }
            WizardStep.STEP_4 -> {
                binding.stepContainer.addView(step4Binding.root)
                binding.buttonPrevious.isVisible = true
                binding.buttonNext.text = "Salvar"
                binding.buttonNext.setOnClickListener { saveAllData() }
            }
        }
    }

    private fun validateStep1(): Boolean {
        return if (step1Binding.editTextName.text.toString().trim().isBlank()) {
            step1Binding.tilName.error = "O nome é obrigatório"
            false
        } else {
            step1Binding.tilName.error = null
            true
        }
    }

    private fun saveAllData() {
        val permissoes = mapOf(
            PermissaoTipo.REGISTRAR_DOSE.key to step4Binding.layoutPermissions.switchCanRegisterDose.isChecked,
            PermissaoTipo.REGISTRAR_ANOTACOES.key to step4Binding.layoutPermissions.switchCanRegisterNotes.isChecked,
            PermissaoTipo.VER_DOCUMENTOS.key to step4Binding.layoutPermissions.switchCanSeeDocuments.isChecked,
            PermissaoTipo.ADICIONAR_DOCUMENTOS.key to step4Binding.layoutPermissions.switchCanAddDocuments.isChecked,
            PermissaoTipo.VER_AGENDA.key to step4Binding.layoutPermissions.switchCanSeeAgenda.isChecked,
            PermissaoTipo.ADICIONAR_AGENDAMENTOS.key to step4Binding.layoutPermissions.switchCanAddSchedules.isChecked,
            PermissaoTipo.EDITAR_AGENDAMENTOS.key to step4Binding.layoutPermissions.switchCanEditSchedules.isChecked
        )

        viewModel.saveDependent(
            id = args.dependente?.id,
            nome = step1Binding.editTextName.text.toString(),
            dataDeNascimento = step1Binding.editTextDob.text.toString(),
            sexo = step1Binding.autoCompleteSexo.text.toString(),
            peso = step2Binding.editTextWeight.text.toString(),
            altura = step2Binding.editTextAltura.text.toString(),
            tipoSanguineo = step2Binding.autoCompleteTipoSanguineo.text.toString(),
            condicoes = step2Binding.editTextConditions.text.toString(),
            alergias = step2Binding.editTextAllergies.text.toString(),
            observacoes = args.dependente?.observacoesMedicas ?: "",
            contatoNome = args.dependente?.contatoEmergenciaNome ?: "",
            contatoTelefone = args.dependente?.contatoEmergenciaTelefone ?: "",
            metaHidratacao = step3Binding.editTextHydrationGoal.text.toString().toIntOrNull() ?: 2000,
            metaAtividade = step3Binding.editTextActivityGoal.text.toString().toIntOrNull() ?: 30,
            metaCalorias = step3Binding.editTextCaloriesGoal.text.toString().toIntOrNull() ?: 2000,
            metaPeso = step3Binding.editTextWeightGoal.text.toString(),
            imageUri = selectedImageUri,
            permissoes = permissoes
        )
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Data de Nascimento")
            .build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            val selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            step1Binding.editTextDob.setText(selectedDate.format(formatter))
        }
        datePicker.show(childFragmentManager, "DATE_PICKER")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}