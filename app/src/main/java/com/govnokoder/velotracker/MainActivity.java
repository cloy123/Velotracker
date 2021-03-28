package com.govnokoder.velotracker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.govnokoder.velotracker.BL.Controller.TrainingController;
import com.govnokoder.velotracker.BL.ParcelableTraining;
import com.govnokoder.velotracker.ui.main.PageStart;
import com.govnokoder.velotracker.ui.main.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity implements PageStart.onSomeEventListener {

    TabLayout tabLayout;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    ViewPager2 pager;
    private final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 111;
    private final int REQUEST_PERMISSION_READ_PHONE_STATE = 333;

    boolean gpsEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pager = (ViewPager2)findViewById(R.id.view_pager);
        FragmentStateAdapter pageAdapter = new ViewPagerAdapter(this);
        pager.setAdapter(pageAdapter);

        tabLayout = findViewById(R.id.tabs);

        TabLayoutMediator tabLayoutMediator= new TabLayoutMediator(tabLayout, pager, new TabLayoutMediator.TabConfigurationStrategy(){
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText(getText(R.string.start));
                        break;
                    case 1:
                        tab.setText(getText(R.string.history));
                        break;
                    case 2:
                        tab.setText(getText(R.string.statistic));
                        break;
                }
            }
        });
        tabLayoutMediator.attach();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //Установите слушателя, который будет получать уведомления при выборе пункта меню
                switch (item.getItemId()){
                    case R.id.nav_start:
                        tabLayout.selectTab(tabLayout.getTabAt(0));
                        break;
                    case R.id.nav_history:
                        tabLayout.selectTab(tabLayout.getTabAt(1));
                        break;
                    case R.id.nav_statistics:
                        tabLayout.selectTab(tabLayout.getTabAt(2));
                        break;
                    case R.id.nav_settings:
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        //TODO доделать настройки
                        break;
                }
                drawerLayout.closeDrawers();
                return false;
            }
        });
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.open();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = false;
        String currentPer = permissions[0];
        allowed = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if(allowed){
            startTraining();
            return;
        }
        if (currentPer.equals(Manifest.permission.ACCESS_FINE_LOCATION) && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
            dialogOpenPermissionsSettings(getString(R.string.dialog_open_sett_access_fine_location_text));
        }
        if (currentPer.equals(Manifest.permission.READ_PHONE_STATE) && !shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)){
            dialogOpenPermissionsSettings(getString(R.string.dialog_open_sett_read_phone_state_text));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                //isGPS = true; // flag maintain before get location
            }
        }
    }

    private void startActivityTraining(){
        Intent intent = new Intent(getApplicationContext(), TrainingActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void openLastTraining() {
        int position = new TrainingController(this).LoadTrainingsData().size() - 1;
        if(position >= 0) {
            tabLayout.selectTab(tabLayout.getTabAt(1));
            Intent intent = new Intent(this, LookTraining.class);
            intent.putExtra("INDEX", position);
            startActivity(intent);
        }
    }

    @Override
    public void openStatistic() {
        tabLayout.selectTab(tabLayout.getTabAt(2));
    }

    private void dialogOpenPermissionsSettings(String text){
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        ConstraintLayout cl  = (ConstraintLayout)getLayoutInflater().inflate(R.layout.dialog_open_settings, null);
        TextView textView = (TextView) cl.getViewById(R.id.textView);
        textView.setText(text);
        cl.getViewById(R.id.openSettB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())));
                dialog.dismiss();
            }
        });
        dialog.setView(cl);
        dialog.show();
    }

    private void dialogBatteryOptimizationSettings(){
        //если таких настроек не существует, они создаются
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.settings_checkboxes), MODE_PRIVATE);
        if(sharedPreferences.contains(getString(R.string.dialog_battery_optimization_settings_checkbox_key))){
            if(sharedPreferences.getBoolean(getString(R.string.dialog_battery_optimization_settings_checkbox_key), false)) {
                //если стоит больше не показывать
                startActivityTraining();
                return;
            }
        }

        final boolean[] isInSettings = {false};
        //добавляю настройку и потом вызываю диалог
        sharedPreferences.edit().putBoolean(getString(R.string.dialog_battery_optimization_settings_checkbox_key), false).apply();

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        ConstraintLayout cl  = (ConstraintLayout)getLayoutInflater().inflate(R.layout.dialog_open_settings_with_checkbox, null);
        TextView textView = (TextView) cl.getViewById(R.id.textView);
        textView.setText(getString(R.string.dialog_battery_optimization_text));
        cl.getViewById(R.id.openSettB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInSettings[0] = true;
                startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                dialog.dismiss();
            }
        });
        AppCompatCheckBox compatCheckBox = (AppCompatCheckBox) cl.getViewById(R.id.checkbox);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(compatCheckBox.isChecked()){
                    SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.settings_checkboxes), MODE_PRIVATE);
                    sharedPreferences.edit().putBoolean(getString(R.string.dialog_battery_optimization_settings_checkbox_key), true).apply();
                }
                if(!isInSettings[0]) {
                    startActivityTraining();
                    return;
                }
            }
        });
        dialog.setView(cl);
        dialog.show();
    }

    @Override
    public void startTraining() {
        //тут переопределяю метод из интерфейса который в фрагменте
        boolean accessFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean readPhoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(!accessFineLocation){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_ACCESS_FINE_LOCATION);
            return;
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            if (!readPhoneState) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_PERMISSION_READ_PHONE_STATE);
                return;
            }
        }

        if(!gpsEnabled) {
            GpsUtils gpsUtils = new GpsUtils(this);
            gpsUtils.turnGPSOn(new GpsUtils.onGpsListener() {
                @Override
                public void gpsStatus(boolean isGPSEnable) {
                    if(isGPSEnable){
                        startTraining();
                    }
                }
            });
        }

        if(!gpsEnabled){
            return;
        }
        dialogBatteryOptimizationSettings();
    }
}