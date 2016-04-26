package com.jcanseco.radio.injection.components;

import com.jcanseco.radio.injection.modules.MainModule;
import com.jcanseco.radio.injection.modules.RadioPlayerActivityModule;
import com.jcanseco.radio.injection.modules.RadioPlayerServiceModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {MainModule.class})
public interface MainComponent {

    RadioPlayerActivityComponent buildRadioPlayerActivityComponent(RadioPlayerActivityModule radioPlayerActivityModule);

    RadioPlayerServiceComponent buildRadioPlayerServiceComponent(RadioPlayerServiceModule radioPlayerServiceModule);
}
