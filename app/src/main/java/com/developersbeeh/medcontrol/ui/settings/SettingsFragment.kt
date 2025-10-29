package com.developersbeeh.medcontrol.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.Usuario
import com.developersbeeh.medcontrol.databinding.FragmentSettingsBinding
import com.developersbeeh.medcontrol.ui.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels() // Obtém o ViewModel da Activity

    private lateinit var settingsAdapter: SettingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                settingsAdapter.submitList(getSettingsList(it))
            }
        }
        // O observador de logout foi movido para a MainActivity
    }

    private fun setupRecyclerView() {
        settingsAdapter = SettingsAdapter { settingItem ->
            when(settingItem.id) {
                1 -> findNavController().navigate(R.id.action_settingsFragment_to_notificationSettingsFragment)
                2 -> findNavController().navigate(R.id.action_settingsFragment_to_privacySettingsFragment)
                3 -> findNavController().navigate(R.id.action_settingsFragment_to_themeSettingsFragment)
                4 -> findNavController().navigate(R.id.action_settingsFragment_to_aboutFragment)
                6 -> openPrivacyPolicy()
                5 -> showLogoutConfirmationDialog()
                7 -> findNavController().navigate(R.id.action_global_to_premiumPlansFragment)
                8 -> findNavController().navigate(R.id.action_global_to_manageFamilyFragment)
            }
        }
        binding.recyclerViewSettings.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = settingsAdapter
        }
    }

    private fun getSettingsList(user: Usuario): List<SettingItem> {
        val list = mutableListOf<SettingItem>()
        val userPreferences = UserPreferences(requireContext())
        if (!user.premium) {
            list.add(SettingItem(7, R.drawable.ic_premium_quality, "Fazer Upgrade para Premium", "Dependentes ilimitados, IA e mais"))
        }
        if (!user.familyId.isNullOrBlank()) {
            list.add(SettingItem(8, R.drawable.ic_family, "Gerenciar Família", "Convide e remova membros do seu plano"))
        }
        list.addAll(listOf(
            SettingItem(1, R.drawable.ic_alarm, "Notificações", "Gerencie os lembretes e alertas do app"),
            SettingItem(2, R.drawable.ic_lock, "Privacidade", "Gerencie suas preferências de dados"),
            SettingItem(3, R.drawable.ic_settings, "Tema", "Escolha a aparência do aplicativo"),
            SettingItem(4, R.drawable.ic_notes, "Sobre o App", "Veja informações sobre o MedControl"),
            SettingItem(6, R.drawable.ic_lock, "Política de Privacidade", "Leia nossos termos de uso de dados"),
            SettingItem(5, R.drawable.ic_unlink, "Sair da Conta", "Desconecte-se do seu perfil")
        ))
        return list
    }

    private fun openPrivacyPolicy() {
        val url = "https://sites.google.com/view/politicamedcontrol/in%C3%ADcio"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = url.toUri()
        startActivity(intent)
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle("Sair da Conta")
            .setMessage("Tem certeza que deseja sair da sua conta?")
            .setPositiveButton("Sair") { dialog, _ ->
                mainViewModel.onLogoutRequest() // Chama a função no ViewModel compartilhado
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}