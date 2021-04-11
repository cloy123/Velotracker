package com.coursework.velotracker

import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

import androidx.viewpager2.widget.ViewPager2
import com.coursework.velotracker.ui.training.ViewPagerAdapter

import com.google.android.material.tabs.TabLayout




class TrainingActivity: AppCompatActivity() {

    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager2
    lateinit var viewPagerAdapter: ViewPagerAdapter
    lateinit var progressBar: ProgressBar
}