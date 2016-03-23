package com.jcanseco.radio.radioplayer;

import com.jcanseco.radio.loaders.RadioContentLoader;
import com.jcanseco.radio.models.Dj;
import com.jcanseco.radio.models.NowPlayingTrack;
import com.jcanseco.radio.models.RadioContent;

public class RadioPlayerPresenter implements RadioContentLoader.RadioContentListener {

    private RadioPlayerPresenter.View radioPlayerView;
    private RadioContentLoader radioContentLoader;

    private boolean isPlayerPlaying = false;
    private String radioStreamUrl = null;

    public RadioPlayerPresenter(RadioContentLoader radioContentLoader) {
        this.radioContentLoader = radioContentLoader;
        this.radioContentLoader.setRadioContentListener(this);
    }

    public void attachView(RadioPlayerPresenter.View radioPlayerView) {
        this.radioPlayerView = radioPlayerView;
    }

    public void onResume() {
        radioContentLoader.beginActiveLoadingOfContent();
    }

    public void onPause() {
        radioContentLoader.stopActiveLoadingOfContent();
    }

    public void onActionButtonClicked() {
        if (isPlayerPlaying()) {
            pausePlayer();
        } else {
            playPlayer();
        }
    }

    protected boolean isPlayerPlaying() {
        return isPlayerPlaying;
    }

    protected void pausePlayer() {
        radioPlayerView.showPlayButton();
        isPlayerPlaying = false;
    }

    protected void playPlayer() {
        String streamUrl = getStreamUrl();
        if (streamUrl != null) {
            radioPlayerView.showPauseButton();
            radioPlayerView.loadRadioStream(streamUrl);
            isPlayerPlaying = true;
        } else {
            radioPlayerView.showCouldNotLoadRadioStreamErrorMessage();
        }
    }

    @Override
    public void onRadioContentLoadSuccess(RadioContent radioContent) {
        NowPlayingTrack currentTrack = radioContent.getCurrentTrack();
        Dj currentDj = radioContent.getCurrentDj();

        radioPlayerView.showCurrentTrackTitle(currentTrack.getTitle());
        radioPlayerView.showCurrentDjName(currentDj.getName());
        radioPlayerView.showCurrentDjAvatar(currentDj.getAvatarUrl());
        radioPlayerView.showNumOfListeners(radioContent.getNumOfListeners());

        setRadioStreamUrl(radioContent.getStreamUrl());
    }

    @Override
    public void onRadioContentLoadFailed() {
        radioPlayerView.showCouldNotLoadRadioContentErrorMessage();
        pausePlayer();
    }

    protected String getStreamUrl() {
        return radioStreamUrl;
    }

    protected void setRadioStreamUrl(String radioStreamUrl) {
        this.radioStreamUrl = radioStreamUrl;
    }


    public interface View {

        void showPlayButton();

        void showPauseButton();

        void showCurrentTrackTitle(String title);

        void showCurrentDjName(String name);

        void showCurrentDjAvatar(String avatarUrl);

        void showNumOfListeners(int numOfListeners);

        void loadRadioStream(String streamUrl);

        void showCouldNotLoadRadioContentErrorMessage();

        void showCouldNotLoadRadioStreamErrorMessage();
    }
}
