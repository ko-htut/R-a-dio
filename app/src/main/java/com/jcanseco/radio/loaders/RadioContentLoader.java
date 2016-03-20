package com.jcanseco.radio.loaders;

import com.jcanseco.radio.api.RadioRestService;
import com.jcanseco.radio.models.RadioContent;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RadioContentLoader {

    private RadioContentListener radioContentListener;
    private RadioRestService radioRestService;

    public RadioContentLoader(RadioRestService radioRestService) {
        this.radioRestService = radioRestService;
    }

    public void setRadioContentListener(RadioContentListener radioContentListener) {
        this.radioContentListener = radioContentListener;
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


    public interface RadioContentListener {

        void onRadioContentLoadSuccess(RadioContent radioContent);

        void onRadioContentLoadFailed();
    }
}
