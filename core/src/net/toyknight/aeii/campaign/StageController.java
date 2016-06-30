package net.toyknight.aeii.campaign;

import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.UnitFactory;

/**
 * @author toyknight 6/24/2016.
 */
public abstract class StageController {

    private CampaignContext.StageContext context;

    private boolean cleared;

    public final void setContext(CampaignContext.StageContext context) {
        this.context = context;
    }

    public CampaignContext.StageContext getContext() {
        return context;
    }

    protected boolean isCommander(Unit unit, int team) {
        return unit.getTeam() == team && unit.isCommander();
    }

    protected boolean isCrystal(Unit unit, int team) {
        return unit.getTeam() == team && unit.getIndex() == UnitFactory.getCrystalIndex();
    }

    public final void setCleared(boolean cleared) {
        this.cleared = cleared;
    }

    public final boolean isCleared() {
        return cleared;
    }

    abstract public void onGameStart();

    abstract public void onUnitMoved(Unit unit, int x, int y);

    abstract public void onUnitStandby(Unit unit);

    abstract public void onUnitAttacked(Unit attacker, Unit defender);

    abstract public void onUnitDestroyed(Unit unit);

    abstract public void onTileRepaired(int x, int y);

    abstract public void onTileOccupied(int x, int y, int team);

    abstract public void onTurnStart(int turn);

    abstract public void onTurnEnd(int turn);

    abstract public String getMapName();

    abstract public Rule getRule();

    abstract public int getStartGold();

    abstract public int getPlayerTeam();

    abstract public String[] getObjectives();

    abstract public int getStageNumber();

    abstract public String getStageName();

    public final Snapshot createSnapshot() {
        return new Snapshot(getStageNumber(), getStageName());
    }

    public class Snapshot {

        public final int stage;
        public final String name;

        public Snapshot(int stage, String name) {
            this.stage = stage;
            this.name = name;
        }

        @Override
        public String toString() {
            return (stage + 1) + ". " + name;
        }

    }

}
