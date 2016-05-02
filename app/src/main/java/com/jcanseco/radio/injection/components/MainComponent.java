package com.jcanseco.radio.injection.components;

import com.jcanseco.radio.injection.modules.ApplicationModule;
import com.jcanseco.radio.injection.modules.NetworkModule;
import com.jcanseco.radio.injection.modules.RadioPlayerActivityModule;
import com.jcanseco.radio.injection.modules.PlayerModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class, NetworkModule.class})
public interface MainComponent {

    RadioPlayerActivityComponent buildRadioPlayerActivityComponent(RadioPlayerActivityModule radioPlayerActivityModule);

    RadioPlayerServiceComponent buildRadioPlayerServiceComponent(PlayerModule playerModule);
}
