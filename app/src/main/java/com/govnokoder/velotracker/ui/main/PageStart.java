package com.govnokoder.velotracker.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.govnokoder.velotracker.BL.Controller.TrainingController;
import com.govnokoder.velotracker.BL.Model.Training;
import com.govnokoder.velotracker.R;

import java.util.List;

public class PageStart extends Fragment {

    private int pageNumber;
    private Button StartButton;
    private LinearLayout linearStat, linearLastTraining;
    private TextView totalDistanceText, lastTrainingDateText, lastTrainingTimeText, lastTrainingDistanceText, lastTrainingAverageSpeedText;


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

    public PageStart() { }

    @Override
    public void onResume() {
        super.onResume();
        TrainingController trainingController = new TrainingController(getContext());
        List<Training> trainings = trainingController.LoadTrainingsData(getContext());
        if(trainings != null){
            Training lastTraining = trainings.get(trainings.size() - 1);
            double totalDest = 0;
            for (Training training : trainings) {
                totalDest += training.WayLength;
                totalDistanceText.setText(Double.toString(Training.round(totalDest, 2)));
            }
            lastTrainingTimeText.setText(lastTraining.AllTime.toString());
            lastTrainingAverageSpeedText.setText(Double.toString(Training.round(lastTraining.AverageSpeed, 1)));
            lastTrainingDistanceText.setText(Double.toString(Training.round(lastTraining.WayLength, 2)));
            lastTrainingDateText.setText(lastTraining.Date.toString());
        }
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
                someEventListener.openLastTraining();
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

        totalDistanceText = result.findViewById(R.id.totalDistanceTextV);
        lastTrainingDateText = result.findViewById(R.id.lastTrainingDateTextV);
        lastTrainingDistanceText = result.findViewById(R.id.lastDistanceTextV);
        lastTrainingAverageSpeedText = result.findViewById(R.id.lastAverageSpeedTextV);
        lastTrainingTimeText = result.findViewById(R.id.lastTimeTextV);
        return result;
    }
}
