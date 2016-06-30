package net.toyknight.aeii.campaign.aeii;

import net.toyknight.aeii.campaign.CampaignController;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/28/2016.
 */
public class AEIICampaign extends CampaignController {

    @Override
    public String getCode() {
        return "C_AEII";
    }

    @Override
    public void initialize() {
        addStage(new AEIIStage1());
        addStage(new AEIIStage2());
        addStage(new AEIIStage3());
        addStage(new AEIIStage4());
        addStage(new AEIIStage5());
        addStage(new AEIIStage6());
        addStage(new AEIIStage7());
        addStage(new AEIIStage8());
    }

    @Override
    public String getCampaignName() {
        return Language.getText("CAMPAIGN_AEII_NAME");
    }

    @Override
    public int getDifficulty() {
        return 1;
    }

}
