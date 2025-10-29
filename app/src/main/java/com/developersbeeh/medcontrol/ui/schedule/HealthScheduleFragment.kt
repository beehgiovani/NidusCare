// src/main/java/com/developersbeeh/medcontrol/ui/schedule/HealthScheduleFragment.kt
package com.developersbeeh.medcontrol.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.AgendamentoSaude
import com.developersbeeh.medcontrol.data.model.PermissaoTipo
import com.developersbeeh.medcontrol.databinding.FragmentHealthScheduleBinding
import com.developersbeeh.medcontrol.databinding.LayoutEmptyStateBinding
import com.developersbeeh.medcontrol.util.UiState
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HealthScheduleFragment : Fragment() {

    private var _binding: FragmentHealthScheduleBinding? = null
    private val binding get() = _binding!!

    private lateinit var emptyStateBinding: LayoutEmptyStateBinding

    private val viewModel: HealthScheduleViewModel by viewModels()
    private val args: HealthScheduleFragmentArgs by navArgs()
    private val adapter by lazy {
        HealthScheduleAdapter(
            onItemClick = { agendamento ->
                if (userPreferences.temPermissao(PermissaoTipo.EDITAR_AGENDAMENTOS)) {
                    viewModel.onScheduleSelected(agendamento)
                }
            },
            onMenuClick = { agendamento, anchorView ->
                if (userPreferences.temPermissao(PermissaoTipo.EDITAR_AGENDAMENTOS)) {
                    showScheduleMenu(agendamento, anchorView)
                }
            }
        )
    }
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyStateBinding = LayoutEmptyStateBinding.bind(binding.emptyStateLayout.root)
        userPreferences = UserPreferences(requireContext())
        viewModel.initialize(args.dependentId, args.dependentName)

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Agenda de ${args.dependentName}"

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        binding.fabAddSchedule.isVisible = userPreferences.temPermissao(PermissaoTipo.ADICIONAR_AGENDAMENTOS)

        showGuideIfFirstTime()
    }

    private fun setupListeners() {
        binding.fabAddSchedule.setOnClickListener {
            viewModel.onAddScheduleClicked()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewSchedule.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.schedules.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state is UiState.Loading
            binding.recyclerViewSchedule.isVisible = state is UiState.Success && state.data.isNotEmpty()
            binding.emptyStateLayout.root.isVisible = state !is UiState.Loading && (state is UiState.Error || (state is UiState.Success && state.data.isEmpty()))

            when (state) {
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        setupEmptyState()
                    } else {
                        adapter.submitList(state.data)
                    }
                }
                is UiState.Error -> {
                    emptyStateBinding.lottieAnimationView.setAnimation(R.raw.error_animation)
                    emptyStateBinding.tvEmptyTitle.text = "Ocorreu um Erro"
                    emptyStateBinding.tvEmptyMessage.text = state.message
                    emptyStateBinding.btnEmptyAction.text = "Tentar Novamente"
                    emptyStateBinding.btnEmptyAction.setOnClickListener { viewModel.initialize(args.dependentId, args.dependentName) }
                }
                else -> {}
            }
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { action ->
                findNavController().navigate(action)
            }
        }

        viewModel.actionFeedback.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupEmptyState() {
        emptyStateBinding.lottieAnimationView.setAnimation(R.raw.empty_list)
        emptyStateBinding.tvEmptyTitle.text = "Nenhum Agendamento"
        emptyStateBinding.tvEmptyMessage.text = "Adicione suas próximas consultas, exames e outros compromissos de saúde."

        val canAdd = userPreferences.temPermissao(PermissaoTipo.ADICIONAR_AGENDAMENTOS)
        emptyStateBinding.btnEmptyAction.isVisible = canAdd
        if (canAdd) {
            emptyStateBinding.btnEmptyAction.text = "Novo Agendamento"
            emptyStateBinding.btnEmptyAction.setOnClickListener {
                binding.fabAddSchedule.performClick()
            }
        }
    }

    private fun showGuideIfFirstTime() {
        if (!userPreferences.hasSeenScheduleGuide() && userPreferences.temPermissao(PermissaoTipo.ADICIONAR_AGENDAMENTOS)) {
            binding.root.doOnPreDraw {
                val activity = activity ?: return@doOnPreDraw
                TapTarget.forView(binding.fabAddSchedule, "Novo Agendamento", "Clique aqui para adicionar consultas, exames ou outros compromissos de saúde na agenda.")
                    .outerCircleColor(R.color.md_theme_primary)
                    .targetCircleColor(R.color.white)
                    .titleTextColor(R.color.white)
                    .descriptionTextColor(R.color.white)
                    .cancelable(true)
                    .tintTarget(true)
                    .let {
                        TapTargetSequence(activity)
                            .targets(it)
                            .listener(object : TapTargetSequence.Listener {
                                override fun onSequenceFinish() { userPreferences.setScheduleGuideSeen(true) }
                                override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {}
                                override fun onSequenceCanceled(lastTarget: TapTarget?) { userPreferences.setScheduleGuideSeen(true) }
                            }).start()
                    }
            }
        }
    }

    private fun showScheduleMenu(agendamento: AgendamentoSaude, anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.schedule_item_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.action_edit_schedule -> {
                    viewModel.onScheduleSelected(agendamento)
                    true
                }
                R.id.action_delete_schedule -> {
                    showDeleteConfirmationDialog(agendamento)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showDeleteConfirmationDialog(agendamento: AgendamentoSaude) {
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle("Excluir Agendamento?")
            .setMessage("Tem certeza que deseja excluir '${agendamento.titulo}'?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.deleteSchedule(agendamento)
            }
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}