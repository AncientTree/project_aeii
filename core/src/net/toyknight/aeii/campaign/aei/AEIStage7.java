package net.toyknight.aeii.campaign.aei;

import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 7/27/2016.
 */
public class AEIStage7 extends StageController {

    @Override
    public void onGameStart() {
        Message message1 = new Message(-1, AER.lang.getText("CAMPAIGN_AEI_STAGE_7_MESSAGE_1"));
        getContext().message(message1);
        getContext().reinforce(1,
                new Reinforcement(0, 13, 0),
                new Reinforcement(7, 12, 0));
        getContext().reinforce(1,
                new Reinforcement(0, 1, 12),
                new Reinforcement(6, 1, 11));
        getContext().reinforce(1,
                new Reinforcement(9, 2, 1, 1),
                new Reinforcement(0, 1, 2),
                new Reinforcement(8, 0, 1));
        getContext().focus(10, 13);
        Message message2 = new Message(0, AER.lang.getText("CAMPAIGN_AEI_STAGE_7_MESSAGE_2"));
        getContext().message(message2);
        getContext().focus(1, 1);
        Message message3 = new Message(1, AER.lang.getText("CAMPAIGN_AEI_STAGE_7_MESSAGE_3"));
        getContext().message(message3);
        getContext().focus(10, 13);
        Message message4 = new Message(0, AER.lang.getText("CAMPAIGN_AEI_STAGE_7_MESSAGE_4"));
        getContext().message(message4);
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
        if (isCommander(unit, getPlayerTeam())) {
            getContext().fail();
        }
        if (getContext().count_unit(1) == 0) {
            Message message5 = new Message(1, AER.lang.getText("CAMPAIGN_AEI_STAGE_7_MESSAGE_5"));
            Message message6 = new Message(0, AER.lang.getText("CAMPAIGN_AEI_STAGE_7_MESSAGE_6"));
            Message message7 = new Message(-1, AER.lang.getText("CAMPAIGN_AEI_STAGE_7_MESSAGE_7"));
            Message message8 = new Message(-1, AER.lang.getText("CAMPAIGN_AEI_STAGE_7_MESSAGE_8"));
            getContext().message(message5, message6, message7, message8);
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
    }

    @Override
    public String getMapName() {
        return "aei_c6.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        Array<Integer> available_units = new Array<Integer>();
        available_units.add(0);
        available_units.add(1);
        available_units.add(2);
        available_units.add(3);
        available_units.add(4);
        available_units.add(5);
        available_units.add(6);
        available_units.add(7);
        available_units.add(8);
        available_units.add(9);
        rule.setAvailableUnits(available_units);
        rule.setValue(Rule.Entry.UNIT_CAPACITY, 30);
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
        return new String[]{AER.lang.getText("CAMPAIGN_AEI_STAGE_7_OBJECTIVE")};
    }

    @Override
    public int getStageNumber() {
        return 6;
    }

    @Override
    public String getStageName() {
        return AER.lang.getText("CAMPAIGN_AEI_STAGE_7_NAME");
    }

}
