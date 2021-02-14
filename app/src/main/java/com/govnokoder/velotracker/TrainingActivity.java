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
import com.govnokoder.velotracker.ui.training.ViewPagerAdapterTr;

import java.util.List;

public class TrainingActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);


        viewPager = findViewById(R.id.view_pager);


        ViewPagerAdapterTr viewPagerAdapterTr = new ViewPagerAdapterTr(this);

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

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
////        Intent intent = new Intent(getApplicationContext() ,MainActivity.class);
////        startActivity(intent);
////        finish();
////        getApplication().onCreate();
////        //getApplication().
////        if(!isActivityRunning(MainActivity.class)){
////            //Intent intent = new Intent(getApplicationContext() ,MainActivity.class);
////
////
////            //startActivity(intent);
////        }
//    }

    protected Boolean isActivityRunning(Class activityClass)
    {
        ActivityManager activityManager = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();

        for (ActivityManager.AppTask task : tasks) {
            try {
                if (task.getTaskInfo().baseActivity.toString() == null){//baseActivity.getClassName()))
                    Toast.makeText(this, "true", Toast.LENGTH_LONG).show();
                    return false;
                }
            }catch (Exception e){
                return false;
            }

        }
        Toast.makeText(this, "false", Toast.LENGTH_LONG).show();
        return true;
    }
}