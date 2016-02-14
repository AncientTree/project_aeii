package com.toyknight.aeii.entity;

/**
 * @author toyknight 9/17/2015.
 */
public class GameSave {

    public final int type;

    public final GameCore game;

    public GameSave(GameCore game, int type) {
        this.type = type;
        this.game = game;
    }

    public int getType() {
        return type;
    }

    public GameCore getGame() {
        return game;
    }

}
