package net.toyknight.aeii.campaign.aeii;

import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/30/2016.
 */
public class AEIIStage8 extends StageController {

    @Override
    public void onGameStart() {
        getContext().head(7, 4, 3);
        getContext().head(6, 15, 2);
        getContext().code(7, 4, "saeth");
        getContext().set_static(7, 4, true);
        getContext().focus(8, 15);
        Message message1 = new Message(0, Language.getText("CAMPAIGN_AEII_STAGE_8_MESSAGE_1"));
        getContext().message(message1);
        getContext().focus(7, 4);
        Message message2 = new Message(4, Language.getText("CAMPAIGN_AEII_STAGE_8_MESSAGE_2"));
        Message message3 = new Message(4, Language.getText("CAMPAIGN_AEII_STAGE_8_MESSAGE_3"));
        getContext().message(message2, message3);
        getContext().focus(6, 15);
        Message message4 = new Message(1, Language.getText("CAMPAIGN_AEII_STAGE_8_MESSAGE_4"));
        getContext().message(message4);
        getContext().focus(7, 4);
        Message message5 = new Message(4, Language.getText("CAMPAIGN_AEII_STAGE_8_MESSAGE_5"));
        getContext().message(message5);
        getContext().move(5, 2, 7, 2);
        getContext().remove_unit(7, 2);
        getContext().move(9, 2, 7, 2);
        getContext().remove_unit(7, 2);
        getContext().move(7, 3, 7, 2);
        getContext().remove_unit(7, 2);
        getContext().move(7, 4, 7, 2);
        getContext().havens_fury(0, 9, 15, 0);
        getContext().destroy_unit(9, 15);
        getContext().focus(8, 15);
        Message message6 = new Message(0, Language.getText("CAMPAIGN_AEII_STAGE_8_MESSAGE_6"));
        getContext().message(message6);
        getContext().focus(7, 2);
        Message message7 = new Message(4, Language.getText("CAMPAIGN_AEII_STAGE_8_MESSAGE_7"));
        getContext().message(message7);
        getContext().focus(6, 15);
        Message message8 = new Message(1, Language.getText("CAMPAIGN_AEII_STAGE_8_MESSAGE_8"));
        Message message9 = new Message(5, Language.getText("CAMPAIGN_AEII_STAGE_8_MESSAGE_9"));
        getContext().message(message8, message9);
        getContext().focus(8, 15);
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
            Message message10 = new Message(4, Language.getText("CAMPAIGN_AEII_STAGE_8_MESSAGE_10"));
            Message message11 = new Message(0, Language.getText("CAMPAIGN_AEII_STAGE_8_MESSAGE_11"));
            Message message12 = new Message(4, Language.getText("CAMPAIGN_AEII_STAGE_8_MESSAGE_12"));
            Message message13 = new Message(5, Language.getText("CAMPAIGN_AEII_STAGE_8_MESSAGE_13"));
            Message message14 = new Message(1, Language.getText("CAMPAIGN_AEII_STAGE_8_MESSAGE_14"));
            Message message15 = new Message(0, Language.getText("CAMPAIGN_AEII_STAGE_8_MESSAGE_15"));
            Message message16 = new Message(0, Language.getText("CAMPAIGN_AEII_STAGE_8_MESSAGE_16"));
            getContext().message(message10, message11, message12, message13, message14, message15, message16);
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
        if (turn % 2 == 0) {
            getContext().havens_fury(0, -1, -1, 99);
        }
    }

    @Override
    public String getMapName() {
        return "aeii_c7.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        Array<Integer> available_units = new Array<Integer>();
        for (int i = 0; i <= 9; i++) {
            available_units.add(i);
        }
        rule.setAvailableUnits(available_units);
        rule.setValue(Rule.Entry.UNIT_CAPACITY, 40);
        return rule;
    }

    @Override
    public int getStartGold() {
        return 800;
    }

    @Override
    public int getPlayerTeam() {
        return 0;
    }

    @Override
    public String[] getObjectives() {
        return new String[]{Language.getText("CAMPAIGN_AEII_STAGE_8_OBJECTIVE")};
    }

    @Override
    public int getStageNumber() {
        return 7;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_AEII_STAGE_8_NAME");
    }

}
