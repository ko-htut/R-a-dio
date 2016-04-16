package com.jcanseco.radio.ui.radioplayer;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jcanseco.radio.BuildConfig;
import com.jcanseco.radio.R;
import com.jcanseco.radio.ui.radioplayer.serviceconnections.RadioPlayerServiceConnection;
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

        radioPlayerActivity.radioPlayerServiceConnection = buildMockRadioPlayerServiceConnection();
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
    public void onRadioPlayerServiceConnected_shouldGetService() {
        RadioPlayerActivity radioPlayerActivity = Robolectric.buildActivity(RadioPlayerActivity.class).create().get();

        radioPlayerActivity.onRadioPlayerServiceConnected(radioPlayerService);

        assertThat(radioPlayerActivity.radioPlayerService).isNotNull()
                .isSameAs(radioPlayerService);
    }

    @Test
    public void onRadioPlayerServiceConnected_ifServiceIsCurrentlyPlayingStream_shouldNotifyPresenter() {
        RadioPlayerActivity radioPlayerActivity = Robolectric.buildActivity(RadioPlayerActivity.class).create().get();
        radioPlayerActivity.radioPlayerPresenter = radioPlayerPresenter;
        boolean isServiceCurrentlyPlayingStream = true;
        when(radioPlayerService.isPlayingStream()).thenReturn(isServiceCurrentlyPlayingStream);

        radioPlayerActivity.onRadioPlayerServiceConnected(radioPlayerService);

        verify(radioPlayerPresenter).onRadioPlayerServiceConnected(isServiceCurrentlyPlayingStream);
    }

    @Test
    public void onRadioPlayerServiceConnected_ifServiceIsNotCurrentlyPlayingStream_shouldNotifyPresenter() {
        RadioPlayerActivity radioPlayerActivity = Robolectric.buildActivity(RadioPlayerActivity.class).create().get();
        radioPlayerActivity.radioPlayerPresenter = radioPlayerPresenter;
        boolean isServiceCurrentlyPlayingStream = false;
        when(radioPlayerService.isPlayingStream()).thenReturn(isServiceCurrentlyPlayingStream);

        radioPlayerActivity.onRadioPlayerServiceConnected(radioPlayerService);

        verify(radioPlayerPresenter).onRadioPlayerServiceConnected(isServiceCurrentlyPlayingStream);
    }

    @Test
    public void onRadioPlayerServiceDisconnected_shouldDiscardReferenceToService() {
        RadioPlayerActivity radioPlayerActivity = Robolectric.buildActivity(RadioPlayerActivity.class).create().get();
        radioPlayerActivity.radioPlayerService = radioPlayerService;

        radioPlayerActivity.onRadioPlayerServiceDisconnected();

        assertThat(radioPlayerActivity.radioPlayerService).isNull();
    }

    @Test
    public void onRadioPlayerServiceDisconnected_shouldNotifyPresenter() {
        RadioPlayerActivity radioPlayerActivity = Robolectric.buildActivity(RadioPlayerActivity.class).create().get();
        radioPlayerActivity.radioPlayerPresenter = radioPlayerPresenter;

        radioPlayerActivity.onRadioPlayerServiceDisconnected();

        verify(radioPlayerPresenter).onRadioPlayerServiceDisconnected();
    }

    @Test
    public void onFailedToPlayStreamBroadcastReceived_shouldNotifyPresenter() {
        radioPlayerActivity.onFailedToPlayStreamBroadcastReceived();

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

    private RadioPlayerServiceConnection buildMockRadioPlayerServiceConnection() {
        RadioPlayerServiceConnection serviceConnection = mock(RadioPlayerServiceConnection.class);
        doNothing().when(serviceConnection).onServiceConnected(any(ComponentName.class), any(IBinder.class));
        doNothing().when(serviceConnection).onServiceDisconnected(any(ComponentName.class));
        return serviceConnection;
    }

    private void assertThatRadioPlayerServiceStartedBy(RadioPlayerActivity activityExpectedToStartService) {
        assertThatServiceStarted(RadioPlayerService.class, activityExpectedToStartService);
    }

    private void assertThatRadioPlayerServiceIsBoundTo(RadioPlayerActivity activityExpectedToBindToService) {
        verify(activityExpectedToBindToService.radioPlayerServiceConnection).onServiceConnected(any(ComponentName.class), any(RadioPlayerService.RadioPlayerBinder.class));
    }

    private void assertThatRadioPlayerServiceIsUnboundFrom(RadioPlayerActivity activityExpectedToUnbindFromService) {
        verify(activityExpectedToUnbindFromService.radioPlayerServiceConnection).onServiceDisconnected(any(ComponentName.class));
    }

    private void assertThatServiceStarted(Class<? extends Service> serviceClass, RadioPlayerActivity activityExpectedToStartService) {
        ShadowActivity shadowActivity = shadowOf(activityExpectedToStartService);
        Intent nextStartedService;

        do {
            nextStartedService = shadowActivity.getNextStartedService();
            if (nextStartedService != null) {
                String nextStartedServiceName = nextStartedService.getComponent().getClassName();
                if (nextStartedServiceName.equals(serviceClass.getName())) {
                    return;
                }
            }
        } while (nextStartedService != null);

        throw new AssertionError(String.format("Was expecting the service %s to be started by the activity %s", serviceClass.getName(), activityExpectedToStartService.getClass().getName()));
    }

    private void assertToastDisplayed(String expectedToastText) {
        String actualToastText = ShadowToast.getTextOfLatestToast();
        assertThat(actualToastText).isEqualTo(expectedToastText);
    }
}