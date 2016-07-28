package net.toyknight.aeii.campaign.aeii;

import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Tile;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/29/2016.
 */
public class AEIIStage7 extends StageController {

    private void checkClear() {
        Tile castle1 = getContext().tile(0, 8);
        Tile castle2 = getContext().tile(12, 9);
        if (getContext().count_unit(1) == 0 && castle1.getTeam() == getPlayerTeam() && castle2.getTeam() == getPlayerTeam()) {
            getContext().clear();
        }
    }

    private void triggerStealEvent() {
        getContext().set("stolen", 1);
        Unit crystal = getContext().crystal(0);
        getContext().fly_over(8, 1, 16, crystal.getY(), crystal.getX(), crystal.getY());
        Message message4 = new Message(5, Language.getText("CAMPAIGN_AEII_STAGE_7_MESSAGE_4"));
        Message message5 = new Message(2, Language.getText("CAMPAIGN_AEII_STAGE_7_MESSAGE_5"));
        getContext().message(message4, message5);
        getContext().carry(crystal.getX(), crystal.getY(), 11, 0, -1, crystal.getY());
        getContext().reinforce(1, 0, 8,
                new Reinforcement(6, 5, 9),
                new Reinforcement(6, 4, 8),
                new Reinforcement(8, 4, 7),
                new Reinforcement(0, 3, 7),
                new Reinforcement(4, 2, 8),
                new Reinforcement(8, 2, 9),
                new Reinforcement(0, 1, 9),
                new Reinforcement(9, 0, 7));
        getContext().reinforce(1, 13, 14,
                new Reinforcement(2, 13, 12),
                new Reinforcement(0, 12, 14),
                new Reinforcement(6, 14, 14),
                new Reinforcement(3, 13, 15));
        Message message6 = new Message(3, Language.getText("CAMPAIGN_AEII_STAGE_7_MESSAGE_6"));
        Message message7 = new Message(5, Language.getText("CAMPAIGN_AEII_STAGE_7_MESSAGE_7"));
        Message message8 = new Message(0, Language.getText("CAMPAIGN_AEII_STAGE_7_MESSAGE_8"));
        getContext().message(message6, message7, message8);
        getContext().reinforce(0, 13, 17,
                new Reinforcement(9, 2, 13, 16),
                new Reinforcement(6, 12, 16),
                new Reinforcement(8, 14, 16),
                new Reinforcement(1, 12, 17),
                new Reinforcement(4, 14, 17));
        Message message9 = new Message(1, Language.getText("CAMPAIGN_AEII_STAGE_7_MESSAGE_9"));
        getContext().message(message9);
        getContext().show_objectives();
    }

    @Override
    public void onGameStart() {
        getContext().set("reinforced", 0);
        getContext().set("stolen", 0);
        getContext().destroy_team(1);
        getContext().reinforce(0, 13, 0,
                new Reinforcement(9, 14, 3),
                new Reinforcement(0, 14, 2),
                new Reinforcement(1, 14, 1),
                new Reinforcement(3, 13, 1),
                new Reinforcement(11, 13, 0));
        getContext().focus(14, 3);
        Message message1 = new Message(5, Language.getText("CAMPAIGN_AEII_STAGE_7_MESSAGE_1"));
        Message message2 = new Message(0, Language.getText("CAMPAIGN_AEII_STAGE_7_MESSAGE_2"));
        getContext().message(message1, message2);
    }

    @Override
    public void onUnitMoved(Unit unit, int x, int y) {
    }

    @Override
    public void onUnitStandby(Unit unit) {
        if (unit.getTeam() == getPlayerTeam()
                && (unit.getX() < 11 || unit.getY() > 10) && getContext().get("stolen") == 0) {
            triggerStealEvent();
        }
    }

    @Override
    public void onUnitAttacked(Unit attacker, Unit defender) {
    }

    @Override
    public void onUnitDestroyed(Unit unit) {
        if (getContext().count_unit(1) == 0 && getContext().get("stolen") == 0) {
            triggerStealEvent();
        }
        if (isCommander(unit, getPlayerTeam()) || isCrystal(unit, getPlayerTeam())) {
            getContext().fail();
        } else {
            checkClear();
        }
    }

    @Override
    public void onTileRepaired(int x, int y) {
    }

    @Override
    public void onTileOccupied(int x, int y, int team) {
        if (team == getPlayerTeam()) {
            if (getContext().tile(x, y).isCastle() && getContext().get("stolen") == 0) {
                triggerStealEvent();
            } else {
                checkClear();
            }
        }
    }

    @Override
    public void onTurnStart(int turn) {
        if (turn == 2 && getContext().get("reinforced") == 0) {
            getContext().restore(1);
            getContext().reinforce(1, 11, 8,
                    new Reinforcement(5, 14, 7),
                    new Reinforcement(0, 13, 7),
                    new Reinforcement(3, 12, 7),
                    new Reinforcement(1, 13, 8));
            Message message3 = new Message(5, Language.getText("CAMPAIGN_AEII_STAGE_7_MESSAGE_3"));
            getContext().message(message3);
        }
    }

    @Override
    public void onTurnEnd(int turn) {
    }

    @Override
    public String getMapName() {
        return "aeii_c6.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        Array<Integer> available_units = new Array<Integer>();
        for (int i = 0; i <= 9; i++) {
            available_units.add(i);
        }
        rule.setAvailableUnits(available_units);
        rule.setValue(Rule.Entry.MAX_POPULATION, 40);
        return rule;
    }

    @Override
    public int getStartGold() {
        return 300;
    }

    @Override
    public int getPlayerTeam() {
        return 0;
    }

    @Override
    public String[] getObjectives() {
        if (getContext().get("stolen") == 0) {
            return new String[]{Language.getText("CAMPAIGN_AEII_STAGE_7_OBJECTIVE_1")};
        } else {
            return new String[]{Language.getText("CAMPAIGN_AEII_STAGE_7_OBJECTIVE_2")};
        }
    }

    @Override
    public int getStageNumber() {
        return 6;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_AEII_STAGE_7_NAME");
    }

}
