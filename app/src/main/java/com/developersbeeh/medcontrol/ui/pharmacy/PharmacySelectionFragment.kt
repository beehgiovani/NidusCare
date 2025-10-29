package com.developersbeeh.medcontrol.ui.pharmacy

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.remote.Place
import com.developersbeeh.medcontrol.data.remote.PlaceDetails
import com.developersbeeh.medcontrol.databinding.FragmentPharmacySelectionBinding
import com.developersbeeh.medcontrol.ui.common.LoadingDialogFragment
import com.developersbeeh.medcontrol.util.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PharmacySelectionFragment : Fragment() {

    private var _binding: FragmentPharmacySelectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PharmacySelectionViewModel by viewModels()
    private lateinit var adapter: PharmacyAdapter
    private var loadingDialog: LoadingDialogFragment? = null

    // ✅ NOVO: Armazenar localização do usuário
    private var userLat: Double? = null
    private var userLng: Double? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        viewModel.onPermissionResult(requireContext(), isGranted)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPharmacySelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Buscar Farmácias"

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    private fun setupRecyclerView() {
        adapter = PharmacyAdapter(
            onPharmacyClick = { place ->
                viewModel.onPharmacySelected(place)
            },
            onOptionsClick = { place, anchorView ->
                viewModel.fetchDetailsForOptions(place)
            },
            userLat = userLat, // ✅ NOVO: Passar localização para adapter
            userLng = userLng   // ✅ NOVO: Passar localização para adapter
        )
        binding.recyclerViewPharmacies.adapter = adapter
        binding.recyclerViewPharmacies.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupListeners() {
        binding.chipGroupDistance.setOnCheckedChangeListener { _, checkedId ->
            val radiusInMeters = when (checkedId) {
                R.id.chip_2km -> 2000
                R.id.chip_5km -> 5000
                R.id.chip_10km -> 10000 // ✅ NOVO: Opção de 10km
                else -> 1000 // Padrão 1km
            }
            viewModel.setDistanceFilter(radiusInMeters)
        }
        
        binding.layoutError.btnRetry.setOnClickListener {
            requestLocationPermission()
        }
    }

    private fun observeViewModel() {
        // ✅ NOVO: Observar localização do usuário
        viewModel.userLocation.observe(viewLifecycleOwner) { location ->
            location?.let {
                userLat = it.first
                userLng = it.second
                
                // Recriar adapter com nova localização
                adapter = PharmacyAdapter(
                    onPharmacyClick = { place ->
                        viewModel.onPharmacySelected(place)
                    },
                    onOptionsClick = { place, anchorView ->
                        viewModel.fetchDetailsForOptions(place)
                    },
                    userLat = userLat,
                    userLng = userLng
                )
                binding.recyclerViewPharmacies.adapter = adapter
            }
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state is UiState.Loading
            binding.recyclerViewPharmacies.isVisible = state is UiState.Success && state.data.isNotEmpty()
            binding.layoutError.root.isVisible = state is UiState.Error || (state is UiState.Success && state.data.isEmpty())

            when (state) {
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        binding.layoutError.tvErrorMessage.text = "Nenhuma farmácia encontrada neste raio. Tente aumentar a distância."
                    } else {
                        adapter.submitList(state.data)
                        
                        // ✅ NOVO: Mostrar banner de propaganda após lista carregar
                        showAdBannerIfNeeded()
                    }
                }
                is UiState.Error -> {
                    binding.layoutError.tvErrorMessage.text = state.message
                }
                is UiState.Loading -> { }
            }
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { navEvent ->
                when (navEvent) {
                    is NavigationEvent.ToMedicationSelection -> {
                        val action = PharmacySelectionFragmentDirections
                            .actionPharmacySelectionFragmentToPharmacyMedicationSelectionFragment(
                                pharmacyName = navEvent.pharmacyDetails.name,
                                pharmacyPhoneNumber = navEvent.pharmacyDetails.phoneNumber ?: ""
                            )
                        findNavController().navigate(action)
                    }
                }
            }
        }

        viewModel.placeDetailsForOptions.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { state ->
                when(state) {
                    is UiState.Loading -> {
                        loadingDialog = LoadingDialogFragment.newInstance("Buscando detalhes...")
                        loadingDialog?.show(childFragmentManager, "details_loading")
                    }
                    is UiState.Success -> {
                        loadingDialog?.dismiss()
                        showPharmacyOptions(state.data.first, state.data.second)
                    }
                    is UiState.Error -> {
                        loadingDialog?.dismiss()
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showPharmacyOptions(details: PlaceDetails, originalPlace: Place) {
        val anchorView = findAnchorViewForPlace(details.placeId) ?: return
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.pharmacy_item_menu, popup.menu)

        popup.menu.findItem(R.id.action_call_pharmacy).isVisible = !details.phoneNumber.isNullOrBlank()

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_call_pharmacy -> {
                    details.phoneNumber?.let { dialPhoneNumber(it) }
                    true
                }
                R.id.action_select_pharmacy -> {
                    viewModel.onPharmacySelected(originalPlace)
                    true
                }
                R.id.action_navigate -> { // ✅ NOVO: Navegar para farmácia
                    navigateToPharmacy(originalPlace)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun findAnchorViewForPlace(placeId: String): View? {
        for (i in 0 until adapter.itemCount) {
            if (adapter.currentList[i].placeId == placeId) {
                val viewHolder = binding.recyclerViewPharmacies.findViewHolderForAdapterPosition(i)
                return viewHolder?.itemView?.findViewById(R.id.buttonMenu)
            }
        }
        return null
    }

    private fun dialPhoneNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(context, "Nenhum aplicativo de telefone encontrado.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * ✅ NOVO: Navegar para farmácia usando Google Maps/Waze
     */
    private fun navigateToPharmacy(place: Place) {
        val lat = place.geometry.location.lat
        val lng = place.geometry.location.lng
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${place.name})")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        } else {
            // Fallback para navegador
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng"))
            startActivity(browserIntent)
        }
    }

    /**
     * ✅ NOVO: Mostrar banner de propaganda discreto (apenas para usuários não-premium)
     */
    private fun showAdBannerIfNeeded() {
        // Verificar se usuário é premium (implementar verificação real)
        val isPremium = false // TODO: Implementar verificação real
        
        if (!isPremium) {
            binding.adBanner.visibility = View.VISIBLE
            binding.adBanner.setOnClickListener {
                // Ação ao clicar no anúncio (ex: abrir upgrade premium)
                Toast.makeText(context, "Upgrade para Premium e remova anúncios!", Toast.LENGTH_SHORT).show()
            }
        } else {
            binding.adBanner.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

