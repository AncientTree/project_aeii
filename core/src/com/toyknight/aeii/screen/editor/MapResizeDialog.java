package com.toyknight.aeii.screen.editor;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.manager.MapEditor;
import com.toyknight.aeii.screen.StageScreen;
import com.toyknight.aeii.screen.dialog.BasicDialog;
import com.toyknight.aeii.utils.Language;

/**
 * @author toyknight 7/9/2015.
 */
public class MapResizeDialog extends BasicDialog {

    private final MapEditor editor;

    private Label lb_width;
    private Slider slider_width;
    private Label lb_height;
    private Slider slider_height;

    public MapResizeDialog(StageScreen owner, MapEditor editor) {
        super(owner);
        this.editor = editor;
        this.initComponents();
    }

    private void initComponents() {
        this.lb_width = new Label(Language.getText("LB_WIDTH"), getContext().getSkin());
        this.add(lb_width).row();
        this.slider_width = new Slider(5, 25, 1, false, getContext().getSkin());
        this.slider_width.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onSizeChanged();
            }
        });
        this.add(slider_width).size(ts * 7, ts).row();

        this.lb_height = new Label(Language.getText("LB_HEIGHT"), getContext().getSkin());
        this.add(lb_height).row();
        this.slider_height = new Slider(5, 25, 1, false, getContext().getSkin());
        this.slider_height.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onSizeChanged();
            }
        });
        this.add(slider_height).size(ts * 7, ts).row();

        Table button_bar = new Table();
        TextButton btn_confirm = new TextButton(Language.getText("LB_CONFIRM"), getContext().getSkin());
        btn_confirm.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Map map = getEditor().createEmptyMap((int) slider_width.getValue(), (int) slider_height.getValue());
                getEditor().setMap(map, "not defined");
                getOwner().closeDialog("resize");
            }
        });
        button_bar.add(btn_confirm).size(ts * 3, ts);
        TextButton btn_cancel = new TextButton(Language.getText("LB_CANCEL"), getContext().getSkin());
        btn_cancel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getOwner().closeDialog("resize");
            }
        });
        button_bar.add(btn_cancel).size(ts * 3, ts).padLeft(ts / 2);
        add(button_bar).size(ts * 7, ts);
    }

    private void onSizeChanged() {
        lb_width.setText(Language.getText("LB_WIDTH") + ": " + (int) slider_width.getValue());
        lb_height.setText(Language.getText("LB_HEIGHT") + ": " + (int) slider_height.getValue());
        this.layout();
    }

    public MapEditor getEditor() {
        return editor;
    }

    @Override
    public void display() {
        slider_width.setValue(getEditor().getMap().getWidth());
        slider_height.setValue(getEditor().getMap().getHeight());
    }

}
