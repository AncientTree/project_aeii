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
public class TutorialStage3 extends StageController {

    private void clear() {
        Message message9 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_3_MESSAGE_9"));
        Message message10 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_3_MESSAGE_10"));
        getContext().message(message9, message10);
        getContext().clear();
    }

    @Override
    public void onGameStart() {
        Message message1 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_3_MESSAGE_1"));
        Message message2 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_3_MESSAGE_2"));
        Message message3 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_3_MESSAGE_3"));
        Message message4 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_3_MESSAGE_4"));
        Message message5 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_3_MESSAGE_5"));
        Message message6 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_3_MESSAGE_6"));
        Message message7 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_3_MESSAGE_7"));
        Message message8 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_3_MESSAGE_8"));
        getContext().message(
                message1, message2, message3, message4, message5, message6, message7, message8);
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
        if (unit.getTeam() == getPlayerTeam() && unit.isCommander()) {
            getContext().fail();
        }
        if (getContext().count_unit(1) == 0 && getContext().tile(4, 0).getTeam() == getPlayerTeam()) {
            clear();
        }
    }

    @Override
    public void onTileRepaired(int x, int y) {
    }

    @Override
    public void onTileOccupied(int x, int y, int team) {
        if (x == 4 && y == 0 && getContext().count_unit(1) == 0) {
            clear();
        }
    }

    @Override
    public void onTurnStart(int turn) {
    }

    @Override
    public String getMapName() {
        return "tutorial_stage_3.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        Array<Integer> available_units = new Array<Integer>();
        available_units.add(0);
        available_units.add(1);
        available_units.add(2);
        available_units.add(3);
        rule.setAvailableUnits(available_units);
        rule.setValue(Rule.Entry.UNIT_CAPACITY, 20);
        return rule;
    }

    @Override
    public int getStartGold() {
        return 500;
    }

    @Override
    public int getPlayerTeam() {
        return 0;
    }

    @Override
    public String[] getObjectives() {
        return new String[]{
                Language.getText("CAMPAIGN_TUTORIAL_STAGE_3_OBJECTIVE_1"),
                Language.getText("CAMPAIGN_TUTORIAL_STAGE_3_OBJECTIVE_2")
        };
    }

    @Override
    public int getStageNumber() {
        return 2;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_TUTORIAL_STAGE_3_NAME");
    }

}
