// src/main/java/com/developersbeeh/medcontrol/ui/pharmacy/PharmacyMedicationSelectionFragment.kt
package com.developersbeeh.medcontrol.ui.pharmacy

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.FragmentPharmacyMedicationSelectionBinding
import com.developersbeeh.medcontrol.util.UiState
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder

@AndroidEntryPoint
class PharmacyMedicationSelectionFragment : Fragment() {

    private var _binding: FragmentPharmacyMedicationSelectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PharmacyMedicationSelectionViewModel by viewModels()
    private val args: PharmacyMedicationSelectionFragmentArgs by navArgs()
    private lateinit var adapter: PharmacyMedicationSelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPharmacyMedicationSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Selecionar Medicamentos"
        binding.textViewPharmacyName.text = args.pharmacyName

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = PharmacyMedicationSelectionAdapter { medication ->
            viewModel.toggleMedicationSelection(medication)
        }
        binding.recyclerViewMedications.adapter = adapter
        binding.recyclerViewMedications.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupListeners() {
        binding.chipGroupFilter.setOnCheckedChangeListener { _, checkedId ->
            val showLowStockOnly = checkedId == R.id.chipLowStock
            viewModel.filterList(showLowStockOnly)
        }

        binding.fabSend.setOnClickListener {
            viewModel.prepareWhatsAppMessage(args.pharmacyName, args.pharmacyPhoneNumber)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state is UiState.Loading
            binding.emptyStateLayout.isVisible = state is UiState.Success && state.data.isEmpty()
            binding.recyclerViewMedications.isVisible = state is UiState.Success && state.data.isNotEmpty()

            if (state is UiState.Success) {
                adapter.submitList(state.data) {
                    // Garante que o FAB apareça/desapareça corretamente após a lista ser atualizada
                    updateFabVisibility()
                }
            }
        }

        viewModel.whatsAppEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { navAction ->
                when(navAction) {
                    is WhatsAppNavigation.SendMessage -> sendWhatsAppMessage(navAction.phoneNumber, navAction.message)
                }
            }
        }

        // Observa a lista para mostrar/esconder o FAB de envio
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                updateFabVisibility()
            }
        })
    }

    private fun updateFabVisibility() {
        val hasSelection = adapter.currentList.any { it.isSelected }
        if (hasSelection) binding.fabSend.show() else binding.fabSend.hide()
    }

    private fun sendWhatsAppMessage(phone: String, message: String) {
        val isWhatsappInstalled = isPackageInstalled("com.whatsapp") || isPackageInstalled("com.whatsapp.w4b")

        if (isWhatsappInstalled) {
            try {
                val encodedMessage = URLEncoder.encode(message, "UTF-8")
                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=$encodedMessage")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao preparar mensagem para o WhatsApp.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "O WhatsApp não está instalado neste dispositivo.", Toast.LENGTH_LONG).show()
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.whatsapp"))
            startActivity(browserIntent)
        }
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            requireContext().packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}