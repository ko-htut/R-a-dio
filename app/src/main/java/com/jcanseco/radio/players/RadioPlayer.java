package com.jcanseco.radio.players;

import android.app.Application;
import android.content.Context;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.TrackRenderer;
import com.jcanseco.radio.players.trackrenderers.TrackRendererFactory;
import com.jcanseco.radio.tasks.RadioPlayerBufferTimeoutTimerTask;

import java.util.Timer;
import java.util.TimerTask;

public class RadioPlayer implements Player, ExoPlayer.Listener {

    private static final long BUFFER_TIMEOUT_IN_MILLIS = 10000;

    private Player.Listener playerListener;

    private ExoPlayer exoPlayer;
    private boolean isPlaying;

    private Timer timer;
    private boolean isCurrentlyCountingDownForBufferTimeout;

    private final Context applicationContext;

    public RadioPlayer(ExoPlayer exoPlayer, Application application) {
        this.exoPlayer = exoPlayer;
        this.exoPlayer.addListener(this);

        this.applicationContext = application;
    }

    @Override
    public void setPlayerListener(Player.Listener playerListener) {
        this.playerListener = playerListener;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void play() {
        if (!isExoPlayerPreparedForPlayback()) {
            prepareExoPlayerForPlayback();
        }
        exoPlayer.setPlayWhenReady(true);
        isPlaying = true;
    }

    private void prepareExoPlayerForPlayback() {
        exoPlayer.prepare(createAudioTrackRenderer());
    }

    protected TrackRenderer createAudioTrackRenderer() {
        return TrackRendererFactory.createAudioTrackRenderer(applicationContext);
    }

    @Override
    public void pause() {
        exoPlayer.setPlayWhenReady(false);
        isPlaying = false;
    }

    @Override
    public void release() {
        exoPlayer.release();
        isPlaying = false;
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if(isPlayerBuffering(playbackState)) {
            beginCountdownForBufferTimeout();
        } else {
            stopCountdownForBufferTimeout();
        }
    }

    private boolean isPlayerBuffering(int playbackState) {
        return playbackState == ExoPlayer.STATE_BUFFERING;
    }

    private void beginCountdownForBufferTimeout() {
        if (!isCurrentlyCountingDownForBufferTimeout()) {
            scheduleTimerTaskForBufferTimeout();
            isCurrentlyCountingDownForBufferTimeout = true;
        }
    }

    private void scheduleTimerTaskForBufferTimeout() {
        timer = initNewTimer();
        TimerTask bufferTimeoutTimerTask = new RadioPlayerBufferTimeoutTimerTask(this);
        timer.schedule(bufferTimeoutTimerTask, BUFFER_TIMEOUT_IN_MILLIS);
    }

    private void stopCountdownForBufferTimeout() {
        if(getTimer() != null) {
            getTimer().cancel();
            getTimer().purge();
        }
        isCurrentlyCountingDownForBufferTimeout = false;
    }

    public void onBufferingTimedOut() {
        onPlayerError(new ExoPlaybackException("Buffering timed out."));
        isCurrentlyCountingDownForBufferTimeout = false;
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        playerListener.onPlayerStreamError();
        exoPlayer.stop();
        isPlaying = false;
    }

    @Override
    public void onPlayWhenReadyCommitted() {}

    protected boolean isExoPlayerPreparedForPlayback() {
        return exoPlayer.getPlaybackState() != ExoPlayer.STATE_IDLE;
    }

    protected boolean isCurrentlyCountingDownForBufferTimeout() {
        return isCurrentlyCountingDownForBufferTimeout;
    }

    protected Timer initNewTimer() {
        return new Timer();
    }

    protected Timer getTimer() {
        return timer;
    }
}
