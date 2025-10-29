package com.developersbeeh.medcontrol.ui.archive

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ArchivedTabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3 // Teremos 3 abas

    override fun createFragment(position: Int): Fragment {
        return ArchivedListFragment.newInstance(
            when (position) {
                 0 -> ArchivedListFragment.Companion.FilterType.FINISHED
                 1 -> ArchivedListFragment.Companion.FilterType.ZERO_STOCK
                else -> ArchivedListFragment.Companion.FilterType.EXPIRED
            }
        )
    }
}