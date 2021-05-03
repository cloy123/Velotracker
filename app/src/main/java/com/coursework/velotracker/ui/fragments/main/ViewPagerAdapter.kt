package com.coursework.velotracker.ui.fragments.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> PageStart.newInstance(position)
            1 -> PageHistory.newInstance(position)
            2 -> PageStatistics.newInstance(position)
            else -> PageStart.newInstance(position)
        }
    }
}