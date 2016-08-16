package net.toyknight.aeii.campaign.aei;

import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 7/25/2016.
 */
public class AEIStage2 extends StageController {

    private void scout() {
        getContext().set("scouted", 1);
        Message message3 = new Message(5, Language.getText("CAMPAIGN_AEI_STAGE_2_MESSAGE_3"));
        Message message4 = new Message(0, Language.getText("CAMPAIGN_AEI_STAGE_2_MESSAGE_4"));
        getContext().message(message3, message4);
    }

    @Override
    public void onGameStart() {
        getContext().alliance(1, 1);
        getContext().set("scouted", 0);
        getContext().set("reported", 0);
        Message message1 = new Message(-1, Language.getText("CAMPAIGN_AEI_STAGE_2_MESSAGE_1"));
        Message message2 = new Message(0, Language.getText("CAMPAIGN_AEI_STAGE_2_MESSAGE_2"));
        getContext().message(message1, message2);
    }

    @Override
    public void onUnitMoved(Unit unit, int x, int y) {
    }

    @Override
    public void onUnitStandby(Unit unit) {
        if (getContext().get("scouted") == 0 && unit.getY() <= 9) {
            scout();
        }
        if (getContext().get("reported") == 0 && unit.getY() <= 5) {
            getContext().set("reported", 1);
            getContext().reinforce(0, 10, 2,
                    new Reinforcement(2, 9, 2),
                    new Reinforcement(2, 10, 1));
            getContext().restore(1);
            getContext().reinforce(1,
                    new Reinforcement(5, 4, 0),
                    new Reinforcement(5, 1, 1),
                    new Reinforcement(5, 1, 5));
            Message message5 = new Message(5, Language.getText("CAMPAIGN_AEI_STAGE_2_MESSAGE_5"));
            Message message6 = new Message(0, Language.getText("CAMPAIGN_AEI_STAGE_2_MESSAGE_6"));
            Message message7 = new Message(5, Language.getText("CAMPAIGN_AEI_STAGE_2_MESSAGE_7"));
            getContext().message(message5, message6, message7);
            getContext().show_objectives();
        }
    }

    @Override
    public void onUnitAttacked(Unit attacker, Unit defender) {
    }

    @Override
    public void onUnitDestroyed(Unit unit) {
        if (isCommander(unit, getPlayerTeam())) {
            getContext().fail();
        }
        if (getContext().count_unit(1) == 0) {
            getContext().clear();
        }
    }

    @Override
    public void onTileRepaired(int x, int y) {
    }

    @Override
    public void onTileOccupied(int x, int y, int team) {
        if (getContext().get("scouted") == 0 && getContext().count_village(team) >= 2) {
            scout();
        }
    }

    @Override
    public void onTurnStart(int turn) {
    }

    @Override
    public String getMapName() {
        return "aei_c1.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        Array<Integer> available_units = new Array<Integer>();
        available_units.add(0);
        available_units.add(1);
        available_units.add(2);
        rule.setAvailableUnits(available_units);
        rule.setValue(Rule.Entry.MAX_POPULATION, 10);
        return rule;
    }

    @Override
    public int getStartGold() {
        return 0;
    }

    @Override
    public int getPlayerTeam() {
        return 0;
    }

    @Override
    public String[] getObjectives() {
        if (getContext().get("reported") == 0) {
            return new String[]{Language.getText("CAMPAIGN_AEI_STAGE_2_OBJECTIVE_1")};
        } else {
            return new String[]{Language.getText("CAMPAIGN_AEI_STAGE_2_OBJECTIVE_2")};
        }
    }

    @Override
    public int getStageNumber() {
        return 1;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_AEI_STAGE_2_NAME");
    }

}
