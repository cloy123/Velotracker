package com.coursework.velotracker

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.coursework.velotracker.BL.Controller.TrainingController
import com.coursework.velotracker.ui.main.PageStart
import com.coursework.velotracker.ui.main.ViewPagerAdapter
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity(), PageStart.OnSomeEventListener {

    private lateinit var tabLayout: TabLayout
    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var pager: ViewPager2

    private val REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 111
    private val REQUEST_PERMISSION_READ_PHONE_STATE = 333

    private var gpsEnabled = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pager = findViewById(R.id.view_pager)
        val pagerAdapter = ViewPagerAdapter(this)
        pager.adapter = pagerAdapter
        tabLayout = findViewById(R.id.tabs)
        val tabLayoutMediator = TabLayoutMediator(
            tabLayout, pager
        ) { tab, position ->
            when (position) {
                0 -> tab.text = getText(R.string.start)
                1 -> tab.text = getText(R.string.history)
                2 -> tab.text = getText(R.string.statistic)
            }
        }
        tabLayoutMediator.attach()
        val  toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { item -> //Установите слушателя, который будет получать уведомления при выборе пункта меню
            when (item.itemId) {
                R.id.nav_start -> tabLayout.selectTab(tabLayout.getTabAt(0))
                R.id.nav_history -> tabLayout.selectTab(tabLayout.getTabAt(1))
                R.id.nav_statistics -> tabLayout.selectTab(tabLayout.getTabAt(2))
                R.id.nav_about_program -> startActivity(
                    Intent(
                        applicationContext,
                        AboutProgramActivity::class.java
                    )
                )
            }
            drawerLayout.closeDrawers()
            false
        }
        toolbar.setNavigationOnClickListener { drawerLayout.open() }
    }

    @SuppressLint("ResourceType")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        var allowed = false
        var currentPer = permissions[0]
        allowed = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        if(allowed){
            startTraining()
            return
        }
        if(currentPer == android.Manifest.permission.ACCESS_FINE_LOCATION && !shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            dialogOpenPermissionsSettings(getString(R.string.dialog_open_sett_access_fine_location_text))
        }
        if (currentPer == Manifest.permission.READ_PHONE_STATE && !shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)){
            dialogOpenPermissionsSettings(getString(R.string.dialog_open_sett_read_phone_state_text))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == AppConstants.GPS_REQUEST){
            //isGPS = true
        }
    }

    private fun startActivityTraining() {
        val intent = Intent(applicationContext, TrainingActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun openLastTraining() {
        val position: Int = TrainingController(this).loadTrainings().size - 1
        if (position >= 0) {
            tabLayout.selectTab(tabLayout.getTabAt(1))
            val intent = Intent(this, LookTraining::class.java)
            intent.putExtra("INDEX", position)
            startActivity(intent)
        }
    }

    override fun openStatistic() {
        tabLayout.selectTab(tabLayout.getTabAt(2))
    }

    private fun dialogOpenPermissionsSettings(text: String) {
        val dialog: AlertDialog = AlertDialog.Builder(this).create()
        val cl = layoutInflater.inflate(R.layout.dialog_open_settings, null) as ConstraintLayout
        val textView = cl.getViewById(R.id.textView) as TextView
        textView.text = text
        cl.getViewById(R.id.openSettB).setOnClickListener {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
            dialog.dismiss()
        }
        dialog.setView(cl)
        dialog.show()
    }

    private fun dialogBatteryOptimizationSettings() {
        //если таких настроек не существует, они создаются
        val sharedPreferences = getSharedPreferences(getString(R.string.settings_checkboxes), MODE_PRIVATE)
        if (sharedPreferences.contains(getString(R.string.dialog_battery_optimization_settings_checkbox_key))) {
            if (sharedPreferences.getBoolean(getString(R.string.dialog_battery_optimization_settings_checkbox_key), false)) {
                //если стоит больше не показывать
                startActivityTraining()
                return
            }
        }
        val isInSettings = booleanArrayOf(false)
        //добавляю настройку и потом вызываю диалог
        sharedPreferences.edit().putBoolean(getString(R.string.dialog_battery_optimization_settings_checkbox_key), false).apply()
        val dialog = AlertDialog.Builder(this).create()
        val cl = layoutInflater.inflate(R.layout.dialog_open_settings_with_checkbox, null) as ConstraintLayout
        val textView = cl.getViewById(R.id.textView) as TextView
        textView.text = getString(R.string.dialog_battery_optimization_text)
        cl.getViewById(R.id.openSettB).setOnClickListener {
            isInSettings[0] = true
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            dialog.dismiss()
        }
        val compatCheckBox = cl.getViewById(R.id.checkbox) as AppCompatCheckBox
        dialog.setOnDismissListener(DialogInterface.OnDismissListener {
            if (compatCheckBox.isChecked) {
                val sharedPreferences = getSharedPreferences(getString(R.string.settings_checkboxes), MODE_PRIVATE)
                sharedPreferences.edit().putBoolean(getString(R.string.dialog_battery_optimization_settings_checkbox_key), true).apply()
            }
            if (!isInSettings[0]) {
                startActivityTraining()
                return@OnDismissListener
            }
        })
        dialog.setView(cl)
        dialog.show()
    }

    override fun startTraining() {
        val accessFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val readPhoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        val locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!accessFineLocation) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_ACCESS_FINE_LOCATION)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!readPhoneState) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), REQUEST_PERMISSION_READ_PHONE_STATE)
                return
            }
        }
        if (!gpsEnabled) {
            val gpsUtils = GpsUtils(this)
            gpsUtils.turnGPSOn(object : GpsUtils.OnGpsListener {
                override fun gpsStatus(isGPSEnable: Boolean) {
                    if (isGPSEnable) {
                        startTraining()
                    }
                }
            })
        }
        if (!gpsEnabled) {
            return
        }
        dialogBatteryOptimizationSettings()
    }
}
