package net.toyknight.aeii.campaign.tutorial;

import net.toyknight.aeii.campaign.CampaignController;
import net.toyknight.aeii.utils.Language;

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
        setRanking(false);
        addStage(new TutorialStage1());
        addStage(new TutorialStage2());
        addStage(new TutorialStage3());
    }

    @Override
    public String getCampaignName() {
        return Language.getText("CAMPAIGN_TUTORIAL_NAME");
    }

    @Override
    public int getDifficulty() {
        return 0;
    }

}
