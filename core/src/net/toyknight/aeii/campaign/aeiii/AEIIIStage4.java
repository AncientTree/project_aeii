package net.toyknight.aeii.campaign.aeiii;

import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 6/28/2016.
 */
public class AEIIIStage4 extends StageController {

    private void checkClear() {
        if (getContext().tile(4, 2).getTeam() == getPlayerTeam()) {
            Message message6 = new Message(1, AER.lang.getText("CAMPAIGN_AEIII_STAGE_2_MESSAGE_6"));
            getContext().message(message6);
            getContext().clear();
        }
    }

    @Override
    public void onGameStart() {
        getContext().head(0, 2);
        Message message1 = new Message(1, AER.lang.getText("CAMPAIGN_AEIII_STAGE_2_MESSAGE_1"));
        getContext().message(message1);
        Message message2 = new Message(1, AER.lang.getText("CAMPAIGN_AEIII_STAGE_2_MESSAGE_2"));
        getContext().message(message2);
        getContext().set("Dead_count", 0);
        getContext().set("Commander", 0);
        getContext().set("Reinforce", 0);
        getContext().set("Mage", 0);
        getContext().level_up(4,2);
        getContext().level_up(4,2);
        getContext().level_up(1, 9);
        getContext().level_up(1, 10);
    }

    @Override
    public void onUnitMoved(Unit unit, int x, int y) {
    }

    @Override
    public void onUnitStandby(Unit unit) {
        if(unit.getTeam() == getPlayerTeam() && unit.getY() <= 5)
        {
            getContext().set("Commander", 1);
        }
    }

    @Override
    public void onUnitAttacked(Unit attacker, Unit defender) {
        if(attacker.getIndex() == 15 && getContext().get("Mage") == 0)
        {
            Message message5 = new Message(1, AER.lang.getText("CAMPAIGN_AEIII_STAGE_2_MESSAGE_5"));
            getContext().message(message5);
            getContext().set("Mage", 1);
        }
    }

    @Override
    public void onUnitDestroyed(Unit unit) {
        if (unit.getTeam() == getPlayerTeam() && unit.isCommander()) {
            getContext().fail();
        }
        else if(unit.getTeam() == getPlayerTeam())
        {
            if(getContext().get("Dead_count") < 3) {
                getContext().set("Dead_count", getContext().get("Dead_count") + 1);
                Message message3 = new Message(1, AER.lang.getText("CAMPAIGN_AEIII_STAGE_2_MESSAGE_3"));
                getContext().message(message3);
            }
        }
        checkClear();
    }

    @Override
    public void onTileRepaired(int x, int y) {
    }

    @Override
    public void onTileOccupied(int x, int y, int team) {
        checkClear();
    }

    @Override
    public void onTurnStart(int turn) {
        if(turn == 2 || turn == 4)
        {
            getContext().get_unit(1, 0).setStandby(true);
            getContext().get_unit(2, 0).setStandby(true);
            getContext().get_unit(1, 1).setStandby(true);
            getContext().get_unit(2, 1).setStandby(true);
            getContext().get_unit(4, 2).setStandby(true);
        }
        else if(turn % 2 == 0)
        {
            if(getContext().get("Commander") == 0)
            {
                getContext().get_unit(4, 2).setStandby(true);
            }
        }
        else if(turn % 2 == 1)
        {
            if(getContext().get("Dead_count") >= 3 && getContext().get("Reinforce") == 0)
            {
                reinforce();
                getContext().set("Reinforce", 1);
            }
        }
    }

    private void reinforce()
    {
        getContext().reinforce(0, 6, 9,
                new Reinforcement(9, 5, 9),
                new Reinforcement(15, 6, 8),
                new Reinforcement(15, 6, 9),
                new Reinforcement(15, 6, 10));
        Message message4 = new Message(1, AER.lang.getText("CAMPAIGN_AEIII_STAGE_2_MESSAGE_4"));
        getContext().message(message4);
    }

    @Override
    public String getMapName() {
        return "aeiii_c1.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        Array<Integer> available_units = new Array<Integer>();
        available_units.add(13);
        rule.setAvailableUnits(available_units);
        rule.setValue(Rule.Entry.UNIT_CAPACITY, 20);
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
        return new String[]{AER.lang.getText("CAMPAIGN_AEIII_STAGE_2_OBJECTIVE")};
    }

    @Override
    public int getStageNumber() {
        return 1;
    }

    @Override
    public String getStageName() {
        return AER.lang.getText("CAMPAIGN_AEIII_STAGE_2_NAME");
    }

}
