package com.jcanseco.radio.injection.components;

import com.jcanseco.radio.injection.modules.RadioPlayerServiceModule;
import com.jcanseco.radio.injection.scopes.ServiceScope;
import com.jcanseco.radio.services.RadioPlayerService;

import dagger.Subcomponent;

@ServiceScope
@Subcomponent(modules = {RadioPlayerServiceModule.class})
public interface RadioPlayerServiceComponent {

    void inject(RadioPlayerService radioPlayerService);
}
