package com.toyknight.aeii.screen.editor;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.toyknight.aeii.manager.MapEditor;
import com.toyknight.aeii.screen.StageScreen;
import com.toyknight.aeii.screen.dialog.BasicDialog;
import com.toyknight.aeii.utils.Language;

/**
 * @author toyknight 7/9/2015.
 */
public class MapSaveDialog extends BasicDialog {

    private final MapEditor editor;

    private TextField tf_filename;
    private TextField tf_author;

    public MapSaveDialog(StageScreen owner, MapEditor editor) {
        super(owner);
        this.editor = editor;
        initComponents();
    }

    private void initComponents() {
        Label lb_filename = new Label(Language.getText("LB_FILENAME"), getContext().getSkin());
        this.add(lb_filename).width(ts * 5).align(Align.left).row();
        this.tf_filename = new TextField("", getContext().getSkin());
        this.tf_filename.setMaxLength(20);
        this.add(tf_filename).width(ts * 5).row();

        Label lb_author = new Label(Language.getText("LB_AUTHOR"), getContext().getSkin());
        this.add(lb_author).width(ts * 5).align(Align.left).padTop(ts / 4).row();
        this.tf_author = new TextField("", getContext().getSkin());
        this.tf_author.setMaxLength(10);
        this.add(tf_author).width(ts * 5).row();

        Table button_bar = new Table();
        TextButton btn_save = new TextButton(Language.getText("LB_SAVE"), getContext().getSkin());
        btn_save.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getEditor().saveMap(tf_filename.getText(), tf_author.getText());
            }
        });
        button_bar.add(btn_save).size(ts * 2, ts);
        TextButton btn_cancel = new TextButton(Language.getText("LB_CANCEL"), getContext().getSkin());
        btn_cancel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getOwner().closeDialog("save");
            }
        });
        button_bar.add(btn_cancel).size(ts * 2, ts).padLeft(ts / 2);
        add(button_bar).size(ts * 5, ts).padTop(ts / 4);
    }

    public MapEditor getEditor() {
        return editor;
    }

    @Override
    public void display() {
        this.tf_filename.setText(getEditor().getFilename());
        this.tf_author.setText(getEditor().getMap().getAuthor());
        this.tf_author.setTouchable(tf_author.getText().equals("default") ? Touchable.enabled : Touchable.disabled);
    }

}
