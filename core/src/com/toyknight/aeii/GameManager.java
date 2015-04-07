package com.toyknight.aeii;

import com.toyknight.aeii.entity.BasicGame;

/**
 * Created by toyknight on 4/4/2015.
 */
public class GameManager {

    private BasicGame game;

    public GameManager() {
    }

    public void setGame(BasicGame game) {
        this.game = game;
    }

    public BasicGame getGame() {
        return game;
    }

}
