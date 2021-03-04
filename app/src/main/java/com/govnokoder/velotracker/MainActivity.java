package com.govnokoder.velotracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
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
import com.govnokoder.velotracker.ui.main.PageStart;
import com.govnokoder.velotracker.ui.main.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity implements PageStart.onSomeEventListener {

    TabLayout tabLayout;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    ViewPager2 pager;
    private final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 111;
    private final int REQUEST_PERMISSION_READ_PHONE_STATE = 333;
    private final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 444;
    private final int REQUEST_PERMISSION_ACCESS_BACKGROUND_LOCATION = 555;

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
                        tab.setText("Старт");
                        break;
                    case 1:
                        tab.setText("История");
                        break;
                    case 2:
                        tab.setText("Статистика");
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
            getPermission();
            return;
        }
        if (currentPer == Manifest.permission.ACCESS_FINE_LOCATION && !allowed && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
            // we will give warning to user that they haven't granted permissions.
            showDialogOpenSett("Для работы приложения требуется разрешение на местоположение устройства!", "per");
        }
        if (currentPer == Manifest.permission.READ_PHONE_STATE && !allowed && !shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)){
            // we will give warning to user that they haven't granted permissions.
            showDialogOpenSett("Для работы приложения требуется разрешение на состояние устройства!", "per");
        }
        if (currentPer == Manifest.permission.ACCESS_COARSE_LOCATION && !allowed && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)){
            // we will give warning to user that they haven't granted permissions.
            showDialogOpenSett("Для работы приложения требуется разрешение на приблизительное местоположение устройства!", "per");
        }
        if (currentPer == Manifest.permission.ACCESS_BACKGROUND_LOCATION && !allowed && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
            // we will give warning to user that they haven't granted permissions.
            showDialogOpenSett("Для работы приложения требуется разрешение на местоположение устройства!", "per");
        }
    }

    @Override
    public void getPermission() {
        //тут переопределяю метод из интерфейса который в фрагменте
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean accessFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean sdkVer = Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q;
        boolean readPhoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        boolean accessCoarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean backgroundLocation = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            backgroundLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        if (!accessFineLocation) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_ACCESS_FINE_LOCATION);
            return;
        }
        if (!sdkVer && !readPhoneState) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_PERMISSION_READ_PHONE_STATE);
            return;
        }
        if (!gpsEnabled) {
            showDialogOpenSett("Для работы приложения требуется доступ к данным о местоположении устройства!", "gps");
            return;
        }
        if (!accessCoarseLocation) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (backgroundLocation) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_PERMISSION_ACCESS_BACKGROUND_LOCATION);
                return;
            }
        }
        Intent intent = new Intent(this, TrainingActivity.class);
        startActivity(intent);
    }

    @Override
    public void openLastTraining() {
        int position = new TrainingController(this).LoadTrainingsData(this).size() - 1;
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

    private void showDialogOpenSett(String text, String target){
        AlertDialog builder = new AlertDialog.Builder(this).create();
        ConstraintLayout cl  = (ConstraintLayout)getLayoutInflater().inflate(R.layout.dialog_open_settings, null);
        TextView textView = (TextView) cl.getViewById(R.id.textView);
        textView.setText(text);
        cl.getViewById(R.id.OpenSettB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(target == "per"){
                    startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())));
                } else if(target == "gps"){
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                } else if(target == "api30"){
                    startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())));
                } else {return;}
                builder.dismiss();
            }
        });
        builder.setView(cl);
        builder.show();
    }
}