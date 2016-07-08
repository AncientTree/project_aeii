package net.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import net.toyknight.aeii.ResourceManager;
import net.toyknight.aeii.screen.StageScreen;

/**
 * @author toyknight 6/24/2016.
 */
public class CampaignMessageDialog extends BasicDialog {

    private final Label label_message;

    public CampaignMessageDialog(StageScreen owner, int width) {
        super(owner);
        setTopBottomBorderEnabled(true);
        setBounds(0, 0, width, ts * 2);

        label_message = new Label("", getContext().getSkin());
        label_message.setWrap(true);
        label_message.setAlignment(Align.center);
        label_message.setBounds(ts * 85 / 24, 0, width - ts * 85 / 24, ts * 2);
        label_message.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                nextMessage();
            }
        });
        addActor(label_message);
    }

    private void nextMessage() {
        if (getContext().getGameManager().nextCampaignMessage()) {
            updateMessage();
        } else {
            getOwner().closeDialog("campaign_message");
        }
    }

    private void updateMessage() {
        label_message.setText(getContext().getGameManager().getCurrentCampaignMessage().getMessage());
    }

    @Override
    public void display() {
        updateMessage();
    }

    @Override
    protected void drawCustom(Batch batch, float parentAlpha) {
        int portrait = getContext().getGameManager().getCurrentCampaignMessage().getPortrait();
        if (portrait >= 0) {
            batch.draw(getResources().getPortraitTexture(portrait), 0, 5, ts * 85 / 24, ts * 85 / 24);
        }
    }

}
