package com.toyknight.aeii.screen.editor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.screen.MapEditorScreen;
import com.toyknight.aeii.utils.Language;

/**
 * Created by toyknight on 7/9/2015.
 */
public class MapSaveDialog extends Table {

    private final int ts;
    private final MapEditorScreen editor;

    private TextField tf_filename;
    private TextField tf_author;

    public MapSaveDialog(MapEditorScreen editor) {
        this.editor = editor;
        this.ts = getContext().getTileSize();
        this.initComponents();
    }

    private void initComponents() {
        Label lb_filename = new Label(Language.getText("LB_FILENAME"), getContext().getSkin());
        this.add(lb_filename).width(ts * 5).align(Align.left).row();
        this.tf_filename = new TextField("", getContext().getSkin());
        this.add(tf_filename).width(ts * 5).row();

        Label lb_author = new Label(Language.getText("LB_AUTHOR"), getContext().getSkin());
        this.add(lb_author).width(ts * 5).align(Align.left).padTop(ts / 4).row();
        this.tf_author = new TextField("", getContext().getSkin());
        this.add(tf_author).width(ts * 5).row();

        TextButton btn_save = new TextButton(Language.getText("LB_SAVE"), getContext().getSkin());
        btn_save.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                editor.saveMap(tf_filename.getText(), tf_author.getText());
            }
        });
        this.add(btn_save).size(ts * 4, ts / 2).padTop(ts / 4);
    }

    private AEIIApplication getContext() {
        return editor.getContext();
    }

    public void display() {
        this.tf_filename.setText(editor.getFilename());
        this.tf_author.setText(editor.getMap().getAuthor());
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
