package com.jcanseco.radio.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Track implements Serializable {

    @SerializedName("meta")
    protected String title;


    public Track(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
