package com.jcanseco.radio.radioplayer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jcanseco.radio.BuildConfig;
import com.jcanseco.radio.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.util.ActivityController;

import java.io.IOException;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = "src/main/AndroidManifest.xml")
public class RadioPlayerActivityTest {

    private ActivityController<RadioPlayerActivity> activityController;
    private RadioPlayerActivity radioPlayerActivity;
    private RadioPlayerPresenter radioPlayerPresenter;

    @Before
    public void setup() {
        activityController = Robolectric.buildActivity(RadioPlayerActivity.class);
        radioPlayerActivity = activityController.create().get();

        radioPlayerPresenter = mock(RadioPlayerPresenter.class);
        radioPlayerActivity.radioPlayerPresenter = radioPlayerPresenter;
    }

    @After
    public void teardown() {
        activityController.pause().userLeaving().stop().destroy();
    }

    @Test
    public void whenResumed_shouldNotifyPresenter() {
        activityController.start().resume();

        verify(radioPlayerPresenter).onResume();
    }

    @Test
    public void whenPaused_shouldNotifyPresenter() {
        activityController.start().resume().visible().pause();

        verify(radioPlayerPresenter).onPause();
    }

    @Test
    public void onActionButtonClicked_shouldNotifyPresenter() {
        activityController.start().resume().visible();
        View actionButtonView = radioPlayerActivity.findViewById(R.id.action_button);

        actionButtonView.callOnClick();

        verify(radioPlayerPresenter).onActionButtonClicked();
    }

    @Test
    public void testShowPlayButton() {
        activityController.start().resume().visible();
        Button actionButton = mock(Button.class);
        radioPlayerActivity.actionButton = actionButton;

        radioPlayerActivity.showPlayButton();

        verify(actionButton).setBackgroundResource(R.drawable.play);
    }

    @Test
    public void testShowPauseButton() {
        activityController.start().resume().visible();
        Button actionButton = mock(Button.class);
        radioPlayerActivity.actionButton = actionButton;

        radioPlayerActivity.showPauseButton();

        verify(actionButton).setBackgroundResource(R.drawable.pause);
    }

    @Test
    public void testShowCurrentTrackTitle() {
        activityController.start().resume().visible();
        TextView trackTitleTextView = (TextView) radioPlayerActivity.findViewById(R.id.track_title);

        radioPlayerActivity.showCurrentTrackTitle("track title");

        assertThat(trackTitleTextView).isNotNull()
                .isVisible()
                .containsText("track title");
    }

    @Test
    public void testShowCurrentDjName() {
        activityController.start().resume().visible();
        TextView djNameTextView = (TextView) radioPlayerActivity.findViewById(R.id.dj_name);

        radioPlayerActivity.showCurrentDjName("Hanyuu-sama");

        assertThat(djNameTextView).isNotNull()
                .isVisible()
                .containsText("Hanyuu-sama");
    }

    @Test
    public void testShowNumOfListeners() {
        activityController.start().resume().visible();
        TextView numOfListeners = (TextView) radioPlayerActivity.findViewById(R.id.num_of_listeners);

        radioPlayerActivity.showNumOfListeners(255);

        assertThat(numOfListeners).isNotNull()
                .isVisible()
                .containsText("255 Listeners");
    }

    @Test
    public void whenStartPlayingRadioStreamInvoked_mediaPlayerShouldBeSetUpForStreamingMusic_andShouldBePreparedForAsyncOperation() throws Exception {
        activityController.start().resume().visible();
        MediaPlayer mediaPlayer = mock(MediaPlayer.class);
        radioPlayerActivity.mediaPlayer = mediaPlayer;

        radioPlayerActivity.startPlayingRadioStream("http://streamurl.com");

        verify(mediaPlayer).setAudioStreamType(AudioManager.STREAM_MUSIC);
        verify(mediaPlayer).setOnPreparedListener(any(MediaPlayer.OnPreparedListener.class));
        verify(mediaPlayer).setOnErrorListener(any(MediaPlayer.OnErrorListener.class));
        verify(mediaPlayer).setDataSource("http://streamurl.com");
        verify(mediaPlayer).prepareAsync();
    }

    @Test
    public void whenStartPlayingRadioStreamInvoked_ifIOExceptionThrown_thenShowCouldNotPlayRadioStreamErrorMessage() throws Exception {
        activityController.start().resume().visible();
        MediaPlayer mediaPlayer = mock(MediaPlayer.class);
        doThrow(new IOException()).when(mediaPlayer).setDataSource(anyString());
        radioPlayerActivity.mediaPlayer = mediaPlayer;

        radioPlayerActivity.startPlayingRadioStream("http://streamurl.com");

        assertToastDisplayed("Stream unavailable. Try again later.");
    }

    @Test
    public void whenStartPlayingRadioStreamInvoked_ifIllegalStateExceptionThrown_thenShowCouldNotPlayRadioStreamErrorMessage() throws Exception {
        activityController.start().resume().visible();
        MediaPlayer mediaPlayer = mock(MediaPlayer.class);
        doThrow(new IllegalStateException()).when(mediaPlayer).prepareAsync();
        radioPlayerActivity.mediaPlayer = mediaPlayer;

        radioPlayerActivity.startPlayingRadioStream("http://streamurl.com");

        assertToastDisplayed("Stream unavailable. Try again later.");
    }

    @Test
    public void whenStopPlayingRadioStreamInvoked_stopAndResetMediaPlayer() {
        activityController.start().resume().visible();
        MediaPlayer mediaPlayer = mock(MediaPlayer.class);
        radioPlayerActivity.mediaPlayer = mediaPlayer;

        radioPlayerActivity.stopPlayingRadioStream();

        verify(mediaPlayer).stop();
        verify(mediaPlayer).reset();
    }

    @Test
    public void testOnPreparedListenerForMediaPlayer() {
        MediaPlayer mediaPlayer = mock(MediaPlayer.class);
        radioPlayerActivity.mediaPlayer = mediaPlayer;
        ArgumentCaptor argumentCaptor = ArgumentCaptor.forClass(MediaPlayer.OnPreparedListener.class);

        radioPlayerActivity.startPlayingRadioStream("http://streamurl.com");

        verify(mediaPlayer).setOnPreparedListener((MediaPlayer.OnPreparedListener) argumentCaptor.capture());
        MediaPlayer.OnPreparedListener onPreparedListener = (MediaPlayer.OnPreparedListener) argumentCaptor.getValue();
        onPreparedListener.onPrepared(mock(MediaPlayer.class));

        verify(mediaPlayer).start();
    }

    @Test
    public void testOnErrorListenerForMediaPlayer() {
        MediaPlayer mediaPlayer = mock(MediaPlayer.class);
        radioPlayerActivity.mediaPlayer = mediaPlayer;
        ArgumentCaptor argumentCaptor = ArgumentCaptor.forClass(MediaPlayer.OnErrorListener.class);

        radioPlayerActivity.startPlayingRadioStream("http://streamurl.com");

        verify(mediaPlayer).setOnErrorListener((MediaPlayer.OnErrorListener) argumentCaptor.capture());
        MediaPlayer.OnErrorListener onErrorListener = (MediaPlayer.OnErrorListener) argumentCaptor.getValue();
        int irrelevantIntParam = 0;
        onErrorListener.onError(mock(MediaPlayer.class), irrelevantIntParam, irrelevantIntParam);

        assertToastDisplayed("Stream unavailable. Try again later.");
        verify(mediaPlayer).reset();
    }

    @Test
    public void testShowCouldNotLoadRadioContentErrorMessage() {
        activityController.start().resume().visible();

        radioPlayerActivity.showCouldNotLoadRadioContentErrorMessage();

        assertToastDisplayed("Failed to load. Try again later.");
    }

    @Test
    public void testShowCouldNotPlayRadioStreamErrorMessage() {
        activityController.start().resume().visible();

        radioPlayerActivity.showCouldNotPlayRadioStreamErrorMessage();

        assertToastDisplayed("Stream unavailable. Try again later.");
    }

    private void assertToastDisplayed(String expectedToastText) {
        String actualToastText = ShadowToast.getTextOfLatestToast();
        assertThat(actualToastText).isEqualTo(expectedToastText);
    }
}