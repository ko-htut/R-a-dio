package com.jcanseco.radio.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.jcanseco.radio.R;
import com.jcanseco.radio.constants.Constants;
import com.jcanseco.radio.injectors.Injector;
import com.jcanseco.radio.loaders.RadioContentLoader;
import com.jcanseco.radio.models.NowPlayingTrack;
import com.jcanseco.radio.models.RadioContent;
import com.jcanseco.radio.radioplayer.RadioPlayerActivity;

import java.io.IOException;

public class RadioPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, RadioContentLoader.RadioContentListener {

    private static final int NOTIFICATION_ID = 1;

    MediaPlayer mediaPlayer;

    private RadioContentLoader radioContentLoader;

    private final IBinder radioPlayerBinder = new RadioPlayerBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = new MediaPlayer();
        radioContentLoader = Injector.provideRadioContentLoader();
        radioContentLoader.setRadioContentListener(this);
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
        } catch (IOException | IllegalStateException e) {
            sendOutFailedToPlayStreamBroadcast();
            startForegroundNotification();
        }
    }

    public void stopPlayingRadioStream() {
        try {
            mediaPlayer.stop();
        } catch (IllegalStateException e) {}

        mediaPlayer.reset();
        stopForegroundNotification();
    }

    private void prepareMediaPlayerForAsyncStreaming(String streamUrl) throws IOException, IllegalStateException {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setDataSource(streamUrl);
        mediaPlayer.prepareAsync();
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

    private void sendOutFailedToPlayStreamBroadcast() {
        Intent intent = new Intent(Constants.Actions.FAILED_TO_PLAY_RADIO_STREAM);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void startForegroundNotification() {
        radioContentLoader.beginActiveLoadingOfContent();
    }

    private void stopForegroundNotification() {
        radioContentLoader.stopActiveLoadingOfContent();
        stopForeground(true);
    }

    @Override
    public void onRadioContentLoadSuccess(RadioContent radioContent) {
        Notification notification = buildForegroundNotification(radioContent);
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onRadioContentLoadFailed() {
        // TODO: 2016-04-11 Handle this error better
        stopPlayingRadioStream();
        sendOutFailedToPlayStreamBroadcast();
    }

    private Notification buildForegroundNotification(RadioContent radioContent) {
        NowPlayingTrack currentTrack = radioContent.getCurrentTrack();

        Intent intent = new Intent(getApplicationContext(), RadioPlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new Notification.Builder(getApplicationContext())
                .setContentTitle("Now Playing")
                .setContentText(currentTrack.getTitle())
                .setSmallIcon(R.drawable.play)
                .setContentIntent(pendingIntent)
                .build();
    }

    public class RadioPlayerBinder extends Binder {
        public RadioPlayerService getService() {
            return RadioPlayerService.this;
        }
    }
}
