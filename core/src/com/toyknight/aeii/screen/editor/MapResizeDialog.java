package com.toyknight.aeii.screen.editor;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.manager.MapEditor;
import com.toyknight.aeii.screen.StageScreen;
import com.toyknight.aeii.screen.dialog.BasicDialog;
import com.toyknight.aeii.screen.widgets.NumberSpinner;
import com.toyknight.aeii.screen.widgets.Spinner;
import com.toyknight.aeii.screen.widgets.SpinnerListener;
import com.toyknight.aeii.utils.Language;

/**
 * @author toyknight 7/9/2015.
 */
public class MapResizeDialog extends BasicDialog {

    private final MapEditor editor;

    private NumberSpinner spinner_width;
    private NumberSpinner spinner_height;

    public MapResizeDialog(StageScreen owner, MapEditor editor) {
        super(owner);
        this.editor = editor;
        this.initComponents();
    }

    private void initComponents() {
        Label lb_width = new Label(Language.getText("LB_WIDTH"), getContext().getSkin());
        lb_width.setAlignment(Align.center);
        this.add(lb_width).size(ts * 3, ts);
        this.spinner_width = new NumberSpinner(5, 25, ts, getContext().getSkin());
        this.add(spinner_width).size(ts * 3, ts).padLeft(ts / 2).row();

        Label lb_height = new Label(Language.getText("LB_HEIGHT"), getContext().getSkin());
        lb_height.setAlignment(Align.center);
        this.add(lb_height).size(ts * 3, ts).padTop(ts / 4);
        this.spinner_height = new NumberSpinner(5, 25, ts, getContext().getSkin());
        this.add(spinner_height).size(ts * 3, ts).padLeft(ts / 2).padTop(ts / 4).row();

        TextButton btn_confirm = new TextButton(Language.getText("LB_CONFIRM"), getContext().getSkin());
        btn_confirm.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getEditor().resizeMap(spinner_width.getSelectedItem(), spinner_height.getSelectedItem());
                getOwner().closeDialog("resize");
            }
        });
        this.add(btn_confirm).size(ts * 3, ts).padTop(ts / 4);
        TextButton btn_cancel = new TextButton(Language.getText("LB_CANCEL"), getContext().getSkin());
        btn_cancel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getOwner().closeDialog("resize");
            }
        });
        this.add(btn_cancel).size(ts * 3, ts).padLeft(ts / 2).padTop(ts / 4);
    }

    public MapEditor getEditor() {
        return editor;
    }

    @Override
    public void display() {
        spinner_width.setSelectedIndex(getEditor().getMap().getWidth() - 5);
        spinner_height.setSelectedIndex(getEditor().getMap().getHeight() - 5);
    }

}
