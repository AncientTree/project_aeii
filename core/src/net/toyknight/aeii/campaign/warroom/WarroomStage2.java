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
        getContext().alliance(3, 1);
        getContext().focus(7, 1);
        Message message = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_2_MESSAGE_1"));
        getContext().message(message);
        getContext().focus(1, 9);
        Message message1 = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_2_MESSAGE_2"));
        getContext().message(message1);
        getContext().focus(13, 13);
        Message message2 = new Message(5, Language.getText("CAMPAIGN_WARROOM_STAGE_2_MESSAGE_3"));
        getContext().message(message2);
    }

    @Override
    public void onUnitMoved(Unit unit, int x, int y) {
    }

    @Override
    public void onUnitStandby(Unit unit) {
        if(unit.getX() > 11 && unit.getY() > 4 && unit.getY() < 10 && unit.getTeam() == 1)
        {
            Random rand = new Random();
            int randomNum = rand.nextInt((24));
            int randomUnit = rand.nextInt((18));
            switch (randomNum)
            {
                case 0: if(getContext().get_unit(10, 2) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 10, 2));
                    break;
                }
                else randomNum++;
                case 1: if(getContext().get_unit(11, 2) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 11, 2));
                    break;
                }
                else randomNum++;
                case 2: if(getContext().get_unit(9, 3) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 9, 3));
                    break;
                }
                else randomNum++;
                case 3: if(getContext().get_unit(10, 3) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 10, 3));
                    break;
                }
                else randomNum++;
                case 4: if(getContext().get_unit(11, 3) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 11, 3));
                    break;
                }
                else randomNum++;
                case 5: if(getContext().get_unit(12, 3) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 12, 3));
                    break;
                }
                else randomNum++;
                case 6: if(getContext().get_unit(12, 4) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 12, 4));
                    break;
                }
                else randomNum++;
                case 7: if(getContext().get_unit(13, 5) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 13, 5));
                    break;
                }
                else randomNum++;
                case 8: if(getContext().get_unit(14, 5) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 14, 5));
                    break;
                }
                else randomNum++;
                case 9: if(getContext().get_unit(12, 6) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 12, 6));
                    break;
                }
                else randomNum++;
                case 10: if(getContext().get_unit(13, 6) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 13, 6));
                    break;
                }
                else randomNum++;
                case 11: if(getContext().get_unit(14, 6) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 14, 6));
                    break;
                }
                else randomNum++;
                case 12: if(getContext().get_unit(12, 7) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 12, 7));
                    break;
                }
                else randomNum++;
                case 13: if(getContext().get_unit(13, 7) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 13, 7));
                    break;
                }
                else randomNum++;
                case 14: if(getContext().get_unit(14, 7) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 14, 7));
                    break;
                }
                else randomNum++;
                case 15: if(getContext().get_unit(10, 8) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 10, 8));
                    break;
                }
                else randomNum++;
                case 16: if(getContext().get_unit(11, 8) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 11, 8));
                    break;
                }
                else randomNum++;
                case 17: if(getContext().get_unit(12, 8) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 12, 8));
                    break;
                }
                else randomNum++;
                case 18: if(getContext().get_unit(13, 8) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 13, 8));
                    break;
                }
                else randomNum++;
                case 19: if(getContext().get_unit(14, 8) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 14, 8));
                    break;
                }
                else randomNum++;
                case 20: if(getContext().get_unit(11, 9) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 11, 9));
                    break;
                }
                else randomNum++;
                case 21: if(getContext().get_unit(12, 9) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 12, 9));
                    break;
                }
                else randomNum++;
                case 22: if(getContext().get_unit(13, 9) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 13, 9));
                    break;
                }
                else randomNum++;
                case 23: if(getContext().get_unit(14, 9) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 14, 9));
                    break;
                }
                else break;
                default: break;
            }

        }
    }

    @Override
    public void onUnitAttacked(Unit attacker, Unit defender) {
        if(attacker.getX() > 11 && attacker.getY() > 4 && attacker.getY() < 10 && attacker.getTeam() == 1)
        {
            Random rand = new Random();
            int randomNum = rand.nextInt((24));
            int randomUnit = rand.nextInt((18));
            switch (randomNum)
            {
                case 0: if(getContext().get_unit(10, 2) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 10, 2));
                    break;
                }
                else;
                case 1: if(getContext().get_unit(11, 2) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 11, 2));
                    break;
                }
                else;
                case 2: if(getContext().get_unit(9, 3) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 9, 3));
                    break;
                }
                else;
                case 3: if(getContext().get_unit(10, 3) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 10, 3));
                    break;
                }
                else;
                case 4: if(getContext().get_unit(11, 3) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 11, 3));
                    break;
                }
                else;
                case 5: if(getContext().get_unit(12, 3) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 12, 3));
                    break;
                }
                else;
                case 6: if(getContext().get_unit(12, 4) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 12, 4));
                    break;
                }
                else;
                case 7: if(getContext().get_unit(13, 5) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 13, 5));
                    break;
                }
                else;
                case 8: if(getContext().get_unit(14, 5) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 14, 5));
                    break;
                }
                else;
                case 9: if(getContext().get_unit(12, 6) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 12, 6));
                    break;
                }
                else;
                case 10: if(getContext().get_unit(13, 6) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 13, 6));
                    break;
                }
                else;
                case 11: if(getContext().get_unit(14, 6) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 14, 6));
                    break;
                }
                else;
                case 12: if(getContext().get_unit(12, 7) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 12, 7));
                    break;
                }
                else;
                case 13: if(getContext().get_unit(13, 7) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 13, 7));
                    break;
                }
                else;
                case 14: if(getContext().get_unit(14, 7) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 14, 7));
                    break;
                }
                else;
                case 15: if(getContext().get_unit(10, 8) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 10, 8));
                    break;
                }
                else;
                case 16: if(getContext().get_unit(11, 8) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 11, 8));
                    break;
                }
                else;
                case 17: if(getContext().get_unit(12, 8) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 12, 8));
                    break;
                }
                else;
                case 18: if(getContext().get_unit(13, 8) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 13, 8));
                    break;
                }
                else;
                case 19: if(getContext().get_unit(14, 8) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 14, 8));
                    break;
                }
                else;
                case 20: if(getContext().get_unit(11, 9) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 11, 9));
                    break;
                }
                else;
                case 21: if(getContext().get_unit(12, 9) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 12, 9));
                    break;
                }
                else;
                case 22: if(getContext().get_unit(13, 9) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 13, 9));
                    break;
                }
                else;
                case 23: if(getContext().get_unit(14, 9) == null)
                {
                    getContext().reinforce(2,
                            new Reinforcement(randomUnit, 14, 9));
                    break;
                }
                else break;
                default: break;
            }

        }
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
            else ;
            if (getContext().count_unit(3) == 0 && getContext().count_castle(3) == 0) {
                getContext().destroy_team(3);
            }
            else ;
            if(unit.getTeam() == 3) {
                getContext().gold(3, (getContext().get_gold(3) + unit.getPrice()));
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
        if(turn % 4 == 3) {
            getContext().reinforce(2,
                    new Reinforcement(1, 6, 8),
                    new Reinforcement(1, 5, 9),
                    new Reinforcement(1, 7, 9),
                    new Reinforcement(1, 6, 10));
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
