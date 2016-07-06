package net.toyknight.aeii.campaign.aeii;

import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/28/2016.
 */
public class AEIIStage2 extends StageController {

    private void checkClear() {
        if (getContext().tile(12, 3).getTeam() == getPlayerTeam() &&
                getContext().tile(3, 5).getTeam() == getPlayerTeam() &&
                getContext().count_unit(1) == 0) {
            Message message9 = new Message(1, Language.getText("CAMPAIGN_AEII_STAGE_2_MESSAGE_9"));
            Message message10 = new Message(5, Language.getText("CAMPAIGN_AEII_STAGE_2_MESSAGE_10"));
            getContext().message(message9, message10);
            getContext().clear();
        }
    }

    @Override
    public void onGameStart() {
        getContext().head(0, 2);
        getContext().reinforce(0, 8, 11,
                new Reinforcement(9, 8, 9),
                new Reinforcement(0, 7, 10),
                new Reinforcement(1, 9, 10));
        Message message1 = new Message(1, Language.getText("CAMPAIGN_AEII_STAGE_2_MESSAGE_1"));
        getContext().message(message1);
        getContext().focus(12, 3);
        Message message2 = new Message(3, Language.getText("CAMPAIGN_AEII_STAGE_2_MESSAGE_2"));
        getContext().message(message2);
        getContext().focus(7, 3);
        Message message3 = new Message(5, Language.getText("CAMPAIGN_AEII_STAGE_2_MESSAGE_3"));
        getContext().message(message3);
        getContext().steal_crystal(7, 3, 6, -1);
        getContext().create(0, 0, 7, 3);
        Message message4 = new Message(2, Language.getText("CAMPAIGN_AEII_STAGE_2_MESSAGE_4"));
        getContext().message(message4);
        getContext().move(7, 3, 6, 2);
        getContext().attack(6, 2, -1);
        Message message5 = new Message(2, Language.getText("CAMPAIGN_AEII_STAGE_2_MESSAGE_5"));
        getContext().message(message5);
        getContext().destroy_unit(6, 2);
        getContext().remove_unit(6, 0);
        getContext().focus(8, 9);
        Message message6 = new Message(5, Language.getText("CAMPAIGN_AEII_STAGE_2_MESSAGE_6"));
        Message message7 = new Message(1, Language.getText("CAMPAIGN_AEII_STAGE_2_MESSAGE_7"));
        getContext().message(message6, message7);
        getContext().focus(3, 5);
        Message message8 = new Message(5, Language.getText("CAMPAIGN_AEII_STAGE_2_MESSAGE_8"));
        getContext().message(message8);
        getContext().focus(8, 9);
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
    }

    @Override
    public void onTurnEnd(int turn) {
    }

    @Override
    public String getMapName() {
        return "aeii_c1.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        Array<Integer> available_units = new Array<Integer>();
        available_units.add(0);
        available_units.add(1);
        available_units.add(9);
        rule.setAvailableUnits(available_units);
        rule.setValue(Rule.Entry.MAX_POPULATION, 10);
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
        return new String[]{Language.getText("CAMPAIGN_AEII_STAGE_2_OBJECTIVE")};
    }

    @Override
    public int getStageNumber() {
        return 1;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_AEII_STAGE_2_NAME");
    }

}
