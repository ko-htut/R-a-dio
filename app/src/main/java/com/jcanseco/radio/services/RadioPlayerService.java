package com.jcanseco.radio.services;

import android.app.Application;
import android.app.Notification;
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
import com.jcanseco.radio.injectors.Injector;
import com.jcanseco.radio.services.notifications.RadioPlayerNotificationFactory;

import java.io.IOException;

public class RadioPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, RadioPlayerNotificationFactory.Client {

    MediaPlayer mediaPlayer;

    RadioPlayerNotificationFactory notificationFactory;

    private static final int NOTIFICATION_ID = 1;

    private final IBinder radioPlayerBinder = new RadioPlayerBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = new MediaPlayer();

        notificationFactory = Injector.provideRadioPlayerNotificationFactory(this, (Application) getApplicationContext());
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
            prepareMediaPlayerForAsyncStreaming(streamUrl);
            notificationFactory.startScheduledCreationOfPlayerNotifications();
        } catch (IOException | IllegalStateException e) {
            sendOutFailedToPlayStreamBroadcast();
        }
    }

    private void prepareMediaPlayerForAsyncStreaming(String streamUrl) throws IOException, IllegalStateException {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setDataSource(streamUrl);
        mediaPlayer.prepareAsync();
    }

    public void stopPlayingRadioStream() {
        try {
            mediaPlayer.stop();
        } catch (IllegalStateException e) {}

        mediaPlayer.reset();
        notificationFactory.stopScheduledCreationOfPlayerNotifications();
        stopForeground(false);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        onFailureDuringStream();
        return true;
    }

    @Override
    public void onPlayerNotificationCreationSuccess(Notification notification) {
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onPlayerNotificationCreationFailed() {
        onFailureDuringStream();
    }

    private void onFailureDuringStream() {
        stopPlayingRadioStream();
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
