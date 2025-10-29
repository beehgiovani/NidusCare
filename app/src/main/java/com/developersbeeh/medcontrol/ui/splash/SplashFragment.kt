package com.developersbeeh.medcontrol.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.FragmentSplashBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.splash_logo_animation)
        binding.imageViewLogo.startAnimation(animation)

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.decideNextScreen()
    }

    private fun observeViewModel() {
        viewModel.destination.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { destination ->
                when (destination) {
                    is SplashDestination.Onboarding -> findNavController().navigate(R.id.action_splashFragment_to_onboardingFragment)
                    is SplashDestination.RoleSelection -> findNavController().navigate(R.id.action_splashFragment_to_roleSelectionFragment)
                    is SplashDestination.CaregiverDashboard -> findNavController().navigate(R.id.action_splashFragment_to_caregiverDashboardFragment)
                    is SplashDestination.DependentDashboard -> {
                        val action = SplashFragmentDirections.actionSplashFragmentToDashboardDependenteFragment(
                            dependentId = destination.dependentId,
                            dependentName = destination.dependentName
                        )
                        findNavController().navigate(action)
                    }
                    is SplashDestination.CompleteProfile -> {
                        findNavController().navigate(R.id.action_global_to_completeProfileFragment)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}