package net.toyknight.aeii.campaign.aeiii;

import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 6/28/2016.
 */
public class AEIIIStage1 extends StageController {



    @Override
    public void onGameStart() {
        getContext().level_up(3, 15);
        getContext().level_up(3, 16);
        Message message1 = new Message(5, AER.lang.getText("CAMPAIGN_AEIII_STAGE_1_MESSAGE_1"));
        getContext().message(message1);
        getContext().focus(7, 9);
        Message message2 = new Message(5, AER.lang.getText("CAMPAIGN_AEIII_STAGE_1_MESSAGE_2"));
        getContext().message(message2);
        Message message3 = new Message(4, AER.lang.getText("CAMPAIGN_AEIII_STAGE_1_MESSAGE_3"));
        getContext().message(message3);
        getContext().move(7,9,7,6);
        getContext().focus(8, 6);
        getContext().move(7,6,2,6);
        getContext().focus(1, 6);
        getContext().move(2,6,6,0);
        getContext().focus(6, 0);
        Message message4 = new Message(4, AER.lang.getText("CAMPAIGN_AEIII_STAGE_1_MESSAGE_4"));
        getContext().message(message4);
        getContext().remove_unit(1, 6);
        getContext().remove_unit(8, 6);
        getContext().focus(7, 9);
        getContext().move(8,9,7,9);
        getContext().level_up(7, 9);
        getContext().level_up(7, 9);
        getContext().reinforce(3,
                new Reinforcement(15, 6, 9),
                new Reinforcement(15, 8, 9),
                new Reinforcement(0, 6, 10),
                new Reinforcement(0, 8, 10),
                new Reinforcement(1, 7, 10));
        getContext().level_up(6, 9);
        getContext().level_up(8, 9);
        Message message5 = new Message(3, AER.lang.getText("CAMPAIGN_AEIII_STAGE_1_MESSAGE_5"));
        getContext().message(message5);
        getContext().remove_unit(6, 0);
        getContext().set("Youxing", 0);
        getContext().set("Reinforce", 0);
    }

    @Override
    public void onUnitMoved(Unit unit, int x, int y) {
    }

    @Override
    public void onUnitStandby(Unit unit) {
        if(unit.isCommander() && unit.getTeam() == getPlayerTeam() && unit.getY() == 0)
            getContext().clear();
        else if(unit.getTeam() == getPlayerTeam() && unit.getY() <= 7 && getContext().get("Reinforce") == 0)
        {
            getContext().set("Reinforce", 1);
            if(unit.getX() <= 4)
            {
                reinforce_element();
            }
            else reinforce_wolf();
        }
    }

    private void reinforce_element()
    {
        getContext().reinforce(3,
                new Reinforcement(4, 0, 2),
                new Reinforcement(15, 0, 3),
                new Reinforcement(15, 1, 2),
                new Reinforcement(2, 2, 2),
                new Reinforcement(2, 1, 3),
                new Reinforcement(2, 0, 4));
        getContext().level_up(0, 3);
        getContext().level_up(1, 2);
        Message message9 = new Message(3, AER.lang.getText("CAMPAIGN_AEIII_STAGE_1_MESSAGE_9"));
        getContext().message(message9);
    }

    private void reinforce_wolf()
    {
        getContext().reinforce(3,
                new Reinforcement(6, 9, 3),
                new Reinforcement(15, 8, 3),
                new Reinforcement(15, 9, 4),
                new Reinforcement(5, 8, 4));
        getContext().level_up(8, 3);
        getContext().level_up(9, 4);
        Message message10 = new Message(3, AER.lang.getText("CAMPAIGN_AEIII_STAGE_1_MESSAGE_10"));
        getContext().message(message10);
    }

    @Override
    public void onUnitAttacked(Unit attacker, Unit defender) {

    }

    @Override
    public void onUnitDestroyed(Unit unit) {
        if (unit.getTeam() == getPlayerTeam() && unit.isCommander()) {
            getContext().fail();
        }
        else if(getContext().count_unit(3) == 0 && getContext().get("Youxing") == 0)
        {
            Message message6 = new Message(5, AER.lang.getText("CAMPAIGN_AEIII_STAGE_1_MESSAGE_6"));
            getContext().message(message6);
            getContext().set("Youxing", 1);
        }
        else if(getContext().count_unit(3) == 0 && getContext().get("Reinforce") == 1)
        {
            getContext().clear();
        }
    }

    @Override
    public void onTileRepaired(int x, int y) {
    }

    @Override
    public void onTileOccupied(int x, int y, int team) {
        if(x == 7 && y == 9 && team == getPlayerTeam())
        {
            Message message7 = new Message(5, AER.lang.getText("CAMPAIGN_AEIII_STAGE_1_MESSAGE_7"));
            getContext().message(message7);
            Message message8 = new Message(3, AER.lang.getText("CAMPAIGN_AEIII_STAGE_1_MESSAGE_8"));
            getContext().message(message8);
        }
    }

    @Override
    public void onTurnStart(int turn) {

    }


    @Override
    public String getMapName() {
        return "aeiii_c1.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        rule.setValue(Rule.Entry.COMMANDER_INCOME, 0);
        rule.setValue(Rule.Entry.VILLAGE_INCOME, 0);
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
        return new String[]{AER.lang.getText("CAMPAIGN_AEIII_STAGE_1_OBJECTIVE")};
    }

    @Override
    public int getStageNumber() {
        return 0;
    }

    @Override
    public String getStageName() {
        return AER.lang.getText("CAMPAIGN_AEIII_STAGE_1_NAME");
    }

}
