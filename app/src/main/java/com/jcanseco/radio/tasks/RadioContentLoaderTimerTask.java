package com.jcanseco.radio.tasks;

import com.jcanseco.radio.loaders.RadioContentLoader;

import java.util.TimerTask;

public class RadioContentLoaderTimerTask extends TimerTask {

    private RadioContentLoader radioContentLoader;

    public RadioContentLoaderTimerTask(RadioContentLoader radioContentLoader) {
        this.radioContentLoader = radioContentLoader;
    }

    @Override
    public void run() {
        radioContentLoader.loadContent();
    }
}
