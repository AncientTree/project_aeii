package net.toyknight.aeii.campaign.aeii;

import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/29/2016.
 */
public class AEIIStage3 extends StageController {

    @Override
    public void onGameStart() {
        getContext().set("reinforced", 0);
        getContext().reinforce(0, 8, 16,
                new Reinforcement(9, 8, 14),
                new Reinforcement(0, 7, 14),
                new Reinforcement(1, 7, 15),
                new Reinforcement(3, 8, 15));
        Message message1 = new Message(5, Language.getText("CAMPAIGN_AEII_STAGE_3_MESSAGE_1"));
        Message message2 = new Message(0, Language.getText("CAMPAIGN_AEII_STAGE_3_MESSAGE_2"));
        Message message3 = new Message(5, Language.getText("CAMPAIGN_AEII_STAGE_3_MESSAGE_3"));
        getContext().message(message1, message2, message3);
    }

    @Override
    public void onUnitMoved(Unit unit, int x, int y) {
    }

    @Override
    public void onUnitStandby(Unit unit) {
        if (unit.getTeam() == getPlayerTeam() && getContext().get("reinforced") == 0 && unit.getY() <= 10) {
            getContext().set("reinforced", 1);
            getContext().reinforce(1,
                    new Reinforcement(5, 0, 8),
                    new Reinforcement(5, 1, 7));
            getContext().reinforce(1, new Reinforcement(5, 8, 6));
            Message message4 = new Message(5, Language.getText("CAMPAIGN_AEII_STAGE_3_MESSAGE_4"));
            getContext().message(message4);
            getContext().reinforce(1,
                    new Reinforcement(2, 3, 8),
                    new Reinforcement(4, 4, 7),
                    new Reinforcement(2, 5, 8));
            Message message5 = new Message(5, Language.getText("CAMPAIGN_AEII_STAGE_3_MESSAGE_5"));
            Message message6 = new Message(-1, Language.getText("CAMPAIGN_AEII_STAGE_3_MESSAGE_6"));
            Message message7 = new Message(0, Language.getText("CAMPAIGN_AEII_STAGE_3_MESSAGE_7"));
            getContext().message(message5, message6, message7);
            getContext().team(3, 8, 0);
            getContext().team(4, 7, 0);
            getContext().team(5, 8, 0);
        }
        else if(unit.getTeam() == getPlayerTeam() && getContext().get("reinforced") == 1 && unit.getY() <= 5)
        {
            getContext().reinforce(1,
                    new Reinforcement(5, 1, 2),
                    new Reinforcement(4, 2, 1),
                    new Reinforcement(5, 3, 2));
        }
        if (isCommander(unit, getPlayerTeam()) && getContext().get("reinforced") == 1 && unit.getX() == 2 && unit.getY() <= 1) {
            getContext().clear();
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
    public String getMapName() {
        return "aeii_c2.aem";
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
        return new String[]{Language.getText("CAMPAIGN_AEII_STAGE_3_OBJECTIVE")};
    }

    @Override
    public int getStageNumber() {
        return 2;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_AEII_STAGE_3_NAME");
    }

}
