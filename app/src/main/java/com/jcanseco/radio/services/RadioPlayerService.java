package com.jcanseco.radio.services;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.jcanseco.radio.constants.Constants;
import com.jcanseco.radio.injectors.Injector;
import com.jcanseco.radio.players.RadioPlayer;

public class RadioPlayerService extends Service implements RadioPlayer.Listener {

    RadioPlayer radioPlayer;

    private final IBinder radioPlayerBinder = new RadioPlayerBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        radioPlayer = Injector.provideRadioPlayer((Application) getApplicationContext());
        radioPlayer.setRadioPlayerListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return radioPlayerBinder;
    }

    @Override
    public void onDestroy() {
        radioPlayer.release();
    }

    public boolean isPlayingStream() {
        return radioPlayer.isPlaying();
    }

    public void startPlayingRadioStream() {
        radioPlayer.play();
    }

    public void stopPlayingRadioStream() {
        radioPlayer.pause();
    }

    @Override
    public void onRadioPlayerStreamError() {
        sendOutFailedToPlayStreamBroadcast();
    }

    private void sendOutFailedToPlayStreamBroadcast() {
        Intent intent = new Intent(Constants.Actions.FAILED_TO_PLAY_RADIO_STREAM);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }


    public class RadioPlayerBinder extends Binder {
        public RadioPlayerService getService() {
            return RadioPlayerService.this;
        }
    }
}
