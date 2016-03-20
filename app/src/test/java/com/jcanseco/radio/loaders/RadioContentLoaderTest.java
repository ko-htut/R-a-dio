package com.jcanseco.radio.loaders;

import com.jcanseco.radio.api.RadioRestService;
import com.jcanseco.radio.models.RadioContent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
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

    @Before
    public void setup() {
        radioContentLoader = spy(new RadioContentLoader(radioRestService));
        radioContentLoader.setRadioContentListener(radioContentListener);

        when(radioRestService.getRadioContent()).thenReturn(radioContentCall);
    }

    @Test
    public void whenLoadContentInvoked_ifSuccessfulNetworkResponse_thenNotifyListenerOfLoadSuccess() {
        Answer networkResponseSuccess = getAnswerForSuccessfulNetworkResponse();
        doAnswer(networkResponseSuccess).when(radioContentCall).enqueue(Matchers.<Callback<RadioContent>>any());

        radioContentLoader.loadContent();

        verify(radioContentListener).onRadioContentLoadSuccess(any(RadioContent.class));
    }

    @Test
    public void whenLoadContentInvoked_ifSuccessfulNetworkResponse_thenDontNotifyListenerOfLoadFailure() {
        Answer networkResponseSuccess = getAnswerForSuccessfulNetworkResponse();
        doAnswer(networkResponseSuccess).when(radioContentCall).enqueue(Matchers.<Callback<RadioContent>>any());

        radioContentLoader.loadContent();

        verify(radioContentListener, never()).onRadioContentLoadFailed();
    }

    @Test
    public void whenLoadContentInvoked_ifFailedNetworkResponse_thenNotifyListenerOfLoadFailure() {
        Answer networkResponseFail = getAnswerForFailedNetworkResponse();
        doAnswer(networkResponseFail).when(radioContentCall).enqueue(Matchers.<Callback<RadioContent>>any());

        radioContentLoader.loadContent();

        verify(radioContentListener).onRadioContentLoadFailed();
    }

    @Test
    public void whenLoadContentInvoked_ifFailedNetworkResponse_thenDontNotifyListenerOfLoadSuccess() {
        Answer networkResponseFail = getAnswerForFailedNetworkResponse();
        doAnswer(networkResponseFail).when(radioContentCall).enqueue(Matchers.<Callback<RadioContent>>any());

        radioContentLoader.loadContent();

        verify(radioContentListener, never()).onRadioContentLoadSuccess(any(RadioContent.class));
    }

    @Test
    public void whenLoadContentInvoked_ifFailedNetworkCall_thenNotifyListenerOfLoadFailure() {
        Answer networkCallFail = getAnswerForFailedNetworkCall();
        doAnswer(networkCallFail).when(radioContentCall).enqueue(Matchers.<Callback<RadioContent>>any());

        radioContentLoader.loadContent();

        verify(radioContentListener).onRadioContentLoadFailed();
    }

    @Test
    public void whenLoadContentInvoked_ifFailedNetworkCall_thenDontNotifyListenerOfLoadSuccess() {
        Answer networkCallFail = getAnswerForFailedNetworkCall();
        doAnswer(networkCallFail).when(radioContentCall).enqueue(Matchers.<Callback<RadioContent>>any());

        radioContentLoader.loadContent();

        verify(radioContentListener, never()).onRadioContentLoadSuccess(any(RadioContent.class));
    }

    private Answer getAnswerForSuccessfulNetworkResponse() {
        Response<RadioContent> response = Response.success(radioContent);
        return getAnswerForNetworkResponse(response);
    }

    private Answer getAnswerForFailedNetworkResponse() {
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

    private Answer getAnswerForFailedNetworkCall() {
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