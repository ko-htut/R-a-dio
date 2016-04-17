package com.jcanseco.radio.players;

import android.app.Application;
import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.SampleSource;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.jcanseco.radio.BuildConfig;
import com.jcanseco.radio.R;
import com.jcanseco.radio.constants.Constants;
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
        exoPlayer.prepare(buildAudioRenderer());
    }

    protected MediaCodecAudioTrackRenderer buildAudioRenderer() {
        return Factory.createAudioRenderer(applicationContext);
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


    public static class Factory {

        private static final int RENDERER_COUNT = 1;
        private static final int MIN_BUFFER_IN_MILLIS = 1000;
        private static final int MIN_REBUFFER_IN_MILLIS = 5000;

        private static final int BUFFER_SEGMENT_SIZE_IN_BYTES = 1024;
        private static final int NUM_OF_SEGMENTS_TO_BUFFER = 64;
        private static final int REQUESTED_BUFFER_SIZE = NUM_OF_SEGMENTS_TO_BUFFER * BUFFER_SEGMENT_SIZE_IN_BYTES;

        public static RadioPlayer create(Application application) {
            ExoPlayer exoPlayer = ExoPlayer.Factory.newInstance(RENDERER_COUNT, MIN_BUFFER_IN_MILLIS, MIN_REBUFFER_IN_MILLIS);
            return new RadioPlayer(exoPlayer, application);
        }

        public static MediaCodecAudioTrackRenderer createAudioRenderer(Context context) {
            SampleSource sampleSource = createSampleSource(context);
            return new MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT);
        }

        private static SampleSource createSampleSource(Context context) {
            Uri streamUri = Uri.parse(Constants.Endpoints.STREAM_URL);
            DataSource dataSource = new DefaultUriDataSource(context, null, getUserAgent(context));
            Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE_IN_BYTES);
            return new ExtractorSampleSource(streamUri, dataSource, allocator, REQUESTED_BUFFER_SIZE);
        }

        private static String getUserAgent(Context context) {
            String appName = getAppName(context);
            String appBuildVersion = getAppBuildVersion();
            return String.format("%s/%s", appName, appBuildVersion);
        }

        private static String getAppName(Context context) {
            return context.getString(R.string.app_name);
        }

        private static String getAppBuildVersion() {
            return BuildConfig.VERSION_NAME;
        }
    }
}
