package com.jcanseco.radio.models;

import com.google.gson.annotations.SerializedName;

public class RadioContentMeta {

    @SerializedName("stream")
    private String streamUrl;


    public String getStreamUrl() {
        return streamUrl;
    }
}
