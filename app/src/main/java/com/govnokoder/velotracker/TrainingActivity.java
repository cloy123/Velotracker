package com.govnokoder.velotracker;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.govnokoder.velotracker.BL.CurrentTraining;
import com.govnokoder.velotracker.ui.training.PageMap;
import com.govnokoder.velotracker.ui.training.PageStat;
import com.govnokoder.velotracker.ui.training.ViewPagerAdapterTr;

import java.util.List;

public class TrainingActivity extends AppCompatActivity{// implements PageMap.OnFragmentSendDataListener {

    TabLayout tabLayout;
    ViewPager2 viewPager;
    ViewPagerAdapterTr viewPagerAdapterTr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        viewPager = findViewById(R.id.view_pager);
        viewPagerAdapterTr = new ViewPagerAdapterTr(this);
        viewPager.setAdapter(viewPagerAdapterTr);

        tabLayout = findViewById(R.id.tabs);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("Карта");
                        break;
                    case 1:
                        tab.setText("stat");
                }
            }
        });
        tabLayoutMediator.attach();
    }
}