package net.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import net.toyknight.aeii.AEIIException;
import net.toyknight.aeii.AudioManager;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.concurrent.AsyncTask;
import net.toyknight.aeii.network.NetworkManager;
import net.toyknight.aeii.screen.StageScreen;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 9/20/2016.
 */
public class RankingClearDialog extends BasicDialog {

    private final Label label_clear_turns;
    private final Label label_clear_actions;

    private final TextButton btn_upload;

    public RankingClearDialog(StageScreen owner) {
        super(owner);
        Label label_stage_clear = new Label(Language.getText("LB_STAGE_CLEAR"), getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(getResources().getWhiteColor(), this.getX(), this.getY(), this.getWidth(), 1);
                batch.draw(getResources().getWhiteColor(), this.getX(), this.getY() + this.getHeight(), this.getWidth(), 1);
                super.draw(batch, parentAlpha);
            }
        };
        label_stage_clear.setAlignment(Align.center);
        add(label_stage_clear).size(ts * 4 + ts / 2, ts).pad(ts / 4).padBottom(0).row();
        label_clear_turns = new Label("", getContext().getSkin());
        add(label_clear_turns).width(ts * 4 + ts / 2).pad(ts / 4).row();
        label_clear_actions = new Label("", getContext().getSkin());
        add(label_clear_actions).width(ts * 4 + ts / 2).pad(ts / 4).padTop(0).row();

        Table button_pane = new Table();
        add(button_pane).size(ts * 4 + ts / 2, ts).padBottom(ts / 4);
        TextButton btn_leave = new TextButton(Language.getText("LB_LEAVE"), getContext().getSkin());
        btn_leave.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                leave();
            }
        });
        button_pane.add(btn_leave).size(ts * 2, ts).padRight(ts / 2);
        btn_upload = new TextButton(Language.getText("LB_UPLOAD"), getContext().getSkin());
        btn_upload.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                upload();
            }
        });
        button_pane.add(btn_upload).size(ts * 2, ts);

        pack();
        setPosition((Gdx.graphics.getWidth() - getWidth()) / 2, (Gdx.graphics.getHeight() - getHeight()) / 2);
    }

    private void leave() {
        getContext().onCampaignNextStage();
        getContext().gotoCampaignScreen();
        AudioManager.loopMainTheme();
    }

    private void upload() {
        getOwner().showPlaceholder(Language.getText("LB_UPLOADING"));
        getContext().submitAsyncTask(new AsyncTask<Void>() {
            @Override
            public Void doTask() throws Exception {
                if (NetworkManager.connect(GameContext.CAMPAIGN_SERVER)) {
                    NetworkManager.submitRecord(
                            getContext().getVerificationString(),
                            getContext().getUsername(),
                            getContext().getCampaignContext().getCurrentCampaign().getCode(),
                            getContext().getCampaignContext().getCurrentCampaign().getCurrentStage().getStageNumber(),
                            getContext().getGame().getCurrentTurn(),
                            getContext().getGame().getStatistics().getActions());
                } else {
                    throw new AEIIException(Language.getText("MSG_ERR_CCS"));
                }
                return null;
            }

            @Override
            public void onFinish(Void result) {
                NetworkManager.disconnect();
                btn_upload.setVisible(false);
                getOwner().closePlaceholder();
                getOwner().showNotification(Language.getText("LB_UPLOADED"), null);
            }

            @Override
            public void onFail(String message) {
                NetworkManager.disconnect();
                getOwner().closePlaceholder();
                getOwner().showNotification(message, null);
            }
        });
    }

    @Override
    public void display() {
        label_clear_turns.setText(Language.getText("LB_TURNS") + ": " + getContext().getGame().getCurrentTurn());
        label_clear_actions.setText(
                Language.getText("LB_ACTIONS") + ": " + getContext().getGame().getStatistics().getActions());
        btn_upload.setVisible(true);
    }

}
