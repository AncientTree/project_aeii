package net.toyknight.aeii.campaign.tutorial;

import net.toyknight.aeii.campaign.CampaignController;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 6/25/2016.
 */
public class TutorialCampaign extends CampaignController {

    @Override
    public String getCode() {
        return "C_TU";
    }

    @Override
    public void initialize() {
        addStage(new TutorialStage1()).setRanking(false);
        addStage(new TutorialStage2()).setRanking(false);
        addStage(new TutorialStage3()).setRanking(false);
    }

    @Override
    public String getCampaignName() {
        return AER.lang.getText("CAMPAIGN_TUTORIAL_NAME");
    }

    @Override
    public int getDifficulty() {
        return 0;
    }

}
