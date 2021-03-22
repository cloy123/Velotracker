package com.govnokoder.velotracker.ui.training;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.govnokoder.velotracker.BL.CurrentTraining;
import com.govnokoder.velotracker.BL.Model.Training;
import com.govnokoder.velotracker.R;
import com.govnokoder.velotracker.messages.MessageEvent;
import com.govnokoder.velotracker.messages.SharedViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
        model.message.observe(getViewLifecycleOwner(), new Observer<CurrentTraining>() {
            @Override
            public void onChanged(CurrentTraining currentTraining) {
                if(currentTraining != null){
                    timeText.setText(currentTraining.Time.toString());
                    wayLengthText.setText(String.valueOf(Training.round(currentTraining.Distance, 2)) + " " + getString(R.string.km));
                    speedText.setText(String.valueOf(Training.round(currentTraining.CurrentSpeed, 1)) + " " + getString(R.string.kph));
                    averageSpeedText.setText(String.valueOf((Training.round(currentTraining.AverageSpeed, 1))) + " " + getString(R.string.kph));
                    maxSpeedText.setText(String.valueOf(Training.round(currentTraining.MaxSpeed, 1)) + " " + getString(R.string.kph));
                    if(currentTraining.Heights.size() > 0){
                        heightText.setText(String.valueOf(currentTraining.Heights.get(currentTraining.Heights.size()-1)) + " " + getString(R.string.m));
                        maxHeightText.setText(String.valueOf(currentTraining.MaxHeight) + " " + getString(R.string.m));
                        minHeightText.setText(String.valueOf(currentTraining.MinHeight) + " " + getString(R.string.m));
                        averageHeightText.setText(String.valueOf(currentTraining.AverageHeight) + " " + getString(R.string.m));
                    }
                }
            }
        });
    }
    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}