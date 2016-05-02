package com.jcanseco.radio.injection.components;

import com.jcanseco.radio.injection.modules.PlayerModule;
import com.jcanseco.radio.injection.scopes.ServiceScope;
import com.jcanseco.radio.services.RadioPlayerService;

import dagger.Subcomponent;

@ServiceScope
@Subcomponent(modules = {PlayerModule.class})
public interface RadioPlayerServiceComponent {

    void inject(RadioPlayerService radioPlayerService);
}
