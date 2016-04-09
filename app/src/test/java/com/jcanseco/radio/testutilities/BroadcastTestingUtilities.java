package com.jcanseco.radio.testutilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import org.mockito.ArgumentCaptor;
import org.robolectric.shadows.ShadowApplication;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BroadcastTestingUtilities {

    public static BroadcastReceiver buildMockLocalBroadcastReceiver(String expectedBroadcastIntentAction) {
        Context context = ShadowApplication.getInstance().getApplicationContext();
        BroadcastReceiver receiver = mock(BroadcastReceiver.class);
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(receiver, new IntentFilter(expectedBroadcastIntentAction));
        return receiver;
    }

    public static void verifyThatReceiverReceivedExpectedBroadcast(BroadcastReceiver receiver, String expectedBroadcastIntentAction) {
        try {
            verify(receiver).onReceive(any(Context.class), any(Intent.class));
        } catch (AssertionError e) {
            throwFailedToReceiveBroadcastError(expectedBroadcastIntentAction);
        }
    }

    public static void verifyThatReceiverReceivedBroadcastWithExtra(BroadcastReceiver receiver, String expectedBroadcastIntentAction, String expectedExtraKey, Object expectedExtraValue) {
        ArgumentCaptor<Intent> argumentCaptor = ArgumentCaptor.forClass(Intent.class);

        try {
            verify(receiver).onReceive(any(Context.class), argumentCaptor.capture());
        } catch (AssertionError e) {
            throwFailedToReceiveBroadcastError(expectedBroadcastIntentAction);
        }

        Bundle extras = argumentCaptor.getValue().getExtras();
        assertThat(extras.get(expectedExtraKey)).isNotNull();
        assertThat(extras.get(expectedExtraKey)).isSameAs(expectedExtraValue);
    }

    public static void throwFailedToReceiveBroadcastError(String expectedBroadcastIntentAction) {
        String errorMessage = String.format("Was expecting broadcast receiver to receive a broadcast with " +
                "the following intent action: %s. Either the receiver was not set up correctly to receive " +
                "the aforementioned intent action, or no broadcast with the intent action was sent out at all.", expectedBroadcastIntentAction);
        throw new AssertionError(errorMessage);
    }
}
