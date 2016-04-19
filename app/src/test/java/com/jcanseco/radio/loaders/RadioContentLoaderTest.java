package com.jcanseco.radio.loaders;

import com.jcanseco.radio.api.RadioRestService;
import com.jcanseco.radio.models.NowPlayingTrack;
import com.jcanseco.radio.models.RadioContent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RadioContentLoaderTest {

    private RadioContentLoader radioContentLoader;

    @Mock
    private RadioContentLoader.RadioContentListener radioContentListener;

    @Mock
    private RadioRestService radioRestService;

    @Mock
    private Call<RadioContent> radioContentCall;

    @Mock
    private RadioContent radioContent;

    @Mock
    private Timer timer;

    @Before
    public void setup() {
        radioContentLoader = spy(new RadioContentLoader(radioRestService));
        radioContentLoader.setRadioContentListener(radioContentListener);

        when(radioContentLoader.initNewTimer()).thenReturn(timer);
        when(radioContentLoader.getTimer()).thenReturn(timer);
        doNothing().when(timer).schedule(any(TimerTask.class), anyLong());

        when(radioRestService.getRadioContent()).thenReturn(radioContentCall);
    }

    @Test
    public void shouldNotBeSetUpForScheduledLoadingbyDefault() {
        assertThat(radioContentLoader.isSetupForScheduledLoading()).isFalse();
    }

    @Test
    public void whenStartScheduledLoadingOfContentInvoked_ifNotCurrentlySetUpForScheduledLoading_thenLoaderShouldBeSetupForScheduledLoading() {
        when(radioContentLoader.isSetupForScheduledLoading()).thenReturn(false).thenCallRealMethod();

        radioContentLoader.startScheduledLoadingOfContent();

        assertThat(radioContentLoader.isSetupForScheduledLoading()).isTrue();
    }

    @Test
    public void whenStartScheduledLoadingOfContentInvoked_ifNotCurrentlySetUpForScheduledLoading_thenShouldInitNewTimer() {
        when(radioContentLoader.isSetupForScheduledLoading()).thenReturn(false);

        radioContentLoader.startScheduledLoadingOfContent();

        verify(radioContentLoader).initNewTimer();
    }

    @Test
    public void whenStartScheduledLoadingOfContentInvoked_ifAlreadySetUpForScheduledLoading_thenDoNothing() {
        when(radioContentLoader.isSetupForScheduledLoading()).thenReturn(true);

        radioContentLoader.startScheduledLoadingOfContent();

        verify(radioContentLoader, never()).initNewTimer();
        verify(radioContentLoader, never()).loadContent();
    }

    @Test
    public void whenStartScheduledLoadingOfContentInvoked_shouldLoadContentAtLeastOnce() {
        radioContentLoader.startScheduledLoadingOfContent();

        verify(radioContentLoader, atLeast(1)).loadContent();
    }

    @Test
    public void whenStopScheduledLoadingOfContentInvoked_shouldCancelAndPurgeTimer() {
        radioContentLoader.stopScheduledLoadingOfContent();

        InOrder inOrder = inOrder(timer);
        inOrder.verify(timer).cancel();
        inOrder.verify(timer).purge();
    }

    @Test
    public void whenStopScheduledLoadingOfContentInvoked_loaderShouldNotBeSetupForScheduledLoading() {
        radioContentLoader.stopScheduledLoadingOfContent();

        assertThat(radioContentLoader.isSetupForScheduledLoading()).isFalse();
    }

    @Test
    public void onNetworkResponseSuccess_ifLoaderSetupForScheduledLoading_thenScheduleNextLoadTaskForWhenTheCurrentTrackEndsPlus1Sec() {
        when(radioContentLoader.isSetupForScheduledLoading()).thenReturn(true);
        when(radioContent.getCurrentTrack()).thenReturn(mock(NowPlayingTrack.class));
        when(radioContent.getCurrentTrack().getRemainingTimeInSeconds()).thenReturn(142);

        radioContentLoader.onResponse(radioContentCall, getSuccessfulNetworkResponse());

        verify(timer).schedule(any(TimerTask.class), eq(142000L + 1000L));
    }

    @Test
    public void onNetworkResponseSuccess_ifLoaderSetupForScheduledLoading_andRemainingTimeForCurrentTrackIsInvalid_thenScheduleNextLoadTaskFor5SecondsFromNow() {
        when(radioContentLoader.isSetupForScheduledLoading()).thenReturn(true);
        when(radioContent.getCurrentTrack()).thenReturn(mock(NowPlayingTrack.class));
        when(radioContent.getCurrentTrack().getRemainingTimeInSeconds()).thenReturn(NowPlayingTrack.INVALID_TIME_VALUE);

        radioContentLoader.onResponse(radioContentCall, getSuccessfulNetworkResponse());

        verify(timer).schedule(any(TimerTask.class), eq(5000L));
    }

    @Test
    public void onNetworkResponseSuccess_shouldNotifyListenerOfLoadSuccess() {
        radioContentLoader.onResponse(radioContentCall, getSuccessfulNetworkResponse());

        verify(radioContentListener).onRadioContentLoadSuccess(any(RadioContent.class));
    }

    @Test
    public void onNetworkResponseFailure_shouldNotifyListenerOfLoadFailure() {
        radioContentLoader.onResponse(radioContentCall, getFailedNetworkResponse());

        verify(radioContentListener).onRadioContentLoadFailed();
    }

    @Test
    public void onNetworkCallFailure_shouldNotifyListenerOfLoadFailure() {
        radioContentLoader.onFailure(radioContentCall, mock(Throwable.class));

        verify(radioContentListener).onRadioContentLoadFailed();
    }

    private Response<RadioContent> getSuccessfulNetworkResponse() {
        return Response.success(radioContent);
    }

    private Response<RadioContent> getFailedNetworkResponse() {
        return Response.error(500, mock(ResponseBody.class));
    }
}