package org.Core.Config;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.Core.Game.Services.GameSessionService;


public class GameEventPublisher {

    private final EventBus eventBus=new EventBus();

    @Inject
    public GameEventPublisher(GameSessionService gameSessionService){
        register(gameSessionService);
    }

    public void post(Object event){
        eventBus.post(event);
    }

    /**
     * Registers an object outside Guice's graph to receive posted events (its
     * @Subscribe methods must be public — Guava requirement). FriendsPanel is
     * the example today: it's manually constructed (LobbyController isn't
     * Guice-bound), so it can't be a GameEventPublisher constructor
     * dependency the way GameSessionService is — instead it's handed this
     * publisher and calls register(this) itself.
     */
    public void register(Object sub){
        eventBus.register(sub);
    }

}
