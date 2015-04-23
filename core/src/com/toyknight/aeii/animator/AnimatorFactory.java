package com.toyknight.aeii.animator;

import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.screen.GameScreen;

import java.util.ArrayList;

/**
 * Created by toyknight on 4/23/2015.
 */
public class AnimatorFactory {

    private static int ts;
    private static GameScreen screen;

    private AnimatorFactory() {
    }

    public static void init(GameScreen screen, int ts) {
        AnimatorFactory.ts = ts;
        AnimatorFactory.screen = screen;
    }

    public static UnitMoveAnimator createUnitMoveAnimator(Unit unit, ArrayList<Point> path) {
        return new UnitMoveAnimator(unit, path, screen, ts);
    }

}
