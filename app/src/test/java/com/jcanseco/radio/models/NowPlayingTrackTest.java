package com.jcanseco.radio.models;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class NowPlayingTrackTest {

    private static final long FAKE_CURRENT_TIME_IN_UNIX_TIME = 1458194220;

    private String title;
    private int startTimeInUnixTime;
    private int endTimeInUnixTime;

    private NowPlayingTrack nowPlayingTrack;

    @Before
    public void setup() {
        title = "track title";
        startTimeInUnixTime = 1458194205;
        endTimeInUnixTime = 1458194511;

        nowPlayingTrack = spy(new NowPlayingTrack(title, startTimeInUnixTime, endTimeInUnixTime));
    }

    @Test
    public void testGetTitle() {
        assertThat(nowPlayingTrack.getTitle()).isEqualTo(title);
    }

    @Test
    public void testGetLengthInSeconds() {
        int trackLengthInSeconds = endTimeInUnixTime - startTimeInUnixTime;
        assertThat(nowPlayingTrack.getLengthInSeconds()).isEqualTo(trackLengthInSeconds);
    }

    @Test
    public void testGetLengthAsTimeString() {
        assertThat(nowPlayingTrack.getLengthAsTimeString()).isEqualTo("05:06");
    }

    @Test
    public void testGetElapsedTimeInSeconds() {
        when(nowPlayingTrack.getCurrentTimeInUnixTime()).thenReturn(FAKE_CURRENT_TIME_IN_UNIX_TIME);

        int elapsedTimeInSeconds = (int) (FAKE_CURRENT_TIME_IN_UNIX_TIME - startTimeInUnixTime);
        assertThat(nowPlayingTrack.getElapsedTimeInSeconds()).isEqualTo(elapsedTimeInSeconds);
    }

    @Test
    public void testGetElapsedTimeAsTimeString() {
        when(nowPlayingTrack.getCurrentTimeInUnixTime()).thenReturn(FAKE_CURRENT_TIME_IN_UNIX_TIME);

        assertThat(nowPlayingTrack.getElapsedTimeAsTimeString()).isEqualTo("00:15");
    }

    @Test
    public void testGetRemainingTimeInSeconds() {
        when(nowPlayingTrack.getCurrentTimeInUnixTime()).thenReturn(FAKE_CURRENT_TIME_IN_UNIX_TIME);

        int remainingTimeInSeconds = (int) (endTimeInUnixTime - FAKE_CURRENT_TIME_IN_UNIX_TIME);
        assertThat(nowPlayingTrack.getRemainingTimeInSeconds()).isEqualTo(remainingTimeInSeconds);
    }

    @Test
    public void testGetRemainingTimeAsTimeString() {
        when(nowPlayingTrack.getCurrentTimeInUnixTime()).thenReturn(FAKE_CURRENT_TIME_IN_UNIX_TIME);

        assertThat(nowPlayingTrack.getRemainingTimeAsTimeString()).isEqualTo("04:51");
    }

    @Test
    public void whenLengthIsNegative_thenGetLengthReturnsInvalidValue() {
        long startTimeInUnixTime = 1500000000;
        long endTimeInUnixTime = 1000000000;
        nowPlayingTrack = new NowPlayingTrack(title, startTimeInUnixTime, endTimeInUnixTime);

        assertThat(nowPlayingTrack.getLengthInSeconds()).isEqualTo(NowPlayingTrack.INVALID_TIME_VALUE);
        assertThat(nowPlayingTrack.getLengthAsTimeString()).isEqualTo(NowPlayingTrack.INVALID_TIME_STRING);
    }

    @Test
    public void whenLengthIsZero_thenGetLengthReturnsInvalidValue() {
        long startTimeInUnixTime = 1000000000;
        long endTimeInUnixTime = 1000000000;
        nowPlayingTrack = new NowPlayingTrack(title, startTimeInUnixTime, endTimeInUnixTime);

        assertThat(nowPlayingTrack.getLengthInSeconds()).isEqualTo(NowPlayingTrack.INVALID_TIME_VALUE);
        assertThat(nowPlayingTrack.getLengthAsTimeString()).isEqualTo(NowPlayingTrack.INVALID_TIME_STRING);
    }

    @Test
    public void whenElapsedTimeIsNegative_thenGetElapsedTimeReturnsInvalidValue() {
        long startTimeInUnixTime = 1500000000;
        long endTimeInUnixTime = 1000000000;
        long fakeCurrentTimeInUnixTime = 1250000000;
        nowPlayingTrack = spy(new NowPlayingTrack(title, startTimeInUnixTime, endTimeInUnixTime));
        when(nowPlayingTrack.getCurrentTimeInUnixTime()).thenReturn(fakeCurrentTimeInUnixTime);

        assertThat(nowPlayingTrack.getElapsedTimeInSeconds()).isEqualTo(NowPlayingTrack.INVALID_TIME_VALUE);
        assertThat(nowPlayingTrack.getElapsedTimeAsTimeString()).isEqualTo(NowPlayingTrack.INVALID_TIME_STRING);
    }

    @Test
    public void whenRemainingTimeIsNegative_thenGetRemainingTimeReturnsInvalidValue() {
        long startTimeInUnixTime = 1500000000;
        long endTimeInUnixTime = 1000000000;
        long fakeCurrentTimeInUnixTime = 1250000000;
        nowPlayingTrack = spy(new NowPlayingTrack(title, startTimeInUnixTime, endTimeInUnixTime));
        when(nowPlayingTrack.getCurrentTimeInUnixTime()).thenReturn(fakeCurrentTimeInUnixTime);

        assertThat(nowPlayingTrack.getRemainingTimeInSeconds()).isEqualTo(NowPlayingTrack.INVALID_TIME_VALUE);
        assertThat(nowPlayingTrack.getRemainingTimeAsTimeString()).isEqualTo(NowPlayingTrack.INVALID_TIME_STRING);
    }
}