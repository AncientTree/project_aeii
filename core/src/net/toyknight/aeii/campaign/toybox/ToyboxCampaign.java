package net.toyknight.aeii.campaign.toybox;

import net.toyknight.aeii.campaign.CampaignController;
import net.toyknight.aeii.utils.Language;

/**
 * @author blackwave 08/25/2016.
 */
public class ToyboxCampaign extends CampaignController {


    @Override
    public String getCode() {
        return "C_TB";
    }

    @Override
    public void initialize() {
        setOpen(true);
        addStage(new ToyboxStage1());

    }

    @Override
    public String getCampaignName() {
        return Language.getText("CAMPAIGN_TOYBOX_NAME");
    }

    @Override
    public int getDifficulty() {
        return 1;
    }
}
