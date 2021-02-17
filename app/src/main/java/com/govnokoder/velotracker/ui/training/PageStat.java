package com.govnokoder.velotracker.ui.training;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.govnokoder.velotracker.BL.CurrentTraining;
import com.govnokoder.velotracker.BL.Model.Training;
import com.govnokoder.velotracker.R;
import com.govnokoder.velotracker.messages.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class PageStat extends Fragment {
    private int pageNumber;

    private TextView timeText, destText, speedText, averageSpeedText, maxSpeedText, tempText, heightText, averageHeightText,
                        minHeightText, maxHeightText;

    private CurrentTraining currentTraining;

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
        destText = result.findViewById(R.id.distanceText);
        speedText = result.findViewById(R.id.speedText);
        averageSpeedText = result.findViewById(R.id.averageSpeedText);
        maxSpeedText = result.findViewById(R.id.maxSpeedText);
        tempText = result.findViewById(R.id.tempText);
        heightText = result.findViewById(R.id.heightText);
        averageHeightText = result.findViewById(R.id.averageHeightText);
        minHeightText = result.findViewById(R.id.minHeightText);
        maxHeightText = result.findViewById(R.id.maxHeightText);
        return result;
    }

    @Override
    public void onResume() {
        EventBus.getDefault().register(this);
        super.onResume();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    private void getMessage(MessageEvent event){
        currentTraining = event.currentTraining;
        if(currentTraining != null){
            timeText.setText(currentTraining.Time.toString());
            destText.setText(Double.toString(Training.round(currentTraining.WayLength, 1)));
            speedText.setText(Double.toString(Training.round(currentTraining.currentSpeed, 1)));
            Double avSpeed = ((currentTraining.WayLength*1000) / (currentTraining.Time.Hours*3600 + currentTraining.Time.Minutes*60 + currentTraining.Time.Seconds))*3.6;
            averageSpeedText.setText(Double.toString(Training.round(avSpeed, 1)));
            maxSpeedText.setText(Double.toString(Training.round(currentTraining.MaxSpeed, 1)));

            tempText.setText(Double.toString(currentTraining.WayLength));
        }
    }


    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
