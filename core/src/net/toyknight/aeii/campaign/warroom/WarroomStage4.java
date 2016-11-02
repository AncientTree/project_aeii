package net.toyknight.aeii.campaign.warroom;


import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;
import net.toyknight.aeii.entity.Tile;



/**
 * Created by liuji_000 on 8/27/2016.
 */
public class WarroomStage4 extends StageController {



    private void checkClear() {
        if (getContext().count_castle(1) == 0) {
            getContext().clear();
        }
    }

    @Override
    public void onGameStart() {
        getContext().focus(7, 7);
        Message message = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_4_MESSAGE_1"));
        getContext().message(message);
        getContext().focus(1, 1);
        getContext().reinforce(1,
                new Reinforcement(1, 0, 0),
                new Reinforcement(0, 0, 1),
                new Reinforcement(0, 1, 0));
        getContext().attack(1 ,1, -1);
        getContext().destroy_unit(1, 1);
        Message message1 = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_4_MESSAGE_2"));
        getContext().message(message1);
        getContext().reinforce(1,
                new Reinforcement(1, 0, 11),
                new Reinforcement(0, 0, 10),
                new Reinforcement(0, 1, 11));
        getContext().attack(1 ,10, -1);
        getContext().destroy_unit(1, 10);
        getContext().reinforce(1,
                new Reinforcement(1, 6, 0),
                new Reinforcement(0, 7, 0),
                new Reinforcement(0, 6, 1));
        getContext().attack(7 ,1, -1);
        getContext().destroy_unit(7, 1);
        Message message2 = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_4_MESSAGE_3"));
        getContext().message(message2);
        getContext().focus(5, 6);
        getContext().move(5, 6, 5, 3);
        getContext().focus(5, 3);
        getContext().move(5, 3, 7, 1);
        getContext().focus(5, 7);
        getContext().move(5, 7, 1, 7);
        getContext().focus(1, 7);
        getContext().move(1, 7, 1, 4);
        getContext().focus(1, 4);
        getContext().move(1, 4, 1, 1);
        getContext().focus(5, 8);
        getContext().move(5, 8, 1, 8);
        getContext().focus(1, 8);
        getContext().move(1, 8, 1, 10);
        getContext().focus(13, 5);
        Message message3 = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_4_MESSAGE_4"));
        getContext().message(message3);
        getContext().focus(1, 1);
        Message message4 = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_4_MESSAGE_5"));
        getContext().message(message4);
        getContext().focus(7, 7);
        Message message5 = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_4_MESSAGE_6"));
        getContext().message(message5);

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
        checkClear();
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
        if(turn == 11)
        {
            Message message6 = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_4_MESSAGE_7"));
            getContext().message(message6);
        }
        else if(turn == 13)
        {
            for (Unit unit : getContext().get_units(0)) {
                getContext().attack(unit.getX(), unit.getY(), -1);
                getContext().hp_change(unit.getX(), unit.getY(), -20);
                if(unit.getCurrentHp() <= 20)
                {
                    getContext().destroy_unit(unit.getX(), unit.getY());
                }
            }
        }
        else if(turn % 20 == 13)
        {
            for (Unit unit : getContext().get_units(0)) {
                getContext().attack(unit.getX(), unit.getY(), -1);
                getContext().hp_change(unit.getX(), unit.getY(), -20);
                if(unit.getCurrentHp() <= 20)
                {
                    getContext().destroy_unit(unit.getX(), unit.getY());
                }
            }
        }
        else if(turn % 20 == 14)
        {
            getContext().reinforce(1,
                    new Reinforcement(1, 0, 0),
                    new Reinforcement(0, 0, 1),
                    new Reinforcement(0, 1, 0),
                    new Reinforcement(1, 0, 11),
                    new Reinforcement(0, 0, 10),
                    new Reinforcement(0, 1, 11),
                    new Reinforcement(1, 6, 0),
                    new Reinforcement(0, 7, 0),
                    new Reinforcement(0, 6, 1));
            Message message7 = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_4_MESSAGE_8"));
            getContext().message(message7);
        }
    }

    @Override
    public String getMapName() {
        return "warroom_stage_4.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        rule.setValue(Rule.Entry.UNIT_CAPACITY, 50);
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
                Language.getText("CAMPAIGN_WARROOM_STAGE_4_OBJECTIVE_1"),
        };
    }

    @Override
    public int getStageNumber() {
        return 3;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_WARROOM_STAGE_4_NAME");
    }


}

