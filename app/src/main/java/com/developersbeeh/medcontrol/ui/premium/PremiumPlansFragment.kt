package com.developersbeeh.medcontrol.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.ProductDetails
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.FragmentPremiumPlansBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PremiumPlansFragment : Fragment() {

    private var _binding: FragmentPremiumPlansBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PremiumPlansViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPremiumPlansBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        binding.contentLayout.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        binding.buttonRetry.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.errorLayout.visibility = View.GONE
            viewModel.retryConnection()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.products.collectLatest { productData ->
                binding.progressBar.visibility = View.GONE

                if (productData == null || (productData.monthly == null && productData.annual == null)) {
                    binding.errorLayout.visibility = View.VISIBLE
                    binding.contentLayout.visibility = View.GONE
                } else {
                    binding.errorLayout.visibility = View.GONE
                    binding.contentLayout.visibility = View.VISIBLE
                    updateUiWithProductDetails(productData)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.purchaseFeedback.collectLatest { event ->
                event?.getContentIfNotHandled()?.let { message ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateUiWithProductDetails(products: SubscriptionProducts) {
        // --- LÓGICA PARA O CARD INDIVIDUAL ---
        fun updateIndividualSelection(details: ProductDetails?) {
            if (details == null) {
                binding.textViewPrice.text = "Indisponível"
                binding.buttonSubscribe.isEnabled = false
                return
            }
            val priceInfo = details.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()
            val period = if (details.productId == "premium_mes") "/ mês" else "/ ano"
            binding.textViewPrice.text = "${priceInfo?.formattedPrice ?: "N/A"} $period"
            binding.buttonSubscribe.isEnabled = true
            binding.buttonSubscribe.setOnClickListener { viewModel.launchPurchaseFlow(requireActivity(), details) }
        }

        binding.chipMonthly.visibility = if (products.monthly != null) View.VISIBLE else View.GONE
        binding.chipAnnual.visibility = if (products.annual != null) View.VISIBLE else View.GONE

        binding.chipGroupPlanSelection.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.contains(R.id.chipMonthly)) {
                updateIndividualSelection(products.monthly)
            } else if (checkedIds.contains(R.id.chipAnnual)) {
                updateIndividualSelection(products.annual)
            }
        }

        // Garante que uma opção seja selecionada por padrão
        if (binding.chipGroupPlanSelection.checkedChipId == View.NO_ID) {
            if (products.monthly != null) {
                binding.chipGroupPlanSelection.check(R.id.chipMonthly)
            } else if (products.annual != null) {
                binding.chipGroupPlanSelection.check(R.id.chipAnnual)
            }
        } else {
            // Garante que a UI seja atualizada com a seleção padrão inicial
            if (binding.chipGroupPlanSelection.checkedChipId == R.id.chipMonthly) {
                updateIndividualSelection(products.monthly)
            } else {
                updateIndividualSelection(products.annual)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}