package net.toyknight.aeii.campaign.warroom;

import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Ability;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author by toyknight 8/2/2016.
 */
public class WarroomStage3 extends StageController {

    private void checkClear() {
        if (getContext().count_unit(1) == 0 && getContext().count_castle(1) == 0) {
            getContext().clear();
        }
    }


    @Override
    public void onGameStart() {
        getContext().alliance(0, 0);
        getContext().alliance(1, 1);
        getContext().alliance(2, 1);
        getContext().alliance(3, 1);
        getContext().focus(2, 6);
        Message message = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_3_MESSAGE_1"));
        getContext().message(message);
        Message message1 = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_3_MESSAGE_2"));
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
        }
        else {
            if (getContext().count_unit(2) == 0) {
                getContext().destroy_team(2);
            }
            else ;
            if (getContext().count_unit(3) == 0) {
                getContext().destroy_team(3);
            }
            else ;
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
        if(turn > 50)
        {
            getContext().fail();;
        }
    }

    public void onTurnEnd(int turn) {
    }

    @Override
    public String getMapName() {
        return "warroom_stage_3.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        rule.setValue(Rule.Entry.UNIT_CAPACITY, 50);
        return rule;
    }

    @Override
    public int getStartGold() {
        return 450;
    }

    @Override
    public int getPlayerTeam() {
        return 0;
    }

    @Override
    public String[] getObjectives() {
        return new String[]{
                Language.getText("CAMPAIGN_WARROOM_STAGE_3_OBJECTIVE_1"),
                Language.getText("CAMPAIGN_WARROOM_STAGE_3_OBJECTIVE_2"),
                Language.getText("CAMPAIGN_WARROOM_STAGE_3_OBJECTIVE_3"),
        };
    }

    @Override
    public int getStageNumber() {
        return 2;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_WARROOM_STAGE_3_NAME");
    }

}
