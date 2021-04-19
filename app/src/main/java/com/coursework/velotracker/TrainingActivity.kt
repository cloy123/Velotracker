package com.coursework.velotracker

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import com.coursework.velotracker.BL.Model.Training.ParcelableTraining
import com.coursework.velotracker.Messages.SharedViewModel
import com.coursework.velotracker.Services.LocationService
import com.coursework.velotracker.ui.training.PageMap
import com.coursework.velotracker.ui.training.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class TrainingActivity: AppCompatActivity(), PageMap.OnSomeEventListener {

    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager2
    lateinit var viewPagerAdapter: ViewPagerAdapter
    lateinit var progressBar: ProgressBar

    private lateinit var myReceiver:MyReceiver

    private var mService: LocationService? = null
    private val isStart = false
    private var mBound = false

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
    }

    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
            mBound = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        viewPager = findViewById(R.id.view_pager)
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter
        tabLayout = findViewById(R.id.tabs)
        val tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "1"
                1 -> tab.text = "2"
            }
        }
        tabLayoutMediator.attach()
        myReceiver = MyReceiver()
        startForegroundService(Intent(this, LocationService::class.java))
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        bindService(Intent(this, LocationService::class.java), mServiceConnection,
                Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                IntentFilter(LocationService.ACTION_BROADCAST))
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver)
        super.onPause()
    }

    override fun onStop() {
        if (mBound) {
            unbindService(mServiceConnection)
            mBound = false
        }
        super.onStop()
    }

    private fun sendParcelableTraining(parcelableTraining: ParcelableTraining) {
        val viewModel: ViewModelStore = ViewModelStore()
        val model: SharedViewModel = ViewModelProvider(this).get<SharedViewModel>(SharedViewModel::class.java)
        model.sendMessage(parcelableTraining)
    }

    override fun onPauseTraining() {
        mService!!.onPause()
    }

    override fun onStopTraining(isSave: Boolean) {
        mService!!.onStop(isSave)
    }

    override fun onResumeTraining() {
        mService!!.onResume()
    }

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val parcelableTraining: ParcelableTraining? = intent.getParcelableExtra(LocationService.EXTRA_PARCELABLE_TRAINING)
            if (parcelableTraining != null) {
                if (!isStart) {
                    progressBar.visibility = View.INVISIBLE
                }
                sendParcelableTraining(parcelableTraining)
            }
        }
    }
}