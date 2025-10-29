package com.developersbeeh.medcontrol.ui.onboarding

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingAdapter(
    fragment: Fragment,
    private val pages: List<OnboardingPage>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment = OnboardingPageFragment.newInstance(pages[position])
}