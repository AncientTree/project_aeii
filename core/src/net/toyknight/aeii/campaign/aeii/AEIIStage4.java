package net.toyknight.aeii.campaign.aeii;

import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Tile;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 6/29/2016.
 */
public class AEIIStage4 extends StageController {

    private void checkClear() {
        Tile castle1 = getContext().tile(7, 1);
        Tile castle2 = getContext().tile(13, 9);
        if (getContext().count_unit(1) == 0 && castle1.getTeam() == getPlayerTeam() && castle2.getTeam() == getPlayerTeam()) {
            Message message6 = new Message(5, AER.lang.getText("CAMPAIGN_AEII_STAGE_4_MESSAGE_6"));
            Message message7 = new Message(0, AER.lang.getText("CAMPAIGN_AEII_STAGE_4_MESSAGE_7"));
            getContext().message(message6, message7);
            getContext().clear();
        }
    }

    @Override
    public void onGameStart() {
        getContext().focus(3, 3);
        Message message1 = new Message(2, AER.lang.getText("CAMPAIGN_AEII_STAGE_4_MESSAGE_1"));
        Message message2 = new Message(0, AER.lang.getText("CAMPAIGN_AEII_STAGE_4_MESSAGE_2"));
        Message message3 = new Message(5, AER.lang.getText("CAMPAIGN_AEII_STAGE_4_MESSAGE_3"));
        getContext().message(message1, message2, message3);
        getContext().focus(6, 10);
        getContext().move(9, 10, 6, 10);
        getContext().attack(4, 9, -1);
        getContext().destroy_tile(4, 9);
        Message message4 = new Message(5, AER.lang.getText("CAMPAIGN_AEII_STAGE_4_MESSAGE_4"));
        Message message5 = new Message(0, AER.lang.getText("CAMPAIGN_AEII_STAGE_4_MESSAGE_5"));
        getContext().message(message4, message5);
        getContext().focus(7, 1);
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
        } else {
            checkClear();
        }
    }

    @Override
    public void onTileRepaired(int x, int y) {
    }

    @Override
    public void onTileOccupied(int x, int y, int team) {
        if (team == getPlayerTeam()) {
            checkClear();
        }
    }

    @Override
    public void onTurnStart(int turn) {
    }

    @Override
    public String getMapName() {
        return "aeii_c3.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        Array<Integer> available_units = new Array<Integer>();
        for (int i = 0; i < 8; i++) {
            available_units.add(i);
        }
        available_units.add(9);
        rule.setAvailableUnits(available_units);
        rule.setValue(Rule.Entry.UNIT_CAPACITY, 25);
        return rule;
    }

    @Override
    public int getStartGold() {
        return 400;
    }

    @Override
    public int getPlayerTeam() {
        return 0;
    }

    @Override
    public String[] getObjectives() {
        return new String[]{AER.lang.getText("CAMPAIGN_AEII_STAGE_4_OBJECTIVE")};
    }

    @Override
    public int getStageNumber() {
        return 3;
    }

    @Override
    public String getStageName() {
        return AER.lang.getText("CAMPAIGN_AEII_STAGE_4_NAME");
    }

}
