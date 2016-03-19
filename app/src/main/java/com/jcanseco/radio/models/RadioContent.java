package com.jcanseco.radio.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RadioContent {

    @SerializedName("main")
    private RadioContentMain radioContentMain;

    @SerializedName("meta")
    private RadioContentMeta radioContentMeta;


    public NowPlayingTrack getCurrentTrack() {
        return radioContentMain.getCurrentTrack();
    }

    public Dj getCurrentDj() {
        return radioContentMain.getCurrentDj();
    }

    public int getNumOfListeners() {
        return radioContentMain.getNumOfListeners();
    }

    public List<Track> getQueuedTracks() {
        return radioContentMain.getQueuedTracks();
    }

    public List<Track> getLastPlayedTracks() {
        return radioContentMain.getLastPlayedTracks();
    }

    public String getDiscussionThreadUrl() {
        return radioContentMain.getDiscussionThreadUrl();
    }

    public String getStreamUrl() {
        return radioContentMeta.getStreamUrl();
    }
}
