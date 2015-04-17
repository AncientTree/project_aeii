package com.toyknight.aeii;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Point;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by toyknight on 4/4/2015.
 */
public class GameManager implements AnimationDispatcher {

    private GameCore game;

    private int[][] move_mark_map;
    private ArrayList<Point> move_path;
    private HashSet<Point> movable_positions;
    private HashSet<Point> attackable_positions;

    private final int[] x_dir = {1, -1, 0, 0};
    private final int[] y_dir = {0, 0, 1, -1};

    public GameManager() {
    }

    public void setGame(GameCore game) {
        this.game = game;
    }

    public GameCore getGame() {
        return game;
    }

}
