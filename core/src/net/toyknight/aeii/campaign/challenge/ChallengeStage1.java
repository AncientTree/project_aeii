package net.toyknight.aeii.campaign.challenge;

import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/26/2016.
 */
public class ChallengeStage1 extends StageController {

    private void checkReinforcementPositions() {
        for (Unit unit : getContext().get_units(getPlayerTeam())) {
            int x = unit.getX();
            int y = unit.getY();
            if (x == 0 || x == 16 || y == 0 || y == 16) {
                getContext().team(x, y, 1);
            }
        }
    }

    @Override
    public void onGameStart() {
        Message message = new Message(5, Language.getText("CAMPAIGN_CHALLENGE_STAGE_1_MESSAGE_1"));
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
            if (getContext().count_unit(1) == 0) {
                getContext().clear();
            }
        }
    }

    @Override
    public void onTileRepaired(int x, int y) {
    }

    @Override
    public void onTileOccupied(int x, int y, int team) {
    }

    @Override
    public void onTurnStart(int turn) {
        checkReinforcementPositions();
        switch (turn) {
            case 2:
                getContext().reinforce(1, new Reinforcement(14, 0, 0));
                getContext().reinforce(1, new Reinforcement(14, 16, 0));
                getContext().reinforce(1, new Reinforcement(14, 0, 16));
                getContext().reinforce(1, new Reinforcement(14, 16, 16));
                break;
            case 4:
                getContext().reinforce(1,
                        new Reinforcement(1, 0, 7),
                        new Reinforcement(3, 0, 8),
                        new Reinforcement(1, 0, 9));
                getContext().reinforce(1,
                        new Reinforcement(1, 16, 7),
                        new Reinforcement(3, 16, 8),
                        new Reinforcement(1, 16, 9));
                break;
            case 6:
                getContext().reinforce(1, new Reinforcement(4, 0, 0));
                getContext().reinforce(1, new Reinforcement(4, 16, 0));
                getContext().reinforce(1, new Reinforcement(4, 0, 16));
                getContext().reinforce(1, new Reinforcement(4, 16, 16));
                break;
            case 8:
                getContext().reinforce(1,
                        new Reinforcement(15, 7, 0),
                        new Reinforcement(12, 8, 0),
                        new Reinforcement(15, 9, 0));
                getContext().reinforce(1,
                        new Reinforcement(15, 7, 16),
                        new Reinforcement(12, 8, 16),
                        new Reinforcement(15, 9, 16));
                break;
            case 10:
                getContext().reinforce(1, new Reinforcement(8, 0, 0));
                getContext().reinforce(1, new Reinforcement(8, 16, 0));
                getContext().reinforce(1, new Reinforcement(8, 0, 16));
                getContext().reinforce(1, new Reinforcement(8, 16, 16));
                break;
        }
    }

    @Override
    public String getMapName() {
        return "challenge_stage_1.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        rule.setValue(Rule.Entry.UNIT_CAPACITY, 30);
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
                Language.getText("CAMPAIGN_CHALLENGE_STAGE_1_OBJECTIVE_1"),
                Language.getText("CAMPAIGN_CHALLENGE_STAGE_1_OBJECTIVE_2"),
        };
    }

    @Override
    public int getStageNumber() {
        return 0;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_CHALLENGE_STAGE_1_NAME");
    }

}
