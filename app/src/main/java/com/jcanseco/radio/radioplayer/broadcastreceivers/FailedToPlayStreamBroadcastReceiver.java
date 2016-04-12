package com.jcanseco.radio.radioplayer.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class FailedToPlayStreamBroadcastReceiver extends BroadcastReceiver {

    private BroadcastReceivedListener broadcastReceivedListener;

    public FailedToPlayStreamBroadcastReceiver(BroadcastReceivedListener broadcastReceivedListener) {
        this.broadcastReceivedListener = broadcastReceivedListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        broadcastReceivedListener.onFailedToPlayStreamBroadcastReceived();
    }


    public interface BroadcastReceivedListener {

        void onFailedToPlayStreamBroadcastReceived();
    }
}
