package com.jcanseco.radio.loaders;

import com.jcanseco.radio.api.RadioRestService;
import com.jcanseco.radio.models.NowPlayingTrack;
import com.jcanseco.radio.models.RadioContent;

import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RadioContentLoader {

    private static final int DEFAULT_SCHEDULED_LOAD_TASK_DELAY_IN_MILLIS = 5000;

    private RadioContentListener radioContentListener;
    private RadioRestService radioRestService;

    private boolean isSetupForActiveLoading;
    private Timer timer;

    public RadioContentLoader(RadioRestService radioRestService) {
        this.radioRestService = radioRestService;
    }

    public void setRadioContentListener(RadioContentListener radioContentListener) {
        this.radioContentListener = radioContentListener;
    }

    public void beginActiveLoadingOfContent() {
        isSetupForActiveLoading = true;
        timer = initNewTimer();
        loadContent();
    }

    public void stopActiveLoadingOfContent() {
        isSetupForActiveLoading = false;
        getTimer().cancel();
    }

    public void loadContent() {
        Call<RadioContent> radioContentCall = radioRestService.getRadioContent();
        radioContentCall.enqueue(buildRadioContentCallback());
    }

    private Callback<RadioContent> buildRadioContentCallback() {
        return new Callback<RadioContent>() {
            @Override
            public void onResponse(Call<RadioContent> call, Response<RadioContent> response) {
                if (response.isSuccess()) {
                    RadioContent radioContent = response.body();
                    radioContentListener.onRadioContentLoadSuccess(radioContent);

                    if(isSetupForActiveLoading()) {
                        long delayInMillis = determineDelayForNextLoadTaskInMillis(radioContent.getCurrentTrack());
                        scheduleNextLoadTask(delayInMillis);
                    }
                } else {
                    radioContentListener.onRadioContentLoadFailed();
                }
            }

            @Override
            public void onFailure(Call<RadioContent> call, Throwable t) {
                radioContentListener.onRadioContentLoadFailed();
            }
        };
    }

    protected boolean isSetupForActiveLoading() {
        return isSetupForActiveLoading;
    }

    private int determineDelayForNextLoadTaskInMillis(NowPlayingTrack currentTrack) {
        int remainingTimeForCurrentTrackInSecs = currentTrack.getRemainingTimeInSeconds();

        if(remainingTimeForCurrentTrackInSecs != NowPlayingTrack.INVALID_TIME_VALUE) {
            return (remainingTimeForCurrentTrackInSecs + 1) * 1000;
        } else {
            return DEFAULT_SCHEDULED_LOAD_TASK_DELAY_IN_MILLIS;
        }
    }

    private void scheduleNextLoadTask(long delayInMillis) {
        TimerTask timerTask = new RadioContentLoaderTimerTask(this);
        getTimer().schedule(timerTask, delayInMillis);
    }

    protected Timer initNewTimer() {
        return new Timer();
    }

    protected Timer getTimer() {
        return timer;
    }


    public interface RadioContentListener {

        void onRadioContentLoadSuccess(RadioContent radioContent);

        void onRadioContentLoadFailed();
    }
}
