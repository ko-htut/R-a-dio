package com.jcanseco.radio.loaders;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RadioContentLoaderTimerTaskTest {

    private RadioContentLoaderTimerTask radioContentLoaderTimerTask;

    @Mock
    private RadioContentLoader radioContentLoader;

    @Before
    public void setup() throws Exception {
        radioContentLoaderTimerTask = new RadioContentLoaderTimerTask(radioContentLoader);
    }

    @Test
    public void whenRun_loadRadioContent() {
        radioContentLoaderTimerTask.run();

        verify(radioContentLoader).loadContent();
    }
}