package com.coursework.velotracker.ui.main

import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {


    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        TODO()
//        switch (position) {
//            case 0:
//            return(PageStart.newInstance(position));
//            case 1:
//            return(PageHistory.newInstance(position));
//            case 2:
//            return(PageStatistics.newInstance(position));
//        }
//        return(PageStart.newInstance(position));
    }

    override fun getItemId(position: Int): Long {
        return 3
    }


}