package com.jcanseco.radio.players;

import android.app.Application;

import com.google.android.exoplayer.ExoPlayer;

public class PlayerFactory {

    private static final int RENDERER_COUNT = 1;
    private static final int MIN_BUFFER_IN_MILLIS = 1000;
    private static final int MIN_REBUFFER_IN_MILLIS = 5000;

    public static Player create(Application application) {
        return new RadioPlayer(createExoPlayer(), application);
    }

    private static ExoPlayer createExoPlayer() {
        return ExoPlayer.Factory.newInstance(RENDERER_COUNT, MIN_BUFFER_IN_MILLIS, MIN_REBUFFER_IN_MILLIS);
    }
}
