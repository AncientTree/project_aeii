package net.toyknight.aeii.campaign.warroom;

import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Ability;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.system.AER;

/**
 * @author blackwave 08/25/2016.
 */
public class WarroomStage1 extends StageController {

    private void checkClear() {
        if (getContext().count_unit(0) == 0 && getContext().count_unit(2) == 0 && getContext().count_castle(0) == 0) {
            getContext().clear();
        }
    }

    @Override
    public void onGameStart() {
        getContext().head(0, 1);
        getContext().head(1, 0);
        Message message = new Message(5, AER.lang.getText("CAMPAIGN_WARROOM_STAGE_1_MESSAGE_1"));
        getContext().message(message);
    }

    @Override
    public void onUnitMoved(Unit unit, int x, int y) {
    }

    @Override
    public void onUnitStandby(Unit unit) {
    }

    @Override
    public void onUnitAttacked(Unit attacker, Unit defender) {
    }

    @Override
    public void onUnitDestroyed(Unit unit) {
        if (isCommander(unit, getPlayerTeam())) {
            getContext().fail();
        } else {
            checkClear();
            if (getContext().count_unit(2) == 0) {
                getContext().destroy_team(2);
            }
            if (!unit.hasAbility(Ability.UNDEAD) && !unit.isCommander()) {
                getContext().restore(2);
                getContext().remove_tomb(unit.getX(), unit.getY());
                getContext().create(14, 2, unit.getX(), unit.getY());
            }
        }
    }

    @Override
    public void onTileRepaired(int x, int y) {
    }

    @Override
    public void onTileOccupied(int x, int y, int team) {
        if (team == getPlayerTeam()) {
            checkClear();
        }
    }

    @Override
    public void onTurnStart(int turn) {
    }

    @Override
    public String getMapName() {
        return "warroom_stage_1.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        rule.setValue(Rule.Entry.UNIT_CAPACITY, 30);
        return rule;
    }

    @Override
    public int getStartGold() {
        return 500;
    }

    @Override
    public int getPlayerTeam() {
        return 1;
    }

    @Override
    public String[] getObjectives() {
        return new String[]{
                AER.lang.getText("CAMPAIGN_WARROOM_STAGE_1_OBJECTIVE_1"),
                AER.lang.getText("CAMPAIGN_WARROOM_STAGE_1_OBJECTIVE_2")
        };
    }

    @Override
    public int getStageNumber() {
        return 0;
    }

    @Override
    public String getStageName() {
        return AER.lang.getText("CAMPAIGN_WARROOM_STAGE_1_NAME");
    }

}
