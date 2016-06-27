package net.toyknight.aeii.campaign.challenge;

import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Tile;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/27/2016.
 */
public class ChallengeStage2 extends StageController {

    @Override
    public void onGameStart() {
        Message message = new Message(5, Language.getText("CAMPAIGN_CHALLENGE_STAGE_2_MESSAGE_1"));
        getContext().message(message);
    }

    @Override
    public void onUnitMoved(Unit unit, int x, int y) {
    }

    @Override
    public void onUnitAttacked(Unit attacker, Unit defender) {
    }

    @Override
    public void onUnitDestroyed(Unit unit) {
        if (unit.getTeam() == getPlayerTeam() && unit.isCommander()) {
            getContext().fail();
        }
    }

    @Override
    public void onTileRepaired(int x, int y) {
    }

    @Override
    public void onTileOccupied(int x, int y, int team) {
        Tile castle1 = getContext().tile(12, 0);
        Tile castle2 = getContext().tile(14, 2);
        if (castle1.getTeam() == getPlayerTeam() && castle2.getTeam() == getPlayerTeam()) {
            getContext().clear();
        }
    }

    @Override
    public void onTurnStart(int turn) {
        if (turn > 31) {
            getContext().fail();
        }
    }

    @Override
    public void onTurnEnd(int turn) {
    }

    @Override
    public String getMapName() {
        return "challenge_stage_2.aem";
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
                Language.getText("CAMPAIGN_CHALLENGE_STAGE_2_OBJECTIVE_1"),
                Language.getText("CAMPAIGN_CHALLENGE_STAGE_2_OBJECTIVE_2"),
                Language.getText("CAMPAIGN_CHALLENGE_STAGE_2_OBJECTIVE_3")
        };
    }

    @Override
    public int getStageNumber() {
        return 1;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_CHALLENGE_STAGE_2_NAME");
    }

}
