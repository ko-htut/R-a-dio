package com.jcanseco.radio.players;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.jcanseco.radio.tasks.RadioPlayerBufferTimeoutTimerTask;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Timer;
import java.util.TimerTask;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RadioPlayerTest {

    RadioPlayer radioPlayer;

    @Mock
    ExoPlayer exoPlayer;

    @Mock
    MediaCodecAudioTrackRenderer audioRenderer;

    @Mock
    RadioPlayer.Listener radioPlayerListener;

    @Mock
    Timer timer;

    @Before
    public void setup() {
        radioPlayer = spy(new RadioPlayer(exoPlayer, audioRenderer));
        radioPlayer.setRadioPlayerListener(radioPlayerListener);

        when(radioPlayer.initNewTimer()).thenReturn(timer);
        when(radioPlayer.getTimer()).thenReturn(timer);
        doNothing().when(timer).schedule(any(TimerTask.class), anyLong());
    }

    @Test
    public void shouldNotBePlayingByDefault() {
        assertThat(radioPlayer.isPlaying()).isFalse();
    }

    @Test
    public void whenPlayInvoked_andExoPlayerAlreadyPreparedForPlayback_thenSetExoPlayerToPlayWhenReadyToTrue() {
        doReturn(true).when(radioPlayer).isExoPlayerPreparedForPlayback();

        radioPlayer.play();

        verify(exoPlayer).setPlayWhenReady(true);
    }

    @Test
    public void whenPlayInvoked_andExoPlayerAlreadyPreparedForPlayback_thenSetPlayerToPlaying() {
        doReturn(true).when(radioPlayer).isExoPlayerPreparedForPlayback();

        radioPlayer.play();

        assertThat(radioPlayer.isPlaying()).isTrue();
    }

    @Test
    public void whenPlayInvoked_andExoPlayerNotYetPreparedForPlayback_thenPrepareExoPlayerForPlayback_withAudioRenderer() {
        doReturn(false).when(radioPlayer).isExoPlayerPreparedForPlayback();

        radioPlayer.play();

        verify(exoPlayer).prepare(audioRenderer);
    }

    @Test
    public void whenPlayInvoked_andExoPlayerNotYetPreparedForPlayback_thenSetExoPlayerToPlayWhenReadyToTrue() {
        doReturn(false).when(radioPlayer).isExoPlayerPreparedForPlayback();

        radioPlayer.play();

        verify(exoPlayer).setPlayWhenReady(true);
    }

    @Test
    public void whenPlayInvoked_andExoPlayerNotYetPreparedForPlayback_thenSetPlayerToPlaying() {
        doReturn(false).when(radioPlayer).isExoPlayerPreparedForPlayback();

        radioPlayer.play();

        assertThat(radioPlayer.isPlaying()).isTrue();
    }

    @Test
    public void whenPauseInvoked_setExoPlayerToPlayWhenReadyToFalse() {
        radioPlayer.pause();

        verify(exoPlayer).setPlayWhenReady(false);
    }

    @Test
    public void whenPauseInvoked_setPlayerToPaused() {
        radioPlayer.pause();

        assertThat(radioPlayer.isPlaying()).isFalse();
    }

    @Test
    public void whenReleaseInvoked_releaseExoPlayer() {
        radioPlayer.release();

        verify(exoPlayer).release();
    }

    @Test
    public void whenReleaseInvoked_setPlayerToPaused() {
        radioPlayer.release();

        assertThat(radioPlayer.isPlaying()).isFalse();
    }

    @Test
    public void onPlayerStateChanged_ifPlayerIsBuffering_andPlayerIsNotCurrentlyCountingDownForBufferTimeout_thenScheduleBufferToTimeoutIn10Secs() {
        doReturn(false).when(radioPlayer).isCurrentlyCountingDownForBufferTimeout();

        radioPlayer.onPlayerStateChanged(true, ExoPlayer.STATE_BUFFERING);

        verify(timer).schedule(any(RadioPlayerBufferTimeoutTimerTask.class), eq((long) 10000));
    }

    @Test
    public void onPlayerStateChanged_ifPlayerIsBuffering_andPlayerIsNotCurrentlyCountingDownForBufferTimeout_thenIndicateThatPlayerIsNowCurrentlyCountingDownForBufferTimeout() {
        doReturn(false).doCallRealMethod().when(radioPlayer).isCurrentlyCountingDownForBufferTimeout();

        radioPlayer.onPlayerStateChanged(true, ExoPlayer.STATE_BUFFERING);

        assertThat(radioPlayer.isCurrentlyCountingDownForBufferTimeout()).isTrue();
    }

    @Test
    public void onPlayerStateChanged_ifPlayerIsBuffering_andPlayerIsCurrentlyCountingDownForBufferTimeout_thenDontScheduleBufferToTimeout() {
        doReturn(true).when(radioPlayer).isCurrentlyCountingDownForBufferTimeout();

        radioPlayer.onPlayerStateChanged(true, ExoPlayer.STATE_BUFFERING);

        verify(timer, never()).schedule(any(RadioPlayerBufferTimeoutTimerTask.class), anyLong());
    }

    @Test
    public void onPlayerStateChanged_ifBufferTimeoutTimerIsNotNull_thenStopCountingDownForBufferTimeout() {
        doReturn(timer).when(radioPlayer).getTimer();

        radioPlayer.onPlayerStateChanged(eq(true), not(eq(ExoPlayer.STATE_BUFFERING)));

        InOrder inOrder = inOrder(timer);
        inOrder.verify(timer).cancel();
        inOrder.verify(timer).purge();
    }

    @Test
    public void onPlayerStateChanged_ifBufferTimeoutTimerIsNull_thenIndicateThatPlayerIsNoLongerCurrentlyCountingDownForBufferTimeout() {
        doReturn(null).when(radioPlayer).getTimer();

        radioPlayer.onPlayerStateChanged(eq(true), not(eq(ExoPlayer.STATE_BUFFERING)));

        assertThat(radioPlayer.isCurrentlyCountingDownForBufferTimeout()).isFalse();
    }

    @Test
    public void onPlayerStateChanged_ifBufferTimeoutTimerIsNotNull_thenIndicateThatPlayerIsNoLongerCurrentlyCountingDownForBufferTimeout() {
        doReturn(timer).when(radioPlayer).getTimer();

        radioPlayer.onPlayerStateChanged(eq(true), not(eq(ExoPlayer.STATE_BUFFERING)));

        assertThat(radioPlayer.isCurrentlyCountingDownForBufferTimeout()).isFalse();
    }

    @Test
    public void onBufferingTimedOut_shouldNotifyRadioPlayerListenerOfStreamError() {
        radioPlayer.onBufferingTimedOut();

        verify(radioPlayerListener).onRadioPlayerStreamError();
    }

    @Test
    public void onBufferingTimedOut_shouldStopExoPlayer() {
        radioPlayer.onBufferingTimedOut();

        verify(exoPlayer).stop();
    }

    @Test
    public void onBufferingTimedOut_shouldSetPlayerToPaused() {
        radioPlayer.onBufferingTimedOut();

        assertThat(radioPlayer.isPlaying()).isFalse();
    }

    @Test
    public void onBufferingTimedOut_shouldIndicateThatPlayerIsNoLongerCurrentlyCountingDownForBufferTimeout() {
        radioPlayer.onBufferingTimedOut();

        assertThat(radioPlayer.isCurrentlyCountingDownForBufferTimeout()).isFalse();
    }

    @Test
    public void onPlayerError_shouldNotifyRadioPlayerListenerOfStreamError() {
        String irrelevantErrorMessage = "";

        radioPlayer.onPlayerError(new ExoPlaybackException(irrelevantErrorMessage));

        verify(radioPlayerListener).onRadioPlayerStreamError();
    }

    @Test
    public void onPlayerError_shouldStopExoPlayer() {
        String irrelevantErrorMessage = "";

        radioPlayer.onPlayerError(new ExoPlaybackException(irrelevantErrorMessage));

        verify(exoPlayer).stop();
    }

    @Test
    public void onPlayerError_shouldSetPlayerToPaused() {
        String irrelevantErrorMessage = "";

        radioPlayer.onPlayerError(new ExoPlaybackException(irrelevantErrorMessage));

        assertThat(radioPlayer.isPlaying()).isFalse();
    }
}