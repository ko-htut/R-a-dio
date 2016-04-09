package com.jcanseco.radio.radioplayer;

import android.app.Service;
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
import com.jcanseco.radio.constants.Constants;
import com.jcanseco.radio.models.RadioContent;
import com.jcanseco.radio.services.RadioContentService;
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
    private RadioContentService radioContentService;

    @Before
    public void setup() {
        activityController = Robolectric.buildActivity(RadioPlayerActivity.class);
        radioPlayerActivity = activityController.create().get();

        radioPlayerPresenter = mock(RadioPlayerPresenter.class);
        radioPlayerActivity.radioPlayerPresenter = radioPlayerPresenter;

        radioPlayerService = mock(RadioPlayerService.class);
        radioPlayerActivity.radioPlayerService = radioPlayerService;

        radioContentService = mock(RadioContentService.class);
        radioPlayerActivity.radioContentService = radioContentService;

        radioPlayerActivity.radioPlayerServiceConnection = buildMockServiceConnection();
        radioPlayerActivity.radioContentServiceConnection = buildMockServiceConnection();
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
    public void onStop_shouldNotifyPresenter() {
        activityController.start().resume().visible().pause().userLeaving().stop();

        verify(radioPlayerPresenter).onStop();
    }

    @Test
    public void whenStartServicesInvoked_startRadioPlayerService() {
        radioPlayerActivity.startServices();

        assertThatRadioPlayerServiceStartedBy(radioPlayerActivity);
    }

    @Test
    public void whenBindToServicesInvoked_bindToRadioContentService() {
        radioPlayerActivity.bindToServices();

        assertThatRadioContentServiceIsBoundTo(radioPlayerActivity);
    }

    @Test
    public void whenBindToServicesInvoked_bindToRadioPlayerService() {
        radioPlayerActivity.bindToServices();

        assertThatRadioPlayerServiceIsBoundTo(radioPlayerActivity);
    }

    @Test
    public void whenUnbindFromServicesInvoked_unbindFromRadioContentService() {
        activityController.start();

        radioPlayerActivity.unbindFromServices();

        assertThatRadioContentServiceIsUnboundFrom(radioPlayerActivity);
    }

    @Test
    public void whenUnbindFromServicesInvoked_unbindFromRadioPlayerService() {
        activityController.start();

        radioPlayerActivity.unbindFromServices();

        assertThatRadioPlayerServiceIsUnboundFrom(radioPlayerActivity);
    }

    @Test
    public void whenUnbindFromServicesInvoked_thenNotifyPresenterThatRadioContentServiceHasBeenDisconnected() {
        activityController.start();

        radioPlayerActivity.unbindFromServices();

        verify(radioPlayerPresenter).onRadioContentServiceDisconnected();
    }

    @Test
    public void whenUnbindFromServicesInvoked_thenNotifyPresenterThatRadioPlayerServiceHasBeenDisconnected() {
        activityController.start();

        radioPlayerActivity.unbindFromServices();

        verify(radioPlayerPresenter).onRadioPlayerServiceDisconnected();
    }


    @Test
    public void whenUnbindFromServicesInvoked_thenDiscardReferenceToRadioContentService() {
        activityController.start();

        radioPlayerActivity.unbindFromServices();

        assertThat(radioPlayerActivity.radioContentService).isNull();
    }

    @Test
    public void whenUnbindFromServicesInvoked_thenDiscardReferenceToRadioPlayerService() {
        activityController.start();

        radioPlayerActivity.unbindFromServices();

        assertThat(radioPlayerActivity.radioPlayerService).isNull();
    }

    @Test
    public void onRadioContentServiceConnected_shouldGetServiceFromIBinder() {
        RadioPlayerActivity radioPlayerActivity = Robolectric.buildActivity(RadioPlayerActivity.class).create().get();
        radioPlayerActivity.radioPlayerPresenter = radioPlayerPresenter;

        RadioContentService.RadioContentBinder binder = mock(RadioContentService.RadioContentBinder.class);
        when(binder.getService()).thenReturn(radioContentService);
        radioPlayerActivity.radioContentServiceConnection.onServiceConnected(mock(ComponentName.class), binder);

        verify(binder).getService();
        assertThat(radioPlayerActivity.radioContentService).isSameAs(binder.getService());
    }

    @Test
    public void onRadioContentServiceConnected_shouldNotifyPresenter() {
        RadioPlayerActivity radioPlayerActivity = Robolectric.buildActivity(RadioPlayerActivity.class).create().get();
        radioPlayerActivity.radioPlayerPresenter = radioPlayerPresenter;

        RadioContentService.RadioContentBinder binder = mock(RadioContentService.RadioContentBinder.class);
        when(binder.getService()).thenReturn(radioContentService);
        radioPlayerActivity.radioContentServiceConnection.onServiceConnected(mock(ComponentName.class), binder);

        verify(radioPlayerPresenter).onRadioContentServiceConnected();
    }

    @Test
    public void onRadioContentServiceDisconnected_shouldDiscardReferenceToService() {
        RadioPlayerActivity radioPlayerActivity = Robolectric.buildActivity(RadioPlayerActivity.class).create().get();
        radioPlayerActivity.radioPlayerPresenter = radioPlayerPresenter;
        radioPlayerActivity.radioContentService = radioContentService;

        radioPlayerActivity.radioContentServiceConnection.onServiceDisconnected(mock(ComponentName.class));

        assertThat(radioPlayerActivity.radioContentService).isNull();
    }

    @Test
    public void onRadioContentServiceDisconnected_shouldNotifyPresenter() {
        RadioPlayerActivity radioPlayerActivity = Robolectric.buildActivity(RadioPlayerActivity.class).create().get();
        radioPlayerActivity.radioPlayerPresenter = radioPlayerPresenter;

        radioPlayerActivity.radioContentServiceConnection.onServiceDisconnected(mock(ComponentName.class));

        verify(radioPlayerPresenter).onRadioContentServiceDisconnected();
    }

    @Test
    public void onRadioPlayerServiceConnected_shouldGetServiceFromIBinder() {
        RadioPlayerActivity radioPlayerActivity = Robolectric.buildActivity(RadioPlayerActivity.class).create().get();
        radioPlayerActivity.radioPlayerPresenter = radioPlayerPresenter;

        RadioPlayerService.RadioPlayerBinder binder = mock(RadioPlayerService.RadioPlayerBinder.class);
        when(binder.getService()).thenReturn(radioPlayerService);
        radioPlayerActivity.radioPlayerServiceConnection.onServiceConnected(mock(ComponentName.class), binder);

        verify(binder).getService();
        assertThat(radioPlayerActivity.radioPlayerService).isSameAs(binder.getService());
    }

    @Test
    public void onRadioPlayerServiceConnected_ifServiceIsCurrentlyPlayingStream_shouldNotifyPresenter() {
        RadioPlayerActivity radioPlayerActivity = Robolectric.buildActivity(RadioPlayerActivity.class).create().get();
        radioPlayerActivity.radioPlayerPresenter = radioPlayerPresenter;
        boolean isServiceCurrentlyPlayingStream = true;
        when(radioPlayerService.isPlayingStream()).thenReturn(isServiceCurrentlyPlayingStream);

        RadioPlayerService.RadioPlayerBinder binder = mock(RadioPlayerService.RadioPlayerBinder.class);
        when(binder.getService()).thenReturn(radioPlayerService);
        radioPlayerActivity.radioPlayerServiceConnection.onServiceConnected(mock(ComponentName.class), binder);

        verify(radioPlayerPresenter).onRadioPlayerServiceConnected(isServiceCurrentlyPlayingStream);
    }

    @Test
    public void onRadioPlayerServiceConnected_ifServiceIsNotCurrentlyPlayingStream_shouldNotifyPresenter() {
        RadioPlayerActivity radioPlayerActivity = Robolectric.buildActivity(RadioPlayerActivity.class).create().get();
        radioPlayerActivity.radioPlayerPresenter = radioPlayerPresenter;
        boolean isServiceCurrentlyPlayingStream = false;
        when(radioPlayerService.isPlayingStream()).thenReturn(isServiceCurrentlyPlayingStream);

        RadioPlayerService.RadioPlayerBinder binder = mock(RadioPlayerService.RadioPlayerBinder.class);
        when(binder.getService()).thenReturn(radioPlayerService);
        radioPlayerActivity.radioPlayerServiceConnection.onServiceConnected(mock(ComponentName.class), binder);

        verify(radioPlayerPresenter).onRadioPlayerServiceConnected(isServiceCurrentlyPlayingStream);
    }

    @Test
    public void onRadioPlayerServiceDisconnected_shouldDiscardReferenceToService() {
        RadioPlayerActivity radioPlayerActivity = Robolectric.buildActivity(RadioPlayerActivity.class).create().get();
        radioPlayerActivity.radioPlayerPresenter = radioPlayerPresenter;
        radioPlayerActivity.radioPlayerService = radioPlayerService;

        radioPlayerActivity.radioPlayerServiceConnection.onServiceDisconnected(mock(ComponentName.class));

        assertThat(radioPlayerActivity.radioPlayerService).isNull();
    }

    @Test
    public void onRadioPlayerServiceDisconnected_shouldNotifyPresenter() {
        RadioPlayerActivity radioPlayerActivity = Robolectric.buildActivity(RadioPlayerActivity.class).create().get();
        radioPlayerActivity.radioPlayerPresenter = radioPlayerPresenter;

        radioPlayerActivity.radioPlayerServiceConnection.onServiceDisconnected(mock(ComponentName.class));

        verify(radioPlayerPresenter).onRadioPlayerServiceDisconnected();
    }

    @Test
    public void onRadioContentLoadSuccessBroadcastReceived_notifyPresenter() {
        RadioContent radioContent = mock(RadioContent.class);
        Intent intent = new Intent(Constants.Actions.RADIO_CONTENT_LOAD_SUCCESS);
        intent.putExtra(Constants.Extras.RADIO_CONTENT, radioContent);

        radioPlayerActivity.radioContentLoadStatusBroadcastReceiver.onReceive(mock(Context.class), intent);

        verify(radioPlayerPresenter).onRadioContentLoadSuccess(radioContent);
    }

    @Test
    public void onRadioContentLoadFailedBroadcastReceived_notifyPresenter() {
        Intent intent = new Intent(Constants.Actions.RADIO_CONTENT_LOAD_FAILED);

        radioPlayerActivity.radioContentLoadStatusBroadcastReceiver.onReceive(mock(Context.class), intent);

        verify(radioPlayerPresenter).onRadioContentLoadFailed();
    }

    @Test
    public void onFailedToPlayStreamBroadcastReceived_notifyPresenter() {
        radioPlayerActivity.failedToPlayStreamBroadcastReceiver.onReceive(mock(Context.class), mock(Intent.class));

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
        assertThatServiceStarted(RadioPlayerService.class, activityExpectedToStartService);
    }

    private void assertThatRadioPlayerServiceIsBoundTo(RadioPlayerActivity activityExpectedToBindToService) {
        verify(activityExpectedToBindToService.radioPlayerServiceConnection).onServiceConnected(any(ComponentName.class), any(RadioPlayerService.RadioPlayerBinder.class));
    }

    private void assertThatRadioContentServiceIsBoundTo(RadioPlayerActivity activityExpectedToBindToService) {
        verify(activityExpectedToBindToService.radioContentServiceConnection).onServiceConnected(any(ComponentName.class), any(RadioContentService.RadioContentBinder.class));
    }

    private void assertThatRadioPlayerServiceIsUnboundFrom(RadioPlayerActivity activityExpectedToUnbindFromService) {
        verify(activityExpectedToUnbindFromService.radioPlayerServiceConnection).onServiceDisconnected(any(ComponentName.class));
    }

    private void assertThatRadioContentServiceIsUnboundFrom(RadioPlayerActivity activityExpectedToUnbindFromService) {
        verify(activityExpectedToUnbindFromService.radioContentServiceConnection).onServiceDisconnected(any(ComponentName.class));
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