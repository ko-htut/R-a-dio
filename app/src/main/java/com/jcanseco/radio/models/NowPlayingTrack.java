package com.jcanseco.radio.models;

import java.util.concurrent.TimeUnit;

public class NowPlayingTrack extends Track {

    public static final int INVALID_TIME_VALUE = -9999;
    public static final String INVALID_TIME_STRING = "INVALID_TIME_STRING";

    private long startTimeInUnixTime;
    private long endTimeInUnixTime;

    public NowPlayingTrack(String title, long startTimeInUnixTime, long endTimeInUnixTime) {
        super(title);
        this.startTimeInUnixTime = startTimeInUnixTime;
        this.endTimeInUnixTime = endTimeInUnixTime;
    }

    public String getLengthAsTimeString() {
        int length = getLengthInSeconds();
        if (length != INVALID_TIME_VALUE) {
            return formatSecondsIntoTimeString(length);
        } else {
            return INVALID_TIME_STRING;
        }
    }

    public int getLengthInSeconds() {
        int lengthInSeconds = (int) (endTimeInUnixTime - startTimeInUnixTime);
        return isValidTrackLength(lengthInSeconds) ? lengthInSeconds : INVALID_TIME_VALUE;
    }

    public String getElapsedTimeAsTimeString() {
        int elapsedTimeInSeconds = getElapsedTimeInSeconds();
        if (elapsedTimeInSeconds != INVALID_TIME_VALUE) {
            return formatSecondsIntoTimeString(elapsedTimeInSeconds);
        } else {
            return INVALID_TIME_STRING;
        }
    }

    public int getElapsedTimeInSeconds() {
        int elapsedTimeInSeconds = (int) (getCurrentTimeInUnixTime() - startTimeInUnixTime);
        return isValidTime(elapsedTimeInSeconds) ? elapsedTimeInSeconds : INVALID_TIME_VALUE;
    }

    public String getRemainingTimeAsTimeString() {
        int remainingTimeInSeconds = getRemainingTimeInSeconds();
        if (remainingTimeInSeconds != INVALID_TIME_VALUE) {
            return formatSecondsIntoTimeString(remainingTimeInSeconds);
        } else {
            return INVALID_TIME_STRING;
        }
    }

    public int getRemainingTimeInSeconds() {
        int remainingTimeInSeconds = (int) (endTimeInUnixTime - getCurrentTimeInUnixTime());
        return isValidTime(remainingTimeInSeconds) ? remainingTimeInSeconds : INVALID_TIME_VALUE;

    }

    private String formatSecondsIntoTimeString(int seconds) {
        long minute = TimeUnit.SECONDS.toMinutes(seconds);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - (minute * 60);
        return String.format("%02d:%02d", minute, second);
    }

    protected long getCurrentTimeInUnixTime() {
        return System.currentTimeMillis() / 1000;
    }

    private boolean isValidTrackLength(int timeInSeconds) {
        return timeInSeconds > 0;
    }

    private boolean isValidTime(int timeInSeconds) {
        return timeInSeconds >= 0;
    }
}
