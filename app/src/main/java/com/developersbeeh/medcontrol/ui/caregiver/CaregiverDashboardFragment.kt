package com.developersbeeh.medcontrol.ui.caregiver

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.NavGraphDirections
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.databinding.FragmentCaregiverDashboardBinding
import com.developersbeeh.medcontrol.util.UiState
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialElevationScale
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CaregiverDashboardFragment : Fragment() {

    private var _binding: FragmentCaregiverDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CaregiverDashboardViewModel by viewModels()
    private lateinit var userPreferences: UserPreferences

    private lateinit var dependentsAdapter: CaregiverDashboardAdapter
    private lateinit var receivedInviteAdapter: DashboardInviteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaregiverDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreferences = UserPreferences(requireContext())

        exitTransition = MaterialElevationScale(false).apply {
            duration = 300
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = 300
        }

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        setupAds()
    }

    private fun setupClickListeners() {
        binding.fabAddDependent.setOnClickListener {
            findNavController().navigate(NavGraphDirections.actionGlobalToAddEditDependentFragment())
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.forceReload()
        }
    }

    private fun setupRecyclerView() {
        dependentsAdapter = CaregiverDashboardAdapter(
            onDependentClick = { dependentWithStatus, cardView ->
                viewModel.onDependentSelected(dependentWithStatus, cardView)
            },
            onViewInsightsClick = { dependentWithStatus ->
                val action = CaregiverDashboardFragmentDirections
                    .actionCaregiverDashboardFragmentToInsightsFragment(
                        dependentId = dependentWithStatus.dependente.id,
                        dependentName = dependentWithStatus.dependente.nome
                    )
                findNavController().navigate(action)
            }
        )
        binding.stateLayout.findViewById<RecyclerView>(R.id.recyclerViewDependents).adapter = dependentsAdapter
        binding.stateLayout.findViewById<RecyclerView>(R.id.recyclerViewDependents).layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down)


        receivedInviteAdapter = DashboardInviteAdapter(
            onAcceptClick = { invite -> viewModel.acceptInvite(invite) },
            onDeclineClick = { invite -> viewModel.declineInvite(invite) },
            onCheckLaterClick = { invite -> viewModel.dismissInviteFromUI(invite) }
        )
        binding.stateLayout.findViewById<RecyclerView>(R.id.recyclerViewPendingInvites).adapter = receivedInviteAdapter
    }

    private fun observeViewModel() {
        viewModel.dependentsWithStatus.observe(viewLifecycleOwner) { state ->
            binding.swipeRefreshLayout.isRefreshing = state is UiState.Loading && binding.swipeRefreshLayout.isRefreshing

            binding.stateLayout.setState(state) { it.isNotEmpty() }

            when (state) {
                is UiState.Success -> {
                    val dependents = state.data
                    dependentsAdapter.submitList(dependents)

                    if(!binding.swipeRefreshLayout.isRefreshing) {
                        binding.stateLayout.findViewById<RecyclerView>(R.id.recyclerViewDependents).scheduleLayoutAnimation()
                    }

                    if (dependents.isEmpty()) {
                        setupEmptyState()
                    }

                    view?.post {
                        if (dependents.size == 1 && !userPreferences.hasSeenDashboardGuide()) {
                            val vh =  binding.stateLayout.findViewById<RecyclerView>(R.id.recyclerViewDependents).findViewHolderForAdapterPosition(0)
                            vh?.itemView?.let { showFirstDependentGuide(it, dependents.first().dependente.nome) }
                        } else if (dependents.isEmpty() && !userPreferences.hasSeenDashboardGuide()) {
                            showEmptyStateGuide()
                        }
                    }
                }
                is UiState.Error -> {
                    binding.stateLayout.onRetry = { viewModel.forceReload() }
                }
                else -> {}
            }
        }

        viewModel.navigateToDependentDashboard.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { (dependentWithStatus, view) ->
                val extras = FragmentNavigatorExtras(view to view.transitionName)
                val action = CaregiverDashboardFragmentDirections
                    .actionCaregiverDashboardFragmentToDashboardDependenteFragment(
                        dependentId = dependentWithStatus.dependente.id,
                        dependentName = dependentWithStatus.dependente.nome
                    )
                findNavController().navigate(action, extras)
            }
        }

        viewModel.dashboardSummary.observe(viewLifecycleOwner) { summary ->
            binding.summaryCard.visibility = if (summary.totalDependents > 0) View.VISIBLE else View.GONE
            if (summary.totalDependents > 0) {
                binding.textViewSummaryDosesToday.text = "Doses hoje: ${summary.dosesTomadasHoje} de ${summary.totalDosesHoje} tomadas"
                binding.progressSummaryDoses.max = if (summary.totalDosesHoje > 0) summary.totalDosesHoje else 1
                binding.progressSummaryDoses.progress = summary.dosesTomadasHoje
                binding.textViewSummaryNextDose.text = summary.proximaDoseGeral

                binding.textViewSummaryAppointments.visibility = if (summary.compromissosHoje > 0) View.VISIBLE else View.GONE
                if (summary.compromissosHoje > 0) {
                    binding.textViewSummaryAppointments.text = "${summary.compromissosHoje} compromisso(s) na agenda hoje."
                }

                binding.textViewSummaryDosesLate.visibility = if (summary.dosesAtrasadasHoje > 0) View.VISIBLE else View.GONE
                if (summary.dosesAtrasadasHoje > 0) {
                    val plural = if (summary.dosesAtrasadasHoje > 1) "doses estão atrasadas" else "dose está atrasada"
                    binding.textViewSummaryDosesLate.text = "${summary.dosesAtrasadasHoje} $plural"
                }
            }
        }

        viewModel.pendingInvites.observe(viewLifecycleOwner) { invites ->
            binding.cardPendingInvites.visibility = if (!invites.isNullOrEmpty()) View.VISIBLE else View.GONE
            receivedInviteAdapter.submitList(invites)
        }

        viewModel.actionFeedback.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.newDependentCreatedEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                showNewDependentCredentialsDialog(it)
            }
        }
    }

    private fun setupEmptyState() {
        binding.stateLayout.setEmptyState(
            title = getString(R.string.empty_state_no_dependents_title),
            subtitle = getString(R.string.empty_state_no_dependents_subtitle)
        )
    }

    private fun showEmptyStateGuide() {
        val userPreferences = UserPreferences(requireContext())
        val target = TapTarget.forView(binding.fabAddDependent, getString(R.string.guide_start_here), getString(R.string.guide_add_first_dependent))
            .outerCircleColor(R.color.md_theme_primary)
            .targetCircleColor(R.color.white)
            .textColor(R.color.white)
            .cancelable(true)
            .tintTarget(true)

        TapTargetView.showFor(requireActivity(), target, object : TapTargetView.Listener() {
            override fun onTargetClick(view: TapTargetView) {
                super.onTargetClick(view)
                userPreferences.setDashboardGuideSeen(true)
                binding.fabAddDependent.performClick()
            }
            override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                userPreferences.setDashboardGuideSeen(true)
            }
        })
    }

    private fun showFirstDependentGuide(itemView: View, dependentName: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isAdded || activity == null) return@postDelayed
            val userPreferences = UserPreferences(requireContext())
            val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
            val targets = mutableListOf<TapTarget>()
            targets.add(
                TapTarget.forView(itemView, getString(R.string.guide_manage_dependent), getString(R.string.guide_manage_dependent_desc, dependentName))
                    .outerCircleColor(R.color.md_theme_primary)
                    .targetCircleColor(R.color.white)
                    .textColor(R.color.white)
                    .cancelable(true)
                    .tintTarget(false)
            )
            if (toolbar != null) {
                targets.add(
                    TapTarget.forToolbarNavigationIcon(toolbar, getString(R.string.guide_main_menu), getString(R.string.guide_main_menu_desc))
                        .outerCircleColor(R.color.md_theme_primary)
                        .targetCircleColor(R.color.white)
                        .textColor(R.color.white)
                        .cancelable(true)
                )
            }
            TapTargetSequence(requireActivity())
                .targets(targets)
                .listener(object : TapTargetSequence.Listener {
                    override fun onSequenceFinish() { userPreferences.setDashboardGuideSeen(true) }
                    override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {}
                    override fun onSequenceCanceled(lastTarget: TapTarget?) { userPreferences.setDashboardGuideSeen(true) }
                })
                .continueOnCancel(true)
                .start()
        }, 500)
    }

    private fun showNewDependentCredentialsDialog(info: NewDependentInfo) {
        val message = getString(R.string.dependent_credentials_message, info.name, info.code, info.password)
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.dependent_created_success))
            .setMessage(message)
            .setPositiveButton(getString(R.string.understand)) { dialog, _ -> dialog.dismiss() }
            .setNeutralButton(getString(R.string.copy)) { _, _ ->
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(getString(R.string.credentials_label), "Código: ${info.code}\nSenha: ${info.password}")
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, getString(R.string.credentials_copied), Toast.LENGTH_SHORT).show()
            }
            .create()
            .show()
    }

    private fun setupAds() {
        MobileAds.initialize(requireContext()) {}
        if (!userPreferences.isPremium()) {
            binding.adView.visibility = View.VISIBLE
            val adRequest = AdRequest.Builder().build()
            binding.adView.loadAd(adRequest)
        } else {
            binding.adView.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}