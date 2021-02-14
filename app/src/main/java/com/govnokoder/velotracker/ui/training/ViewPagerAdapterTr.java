package com.govnokoder.velotracker.ui.training;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.govnokoder.velotracker.R;
import com.govnokoder.velotracker.ui.main.PageStart;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class ViewPagerAdapterTr extends FragmentStateAdapter {

    public ViewPagerAdapterTr(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position)
        {
            case 0:
                return(PageMap.newInstance(position));
            case 1:
                return(PageStat.newInstance(position));
        }
        return(PageMap.newInstance(position));
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}