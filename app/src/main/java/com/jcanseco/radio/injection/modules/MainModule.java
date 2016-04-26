package com.jcanseco.radio.injection.modules;

import com.jcanseco.radio.MainApplication;
import com.jcanseco.radio.api.RadioRestService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class MainModule {

    private MainApplication application;

    public MainModule(MainApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    MainApplication provideApplication() {
        return application;
    }

    @Provides
    @Singleton
    RadioRestService provideRadioRestService() {
        return RadioRestService.Factory.create();
    }
}
