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
        switch (turn) {
            case 2:
                getContext().reinforce(1,
                        new Reinforcement(4, 0, 1),
                        new Reinforcement(2, 0, 0),
                        new Reinforcement(4, 1, 0));
                getContext().reinforce(1,
                        new Reinforcement(4, 15, 16),
                        new Reinforcement(2, 16, 16),
                        new Reinforcement(4, 16, 15));
                break;
            case 4:
                getContext().reinforce(1,
                        new Reinforcement(13, 0, 7),
                        new Reinforcement(3, 0, 8),
                        new Reinforcement(13, 0, 9));
                getContext().reinforce(1,
                        new Reinforcement(13, 16, 7),
                        new Reinforcement(3, 16, 8),
                        new Reinforcement(13, 16, 9));
                break;
            case 6:
                getContext().reinforce(1,
                        new Reinforcement(5, 7, 0),
                        new Reinforcement(6, 8, 0),
                        new Reinforcement(5, 9, 0));
                getContext().reinforce(1,
                        new Reinforcement(5, 7, 16),
                        new Reinforcement(6, 8, 16),
                        new Reinforcement(5, 9, 16));
                break;
            case 8:
                getContext().reinforce(1,
                        new Reinforcement(16, 0, 7),
                        new Reinforcement(8, 0, 8),
                        new Reinforcement(16, 0, 9));
                getContext().reinforce(1,
                        new Reinforcement(16, 16, 7),
                        new Reinforcement(8, 16, 8),
                        new Reinforcement(16, 16, 9));
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
