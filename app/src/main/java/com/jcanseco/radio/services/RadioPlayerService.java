package com.jcanseco.radio.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.jcanseco.radio.constants.Constants;

import java.io.IOException;

public class RadioPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    MediaPlayer mediaPlayer;

    private final IBinder radioPlayerBinder = new RadioPlayerBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = new MediaPlayer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return radioPlayerBinder;
    }

    public boolean isPlayingStream() {
        return mediaPlayer.isPlaying();
    }

    public void startPlayingRadioStream(String streamUrl) {
        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setDataSource(streamUrl);
            mediaPlayer.prepareAsync();
        } catch (IOException | IllegalStateException e) {
            Intent intent = new Intent(Constants.Actions.FAILED_TO_PLAY_RADIO_STREAM);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    public void stopPlayingRadioStream() {
        try {
            mediaPlayer.stop();
        } catch (IllegalStateException e) {}

        mediaPlayer.reset();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mediaPlayer.reset();
        return true;
    }


    public class RadioPlayerBinder extends Binder {
        public RadioPlayerService getService() {
            return RadioPlayerService.this;
        }
    }
}
