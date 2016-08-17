package net.toyknight.aeii.campaign.challenge;

import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Ability;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author by toyknight 8/2/2016.
 */
public class ChallengeStage5 extends StageController {

    private void checkClear() {
        if (getContext().count_unit(3) == 0 && getContext().count_unit(1) == 0 && getContext().count_castle(3) == 0) {
            getContext().clear();
        }
    }


    @Override
    public void onGameStart() {
        getContext().alliance(1, 0);
        getContext().alliance(3, 0);
        Message message = new Message(5, Language.getText("CAMPAIGN_CHALLENGE_STAGE_5_MESSAGE_1"));
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
            if (unit.getX() >= 8 && !unit.hasAbility(Ability.UNDEAD) && unit.getIndex() != 6) {
                getContext().restore(1);
                getContext().remove_tomb(unit.getX(), unit.getY());
                getContext().create(6, 1, unit.getX(), unit.getY());
            }
            if (getContext().count_unit(1) == 0) {
                getContext().destroy_team(1);
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
        if (turn > 200) {
            getContext().fail();
        } else {
            if (turn % 10 == 0) {
                getContext().restore(1);
                getContext().reinforce(1,
                        new Reinforcement(7, 14, 3),
                        new Reinforcement(7, 14, 4));
            }
        }
    }

    @Override
    public String getMapName() {
        return "challenge_stage_5.aem";
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
        return 2;
    }

    @Override
    public String[] getObjectives() {
        return new String[]{
                Language.getText("CAMPAIGN_CHALLENGE_STAGE_5_OBJECTIVE_1"),
                Language.getText("CAMPAIGN_CHALLENGE_STAGE_5_OBJECTIVE_2"),
                Language.getText("CAMPAIGN_CHALLENGE_STAGE_5_OBJECTIVE_3")
        };
    }

    @Override
    public int getStageNumber() {
        return 4;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_CHALLENGE_STAGE_5_NAME");
    }

}
