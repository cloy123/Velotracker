package com.govnokoder.velotracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.govnokoder.velotracker.BL.Controller.TrainingController;

public class SettingsActivity extends AppCompatActivity {

    private Button DeleteAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        DeleteAll = (Button) findViewById(R.id.DeleteAll);
        DeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TrainingController trainingController = new TrainingController(getApplicationContext());
                trainingController.DeleteAll(getApplicationContext());
            }
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.settings);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                this.finish();
                //TODO выход из настроек
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}