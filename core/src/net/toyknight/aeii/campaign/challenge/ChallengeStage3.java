package net.toyknight.aeii.campaign.challenge;

import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/27/2016.
 */
public class ChallengeStage3 extends StageController {

    @Override
    public void onGameStart() {
//        getContext().gold(0, 0);
        getContext().alliance(0, 0);
        getContext().alliance(1, 1);
        getContext().alliance(2, 1);
        getContext().alliance(3, 1);
        Message message = new Message(5, Language.getText("CAMPAIGN_CHALLENGE_STAGE_3_MESSAGE_1"));
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
        if (unit.getTeam() == getPlayerTeam() && unit.isCommander()) {
            getContext().fail();
        }
        if (getContext().count_unit(2) == 0) {
            getContext().destroy_team(2);
        }
    }

    @Override
    public void onTileRepaired(int x, int y) {
    }

    @Override
    public void onTileOccupied(int x, int y, int team) {
        if (getContext().tile(2, 0).getTeam() == getPlayerTeam() &&
                getContext().tile(3, 0).getTeam() == getPlayerTeam() &&
                getContext().tile(8, 0).getTeam() == getPlayerTeam() &&
                getContext().tile(9, 0).getTeam() == getPlayerTeam() &&
                getContext().tile(4, 6).getTeam() == getPlayerTeam()) {
            getContext().clear();
        } else {
            if (getContext().tile(x, y).isCastle() && team == getPlayerTeam() && y == 6) {
                boolean reinforced = getContext().reinforce(2,
                        new Reinforcement(8, 11, 5),
                        new Reinforcement(8, 11, 6),
                        new Reinforcement(8, 11, 7));
                if (reinforced) getContext().restore(2);
            }
            if (getContext().tile(x, y).isCastle() && team == getPlayerTeam() && y == 0) {
                boolean reinforced = getContext().reinforce(2,
                        new Reinforcement(8, 10, 3),
                        new Reinforcement(8, 11, 2),
                        new Reinforcement(8, 11, 3),
                        new Reinforcement(8, 11, 4));
                if (reinforced) getContext().restore(2);
            }
            if (getContext().tile(x, y).isVillage() && team == getPlayerTeam() && y <= 2) {
                boolean reinforced = getContext().reinforce(2,
                        new Reinforcement(8, 1, 8),
                        new Reinforcement(8, 3, 9),
                        new Reinforcement(8, 5, 9),
                        new Reinforcement(8, 7, 8));
                if (reinforced) getContext().restore(2);
            }
        }
    }

    @Override
    public void onTurnStart(int turn) {
    }

    @Override
    public String getMapName() {
        return "challenge_stage_3.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        rule.setValue(Rule.Entry.UNIT_CAPACITY, 25);
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
                Language.getText("CAMPAIGN_CHALLENGE_STAGE_3_OBJECTIVE_1"),
                Language.getText("CAMPAIGN_CHALLENGE_STAGE_3_OBJECTIVE_2")
        };
    }

    @Override
    public int getStageNumber() {
        return 2;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_CHALLENGE_STAGE_3_NAME");
    }

}
