package net.toyknight.aeii.campaign.tutorial;

import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/25/2016.
 */
public class TutorialStage1 extends StageController {

    @Override
    public void onGameStart() {
        getContext().set("moved", 0);
        getContext().set("attacked", 0);
        Message message1 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_1_MESSAGE_1"));
        Message message2 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_1_MESSAGE_2"));
        Message message3 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_1_MESSAGE_3"));
        Message message4 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_1_MESSAGE_4"));
        Message message5 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_1_MESSAGE_5"));
        Message message6 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_1_MESSAGE_6"));
        getContext().message(message1, message2, message3, message4, message5, message6);
    }

    @Override
    public void onUnitMoved(Unit unit, int x, int y) {
        int moved = getContext().get("moved");
        if (unit.getTeam() == getPlayerTeam() && unit.isCommander() && x == 4 && y == 4 && moved == 0) {
            Message message7 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_1_MESSAGE_7"));
            Message message8 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_1_MESSAGE_8"));
            getContext().message(message7, message8);
            getContext().set("moved", 1);
        }
    }

    @Override
    public void onUnitStandby(Unit unit) {
    }

    @Override
    public void onUnitAttacked(Unit attacker, Unit defender) {
        int attacked = getContext().get("attacked");
        if (attacker.getTeam() == getPlayerTeam() && attacker.isCommander() && attacked == 0) {
            Message message11 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_1_MESSAGE_11"));
            getContext().message(message11);
            getContext().set("attacked", 1);
        }
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

    }

    @Override
    public void onTileOccupied(int x, int y, int team) {

    }

    @Override
    public void onTurnStart(int turn) {
        if (turn == 3) {
            Message message9 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_1_MESSAGE_9"));
            Message message10 = new Message(5, Language.getText("CAMPAIGN_TUTORIAL_STAGE_1_MESSAGE_10"));
            getContext().message(message9, message10);
        }
    }

    @Override
    public void onTurnEnd(int turn) {

    }

    @Override
    public String getMapName() {
        return "tutorial_stage_1.aem";
    }

    @Override
    public Rule getRule() {
        return Rule.createDefault();
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
        return new String[]{
                Language.getText("CAMPAIGN_TUTORIAL_STAGE_1_OBJECTIVE_1"),
                Language.getText("CAMPAIGN_TUTORIAL_STAGE_1_OBJECTIVE_2")
        };
    }

    @Override
    public int getStageNumber() {
        return 0;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_TUTORIAL_STAGE_1_NAME");
    }

}
