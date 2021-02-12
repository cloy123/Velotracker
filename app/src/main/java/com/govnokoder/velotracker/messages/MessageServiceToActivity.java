package com.govnokoder.velotracker.messages;

import com.govnokoder.velotracker.BL.CurrentTraining;
public class MessageServiceToActivity {
    public final CurrentTraining currentTraining;
    public MessageServiceToActivity(CurrentTraining currentTraining) {
        this.currentTraining = currentTraining;
    }
}
