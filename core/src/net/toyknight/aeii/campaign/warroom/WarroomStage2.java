package net.toyknight.aeii.campaign.warroom;

import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;
import java.util.Random;

/**
 * @author blackwave 8/25/2016.
 */
public class WarroomStage2 extends StageController {

    private void checkClear() {
        if (getContext().tile(13, 13).getTeam() == 1) {
            getContext().clear();
        }
    }

    @Override
    public void onGameStart() {
        getContext().head(0, 1);
        getContext().head(1, 0);
        getContext().alliance(0, 1);
        getContext().alliance(1, 0);
        getContext().alliance(2, 1);
        getContext().alliance(3, 2);
        getContext().focus(7, 1);
        Message message = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_2_MESSAGE_1"));
        getContext().message(message);
        getContext().focus(1, 9);
        Message message1 = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_2_MESSAGE_2"));
        getContext().message(message1);
        getContext().focus(13, 13);
        Message message2 = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_2_MESSAGE_3"));
        getContext().message(message2);
        getContext().gold(3, 3000);

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
            if (getContext().count_unit(2) == 0 && getContext().count_castle(2) == 0) {
                getContext().destroy_team(2);
            }
            if (getContext().count_unit(3) == 0 && getContext().count_castle(3) == 0) {
                getContext().destroy_team(3);
            }
            if(unit.getTeam() == 3) {
                getContext().gold(3, (getContext().get_gold(3) +  (int)(unit.getPrice() * 0.8)));
            }
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

        if(getContext().current_team() == 2) {
            getContext().reinforce(2,
                    new Reinforcement(1, 12, 6),
                    new Reinforcement(1, 14, 6),
                    new Reinforcement(1, 13, 5),
                    new Reinforcement(1, 13, 7));

            for (Unit unit : getContext().get_units(1)) {
                if(unit.getX() > 9 && unit.getY() > 2 && unit.getY() < 10)
                {
                    getContext().gold(2, getContext().get_gold(2) + unit.getPrice() / 10);
                }
            }
        }


    }

    @Override
    public String getMapName() {
        return "warroom_stage_2.aem";
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
        return 1;
    }

    @Override
    public String[] getObjectives() {
        return new String[]{
                Language.getText("CAMPAIGN_WARROOM_STAGE_2_OBJECTIVE_1"),
                Language.getText("CAMPAIGN_WARROOM_STAGE_2_OBJECTIVE_2"),
        };
    }

    @Override
    public int getStageNumber() {
        return 1;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_WARROOM_STAGE_2_NAME");
    }


}
