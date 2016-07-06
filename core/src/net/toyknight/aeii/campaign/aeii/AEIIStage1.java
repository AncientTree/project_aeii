package net.toyknight.aeii.campaign.aeii;

import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/28/2016.
 */
public class AEIIStage1 extends StageController {

    @Override
    public void onGameStart() {
        getContext().set("reinforced", 0);
        getContext().reinforce(0, 0, 9,
                new Reinforcement(9, 4, 9),
                new Reinforcement(0, 3, 8),
                new Reinforcement(1, 3, 10));
        getContext().focus(9, 3);
        Message message1 = new Message(2, Language.getText("CAMPAIGN_AEII_STAGE_1_MESSAGE_1"));
        getContext().message(message1);
        getContext().attack(9, 3, -1);
        Message message2 = new Message(2, Language.getText("CAMPAIGN_AEII_STAGE_1_MESSAGE_2"));
        getContext().message(message2);
        getContext().destroy_unit(9, 3);
        getContext().focus(4, 9);
        Message message3 = new Message(0, Language.getText("CAMPAIGN_AEII_STAGE_1_MESSAGE_3"));
        Message message4 = new Message(5, Language.getText("CAMPAIGN_AEII_STAGE_1_MESSAGE_4"));
        getContext().message(message3, message4);
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
        if (getContext().count_unit(1) == 0) {
            if (getContext().get("reinforced") == 0) {
                getContext().reinforce(1, new Reinforcement(0, 10, 10));
                getContext().move(10, 10, 10, 9);
                getContext().reinforce(1, new Reinforcement(1, 1, 1));
                getContext().move(1, 1, 2, 1);
                getContext().set("reinforced", 1);
                Message message5 = new Message(5, Language.getText("CAMPAIGN_AEII_STAGE_1_MESSAGE_5"));
                Message message6 = new Message(0, Language.getText("CAMPAIGN_AEII_STAGE_1_MESSAGE_6"));
                getContext().message(message5, message6);
            } else {
                Message message7 = new Message(2, Language.getText("CAMPAIGN_AEII_STAGE_1_MESSAGE_7"));
                Message message8 = new Message(0, Language.getText("CAMPAIGN_AEII_STAGE_1_MESSAGE_8"));
                Message message9 = new Message(2, Language.getText("CAMPAIGN_AEII_STAGE_1_MESSAGE_9"));
                Message message10 = new Message(0, Language.getText("CAMPAIGN_AEII_STAGE_1_MESSAGE_10"));
                getContext().message(message7, message8, message9, message10);
                getContext().clear();
            }
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
    }

    @Override
    public void onTurnEnd(int turn) {
    }

    @Override
    public String getMapName() {
        return "aeii_c0.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        rule.setValue(Rule.Entry.CASTLE_INCOME, 0);
        rule.setValue(Rule.Entry.VILLAGE_INCOME, 0);
        rule.setValue(Rule.Entry.COMMANDER_INCOME, 0);
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
        return new String[]{Language.getText("CAMPAIGN_AEII_STAGE_1_OBJECTIVE")};
    }

    @Override
    public int getStageNumber() {
        return 0;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_AEII_STAGE_1_NAME");
    }

}
