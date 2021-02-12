package com.govnokoder.velotracker.messages;

import com.govnokoder.velotracker.BL.CurrentTraining;

public class MessageActivityToService {
    public final CurrentTraining currentTraining;
    public MessageActivityToService(CurrentTraining currentTraining) {
        this.currentTraining = currentTraining;
    }
}
