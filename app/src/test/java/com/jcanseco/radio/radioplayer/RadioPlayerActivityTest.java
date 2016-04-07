package com.jcanseco.radio.radioplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jcanseco.radio.BuildConfig;
import com.jcanseco.radio.R;
import com.jcanseco.radio.services.RadioPlayerService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.util.ActivityController;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = "src/main/AndroidManifest.xml")
public class RadioPlayerActivityTest {

    private ActivityController<RadioPlayerActivity> activityController;
    private RadioPlayerActivity radioPlayerActivity;
    private RadioPlayerPresenter radioPlayerPresenter;
    private RadioPlayerService radioPlayerService;

    @Before
    public void setup() {
        activityController = Robolectric.buildActivity(RadioPlayerActivity.class);
        radioPlayerActivity = activityController.create().get();

        radioPlayerPresenter = mock(RadioPlayerPresenter.class);
        radioPlayerActivity.radioPlayerPresenter = radioPlayerPresenter;

        radioPlayerService = mock(RadioPlayerService.class);
        radioPlayerActivity.radioPlayerService = radioPlayerService;

        radioPlayerActivity.serviceConnection = buildMockServiceConnection();
    }

    @After
    public void teardown() {
        activityController.pause().userLeaving().stop().destroy();
    }

    @Test
    public void onStart_shouldNotifyPresenter() {
        activityController.start();

        verify(radioPlayerPresenter).onStart();
    }

    @Test
    public void onResume_shouldNotifyPresenter() {
        activityController.start().resume();

        verify(radioPlayerPresenter).onResume();
    }

    @Test
    public void onPause_shouldNotifyPresenter() {
        activityController.start().resume().visible().pause();

        verify(radioPlayerPresenter).onPause();
    }

    @Test
    public void onStop_shouldNotifyPresenter() {
        activityController.start().resume().visible().pause().userLeaving().stop();

        verify(radioPlayerPresenter).onStop();
    }

    @Test
    public void testStartRadioPlayerService() {
        radioPlayerActivity.startRadioPlayerService();

        assertThatRadioPlayerServiceStartedBy(radioPlayerActivity);
    }

    @Test
    public void testBindToRadioPlayerService() {
        radioPlayerActivity.bindToRadioPlayerService();

        assertThatRadioPlayerServiceIsBoundTo(radioPlayerActivity);
    }

    @Test
    public void testUnbindFromRadioPlayerService() {
        activityController.start();

        radioPlayerActivity.unbindFromRadioPlayerService();

        assertThatRadioPlayerServiceIsUnboundFrom(radioPlayerActivity);
    }

    @Test
    public void whenUnbindFromRadioPlayerServiceInvoked_thenNotifyPresenterThatServiceHasBeenDisconnected() {
        activityController.start();

        radioPlayerActivity.unbindFromRadioPlayerService();

        verify(radioPlayerPresenter).onRadioPlayerServiceDisconnected();
    }

    @Test
    public void whenUnbindFromRadioPlayerServiceInvoked_thenDiscardReferenceToService() {
        activityController.start();

        radioPlayerActivity.unbindFromRadioPlayerService();

        assertThat(radioPlayerActivity.radioPlayerService).isNull();
    }

    @Test
    public void onRadioPlayerServiceConnected_shouldGetServiceFromIBinder() {
        RadioPlayerActivity radioPlayerActivity = Robolectric.buildActivity(RadioPlayerActivity.class).create().get();
        radioPlayerActivity.radioPlayerPresenter = radioPlayerPresenter;

        RadioPlayerService.RadioPlayerBinder binder = mock(RadioPlayerService.RadioPlayerBinder.class);
        when(binder.getService()).thenReturn(radioPlayerService);
        radioPlayerActivity.serviceConnection.onServiceConnected(mock(ComponentName.class), binder);

        verify(binder).getService();
        assertThat(radioPlayerActivity.radioPlayerService).isSameAs(binder.getService());
    }

    @Test
    public void onRadioPlayerServiceConnected_shouldNotifyPresenter() {
        RadioPlayerActivity radioPlayerActivity = Robolectric.buildActivity(RadioPlayerActivity.class).create().get();
        radioPlayerActivity.radioPlayerPresenter = radioPlayerPresenter;

        RadioPlayerService.RadioPlayerBinder binder = mock(RadioPlayerService.RadioPlayerBinder.class);
        when(binder.getService()).thenReturn(radioPlayerService);
        radioPlayerActivity.serviceConnection.onServiceConnected(mock(ComponentName.class), binder);

        verify(radioPlayerPresenter).onRadioPlayerServiceConnected();
    }

    @Test
    public void onRadioPlayerServiceDisconnected_shouldDiscardReferenceToService() {
        RadioPlayerActivity radioPlayerActivity = Robolectric.buildActivity(RadioPlayerActivity.class).create().get();
        radioPlayerActivity.radioPlayerPresenter = radioPlayerPresenter;
        radioPlayerActivity.radioPlayerService = radioPlayerService;

        radioPlayerActivity.serviceConnection.onServiceDisconnected(mock(ComponentName.class));

        assertThat(radioPlayerActivity.radioPlayerService).isNull();
    }

    @Test
    public void onRadioPlayerServiceDisconnected_shouldNotifyPresenter() {
        RadioPlayerActivity radioPlayerActivity = Robolectric.buildActivity(RadioPlayerActivity.class).create().get();
        radioPlayerActivity.radioPlayerPresenter = radioPlayerPresenter;

        radioPlayerActivity.serviceConnection.onServiceDisconnected(mock(ComponentName.class));

        verify(radioPlayerPresenter).onRadioPlayerServiceDisconnected();
    }

    @Test
    public void onFailedToPlayStreamBroadcastReceived_notifyPresenter() {
        radioPlayerActivity.receiver.onReceive(mock(Context.class), mock(Intent.class));

        verify(radioPlayerPresenter).onFailedToPlayStreamBroadcastReceived();
    }

    @Test
    public void onActionButtonClicked_shouldNotifyPresenter() {
        activityController.start().resume().visible();
        View actionButtonView = radioPlayerActivity.findViewById(R.id.action_button);

        actionButtonView.callOnClick();

        verify(radioPlayerPresenter).onActionButtonClicked();
    }

    @Test
    public void testShowPlayButton() {
        activityController.start().resume().visible();
        Button actionButton = mock(Button.class);
        radioPlayerActivity.actionButton = actionButton;

        radioPlayerActivity.showPlayButton();

        verify(actionButton).setBackgroundResource(R.drawable.play);
    }

    @Test
    public void testShowPauseButton() {
        activityController.start().resume().visible();
        Button actionButton = mock(Button.class);
        radioPlayerActivity.actionButton = actionButton;

        radioPlayerActivity.showPauseButton();

        verify(actionButton).setBackgroundResource(R.drawable.pause);
    }

    @Test
    public void testShowCurrentTrackTitle() {
        activityController.start().resume().visible();
        TextView trackTitleTextView = (TextView) radioPlayerActivity.findViewById(R.id.track_title);

        radioPlayerActivity.showCurrentTrackTitle("track title");

        assertThat(trackTitleTextView).isNotNull()
                .isVisible()
                .containsText("track title");
    }

    @Test
    public void testShowCurrentDjName() {
        activityController.start().resume().visible();
        TextView djNameTextView = (TextView) radioPlayerActivity.findViewById(R.id.dj_name);

        radioPlayerActivity.showCurrentDjName("Hanyuu-sama");

        assertThat(djNameTextView).isNotNull()
                .isVisible()
                .containsText("Hanyuu-sama");
    }

    @Test
    public void testShowNumOfListeners() {
        activityController.start().resume().visible();
        TextView numOfListeners = (TextView) radioPlayerActivity.findViewById(R.id.num_of_listeners);

        radioPlayerActivity.showNumOfListeners(255);

        assertThat(numOfListeners).isNotNull()
                .isVisible()
                .containsText("255 Listeners");
    }

    @Test
    public void whenStartPlayingRadioStreamInvoked_invokeStartPlayingRadioStreamOnService() {
        activityController.start().resume().visible();

        radioPlayerActivity.startPlayingRadioStream("http://streamurl.com");

        verify(radioPlayerService).startPlayingRadioStream("http://streamurl.com");
    }

    @Test
    public void whenStopPlayingRadioStreamInvoked_invokeStopPlayingRadioStreamOnService() {
        activityController.start().resume().visible();

        radioPlayerActivity.stopPlayingRadioStream();

        verify(radioPlayerService).stopPlayingRadioStream();
    }

    @Test
    public void testShowCouldNotLoadRadioContentErrorMessage() {
        activityController.start().resume().visible();

        radioPlayerActivity.showCouldNotLoadRadioContentErrorMessage();

        assertToastDisplayed("Failed to load. Try again later.");
    }

    @Test
    public void testShowCouldNotPlayRadioStreamErrorMessage() {
        activityController.start().resume().visible();

        radioPlayerActivity.showCouldNotPlayRadioStreamErrorMessage();

        assertToastDisplayed("Error playing stream. Try again later.");
    }

    private ServiceConnection buildMockServiceConnection() {
        ServiceConnection serviceConnection = mock(ServiceConnection.class);
        doNothing().when(serviceConnection).onServiceConnected(any(ComponentName.class), any(IBinder.class));
        doNothing().when(serviceConnection).onServiceDisconnected(any(ComponentName.class));
        return serviceConnection;
    }

    private void assertThatRadioPlayerServiceStartedBy(RadioPlayerActivity activityExpectedToStartService) {
        ShadowActivity shadowActivity = shadowOf(activityExpectedToStartService);
        String nextStartedService = shadowActivity.getNextStartedService().getComponent().getClassName();
        assertThat(nextStartedService).isEqualTo(RadioPlayerService.class.getName());
    }

    private void assertThatRadioPlayerServiceIsBoundTo(RadioPlayerActivity activityExpectedToBindToService) {
        verify(activityExpectedToBindToService.serviceConnection).onServiceConnected(any(ComponentName.class), any(RadioPlayerService.RadioPlayerBinder.class));
    }

    private void assertThatRadioPlayerServiceIsUnboundFrom(RadioPlayerActivity activityExpectedToUnbindFromService) {
        verify(activityExpectedToUnbindFromService.serviceConnection).onServiceDisconnected(any(ComponentName.class));
    }

    private void assertToastDisplayed(String expectedToastText) {
        String actualToastText = ShadowToast.getTextOfLatestToast();
        assertThat(actualToastText).isEqualTo(expectedToastText);
    }
}