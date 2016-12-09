package net.toyknight.aeii.campaign.challenge;

import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.system.AER;

import java.util.Random;

/**
 * @author liuji_000 9/19/2016.
 */

public class ChallengeStage6 extends StageController {

    private void checkClear() {
        if (getContext().count_unit(1) == 0 && getContext().count_castle(1) == 0 && getContext().count_unit(2) == 0 && getContext().count_castle(2) == 0) {
            getContext().clear();
        }
    }

    @Override
    public void onGameStart() {
        getContext().alliance(1, 1);
        getContext().alliance(2, 1);
        Message message = new Message(5, AER.lang.getText("CAMPAIGN_CHALLENGE_STAGE_6_MESSAGE_1"));
        getContext().message(message);
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
        } else {
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
        if (getContext().current_team() == 0) {
            for (Unit unit : getContext().get_units(0)) {
                if (unit.getCurrentHP() == unit.getMaxHP()) {
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

    @Override
    public String getMapName() {
        return "challenge_stage_6.aem";
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
                AER.lang.getText("CAMPAIGN_CHALLENGE_STAGE_6_OBJECTIVE_1"),
        };
    }

    @Override
    public int getStageNumber() {
        return 5;
    }

    @Override
    public String getStageName() {
        return AER.lang.getText("CAMPAIGN_CHALLENGE_STAGE_6_NAME");
    }

}


