package com.jcanseco.radio.ui.radioplayer.serviceconnections;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.jcanseco.radio.services.RadioPlayerService;

public class RadioPlayerServiceConnection implements ServiceConnection {

    private ServiceConnectionListener serviceConnectionListener;

    public RadioPlayerServiceConnection(ServiceConnectionListener serviceConnectionListener) {
        this.serviceConnectionListener = serviceConnectionListener;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        RadioPlayerService.RadioPlayerBinder radioPlayerBinder = (RadioPlayerService.RadioPlayerBinder) binder;
        RadioPlayerService radioPlayerService = radioPlayerBinder.getService();
        serviceConnectionListener.onRadioPlayerServiceConnected(radioPlayerService);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        serviceConnectionListener.onRadioPlayerServiceDisconnected();
    }


    public interface ServiceConnectionListener {

        void onRadioPlayerServiceConnected(RadioPlayerService radioPlayerService);

        void onRadioPlayerServiceDisconnected();
    }
}
