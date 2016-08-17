package net.toyknight.aeii.campaign.aei;

import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 7/27/2016.
 */
public class AEIStage6 extends StageController {

    @Override
    public void onGameStart() {
        Message message1 = new Message(-1, Language.getText("CAMPAIGN_AEI_STAGE_6_MESSAGE_1"));
        getContext().message(message1);
        getContext().focus(13, 13);
        Message message2 = new Message(5, Language.getText("CAMPAIGN_AEI_STAGE_6_MESSAGE_2"));
        Message message3 = new Message(0, Language.getText("CAMPAIGN_AEI_STAGE_6_MESSAGE_3"));
        getContext().message(message2, message3);
        getContext().focus(1, 1);
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
        return "aei_c5.aem";
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
        rule.setValue(Rule.Entry.UNIT_CAPACITY, 25);
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
        return new String[]{Language.getText("CAMPAIGN_AEI_STAGE_6_OBJECTIVE")};
    }

    @Override
    public int getStageNumber() {
        return 5;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_AEI_STAGE_6_NAME");
    }

}
