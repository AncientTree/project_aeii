package com.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.toyknight.aeii.AEIIApplication;

/**
 * Created by toyknight on 6/7/2015.
 */
public class SaveLoadDialog extends Table {

    public static final int MODE_SAVE = 0x1;
    public static final int MODE_LOAD = 0x2;

    private final int ts;
    private final int SLOT_WIDTH;
    private final int SLOT_HEIGHT;
    private final int RIGHT_PANEL_WIDTH;
    private final AEIIApplication context;

    private int mode;
//    private TextButton btn_action;
//    private TextButton btn_cancel;

    public SaveLoadDialog(AEIIApplication context, Rectangle parent_bounds) {
        this.context = context;
        this.ts = getContext().getTileSize();
        this.SLOT_WIDTH = ts * 3;
        this.SLOT_HEIGHT = ts * 2;
        this.RIGHT_PANEL_WIDTH = ts * 5;
        this.initComponents(parent_bounds);
    }

    private void initComponents(Rectangle parent_bounds) {
        int width = SLOT_WIDTH + RIGHT_PANEL_WIDTH;
        int height = SLOT_HEIGHT * 3;
        this.setBounds(
                parent_bounds.x + (parent_bounds.width - width) / 2,
                parent_bounds.y + (parent_bounds.height - height) / 2,
                width, height);
//        this.btn_action = new TextButton("", getContext().getSkin());
//        this.btn_action.setBounds(0, 0, BTN_WIDTH, BTN_HEIGHT);
//        this.addActor(btn_action);
//
//        this.btn_cancel = new TextButton(Language.getText("LB_CANCEL"), getContext().getSkin());
//        this.btn_cancel.setBounds(BTN_WIDTH, 0, BTN_WIDTH, BTN_HEIGHT);
//        this.btn_cancel.addListener(new ClickListener() {
//            @Override
//            public void clicked(InputEvent event, float x, float y) {
//                SaveLoadDialog.this.setVisible(false);
//            }
//        });
//        this.addActor(btn_cancel);
    }

    private AEIIApplication getContext() {
        return context;
    }

    public void display(int mode) {
        this.mode = mode;
//        switch (mode) {
//            case MODE_SAVE:
//                btn_action.setText(Language.getText("LB_SAVE"));
//                break;
//            case MODE_LOAD:
//                btn_action.setText(Language.getText("LB_LOAD"));
//                break;
//        }
        this.setVisible(true);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = getX(), y = getY(), width = getWidth(), height = getHeight();
//        batch.draw(ResourceManager.getPanelBackground(), x, y, width, TOP_BAR_HEIGHT + SLOT_HEIGHT);
//        BorderRenderer.drawBorder(batch, x, y + SLOT_HEIGHT, width, TOP_BAR_HEIGHT);
//        BorderRenderer.drawBorder(batch, x, y, width, SLOT_HEIGHT);
        batch.flush();
        super.draw(batch, parentAlpha);
    }

}
