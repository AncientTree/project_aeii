package com.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.entity.Position;
import com.toyknight.aeii.entity.Tile;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.screen.StageScreen;

/**
 * @author toyknight 8/31/2015.
 */
public class MiniMapDialog extends BasicDialog {

    private final int sts;

    private final Button btn_close;

    private Map map;

    private float state_time;

    public MiniMapDialog(StageScreen owner) {
        super(owner);
        this.sts = ts / 24 * 10;
        this.btn_close = new Button(getContext().getSkin());
        this.addActor(btn_close);
    }

    public void addClickListener(ClickListener listener) {
        this.btn_close.addListener(listener);
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public Map getMap() {
        return map;
    }

    @Override
    public void display() {
        state_time = 0f;
    }

    public void update(float delta) {
        state_time += delta;
    }

    public void updateBounds(int parent_x, int parent_y, int parent_width, int parent_height) {
        int width = getMap().getWidth() * sts + 10;
        int height = getMap().getHeight() * sts + 10;
        this.setBounds((parent_width - width) / 2 + parent_x, (parent_height - height) / 2 + parent_y, width, height);
        this.btn_close.setBounds(0, 0, getWidth(), getHeight());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        float x = getX(), y = getY(), width = getWidth(), height = getHeight();
        batch.draw(ResourceManager.getBorderDarkColor(), x, y, width, height);
        batch.draw(ResourceManager.getBorderLightColor(), x + 1, y + 1, width - 2, height - 2);
        batch.draw(ResourceManager.getBorderDarkColor(), x + 4, y + 4, width - 8, height - 8);
        for (int map_x = 0; map_x < getMap().getWidth(); map_x++) {
            for (int map_y = 0; map_y < getMap().getHeight(); map_y++) {
                Tile tile = getMap().getTile(map_x, map_y);
                batch.draw(
                        ResourceManager.getSTileTexture(tile.getMiniMapIndex()),
                        x + map_x * sts + 5, y + height - 5 - map_y * sts - sts, sts, sts);
            }
        }

        ObjectMap.Keys<Position> unit_positions = getMap().getUnitPositionSet();
        for (Position position : unit_positions) {
            Unit unit = getMap().getUnit(position.x, position.y);
            if (((int) (state_time / 0.3f)) % 2 != 0) {
                batch.draw(
                        ResourceManager.getMiniIcon(unit.getTeam()),
                        x + unit.getX() * sts + 5, y + height - 5 - unit.getY() * sts - sts, sts, sts);
            }
        }
        batch.flush();
    }

}
