package com.jcanseco.radio.tasks;

import com.jcanseco.radio.players.RadioPlayer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RadioPlayerBufferTimeoutTimerTaskTest {

    private RadioPlayerBufferTimeoutTimerTask radioPlayerBufferTimeoutTimerTask;

    @Mock
    private RadioPlayer radioPlayer;

    @Before
    public void setup() {
        radioPlayerBufferTimeoutTimerTask = new RadioPlayerBufferTimeoutTimerTask(radioPlayer);
    }

    @Test
    public void whenRun_notifyRadioPlayerThatBufferingTimedOut() {
        radioPlayerBufferTimeoutTimerTask.run();

        verify(radioPlayer).onBufferingTimedOut();
    }
}