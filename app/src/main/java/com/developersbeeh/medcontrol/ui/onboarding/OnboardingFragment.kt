// src/main/java/com/developersbeeh/medcontrol/ui/onboarding/OnboardingFragment.kt
package com.developersbeeh.medcontrol.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.databinding.FragmentOnboardingBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OnboardingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.pages.observe(viewLifecycleOwner) { pages ->
            if (pages.isNullOrEmpty()) return@observe

            binding.viewPager.adapter = OnboardingAdapter(this, pages)

            // ✅ ANIMAÇÃO DE TRANSIÇÃO ADICIONADA
            // Adiciona a animação de profundidade ao passar as páginas.
            binding.viewPager.setPageTransformer(DepthPageTransformer())

            TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ ->
            }.attach()

            binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == pages.size - 1) {
                        binding.buttonNext.text = "Começar a Usar"
                        binding.textViewSkip.visibility = View.INVISIBLE
                    } else {
                        binding.buttonNext.text = "Próximo"
                        binding.textViewSkip.visibility = View.VISIBLE
                    }
                }
            })

            binding.buttonNext.setOnClickListener {
                if (binding.viewPager.currentItem < pages.size - 1) {
                    binding.viewPager.currentItem += 1
                } else {
                    finishOnboarding()
                }
            }

            binding.textViewSkip.setOnClickListener {
                finishOnboarding()
            }
        }
    }

    private fun finishOnboarding() {
        val userPreferences = UserPreferences(requireContext())
        userPreferences.setOnboardingCompleted(true)
        findNavController().navigate(R.id.action_onboardingFragment_to_roleSelectionFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}