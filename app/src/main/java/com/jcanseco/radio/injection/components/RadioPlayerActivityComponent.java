package com.jcanseco.radio.injection.components;

import com.jcanseco.radio.injection.modules.RadioPlayerActivityModule;
import com.jcanseco.radio.injection.scopes.ActivityScope;
import com.jcanseco.radio.ui.radioplayer.RadioPlayerActivity;

import dagger.Subcomponent;

@ActivityScope
@Subcomponent(modules = {RadioPlayerActivityModule.class})
public interface RadioPlayerActivityComponent {

    void inject(RadioPlayerActivity radioPlayerActivity);
}
