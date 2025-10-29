package com.developersbeeh.medcontrol.ui.scan

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.developersbeeh.medcontrol.BuildConfig
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.FragmentScanAndConfirmBinding
import com.developersbeeh.medcontrol.ui.common.LoadingDialogFragment // ✅ ADIÇÃO
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@AndroidEntryPoint
class ScanAndConfirmFragment : Fragment() {

    private var _binding: FragmentScanAndConfirmBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScanAndConfirmViewModel by viewModels()
    private val args: ScanAndConfirmFragmentArgs by navArgs()
    private var currentPhotoUri: Uri? = null

    private val expirationDateFormatter = DateTimeFormatter.ofPattern("MM/yyyy")

    // ✅ ADIÇÃO: Variável para o diálogo
    private var loadingDialog: LoadingDialogFragment? = null

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                binding.imageViewPreview.setImageURI(uri)
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    viewModel.analyzeImage(uri, userId)
                }
            }
        } else {
            findNavController().popBackStack()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScanAndConfirmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.scan_medication_box_title)

        observeViewModel()
        setupListeners()

        if (savedInstanceState == null) {
            startCamera()
        }
    }

    private fun startCamera() {
        currentPhotoUri = getTmpFileUri()
        takePicture.launch(currentPhotoUri)
    }

    private fun observeViewModel() {
        // ✅ ADIÇÃO: Observadores para o diálogo de loading
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

        viewModel.scanState.observe(viewLifecycleOwner) { state ->
            // ✅ CORREÇÃO: Remove a lógica do 'loadingOverlay'
            binding.buttonSalvar.isEnabled = state is ScanState.Success

            when (state) {
                is ScanState.Success -> populateFields(state.extractedData)
                is ScanState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }
                else -> {}
            }
        }

        viewModel.saveStatus.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { success ->
                if (success) {
                    Toast.makeText(context, getString(R.string.medication_saved_to_farmacinha), Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(context, getString(R.string.error_saving_medication), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.fabRetakePhoto.setOnClickListener { startCamera() }
        binding.editTextValidade.setOnClickListener { showMonthYearPicker() }
        binding.tilValidade.setEndIconOnClickListener { showMonthYearPicker() }

        binding.buttonSalvar.setOnClickListener {
            binding.tilNome.error = null
            binding.tilEstoque.error = null
            binding.tilValidade.error = null

            val nome = binding.editTextNome.text.toString().trim()
            val estoque = binding.editTextEstoque.text.toString().toDoubleOrNull()
            val validadeStr = binding.editTextValidade.text.toString().trim()

            var isValid = true
            if (nome.isBlank()) {
                binding.tilNome.error = getString(R.string.field_required)
                isValid = false
            }
            if (estoque == null || estoque <= 0) {
                binding.tilEstoque.error = getString(R.string.invalid_quantity)
                isValid = false
            }
            if (validadeStr.isBlank()) {
                binding.tilValidade.error = getString(R.string.field_required)
                isValid = false
            }
            if (!isValid) return@setOnClickListener

            try {
                val validade = YearMonth.parse(validadeStr, expirationDateFormatter).atEndOfMonth()
                viewModel.saveScannedMedication(
                    dependentId = args.dependentId,
                    nome = nome,
                    principioAtivo = binding.editTextPrincipioAtivo.text.toString().trim(),
                    estoque = estoque!!,
                    validade = validade,
                    loteNumero = binding.editTextLote.text.toString().trim(),
                    classeTerapeutica = binding.editTextClasseTerapeutica.text.toString().trim(),
                    anotacoes = binding.editTextAnotacoes.text.toString().trim()
                )
            } catch (e: DateTimeParseException) {
                binding.tilValidade.error = getString(R.string.error_invalid_date_format_mm_yyyy)
            }
        }
    }

    private fun populateFields(data: ExtractedMedicationData) {
        binding.editTextNome.setText(data.nome ?: "")
        binding.editTextPrincipioAtivo.setText(data.principioAtivo ?: "")
        binding.editTextEstoque.setText(data.estoque?.toInt()?.toString() ?: "")
        binding.editTextLote.setText(data.lote ?: "")
        binding.editTextClasseTerapeutica.setText(data.classeTerapeutica ?: "")
        binding.editTextAnotacoes.setText(data.anotacoes ?: "")
        binding.editTextValidade.setText(data.validade ?: "")
    }

    private fun showMonthYearPicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.expiration_date))
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val date = Instant.ofEpochMilli(selection).atZone(ZoneOffset.UTC).toLocalDate()
            binding.editTextValidade.setText(date.format(expirationDateFormatter))
        }
        datePicker.show(childFragmentManager, "EXPIRATION_DATE_PICKER")
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("temp_image_file", ".jpg", requireContext().cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(requireActivity(), "${BuildConfig.APPLICATION_ID}.provider", tmpFile)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}