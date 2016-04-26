package com.jcanseco.radio;

import android.app.Application;
import android.content.Context;

import com.jcanseco.radio.injection.components.DaggerMainComponent;
import com.jcanseco.radio.injection.components.MainComponent;
import com.jcanseco.radio.injection.modules.MainModule;

public class MainApplication extends Application {

    private MainComponent mainComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        buildMainComponent();
    }

    private void buildMainComponent() {
        mainComponent = DaggerMainComponent.builder()
                .mainModule(new MainModule(this))
                .build();
    }

    public static MainApplication getInstance(Context context) {
        return (MainApplication) context.getApplicationContext();
    }

    public MainComponent getMainComponent() {
        return mainComponent;
    }
}
