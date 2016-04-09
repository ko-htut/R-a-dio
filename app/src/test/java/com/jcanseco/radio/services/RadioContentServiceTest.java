package com.jcanseco.radio.services;

import android.content.BroadcastReceiver;
import android.content.Intent;

import com.jcanseco.radio.BuildConfig;
import com.jcanseco.radio.constants.Constants;
import com.jcanseco.radio.loaders.RadioContentLoader;
import com.jcanseco.radio.models.RadioContent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.ServiceController;

import static com.jcanseco.radio.testutilities.BroadcastTestingUtilities.buildMockLocalBroadcastReceiver;
import static com.jcanseco.radio.testutilities.BroadcastTestingUtilities.verifyThatReceiverReceivedBroadcastWithExtra;
import static com.jcanseco.radio.testutilities.BroadcastTestingUtilities.verifyThatReceiverReceivedExpectedBroadcast;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = "src/main/AndroidManifest.xml")
public class RadioContentServiceTest {

    private RadioContentService radioContentService;
    private ServiceController<RadioContentService> serviceController;
    private RadioContentLoader radioContentLoader;

    @Before
    public void setup() {
        ShadowApplication.getInstance().clearStartedServices();

        serviceController = Robolectric.buildService(RadioContentService.class);
        radioContentService = serviceController.attach().create().get();

        radioContentLoader = mock(RadioContentLoader.class);
        radioContentService.radioContentLoader = radioContentLoader;
        radioContentLoader.setRadioContentListener(radioContentService);
    }

    @Test
    public void testThatServiceCanBeBound() {
        RadioContentService.RadioContentBinder binder = (RadioContentService.RadioContentBinder) radioContentService.onBind(mock(Intent.class));

        assertThat(binder).isNotNull();
        assertThat(binder.getService()).isEqualTo(radioContentService);
    }

    @Test
    public void onBind_shouldBeginActiveLoadingOfContent() {
        radioContentService.onBind(mock(Intent.class));

        verify(radioContentLoader).beginActiveLoadingOfContent();
    }

    @Test
    public void onRebind_shouldBeginActiveLoadingOfContent() {
        radioContentService.onRebind(mock(Intent.class));

        verify(radioContentLoader).beginActiveLoadingOfContent();
    }

    @Test
    public void onUnbind_shouldStopActiveLoadingOfContent() {
        radioContentService.onUnbind(mock(Intent.class));

        verify(radioContentLoader).stopActiveLoadingOfContent();
    }

    @Test
    public void onUnbind_shouldIndicateThatOnRebindShouldBeCalledWhenNewClientsBindToTheService() {
        assertThat(radioContentService.onUnbind(mock(Intent.class))).isTrue();
    }

    @Test
    public void onRadioContentLoadSuccess_sendOutRadioContentLoadSuccessBroadcast() {
        String expectedBroadcastIntentAction = Constants.Actions.RADIO_CONTENT_LOAD_SUCCESS;
        BroadcastReceiver receiver = buildMockLocalBroadcastReceiver(expectedBroadcastIntentAction);

        radioContentService.onRadioContentLoadSuccess(mock(RadioContent.class));

        verifyThatReceiverReceivedExpectedBroadcast(receiver, expectedBroadcastIntentAction);
    }

    @Test
    public void onRadioContentLoadSuccess_sendOutRadioContentLoadSuccessBroadcast_withRadioContentExtra() {
        String expectedBroadcastIntentExtraKey = Constants.Extras.RADIO_CONTENT;
        RadioContent expectedRadioContentExtra = mock(RadioContent.class);

        String expectedBroadcastIntentAction = Constants.Actions.RADIO_CONTENT_LOAD_SUCCESS;
        BroadcastReceiver receiver = buildMockLocalBroadcastReceiver(expectedBroadcastIntentAction);

        radioContentService.onRadioContentLoadSuccess(expectedRadioContentExtra);

        verifyThatReceiverReceivedExpectedBroadcast(receiver, expectedBroadcastIntentAction);
        verifyThatReceiverReceivedBroadcastWithExtra(receiver, expectedBroadcastIntentAction, expectedBroadcastIntentExtraKey, expectedRadioContentExtra);
    }

    @Test
    public void onRadioContentLoadFailed_sendOutRadioContentLoadFailedBroadcast() {
        String expectedBroadcastIntentAction = Constants.Actions.RADIO_CONTENT_LOAD_FAILED;
        BroadcastReceiver receiver = buildMockLocalBroadcastReceiver(expectedBroadcastIntentAction);

        radioContentService.onRadioContentLoadFailed();

        verifyThatReceiverReceivedExpectedBroadcast(receiver, expectedBroadcastIntentAction);
    }
}