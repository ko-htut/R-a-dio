package com.jcanseco.radio.services.notifications;

import android.app.Notification;

import com.jcanseco.radio.BuildConfig;
import com.jcanseco.radio.R;
import com.jcanseco.radio.loaders.RadioContentLoader;
import com.jcanseco.radio.models.RadioContent;
import com.jcanseco.radio.testfakes.FakeRadioContent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowNotification;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = "src/main/AndroidManifest.xml")
public class RadioPlayerNotificationFactoryTest {

    private RadioPlayerNotificationFactory notificationFactory;
    private RadioPlayerNotificationFactory.Client client;
    private RadioContentLoader radioContentLoader;
    private RadioContent radioContent;

    @Before
    public void setup() {
        client = mock(RadioPlayerNotificationFactory.Client.class);
        radioContentLoader = mock(RadioContentLoader.class);
        notificationFactory = new RadioPlayerNotificationFactory(client, radioContentLoader, RuntimeEnvironment.application);

        radioContent = new FakeRadioContent();
    }

    @Test
    public void whenStartScheduledCreationOfPlayerNotificationsInvoked_beginActiveLoadingOfContent() {
        notificationFactory.startScheduledCreationOfPlayerNotifications();

        verify(radioContentLoader).beginActiveLoadingOfContent();
    }

    @Test
    public void whenStopScheduledCreationOfPlayerNotificationsInvoked_stopActiveLoadingOfContent() {
        notificationFactory.stopScheduledCreationOfPlayerNotifications();

        verify(radioContentLoader).stopActiveLoadingOfContent();
    }

    @Test
    public void onRadioContentLoadSuccess_notifyClientOfPlayerNotificationCreationSuccess() {
        notificationFactory.onRadioContentLoadSuccess(radioContent);

        verify(client).onPlayerNotificationCreationSuccess(any(Notification.class));
    }

    @Test
    public void testThatTheCreatedNotificationShowsTheRightInformation() {
        ArgumentCaptor<Notification> argumentCaptor = ArgumentCaptor.forClass(Notification.class);

        notificationFactory.onRadioContentLoadSuccess(radioContent);

        verify(client).onPlayerNotificationCreationSuccess(argumentCaptor.capture());
        ShadowNotification actualNotification = shadowOf(argumentCaptor.getValue());

        assertThat(actualNotification.getContentTitle()).isEqualTo("Now Playing");
        assertThat(actualNotification.getContentText()).isEqualTo("current track title");
        assertThat(actualNotification.getSmallIcon()).isEqualTo(R.drawable.play);
    }

    @Test
    public void onRadioContentLoadFailed_notifyClientOfPlayerNotificationCreationFailed() {
        notificationFactory.onRadioContentLoadFailed();

        verify(client).onPlayerNotificationCreationFailed();
    }

}