package com.govnokoder.velotracker;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.govnokoder.velotracker.BL.ParcelableTraining;
import com.govnokoder.velotracker.messages.SharedViewModel;
import com.govnokoder.velotracker.services.LocationService;
import com.govnokoder.velotracker.ui.training.PageMap;
import com.govnokoder.velotracker.ui.training.ViewPagerAdapterTr;

public class TrainingActivity extends AppCompatActivity implements PageMap.onSomeEventListener {

    TabLayout tabLayout;
    ViewPager2 viewPager;
    ViewPagerAdapterTr viewPagerAdapterTr;
    ProgressBar progressBar;

    private MyReceiver myReceiver;

    private LocationService mService = null;
    private boolean isStart = false;

    private boolean mBound = false;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        viewPager = findViewById(R.id.view_pager);
        viewPagerAdapterTr = new ViewPagerAdapterTr(this);
        viewPager.setAdapter(viewPagerAdapterTr);

        tabLayout = findViewById(R.id.tabs);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("1");
                        break;
                    case 1:
                        tab.setText("2");
                }
            }
        });
        tabLayoutMediator.attach();
        myReceiver = new MyReceiver();
        startForegroundService(new Intent(this, LocationService.class));
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, LocationService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
    }

    private void sendParcelableTraining(ParcelableTraining parcelableTraining){
        SharedViewModel model = new ViewModelProvider(this).get(SharedViewModel.class);
        model.sendMessage(parcelableTraining);
    }

    @Override
    public void onPauseTraining() {
        mService.onPause();
    }

    @Override
    public void onStopTraining(boolean isSave) {
        mService.onStop(isSave);
    }

    @Override
    public void onResumeTraining() {
        mService.onResume();
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ParcelableTraining parcelableTraining = intent.getParcelableExtra(LocationService.EXTRA_PARCELABLE_TRAINING);
            if (parcelableTraining != null) {
                if(!isStart){
                    progressBar.setVisibility(View.INVISIBLE);
                }
                sendParcelableTraining(parcelableTraining);
            }
        }
    }


}