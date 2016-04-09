package com.jcanseco.radio.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RadioContentMeta implements Serializable {

    @SerializedName("stream")
    private String streamUrl;


    public String getStreamUrl() {
        return streamUrl;
    }
}
