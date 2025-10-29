package com.developersbeeh.medcontrol.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.FragmentThemeSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThemeSettingsFragment : Fragment() {

    private var _binding: FragmentThemeSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ThemeSettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThemeSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCurrentTheme()
        setupThemeSelection()
    }

    private fun setupCurrentTheme() {
        when (viewModel.getCurrentTheme()) {
            "light" -> binding.radioGroupTheme.check(R.id.radioButtonLight)
            "dark" -> binding.radioGroupTheme.check(R.id.radioButtonDark)
            else -> binding.radioGroupTheme.check(R.id.radioButtonSystem)
        }
    }

    private fun setupThemeSelection() {
        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonLight -> viewModel.setTheme("light")
                R.id.radioButtonDark -> viewModel.setTheme("dark")
                R.id.radioButtonSystem -> viewModel.setTheme("auto")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}