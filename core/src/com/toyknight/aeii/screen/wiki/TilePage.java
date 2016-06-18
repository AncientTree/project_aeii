package com.toyknight.aeii.screen.wiki;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Tile;
import com.toyknight.aeii.screen.widgets.PreviewFrame;
import com.toyknight.aeii.utils.Language;
import com.toyknight.aeii.utils.TileFactory;

/**
 * @author toyknight 6/17/2016.
 */
public class TilePage extends Table {

    private final int ts;

    private final Wiki wiki;

    private final PreviewFrame tile_preview;

    private final Label label_mp_cost_value;
    private final Label label_defence_bonus_value;
    private final Label label_hp_recovery_value;

    public TilePage(Wiki wiki) {
        super();
        this.wiki = wiki;
        this.ts = getWiki().getContext().getTileSize();

        tile_preview = new PreviewFrame(ts);
        tile_preview.setUnitIndex(-1);
        add(tile_preview).padBottom(ts / 8).row();

        Table data_pane = new Table() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(ResourceManager.getWhiteColor(), this.getX(), this.getY() + this.getHeight(), this.getWidth(), 1);
                super.draw(batch, parentAlpha);
            }
        };
        add(data_pane).width(ts * 7);

        Label label_mp_cost = new Label(Language.getText("LB_MOBILITY_COST") + ": ", getWiki().getContext().getSkin());
        data_pane.add(label_mp_cost).width(ts * 4).padTop(ts / 8);

        label_mp_cost_value = new Label("", getWiki().getContext().getSkin());
        data_pane.add(label_mp_cost_value).width(ts * 3).padTop(ts / 8).row();

        Label label_defence_bonus = new Label(Language.getText("LB_DEFENCE_BONUS") + ": ", getWiki().getContext().getSkin());
        data_pane.add(label_defence_bonus).width(ts * 4).padTop(ts / 8);

        label_defence_bonus_value = new Label("", getWiki().getContext().getSkin());
        data_pane.add(label_defence_bonus_value).width(ts * 3).padTop(ts / 8).row();

        Label label_hp_recovery = new Label(Language.getText("LB_HP_RECOVERY") + ": ", getWiki().getContext().getSkin());
        data_pane.add(label_hp_recovery).width(ts * 4).padTop(ts / 8);

        label_hp_recovery_value = new Label("", getWiki().getContext().getSkin());
        data_pane.add(label_hp_recovery_value).width(ts * 3).padTop(ts / 8).row();
    }

    public Wiki getWiki() {
        return wiki;
    }

    public void setIndex(int index) {
        tile_preview.setTileIndex(index);

        Tile tile = TileFactory.getTile(index);
        label_mp_cost_value.setText(Integer.toString(tile.getStepCost()));
        label_defence_bonus_value.setText(Integer.toString(tile.getDefenceBonus()));
        label_hp_recovery_value.setText(Integer.toString(tile.getHpRecovery()));
    }

}
