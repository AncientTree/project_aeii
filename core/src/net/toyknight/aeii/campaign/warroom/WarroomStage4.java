package net.toyknight.aeii.campaign.warroom;

import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

import java.util.Random;

/**
 * Created by liuji_000 on 8/27/2016.
 */
public class WarroomStage4 extends StageController {



    private void checkClear() {
        if (getContext().count_unit(1) == 0 && getContext().count_castle(1) == 0) {
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
                new Reinforcement(0, 5, 0),
                new Reinforcement(0, 6, 1));
        getContext().attack(5 ,1, -1);
        getContext().destroy_unit(5, 1);
        getContext().reinforce(0,
                new Reinforcement(13, 1, 1),
                new Reinforcement(13, 6, 1));
        Message message2 = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_4_MESSAGE_3"));
        getContext().message(message2);
        getContext().havens_fury(0, 1, 1, 0);
        getContext().havens_fury(0, 6, 1, 0);
        Message message3 = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_4_MESSAGE_4"));
        getContext().message(message3);
        getContext().move(5, 6, 5, 1);
        getContext().move(5, 7, 1, 1);
        getContext().move(5, 8, 1, 10);
        Message message4 = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_4_MESSAGE_5"));
        getContext().message(message4);

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
        if(turn == 20)
        {

            Message message5 = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_4_MESSAGE_6"));
            getContext().message(message5);
        }
        else if(turn % 20 == 0)
        {
            Message message6 = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_4_MESSAGE_7"));
            getContext().message(message6);
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
        return 500;
    }

    @Override
    public int getPlayerTeam() {
        return 0;
    }

    @Override
    public String[] getObjectives() {
        return new String[]{
                Language.getText("CAMPAIGN_WARROOM_STAGE_4_OBJECTIVE_1"),
                Language.getText("CAMPAIGN_WARROOM_STAGE_4_OBJECTIVE_2"),
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

