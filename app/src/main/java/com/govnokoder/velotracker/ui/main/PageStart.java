package com.govnokoder.velotracker.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.LinkAddress;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.govnokoder.velotracker.MainActivity;
import com.govnokoder.velotracker.R;
import com.govnokoder.velotracker.TrainingActivity;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;

import java.security.Permission;
import java.security.Permissions;
import java.util.List;

public class PageStart extends Fragment {

    private int pageNumber;
    private Button StartButton;
    private LinearLayout linearStat, linearLastTraining;


    public interface onSomeEventListener {
        public void getPermission();
        public void openLastTraining();
        public void openStatistic();
    }

    onSomeEventListener someEventListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            someEventListener = (onSomeEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }



    public static PageStart newInstance(int page) {
        PageStart fragment = new PageStart();
        Bundle args=new Bundle();
        args.putInt("num", page);
        fragment.setArguments(args);
        return fragment;
    }

    public PageStart() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments() != null ? getArguments().getInt("num") : 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.start_page, container, false);
        linearLastTraining = result.findViewById(R.id.linearLastTraining);
        linearLastTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO переход в историю и открытие ласт записи
                someEventListener.openLastTraining();
                Toast.makeText(getContext(), "click!!!", Toast.LENGTH_LONG).show();
            }
        });


        StartButton = result.findViewById(R.id.startButton);
        StartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    someEventListener.getPermission();
            }
        });


        linearStat = result.findViewById(R.id.linearStat);
        linearStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                someEventListener.openStatistic();
            }
        });


        return result;
    }
}
