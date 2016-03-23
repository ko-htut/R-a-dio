package com.jcanseco.radio.loaders;

import com.jcanseco.radio.api.RadioRestService;
import com.jcanseco.radio.models.NowPlayingTrack;
import com.jcanseco.radio.models.RadioContent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
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
    public void whenBeginActiveLoadingOfContentInvoked_loaderShouldBeSetupForActiveLoading() {
        radioContentLoader.beginActiveLoadingOfContent();

        assertThat(radioContentLoader.isSetupForActiveLoading()).isTrue();
    }

    @Test
    public void whenBeginActiveLoadingOfContentInvoked_shouldInitNewTimer() {
        radioContentLoader.beginActiveLoadingOfContent();

        verify(radioContentLoader).initNewTimer();
    }

    @Test
    public void whenBeginActiveLoadingOfContentInvoked_shouldLoadContentAtLeastOnce() {
        radioContentLoader.beginActiveLoadingOfContent();

        verify(radioContentLoader, atLeast(1)).loadContent();
    }

    @Test
    public void whenStopActiveLoadingOfContentInvoked_shouldCancelTimer() {
        radioContentLoader.stopActiveLoadingOfContent();

        verify(timer).cancel();
    }

    @Test
    public void whenStopActiveLoadingOfContentInvoked_loaderShouldNotBeSetupForActiveLoading() {
        radioContentLoader.stopActiveLoadingOfContent();

        assertThat(radioContentLoader.isSetupForActiveLoading()).isFalse();
    }

    @Test
    public void whenLoadContentInvoked_ifNetworkResponseSuccess_andLoaderSetupForActiveLoading_thenScheduleNextLoadTaskForWhenTheCurrentTrackEndsPlus1Sec() {
        Answer networkResponseSuccess = getAnswerForNetworkResponseSuccess();
        doAnswer(networkResponseSuccess).when(radioContentCall).enqueue(Matchers.<Callback<RadioContent>>any());
        when(radioContentLoader.isSetupForActiveLoading()).thenReturn(true);
        when(radioContent.getCurrentTrack()).thenReturn(mock(NowPlayingTrack.class));
        when(radioContent.getCurrentTrack().getRemainingTimeInSeconds()).thenReturn(142);

        radioContentLoader.loadContent();

        verify(timer).schedule(any(TimerTask.class), eq(142000L + 1000L));
    }

    @Test
    public void whenLoadContentInvoked_ifNetworkResponseSuccess_andLoaderSetupForActiveLoading_andRemainingTimeForCurrentTrackIsInvalid_thenScheduleNextLoadTaskFor5SecondsFromNow() {
        Answer networkResponseSuccess = getAnswerForNetworkResponseSuccess();
        doAnswer(networkResponseSuccess).when(radioContentCall).enqueue(Matchers.<Callback<RadioContent>>any());
        when(radioContentLoader.isSetupForActiveLoading()).thenReturn(true);
        when(radioContent.getCurrentTrack()).thenReturn(mock(NowPlayingTrack.class));
        when(radioContent.getCurrentTrack().getRemainingTimeInSeconds()).thenReturn(NowPlayingTrack.INVALID_TIME_VALUE);

        radioContentLoader.loadContent();

        verify(timer).schedule(any(TimerTask.class), eq(5000L));
    }

    @Test
    public void whenLoadContentInvoked_ifNetworkResponseSuccess_thenNotifyListenerOfLoadSuccess() {
        Answer networkResponseSuccess = getAnswerForNetworkResponseSuccess();
        doAnswer(networkResponseSuccess).when(radioContentCall).enqueue(Matchers.<Callback<RadioContent>>any());

        radioContentLoader.loadContent();

        verify(radioContentListener).onRadioContentLoadSuccess(any(RadioContent.class));
    }

    @Test
    public void whenLoadContentInvoked_ifNetworkResponseFailure_thenNotifyListenerOfLoadFailure() {
        Answer networkResponseFail = getAnswerForNetworkResponseFailure();
        doAnswer(networkResponseFail).when(radioContentCall).enqueue(Matchers.<Callback<RadioContent>>any());

        radioContentLoader.loadContent();

        verify(radioContentListener).onRadioContentLoadFailed();
    }

    @Test
    public void whenLoadContentInvoked_ifNetworkCallFailure_thenNotifyListenerOfLoadFailure() {
        Answer networkCallFail = getAnswerForNetworkCallFailure();
        doAnswer(networkCallFail).when(radioContentCall).enqueue(Matchers.<Callback<RadioContent>>any());

        radioContentLoader.loadContent();

        verify(radioContentListener).onRadioContentLoadFailed();
    }

    private Answer getAnswerForNetworkResponseSuccess() {
        Response<RadioContent> response = Response.success(radioContent);
        return getAnswerForNetworkResponse(response);
    }

    private Answer getAnswerForNetworkResponseFailure() {
        Response<RadioContent> response = Response.error(500, mock(ResponseBody.class));
        return getAnswerForNetworkResponse(response);
    }

    private Answer getAnswerForNetworkResponse(final Response response) {
        return new Answer() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                Callback<RadioContent> radioContentCallback = (Callback<RadioContent>) invocation.getArguments()[0];
                radioContentCallback.onResponse(radioContentCall, response);
                return null;
            }
        };
    }

    private Answer getAnswerForNetworkCallFailure() {
        return new Answer() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                Callback<RadioContent> radioContentCallback = (Callback<RadioContent>) invocation.getArguments()[0];
                radioContentCallback.onFailure(radioContentCall, mock(Throwable.class));
                return null;
            }
        };
    }
}