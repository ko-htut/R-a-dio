package com.jcanseco.radio.injection.modules;

import com.jcanseco.radio.api.RadioRestService;
import com.jcanseco.radio.injection.scopes.ActivityScope;
import com.jcanseco.radio.loaders.RadioContentLoader;
import com.jcanseco.radio.ui.radioplayer.RadioPlayerActivity;
import com.jcanseco.radio.ui.radioplayer.RadioPlayerPresenter;
import com.jcanseco.radio.ui.radioplayer.broadcastreceivers.FailedToPlayStreamBroadcastReceiver;
import com.jcanseco.radio.ui.radioplayer.serviceconnections.RadioPlayerServiceConnection;

import dagger.Module;
import dagger.Provides;

@Module
public class RadioPlayerActivityModule {

    private RadioPlayerActivity radioPlayerActivity;

    public RadioPlayerActivityModule(RadioPlayerActivity radioPlayerActivity) {
        this.radioPlayerActivity = radioPlayerActivity;
    }

    @Provides
    @ActivityScope
    RadioContentLoader provideRadioContentLoader(RadioRestService radioRestService) {
        return new RadioContentLoader(radioRestService);
    }

    @Provides
    @ActivityScope
    RadioPlayerPresenter provideRadioPlayerPresenter(RadioContentLoader radioContentLoader) {
        return new RadioPlayerPresenter(radioContentLoader);
    }

    @Provides
    @ActivityScope
    RadioPlayerServiceConnection provideRadioPlayerServiceConnection() {
        return new RadioPlayerServiceConnection(radioPlayerActivity);
    }

    @Provides
    @ActivityScope
    FailedToPlayStreamBroadcastReceiver provideFailedToPlayStreamBroadcastReceiver() {
        return new FailedToPlayStreamBroadcastReceiver(radioPlayerActivity);
    }
}
