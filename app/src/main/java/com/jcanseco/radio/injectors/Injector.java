package com.jcanseco.radio.injectors;

import com.jcanseco.radio.api.RadioRestService;
import com.jcanseco.radio.api.RestClient;
import com.jcanseco.radio.loaders.RadioContentLoader;
import com.jcanseco.radio.radioplayer.RadioPlayerPresenter;

public class Injector {

    public static RadioRestService provideRadioRestService() {
        return RestClient.getRadioRestService();
    }

    public static RadioContentLoader provideRadioContentLoader() {
        return new RadioContentLoader(provideRadioRestService());
    }

    public static RadioPlayerPresenter provideRadioPlayerPresenter() {
        return new RadioPlayerPresenter();
    }
}
