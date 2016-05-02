package com.jcanseco.radio.injection.modules;

import com.google.android.exoplayer.ExoPlayer;
import com.jcanseco.radio.MainApplication;
import com.jcanseco.radio.injection.scopes.ServiceScope;
import com.jcanseco.radio.players.RadioPlayer;

import dagger.Module;
import dagger.Provides;

@Module
public class PlayerModule {

    @Provides
    @ServiceScope
    ExoPlayer provideExoPlayer() {
        final int rendererCount = 1;
        final int minBufferInMillis = 1000;
        final int minRebufferInMillis = 5000;

        return ExoPlayer.Factory.newInstance(rendererCount, minBufferInMillis, minRebufferInMillis);
    }

    @Provides
    @ServiceScope
    RadioPlayer provideRadioPlayer(ExoPlayer exoPlayer, MainApplication application) {
        return new RadioPlayer(exoPlayer, application);
    }
}
