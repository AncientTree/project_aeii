package net.toyknight.aeii.campaign.tutorial;

import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/26/2016.
 */
public class TutorialStage2 extends StageController {

    @Override
    public void onGameStart() {
        getContext().set("occupied_castle", 0);
        getContext().set("repaired_village_left", 0);
        getContext().set("occupied_village_left", 0);
        getContext().set("repaired_village_right", 0);
        getContext().set("occupied_village_right", 0);
        Message message1 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_2_MESSAGE_1"));
        Message message2 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_2_MESSAGE_2"));
        Message message3 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_2_MESSAGE_3"));
        Message message4 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_2_MESSAGE_4"));
        getContext().message(message1, message2, message3, message4);
    }

    @Override
    public void onUnitMoved(Unit unit, int x, int y) {
    }

    @Override
    public void onUnitAttacked(Unit attacker, Unit defender) {
    }

    @Override
    public void onUnitDestroyed(Unit unit) {
        if (unit.getTeam() == getPlayerTeam() && unit.isCommander()) {
            getContext().fail();
        }
        if (getContext().count(1) == 0) {
            getContext().clear();
        }
    }

    @Override
    public void onTileRepaired(int x, int y) {
        if (x == 2 && y == 7) {
            getContext().set("repaired_village_left", 1);
        }
        if (x == 6 && y == 7) {
            getContext().set("repaired_village_right", 1);
        }
        if (getContext().get("repaired_village_left") == 1 && getContext().get("repaired_village_right") == 1) {
            Message message9 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_2_MESSAGE_9"));
            Message message10 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_2_MESSAGE_10"));
            getContext().message(message9, message10);
        }
    }

    @Override
    public void onTileOccupied(int x, int y, int team) {
        if (x == 4 && y == 7 && team == getPlayerTeam()) {
            Message message5 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_2_MESSAGE_5"));
            Message message6 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_2_MESSAGE_6"));
            Message message7 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_2_MESSAGE_7"));
            Message message8 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_2_MESSAGE_8"));
            getContext().message(message5, message6, message7, message8);
        }
        if (x == 2 && y == 7 && team == getPlayerTeam()) {
            getContext().set("occupied_village_left", 1);
        }
        if (x == 6 && y == 7 && team == getPlayerTeam()) {
            getContext().set("occupied_village_right", 1);
        }
        if (getContext().get("occupied_village_left") == 1 && getContext().get("occupied_village_right") == 1) {
            Message message11 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_2_MESSAGE_11"));
            getContext().message(message11);
        }
    }

    @Override
    public void onTurnStart(int turn) {
    }

    @Override
    public void onTurnEnd(int turn) {
    }

    @Override
    public String getMapName() {
        return "tutorial_stage_2.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        Array<Integer> available_units = new Array<Integer>();
        available_units.add(0);
        available_units.add(1);
        rule.setAvailableUnits(available_units);
        rule.setValue(Rule.Entry.MAX_POPULATION, 20);
        return rule;
    }

    @Override
    public int getStartGold() {
        return 450;
    }

    @Override
    public int getPlayerTeam() {
        return 0;
    }

    @Override
    public String[] getObjectives() {
        return new String[]{
                Language.getText("CAMPAIGN_TUTORIAL_STAGE_2_OBJECTIVE_1"),
                Language.getText("CAMPAIGN_TUTORIAL_STAGE_2_OBJECTIVE_2")
        };
    }

    @Override
    public int getStageNumber() {
        return 1;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_TUTORIAL_STAGE_2_NAME");
    }

}
