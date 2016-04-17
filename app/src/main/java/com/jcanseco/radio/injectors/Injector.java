package com.jcanseco.radio.injectors;

import android.app.Application;

import com.jcanseco.radio.api.RadioRestService;
import com.jcanseco.radio.api.RestClient;
import com.jcanseco.radio.loaders.RadioContentLoader;
import com.jcanseco.radio.players.Player;
import com.jcanseco.radio.players.RadioPlayer;
import com.jcanseco.radio.ui.radioplayer.RadioPlayerPresenter;
import com.jcanseco.radio.ui.radioplayer.broadcastreceivers.FailedToPlayStreamBroadcastReceiver;
import com.jcanseco.radio.ui.radioplayer.serviceconnections.RadioPlayerServiceConnection;

public class Injector {

    public static RadioRestService provideRadioRestService() {
        return RestClient.getRadioRestService();
    }

    public static RadioContentLoader provideRadioContentLoader() {
        return new RadioContentLoader(provideRadioRestService());
    }

    public static RadioPlayerPresenter provideRadioPlayerPresenter() {
        return new RadioPlayerPresenter(provideRadioContentLoader());
    }

    public static RadioPlayerServiceConnection provideRadioPlayerServiceConnection(RadioPlayerServiceConnection.ServiceConnectionListener serviceConnectionListener) {
        return new RadioPlayerServiceConnection(serviceConnectionListener);
    }

    public static FailedToPlayStreamBroadcastReceiver provideFailedToPlayStreamBroadcastReceiver(FailedToPlayStreamBroadcastReceiver.BroadcastReceivedListener broadcastReceivedListener) {
        return new FailedToPlayStreamBroadcastReceiver(broadcastReceivedListener);
    }

    public static Player providePlayer(Application application) {
        return RadioPlayer.Factory.create(application);
    }
}
