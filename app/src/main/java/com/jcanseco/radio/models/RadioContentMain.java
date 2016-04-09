package com.jcanseco.radio.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class RadioContentMain implements Serializable {

    @SerializedName("np")
    private String currentTrackTitle;

    @SerializedName("start_time")
    private long currentTrackStartTimeInUnixTime;

    @SerializedName("end_time")
    private long currentTrackEndTimeInUnixTime;

    @SerializedName("dj")
    private Dj currentDj;

    @SerializedName("listeners")
    private int numOfListeners;

    @SerializedName("queue")
    private List<Track> queuedTracks;

    @SerializedName("lp")
    private List<Track> lastPlayedTracks;

    @SerializedName("thread")
    private String discussionThreadUrl;


    public NowPlayingTrack getCurrentTrack() {
        return new NowPlayingTrack(currentTrackTitle, currentTrackStartTimeInUnixTime, currentTrackEndTimeInUnixTime);
    }

    public Dj getCurrentDj() {
        return currentDj;
    }

    public int getNumOfListeners() {
        return numOfListeners;
    }

    public List<Track> getQueuedTracks() {
        return queuedTracks;
    }

    public List<Track> getLastPlayedTracks() {
        return lastPlayedTracks;
    }

    public String getDiscussionThreadUrl() {
        return !discussionThreadUrl.equals("none") ? discussionThreadUrl : null;
    }
}
