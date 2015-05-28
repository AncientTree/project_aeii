package com.toyknight.aeii.manager;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;

/**
 * Created by toyknight on 4/3/2015.
 */
public interface GameEvent {

    public Point getFocus();

    public boolean canExecute(GameCore game);

    public void execute(GameManager manager);

}
