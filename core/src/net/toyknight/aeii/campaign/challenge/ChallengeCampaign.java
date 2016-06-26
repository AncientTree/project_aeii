package net.toyknight.aeii.campaign.challenge;

import net.toyknight.aeii.campaign.CampaignController;
import net.toyknight.aeii.utils.Language;

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
        addStage(new ChallengeStage1());
    }

    @Override
    public String getCampaignName() {
        return Language.getText("CAMPAIGN_CHALLENGE_NAME");
    }

    @Override
    public int getDifficulty() {
        return 3;
    }

}
