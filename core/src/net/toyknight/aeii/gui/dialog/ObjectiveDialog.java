package net.toyknight.aeii.gui.dialog;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import net.toyknight.aeii.Callable;
import net.toyknight.aeii.entity.GameCore;
import net.toyknight.aeii.gui.GameScreen;
import net.toyknight.aeii.gui.StageScreen;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 6/17/2016.
 */
public class ObjectiveDialog extends BasicDialog {

    private final Table objective_pane;

    private Callable callable_ok;

    public ObjectiveDialog(StageScreen owner) {
        super(owner);

        setTopBottomBorderEnabled(true);

        Label label_title = new Label(AER.lang.getText("LB_OBJECTIVE"), getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(getResources().getWhiteColor(), this.getX(), this.getY(), this.getWidth(), 1);
                super.draw(batch, parentAlpha);
            }
        };
        label_title.setAlignment(Align.center);
        add(label_title).width(getOwner().getViewportWidth()).height(ts).padBottom(ts / 4).row();

        objective_pane = new Table();
        add(objective_pane).width(ts * 6).row();

        TextButton btn_ok = new TextButton(AER.lang.getText("LB_OK"), getContext().getSkin());
        btn_ok.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (callable_ok != null) {
                    callable_ok.call();
                }
            }
        });
        add(btn_ok).size(ts * 3, ts / 2).padBottom(ts / 4);
    }

    public GameScreen getOwner() {
        return (GameScreen) super.getOwner();
    }

    @Override
    public void display() {
        objective_pane.clearChildren();
        if (getContext().getGame().getType() == GameCore.CAMPAIGN) {
            for (String objective : getContext().getCampaignContext().getCurrentCampaign().getCurrentStage().getObjectives()) {
                Label label_objective = new Label(objective, getContext().getSkin());
                label_objective.setWrap(true);
                objective_pane.add(label_objective).width(getOwner().getViewportWidth() - ts).padBottom(ts / 4).row();
            }
        } else {
            Label label_objective_cu = new Label(AER.lang.getText("OBJECTIVE_CU"), getContext().getSkin());
            label_objective_cu.setWrap(true);
            Label label_objective_cc = new Label(AER.lang.getText("OBJECTIVE_CC"), getContext().getSkin());
            label_objective_cc.setWrap(true);
            objective_pane.add(label_objective_cu).width(getOwner().getViewportWidth() - ts).padBottom(ts / 4).row();
            objective_pane.add(label_objective_cc).width(getOwner().getViewportWidth() - ts).padBottom(ts / 4);
        }
        objective_pane.layout();
        layout();

        float width = getOwner().getViewportWidth();
        float height = getPrefHeight();
        setBounds(0, (getOwner().getViewportHeight() - height) / 2 + ts, width, height);
    }

    public void setOkCallable(Callable callable_ok) {
        this.callable_ok = callable_ok;
    }

}
