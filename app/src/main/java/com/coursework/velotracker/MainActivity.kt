package com.coursework.velotracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TableLayout
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2

import com.coursework.velotracker.BL.Model.toString
import com.google.android.material.navigation.NavigationView
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var tableLayout: TableLayout
    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var pager: ViewPager2

    private val REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 111
    private val REQUEST_PERMISSION_READ_PHONE_STATE = 333


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pager = findViewById(R.id.view_pager)
        //val FragmentStateAdapter = ViewPagerAdapter()
    }

    override fun onResume() {
        super.onResume()

        //"dd/MM/yyyy"
    }

}
