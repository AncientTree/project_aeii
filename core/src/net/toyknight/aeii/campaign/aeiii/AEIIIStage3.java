package net.toyknight.aeii.campaign.aeiii;

import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 6/29/2016.
 */
public class AEIIIStage3 extends StageController {

    private void checkClear() {

    }
    @Override
    public void onGameStart() {
        getContext().reinforce(0, 0, 5,
                new Reinforcement(9, 6, 5),
                new Reinforcement(0, 5, 4),
                new Reinforcement(0, 5, 6),
                new Reinforcement(1, 5, 5),
                new Reinforcement(13, 4, 6),
                new Reinforcement(15, 4, 4),
                new Reinforcement(12, 4, 5));
        Message message1 = new Message(5, AER.lang.getText("CAMPAIGN_AEIII_STAGE_3_MESSAGE_1"));
        Message message2 = new Message(4, AER.lang.getText("CAMPAIGN_AEIII_STAGE_3_MESSAGE_2"));
        Message message3 = new Message(5, AER.lang.getText("CAMPAIGN_AEIII_STAGE_3_MESSAGE_3"));
        getContext().message(message1, message2, message3);
        getContext().move(8, 7, 10, 5);
        getContext().move(8, 8, 10, 6);
        getContext().move(7, 8, 9, 6);
        getContext().move(9, 8, 11, 6);
        getContext().focus(10, 5);
        Message message4 = new Message(4, AER.lang.getText("CAMPAIGN_AEIII_STAGE_3_MESSAGE_4"));
        getContext().message(message4);
        getContext().move(8, 9, 8, 5);
        getContext().move(7, 9, 8, 4);
        getContext().move(9, 9, 8, 6);
        Message message5 = new Message(4, AER.lang.getText("CAMPAIGN_AEIII_STAGE_3_MESSAGE_5"));
        getContext().message(message5);
        getContext().focus(8, 5);
        Message message6 = new Message(5, AER.lang.getText("CAMPAIGN_AEIII_STAGE_3_MESSAGE_6"));
        getContext().message(message6);
        getContext().set("Commander", 0);
        getContext().set("Reinforce", 0);
        getContext().set("Ghost", 0);
        getContext().level_up(6, 5);
        getContext().level_up(4, 5);
    }

    @Override
    public void onUnitMoved(Unit unit, int x, int y) {
    }

    @Override
    public void onUnitStandby(Unit unit) {
        if(unit.isCommander() && unit.getTeam() == 0)
        {
            if((unit.getX() == 0 && unit.getY() == 5) || (unit.getX() == 14 && unit.getY() == 3))
                getContext().clear();
        }
    }

    @Override
    public void onUnitAttacked(Unit attacker, Unit defender) {
        if(defender.isCommander() && defender.getTeam() == 3)
        {
            getContext().set("Commander", 1);
        }
        else if(attacker.getIndex() == 14 && getContext().get("Ghost") == 0)
        {
            getContext().set("Ghost", 1);
            Message message10 = new Message(5, AER.lang.getText("CAMPAIGN_AEIII_STAGE_3_MESSAGE_10"));
            getContext().message(message10);
            Message message11 = new Message(5, AER.lang.getText("CAMPAIGN_AEIII_STAGE_3_MESSAGE_11"));
            getContext().message(message11);
        }
    }

    @Override
    public void onUnitDestroyed(Unit unit) {
        if (isCommander(unit, getPlayerTeam())) {
            getContext().fail();
        }
        else if(isCommander(unit, 3))
        {
            Message message8 = new Message(5, AER.lang.getText("CAMPAIGN_AEIII_STAGE_3_MESSAGE_8"));
            getContext().message(message8);
            getContext().clear();
        }
        else if(getContext().get("Reinforce") == 1 && getContext().count_unit(2) == 0 && getContext().count_unit(3) == 0)
        {
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
        if(turn == 3)
        {
            getContext().move(10, 5, 12, 3);
            getContext().get_unit(10, 5).setStandby(true);
            getContext().move(10, 6, 12, 4);
            getContext().get_unit(10, 6).setStandby(true);
            getContext().move(9, 6, 11, 4);
            getContext().get_unit(9, 6).setStandby(true);
            getContext().move(11, 6, 11, 3);
            getContext().get_unit(11, 6).setStandby(true);
        }
        if(turn == 6)
        {
            if(getContext().get("Commander") == 0)
            {
                getContext().move(12, 3, 14, 3);
                getContext().remove_unit(14, 3);
                getContext().move(12, 4, 14, 3);
                getContext().remove_unit(14, 3);
                getContext().move(11, 4, 14, 3);
                getContext().remove_unit(14, 3);
                getContext().move(11, 3, 14, 3);
                getContext().remove_unit(14, 3);
                Message message7 = new Message(5, AER.lang.getText("CAMPAIGN_AEIII_STAGE_3_MESSAGE_7"));
                getContext().message(message7);
            }
            else
            {
                getContext().move(12, 3, 14, 3);
                getContext().remove_unit(14, 3);
                Message message7 = new Message(5, AER.lang.getText("CAMPAIGN_AEIII_STAGE_3_MESSAGE_7"));
                getContext().message(message7);
            }
        }
        if(turn == 7)
        {
            getContext().reinforce(2,
                    new Reinforcement(14, 4, 1),
                    new Reinforcement(14, 3, 2),
                    new Reinforcement(14, 3, 8),
                    new Reinforcement(14, 4, 9),
                    new Reinforcement(14, 13, 1),
                    new Reinforcement(14, 14, 2),
                    new Reinforcement(14, 13, 9),
                    new Reinforcement(14, 14, 8));
            Message message9 = new Message(5, AER.lang.getText("CAMPAIGN_AEIII_STAGE_3_MESSAGE_9"));
            getContext().message(message9);
            getContext().set("Reinforce", 1);
        }
        else if(turn == 13)
        {
            getContext().reinforce(2,
                    new Reinforcement(14, 4, 1),
                    new Reinforcement(14, 3, 2),
                    new Reinforcement(2, 3, 1),
                    new Reinforcement(14, 3, 8),
                    new Reinforcement(14, 4, 9),
                    new Reinforcement(2, 3, 9),
                    new Reinforcement(14, 13, 1),
                    new Reinforcement(14, 14, 2),
                    new Reinforcement(2, 14, 1),
                    new Reinforcement(14, 13, 9),
                    new Reinforcement(14, 14, 8),
                    new Reinforcement(2, 14, 9));
            Message message13 = new Message(5, AER.lang.getText("CAMPAIGN_AEIII_STAGE_3_MESSAGE_13"));
            getContext().message(message13);
        }
    }

    @Override
    public String getMapName() {
        return "aeiii_c3.aem";
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

        if(getContext().get("Reinforce") == 0)
            return new String[]{AER.lang.getText("CAMPAIGN_AEIII_STAGE_3_OBJECTIVE1")};
        return new String[]{AER.lang.getText("CAMPAIGN_AEIII_STAGE_3_OBJECTIVE2")};
    }

    @Override
    public int getStageNumber() {
        return 2;
    }

    @Override
    public String getStageName() {
        return AER.lang.getText("CAMPAIGN_AEIII_STAGE_3_NAME");
    }

}
