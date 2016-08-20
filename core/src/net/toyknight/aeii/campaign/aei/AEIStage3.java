package net.toyknight.aeii.campaign.aei;

import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 7/25/2016.
 */
public class AEIStage3 extends StageController {

    @Override
    public void onGameStart() {
        getContext().code(14, 12, "element_chief");
        getContext().set_static(3, 14, true);
        getContext().set_static(3, 13, true);
        getContext().set_static(4, 13, true);
        Message message1 = new Message(-1, Language.getText("CAMPAIGN_AEI_STAGE_3_MESSAGE_1"));
        Message message2 = new Message(-1, Language.getText("CAMPAIGN_AEI_STAGE_3_MESSAGE_2"));
        getContext().message(message1, message2);
        getContext().destroy_tile(9, 12, 0);
        getContext().destroy_tile(8, 12, 0);
        getContext().destroy_tile(7, 12, 0);
        getContext().destroy_tile(6, 12, 0);
        getContext().destroy_tile(5, 12, 0);
        Message message3 = new Message(5, Language.getText("CAMPAIGN_AEI_STAGE_3_MESSAGE_3"));
        Message message4 = new Message(0, Language.getText("CAMPAIGN_AEI_STAGE_3_MESSAGE_4"));
        Message message5 = new Message(5, Language.getText("CAMPAIGN_AEI_STAGE_3_MESSAGE_5"));
        getContext().message(message3, message4, message5);
        getContext().focus(14, 13);
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
        if (isCommander(unit, getPlayerTeam()) || unit.getUnitCode().equals("element_chief")) {
            getContext().fail();
        }
    }

    @Override
    public void onTileRepaired(int x, int y) {
    }

    @Override
    public void onTileOccupied(int x, int y, int team) {
        if (getContext().count_castle(getPlayerTeam()) == 1) {
            getContext().clear();
        }
    }

    @Override
    public void onTurnStart(int turn) {
    }

    @Override
    public String getMapName() {
        return "aei_c2.aem";
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
        return new String[]{Language.getText("CAMPAIGN_AEI_STAGE_3_OBJECTIVE")};
    }

    @Override
    public int getStageNumber() {
        return 2;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_AEI_STAGE_3_NAME");
    }

}