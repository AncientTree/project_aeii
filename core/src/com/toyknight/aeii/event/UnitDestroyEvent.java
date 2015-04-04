package com.toyknight.aeii.event;

import com.toyknight.aeii.utils.UnitFactory;
import com.toyknight.aeii.entity.BasicGame;
import com.toyknight.aeii.entity.SkirmishGame;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.listener.GameListener;

/**
 * @author toyknight
 */
public class UnitDestroyEvent implements GameEvent {

    private final BasicGame game;
    private final Unit unit;

    public UnitDestroyEvent(BasicGame game, Unit unit) {
        this.game = game;
        this.unit = unit;
    }

    protected BasicGame getGame() {
        return game;
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public void execute(GameListener listener) {
        getGame().getMap().removeUnit(unit.getX(), unit.getY());
        getGame().updatePopulation(unit.getTeam());
        listener.onUnitDestroy(unit);
        if (unit.getIndex() != UnitFactory.getSkeletonIndex()) {
            getGame().getMap().addTomb(unit.getX(), unit.getY());
        }
        if (unit.isCommander()) {
            getGame().changeCommanderPriceDelta(unit.getTeam(), 500);
        }
        if (getGame() instanceof SkirmishGame) {
            ((SkirmishGame) getGame()).onUnitDestroyed(unit);
        }
    }

}
