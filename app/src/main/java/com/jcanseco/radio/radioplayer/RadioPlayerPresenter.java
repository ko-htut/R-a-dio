package com.jcanseco.radio.radioplayer;

import com.jcanseco.radio.models.Dj;
import com.jcanseco.radio.models.NowPlayingTrack;
import com.jcanseco.radio.models.RadioContent;

public class RadioPlayerPresenter {

    private RadioPlayerPresenter.View radioPlayerView;

    private boolean isPlayerPlaying;
    private String radioStreamUrl;

    private boolean isRadioContentServiceConnected;
    private boolean isRadioPlayerServiceConnected;

    public void attachView(RadioPlayerPresenter.View radioPlayerView) {
        this.radioPlayerView = radioPlayerView;
    }

    public void onStart() {
        radioPlayerView.startServices();
        radioPlayerView.bindToServices();
        radioPlayerView.registerBroadcastReceivers();
    }

    public void onStop() {
        radioPlayerView.unbindFromServices();
        radioPlayerView.unregisterBroadcastReceivers();
    }

    public void onRadioContentServiceConnected() {
        isRadioContentServiceConnected = true;
    }

    public void onRadioContentServiceDisconnected() {
        isRadioContentServiceConnected = false;
    }

    public void onRadioPlayerServiceConnected(boolean isServiceCurrentlyPlayingStream) {
        if (isServiceCurrentlyPlayingStream) {
            setPlayerStateAsPlaying();
        } else {
            setPlayerStateAsPaused();
        }
        isRadioPlayerServiceConnected = true;
    }

    public void onRadioPlayerServiceDisconnected() {
        isRadioPlayerServiceConnected = false;
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
        if (isRadioPlayerServiceConnected()) {
            radioPlayerView.stopPlayingRadioStream();
            setPlayerStateAsPaused();
        }
    }

    protected void playPlayer() {
        if (isRadioPlayerServiceConnected()) {
            String streamUrl = getStreamUrl();
            if (streamUrl != null) {
                radioPlayerView.startPlayingRadioStream(streamUrl);
                setPlayerStateAsPlaying();
            } else {
                radioPlayerView.showCouldNotPlayRadioStreamErrorMessage();
            }
        }
    }

    public void onRadioContentLoadSuccess(RadioContent radioContent) {
        NowPlayingTrack currentTrack = radioContent.getCurrentTrack();
        Dj currentDj = radioContent.getCurrentDj();

        radioPlayerView.showCurrentTrackTitle(currentTrack.getTitle());
        radioPlayerView.showCurrentDjName(currentDj.getName());
        radioPlayerView.showNumOfListeners(radioContent.getNumOfListeners());

        setRadioStreamUrl(radioContent.getStreamUrl());
    }

    public void onRadioContentLoadFailed() {
        radioPlayerView.showCouldNotLoadRadioContentErrorMessage();
        pausePlayer();
    }

    public void onFailedToPlayStreamBroadcastReceived() {
        radioPlayerView.showCouldNotPlayRadioStreamErrorMessage();
        pausePlayer();
    }

    protected boolean isRadioContentServiceConnected() {
        return isRadioContentServiceConnected;
    }

    protected boolean isRadioPlayerServiceConnected() {
        return isRadioPlayerServiceConnected;
    }

    protected String getStreamUrl() {
        return radioStreamUrl;
    }

    protected void setRadioStreamUrl(String radioStreamUrl) {
        this.radioStreamUrl = radioStreamUrl;
    }

    private void setPlayerStateAsPaused() {
        radioPlayerView.showPlayButton();
        isPlayerPlaying = false;
    }

    private void setPlayerStateAsPlaying() {
        radioPlayerView.showPauseButton();
        isPlayerPlaying = true;
    }


    public interface View {

        void startServices();

        void bindToServices();

        void unbindFromServices();

        void registerBroadcastReceivers();

        void unregisterBroadcastReceivers();

        void showPlayButton();

        void showPauseButton();

        void showCurrentTrackTitle(String title);

        void showCurrentDjName(String name);

        void showNumOfListeners(int numOfListeners);

        void startPlayingRadioStream(String streamUrl);

        void stopPlayingRadioStream();

        void showCouldNotLoadRadioContentErrorMessage();

        void showCouldNotPlayRadioStreamErrorMessage();
    }
}
