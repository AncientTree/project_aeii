package net.toyknight.aeii.campaign.toybox;

import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Ability;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;
import java.util.Random;

/**
 * @author blackwave 9/22/2016.
 */
public class ToyboxStage1 extends StageController {


    private void checkClear(){
        if (getContext().count_unit(0) == 0 && getContext().count_castle(0) == 0) {
            Message message_end = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_8") + ' ' + (getContext().get("Wave") - 1) + ' ' + Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_9")
                    + ' ' + getContext().get("Gold") + ' ' + Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_10"));
            getContext().message(message_end);
            getContext().clear();
        }
    }

    private boolean checkClean(){
        if(getContext().count_unit(1) == 0)
            return true;
        return false;
    }

    private Reinforcement reinforce_trap(int index, int x, int y){
        if(getContext().get_unit(x, y) != null)
            if(getContext().get_unit(x, y).isCommander() == false) {
                getContext().gold(1, getContext().get_gold(1) + getContext().get_unit(x, y).getPrice());
                getContext().destroy_unit(x, y);
                getContext().remove_tomb(x, y);
            }
        return new Reinforcement(index, x, y);
    }

    private void reinforceKing(){
        if(getContext().get("King") == 0)
        {
            getContext().reinforce(1,
                    reinforce_trap(9, 3, 3));
            getContext().set("King", 1);
        }
    }

    private void nextWave(int wave)
    {
        int gold;
        Message message3 = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_4") + ' ' + wave + ' ' + Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_5"));
        getContext().message(message3);
        switch(wave % 10){
            case 1:
                for (Unit unit : getContext().get_units(1)) {
                    if(unit.isCrystal())
                    {
                        getContext().destroy_unit(unit.getX(), unit.getY());
                        getContext().remove_tomb(unit.getX(), unit.getY());
                    }
                }
                reinforceKing();
                getContext().reinforce(1,
                    reinforce_trap(0, 2, 2),
                    reinforce_trap(0, 3, 2),
                    reinforce_trap(0, 4, 2),
                    reinforce_trap(1, 2, 1),
                    reinforce_trap(1, 3, 1),
                    reinforce_trap(1, 4, 1)
                );

                gold = 200;
                break;
            case 2:reinforceKing();
                getContext().reinforce(1,
                    reinforce_trap(18, 2, 2),
                    reinforce_trap(18, 3, 2),
                    reinforce_trap(18, 4, 2),
                    reinforce_trap(14, 2, 1),
                    reinforce_trap(14, 3, 1),
                    reinforce_trap(14, 4, 1)
                );
                gold = 275;
                break;
            case 3:reinforceKing();
                getContext().reinforce(1,
                    reinforce_trap(2, 2, 2),
                    reinforce_trap(2, 3, 2),
                    reinforce_trap(2, 4, 2),
                    reinforce_trap(3, 2, 1),
                    reinforce_trap(3, 3, 1),
                    reinforce_trap(3, 4, 1)
                );
                gold = 350;
                break;
            case 4:reinforceKing();
                getContext().reinforce(1,
                    reinforce_trap(12, 2, 2),
                    reinforce_trap(12, 3, 2),
                    reinforce_trap(12, 4, 2),
                    reinforce_trap(15, 2, 1),
                    reinforce_trap(15, 3, 1),
                    reinforce_trap(15, 4, 1)
                );
                gold = 400;
                break;
            case 5:reinforceKing();
                getContext().reinforce(1,
                    reinforce_trap(13, 2, 2),
                    reinforce_trap(13, 3, 2),
                    reinforce_trap(13, 4, 2),
                    reinforce_trap(4, 2, 1),
                    reinforce_trap(4, 3, 1),
                    reinforce_trap(4, 4, 1)
            );
                gold = 500;
                break;
            case 6:reinforceKing();
                getContext().reinforce(1,
                    reinforce_trap(5, 2, 2),
                    reinforce_trap(5, 3, 2),
                    reinforce_trap(5, 4, 2),
                    reinforce_trap(17, 2, 1),
                    reinforce_trap(17, 3, 1),
                    reinforce_trap(17, 4, 1)
            );
                gold = 600;
                break;
            case 7:reinforceKing();
                getContext().reinforce(1,
                    reinforce_trap(6, 2, 2),
                    reinforce_trap(6, 3, 2),
                    reinforce_trap(6, 4, 2),
                    reinforce_trap(7, 2, 1),
                    reinforce_trap(7, 3, 1),
                    reinforce_trap(7, 4, 1)
            );
                gold = 700;
                break;
            case 8:reinforceKing();
                getContext().reinforce(1,
                    reinforce_trap(16, 2, 2),
                    reinforce_trap(16, 3, 2),
                    reinforce_trap(16, 4, 2),
                    reinforce_trap(8, 2, 1),
                    reinforce_trap(8, 3, 1),
                    reinforce_trap(8, 4, 1)
            );
                gold = 850;
                break;
            case 9:reinforceKing();
                for(int x = 0; x < 7; x++)
                {
                    for(int y = 0; y < 7; y++)
                    {
                        if(x != 3 || y != 3) {
                            getContext().reinforce(1,
                                    new Reinforcement(10, x, y));
                        }
                    }
                }
                gold = 0;
                break;
            case 0:reinforceKing();
                getContext().reinforce(1,
                    reinforce_trap(11, 2, 2),
                    reinforce_trap(11, 3, 2),
                    reinforce_trap(11, 4, 2),
                    reinforce_trap(11, 2, 1),
                    reinforce_trap(11, 3, 1),
                    reinforce_trap(11, 4, 1)
            );
                gold = 0;
                break;
            default:gold = 0;break;
        }
        getContext().gold(0, getContext().get_gold(0) + gold);
        getContext().set("Wave", getContext().get("Wave") + 1);
        Message message_gold = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_6") + ' ' + gold);
        getContext().message(message_gold);
        if(wave % 10 != 9 && wave % 10 != 0) {
            for (int i = 0; i < (wave - 2) / 10 && i < 3; i++) {
                getContext().level_up(2, 2);
                getContext().level_up(3, 2);
                getContext().level_up(4, 2);
                getContext().level_up(2, 1);
                getContext().level_up(3, 1);
                getContext().level_up(4, 1);
            }
        }
        else if(wave % 10 == 9)
        {
            for (Unit unit : getContext().get_units(1)) {
                if(unit.getIndex() == 10)
                    getContext().level_up(unit.getX(), unit.getY());
            }
            Message message_bone = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_12"));
            getContext().message(message_bone);
        }
        else if(wave % 10 == 0)
        {
            Message message_crystal = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_11"));
            getContext().message(message_crystal);
        }
        else;
        if(wave > 30)
        {
            getContext().gold(1, getContext().get_gold(1) + ((wave - 20) / 10) * 1500);
        }
    }

    @Override
    public void onGameStart() {
        getContext().gold(0, 1500);
        Message message = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_1"));
        getContext().message(message);
        Message message1 = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_2"));
        getContext().message(message1);
        Message message2 = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_3"));
        getContext().message(message2);
        getContext().set("Wave", 1 );
        getContext().set("Time", 0);
        getContext().set("King", 1);
        getContext().set("Gold", 0);


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
        checkClear();
        if(isCrystal(unit, 1))
        {
            getContext().remove_tomb(unit.getX(), unit.getY());
            Random random = new Random();
            int rand = random.nextInt(100);
            if(rand == 0)
            {
                Message message_nothing = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_13"));
                getContext().message(message_nothing);
            }
            else if(rand <= 20)
            {
                getContext().reinforce(0, new Reinforcement(10, unit.getX(), unit.getY()));
            }
            else if(rand <= 35)
            {
                getContext().reinforce(0, new Reinforcement(0, unit.getX(), unit.getY()));
            }
            else if(rand <= 45)
            {
                getContext().reinforce(0, new Reinforcement(1, unit.getX(), unit.getY()));
            }
            else if(rand <= 55)
            {
                getContext().reinforce(0, new Reinforcement(18, unit.getX(), unit.getY()));
            }
            else if(rand <= 62)
            {
                getContext().reinforce(0, new Reinforcement(2, unit.getX(), unit.getY()));
            }
            else if(rand <= 69)
            {
                getContext().reinforce(0, new Reinforcement(14, unit.getX(), unit.getY()));
            }
            else if(rand <= 74)
            {
                getContext().reinforce(0, new Reinforcement(3, unit.getX(), unit.getY()));
            }
            else if(rand <= 79)
            {
                getContext().reinforce(0, new Reinforcement(12, unit.getX(), unit.getY()));
            }
            else if(rand <= 84)
            {
                getContext().reinforce(0, new Reinforcement(15, unit.getX(), unit.getY()));
            }
            else if(rand <= 87)
            {
                getContext().reinforce(0, new Reinforcement(4, unit.getX(), unit.getY()));
            }
            else if(rand <= 90)
            {
                getContext().reinforce(0, new Reinforcement(13, unit.getX(), unit.getY()));
            }
            else if(rand <= 92)
            {
                getContext().reinforce(0, new Reinforcement(5, unit.getX(), unit.getY()));
            }
            else if(rand <= 94)
            {
                getContext().reinforce(0, new Reinforcement(6, unit.getX(), unit.getY()));
            }
            else if(rand <= 96)
            {
                getContext().reinforce(0, new Reinforcement(17, unit.getX(), unit.getY()));
            }
            else if(rand <= 97)
            {
                getContext().reinforce(0, new Reinforcement(16, unit.getX(), unit.getY()));
            }
            else if(rand <= 98)
            {
                getContext().reinforce(0, new Reinforcement(7, unit.getX(), unit.getY()));
            }
            else if(rand <= 99)
            {
                getContext().reinforce(0, new Reinforcement(8, unit.getX(), unit.getY()));
            }
            else if(rand == 25)
            {
                getContext().reinforce(1, new Reinforcement(11, unit.getX(), unit.getY()));
            }
        }
        if(isCommander(unit, 1))
        {
            getContext().set("King", 0);
        }
        if(unit.getTeam() == 1)
        {
            getContext().set("Gold", getContext().get("Gold") + unit.getPrice());
        }
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
        if(turn == 2)
        {
            getContext().set("Time", turn);
            nextWave(getContext().get("Wave"));
        }
        else {
            int remain_turn = getContext().get("Time") + 10 - turn;
            if((turn % 2 == 0 && checkClean() == true) || (turn % 2 == 0 && remain_turn <= 0))
            {
                if(remain_turn / 2 > 0) {
                    Message message_bonus = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_7") + ' ' + remain_turn);
                    getContext().message(message_bonus);
                    getContext().gold(0, getContext().get_gold(0) + (10 - turn +  getContext().get("Time"))  * 50);
                    getContext().set("Time", turn);
                    nextWave(getContext().get("Wave"));
                }
                else {
                    if(getContext().get("Wave") % 10 == 0) {
                        getContext().set("Wave", getContext().get("Wave") + 1);
                        getContext().set("Time", turn);
                        nextWave(getContext().get("Wave"));
                    }
                    else
                    {
                        getContext().set("Time", turn);
                        nextWave(getContext().get("Wave"));
                    }
                }
            }
        }
    }


    public void onTurnEnd(int turn) {
    }

    @Override
    public String getMapName() {
        return "toybox_stage_1.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        rule.setValue(Rule.Entry.CASTLE_INCOME, 0);
        rule.setValue(Rule.Entry.VILLAGE_INCOME, 0);
        rule.setValue(Rule.Entry.COMMANDER_INCOME, 0);
        rule.setValue(Rule.Entry.UNIT_CAPACITY, 50);
        rule.getAvailableUnits().removeValue(9, false);
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
                Language.getText("CAMPAIGN_TOYBOX_STAGE_1_OBJECTIVE_1"),
                Language.getText("CAMPAIGN_TOYBOX_STAGE_1_OBJECTIVE_2"),
        };
    }

    @Override
    public int getStageNumber() {
        return 0;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_TOYBOX_STAGE_1_NAME");
    }

}


