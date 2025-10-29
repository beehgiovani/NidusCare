package com.developersbeeh.medcontrol.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.developersbeeh.medcontrol.databinding.FragmentPrivacySettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrivacySettingsFragment : Fragment() {

    private var _binding: FragmentPrivacySettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PrivacySettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrivacySettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Exemplo de como observar o ViewModel e atualizar a UI
        viewModel.dataCollectionEnabled.observe(viewLifecycleOwner) {
            binding.switchDataCollection.isChecked = it
        }

        binding.switchDataCollection.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDataCollectionEnabled(isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
