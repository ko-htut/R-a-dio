package com.jcanseco.radio.services;

import android.content.BroadcastReceiver;
import android.content.Intent;

import com.jcanseco.radio.BuildConfig;
import com.jcanseco.radio.constants.Constants;
import com.jcanseco.radio.players.RadioPlayer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.ServiceController;

import static com.jcanseco.radio.testutilities.BroadcastTestingUtilities.buildMockLocalBroadcastReceiver;
import static com.jcanseco.radio.testutilities.BroadcastTestingUtilities.verifyThatReceiverReceivedExpectedBroadcast;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = "src/main/AndroidManifest.xml")
public class RadioPlayerServiceTest {

    private RadioPlayerService radioPlayerService;
    private ServiceController<RadioPlayerService> serviceController;

    private RadioPlayer radioPlayer;

    @Before
    public void setup() {
        ShadowApplication.getInstance().clearStartedServices();

        serviceController = Robolectric.buildService(RadioPlayerService.class);
        radioPlayerService = serviceController.attach().create().get();

        radioPlayer = mock(RadioPlayer.class);
        radioPlayer.setRadioPlayerListener(radioPlayerService);
        radioPlayerService.radioPlayer = radioPlayer;
    }

    @After
    public void teardown() {
        serviceController.destroy();
    }

    @Test
    public void testThatServiceCanBeBound() {
        RadioPlayerService.RadioPlayerBinder binder = (RadioPlayerService.RadioPlayerBinder) radioPlayerService.onBind(mock(Intent.class));

        assertThat(binder).isNotNull();
        assertThat(binder.getService()).isEqualTo(radioPlayerService);
    }

    @Test
    public void onDestroy_shouldReleaseRadioPlayer() {
        serviceController.destroy();

        verify(radioPlayer).release();
    }

    @Test
    public void whenIsPlayingStreamInvoked_ifRadioPlayerIsPlaying_thenReturnTrue() {
        when(radioPlayer.isPlaying()).thenReturn(true);

        assertThat(radioPlayerService.isPlayingStream()).isTrue();
    }

    @Test
    public void whenIsPlayingStreamInvoked_ifRadioPlayerIsNotPlaying_thenReturnFalse() {
        when(radioPlayer.isPlaying()).thenReturn(false);

        assertThat(radioPlayerService.isPlayingStream()).isFalse();
    }

    @Test
    public void whenStartPlayingRadioStreamInvoked_playRadioPlayer() throws Exception {
        radioPlayerService.startPlayingRadioStream();

        verify(radioPlayer).play();
    }

    @Test
    public void whenStopPlayingRadioStreamInvoked_pauseRadioPlayer() {
        radioPlayerService.stopPlayingRadioStream();

        verify(radioPlayer).pause();
    }

    @Test
    public void onRadioPlayerStreamError_shouldSendOutFailureToPlayStreamBroadcast() throws Exception {
        String expectedBroadcastIntentAction = Constants.Actions.FAILED_TO_PLAY_RADIO_STREAM;
        BroadcastReceiver receiver = buildMockLocalBroadcastReceiver(expectedBroadcastIntentAction);

        radioPlayerService.onRadioPlayerStreamError();

        verifyThatReceiverReceivedExpectedBroadcast(receiver, expectedBroadcastIntentAction);
    }
}