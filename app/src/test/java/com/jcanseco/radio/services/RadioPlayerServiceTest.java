package com.jcanseco.radio.services;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;

import com.jcanseco.radio.BuildConfig;
import com.jcanseco.radio.constants.Constants;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.ServiceController;

import java.io.IOException;

import static com.jcanseco.radio.testutilities.BroadcastTestingUtilities.buildMockLocalBroadcastReceiver;
import static com.jcanseco.radio.testutilities.BroadcastTestingUtilities.verifyThatReceiverReceivedExpectedBroadcast;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = "src/main/AndroidManifest.xml")
public class RadioPlayerServiceTest {

    private RadioPlayerService radioPlayerService;
    private ServiceController<RadioPlayerService> serviceController;

    @Before
    public void setup() {
        ShadowApplication.getInstance().clearStartedServices();

        serviceController = Robolectric.buildService(RadioPlayerService.class);
        radioPlayerService = serviceController.attach().create().get();
    }

    @Test
    public void testThatServiceCanBeBound() {
        RadioPlayerService.RadioPlayerBinder binder = (RadioPlayerService.RadioPlayerBinder) radioPlayerService.onBind(mock(Intent.class));

        assertThat(binder).isNotNull();
        assertThat(binder.getService()).isEqualTo(radioPlayerService);
    }

    @Test
    public void whenIsPlayingStreamInvoked_ifMediaPlayerIsPlaying_thenReturnTrue() {
        radioPlayerService.mediaPlayer = mock(MediaPlayer.class);
        when(radioPlayerService.mediaPlayer.isPlaying()).thenReturn(true);

        assertThat(radioPlayerService.isPlayingStream()).isTrue();
    }

    @Test
    public void whenIsPlayingStreamInvoked_ifMediaPlayerIsNotPlaying_thenReturnFalse() {
        radioPlayerService.mediaPlayer = mock(MediaPlayer.class);
        when(radioPlayerService.mediaPlayer.isPlaying()).thenReturn(false);

        assertThat(radioPlayerService.isPlayingStream()).isFalse();
    }

    @Test
    public void whenStartPlayingRadioStreamInvoked_mediaPlayerShouldBeSetUpProperly() throws Exception {
        MediaPlayer mediaPlayer = mock(MediaPlayer.class);
        radioPlayerService.mediaPlayer = mediaPlayer;

        radioPlayerService.startPlayingRadioStream("http://streamurl.com");

        verify(mediaPlayer).setAudioStreamType(AudioManager.STREAM_MUSIC);
        verify(mediaPlayer).setWakeMode(ShadowApplication.getInstance().getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        verify(mediaPlayer).setOnPreparedListener(any(MediaPlayer.OnPreparedListener.class));
        verify(mediaPlayer).setOnErrorListener(any(MediaPlayer.OnErrorListener.class));
        verify(mediaPlayer).setDataSource("http://streamurl.com");
        verify(mediaPlayer).prepareAsync();
    }

    @Test
    public void whenStartPlayingRadioStreamInvoked_ifIOExceptionThrown_thenSendOutFailureToPlayStreamBroadcast() throws Exception {
        MediaPlayer mediaPlayer = mock(MediaPlayer.class);
        doThrow(new IOException()).when(mediaPlayer).setDataSource(anyString());
        radioPlayerService.mediaPlayer = mediaPlayer;

        String expectedBroadcastIntentAction = Constants.Actions.FAILED_TO_PLAY_RADIO_STREAM;
        BroadcastReceiver receiver = buildMockLocalBroadcastReceiver(expectedBroadcastIntentAction);

        radioPlayerService.startPlayingRadioStream("http://streamurl.com");

        verifyThatReceiverReceivedExpectedBroadcast(receiver, expectedBroadcastIntentAction);
    }

    @Test
    public void whenStartPlayingRadioStreamInvoked_ifIllegalStateExceptionThrown_thenSendOutFailureToPlayStreamBroadcast() {
        MediaPlayer mediaPlayer = mock(MediaPlayer.class);
        doThrow(new IllegalStateException()).when(mediaPlayer).prepareAsync();
        radioPlayerService.mediaPlayer = mediaPlayer;

        String expectedBroadcastIntentAction = Constants.Actions.FAILED_TO_PLAY_RADIO_STREAM;
        BroadcastReceiver receiver = buildMockLocalBroadcastReceiver(expectedBroadcastIntentAction);

        radioPlayerService.startPlayingRadioStream("http://streamurl.com");

        verifyThatReceiverReceivedExpectedBroadcast(receiver, expectedBroadcastIntentAction);
    }

    @Test
    public void whenStopPlayingRadioStreamInvoked_stopAndResetMediaPlayer() {
        MediaPlayer mediaPlayer = mock(MediaPlayer.class);
        radioPlayerService.mediaPlayer = mediaPlayer;

        radioPlayerService.stopPlayingRadioStream();

        verify(mediaPlayer).stop();
        verify(mediaPlayer).reset();
    }

    @Test
    public void whenStopPlayingRadioStreamInvoked_ifIllegalStateExceptionThrown_thenResetMediaPlayerShouldStillBeInvoked() {
        MediaPlayer mediaPlayer = mock(MediaPlayer.class);
        doThrow(new IllegalStateException()).when(mediaPlayer).stop();
        radioPlayerService.mediaPlayer = mediaPlayer;

        radioPlayerService.stopPlayingRadioStream();

        verify(mediaPlayer).reset();
    }

    @Test
    public void onMediaPlayerPrepared_startMediaPlayer() {
        MediaPlayer mediaPlayer = mock(MediaPlayer.class);
        radioPlayerService.mediaPlayer = mediaPlayer;

        radioPlayerService.onPrepared(mock(MediaPlayer.class));

        verify(mediaPlayer).start();
    }

    @Test
    public void onMediaPlayerError_resetMediaPlayer_andIndicateErrorHandled() {
        MediaPlayer mediaPlayer = mock(MediaPlayer.class);
        radioPlayerService.mediaPlayer = mediaPlayer;

        int irrelevantInt = 0;
        boolean wasErrorHandled = radioPlayerService.onError(mock(MediaPlayer.class), irrelevantInt, irrelevantInt);

        verify(mediaPlayer).reset();
        assertTrue(wasErrorHandled);
    }
}