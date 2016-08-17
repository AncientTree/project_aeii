package net.toyknight.aeii.campaign.aeii;

import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Tile;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/29/2016.
 */
public class AEIIStage6 extends StageController {

    private void checkClear() {
        Tile castle1 = getContext().tile(5, 17);
        Tile castle2 = getContext().tile(5, 5);
        if (getContext().count_unit(1) == 0 && castle1.getTeam() == getPlayerTeam() && castle2.getTeam() == getPlayerTeam()) {
            Message message2 = new Message(1, Language.getText("CAMPAIGN_AEII_STAGE_6_MESSAGE_2"));
            getContext().message(message2);
            getContext().clear();
        }
    }

    @Override
    public void onGameStart() {
        getContext().head(0, 2);
        getContext().focus(5, 17);
        Message message1 = new Message(1, Language.getText("CAMPAIGN_AEII_STAGE_6_MESSAGE_1"));
        getContext().message(message1);
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
        return "aeii_c5.aem";
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
        rule.setValue(Rule.Entry.UNIT_CAPACITY, 30);
        return rule;
    }

    @Override
    public int getStartGold() {
        return 600;
    }

    @Override
    public int getPlayerTeam() {
        return 0;
    }

    @Override
    public String[] getObjectives() {
        return new String[]{Language.getText("CAMPAIGN_AEII_STAGE_6_OBJECTIVE")};
    }

    @Override
    public int getStageNumber() {
        return 5;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_AEII_STAGE_6_NAME");
    }

}
