package com.jcanseco.radio.tasks;

import com.jcanseco.radio.players.RadioPlayer;

import java.util.TimerTask;

public class RadioPlayerBufferTimeoutTimerTask extends TimerTask {

    private RadioPlayer player;

    public RadioPlayerBufferTimeoutTimerTask(RadioPlayer player) {
        this.player = player;
    }

    @Override
    public void run() {
        player.onBufferingTimedOut();
    }
}
