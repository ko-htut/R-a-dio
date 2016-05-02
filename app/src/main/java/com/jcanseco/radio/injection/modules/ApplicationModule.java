package com.jcanseco.radio.injection.modules;

import com.jcanseco.radio.MainApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    private MainApplication application;

    public ApplicationModule(MainApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    MainApplication provideApplication() {
        return application;
    }
}
