package net.toyknight.aeii.campaign.challenge;

import net.toyknight.aeii.campaign.CampaignController;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 6/26/2016.
 */
public class ChallengeCampaign extends CampaignController {

    @Override
    public String getCode() {
        return "C_CH";
    }

    @Override
    public void initialize() {
        setOpen(true);
        addStage(new ChallengeStage1());
        addStage(new ChallengeStage2());
        addStage(new ChallengeStage3());
        addStage(new ChallengeStage4());
        addStage(new ChallengeStage5());
        addStage(new ChallengeStage6());
        addStage(new ChallengeStage7());
    }

    @Override
    public String getCampaignName() {
        return AER.lang.getText("CAMPAIGN_CHALLENGE_NAME");
    }

    @Override
    public int getDifficulty() {
        return 3;
    }

}
