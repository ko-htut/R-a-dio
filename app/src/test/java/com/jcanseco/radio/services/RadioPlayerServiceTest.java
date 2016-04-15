package com.jcanseco.radio.services;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;

import com.jcanseco.radio.BuildConfig;
import com.jcanseco.radio.constants.Constants;
import com.jcanseco.radio.services.notifications.RadioPlayerNotificationFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowService;
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
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = "src/main/AndroidManifest.xml")
public class RadioPlayerServiceTest {

    private RadioPlayerService radioPlayerService;
    private ServiceController<RadioPlayerService> serviceController;

    private MediaPlayer mediaPlayer;
    private RadioPlayerNotificationFactory notificationFactory;

    @Before
    public void setup() {
        ShadowApplication.getInstance().clearStartedServices();

        serviceController = Robolectric.buildService(RadioPlayerService.class);
        radioPlayerService = serviceController.attach().create().get();

        mediaPlayer = mock(MediaPlayer.class);
        radioPlayerService.mediaPlayer = mediaPlayer;

        notificationFactory = mock(RadioPlayerNotificationFactory.class);
        radioPlayerService.notificationFactory = notificationFactory;
    }

    @Test
    public void testThatServiceCanBeBound() {
        RadioPlayerService.RadioPlayerBinder binder = (RadioPlayerService.RadioPlayerBinder) radioPlayerService.onBind(mock(Intent.class));

        assertThat(binder).isNotNull();
        assertThat(binder.getService()).isEqualTo(radioPlayerService);
    }

    @Test
    public void whenIsPlayingStreamInvoked_ifMediaPlayerIsPlaying_thenReturnTrue() {
        when(mediaPlayer.isPlaying()).thenReturn(true);

        assertThat(radioPlayerService.isPlayingStream()).isTrue();
    }

    @Test
    public void whenIsPlayingStreamInvoked_ifMediaPlayerIsNotPlaying_thenReturnFalse() {
        when(mediaPlayer.isPlaying()).thenReturn(false);

        assertThat(radioPlayerService.isPlayingStream()).isFalse();
    }

    @Test
    public void whenStartPlayingRadioStreamInvoked_mediaPlayerShouldBeSetUpProperly() throws Exception {
        radioPlayerService.startPlayingRadioStream("http://streamurl.com");

        verify(mediaPlayer).setAudioStreamType(AudioManager.STREAM_MUSIC);
        verify(mediaPlayer).setWakeMode(ShadowApplication.getInstance().getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        verify(mediaPlayer).setOnPreparedListener(any(MediaPlayer.OnPreparedListener.class));
        verify(mediaPlayer).setOnErrorListener(any(MediaPlayer.OnErrorListener.class));
        verify(mediaPlayer).setDataSource("http://streamurl.com");
        verify(mediaPlayer).prepareAsync();
    }

    @Test
    public void whenStartPlayingRadioStreamInvoked_startScheduledCreationOfPlayerNotifications() {
        radioPlayerService.startPlayingRadioStream("http://streamurl.com");

        verify(notificationFactory).startScheduledCreationOfPlayerNotifications();
    }

    @Test
    public void whenStartPlayingRadioStreamInvoked_ifIOExceptionThrown_thenSendOutFailureToPlayStreamBroadcast() throws Exception {
        doThrow(new IOException()).when(mediaPlayer).setDataSource(anyString());
        String expectedBroadcastIntentAction = Constants.Actions.FAILED_TO_PLAY_RADIO_STREAM;
        BroadcastReceiver receiver = buildMockLocalBroadcastReceiver(expectedBroadcastIntentAction);

        radioPlayerService.startPlayingRadioStream("http://streamurl.com");

        verifyThatReceiverReceivedExpectedBroadcast(receiver, expectedBroadcastIntentAction);
    }

    @Test
    public void whenStartPlayingRadioStreamInvoked_ifIllegalStateExceptionThrown_thenSendOutFailureToPlayStreamBroadcast() {
        doThrow(new IllegalStateException()).when(mediaPlayer).prepareAsync();
        String expectedBroadcastIntentAction = Constants.Actions.FAILED_TO_PLAY_RADIO_STREAM;
        BroadcastReceiver receiver = buildMockLocalBroadcastReceiver(expectedBroadcastIntentAction);

        radioPlayerService.startPlayingRadioStream("http://streamurl.com");

        verifyThatReceiverReceivedExpectedBroadcast(receiver, expectedBroadcastIntentAction);
    }

    @Test
    public void whenStopPlayingRadioStreamInvoked_stopAndResetMediaPlayer() {
        radioPlayerService.stopPlayingRadioStream();

        verify(mediaPlayer).stop();
        verify(mediaPlayer).reset();
    }

    @Test
    public void whenStopPlayingRadioStreamInvoked_ifIllegalStateExceptionThrown_thenResetMediaPlayerShouldStillBeInvoked() {
        doThrow(new IllegalStateException()).when(mediaPlayer).stop();

        radioPlayerService.stopPlayingRadioStream();

        verify(mediaPlayer).reset();
    }

    @Test
    public void whenStopPlayingRadioStreamInvoked_stopScheduledCreationOfPlayerNotifications() {
        radioPlayerService.stopPlayingRadioStream();

        verify(notificationFactory).stopScheduledCreationOfPlayerNotifications();
    }

    @Test
    public void whenStopPlayingRadioStreamInvoked_ifIllegalStateExceptionThrown_thenStopScheduledCreationOfPlayerNotificationsShouldStillBeInvoked() {
        doThrow(new IllegalStateException()).when(mediaPlayer).stop();

        radioPlayerService.stopPlayingRadioStream();

        verify(notificationFactory).stopScheduledCreationOfPlayerNotifications();
    }

    @Test
    public void onMediaPlayerPrepared_startMediaPlayer() {
        radioPlayerService.onPrepared(mock(MediaPlayer.class));

        verify(mediaPlayer).start();
    }

    @Test
    public void onMediaPlayerError_sendOutFailedToPlayStreamBroadcast() {
        String expectedBroadcastIntentAction = Constants.Actions.FAILED_TO_PLAY_RADIO_STREAM;
        BroadcastReceiver receiver = buildMockLocalBroadcastReceiver(expectedBroadcastIntentAction);

        int irrelevantInt = 0;
        radioPlayerService.onError(mock(MediaPlayer.class), irrelevantInt, irrelevantInt);

        verifyThatReceiverReceivedExpectedBroadcast(receiver, expectedBroadcastIntentAction);
    }

    @Test
    public void onMediaPlayerError_resetMediaPlayer() {
        int irrelevantInt = 0;
        radioPlayerService.onError(mock(MediaPlayer.class), irrelevantInt, irrelevantInt);

        verify(mediaPlayer).reset();
    }

    @Test
    public void onMediaPlayerError_stopScheduledCreationOfPlayerNotifications() {
        int irrelevantInt = 0;
        radioPlayerService.onError(mock(MediaPlayer.class), irrelevantInt, irrelevantInt);

        verify(notificationFactory).stopScheduledCreationOfPlayerNotifications();
    }

    @Test
    public void onMediaPlayerError_indicateErrorHandled() {
        int irrelevantInt = 0;
        boolean wasErrorHandled = radioPlayerService.onError(mock(MediaPlayer.class), irrelevantInt, irrelevantInt);

        assertTrue(wasErrorHandled);
    }

    @Test
    public void onPlayerNotificationCreationSuccess_startServiceAsForegroundServiceWithTheGivenNotification() {
        ShadowService shadowService = shadowOf(radioPlayerService);
        Notification notification = mock(Notification.class);

        radioPlayerService.onPlayerNotificationCreationSuccess(notification);

        assertThat(shadowService.getLastForegroundNotification()).isSameAs(notification);
    }

    @Test
    public void onPlayerNotificationCreationFailed_sendOutFailureToPlayStreamBroadcast() {
        String expectedBroadcastIntentAction = Constants.Actions.FAILED_TO_PLAY_RADIO_STREAM;
        BroadcastReceiver receiver = buildMockLocalBroadcastReceiver(expectedBroadcastIntentAction);

        radioPlayerService.onPlayerNotificationCreationFailed();

        verifyThatReceiverReceivedExpectedBroadcast(receiver, expectedBroadcastIntentAction);
    }

    @Test
    public void onPlayerNotificationCreationFailed_resetMediaPlayer() {
        radioPlayerService.onPlayerNotificationCreationFailed();

        verify(mediaPlayer).reset();
    }

    @Test
    public void onPlayerNotificationCreationFailed_stopScheduledCreationOfPlayerNotifications() {
        radioPlayerService.onPlayerNotificationCreationFailed();

        verify(notificationFactory).stopScheduledCreationOfPlayerNotifications();
    }
}