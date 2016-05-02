package com.jcanseco.radio.injection.modules;

import com.jcanseco.radio.api.RadioRestService;
import com.jcanseco.radio.loaders.RadioContentLoader;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class NetworkModule {

    @Provides
    @Singleton
    RadioRestService provideRadioRestService() {
        return RadioRestService.Factory.create();
    }

    @Provides
    RadioContentLoader provideRadioContentLoader(RadioRestService radioRestService) {
        return new RadioContentLoader(radioRestService);
    }
}
