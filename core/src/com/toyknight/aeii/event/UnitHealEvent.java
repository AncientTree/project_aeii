package com.toyknight.aeii.event;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.listener.GameListener;

/**
 * @author toyknight
 */
public class UnitHealEvent implements GameEvent {

    private final GameCore game;
    private final Unit healer;
    private final Unit target;

    public UnitHealEvent(GameCore game, Unit healer, Unit target) {
        this.game = game;
        this.healer = healer;
        this.target = target;
    }

    protected GameCore getGame() {
        return game;
    }

    @Override
    public boolean canExecute() {
        return target.getCurrentHp() < target.getMaxHp()
                && !getGame().isEnemy(healer.getTeam(), target.getTeam());
    }

    @Override
    public void execute(GameListener listener) {
        int base_heal = healer.getLevel() * 10 + 50;
        int heal = target.getCurrentHp() + base_heal < target.getMaxHp()
                ? base_heal : target.getMaxHp() - target.getCurrentHp();
        target.setCurrentHp(target.getCurrentHp() + heal);
        listener.onUnitHpChange(target, heal);
        if (healer.gainExperience(30)) {
            listener.onUnitLevelUp(healer);
        }
    }

}
