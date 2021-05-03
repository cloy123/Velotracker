package com.coursework.velotracker

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewAnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.coursework.velotracker.BL.Controller.TrainingController
import com.coursework.velotracker.databinding.ActivityMainBinding
import com.coursework.velotracker.ui.fragments.main.PageStart
import com.coursework.velotracker.ui.fragments.main.ViewPagerAdapter
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity(), PageStart.OnSomeEventListener, NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var header: View
    private lateinit var themeBtn: AppCompatImageButton
    private val REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 111

    private var gpsEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun changeTheme(){
        val sharedPreferences = getSharedPreferences("theme", MODE_PRIVATE)
        if(sharedPreferences.getBoolean("dark", false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            sharedPreferences.edit().putBoolean("light", true).apply()
            sharedPreferences.edit().putBoolean("dark", false).apply()
            themeBtn.foreground = getDrawable(R.drawable.ic_light_mode_foreground)
        }
        else if(sharedPreferences.getBoolean("light", false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            sharedPreferences.edit().putBoolean("dark", true).apply()
            sharedPreferences.edit().putBoolean("light", false).apply()
            themeBtn.foreground = getDrawable(R.drawable.ic_dark_mode_foreground)
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            sharedPreferences.edit().putBoolean("light", true).apply()
            sharedPreferences.edit().putBoolean("dark", false).apply()
            themeBtn.foreground = getDrawable(R.drawable.ic_light_mode_foreground)
        }
    }


    override fun onStart() {
        super.onStart()
        initFunc()
    }

    private fun initFunc(){
        createViewPager()
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { binding.drawerLayout.open() }
        binding.navView.setNavigationItemSelectedListener(this)
        header = binding.navView.getHeaderView(0)
        themeBtn = header.findViewById<AppCompatImageButton>(R.id.themeBtn)
        themeBtn.setOnClickListener {changeTheme()}
        setCurrentTheme()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setCurrentTheme(){
        val sharedPreferences = getSharedPreferences("theme", MODE_PRIVATE)
        if(sharedPreferences.contains("dark") && sharedPreferences.getBoolean("dark", false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            sharedPreferences.edit().putBoolean("dark", true).apply()
            sharedPreferences.edit().putBoolean("light", false).apply()
            themeBtn.foreground = getDrawable(R.drawable.ic_light_mode_foreground)
        }
        else if(sharedPreferences.contains("light") && sharedPreferences.getBoolean("light", false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            sharedPreferences.edit().putBoolean("dark", false).apply()
            sharedPreferences.edit().putBoolean("light", true).apply()
            themeBtn.foreground = getDrawable(R.drawable.ic_dark_mode_foreground)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_start -> binding.tabs.selectTab(binding.tabs.getTabAt(0))
            R.id.nav_history -> binding.tabs.selectTab(binding.tabs.getTabAt(1))
            R.id.nav_statistics -> binding.tabs.selectTab(binding.tabs.getTabAt(2))
            R.id.nav_about_program -> startActivity(
                Intent(
                    applicationContext,
                    AboutProgramActivity::class.java
                )
            )
        }
        binding.drawerLayout.closeDrawers()
        return false
    }

    private fun createViewPager(){
        val pagerAdapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        val tabLayoutMediator = TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getText(R.string.start)
                1 -> tab.text = getText(R.string.history)
                2 -> tab.text = getText(R.string.statistic)
            }
        }
        tabLayoutMediator.attach()
    }

    @SuppressLint("ResourceType")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        var allowed = false
        val currentPer = permissions[0]
        allowed = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        if(allowed){
            startTraining()
            return
        }
        if(currentPer == Manifest.permission.ACCESS_FINE_LOCATION && !shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_FINE_LOCATION
            )) {
            dialogOpenPermissionsSettings(getString(R.string.dialog_open_sett_access_fine_location_text))
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
            binding.tabs.selectTab(binding.tabs.getTabAt(1))
            val intent = Intent(this, LookTraining::class.java)
            intent.putExtra("INDEX", position)
            startActivity(intent)
        }
    }

    override fun openStatistic() {
        binding.tabs.selectTab(binding.tabs.getTabAt(2))
    }

    private fun dialogOpenPermissionsSettings(text: String) {
        val dialog: AlertDialog = AlertDialog.Builder(this).create()
        val cl = layoutInflater.inflate(R.layout.dialog_open_settings, null) as ConstraintLayout
        val textView = cl.getViewById(R.id.textView) as TextView
        textView.text = text
        cl.getViewById(R.id.openSettB).setOnClickListener {
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:$packageName")
                )
            )
            dialog.dismiss()
        }
        dialog.setView(cl)
        dialog.show()
    }

    private fun dialogBatteryOptimizationSettings() {
        //если таких настроек не существует, они создаются
        val sharedPreferences = getSharedPreferences(
            getString(R.string.settings_checkboxes),
            MODE_PRIVATE
        )
        if (sharedPreferences.contains(getString(R.string.dialog_battery_optimization_settings_checkbox_key))) {
            if (sharedPreferences.getBoolean(
                    getString(R.string.dialog_battery_optimization_settings_checkbox_key),
                    false
                )) {
                //если стоит больше не показывать
                startActivityTraining()
                return
            }
        }
        val isInSettings = booleanArrayOf(false)
        //добавляю настройку и потом вызываю диалог
        sharedPreferences.edit().putBoolean(
            getString(R.string.dialog_battery_optimization_settings_checkbox_key),
            false
        ).apply()
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
                val sharedPreferences = getSharedPreferences(
                    getString(R.string.settings_checkboxes),
                    MODE_PRIVATE
                )
                sharedPreferences.edit().putBoolean(
                    getString(R.string.dialog_battery_optimization_settings_checkbox_key),
                    true
                ).apply()
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
        val locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!accessFineLocation) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_ACCESS_FINE_LOCATION)
            return
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
