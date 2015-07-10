package com.toyknight.aeii.screen.editor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.screen.MapEditorScreen;
import com.toyknight.aeii.utils.Language;

/**
 * Created by toyknight on 7/9/2015.
 */
public class MapResizeDialog extends Table {

    private final int ts;
    private final MapEditorScreen editor;

    private Label lb_width;
    private Slider slider_width;
    private Label lb_height;
    private Slider slider_height;

    public MapResizeDialog(MapEditorScreen editor) {
        this.editor = editor;
        this.ts = getContext().getTileSize();
        this.initComponents();
    }

    private void initComponents() {
        this.lb_width = new Label(Language.getText("LB_WIDTH"), getContext().getSkin());
        this.add(lb_width).row();
        this.slider_width = new Slider(5, 30, 1, false, getContext().getSkin());
        this.slider_width.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onSizeChanged();
            }
        });
        this.add(slider_width).size(ts * 7, ts).row();

        this.lb_height = new Label(Language.getText("LB_HEIGHT"), getContext().getSkin());
        this.add(lb_height).row();
        this.slider_height = new Slider(5, 30, 1, false, getContext().getSkin());
        this.slider_height.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onSizeChanged();
            }
        });
        this.add(slider_height).size(ts * 7, ts).row();

        TextButton btn_confirm = new TextButton(Language.getText("LB_CONFIRM"), getContext().getSkin());
        btn_confirm.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                editor.createEmptyMap((int) slider_width.getValue(), (int) slider_height.getValue());
                setVisible(false);
            }
        });
        this.add(btn_confirm).size(ts *3, ts / 2);
    }

    private AEIIApplication getContext() {
        return editor.getContext();
    }

    private void onSizeChanged() {
        lb_width.setText(Language.getText("LB_WIDTH") + ": " + (int) slider_width.getValue());
        lb_height.setText(Language.getText("LB_HEIGHT") + ": " + (int) slider_height.getValue());
        this.layout();
    }

    public void display() {
        slider_width.setValue(editor.getMap().getWidth());
        slider_height.setValue(editor.getMap().getHeight());
        this.setVisible(true);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(ResourceManager.getPanelBackground(), getX(), getY(), getWidth(), getHeight());
        super.draw(batch, parentAlpha);
        BorderRenderer.drawBorder(batch, getX(), getY(), getWidth(), getHeight());
        batch.flush();
    }

}
