package com.toyknight.aeii.manager.events;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.manager.GameManager;

/**
 * @author toyknight 4/3/2015.
 */
public interface GameEvent {

    Point getFocus(GameCore game);

    boolean canExecute(GameCore game);

    void execute(GameManager manager);

    GameEvent copy();

}
