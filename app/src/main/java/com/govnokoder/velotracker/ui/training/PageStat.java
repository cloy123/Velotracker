package com.govnokoder.velotracker.ui.training;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.govnokoder.velotracker.BL.ParcelableTraining;
import com.govnokoder.velotracker.BL.Model.Training;
import com.govnokoder.velotracker.R;
import com.govnokoder.velotracker.messages.SharedViewModel;

public class PageStat extends Fragment {
    private int pageNumber;

    private TextView timeText, wayLengthText, speedText, averageSpeedText, maxSpeedText, heightText, averageHeightText,
                        minHeightText, maxHeightText;

    public static PageStat newInstance(int page){
        PageStat fragment = new PageStat();
        Bundle args=new Bundle();
        args.putInt("num", page);
        fragment.setArguments(args);
        return fragment;
    }

    public PageStat(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments() != null ? getArguments().getInt("num"):1;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.training_stat_page, container, false);
        timeText = result.findViewById(R.id.TimeText);
        wayLengthText = result.findViewById(R.id.wayLengthText);
        speedText = result.findViewById(R.id.speedText);
        averageSpeedText = result.findViewById(R.id.averageSpeedText);
        maxSpeedText = result.findViewById(R.id.maxSpeedText);
        heightText = result.findViewById(R.id.heightText);
        averageHeightText = result.findViewById(R.id.averageHeightText);
        minHeightText = result.findViewById(R.id.minHeightText);
        maxHeightText = result.findViewById(R.id.maxHeightText);
        return result;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedViewModel model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        model.messagesParcelableTraining.observe(getViewLifecycleOwner(), new Observer<ParcelableTraining>() {
            @Override
            public void onChanged(ParcelableTraining parcelableTraining) {
                if(parcelableTraining != null){
                    timeText.setText(parcelableTraining.time.toString());
                    wayLengthText.setText(String.valueOf(Training.round(parcelableTraining.distance, 2)) + " " + getString(R.string.km));
                    speedText.setText(String.valueOf(Training.round(parcelableTraining.originLocation.getSpeed() * 3.6, 1)) + " " + getString(R.string.kph));
                    averageSpeedText.setText(String.valueOf((Training.round(parcelableTraining.averageSpeed, 1))) + " " + getString(R.string.kph));
                    maxSpeedText.setText(String.valueOf(Training.round(parcelableTraining.maxSpeed, 1)) + " " + getString(R.string.kph));
                    heightText.setText(String.valueOf(parcelableTraining.height + " " + getString(R.string.m)));
                    String maxHeight = Long.toString(parcelableTraining.maxHeight) + " " + getString(R.string.m);
                    if(parcelableTraining.maxHeight == Long.MIN_VALUE){maxHeight = "";}
                    String minHeight = Long.toString(parcelableTraining.minHeight) + " " + getString(R.string.m);
                    if(parcelableTraining.minHeight == Long.MAX_VALUE){minHeight = "";}
                    maxHeightText.setText(maxHeight);
                    minHeightText.setText(minHeight);
                    averageHeightText.setText(String.valueOf(parcelableTraining.averageHeight) + " " + getString(R.string.m));
                }
            }
        });
    }
    @Override
    public void onStop() {
        super.onStop();
    }
}