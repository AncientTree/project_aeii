package net.toyknight.aeii.campaign.aei;

import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 7/25/2016.
 */
public class AEIStage1 extends StageController {

    @Override
    public void onGameStart() {
        getContext().alliance(1, 1);
        getContext().set("reinforced", 0);
        Message message1 = new Message(-1, Language.getText("CAMPAIGN_AEI_STAGE_1_MESSAGE_1"));
        Message message2 = new Message(-1, Language.getText("CAMPAIGN_AEI_STAGE_1_MESSAGE_2"));
        Message message3 = new Message(5, Language.getText("CAMPAIGN_AEI_STAGE_1_MESSAGE_3"));
        Message message4 = new Message(0, Language.getText("CAMPAIGN_AEI_STAGE_1_MESSAGE_4"));
        Message message5 = new Message(0, Language.getText("CAMPAIGN_AEI_STAGE_1_MESSAGE_5"));
        getContext().message(message1, message2, message3, message4, message5);
    }

    @Override
    public void onUnitMoved(Unit unit, int x, int y) {
    }

    @Override
    public void onUnitStandby(Unit unit) {
        if (unit.getX() >= 1 && unit.getY() >= 5 && getContext().get("reinforced") == 0) {
            getContext().set("reinforced", 1);
            getContext().restore(1);
            getContext().reinforce(1, 8, 9, new Reinforcement(0, 5, 8));
            Message message6 = new Message(5, Language.getText("CAMPAIGN_AEI_STAGE_1_MESSAGE_6"));
            getContext().message(message6);
        }
    }

    @Override
    public void onUnitAttacked(Unit attacker, Unit defender) {
    }

    @Override
    public void onUnitDestroyed(Unit unit) {
        if (getContext().count_unit(1) == 0) {
            getContext().destroy_team(1);
        }
        if (isCommander(unit, getPlayerTeam())) {
            getContext().fail();
        }
    }

    @Override
    public void onTileRepaired(int x, int y) {
    }

    @Override
    public void onTileOccupied(int x, int y, int team) {
        if (team == getPlayerTeam() && getContext().count_castle(team) == 1) {
            getContext().clear();
        }
    }

    @Override
    public void onTurnStart(int turn) {
    }

    @Override
    public void onTurnEnd(int turn) {
    }

    @Override
    public String getMapName() {
        return "aei_c0.aem";
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
        return new String[]{Language.getText("CAMPAIGN_AEI_STAGE_1_OBJECTIVE")};
    }

    @Override
    public int getStageNumber() {
        return 0;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_AEI_STAGE_1_NAME");
    }

}
