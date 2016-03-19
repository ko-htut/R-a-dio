package com.jcanseco.radio.api;

public class RestClient {

    private static RadioRestService radioRestService;

    public static RadioRestService getRadioRestService() {
        if (radioRestService == null) {
            radioRestService = RadioRestService.Factory.create();
        }
        return radioRestService;
    }
}
