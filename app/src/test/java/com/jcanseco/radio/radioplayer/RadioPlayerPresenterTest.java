package com.jcanseco.radio.radioplayer;

import com.jcanseco.radio.loaders.RadioContentLoader;
import com.jcanseco.radio.testfakes.FakeRadioContent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RadioPlayerPresenterTest {

    private RadioPlayerPresenter radioPlayerPresenter;

    @Mock
    private RadioContentLoader radioContentLoader;

    @Mock
    private RadioPlayerPresenter.View radioPlayerView;

    @Before
    public void setup() {
        radioPlayerPresenter = spy(new RadioPlayerPresenter(radioContentLoader));
        radioPlayerPresenter.attachView(radioPlayerView);
    }

    @Test
    public void onStart_shouldStartRadioPlayerService() {
        radioPlayerPresenter.onStart();

        verify(radioPlayerView).startRadioPlayerService();
    }

    @Test
    public void onStart_shouldBindToRadioPlayerService() {
        radioPlayerPresenter.onStart();

        verify(radioPlayerView).bindToRadioPlayerService();
    }

    @Test
    public void onStart_shouldRegisterBroadcastReceiverToListenForBroadcastIndicatingFailureToPlayStream() {
        radioPlayerPresenter.onStart();

        String expectedBroadcastIntentAction = "com.jcanseco.radio.constants.Constants.Actions.NOTIFY_USER_OF_FAILURE_TO_PLAY_RADIO_STREAM";
        verify(radioPlayerView).registerBroadcastReceiverToListenLocallyFor(expectedBroadcastIntentAction);
    }

    @Test
    public void onResume_shouldBeginActiveLoadingOfRadioContent() {
        radioPlayerPresenter.onResume();

        verify(radioContentLoader).beginActiveLoadingOfContent();
    }

    @Test
    public void onPause_shouldStopActiveLoadingOfRadioContent() {
        radioPlayerPresenter.onPause();

        verify(radioContentLoader).stopActiveLoadingOfContent();
    }

    @Test
    public void onStop_shouldUnbindFromRadioPlayerService() {
        radioPlayerPresenter.onStop();

        radioPlayerView.unbindFromRadioPlayerService();
    }

    @Test
    public void onStop_shouldUnregisterReceiver() {
        radioPlayerPresenter.onStop();

        radioPlayerView.unregisterBroadcastReceiver();
    }

    @Test
    public void radioPlayerServiceShouldNotBeConnectedByDefault() {
        assertThat(radioPlayerPresenter.isRadioPlayerServiceConnected()).isFalse();
    }

    @Test
    public void onRadioPlayerServiceConnected_ifServiceIsCurrentlyPlayingStream_thenShowPauseButton() {
        boolean isServiceCurrentlyPlayingStream = true;
        radioPlayerPresenter.onRadioPlayerServiceConnected(isServiceCurrentlyPlayingStream);

        verify(radioPlayerView).showPauseButton();
    }

    @Test
    public void onRadioPlayerServiceConnected_ifServiceIsCurrentlyPlayingStream_thenSetPlayerStateToPlaying() {
        boolean isServiceCurrentlyPlayingStream = true;
        radioPlayerPresenter.onRadioPlayerServiceConnected(isServiceCurrentlyPlayingStream);

        assertThat(radioPlayerPresenter.isPlayerPlaying()).isTrue();
    }

    @Test
    public void onRadioPlayerServiceConnected_ifServiceIsNotCurrentlyPlayingStream_thenShowPlayButton() {
        boolean isServiceCurrentlyPlayingStream = false;
        radioPlayerPresenter.onRadioPlayerServiceConnected(isServiceCurrentlyPlayingStream);

        verify(radioPlayerView).showPlayButton();
    }

    @Test
    public void onRadioPlayerServiceConnected_ifServiceIsNotCurrentlyPlayingStream_thenSetPlayerStateToPaused() {
        boolean isServiceCurrentlyPlayingStream = false;
        radioPlayerPresenter.onRadioPlayerServiceConnected(isServiceCurrentlyPlayingStream);

        assertThat(radioPlayerPresenter.isPlayerPlaying()).isFalse();
    }

    @Test
    public void onRadioPlayerServiceConnected_shouldIndicateThatServiceIsConnected_regardlessOfWhetherServiceIsCurrentlyPlayingStream() {
        RadioPlayerPresenter radioPlayerPresenter1 = spy(new RadioPlayerPresenter(mock(RadioContentLoader.class)));
        RadioPlayerPresenter radioPlayerPresenter2 = spy(new RadioPlayerPresenter(mock(RadioContentLoader.class)));
        radioPlayerPresenter1.attachView(mock(RadioPlayerPresenter.View.class));
        radioPlayerPresenter2.attachView(mock(RadioPlayerPresenter.View.class));

        radioPlayerPresenter1.onRadioPlayerServiceConnected(true);
        radioPlayerPresenter2.onRadioPlayerServiceConnected(false);

        assertThat(radioPlayerPresenter1.isRadioPlayerServiceConnected()).isTrue();
        assertThat(radioPlayerPresenter2.isRadioPlayerServiceConnected()).isTrue();
    }

    @Test
    public void onRadioPlayerServiceDisconnected_shouldIndicateThatServiceIsNotConnected() {
        radioPlayerPresenter.onRadioPlayerServiceDisconnected();

        assertThat(radioPlayerPresenter.isRadioPlayerServiceConnected()).isFalse();
    }

    @Test
    public void playerShouldBePausedByDefault() {
        assertThat(radioPlayerPresenter.isPlayerPlaying()).isFalse();
    }

    @Test
    public void onActionButtonClicked_ifPlayerIsPlaying_thenPausePlayer() {
        when(radioPlayerPresenter.isPlayerPlaying()).thenReturn(true);

        radioPlayerPresenter.onActionButtonClicked();

        verify(radioPlayerPresenter).pausePlayer();
    }

    @Test
    public void onActionButtonClicked_ifPlayerIsPaused_thenPlayPlayer() {
        when(radioPlayerPresenter.isPlayerPlaying()).thenReturn(false);

        radioPlayerPresenter.onActionButtonClicked();

        verify(radioPlayerPresenter).playPlayer();
    }

    @Test
    public void whenPlayerPaused_ifRadioPlayerServiceConnected_thenShowPlayButton() {
        when(radioPlayerPresenter.isRadioPlayerServiceConnected()).thenReturn(true);

        radioPlayerPresenter.pausePlayer();

        verify(radioPlayerView).showPlayButton();
    }

    @Test
    public void whenPlayerPaused_ifRadioPlayerServiceConnected_thenStopPlayingRadioStream() {
        when(radioPlayerPresenter.isRadioPlayerServiceConnected()).thenReturn(true);

        radioPlayerPresenter.pausePlayer();

        verify(radioPlayerView).stopPlayingRadioStream();
    }

    @Test
    public void whenPlayerPaused_ifRadioPlayerServiceConnected_thenChangePlayerStateToPaused() {
        when(radioPlayerPresenter.isRadioPlayerServiceConnected()).thenReturn(true);

        radioPlayerPresenter.pausePlayer();

        assertThat(radioPlayerPresenter.isPlayerPlaying()).isFalse();
    }

    @Test
    public void whenPlayerPaused_ifRadioPlayerServiceNotConnected_thenDontManipulateTheView() {
        when(radioPlayerPresenter.isRadioPlayerServiceConnected()).thenReturn(false);

        radioPlayerPresenter.pausePlayer();

        verify(radioPlayerView, never()).showPlayButton();
        verify(radioPlayerView, never()).stopPlayingRadioStream();
    }

    @Test
    public void whenPlayerPaused_ifRadioPlayerServiceNotConnected_thenDontChangePlayerState() {
        when(radioPlayerPresenter.isRadioPlayerServiceConnected()).thenReturn(false);
        boolean oldValue = radioPlayerPresenter.isPlayerPlaying();

        radioPlayerPresenter.pausePlayer();

        boolean newValue = radioPlayerPresenter.isPlayerPlaying();
        assertEquals(oldValue, newValue);
    }

    @Test
    public void whenPlayerPlayed_ifServiceConnected_andRadioStreamUrlNotNull_thenShowPauseButton() {
        when(radioPlayerPresenter.isRadioPlayerServiceConnected()).thenReturn(true);
        when(radioPlayerPresenter.getStreamUrl()).thenReturn("https://streamurl.com");

        radioPlayerPresenter.playPlayer();

        verify(radioPlayerView).showPauseButton();
    }

    @Test
    public void whenPlayerPlayed_ifServiceConnected_andRadioStreamUrlNotNull_thenStartPlayingRadioStream() {
        when(radioPlayerPresenter.isRadioPlayerServiceConnected()).thenReturn(true);
        when(radioPlayerPresenter.getStreamUrl()).thenReturn("https://streamurl.com");

        radioPlayerPresenter.playPlayer();

        verify(radioPlayerView).startPlayingRadioStream("https://streamurl.com");
    }

    @Test
    public void whenPlayerPlayed_ifServiceConnected_andRadioStreamUrlNotNull_thenChangePlayerStateToPlaying() {
        when(radioPlayerPresenter.isRadioPlayerServiceConnected()).thenReturn(true);
        when(radioPlayerPresenter.getStreamUrl()).thenReturn("https://streamurl.com");

        radioPlayerPresenter.playPlayer();

        assertThat(radioPlayerPresenter.isPlayerPlaying()).isTrue();
    }

    @Test
    public void whenPlayerPlayed_ifServiceConnected_andRadioStreamUrlIsNull_thenDontShowPauseButton() {
        when(radioPlayerPresenter.isRadioPlayerServiceConnected()).thenReturn(true);
        when(radioPlayerPresenter.getStreamUrl()).thenReturn(null);

        radioPlayerPresenter.playPlayer();

        verify(radioPlayerView, never()).showPauseButton();
    }

    @Test
    public void whenPlayerPlayed_ifServiceConnected_andRadioStreamUrlIsNull_thenDontStartPlayingRadioStream() {
        when(radioPlayerPresenter.isRadioPlayerServiceConnected()).thenReturn(true);
        when(radioPlayerPresenter.getStreamUrl()).thenReturn(null);

        radioPlayerPresenter.playPlayer();

        verify(radioPlayerView, never()).startPlayingRadioStream(anyString());
    }

    @Test
    public void whenPlayerPlayed_ifServiceConnected_andRadioStreamUrlIsNull_thenDontChangePlayerState() {
        when(radioPlayerPresenter.isRadioPlayerServiceConnected()).thenReturn(true);
        when(radioPlayerPresenter.getStreamUrl()).thenReturn(null);
        boolean oldValue = radioPlayerPresenter.isPlayerPlaying();

        radioPlayerPresenter.playPlayer();

        boolean newValue = radioPlayerPresenter.isPlayerPlaying();
        assertEquals(oldValue, newValue);
    }

    @Test
    public void whenPlayerPlayed_ifServiceConnected_andRadioStreamUrlIsNull_thenShowCouldNotPlayRadioStreamErrorMessage() {
        when(radioPlayerPresenter.isRadioPlayerServiceConnected()).thenReturn(true);
        when(radioPlayerPresenter.getStreamUrl()).thenReturn(null);

        radioPlayerPresenter.playPlayer();

        verify(radioPlayerView).showCouldNotPlayRadioStreamErrorMessage();
    }

    @Test
    public void whenPlayerPlayed_ifServiceNotConnected_thenDontManipulateTheView() {
        when(radioPlayerPresenter.isRadioPlayerServiceConnected()).thenReturn(false);

        radioPlayerPresenter.playPlayer();

        verify(radioPlayerView, never()).showPauseButton();
        verify(radioPlayerView, never()).startPlayingRadioStream("http://streamurl.com");
        verify(radioPlayerView, never()).showCouldNotPlayRadioStreamErrorMessage();
    }

    @Test
    public void whenPlayerPlayed_ifServiceNotConnected_thenDontChangePlayerState() {
        when(radioPlayerPresenter.isRadioPlayerServiceConnected()).thenReturn(false);
        boolean oldValue = radioPlayerPresenter.isPlayerPlaying();

        radioPlayerPresenter.playPlayer();

        boolean newValue = radioPlayerPresenter.isPlayerPlaying();
        assertEquals(oldValue, newValue);
    }

    @Test
    public void onRadioContentLoadSuccess_shouldShowCurrentTrackTitle() {
        FakeRadioContent radioContent = new FakeRadioContent();

        radioPlayerPresenter.onRadioContentLoadSuccess(radioContent);

        verify(radioPlayerView).showCurrentTrackTitle("current track title");
    }

    @Test
    public void onRadioContentLoadSuccess_shouldShowCurrentDjName() {
        FakeRadioContent radioContent = new FakeRadioContent();

        radioPlayerPresenter.onRadioContentLoadSuccess(radioContent);

        verify(radioPlayerView).showCurrentDjName("current dj name");
    }

    @Test
    public void onRadioContentLoadSuccess_shouldShowNumOfListeners() {
        FakeRadioContent radioContent = new FakeRadioContent();

        radioPlayerPresenter.onRadioContentLoadSuccess(radioContent);

        verify(radioPlayerView).showNumOfListeners(253);
    }

    @Test
    public void onRadioContentLoadSuccess_shouldSaveRadioStreamUrl() {
        FakeRadioContent radioContent = new FakeRadioContent();

        radioPlayerPresenter.onRadioContentLoadSuccess(radioContent);

        verify(radioPlayerPresenter).setRadioStreamUrl("https://streamurl.com");
        assertThat(radioPlayerPresenter.getStreamUrl()).isEqualTo("https://streamurl.com");
    }

    @Test
    public void onRadioContentLoadFailed_shouldShowCouldNotLoadRadioContentErrorMessage() {
        radioPlayerPresenter.onRadioContentLoadFailed();

        verify(radioPlayerView).showCouldNotLoadRadioContentErrorMessage();
    }

    @Test
    public void onRadioContentLoadFailed_shouldPausePlayer() {
        radioPlayerPresenter.onRadioContentLoadFailed();

        verify(radioPlayerPresenter).pausePlayer();
    }

    @Test
    public void onFailedToPlayStreamBroadcastReceived_shouldShowCouldNotPlayRadioStreamErrorMessage() {
        radioPlayerPresenter.onFailedToPlayStreamBroadcastReceived();

        verify(radioPlayerView).showCouldNotPlayRadioStreamErrorMessage();
    }

    @Test
    public void onFailedToPlayStreamBroadcastReceived_shouldPausePlayer() {
        radioPlayerPresenter.onFailedToPlayStreamBroadcastReceived();

        verify(radioPlayerPresenter).pausePlayer();
    }
}