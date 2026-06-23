package org.Core.Game.Services;

import com.google.common.eventbus.Subscribe;
import org.Core.Game.Events.GameFound;
import org.Core.Game.Events.GameMove;

public class GameSessionService{

    @Subscribe
    public void onMatchFound(GameFound event){
        System.out.println(event);
    }

    @Subscribe
    public void onMove(GameMove event){
        System.out.println(event);
    }


}
