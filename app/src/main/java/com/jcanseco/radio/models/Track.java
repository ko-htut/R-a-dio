package com.jcanseco.radio.models;

import com.google.gson.annotations.SerializedName;

public class Track {

    @SerializedName("meta")
    protected String title;


    public Track(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
