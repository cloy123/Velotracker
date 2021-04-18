package com.coursework.velotracker.ui.training

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> PageMap.newInstance(position)
            1 -> PageStat.newInstance(position)
            else -> PageMap.newInstance(position)
        }
    }

    override fun getItemCount(): Int {
        return 2
    }
}