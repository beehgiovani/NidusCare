    package com.developersbeeh.medcontrol.ui.onboarding

    import android.os.Build
    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import androidx.fragment.app.Fragment
    import com.developersbeeh.medcontrol.databinding.FragmentOnboardingPageBinding

    class OnboardingPageFragment : Fragment() {

        private var _binding: FragmentOnboardingPageBinding? = null
        private val binding get() = _binding!!

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentOnboardingPageBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val page = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arguments?.getParcelable(ARG_PAGE, OnboardingPage::class.java)
            } else {
                @Suppress("DEPRECATION")
                arguments?.getParcelable(ARG_PAGE)
            }

            page?.let {
                binding.imageViewOnboarding.setImageResource(it.imageRes)
                binding.textViewTitle.text = it.title
                binding.textViewDescription.text = it.description
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

        companion object {
            private const val ARG_PAGE = "arg_page"

            fun newInstance(page: OnboardingPage): OnboardingPageFragment {
                return OnboardingPageFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_PAGE, page)
                    }
                }
            }
        }
    }