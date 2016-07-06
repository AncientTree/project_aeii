package net.toyknight.aeii.campaign.challenge;

import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Ability;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 7/3/2016.
 */
public class ChallengeStage4 extends StageController {

    private void checkClear() {
        if (getContext().count_unit(0) == 0 && getContext().count_unit(2) == 0 && getContext().count_castle(0) == 0) {
            getContext().clear();
        }
    }

    @Override
    public void onGameStart() {
        getContext().head(0, 1);
        getContext().head(1, 0);
        Message message = new Message(5, Language.getText("CAMPAIGN_CHALLENGE_STAGE_4_MESSAGE_1"));
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
            if (!unit.hasAbility(Ability.UNDEAD) && !unit.isCommander()) {
                getContext().restore(2);
                getContext().remove_tomb(unit.getX(), unit.getY());
                getContext().create(14, 2, unit.getX(), unit.getY());
            }
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
    }

    @Override
    public void onTurnEnd(int turn) {
        if (getContext().count_unit(2) == 0) {
            getContext().destroy_team(2);

        }
    }

    @Override
    public String getMapName() {
        return "challenge_stage_4.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        rule.setValue(Rule.Entry.MAX_POPULATION, 30);
        return rule;
    }

    @Override
    public int getStartGold() {
        return 500;
    }

    @Override
    public int getPlayerTeam() {
        return 1;
    }

    @Override
    public String[] getObjectives() {
        return new String[]{
                Language.getText("CAMPAIGN_CHALLENGE_STAGE_4_OBJECTIVE_1"),
                Language.getText("CAMPAIGN_CHALLENGE_STAGE_4_OBJECTIVE_2")
        };
    }

    @Override
    public int getStageNumber() {
        return 3;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_CHALLENGE_STAGE_4_NAME");
    }

}
