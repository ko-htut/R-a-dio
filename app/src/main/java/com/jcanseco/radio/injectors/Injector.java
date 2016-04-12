package com.jcanseco.radio.injectors;

import com.jcanseco.radio.api.RadioRestService;
import com.jcanseco.radio.api.RestClient;
import com.jcanseco.radio.loaders.RadioContentLoader;
import com.jcanseco.radio.radioplayer.RadioPlayerPresenter;
import com.jcanseco.radio.radioplayer.broadcastreceivers.FailedToPlayStreamBroadcastReceiver;
import com.jcanseco.radio.radioplayer.serviceconnections.RadioPlayerServiceConnection;

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
}
