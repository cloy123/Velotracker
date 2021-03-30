package com.govnokoder.velotracker.ui.main;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.style.DrawableMarginSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.govnokoder.velotracker.BL.Controller.TrainingController;
import com.govnokoder.velotracker.BL.Model.Training;
import com.govnokoder.velotracker.LookTraining;
import com.govnokoder.velotracker.R;

import java.util.ArrayList;
import java.util.List;

public class PageHistory extends Fragment {

    private int pageNumber;
    private ListView listView;

    public static PageHistory newInstance(int page) {
        PageHistory fragment = new PageHistory();
        Bundle args=new Bundle();
        args.putInt("num", page);
        fragment.setArguments(args);
        return fragment;
    }

    public PageHistory() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments() != null ? getArguments().getInt("num") : 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.history_page, container, false);
        listView = (ListView) result.findViewById(R.id.listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), LookTraining.class);
                intent.putExtra("INDEX", position);
                startActivity(intent);
            }
        });
        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        TrainingController trainingController = new TrainingController(getContext());
        List<Training> trainings = trainingController.LoadTrainingsData();
        List<String> listDate = new ArrayList<>();
        if(trainings != null) {
            for (Training training: trainings) {
                listDate.add(training.Date.toString());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter(getContext(),
                android.R.layout.simple_list_item_1, listDate){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView)view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE);
                textView.setBackgroundColor(Color.argb(255, 0, 0, 0));
                return view;
            }
        };
        listView.setAdapter(adapter);
    }
}
