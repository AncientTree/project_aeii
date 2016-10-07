package net.toyknight.aeii.campaign.warroom;

import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Ability;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author blackwave 9/22/2016.
 */
public class WarroomStage5 extends StageController {

    private void checkClear() {
        if (getContext().count_unit(1) == 0 && getContext().count_castle(1) == 0) {
            getContext().clear();
        }
    }


    @Override
    public void onGameStart() {
        Message message = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_5_MESSAGE_1"));
        getContext().message(message);
        getContext().gold(0, 1500);
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
        else if(unit.getIndex() == 7){
            Message message1 = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_5_MESSAGE_2"));
            getContext().message(message1);
            for (Unit player : getContext().get_units(0)) {

                if(player.getMaxHp() - player.getCurrentHp() <= 50) {
                    getContext().hp_change(player.getX(), player.getY(), player.getMaxHp() - player.getCurrentHp());
                }
                else
                {
                    getContext().hp_change(player.getX(), player.getY(), 50);
                }
                getContext().level_up(player.getX(), player.getY());
            }

        }
        else{
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

    public void onTurnEnd(int turn) {
    }

    @Override
    public String getMapName() {
        return "warroom_stage_5.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        rule.getAvailableUnits().removeValue(7, false);
        rule.setValue(Rule.Entry.UNIT_CAPACITY, 40);
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
        return new String[]{
                Language.getText("CAMPAIGN_WARROOM_STAGE_5_OBJECTIVE_1"),
                Language.getText("CAMPAIGN_WARROOM_STAGE_5_OBJECTIVE_2"),
        };
    }

    @Override
    public int getStageNumber() {
        return 4;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_WARROOM_STAGE_5_NAME");
    }

}
