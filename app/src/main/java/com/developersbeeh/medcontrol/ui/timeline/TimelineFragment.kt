package com.developersbeeh.medcontrol.ui.timeline

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.developersbeeh.medcontrol.NavGraphDirections
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.databinding.FragmentTimelineBinding
import com.developersbeeh.medcontrol.databinding.LayoutEmptyStateBinding
import com.developersbeeh.medcontrol.databinding.LayoutErrorStateBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TimelineFragment : Fragment() {

    private lateinit var userPreferences: UserPreferences
    private var _binding: FragmentTimelineBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TimelineViewModel by viewModels()
    private val args: TimelineFragmentArgs by navArgs()
    private lateinit var adapter: TimelineAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initialize(args.dependentId)
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.timeline_title, args.dependentName)
        userPreferences = UserPreferences(requireContext())
        setupRecyclerView()
        setupFilters()
        setupRefreshListener()
        observeViewModel()
        setupAds()
    }

    private fun setupRecyclerView() {
        adapter = TimelineAdapter()
        binding.recyclerViewTimeline.adapter = adapter
        binding.recyclerViewTimeline.layoutManager = LinearLayoutManager(requireContext())

        adapter.addLoadStateListener { loadState ->
            val refreshState = loadState.refresh

            binding.shimmerLayout.isVisible = refreshState is LoadState.Loading && adapter.itemCount == 0
            binding.swipeRefreshLayout.isRefreshing = refreshState is LoadState.Loading

            val hasContent = refreshState is LoadState.NotLoading && adapter.itemCount > 0
            binding.recyclerViewTimeline.isVisible = hasContent

            val isEmpty = refreshState is LoadState.NotLoading && adapter.itemCount == 0
            binding.emptyStateLayout.root.isVisible = isEmpty
            if (isEmpty) {
                setupEmptyState()
            }

            val hasError = refreshState is LoadState.Error
            binding.errorStateLayout.root.isVisible = hasError
            if (hasError) {
                LayoutErrorStateBinding.bind(binding.errorStateLayout.root).tvErrorMessage.text = refreshState.error.localizedMessage
            }
        }
    }

    private fun setupRefreshListener() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            adapter.refresh()
        }
        binding.errorStateLayout.btnRetry.setOnClickListener {
            adapter.retry()
        }
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

    private fun setupFilters() {
        binding.chipFilterAll.text = getString(R.string.timeline_filter_all)
        binding.chipFilterDoses.text = getString(R.string.timeline_filter_doses)
        binding.chipFilterNotes.text = getString(R.string.timeline_filter_notes)
        binding.chipFilterOtherActivities.text = getString(R.string.timeline_filter_wellbeing)
        binding.chipFilterInsights.text = getString(R.string.timeline_filter_insights)

        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            val filter = if (checkedIds.isEmpty()) {
                group.check(R.id.chipFilterAll)
                TimelineFilter.ALL
            } else {
                when (checkedIds.first()) {
                    R.id.chipFilterDoses -> TimelineFilter.DOSE
                    R.id.chipFilterNotes -> TimelineFilter.NOTE
                    R.id.chipFilterOtherActivities -> TimelineFilter.WELLBEING
                    R.id.chipFilterInsights -> TimelineFilter.INSIGHT
                    else -> TimelineFilter.ALL
                }
            }
            viewModel.setFilter(filter)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.timelinePagerFlow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    private fun setupEmptyState() {
        val emptyBinding = LayoutEmptyStateBinding.bind(binding.emptyStateLayout.root)
        emptyBinding.lottieAnimationView.setAnimation(R.raw.empty_list)
        emptyBinding.tvEmptyTitle.text = getString(R.string.empty_state_timeline_title)
        emptyBinding.tvEmptyMessage.text = getString(R.string.empty_state_timeline_subtitle)
        emptyBinding.btnEmptyAction.text = getString(R.string.empty_state_timeline_button)
        emptyBinding.btnEmptyAction.setOnClickListener {
            val action = NavGraphDirections.actionGlobalToWellbeingDiaryFragment(args.dependentId, args.dependentName)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}