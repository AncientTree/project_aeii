package com.toyknight.aeii.screen.internal;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.screen.GameScreen;
import com.toyknight.aeii.utils.Language;

/**
 * Created by toyknight on 4/20/2015.
 */
public class UnitStore extends Table {

    private final int ts;
    private final GameScreen screen;

    private TextButton btn_buy;

    public UnitStore(final GameScreen screen, Skin skin) {
        this.screen = screen;
        this.ts = screen.getContext().getTileSize();
        this.initComponents(skin);
    }

    private void initComponents(Skin skin) {
        this.btn_buy = new TextButton(Language.getText("LB_BUY"), skin);
        this.btn_buy.setBounds(ts * 6, ts / 2, ts * 2, ts / 2);
        this.addActor(btn_buy);
        TextButton btn_close = new TextButton(Language.getText("LB_CLOSE"), skin);
        btn_close.setBounds(ts * 8 + ts / 2, ts / 2, ts * 2, ts / 2);
        btn_close.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                close();
            }
        });
        this.addActor(btn_close);
    }

    public void close() {
        this.setVisible(false);
        screen.onButtonUpdateRequested();
    }

    public void draw(Batch batch, float parentAlpha) {
        batch.draw(ResourceManager.getPanelBackground(), getX(), getY(), getWidth(), getHeight());
        BorderRenderer.drawBorder(batch, getX(), getY(), getWidth(), getHeight());
        batch.flush();
        super.draw(batch, parentAlpha);
    }

}
