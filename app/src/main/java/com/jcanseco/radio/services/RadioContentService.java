package com.jcanseco.radio.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.jcanseco.radio.constants.Constants;
import com.jcanseco.radio.injectors.Injector;
import com.jcanseco.radio.loaders.RadioContentLoader;
import com.jcanseco.radio.models.RadioContent;

public class RadioContentService extends Service implements RadioContentLoader.RadioContentListener {

    RadioContentLoader radioContentLoader;

    private final IBinder radioContentBinder = new RadioContentBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        radioContentLoader = Injector.provideRadioContentLoader();
        radioContentLoader.setRadioContentListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        radioContentLoader.beginActiveLoadingOfContent();

        return radioContentBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        radioContentLoader.beginActiveLoadingOfContent();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        radioContentLoader.stopActiveLoadingOfContent();

        return true;
    }

    @Override
    public void onRadioContentLoadSuccess(RadioContent radioContent) {
        sendOutRadioContentLoadSuccessBroadcast(radioContent);
    }

    @Override
    public void onRadioContentLoadFailed() {
        sendOutRadioContentLoadFailedBroadcast();
    }

    private void sendOutRadioContentLoadSuccessBroadcast(RadioContent radioContent) {
        Intent intent = new Intent(Constants.Actions.RADIO_CONTENT_LOAD_SUCCESS);
        intent.putExtra(Constants.Extras.RADIO_CONTENT, radioContent);
        sendOutLocalBroadcast(intent);
    }

    private void sendOutRadioContentLoadFailedBroadcast() {
        Intent intent = new Intent(Constants.Actions.RADIO_CONTENT_LOAD_FAILED);
        sendOutLocalBroadcast(intent);
    }

    private void sendOutLocalBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }


    public class RadioContentBinder extends Binder {
        public RadioContentService getService() {
            return RadioContentService.this;
        }
    }
}
