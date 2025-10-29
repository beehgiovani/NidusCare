// src/main/java/com/developersbeeh/medcontrol/ui/documents/AddEditDocumentFragment.kt
package com.developersbeeh.medcontrol.ui.documents

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.developersbeeh.medcontrol.data.model.DocumentoSaude
import com.developersbeeh.medcontrol.data.model.TipoDocumento
import com.developersbeeh.medcontrol.databinding.FragmentAddEditDocumentBinding
import com.developersbeeh.medcontrol.ui.common.LoadingDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AddEditDocumentFragment : Fragment() {

    private var _binding: FragmentAddEditDocumentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditDocumentViewModel by viewModels()
    private val args: AddEditDocumentFragmentArgs by navArgs()

    private var selectedFileUri: Uri? = null
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private var selectedDate: LocalDate = LocalDate.now()
    private var loadingDialog: LoadingDialogFragment? = null

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            displaySelectedFileName(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditDocumentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTypeDropdown()

        if (args.documento != null) {
            (activity as? AppCompatActivity)?.supportActionBar?.title = "Editar Documento"
            binding.textViewTitle.text = "Editar Documento"
            prefillForm(args.documento!!)
        } else {
            (activity as? AppCompatActivity)?.supportActionBar?.title = "Novo Documento"
            binding.textViewTitle.text = "Adicionar Novo Documento"
            binding.editTextDocDate.setText(selectedDate.format(dateFormatter))
        }

        setupListeners()
        observeViewModel()
    }

    private fun prefillForm(documento: DocumentoSaude) {
        binding.autoCompleteDocType.setText(documento.tipo.displayName, false)
        binding.editTextDocTitle.setText(documento.titulo)
        selectedDate = LocalDate.parse(documento.dataDocumento, DateTimeFormatter.ISO_LOCAL_DATE)
        binding.editTextDocDate.setText(selectedDate.format(dateFormatter))
        binding.editTextDoctor.setText(documento.medicoSolicitante)
        binding.editTextLab.setText(documento.laboratorio)
        binding.editTextNotes.setText(documento.anotacoes)
        binding.textViewFileName.text = documento.fileName.ifEmpty { "Arquivo existente" }
    }

    private fun setupTypeDropdown() {
        val types = TipoDocumento.values().map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types)
        binding.autoCompleteDocType.setAdapter(adapter)
        binding.autoCompleteDocType.setText(types[0], false)
    }

    private fun setupListeners() {
        binding.buttonSelectFile.setOnClickListener {
            filePickerLauncher.launch("*/*") // Permite qualquer tipo de arquivo
        }
        binding.editTextDocDate.setOnClickListener { showDatePicker() }
        binding.tilDocDate.setEndIconOnClickListener { showDatePicker() }

        binding.buttonSave.setOnClickListener {
            saveDocument()
        }
        binding.buttonCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun saveDocument() {
        viewModel.saveDocument(
            dependentId = args.dependentId,
            documentoToEdit = args.documento,
            titulo = binding.editTextDocTitle.text.toString().trim(),
            tipo = TipoDocumento.values().first { it.displayName == binding.autoCompleteDocType.text.toString() },
            data = selectedDate,
            medico = binding.editTextDoctor.text.toString().trim().takeIf { it.isNotEmpty() },
            laboratorio = binding.editTextLab.text.toString().trim().takeIf { it.isNotEmpty() },
            anotacoes = binding.editTextNotes.text.toString().trim().takeIf { it.isNotEmpty() },
            fileUri = selectedFileUri
        )
    }

    private fun observeViewModel() {
        viewModel.showLoading.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                loadingDialog = LoadingDialogFragment.newInstance(message)
                loadingDialog?.show(childFragmentManager, LoadingDialogFragment.TAG)
            }
        }

        viewModel.hideLoading.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                loadingDialog?.dismissAllowingStateLoss()
                loadingDialog = null
            }
        }

        viewModel.saveStatus.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                result.onSuccess {
                    Toast.makeText(context, "Documento salvo com sucesso!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }.onFailure {
                    Toast.makeText(context, "Erro ao salvar: ${it.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun displaySelectedFileName(uri: Uri) {
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            binding.textViewFileName.text = cursor.getString(nameIndex)
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Data do Documento")
            .setSelection(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
            .build()
        datePicker.addOnPositiveButtonClickListener {
            selectedDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            binding.editTextDocDate.setText(selectedDate.format(dateFormatter))
        }
        datePicker.show(childFragmentManager, "DOC_DATE_PICKER")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}