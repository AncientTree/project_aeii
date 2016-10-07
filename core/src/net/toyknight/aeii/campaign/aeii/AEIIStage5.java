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
public class AEIIStage5 extends StageController {

    @Override
    public void onGameStart() {
        getContext().set("reinforced1", 0);
        getContext().set("reinforced2", 0);
        getContext().set("reinforced3", 0);
        getContext().set("reinforced4", 0);
        getContext().destroy_team(1);
        getContext().reinforce(0, 10, 0,
                new Reinforcement(0, 11, 2),
                new Reinforcement(9, 11, 1),
                new Reinforcement(11, 11, 0),
                new Reinforcement(5, 10, 1),
                new Reinforcement(3, 12, 1),
                new Reinforcement(1, 12, 0));
        getContext().focus(11, 1);
        Message message1 = new Message(5, Language.getText("CAMPAIGN_AEII_STAGE_5_MESSAGE_1"));
        Message message2 = new Message(0, Language.getText("CAMPAIGN_AEII_STAGE_5_MESSAGE_2"));
        getContext().message(message1, message2);
    }

    @Override
    public void onUnitMoved(Unit unit, int x, int y) {
    }

    @Override
    public void onUnitStandby(Unit unit) {
        if (isCrystal(unit, getPlayerTeam()) && unit.isAt(16, 11)) {
            getContext().clear();
        }
        if (unit.getTeam() == getPlayerTeam()
                && unit.getY() < 4 && unit.getX() < 8 && getContext().get("reinforced1") == 0) {
            reinforce1();
        }
        if (unit.getTeam() == getPlayerTeam()
                && unit.getY() > 6 && unit.getX() > 1 &&
                getContext().get("reinforced2") == 0) {
            reinforce2();
        }
        if (unit.getTeam() == getPlayerTeam()
                && unit.getY() > 4 && unit.getX() > 8 &&
                getContext().get("reinforced3") == 0) {
            reinforce3();
        }
        if (unit.getTeam() == getPlayerTeam()
                && unit.getY() > 7 && unit.getX() > 14 &&
                getContext().get("reinforced4") == 0) {
            reinforce4();
        }

    }

    public void reinforce1()
    {
        getContext().set("reinforced1", 1);
        getContext().restore(1);
        getContext().reinforce(1, 4, 4,
                new Reinforcement(10, 4, 1),
                new Reinforcement(1, 5, 2),
                new Reinforcement(10, 4, 3));
        Message message3 = new Message(5, Language.getText("CAMPAIGN_AEII_STAGE_5_MESSAGE_3"));
        getContext().message(message3);
    }

    public void reinforce2()
    {
        if(getContext().get("reinforced1") == 0)
        {
            reinforce1();
        }
        else;
        getContext().set("reinforced2", 1);
        getContext().restore(1);
        getContext().reinforce(1, 6, 10,
                new Reinforcement(5, 5, 10),
                new Reinforcement(5, 7, 8),
                new Reinforcement(1, 7, 9));
    }

    public void reinforce3()
    {
        if(getContext().get("reinforced2") == 0)
        {
            reinforce2();
        }
        else;
        getContext().set("reinforced3", 1);
        getContext().restore(1);
        getContext().reinforce(1, 12, 5,
                new Reinforcement(5, 11, 5),
                new Reinforcement(5, 12, 7),
                new Reinforcement(6, 12, 6));
    }

    public void reinforce4() {
        if(getContext().get("reinforced3") == 0)
        {
            reinforce3();
        }
        else;
        getContext().set("reinforced4", 1);
        getContext().restore(1);
        getContext().reinforce(1, 18, 8,
                new Reinforcement(5, 16, 10),
                new Reinforcement(6, 17, 10),
                new Reinforcement(5, 18, 10),
                new Reinforcement(1, 18, 9));
        Message message4 = new Message(0, Language.getText("CAMPAIGN_AEII_STAGE_5_MESSAGE_4"));
        getContext().message(message4);
    }

    @Override
    public void onUnitAttacked(Unit attacker, Unit defender) {
    }

    @Override
    public void onUnitDestroyed(Unit unit) {
        if (isCommander(unit, getPlayerTeam()) || isCrystal(unit, getPlayerTeam())) {
            getContext().fail();
        }
        if (getContext().count_unit(1) == 0) {
            getContext().destroy_team(1);
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
        return "aeii_c4.aem";
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
        return new String[]{Language.getText("CAMPAIGN_AEII_STAGE_5_OBJECTIVE")};
    }

    @Override
    public int getStageNumber() {
        return 4;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_AEII_STAGE_5_NAME");
    }

}
