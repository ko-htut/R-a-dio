package com.jcanseco.radio.radioplayer;

import com.jcanseco.radio.loaders.RadioContentLoader;
import com.jcanseco.radio.models.fakes.FakeRadioContent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.anyString;
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
    public void whenPlayerPaused_thenShowPlayButton() {
        radioPlayerPresenter.pausePlayer();

        verify(radioPlayerView).showPlayButton();
    }

    @Test
    public void whenPlayerPaused_thenChangePlayerStateToPaused() {
        radioPlayerPresenter.pausePlayer();

        assertThat(radioPlayerPresenter.isPlayerPlaying()).isFalse();
    }

    @Test
    public void whenPlayerPlayed_andStreamUrlNotNull_thenChangePlayerStateToPlaying() {
        when(radioPlayerPresenter.getStreamUrl()).thenReturn("https://streamurl.com");

        radioPlayerPresenter.playPlayer();

        assertThat(radioPlayerPresenter.isPlayerPlaying()).isTrue();
    }

    @Test
    public void whenPlayerPlayed_andRadioStreamUrlNotNull_thenDontChangePlayerStateToPlaying() {
        when(radioPlayerPresenter.getStreamUrl()).thenReturn(null);

        radioPlayerPresenter.playPlayer();

        assertThat(radioPlayerPresenter.isPlayerPlaying()).isFalse();
    }

    @Test
    public void whenPlayerPlayed_andRadioStreamUrlNotNull_thenShowPauseButton() {
        when(radioPlayerPresenter.getStreamUrl()).thenReturn("https://streamurl.com");

        radioPlayerPresenter.playPlayer();

        verify(radioPlayerView).showPauseButton();
    }

    @Test
    public void whenPlayerPlayed_andRadioStreamUrlIsNull_thenDontShowPauseButton() {
        when(radioPlayerPresenter.getStreamUrl()).thenReturn(null);

        radioPlayerPresenter.playPlayer();

        verify(radioPlayerView, never()).showPauseButton();
    }

    @Test
    public void whenPlayerPlayed_andRadioStreamUrlNotNull_thenLoadRadioStreamOntoView() {
        when(radioPlayerPresenter.getStreamUrl()).thenReturn("https://streamurl.com");

        radioPlayerPresenter.playPlayer();

        verify(radioPlayerView).loadRadioStream("https://streamurl.com");
    }

    @Test
    public void whenPlayerPlayed_andRadioStreamUrlIsNull_thenDontLoadRadioStreamOntoView() {
        when(radioPlayerPresenter.getStreamUrl()).thenReturn(null);

        radioPlayerPresenter.playPlayer();

        verify(radioPlayerView, never()).loadRadioStream(anyString());
    }

    @Test
    public void whenPlayerPlayed_andRadioStreamUrlIsNull_thenShowCouldNotLoadRadioStreamErrorMessage() {
        when(radioPlayerPresenter.getStreamUrl()).thenReturn(null);

        radioPlayerPresenter.playPlayer();

        verify(radioPlayerView).showCouldNotLoadRadioStreamErrorMessage();
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
    public void onRadioContentLoadSuccess_shouldShowCurrentDjAvatar() {
        FakeRadioContent radioContent = new FakeRadioContent();

        radioPlayerPresenter.onRadioContentLoadSuccess(radioContent);

        verify(radioPlayerView).showCurrentDjAvatar("http://r-a-d.io/api/dj-image/20");
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
}