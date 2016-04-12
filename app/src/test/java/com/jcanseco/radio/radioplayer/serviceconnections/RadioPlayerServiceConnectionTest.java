package com.jcanseco.radio.radioplayer.serviceconnections;

import android.content.ComponentName;

import com.jcanseco.radio.services.RadioPlayerService;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RadioPlayerServiceConnectionTest {

    private RadioPlayerServiceConnection radioPlayerServiceConnection;
    private RadioPlayerServiceConnection.ServiceConnectionListener serviceConnectionListener;

    @Before
    public void setup() {
        serviceConnectionListener = mock(RadioPlayerServiceConnection.ServiceConnectionListener.class);

        radioPlayerServiceConnection = new RadioPlayerServiceConnection(serviceConnectionListener);
    }

    @Test
    public void onServiceConnected_shouldNotifyListener() {
        radioPlayerServiceConnection.onServiceConnected(mock(ComponentName.class), mock(RadioPlayerService.RadioPlayerBinder.class));

        verify(serviceConnectionListener).onRadioPlayerServiceConnected(any(RadioPlayerService.class));
    }

    @Test
    public void onServiceConnected_shouldNotifyListener_andPassItTheRadioPlayerService() {
        RadioPlayerService radioPlayerService = mock(RadioPlayerService.class);
        RadioPlayerService.RadioPlayerBinder radioPlayerBinder = mock(RadioPlayerService.RadioPlayerBinder.class);
        when(radioPlayerBinder.getService()).thenReturn(radioPlayerService);

        radioPlayerServiceConnection.onServiceConnected(mock(ComponentName.class), radioPlayerBinder);

        verify(serviceConnectionListener).onRadioPlayerServiceConnected(same(radioPlayerService));
    }

    @Test
    public void onServiceDisconnected_shouldNotifyListener() {
        radioPlayerServiceConnection.onServiceDisconnected(mock(ComponentName.class));

        verify(serviceConnectionListener).onRadioPlayerServiceDisconnected();
    }
}