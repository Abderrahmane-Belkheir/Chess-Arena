package org.Core.Config;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.Core.Game.Services.GameSessionService;


public class AppEvents {

    private final EventBus eventBus=new EventBus();
    @Inject
    public AppEvents(GameSessionService gameSessionService){
        register(gameSessionService);
    }
    public void post(Object event){
        eventBus.post(event);
    }

    private void register(Object sub){
        eventBus.register(sub);
    }

}
