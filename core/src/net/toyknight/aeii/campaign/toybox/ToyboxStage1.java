package net.toyknight.aeii.campaign.toybox;

import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Ability;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;
import java.util.Random;
import java.lang.Math;

/**
 * @author blackwave 9/22/2016.
 */
public class ToyboxStage1 extends StageController {


    private void checkClear(){
        if (getContext().count_unit(0) == 0 && getContext().count_castle(0) == 0 || getContext().count_unit(1) >= 70) {
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

    private Reinforcement reinforce_trap(int index, int x, int y, int level){
        getContext().remove_tomb(x, y);
        if(getContext().get_unit(x, y) != null)
            if(getContext().get_unit(x, y).isCommander() == false) {
                getContext().gold(1, getContext().get_gold(1) + getContext().get_unit(x, y).getPrice());
                getContext().remove_unit(x, y);
            }
            else;
        else;
        Reinforcement unit = new Reinforcement(index, x, y);
        if(level != -1)
            unit.setLevel(level);
        return unit;
    }

    private void reinforceKing(){
        if(getContext().get("King") == 0)
        {
            getContext().reinforce(1,
                    reinforce_trap(9, 3, 3, -1));
            getContext().set("King", 1);
        }
    }

    private void nextWave(int wave)
    {
        int gold;
        int level = (wave - 1) / 10;
        if(level > 3)
            level = 3;
        clear_power();
        if(wave % 10 == 1)
        {
            getContext().set("CurrentHard", getContext().get("Hard"));
            getContext().set("Hard", getContext().get("Hard") + 3000);
        }
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
                    reinforce_trap(0, 2, 2, level),
                    reinforce_trap(0, 3, 2, level),
                    reinforce_trap(0, 4, 2, level),
                    reinforce_trap(1, 2, 1, level),
                    reinforce_trap(1, 3, 1, level),
                    reinforce_trap(1, 4, 1, level)
                );

                gold = 200;
                break;
            case 2:reinforceKing();
                getContext().reinforce(1,
                    reinforce_trap(18, 2, 2, level),
                    reinforce_trap(18, 3, 2, level),
                    reinforce_trap(18, 4, 2, level),
                    reinforce_trap(14, 2, 1, level),
                    reinforce_trap(14, 3, 1, level),
                    reinforce_trap(14, 4, 1, level)
                );
                gold = 275;
                break;
            case 3:reinforceKing();
                getContext().reinforce(1,
                    reinforce_trap(2, 2, 2, level),
                    reinforce_trap(2, 3, 2, level),
                    reinforce_trap(2, 4, 2, level),
                    reinforce_trap(3, 2, 1, level),
                    reinforce_trap(3, 3, 1, level),
                    reinforce_trap(3, 4, 1, level)
                );
                gold = 350;
                break;
            case 4:reinforceKing();
                getContext().reinforce(1,
                    reinforce_trap(12, 2, 2, level),
                    reinforce_trap(12, 3, 2, level),
                    reinforce_trap(12, 4, 2, level),
                    reinforce_trap(15, 2, 1, level),
                    reinforce_trap(15, 3, 1, level),
                    reinforce_trap(15, 4, 1, level)
                );
                gold = 400;
                break;
            case 5:reinforceKing();
                getContext().reinforce(1,
                    reinforce_trap(13, 2, 2, level),
                    reinforce_trap(13, 3, 2, level),
                    reinforce_trap(13, 4, 2, level),
                    reinforce_trap(4, 2, 1, level),
                    reinforce_trap(4, 3, 1, level),
                    reinforce_trap(4, 4, 1, level)
            );
                gold = 500;
                break;
            case 6:reinforceKing();
                getContext().reinforce(1,
                    reinforce_trap(5, 2, 2, level),
                    reinforce_trap(5, 3, 2, level),
                    reinforce_trap(5, 4, 2, level),
                    reinforce_trap(17, 2, 1, level),
                    reinforce_trap(17, 3, 1, level),
                    reinforce_trap(17, 4, 1, level)
            );
                gold = 600;
                break;
            case 7:reinforceKing();
                getContext().reinforce(1,
                    reinforce_trap(6, 2, 2, level),
                    reinforce_trap(6, 3, 2, level),
                    reinforce_trap(6, 4, 2, level),
                    reinforce_trap(7, 2, 1, level),
                    reinforce_trap(7, 3, 1, level),
                    reinforce_trap(7, 4, 1, level)
            );
                gold = 700;
                break;
            case 8:reinforceKing();
                getContext().reinforce(1,
                    reinforce_trap(16, 2, 2, level),
                    reinforce_trap(16, 3, 2, level),
                    reinforce_trap(16, 4, 2, level),
                    reinforce_trap(8, 2, 1, level),
                    reinforce_trap(8, 3, 1, level),
                    reinforce_trap(8, 4, 1, level)
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
                    reinforce_trap(11, 2, 2, 0),
                    reinforce_trap(11, 3, 2, 0),
                    reinforce_trap(11, 4, 2, 0),
                    reinforce_trap(11, 2, 1, 0),
                    reinforce_trap(11, 3, 1, 0),
                    reinforce_trap(11, 4, 1, 0)
            );
                gold = 0;
                break;
            default:gold = 0;break;
        }
        getContext().gold(0, getContext().get_gold(0) + gold);
        getContext().set("Wave", getContext().get("Wave") + 1);
        Message message_gold = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_6") + ' ' + gold);
        getContext().message(message_gold);
        if(wave % 10 == 9)
        {
            Message message_bone = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_12"));
            getContext().message(message_bone);
        }
        else if(wave % 10 == 0)
        {
            Message message_crystal = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_11"));
            getContext().message(message_crystal);
        }
        else;
        getContext().gold(1, getContext().get_gold(1) + getContext().get("CurrentHard") / 2);


        if(wave % 10 != 0 && wave % 10 != 9) {
            double prob = Math.log(((double)getContext().get("CurrentHard")) / 1000) / Math.log(2);
            Random random = new Random();
            int rand = random.nextInt(100);
            int skill = random.nextInt(9);
            int skill_level = 0;
            if (prob * 10 > rand) {
                if(rand > 50 && getContext().get_gold(1) > 10000)
                {
                    skill_level = 3;
                    getContext().gold(1, getContext().get_gold(1) - 10000);
                }
                else if(rand > 30 && getContext().get_gold(1) > 5000)
                {
                    skill_level = 2;
                    getContext().gold(1, getContext().get_gold(1) - 5000);
                }
                else if(getContext().get_gold(1) > 2000)
                {
                    skill_level = 1;
                    getContext().gold(1, getContext().get_gold(1) - 2000);
                }
            }
            if(skill_level > 0)
            {
                switch(skill){
                    case 0: heaven_fury(skill_level);break;
                    case 1: blizzard(skill_level);break;
                    case 2: hyper_upgrade(skill_level);break;
                    case 3: avatar(skill_level);break;
                    case 4: curse(skill_level);break;
                    case 5: poison(skill_level);break;
                    case 6: ghost(skill_level);break;
                    case 7: dragon(skill_level);break;
                    case 8: gold_rush(skill_level);break;
                    case 9: market_crash(skill_level);break;
                    default: break;
                }
            }
        }

    }

    void clear_power()
    {
        getContext().set("avatar", 0);
        getContext().set("curse", 0);
        getContext().set("poison", 0);
        getContext().set("ghost", 0);
    }

    void heaven_fury(int level)
    {
        Message message_power = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_14" + "Heaven Fury"));
        getContext().message(message_power);
        for(int i = 0;i < 5; i++)
        {
            if(level == 1)
            {
                getContext().havens_fury(0, -1, -1, 30);
            }
            else if(level == 2)
            {
                getContext().havens_fury(0, -1, -1, 60);
            }
            else
            {
                getContext().havens_fury(0, -1, -1, 99);
            }
        }
    }

    void blizzard(int level)
    {
        Message message_power = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_14" + "Blizzard"));
        getContext().message(message_power);
        int damage = -(10 + 10 * level);
        for (Unit unit : getContext().get_units(0)) {
            getContext().attack(unit.getX(), unit.getY(), -1);
            getContext().hp_change(unit.getX(), unit.getY(), damage);
            if(unit.getCurrentHp() <= -1 * damage)
            {
                getContext().destroy_unit(unit.getX(), unit.getY());
            }
        }
    }

    void hyper_upgrade(int level)
    {
        Message message_power = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_14" + "Hyper Upgrade"));
        getContext().message(message_power);
         for (Unit player : getContext().get_units(1)) {
             getContext().hp_change(player.getX(), player.getY(), player.getMaxHp() - player.getCurrentHp());
             for(int i = 0; i < level; i++) {
                 getContext().level_up(player.getX(), player.getY());
             }
        }
    }

    void avatar(int level)
    {
        Message message_power = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_14" + "Avatar"));
        getContext().message(message_power);
        getContext().set("avatar", level);
    }

    void curse(int level)
    {
        Message message_power = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_14" + "Curse"));
        getContext().message(message_power);
        getContext().set("curse", level);
    }

    void poison(int level)
    {
        Message message_power = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_14" + "Poison"));
        getContext().message(message_power);
        getContext().set("poison", level);
    }

    void ghost(int level)
    {
        Message message_power = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_14" + "Ghost"));
        getContext().message(message_power);
        getContext().set("ghost", level);
    }

    void dragon(int level)
    {
        Message message_power = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_14" + "Dragon"));
        getContext().message(message_power);
        for(int i = 9; i >= 10 - level; i--)
        {
            getContext().reinforce(1,
                    reinforce_trap(8, 2, i, level),
                    reinforce_trap(8, 3, i, level),
                    reinforce_trap(8, 4, i, level));
        }
    }

    void gold_rush(int level)
    {
        Message message_power = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_14" + "Gold Rush"));
        getContext().message(message_power);
        if(level == 1)
        {
            getContext().gold(1, getContext().get_gold(1) * 2);
        }
        else if(level == 2)
        {
            getContext().gold(1, getContext().get_gold(1) * 5);
        }
        else if(level == 3)
        {
            getContext().gold(1, getContext().get_gold(1) * 10);
        }
    }

    void market_crash(int level)
    {
        Message message_power = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_14" + "Market Crash"));
        getContext().message(message_power);
        if(level == 1)
        {
            getContext().gold(0, getContext().get_gold(0) / 2);
        }
        else if(level == 2)
        {
            getContext().gold(0, getContext().get_gold(0) / 5);
        }
        else if(level == 3)
        {
            getContext().gold(0, getContext().get_gold(0) / 10);
        }
    }

    @Override
    public void onGameStart() {
        getContext().gold(0, 2000);
        clear_power();
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
        getContext().set("Hard", 1000);
        getContext().set("CurrentHard", 0);


    }


    @Override
    public void onUnitMoved(Unit unit, int x, int y) {
    }

    @Override
    public void onUnitStandby(Unit unit) {
        if(unit.getTeam() == 1 && unit.isCommander() && getContext().get("avatar") > 2 && getContext().get("Secondmove") > 0)
        {
            unit.setStandby(false);
            unit.resetMovementPoint();
            getContext().set("Secondmove", 0);
        }
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
        if(getContext().get("ghost") > 0)
        {
            if (!unit.hasAbility(Ability.UNDEAD) && !unit.isCommander()) {
                getContext().remove_tomb(unit.getX(), unit.getY());
                getContext().create(14, 1, unit.getX(), unit.getY());
            }
            for(int i = 0; i < getContext().get("ghost"); i++)
            {
                getContext().level_up(unit.getX(), unit.getY());
            }
        }
        if(isCommander(unit, 1))
        {
            getContext().set("King", 0);
        }
        if(unit.getTeam() == 1)
        {
            getContext().set("Gold", getContext().get("Gold") + unit.getPrice());
            if(getContext().get("avatar") > 1)
            {
                if(unit.isCommander())
                {
                    getContext().reinforce(1, new Reinforcement(9, unit.getX(), unit.getY()));
                }
            }
        }
        if(unit.getTeam() == 0)
        {
            getContext().set("Hard", (int)(getContext().get("Hard") - unit.getPrice() * 0.25));
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
        getContext().set("Secondmove", 1);
        checkClear();
        if(turn == 2)
        {
            getContext().set("Time", turn);
            nextWave(getContext().get("Wave"));
        }
        else if(turn % 2 == 0){
            int remain_turn = getContext().get("Time") + 10 - turn;
            if((turn % 2 == 0 && checkClean() == true) || (turn % 2 == 0 && remain_turn <= 0))
            {
                if(remain_turn / 2 > 0) {
                    Message message_bonus = new Message(5, Language.getText("CAMPAIGN_TOYBOX_STAGE_1_MESSAGE_7") + ' ' + remain_turn);
                    getContext().message(message_bonus);
                    getContext().gold(0, getContext().get_gold(0) + (10 - turn +  getContext().get("Time"))  * 50);
                    getContext().set("Time", turn);
                    nextWave(getContext().get("Wave"));
                    getContext().set("Hard", getContext().get("Hard") + (10 - turn + getContext().get("Time") * 25));
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
        else{
            if(getContext().get("avatar") > 0)
            {
                for (Unit unit : getContext().get_units(1)) {
                    if(unit.isCommander())
                    {
                        getContext().hp_change(unit.getX(), unit.getY(), unit.getMaxHp() - unit.getCurrentHp());
                    }
                }
            }
            if(getContext().get("curse") > 0 && turn % (4 - getContext().get("curse")) == 0)
            {
                blasted_land();
            }
            if(getContext().get("poison") > 0)
            {
                int damage = -(5 + 5 * getContext().get("poison"));
                for (Unit unit : getContext().get_units(0)) {
                    getContext().attack(unit.getX(), unit.getY(), -1);
                    getContext().hp_change(unit.getX(), unit.getY(), damage);
                    if(unit.getCurrentHp() <= -1 * damage)
                    {
                        getContext().destroy_unit(unit.getX(), unit.getY());
                    }
                }
            }
        }
    }

    void blasted_land()
    {
        if (getContext().current_team() == 0) {
            for (Unit unit : getContext().get_units(0)) {
                if (unit.getCurrentHp() == unit.getMaxHp()) {
                    if(unit.getIndex() != 9  && unit.getIndex() != 10)
                    {
                        getContext().remove_unit(unit.getX(), unit.getY());
                        Random random = new Random();
                        int ran = random.nextInt(6);
                        switch (unit.getIndex()) {
                            case 0:
                                getContext().reinforce(0,
                                        new Reinforcement(10, unit.getX(), unit.getY()));
                                break;
                            case 1:
                                getContext().reinforce(0,
                                        new Reinforcement(0, unit.getX(), unit.getY()));
                                break;
                            case 2:
                                getContext().reinforce(0,
                                        new Reinforcement(1, unit.getX(), unit.getY()));
                                break;
                            case 3:
                                getContext().reinforce(0,
                                        new Reinforcement(14, unit.getX(), unit.getY()));
                                break;
                            case 4:
                                if (ran % 2 == 0) {
                                    getContext().reinforce(0,
                                            new Reinforcement(3, unit.getX(), unit.getY()));
                                    break;
                                } else {
                                    getContext().reinforce(0,
                                            new Reinforcement(15, unit.getX(), unit.getY()));
                                    break;
                                }
                            case 5:
                                getContext().reinforce(0,
                                        new Reinforcement(13, unit.getX(), unit.getY()));
                                break;
                            case 6:
                                getContext().reinforce(0,
                                        new Reinforcement(13, unit.getX(), unit.getY()));
                                break;
                            case 7:
                                getContext().reinforce(0,
                                        new Reinforcement(16, unit.getX(), unit.getY()));
                                break;
                            case 8:
                                getContext().reinforce(0,
                                        new Reinforcement(7, unit.getX(), unit.getY()));
                                break;
                            case 9:
                                break;
                            case 10:
                                break;
                            case 11:
                                break;
                            case 12:
                                getContext().reinforce(0,
                                        new Reinforcement(2, unit.getX(), unit.getY()));
                                break;
                            case 13:
                                getContext().reinforce(0,
                                        new Reinforcement(12, unit.getX(), unit.getY()));
                                break;
                            case 14:
                                getContext().reinforce(0,
                                        new Reinforcement(18, unit.getX(), unit.getY()));
                                break;
                            case 15:
                                getContext().reinforce(0,
                                        new Reinforcement(14, unit.getX(), unit.getY()));
                                break;
                            case 16:
                                if (ran % 3 == 0) {
                                    getContext().reinforce(0,
                                            new Reinforcement(5, unit.getX(), unit.getY()));
                                    break;
                                } else if (ran % 3 == 1) {
                                    getContext().reinforce(0,
                                            new Reinforcement(6, unit.getX(), unit.getY()));
                                    break;
                                } else if (ran % 3 == 2) {
                                    getContext().reinforce(0,
                                            new Reinforcement(17, unit.getX(), unit.getY()));
                                    break;
                                }
                            case 17:
                                getContext().reinforce(0,
                                        new Reinforcement(4, unit.getX(), unit.getY()));
                                break;
                            case 18:
                                getContext().reinforce(0,
                                        new Reinforcement(0, unit.getX(), unit.getY()));
                                break;
                            default:
                                break;
                        }
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
        rule.setValue(Rule.Entry.UNIT_CAPACITY, 30);
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


