package com.govnokoder.velotracker.messages;

import com.govnokoder.velotracker.BL.CurrentTraining;

public class MessageEvent {

    public final CurrentTraining currentTraining;
    //public final String dest;

    public MessageEvent(CurrentTraining currentTraining) {
        this.currentTraining = currentTraining;
    }
}
