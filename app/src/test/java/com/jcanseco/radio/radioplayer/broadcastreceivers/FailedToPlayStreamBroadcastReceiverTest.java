package com.jcanseco.radio.radioplayer.broadcastreceivers;

import android.content.Context;
import android.content.Intent;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FailedToPlayStreamBroadcastReceiverTest {

    FailedToPlayStreamBroadcastReceiver.BroadcastReceivedListener broadcastReceivedListener;
    FailedToPlayStreamBroadcastReceiver broadcastReceiver;

    @Before
    public void setup() {
        broadcastReceivedListener = mock(FailedToPlayStreamBroadcastReceiver.BroadcastReceivedListener.class);

        broadcastReceiver = new FailedToPlayStreamBroadcastReceiver(broadcastReceivedListener);
    }

    @Test
    public void onReceive_shouldNotifyListener() {
        broadcastReceiver.onReceive(mock(Context.class), mock(Intent.class));

        verify(broadcastReceivedListener).onFailedToPlayStreamBroadcastReceived();
    }
}